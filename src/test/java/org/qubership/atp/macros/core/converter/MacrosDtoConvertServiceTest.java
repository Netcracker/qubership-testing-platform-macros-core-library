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

package org.qubership.atp.macros.core.converter;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.qubership.atp.macros.core.clients.api.dto.macros.MacrosDto;
import org.qubership.atp.macros.core.clients.api.dto.macros.MacrosParameterDto;
import org.qubership.atp.macros.core.model.Macros;
import org.qubership.atp.macros.core.model.MacrosParameter;

public class MacrosDtoConvertServiceTest {

    private MacrosDtoConvertService dtoConverter;

    @BeforeEach
    public void setUp() {
        dtoConverter = new MacrosDtoConvertService();
    }

    @Test
    public void testConvertMacrosDtoToMacrosObj() {
        UUID macrosId = UUID.randomUUID();
        UUID paramId = UUID.randomUUID();
        String macrosName = "Macros 1";
        String paramName = "Param 1";
        String paramVal = "def value";
        MacrosDto macrosDto = new MacrosDto();
        Instant instant = Instant.now();
        Instant instant2 = Instant.now().plus(1, ChronoUnit.MINUTES);
        macrosDto.setCreatedWhen(OffsetDateTime.ofInstant(instant, ZoneId.systemDefault()));
        macrosDto.setModifiedWhen(OffsetDateTime.ofInstant(instant2, ZoneId.systemDefault()));
        macrosDto.setUuid(macrosId);
        macrosDto.setName(macrosName);

        MacrosParameterDto parameterDto = new MacrosParameterDto();
        parameterDto.setName(paramName);
        parameterDto.setDefaultValue(paramVal);
        parameterDto.setUuid(paramId);
        macrosDto.setParameters(Arrays.asList(parameterDto));

        Macros expected = new Macros();
        expected.setCreatedWhen(Date.from(instant));
        expected.setModifiedWhen(Date.from(instant2));
        expected.setName(macrosName);
        expected.setUuid(macrosId);

        MacrosParameter macrosParameter = new MacrosParameter();
        macrosParameter.setName(paramName);
        macrosParameter.setDefaultValue(paramVal);
        macrosParameter.setUuid(paramId);
        expected.setParameters(Arrays.asList(macrosParameter));

        Macros result = dtoConverter.convert(macrosDto, Macros.class);
        Assertions.assertEquals(expected.getCreatedWhen(), result.getCreatedWhen());
        Assertions.assertEquals(expected.getModifiedWhen(), result.getModifiedWhen());
        Assertions.assertEquals(expected.getParameters(), result.getParameters());
        Assertions.assertEquals(macrosName, result.getName());
        Assertions.assertEquals(expected, result);
    }
}
