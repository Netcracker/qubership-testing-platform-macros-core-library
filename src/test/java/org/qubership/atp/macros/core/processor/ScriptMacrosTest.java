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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.script.ScriptEngineManager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.qubership.atp.macros.core.calculator.ScriptMacrosCalculator;
import org.qubership.atp.macros.core.exception.MacrosCompilationException;
import org.qubership.atp.macros.core.model.Macros;
import org.qubership.atp.macros.core.registry.MacroRegistryImpl;

public class ScriptMacrosTest {

    static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    EvaluatorImpl evaluator;
    ScriptMacrosCalculator calculator;

    @BeforeEach
    public void fillRegistry() throws IOException {
        File macros = new File(this.getClass().getClassLoader().getResource("globalMacros.json").getFile());
        File fakeMacros = new File(this.getClass().getClassLoader().getResource("fakeTestMacros.json").getFile());
        String macrosString = new String(Files.readAllBytes(macros.toPath()));
        String fakeMacrosString = new String(Files.readAllBytes(fakeMacros.toPath()));
        List<Macros> macrosList = OBJECT_MAPPER.readValue(macrosString, new TypeReference<List<Macros>>() {
        });
        List<Macros> fakeMacrosList = OBJECT_MAPPER.readValue(fakeMacrosString, new TypeReference<List<Macros>>() {
        });
        macrosList.addAll(fakeMacrosList);
        calculator = new ScriptMacrosCalculator(new ScriptEngineManager());
        evaluator = new EvaluatorImpl(new MacroRegistryImpl(macrosList), calculator);
    }

    private String evaluate(@Nonnull String macros, EvaluatorImpl evaluator,
                            Map<String, Object> context) {
        SimpleContext macroContext = new SimpleContext();
        macroContext.setContextParameters(context);
        return evaluator.evaluate(macros, macroContext);
    }

    @Test
    public void testMacros_OptionalParametersCompilation_NoValue() {
        String evaluate = evaluate("#TEST_OPTIONAL('Test: ')", evaluator, new HashMap<>());
        Assertions.assertEquals("Test: undefined", evaluate);
    }

    @Test
    public void testMacros_OptionalParametersCompilation_ViaValue() {
        String evaluate = evaluate("#TEST_OPTIONAL('Test: ', 'Some value')", evaluator, new HashMap<>());
        Assertions.assertEquals("Test: Some value", evaluate);
    }

    @Test
    public void testMacros_MandatoryParametersCompilation_NoValue() {
        String evaluate = evaluate("#TEST_MANDATORY('Test: ')", evaluator, new HashMap<>());
        Assertions.assertEquals("Test: Im mandatory", evaluate);
    }

    @Test
    public void testMacros_MandatoryParametersCompilation_ViaValue() {
        String evaluate = evaluate("#TEST_MANDATORY('Test: ', 'Some value')", evaluator, new HashMap<>());
        Assertions.assertEquals("Test: Some value", evaluate);
    }

    @Test
    public void testMacros_InvalidMacrosContext() {
        Assertions.assertThrows(CtxEvalException.class, () -> {
            SimpleContext macroContext = new SimpleContext(new SimpleContext(), null, null, null);
            macroContext.setContextParameters(new HashMap<>());
            evaluator.evaluate("#TEST_MANDATORY('Test: ', 'Some value')", macroContext);
        });
    }

    @Test
    public void testMacros_NotInvocableEngine() throws MacrosCompilationException {
        Assertions.assertThrows(MacrosCompilationException.class, () -> {
            Macros macros = new Macros();
            macros.setEngine("java");
            macros.setContent("public int main() { return 1; }");
            macros.setName("test");
            calculator.compile(macros);
        });
    }

    @Test
    public void testMacros_InvalidMacros() throws MacrosCompilationException {
        Assertions.assertThrows(MacrosCompilationException.class, () -> {
            Macros macros = new Macros();
            macros.setEngine("javascript");
            macros.setContent("function main(test, mandatory) { return some_method(; }");
            macros.setName("test");
            calculator.compile(macros);
        });
    }

