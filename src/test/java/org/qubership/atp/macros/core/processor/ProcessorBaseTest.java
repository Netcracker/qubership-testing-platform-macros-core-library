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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import org.qubership.atp.macros.core.calculator.MacrosCalculator;
import org.qubership.atp.macros.core.model.Macros;
import org.qubership.atp.macros.core.parser.antlr4.MacrosParser;
import org.qubership.atp.macros.core.registry.MacroRegistry;

public class ProcessorBaseTest {
    private static final Set<String> MACROSES = new HashSet<String>() {{
        add("REF_DSL");
        add("SUM");
        add("REF_ALIAS");
        add("REF_THIS");
        add("UUID");
        add("GET_MONTH");
    }};
    private static final DataKey<String> MACRO_NAME = DataKey.create("test macro name");

    private static void validate(String inputMacro, String overallEr, EvaluationStep... steps) {
        Iterator<EvaluationStep> er = Arrays.asList(steps).iterator();

        Evaluator evaluator = new EvaluatorImpl(new TestMacroRegistry() {
        }, new MacrosCalculator() {
            @Override
            public String calculate(@Nonnull Macros macros, @Nullable List<String> arguments,
                                    @Nonnull AbstractContext item) {
                if (!er.hasNext()) {
                    throw new IllegalStateException("\nGot unexpected iteration:\n"
                                                    + new EvaluationStep(MACRO_NAME.get(item),
                            ((SimpleContext) item).args, null));
                }
                EvaluationStep erItem = er.next();
                return erItem.calculate((SimpleContext) item);
            }
        });
        SimpleContext context = new TestContext();
        String result = evaluator.evaluate(inputMacro, context);
        if (er.hasNext()) {
            String extra = StreamSupport.stream(Spliterators.spliteratorUnknownSize(er, Spliterator.ORDERED), false)
                    .map(Objects::toString)
                    .collect(Collectors.joining("\n"));
            throw new IllegalStateException("Additional iteration(s) expected:\n" + extra);
        }
        Assertions.assertEquals(overallEr, result);
    }

    @Test
    public void macroReturnsMacroPart_MacroUnionInsideAnotherMacro_MacroPartEvaluated() throws Exception {
        String vlookupMacro = "#REF_DSL(InternationalRateCost.#REF_#REF_THIS(OriginCountry.Zone).#REF_THIS"
                              + "(DestinationCountry.Zone))";
        validate(vlookupMacro, "end",
                new EvaluationStep("REF_THIS",  Lists.list("OriginCountry.Zone"), "DSL(Countries.EU.From)"),
                new EvaluationStep("REF_THIS",  Lists.list("DestinationCountry.Zone"), "#REF_DSL(Countries.EU.To)"),
                new EvaluationStep("REF_DSL",  Lists.list("Countries.EU.To"), "to"),
                new EvaluationStep("REF_DSL",  Lists.list("Countries.EU.From"), "from"),
                new EvaluationStep("REF_DSL",  Lists.list("InternationalRateCost.from.to"), "end"));
    }

    @Test
    public void macroReturnsAnotherMacro_MacroUnionInsideAnotherMacro_BothAreEvaluated() throws Exception {
        String vlookupMacro = "#REF_DSL(InternationalRateCost.#REF_THIS(OriginCountry.Zone).#REF_THIS"
                              + "(DestinationCountry.Zone))";
        validate(vlookupMacro, "end",
                new EvaluationStep("REF_THIS",  Lists.list("OriginCountry.Zone"), "#REF_DSL(Countries.EU.Any)"),
                new EvaluationStep("REF_DSL",  Lists.list("Countries.EU.Any"), "from"),
                new EvaluationStep("REF_THIS",  Lists.list("DestinationCountry.Zone"), "to"),
                new EvaluationStep("REF_DSL",  Lists.list("InternationalRateCost.from.to"), "end"));
    }

