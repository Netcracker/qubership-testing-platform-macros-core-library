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

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.antlr.v4.runtime.tree.ParseTree;
import org.springframework.util.StringUtils;

import org.qubership.atp.macros.core.model.Macros;
import org.qubership.atp.macros.core.parser.antlr4.MacrosParser;

public class SimpleContext extends AbstractContext<SimpleContext> {

    protected List<String> args = new ArrayList<>();
    protected SimpleContext reducedChild = null;
    protected List<String> text = new ArrayList();
    protected Macros macro;
    protected Evaluator evaluator;

    public SimpleContext() {
        super();
    }

    public SimpleContext(@Nonnull SimpleContext parent,
                         @Nonnull MacrosParser.MacrosStartContext macro,
                         @Nonnull Macros macros,
                         @Nonnull Evaluator evaluator) {
        super(parent, macro, macros, evaluator);
    }

    @Override
    protected void notifyMacroStarts(@Nonnull MacrosParser.MacrosStartContext macro,
                                     @Nonnull Macros macros,
                                     @Nonnull Evaluator evaluator) {
        this.macro = macros;
        this.evaluator = evaluator;
    }

    protected String getArgument(MacrosParser.MacroArgContext arg) {
        StringBuilder builder = new StringBuilder();
        if (null != arg) {
            int childCount = arg.getChildCount();
            for (int i = 0; i < childCount; i++) {
                ParseTree child = arg.getChild(i);
                if (child instanceof MacrosParser.TextContext) {
                    builder.append(child.getText());
                } else if (child instanceof MacrosParser.MacrosContext) {
                    builder.append(evaluator.evaluate(child.getText(), createRootContext()));
                }
            }
        }
        return builder.toString();
    }

    @Override
    protected void pushArguments(@Nonnull MacrosParser.MacrosStartContext args) {
        this.args.add(args.getText());
    }

    @Override
    protected void pushArguments(@Nonnull MacrosParser.TextContext args) {
        this.args.add(args.getText());
    }

    @Override
    protected void pushArguments(@Nonnull MacrosParser.QuoteContext args) {
        this.args.add(args.getText());
    }

    @Override
    protected void pushArguments(@Nonnull MacrosParser.SlashContext args) {
        this.args.add(args.getText());
    }

    protected void pushArguments(MacrosParser.MacroParamContext arg) {
        String result = getArgument(arg.macroArg());
        if (!StringUtils.isEmpty(result)) {
            this.args.add(evaluator.evaluate(result, createRootContext()));
        }
    }

    @Override
    protected void pushArguments(@Nonnull MacrosParser.MacroParamsContext args) {
        for (MacrosParser.MacroParamContext arg : args.macroParam()) {
            pushArguments(arg);
        }
    }

    @Override
    protected void pushArguments(@Nonnull MacrosParser.MacrosEndContext args) {
        this.args.add(args.getText());
    }

    @Override
    protected void notifyMacroEnds(MacrosParser.MacrosEndContext ctx) {
        if (reducedChild != null) {
            String evaluatedArgs = evaluator.evaluate(String.join("", args), createRootContext());
            args.clear();
            args.add(evaluatedArgs);
        }
        String result = evaluator.evaluate(macro, args, this);

        text.add(result);
        resetMacro();
    }

    protected SimpleContext createRootContext() {
        return new SimpleContext();
    }

    @Override
    protected void pushText(@Nonnull MacrosParser.MacrosStartContext text) {
        this.text.add(text.getText());
    }

    @Override
    protected void pushText(@Nonnull MacrosParser.TextContext text) {
        this.text.add(text.getText());
    }

    @Override
    protected void pushText(@Nonnull MacrosParser.QuoteContext text) {
        this.text.add(text.getText());
    }

    @Override
    protected void pushText(@Nonnull MacrosParser.SlashContext text) {
        this.text.add(text.getText());
    }

    @Override
    protected void pushText(@Nonnull MacrosParser.MacroParamsContext text) {
        this.text.add(text.getText());
    }

    @Override
    protected void pushText(@Nonnull MacrosParser.MacrosEndContext text) {
        this.text.add(text.getText());
    }

    @Nonnull
    @Override
    protected SimpleContext createChildContext(@Nonnull MacrosParser.MacrosStartContext ctx,
                                               @Nonnull Macros macros,
                                               @Nonnull Evaluator evaluator) {
        return new SimpleContext(this, ctx, macros, evaluator);
    }

    @Override
    protected void reduce(@Nonnull MacrosParser.MacrosEndContext ctx, @Nonnull SimpleContext child) {
        args.addAll(child.text);
        reducedChild = child;
    }

    @Override
    public String getResultOnEvaluationEnd() {
        strategy.assertEvaluationEnded(this);
        return String.join("", text);
    }

    protected void resetMacro() {
        macro = null;
        evaluator = null;
        args.clear();
        reducedChild = null;
    }

    @Override
    public SimpleContext reset() {
        resetMacro();
        text.clear();
        return this;
    }
}