    @Test
    public void testMacros_Env_VariableContextMacros_MustReturnFirstParameterThatExists() {
        Map<String, Object> testContext = new HashMap<>();
        testContext.put("ENV_ID", "envId");
        HashMap<String, Object> environment = new HashMap<>();
        HashMap<String, Object> system = new HashMap<>();
        HashMap<String, Object> connectionParams = new HashMap<>();
        connectionParams.put("par1", "some");
        connectionParams.put("par2", "some2");
        system.put("conn", connectionParams);
        environment.put("sys", system);
        testContext.put("envId", environment);
        String evaluate = evaluate("#ENV_VARIABLE('par1')", evaluator, testContext);
        Assertions.assertEquals("some", evaluate);
    }

    @Test
    public void testMacros_Res_VariableContextMacros_MustReturnSystemParameter() {
        Map<String, Object> testContext = new HashMap<>();
        testContext.put("ENV_ID", UUID.fromString("de22bb29-ddfe-4816-9cdf-2e6ed7c527d8"));
        HashMap<String, Object> environment = new HashMap<>();
        HashMap<String, Object> system = new HashMap<>();
        HashMap<String, Object> system2 = new HashMap<>();
        HashMap<String, Object> connectionParams = new HashMap<>();
        connectionParams.put("par1", "some");
        HashMap<String, Object> connectionParams2 = new HashMap<>();
        connectionParams2.put("par1", "some2");
        system.put("conn", connectionParams);
        system2.put("conn", connectionParams2);
        environment.put("sys", system);
        environment.put("sys2", system2);
        testContext.put("de22bb29-ddfe-4816-9cdf-2e6ed7c527d8", environment);
        String evaluate = evaluate("#RES_VARIABLE('sys2','conn.par1')", evaluator, testContext);
        Assertions.assertEquals("some2", evaluate);
    }

    @Test
    public void testMacros_Res_VariableContextMacros_MustReturnSystemParameterWithDotSeparator() {
        Map<String, Object> testContext = new HashMap<>();
        testContext.put("ENV_ID", "envId");
        HashMap<String, Object> environment = new HashMap<>();
        HashMap<String, Object> system = new HashMap<>();
        HashMap<String, Object> system2 = new HashMap<>();
        HashMap<String, Object> connectionParams = new HashMap<>();
        connectionParams.put("par1", "some");
        HashMap<String, Object> connectionParams2 = new HashMap<>();
        connectionParams2.put("par1", "some2");
        system.put("conn", connectionParams);
        system2.put("conn", connectionParams2);
        environment.put("sys", system);
        environment.put("sys2", system2);
        testContext.put("envId", environment);
        String evaluate = evaluate("#RES_VARIABLE('sys2','conn.par1')", evaluator, testContext);
        Assertions.assertEquals("some2", evaluate);
    }

    @Test
    public void testMacros_emptyMacrosInInput_shouldReturnEmptyParam() {
        String evaluate = evaluate("", evaluator, new HashMap<>());
        Assertions.assertEquals("", evaluate);
    }

    @Test
    public void testServiceDatasetFullNameMacros() {
        Map<String, Object> context = new HashMap<>();
        context.put("VISIBILITY_AREA_NAME", "va");
        context.put("DATASET_STORAGE_NAME", "dsl");
        context.put("DATASET_NAME", "ds");
        String evaluate = evaluate("$SDS_FULL_NAME()", evaluator, context);
        Assertions.assertEquals("va|dsl|ds", evaluate);
    }


    @Test
    public void testExecutionRequestShortNameMacros() {
        Map<String, Object> context = new HashMap<>();
        context.put("EXECUTION_REQUEST_SHORT_NAME", "name");
        String evaluate = evaluate("$EXECUTION_REQUEST_SHORT_NAME()", evaluator, context);
        Assertions.assertEquals("name", evaluate);
    }

    @Test
    public void testExecutionRequestFullNameMacros() {
        Map<String, Object> context = new HashMap<>();
        context.put("EXECUTION_REQUEST_NAME", "name");
        String evaluate = evaluate("$EXECUTION_REQUEST_NAME()", evaluator, context);
        Assertions.assertEquals("name", evaluate);
    }

