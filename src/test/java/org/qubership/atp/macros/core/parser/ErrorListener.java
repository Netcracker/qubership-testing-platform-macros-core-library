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

import java.io.StringWriter;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

public class ErrorListener extends BaseErrorListener {
    private String _symbol = "";
    private StringWriter _stream;

    public ErrorListener(StringWriter stream) {
        this._stream = stream;
    }

    public String getSymbol() {
        return _symbol;
    }

    public StringWriter getStream() {
        return _stream;
    }

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer,
                            Object offendingSymbol,
                            int line,
                            int charPositionInLine,
                            String msg,
                            RecognitionException e) {
        this._stream.write(msg);
        this._stream.write(System.getProperty("line.separator"));

        if (offendingSymbol.getClass() == CommonToken.class) {
            CommonToken token = (CommonToken) offendingSymbol;
            this._symbol = token.getText();
        }
    }
}
