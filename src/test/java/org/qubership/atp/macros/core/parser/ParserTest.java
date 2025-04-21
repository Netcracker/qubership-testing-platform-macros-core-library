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

package org.qubership.atp.macros.core.parser;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import org.qubership.atp.macros.core.parser.antlr4.MacrosBaseVisitor;
import org.qubership.atp.macros.core.parser.antlr4.MacrosLexer;
import org.qubership.atp.macros.core.parser.antlr4.MacrosParser;
import org.qubership.atp.macros.core.parser.antlr4.MacrosVisitor;

@RunWith(Parameterized.class)
public class ParserTest {
    private final String fInput;
    private final int tokenType;
    private ErrorListener errorListener;
    private MacrosLexer markupLexer;

    public ParserTest(String fInput,
                      int tokenType) {
        this.fInput = fInput;
        this.tokenType = tokenType;
    }

    @Parameterized.Parameters(name = "{index}: {0} {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {"anything in here", MacrosLexer.TEXT},
                {"#just_a text #", MacrosLexer.MACROS_MARKER},
                {"#(anything in here)", MacrosLexer.MACROS_MARKER},
                {"#just_a text#REF_DSL(1.2.3)#", MacrosLexer.MACROS_MARKER},
                {"#REF_DSL(InternationalRateCost.#REF_#REF_THIS(OriginCountry.Zone).#REF_THIS(DestinationCountry"
                        + ".Zone))", MacrosLexer.MACROS},
                {"#REF_DSL(InternationalRateCost.#REF_THIS(OriginCountry.Zone).#REF_THIS(DestinationCountry.Zone))",
                        MacrosLexer.MACROS},
                {"#REF_DSL(National Rates.#REF_THIS(Subscription.TariffName).#REF_THIS(UsageType))",
                        MacrosLexer.MACROS},
                {"#REF_DSL(#REF_THIS(OriginCountry.Zone))", MacrosLexer.MACROS},
                {"#REF_THIS(OriginCountry.Zone)", MacrosLexer.MACROS},
                {"#REF_DSL(Roaming SMS Rates.#REF_THIS(Account.AccountType).#REF_THIS(OriginCountry.Zone).#REF_THIS"
                        + "(DestinationCountry.Zone))", MacrosLexer.MACROS},
                {"#REF_DSL(InternationalRateCost.#REF_THIS(OriginCountry.Zone).#REF_THIS(DestinationCountry.Zone))",
                        MacrosLexer.MACROS},
                {"#RAND_UUID()", MacrosLexer.MACROS}
        });
    }


    @Test
    public void testText() {
        MacrosParser parser = setup(fInput);
        TokenStream ts = parser.getTokenStream();
        assertEquals("", this.errorListener.getSymbol());
        assertEquals(tokenType, ts.get(0).getType());
    }

    private MacrosParser setup(String input) {
        ANTLRInputStream inputStream = new ANTLRInputStream(input);
        markupLexer = new MacrosLexer(inputStream);
        markupLexer.removeErrorListeners();
        CommonTokenStream commonTokenStream = new CommonTokenStream(markupLexer);
        MacrosParser macrosParser = new MacrosParser(commonTokenStream);
        macrosParser.removeErrorListeners();
        errorListener = new ErrorListener(new StringWriter());
        macrosParser.addErrorListener(errorListener);
        MacrosParser.MacroArgContext context = macrosParser.macroArg();
        MacrosVisitor visitor = new MacrosBaseVisitor();
        visitor.visit(context);
        return macrosParser;
    }

}