    @Test
    public void testExecutionRequestIdMacros() {
        Map<String, Object> context = new HashMap<>();
        context.put("EXECUTION_REQUEST_ID", "123345456");
        String evaluate = evaluate("$EXECUTION_REQUEST_ID()", evaluator, context);
        Assertions.assertEquals("123345456", evaluate);
    }

    @Test
    public void testContextMacros() {
        Map<String, Object> context = new HashMap<>();
        context.put("var", "value");
        String evaluate = evaluate("$CONTEXT('var')", evaluator, context);
        Assertions.assertEquals("value", evaluate);
    }

    @Test
    public void testExecutionRequestKeyMacros() {
        Map<String, Object> context = new HashMap<>();
        context.put("EXECUTION_REQUEST_KEY", "key");
        String evaluate = evaluate("$EXECUTION_REQUEST_KEY()", evaluator, context);
        Assertions.assertEquals("key", evaluate);
    }

    @Test
    public void testDatasetServiceValueMacros() {
        Map<String, Object> context = new HashMap<>();
        context.put("Ben's Cases.Top Offerings.Internet Dedicada.SLOs.Gestão Smart.MRC", "100");
        String evaluate =
                evaluate("$DATA_SET_SERVICE_VALUE('Ben\\'s Cases.Top Offerings.Internet Dedicada.SLOs.Gestão Smart"
                         + ".MRC')",
                        evaluator, context);
        Assertions.assertEquals("100", evaluate);
    }

    @Test
    public void noMacro_SqlDifficult_MacroBetweenTextParts_OneEvaluationStepsOccurred() {
        Map<String, Object> context = new HashMap<>();
        String macro = "Store SQL \"select o.object_id from nc_objects o join nc_addresses a on a.id=o.object_id join"
                       + " nc_references r7 on (o.object_id=r7.object_id and r7.attr_id=9147412578213883528 /* "
                       + "Serviceability Data */ and r7.reference=9153651356313208758 /* Magyar Telekom xPON 150 (60)"
                       + " */) join nc_references r8 on (o.object_id=r8.object_id and r8.attr_id=9147412578213883528 "
                       + "/* Serviceability Data */ and r8.reference=9153651356313208757 /* Magyar Telekom xPON 300 "
                       + "(100) */ ) join nc_references r9 on (o.object_id=r9.object_id and r9"
                       + ".attr_id=9147412578213883528 /* Serviceability Data */ and r9.reference=9153651350713208756"
                       + " /* Magyar Telekom xPON 1000 (300) */ ) and o.object_id not in ('9153642155513202112' /* "
                       + "Magyar Telekom HFC 500 (100) */ , '9153642181313202113' /* Magyar Telekom HFC 500 (15) "
                       + "*/ ,"
                       + " '9153651338213208754' /* Magyar Telekom HFC 150 (15) */, '9153651338213208753' /* "
                       + "Magyar "
                       + "Telekom HFC 150 (24) */, '9153651338213208752' /* Magyar Telekom HFC 300 (15) */, "
                       + "'9153642186313202114' /* Magyar Telekom HFC 300 (50) */) and a.addr like "
                       + "'%Budapest_AT%' "
                       + "and type_id = 9153227660013270601 /* Unit */\" execution result in variable \"unit_id\" in "
                       + "local context";
        String evaluate =
                evaluate(macro, evaluator, context);
        Assertions.assertEquals(macro, evaluate);
    }

    @Test
    public void testJustTextWithQuo() {
        String evaluate =
                evaluate("Ben's Cases.Top Offerings.Internet Dedicada.SLOs.Gestão Smart.MRC",
                        evaluator, new HashMap<>());
        Assertions.assertEquals("Ben's Cases.Top Offerings.Internet Dedicada.SLOs.Gestão Smart.MRC", evaluate);
    }