    @Test
    public void macroReturnsAnotherMacro_MacroUnionWithDifferentArgsCountInsideAnotherMacro_BothAreEvaluated() throws Exception {
        String vlookupMacro = "#REF_DSL(National Rates.#REF_THIS(Subscription.TariffName).#REF_THIS(UsageType))";
        validate(vlookupMacro, "0",
                new EvaluationStep("REF_THIS",  Lists.list("Subscription.TariffName"), "Pro Contact"),
                new EvaluationStep("REF_THIS",  Lists.list("UsageType"), "SMS"),
                new EvaluationStep("REF_DSL",  Lists.list("National Rates.Pro Contact.SMS"), "0"));
    }

    @Test
    public void macroReturnsAnotherMacro_insideAnotherMacro_AllAreEvaluated() throws Exception {
        String vlookupMacro = "#REF_DSL(#REF_THIS(OriginCountry.Zone))";
        validate(vlookupMacro, "end",
                new EvaluationStep("REF_THIS",  Lists.list("OriginCountry.Zone"), "#REF_DSL(Countries.EU.Any)"),
                new EvaluationStep("REF_DSL",  Lists.list("Countries.EU.Any"), "from"),
                new EvaluationStep("REF_DSL",  Lists.list("from"), "end"));
    }

    @Test
    public void macroReturnsAnotherMacro_rootMacro_AllAreEvaluated() throws Exception {
        String vlookupMacro = "#REF_THIS(OriginCountry.Zone)";
        validate(vlookupMacro, "end",
                new EvaluationStep("REF_THIS",  Lists.list("OriginCountry.Zone"), "#REF_DSL(Countries.EU.Any)"),
                new EvaluationStep("REF_DSL",  Lists.list("Countries.EU.Any"), "end"));
    }

    @Test
    public void macroReturnsAnotherMacro_complexRootMacro_AllAreEvaluated() throws Exception {
        String macro = "#REF_DSL(Roaming SMS Rates.#REF_THIS(Account.AccountType).#REF_THIS(OriginCountry.Zone)"
                       + ".#REF_THIS(DestinationCountry.Zone))";
        String secondMacro = "#REF_DSL(International Voice Rates.#REF_THIS(Subscription.TariffName).#REF_THIS"
                             + "(UsageType).#REF_THIS(DestinationCountry.IntenationalZone))";
        validate(macro, "0.33",
                new EvaluationStep("REF_THIS",  Lists.list("Account.AccountType"), "B2B"),
                new EvaluationStep("REF_THIS",  Lists.list("OriginCountry.Zone"), "Belgium"),
                new EvaluationStep("REF_THIS",  Lists.list("DestinationCountry.Zone"), "Magreb"),
                new EvaluationStep("REF_DSL",  Lists.list("Roaming SMS Rates.B2B.Belgium.Magreb"), secondMacro),
                new EvaluationStep("REF_THIS",  Lists.list("Subscription.TariffName"), "Pro Contact"),
                new EvaluationStep("REF_THIS",  Lists.list("UsageType"), "SMS"),
                new EvaluationStep("REF_THIS",  Lists.list("DestinationCountry.IntenationalZone"), "World 2"),
                new EvaluationStep("REF_DSL",  Lists.list("International Voice Rates.Pro Contact.SMS.World 2"), "0.33"));
    }

    @Test
    public void macroUnion_InsideAnotherMacro_ThreeEvaluationStepsOccurred() throws Exception {
        String vlookupMacro = "#REF_DSL(InternationalRateCost.#REF_THIS(OriginCountry.Zone).#REF_THIS"
                              + "(DestinationCountry.Zone))";
        validate(vlookupMacro, "end",
                new EvaluationStep("REF_THIS",  Lists.list("OriginCountry.Zone"), "1"),
                new EvaluationStep("REF_THIS",  Lists.list("DestinationCountry.Zone"), "2"),
                new EvaluationStep("REF_DSL",  Lists.list("InternationalRateCost.1.2"), "end"));
    }

    @Test
    public void noMacro_textWithSpecialSymbols_NoEvaluationStepsOccurred() throws Exception {
        String macro = "#just_a text #";
        validate(macro, macro);
    }

    @Test
    public void singleMacro_MacroBetweenTextParts_OneEvaluationStepsOccurred() throws Exception {
        String macro = "#just_a text#REF_DSL(1.2.3)#";
        validate(macro, "#just_a text #",
                new EvaluationStep("REF_DSL", Lists.list("1.2.3"), " "));
    }

