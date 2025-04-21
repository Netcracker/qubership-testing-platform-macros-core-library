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

import java.util.IdentityHashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.qubership.atp.macros.core.model.Macros;
import org.qubership.atp.macros.core.parser.antlr4.MacrosParser;
import lombok.Data;

@Data
public abstract class AbstractContext<T extends AbstractContext> {
    protected final T parent;
    protected Map<DataKey, Object> contextMap = null;
    protected Map<String, Object> contextParameters;
    protected MacrosVisitorState strategy;

    /**
     * Creates root.
     */
    public AbstractContext() {
        this.parent = null;
        this.strategy = MacrosVisitorState.OUTSIDE_MACRO;
    }

    /**
     * Creates child.
     */
    public AbstractContext(@Nonnull T parent,
                           @Nonnull MacrosParser.MacrosStartContext macro,
                           @Nonnull Macros macros,
                           @Nonnull Evaluator evaluator) {
        this.parent = parent;
        this.strategy = MacrosVisitorState.IN_MACRO;
        notifyMacroStarts(macro, macros, evaluator);
    }

    /**
     * Initializes with macro. Should clear args in case it was uninitialized previously
     * and reused for next sibling now.
     */
    protected abstract void notifyMacroStarts(@Nonnull MacrosParser.MacrosStartContext macro,
                                              @Nonnull Macros macros,
                                              @Nonnull Evaluator evaluator);

    /**
     * Invoked only on initialized context (in the macro).
     * Will be invoked when user has defined a macro which does not exist. May be interpreted as text by default.
     *
     * @param args - the part of an arguments text.
     */
    protected abstract void pushArguments(@Nonnull MacrosParser.MacrosStartContext args);

    /**
     * Invoked only on initialized context (in the macro).
     *
     * @param args - the part of an arguments text.
     */
    protected abstract void pushArguments(@Nonnull MacrosParser.TextContext args);

    /**
     * Invoked only on initialized context (in the macro).
     *
     * @param args - the part of an arguments text.
     */
    protected abstract void pushArguments(@Nonnull MacrosParser.QuoteContext args);

    /**
     * Invoked only on initialized context (in the macro).
     *
     * @param args - the part of an arguments text.
     */
    protected abstract void pushArguments(@Nonnull MacrosParser.SlashContext args);


    /**
     * Invoked only on initialized context (in the macro).
     *
     * @param args - the part of an arguments text.
     */
    protected abstract void pushArguments(@Nonnull MacrosParser.MacroParamsContext args);

    /**
     * Invoked only on initialized context (in the macro).
     * Will be invoked when user has defined a macro which does not exist. May be interpreted as text by default.
     *
     * @param args - the part of an arguments text.
     */
    protected abstract void pushArguments(@Nonnull MacrosParser.MacrosEndContext args);


    /**
     * Args are filled up now. Result of evaluation should be pushed into state text.
     */
    protected abstract void notifyMacroEnds(MacrosParser.MacrosEndContext ctx);


    /**
     * Invoked only on uninitialized context (outside the macro).
     * Will be invoked when user has defined a macro which does not exist. May be interpreted as text by default.
     *
     * @param text - the part of an arguments text.
     */
    protected abstract void pushText(@Nonnull MacrosParser.MacrosStartContext text);

    /**
     * Invoked only on uninitialized context (outside the macro).
     *
     * @param text - the part of an arguments text.
     */
    protected abstract void pushText(@Nonnull MacrosParser.TextContext text);

    protected abstract void pushText(@Nonnull MacrosParser.QuoteContext text);

    protected abstract void pushText(@Nonnull MacrosParser.SlashContext text);

    protected abstract void pushText(@Nonnull MacrosParser.MacroParamsContext text);

    /**
     * Invoked only on uninitialized context (outside the macro).
     * Will be invoked when user has defined a macro which does not exist. May be interpreted as text by default.
     *
     * @param text - the part of an arguments text.
     */
    protected abstract void pushText(@Nonnull MacrosParser.MacrosEndContext text);

    /**
     * Invoked when macro denoted by current context contains another macro as argument.
     * So child context will be bound to the nested macro.
     */
    @Nonnull
    protected abstract T createChildContext(@Nonnull MacrosParser.MacrosStartContext ctx,
                                            @Nonnull Macros macros,
                                            Evaluator evaluator);

    /**
     * Invoked on parent to notify that child (nested macro) is fully processed and should be aggregated with parent.
     *
     * @param child - child context which will be dropped by processor.
     */
    protected abstract void reduce(@Nonnull MacrosParser.MacrosEndContext ctx, @Nonnull T child);

    public abstract T reset();

    @Nullable
    public abstract String getResultOnEvaluationEnd();

    @SuppressWarnings("unchecked")
    @Nullable
    <K> K getData(@Nonnull DataKey<K> key) {
        if (contextMap == null) {
            return null;
        }
        return (K) contextMap.get(key);
    }

    @SuppressWarnings("unchecked")
    <K> K setData(@Nonnull DataKey<K> key, @Nonnull K data) {
        if (contextMap == null) {
            contextMap = new IdentityHashMap<>();
        }
        return (K) contextMap.put(key, data);
    }

    @SuppressWarnings("unchecked")
    <K> K removeData(@Nonnull DataKey<K> key) {
        if (contextMap == null) {
            return null;
        }
        return (K) contextMap.remove(key);
    }
}
