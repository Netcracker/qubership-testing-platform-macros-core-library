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

package org.qubership.atp.macros.core.registry;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.qubership.atp.macros.core.model.Macros;
import lombok.Data;

@Data
public class MacroRegistryImpl implements MacroRegistry, Serializable {

    private static final long serialVersionUID = 4156422395580349521L;

    private final HashMap<String, Macros> macros = new HashMap<>();

    /**
     * Contructor.
     *
     * @param macros the list of macros
     */
    public MacroRegistryImpl(@Nonnull List<Macros> macros) {
        for (Macros macro : macros) {
            this.macros.put(macro.getName(), macro);
        }
    }

    @Nullable
    @Override
    public Macros getMacros(@Nonnull String key) {
        if (macros.containsKey(key)) {
            return macros.get(key);
        }
        return null;
    }
}
