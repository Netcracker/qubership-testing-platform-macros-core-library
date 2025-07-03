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

package org.qubership.atp.macros.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@Document
@JsonIgnoreProperties(ignoreUnknown = true)
public class Macros extends AbstractEntity {

    private static final long serialVersionUID = -8448338543183169090L;

    private static final String UNKNOWN_PROJECT_UUID = "38400000-8cf0-11bd-b23e-10b96e4ef00d";

    @Indexed
    private UUID projectUuid;
    private String engine;
    private String content;
    private boolean technical;

    protected List<MacrosParameter> parameters = new ArrayList<>();

    /**
     * Get project UUID as string.
     *
     * @return string
     */
    @Nonnull
    public String getProjectId() {
        if (null != getProjectUuid()) {
            return getProjectUuid().toString();
        }
        return UNKNOWN_PROJECT_UUID;
    }

    @Override
    public UUID getParentId() {
        return projectUuid;
    }
}
