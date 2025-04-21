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

package org.qubership.atp.macros.core.clientpart;

import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import org.qubership.atp.macros.core.clients.api.dto.macros.MacrosDto;
import org.qubership.atp.macros.core.client.MacrosFeignClient;
import org.qubership.atp.macros.core.converter.MacrosDtoConvertService;
import org.qubership.atp.macros.core.model.Macros;
import org.qubership.atp.macros.core.repository.MacrosRepository;

@ExtendWith(SpringExtension.class)
public class MacrosAsClientTest {

    @MockBean
    private MacrosRepository service;
    @MockBean
    private MacrosFeignClient feignClient;

    @MockBean
    private MacrosDtoConvertService macrosDtoConvertService;


    private static final String MACROS_NAME_1 = "TestMacros1";
    private static final UUID MACROS_UUID_1 = UUID.randomUUID();
    private static final String MACROS_NAME_2 = "TestMacros2";
    private static final UUID MACROS_UUID_2 = UUID.randomUUID();

    private static final String MACROS_STRING = "test";

    private Macros macros1;
    private Macros macros2;
    private MacrosDto macrosDto1;
    private MacrosDto macrosDto2;


    public void setUp() {
        macros1 = new Macros();
        macros1.setName(MACROS_NAME_1);
        macros1.setUuid(MACROS_UUID_1);

        macrosDto1 = new MacrosDto();
        macrosDto1.setName(MACROS_NAME_1);
        macrosDto1.setUuid(MACROS_UUID_1);

        macros2 = new Macros();
        macros2.setName(MACROS_NAME_2);
        macros2.setUuid(MACROS_UUID_2);

        macrosDto2 = new MacrosDto();
        macrosDto2.setName(MACROS_NAME_2);
        macrosDto2.setUuid(MACROS_UUID_2);

    }

    @Test
    public void testFindMacrosByProjectId() {
        UUID projectId = UUID.randomUUID();
        List<Macros> expectedMacros = Arrays.asList(macros1, macros2);
        when(service.findByProjectId(projectId)).thenReturn(expectedMacros);
        Assertions.assertEquals(expectedMacros, service.findByProjectId(projectId));
    }

    @Test
    public void testEvaluate() {
        when(service.evaluate(MACROS_STRING)).thenReturn(MACROS_STRING);
        Assertions.assertEquals(MACROS_STRING, service.evaluate(MACROS_STRING));
    }

    @Test
    public void testFindMacrosByProjectIdRestClient() {
        UUID projectId = UUID.randomUUID();
        List<MacrosDto> expectedMacroDtos = Arrays.asList(macrosDto1, macrosDto2);
        ResponseEntity<List<MacrosDto>> responseEntity = new ResponseEntity<>(expectedMacroDtos, HttpStatus.OK);
        when(feignClient.findAllByProject(projectId)).thenReturn(responseEntity);
        Assertions.assertEquals(expectedMacroDtos, feignClient.findAllByProject(projectId).getBody());
    }

    @Test
    public void testEvaluateRestClient() {
        ResponseEntity<String> responseEntity = new ResponseEntity<>(MACROS_STRING, HttpStatus.OK);
        when(feignClient.evaluate(MACROS_STRING)).thenReturn(responseEntity);
        Assertions.assertEquals(MACROS_STRING, feignClient.evaluate(MACROS_STRING).getBody());
    }
}
