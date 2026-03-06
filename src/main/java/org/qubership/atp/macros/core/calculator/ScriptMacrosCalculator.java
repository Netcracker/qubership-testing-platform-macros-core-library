/*
 * # Copyright 2024-2026 NetCracker Technology Corporation
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

package org.qubership.atp.macros.core.calculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.apache.commons.lang3.StringUtils;
import org.qubership.atp.macros.core.exception.MacrosCompilationException;
import org.qubership.atp.macros.core.model.Macros;
import org.qubership.atp.macros.core.model.MacrosParameter;
import org.qubership.atp.macros.core.processor.AbstractContext;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ScriptMacrosCalculator implements MacrosCalculator {
    private final Map<String, CompiledScript> compiledScripts = new HashMap<>();
    private final ScriptEngineManager scriptEngineManager;

    @Nullable
    @Override
    public String calculate(@Nonnull Macros macros,
                            @Nullable List<String> arguments,
                            @Nonnull AbstractContext context) {
        ScriptEngine engine = scriptEngineManager.getEngineByName(macros.getEngine());
        if (engine instanceof Invocable) {
            try {
                CompiledScript compiledScript = compile(engine, macros);
                Bindings bindings = compiledScript.getEngine().createBindings();
                List<String> compiledArguments = compileArguments(macros, arguments);
                bindings.put("args", compiledArguments);
                if (context.getContextParameters() != null) {
                    bindings.put("contextMap", context.getContextParameters());
                }
                SimpleScriptContext scriptCtx = new SimpleScriptContext();
                scriptCtx.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
                compiledScript.eval(scriptCtx);
                return ((Invocable) compiledScript.getEngine()).invokeMethod(
                        bindings, "main", compiledArguments.toArray()).toString();
            } catch (MacrosCompilationException | NoSuchMethodException | ScriptException e) {
                final String message = "Error during evaluation of %s macros: %s".formatted(macros.getName(),
                        e.getMessage());
                log.error(message, e);
                return message;
            }
        }
        return null;
    }

    private List<String> compileArguments(@Nonnull Macros macros, @Nullable List<String> arguments) {
        List<String> compiledArguments = new ArrayList<>();
        List<MacrosParameter> parameters = macros.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            MacrosParameter macrosParameter = parameters.get(i);
            if (arguments != null && arguments.size() - 1 >= i) {
                compiledArguments.add(unEscapeQuoteInsideMacroArguments(arguments.get(i)));
            } else if (!macrosParameter.getOptional()) {
                compiledArguments.add(macrosParameter.getDefaultValue());
            }
        }
        return compiledArguments;
    }

    private String unEscapeQuoteInsideMacroArguments(String agr) {
        return StringUtils.replace(agr, "\\'", "'");
    }

    private @Nonnull
    CompiledScript compile(@Nonnull ScriptEngine engine, @Nonnull Macros macros) throws MacrosCompilationException {
        if (engine instanceof Compilable compilable) {
            try {
                String key = macros.getName();
                if (compiledScripts.containsKey(key)) {
                    return compiledScripts.get(key);
                }
                CompiledScript compiledScript = compilable.compile(macros.getContent());
                compiledScripts.put(macros.getName(), compiledScript);
                return compiledScript;
            } catch (ScriptException e) {
                final String message = "Error during compilation of %s macros: %s".formatted(macros.getName(),
                        e.getMessage());
                log.error(message, e);
                throw new MacrosCompilationException(message, e);
            }
        } else {
            String message = "Engine %s is not compilable".formatted(macros.getEngine());
            log.error(message);
            throw new MacrosCompilationException(message);
        }
    }

    /**
     * Try to compile a macros.
     *
     * @param macros macros
     * @return none
     * @throws MacrosCompilationException if compilation failed
     */
    public @Nullable
    CompiledScript compile(@Nonnull Macros macros) throws MacrosCompilationException {
        ScriptEngine engine = scriptEngineManager.getEngineByName(macros.getEngine());
        if (engine instanceof Invocable) {
            return compile(engine, macros);
        } else {
            throw new MacrosCompilationException("Engine %s is not invocable".formatted(macros.getEngine()));
        }
    }
}