    @Test
    public void testRandMacros() {
        Map<String, Object> context = new HashMap<>();
        String evaluate =
                evaluate("$RANDOM('$$$$$$$$$$')", evaluator, context);
        Assertions.assertEquals(10, evaluate.length());
    }

    @Test
    public void testTestEnvNameEMacros() {
        Map<String, Object> context = new HashMap<>();
        context.put("ENV_NAME", "env1");
        String evaluate =
                evaluate("$TEST_ENV_NAME()", evaluator, context);
        Assertions.assertEquals("env1", evaluate);
    }

    @Test
    public void testMacroInsideSqlStringWithBracketAfterMacro() {
        Map<String, Object> context = new HashMap<>();
        context.put("TEST_CASE_SHORT_NAME", "test");
        String evaluate =
                evaluate("insert into nc_testdata  (TC_ID, TC_NAME, OBJECT_ID, STATUS) values ('${TD.prefix}', "
                         + "'$TEST_CASE_SHORT_NAME()', '[]', 'Available')", evaluator, context);
        Assertions.assertEquals("insert into nc_testdata  (TC_ID, TC_NAME, OBJECT_ID, STATUS) values ('${TD.prefix}', "
                            + "'test', '[]', 'Available')", evaluate);
    }

    @Test
    public void testMacroInsideSqlString() {
        Map<String, Object> context = new HashMap<>();
        context.put("TEST_CASE_SHORT_NAME", "test");
        String evaluate =
                evaluate("insert into nc_testdata '$TEST_CASE_SHORT_NAME()' some", evaluator, context);
        Assertions.assertEquals("insert into nc_testdata 'test' some", evaluate);
    }

    @Test
    public void testParsingShiftMonthMacros() {
        String evaluate = evaluate("$SHIFT_MONTH('-1', '2019-02-25T12:01:41.515Z',  'yyyy-MM-dd\\'T\\'HH:mm:ss"
                                   + ".SSS\\'Z\\'')", evaluator, new HashMap<>());
        Assertions.assertEquals("2019-01-25T12:01:41.515Z", evaluate);
    }

    @Test
    public void testDifficultStringMacro() {
        String evaluate =
                evaluate(",dfghdfghdfgh,#UUID(), Active #RANDBETWEEN('#RANDBETWEEN('#RANDBETWEEN('#RANDBETWEEN('5',"
                         + "'7')','7')','7')','1')",
                        evaluator, new HashMap<>());
        String regex = ",dfghdfghdfgh,\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b, "
                       + "Active \\d";
        Assertions.assertTrue(evaluate.matches(regex));
    }

    @Test
    public void testDifficultStringMacroWithoutQuote() {
        String evaluate =
                evaluate(",dfghdfghdfgh,#UUID(), Active #RANDBETWEEN(#RANDBETWEEN(#RANDBETWEEN(#RANDBETWEEN(5,7)"
                         + ",7),7),1)",
                        evaluator, new HashMap<>());
        String regex = ",dfghdfghdfgh,\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b, "
                       + "Active \\d";
        Assertions.assertTrue(evaluate.matches(regex));
    }

    @Test
    public void testDifficultStringMacro2() {
        String evaluate =
                evaluate("#UUID(),dfghdfghdfgh,#UUID_UPPERCASE(), Active #RANDBETWEEN('#RANDBETWEEN('#RANDBETWEEN"
                         + "('#RANDBETWEEN('5','7')','7')','7')','1')",
                        evaluator, new HashMap<>());
        String regex = "\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b,dfghdfghdfgh,"
                       + "#UUID_UPPERCASE\\(\\), Active \\d";
        Assertions.assertTrue(evaluate.matches(regex));
    }

    @Test
    public void testDifficultStringNonMacroWithSlash() {
        String evaluate =
                evaluate("Change separator in path \".\\JSON\\NewDocumentID.txt\" and store in local param \"path\"",
                        evaluator, new HashMap<>());
        String regex = "Change separator in path \".\\\\JSON\\\\NewDocumentID.txt\" and store in local param \"path\"";
        Assertions.assertTrue(evaluate.matches(regex));
    }
}
