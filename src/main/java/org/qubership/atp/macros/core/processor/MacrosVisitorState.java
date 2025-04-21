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
import org.qubership.atp.macros.core.parser.antlr4.MacrosParser;

interface MacrosVisitorState {
    MacrosVisitorState.InMacro IN_MACRO = new MacrosVisitorState.InMacro();
    MacrosVisitorState.OutsideMacro OUTSIDE_MACRO = new MacrosVisitorState.OutsideMacro();

    <T extends AbstractContext<T>> void visitMacrosStart(@Nonnull MacrosParser.MacrosStartContext ctx,
                                                         @Nonnull Macros macros,
                                                         @Nonnull MacrosVisitorImpl<T> processor);

    <T extends AbstractContext<T>> void visitMacrosEnd(@Nonnull MacrosParser.MacrosEndContext ctx,
                                                       @Nonnull MacrosVisitorImpl<T> processor);

    void visitText(@Nonnull MacrosParser.TextContext ctx, @Nonnull AbstractContext state);

    void visitText(@Nonnull MacrosParser.QuoteContext ctx, @Nonnull AbstractContext state);

    void visitText(@Nonnull MacrosParser.SlashContext ctx, @Nonnull AbstractContext state);

    void visitText(@Nonnull MacrosParser.MacroParamsContext ctx, @Nonnull AbstractContext state);

    void visitText(@Nonnull MacrosParser.MacrosStartContext ctx, @Nonnull AbstractContext state);

    void visitText(@Nonnull MacrosParser.MacrosEndContext ctx, @Nonnull AbstractContext state);

    void assertEvaluationEnded(AbstractContext state);

    class InMacro implements MacrosVisitorState {

        @Override
        public <T extends AbstractContext<T>> void visitMacrosStart(@Nonnull MacrosParser.MacrosStartContext ctx,
                                                                    @Nonnull Macros macros,
                                                                    @Nonnull MacrosVisitorImpl<T> processor)
                throws CtxEvalException {
            processor.state = processor.state.createChildContext(ctx, macros, processor.evaluator);
        }

        @Override
        public <T extends AbstractContext<T>> void visitMacrosEnd(@Nonnull MacrosParser.MacrosEndContext ctx,
                                                                  @Nonnull MacrosVisitorImpl<T> processor)
                throws CtxEvalException {
            T state = processor.state;
            state.notifyMacroEnds(ctx);
            state.strategy = MacrosVisitorState.OUTSIDE_MACRO;
        }

        @Override
        public void visitText(@Nonnull MacrosParser.TextContext ctx, @Nonnull AbstractContext state)
                throws CtxEvalException {
            state.pushArguments(ctx);
        }

        @Override
        public void visitText(@Nonnull MacrosParser.QuoteContext ctx, @Nonnull AbstractContext state)
                throws CtxEvalException {
            state.pushArguments(ctx);
        }

        @Override
        public void visitText(@Nonnull MacrosParser.SlashContext ctx, @Nonnull AbstractContext state) {
            state.pushArguments(ctx);
        }

        @Override
        public void visitText(@Nonnull MacrosParser.MacroParamsContext ctx, @Nonnull AbstractContext state)
                throws CtxEvalException {
            state.pushArguments(ctx);
        }


        @Override
        public void visitText(@Nonnull MacrosParser.MacrosStartContext ctx, @Nonnull AbstractContext state) {
            state.pushArguments(ctx);
        }

        @Override
        public void visitText(@Nonnull MacrosParser.MacrosEndContext ctx, @Nonnull AbstractContext state) {
            state.pushArguments(ctx);
        }

        @Override
        public void assertEvaluationEnded(AbstractContext state) throws CtxEvalException {
            throw new CtxEvalException("Macro is not ended properly", state);
        }
    }

    class OutsideMacro implements MacrosVisitorState {

        @Override
        public <T extends AbstractContext<T>> void visitMacrosStart(@Nonnull MacrosParser.MacrosStartContext ctx,
                                                                    @Nonnull Macros macros,
                                                                    @Nonnull MacrosVisitorImpl<T> processor)
                throws CtxEvalException {
            processor.state.strategy = IN_MACRO;
            processor.state.notifyMacroStarts(ctx, macros, processor.evaluator);
        }

        /**
         * Should transfer the contained data into it's parent.
         * Child will be thrown away. Child is evaluated - content in it's text. Parent may have an
         * arguments, which should be reevaluated too. Sample: this.tail.append(parent.args).append(child.text)
         * .append(")");
         * parent.args.clear();
         */
        @Override
        public <T extends AbstractContext<T>> void visitMacrosEnd(@Nonnull MacrosParser.MacrosEndContext ctx,
                                                                  @Nonnull MacrosVisitorImpl<T> processor)
                throws CtxEvalException {
            T state = processor.state;
            if (state.parent == null) {
                throw new CtxEvalException("Unexpected macro ending: " + ctx, state);
            }
            T parent = state.parent;
            parent.reduce(ctx, state);
            parent.notifyMacroEnds(ctx);
            parent.strategy = MacrosVisitorState.OUTSIDE_MACRO;
            processor.state = parent;
        }

        @Override
        public void visitText(@Nonnull MacrosParser.TextContext ctx, @Nonnull AbstractContext state)
                throws CtxEvalException {
            state.pushText(ctx);
        }

        @Override
        public void visitText(@Nonnull MacrosParser.QuoteContext ctx, @Nonnull AbstractContext state)
                throws CtxEvalException {
            state.pushText(ctx);
        }

        @Override
        public void visitText(@Nonnull MacrosParser.SlashContext ctx, @Nonnull AbstractContext state)
                throws CtxEvalException {
            state.pushText(ctx);
        }

        @Override
        public void visitText(@Nonnull MacrosParser.MacroParamsContext ctx, @Nonnull AbstractContext state) {
            state.pushText(ctx);
        }


        @Override
        public void visitText(@Nonnull MacrosParser.MacrosStartContext ctx, @Nonnull AbstractContext state) {
            state.pushText(ctx);
        }

        @Override
        public void visitText(@Nonnull MacrosParser.MacrosEndContext ctx, @Nonnull AbstractContext state) {
            state.pushText(ctx);
        }

        @Override
        public void assertEvaluationEnded(AbstractContext state) throws CtxEvalException {
            if (state.parent != null) {
                throw new CtxEvalException("Macro is not closed properly", state);
            }
        }
    }

}
