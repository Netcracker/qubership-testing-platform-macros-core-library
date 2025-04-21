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

package org.qubership.atp.macros.core.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import org.qubership.atp.macros.core.client.MacrosFeignClient;
import org.qubership.atp.macros.core.converter.MacrosDtoConvertService;
import org.qubership.atp.macros.core.model.Macros;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@AllArgsConstructor
@Slf4j
public class MacrosRepository {

    private final MacrosFeignClient client;
    private final MacrosDtoConvertService macrosDtoConvertService;

    /**
     * Get All macros by projectId include Global.
     *
     * @param projectId uuid.
     * @return List of macro.
     */
    public List<Macros> findByProjectId(UUID projectId) {
        log.debug("findByProjectId {}", projectId);
        return macrosDtoConvertService.convertList(
                this.client.findAllByProject(projectId).getBody(), Macros.class);
    }

    /**
     * Evaluate macro.
     *
     * @param content some string.
     * @return the same string if content doesn't contain macro.
     */
    public String evaluate(String content) {
        log.debug("evaluate {}", content);
        return this.client.evaluate(content).getBody();
    }
}