    @Test
    public void noMacro_ParameterNameWithBrackets_MacroBetweenTextParts_OneEvaluationStepsOccurred() throws Exception {
        String macro = "Click the UIElement (\"Button\"='Submit')";
        validate(macro, "Click the UIElement (\"Button\"='Submit')");
    }

    @Test
    public void singleMacros_ParameterNameWithBrackets_MacroBetweenTextParts_OneEvaluationStepsOccurred() throws Exception {
        String macro = "Click the UIElement (\"Button\"='Submit')#REF_DSL(1.2.3)#";
        validate(macro, "Click the UIElement (\"Button\"='Submit')test#",
                new EvaluationStep("REF_DSL", Lists.list("1.2.3"), "test"));
    }

    @Test
    public void noMacro_ParameterNameWithVariable_MacroBetweenTextParts_OneEvaluationStepsOccurred() throws Exception {
        String macro = "Fill the following parameters: (\"Market\"='${Country market}')";
        validate(macro, "Fill the following parameters: (\"Market\"='${Country market}')");
    }

    @Test
    public void noMacro_Regex_MacroBetweenTextParts_OneEvaluationStepsOccurred() throws Exception {
        String macro = "(.//*[contains(@class, 'consent-text')])[last()]";
        validate(macro, "(.//*[contains(@class, 'consent-text')])[last()]");
    }

    @Test
    public void noMacro_SqlSimple_MacroBetweenTextParts_OneEvaluationStepsOccurred() throws Exception {
        String macro = "some(o.object_id=r7.object_id and r7.attr_id=9147412578213883528 /* "
                       + "Serviceability Data */ and r7.reference=9153651356313208758 /* Magyar Telekom xPON 150 (60))";
        validate(macro, "some(o.object_id=r7.object_id and r7.attr_id=9147412578213883528 /* "
                        + "Serviceability Data */ and r7.reference=9153651356313208758 /* Magyar Telekom xPON 150 "
                        + "(60))");
    }

    @Test
    public void noMacro1_SqlSimple_MacroBetweenTextParts_OneEvaluationStepsOccurred() throws Exception {
        String macro = "some(asfdsfds)";
        validate(macro, "some(asfdsfds)");
    }

    @Test
    public void noMacro_SqlDifficult_MacroBetweenTextParts_OneEvaluationStepsOccurred() throws Exception {
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
        validate(macro, "Store SQL \"select o.object_id from nc_objects o join nc_addresses a on a.id=o.object_id join"
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
                        + "local context");
    }

    @Test
    public void singleMacroJustEmpty_MacroBetweenTextParts_OneEvaluationStepsOccurred() throws Exception {
        String macro = "Bla Bla $UUID()";
        validate(macro, "Bla Bla asb",
                new EvaluationStep("UUID", Lists.emptyList(), "asb"));
    }

    @Test
    public void singleMacroJustEmptyQuotes_MacroBetweenTextParts_OneEvaluationStepsOccurred() throws Exception {
        String macro = "Bla Bla #UUID('')";
        validate(macro, "Bla Bla asb",
                new EvaluationStep("UUID", Lists.emptyList(), "asb"));
    }

    @Test
    public void macrosWithQuotesAndSubMacros_SubMacrosAsFirstParam_TwoEvaluationStepsOccurred() throws Exception {
        String macro = "Bla Bla #UUID('#UUID('a','b')','c')";
        validate(macro, "Bla Bla passed",
                new EvaluationStep("UUID", Lists.list("a", "b"), "d"),
                new EvaluationStep("UUID", Lists.list("d", "c"), "passed"));
    }

    @Test
    public void macrosWithQuotesAndSubMacros_SubMacrosAsSecondParam_TwoEvaluationStepsOccurred() throws Exception {
        String macro = "Bla Bla #UUID('a','#UUID('b','c')')";
        validate(macro, "Bla Bla passed",
                new EvaluationStep("UUID", Lists.list("b", "c"), "d"),
                new EvaluationStep("UUID", Lists.list("a", "d"), "passed"));
    }

