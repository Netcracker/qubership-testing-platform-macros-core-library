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

import java.util.Deque;
import java.util.LinkedList;

public class MacrosOpeningCounter {
    private final Deque<Integer> otherLevels;
    private int curValue = 0;

    MacrosOpeningCounter() {
        otherLevels = new LinkedList<>();
    }

    public void open() {
        curValue++;
    }

    /**
     * Is going upper on close.
     * @return boolean
     */
    public boolean isGoingUpperOnClose() {
        if (curValue == 0) {
            goUpper();
            return true;
        }
        curValue--;
        return false;
    }

    public void goDeeper() {
        otherLevels.add(curValue);
        curValue = 0;
    }

    private void goUpper() {
        curValue = otherLevels.removeLast();
    }
}
