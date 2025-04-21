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

import javax.annotation.Nonnull;

import org.qubership.atp.macros.core.model.Macros;
import org.qubership.atp.macros.core.parser.antlr4.MacrosBaseVisitor;
import org.qubership.atp.macros.core.parser.antlr4.MacrosParser;
import org.qubership.atp.macros.core.registry.MacroRegistry;

public class MacrosVisitorImpl<T extends AbstractContext<T>> extends MacrosBaseVisitor<T> {

    protected final MacrosOpeningCounter brackets = new MacrosOpeningCounter();
    protected final MacroRegistry registry;
    protected final Evaluator evaluator;
    protected T state;

    /**
     * Constructor.
     *
     * @param registry  registry
     * @param evaluator evaluator
     * @param state     state
     */
    public MacrosVisitorImpl(@Nonnull MacroRegistry registry, @Nonnull Evaluator evaluator, @Nonnull T state) {
        this.registry = registry;
        this.state = state;
        this.evaluator = evaluator;
    }

    @Override
    public T visitText(MacrosParser.TextContext ctx) {
        state.strategy.visitText(ctx, state);
        return state;
    }

    @Override
    public T visitQuote(MacrosParser.QuoteContext ctx) {
        state.strategy.visitText(ctx, state);
        return state;
    }

    @Override
    public T visitSlash(MacrosParser.SlashContext ctx) {
        state.strategy.visitText(ctx, state);
        return state;
    }

    @Override
    public T visitMacroParams(MacrosParser.MacroParamsContext ctx) {
        state.strategy.visitText(ctx, state);
        return state;
    }

    @Override
    public T visitMacrosStart(MacrosParser.MacrosStartContext ctx) {
        String macroStart = ctx.MACROS().getText();
        Macros macros = registry.getMacros(macroStart.substring(1, macroStart.length() - 1));
        if (macros == null) {
            state.strategy.visitText(ctx, state);
            brackets.open();
        } else {
            state.strategy.visitMacrosStart(ctx, macros, this);
            brackets.goDeeper();
        }
        return state;
    }

    @Override
    public T visitMacrosEnd(MacrosParser.MacrosEndContext ctx) {
        if (brackets.isGoingUpperOnClose()) {
            state.strategy.visitMacrosEnd(ctx, this);
        } else {
            state.strategy.visitText(ctx, state);
        }
        return state;
    }

    @Override
    public String toString() {
        return state.toString();
    }

}