    @Test
    public void macrosWithQuotesAndSubMacros_SubMacrosWithQuotesInParam_TwoEvaluationStepsOccurred() throws Exception {
        String macro = "#GET_MONTH('2019-01-25T12:01:41.515Z', 'yyyy-MM-dd\\'T\\'HH:mm:ss.SSS\\'Z\\'')";
        validate(macro, "passed",
                new EvaluationStep("GET_MONTH", Lists.list("2019-01-25T12:01:41.515Z", "yyyy-MM-dd\\'T\\'HH:mm:ss"
                                                                                       + ".SSS\\'Z\\'"), "passed"));
        macro = "#GET_MONTH('2019-01-25T12:01:41.515Z', 'yyyy-MM-dd\\'T\\'HH:mm:ss.SSS\\'Z\\'')";
        validate(macro, "passed",
                new EvaluationStep("GET_MONTH", Lists.list("2019-01-25T12:01:41.515Z", "yyyy-MM-dd\\'T\\'HH:mm:ss"
                                                                                       + ".SSS\\'Z\\'"), "passed"));
    }

    @Test
    public void threeMacroParams_MacroBetweenTextParts_OneEvaluationStepsOccurred() throws Exception {
        String macro = "$UUID('1','abs', 'three')";
        validate(macro, "asb",
                new EvaluationStep("UUID", Lists.list("1", "abs", "three"), "asb"));
    }

    @Test
    public void singleMacroParams_MacroBetweenTextParts_OneEvaluationStepsOccurred() throws Exception {
        String macro = "$UUID('1','abs')";
        validate(macro, "asb",
                new EvaluationStep("UUID", Lists.list("1", "abs"), "asb"));
    }

    private static abstract class TestMacroRegistry implements MacroRegistry {
        private final Macros macros = new Macros();

        @Nullable
        @Override
        public Macros getMacros(@Nonnull String key) {
            if (!MACROSES.contains(key)) {
                return null;
            }
            return macros;
        }
    }

    private static class TestContext extends SimpleContext {

        public TestContext() {
        }

        public TestContext(@Nonnull SimpleContext parent, @Nonnull MacrosParser.MacrosStartContext macro,
                           @Nonnull Macros macros, @Nonnull Evaluator evaluator) {
            super(parent, macro, macros, evaluator);
        }

        @Override
        protected void notifyMacroStarts(@Nonnull MacrosParser.MacrosStartContext macro, @Nonnull Macros macros,
                                         @Nonnull Evaluator evaluator) {
            super.notifyMacroStarts(macro, macros, evaluator);
            String macroStart = macro.MACROS().getText();
            MACRO_NAME.set(this, macroStart.substring(1, macroStart.length()-1));
        }

        @Nonnull
        @Override
        protected SimpleContext createChildContext(@Nonnull MacrosParser.MacrosStartContext ctx,
                                                   @Nonnull Macros macros, @Nonnull Evaluator evaluator) {
            return new TestContext(this, ctx, macros, evaluator);
        }

        @Override
        protected SimpleContext createRootContext() {
            return new TestContext();
        }

        @Override
        protected void resetMacro() {
            super.resetMacro();
            MACRO_NAME.remove(this);
        }
    }

    private static class EvaluationStep {

        public final String macroName;
        public final List<String> macroArgs;
        public final String result;

        private EvaluationStep(String macroName, List<String> macroArgs, String result) {
            this.macroName = macroName;
            this.macroArgs = macroArgs;
            this.result = result;
        }

        public String calculate(SimpleContext context) throws CtxEvalException {
            String macroName = MACRO_NAME.getNonnull(context);
            Assertions.assertEquals(this,
                    new EvaluationStep(macroName, context.args, result));
            return result;
        }

        @Override
        public String toString() {
            return "#" + macroName + "(" + macroArgs + ")=" + result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof EvaluationStep)) {
                return false;
            }
            EvaluationStep that = (EvaluationStep) o;
            return Objects.equals(macroName, that.macroName) &&
                   Objects.equals(macroArgs, that.macroArgs) &&
                   Objects.equals(result, that.result);
        }

        @Override
        public int hashCode() {
            return Objects.hash(macroName, macroArgs, result);
        }
    }

}
