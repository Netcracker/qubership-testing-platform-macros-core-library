/*
 * # Copyright 2024-2025 NetCracker Technology Corporation
 * #
 * # Licensed under the Apache License, Version 2.0 (the "License");
 * # you may not use this file except in compliance with the License.
 * # You may obtain a copy of the License at
 * #
 * #      http://www.apache.org/licenses/LICENSE-2.0
 * #
 * # Unless required by applicable law or agreed to in writing, software
 * # distributed under the License is distributed on an "AS IS" BASIS,
 * # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * # See the License for the specific language governing permissions and
 * # limitations under the License.
 */

package org.qubership.atp.macros.core.processor;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeVisitor;
import org.apache.commons.lang.StringUtils;

import org.qubership.atp.macros.core.calculator.MacrosCalculator;
import org.qubership.atp.macros.core.exception.ThrowingErrorListener;
import org.qubership.atp.macros.core.model.Macros;
import org.qubership.atp.macros.core.parser.antlr4.MacrosLexer;
import org.qubership.atp.macros.core.parser.antlr4.MacrosParser;
import org.qubership.atp.macros.core.registry.MacroRegistry;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EvaluatorImpl implements Evaluator {
    private final MacroRegistry registry;
    private final MacrosCalculator calculator;
    private MacrosLexer macrosLexer = null;
    private MacrosParser macrosParser = null;

    /**
     * Evaluate a string.
     * @param input input string
     * @param context macros context
     * @param <T> context type
     * @return evaluated string
     */
    @Nullable
    public <T extends AbstractContext<T>> String evaluate(@Nonnull String input, @Nonnull T context) {
        String result = input;
        int triesCount = 10;
        while (true) {
            if (triesCount-- == -1) {
                throw new CtxEvalException("Macros constantly reevaluates, please check for recursion."
                        + " Only 10 reevaluates are allowed.", context);
            }
            //for empty input
            if (StringUtils.isEmpty(result)) {
                return result;
            }
            context = oneTimeEvaluate(result, context.reset());
            //for constructions that lexer can not parse
            if (context == null) {
                return result;
            }
            String newResult = context.getResultOnEvaluationEnd();
            if (newResult == null) {
                return null;
            }
            if (newResult.equals(result)) {
                return result;
            }
            result = newResult;
        }
    }

    @Override
    public <T extends AbstractContext<T>> String evaluate(@Nonnull Macros macros, @Nullable List<String> args,
                                                          @Nonnull T context) {
        return calculator.calculate(macros, args, context);
    }

    private <T extends AbstractContext<T>> T oneTimeEvaluate(@Nonnull String input, @Nonnull T context) {
        return visitMacro(input, new MacrosVisitorImpl<>(registry, this, context));
    }

    private <T> T visitMacro(@Nonnull String input, @Nonnull ParseTreeVisitor<T> visitor) {
        CodePointCharStream inputStream = CharStreams.fromString(input);
        if (macrosLexer == null) {
            macrosLexer = new MacrosLexer(inputStream);
            macrosLexer.removeErrorListeners();
            macrosLexer.addErrorListener(ThrowingErrorListener.ERROR_LISTENER);
            macrosParser = new MacrosParser(new CommonTokenStream(macrosLexer));
        } else {
            macrosLexer.setInputStream(inputStream);
            macrosParser.setTokenStream(new CommonTokenStream(macrosLexer));
            macrosParser.removeErrorListeners();
            macrosParser.addErrorListener(ThrowingErrorListener.ERROR_LISTENER);
        }
        MacrosParser.BodyContext context = macrosParser.body();
        return visitor.visit(context);
    }
}
