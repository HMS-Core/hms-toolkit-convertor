/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.huawei.codebot.analyzer.x2y.java;

import com.huawei.codebot.framework.ChangeTrace;
import com.huawei.codebot.framework.model.DefectInstance;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.eclipse.jdt.core.compiler.ITerminalSymbols.TokenNameEOF;

/**
 * An abstract class for all rename changers. It contains common operation of all rename changers.
 *
 * @since 2020-04-22
 */
public abstract class RenameBaseChanger extends AtomicAndroidAppChanger {
    private static final Logger LOGGER = LoggerFactory.getLogger(RenameBaseChanger.class);
    /**
     * A map of old qualified name and new qualified name.
     */
    public Map<String, String> renamePatterns = new HashMap<>();
    /**
     * A map of old qualified name and it's corresponding description.
     */
    public Map<String, Map> fullName2Description = new HashMap<>();


    /**
     * Update <b>line2Change</b> according to <b>startLineNumber</b>
     * <br/>
     * The <b>line2Change</b> is a mapping of lineNumber and this line's {@link ChangeTrace},
     * <ul>
     *     <li>if <b>startLineNumber</b> already exists in <b>line2Change</b>， we retrieve it</li>
     *     <li>else we create a new one and put it into <b>line2Change</b></li>
     * </ul>
     * finally, we update this changeTrace instance
     *
     * @param line2Change a mapping of lineNumber and this line's {@link ChangeTrace}
     * @param lineText used to construct {@link ChangeTrace}
     * @param replacement used to construct {@link ChangeTrace}
     * @param startLineNumber used to construct {@link ChangeTrace} and update <b>line2Change</b>
     * @param startColumnNumber used to construct {@link ChangeTrace}
     * @param endColumnNumber used to construct {@link ChangeTrace}
     * @param desc used to construct {@link ChangeTrace}
     */
    public void updateChangeTraceForALine(
            Map<Integer, ChangeTrace> line2Change,
            String lineText,
            String replacement,
            int startLineNumber,
            int startColumnNumber,
            int endColumnNumber,
            String desc) {
        ChangeTrace changeTrace;
        if (line2Change.containsKey(startLineNumber)) {
            changeTrace = line2Change.get(startLineNumber);
        } else {
            changeTrace = new ChangeTrace(lineText);
            line2Change.put(startLineNumber, changeTrace);
        }
        changeTrace.addChange(startColumnNumber, endColumnNumber, replacement);
        changeTrace.addDesc(desc);
    }

    /**
     * Transfer all {@link ChangeTrace} to {@link DefectInstance}.
     *
     * @param buggyFilePath File path this changer is accessing.
     * @param line2Change A map of buggyLine line number and it's corresponding {@code ChangeTrace}.
     * @return A list of {@code DefectInstance} transferred from {@code line2Change}
     */
    protected List<DefectInstance> generateDefectInstancesFromChangeTrace(
            String buggyFilePath, Map<Integer, ChangeTrace> line2Change) {
        List<DefectInstance> defectInstanceList = new ArrayList<>();
        for (Map.Entry<Integer, ChangeTrace> entry : line2Change.entrySet()) {
            DefectInstance defectInstance =
                    createDefectInstance(
                            buggyFilePath,
                            entry.getKey(),
                            entry.getValue().getOriginalText(),
                            entry.getValue().getChangedText());
            defectInstance.setMessage(entry.getValue().getDesc());
            defectInstanceList.add(defectInstance);
        }
        return defectInstanceList;
    }

    /**
     * Depending on how the {@code originalString} invoked a class member by using different form name, we return a
     * corresponding name and this corresponding name's replacement.
     * <br/>
     * Actually, there are three way to invoke a class member by using name:
     * <ol>
     *     <li>Qualified name. e.g com.huawei.ClassA.ClassB.member_a</li>
     *     <li>Full short name, qualified name without package. e.g. ClassA.ClassB.member_a</li>
     *     <li>Partial Short name, full short name without some out class. e.g. ClassB.member_a</li>
     * </ol>
     *
     * @param originalString Raw line we get from source code.
     * @param oldFullName Qualified name we infer from raw code.
     * @param newFullName A new qualified name that the {@code oldFullName} should be replaced by it.
     * @return A string array contains two item, the first is old full name and the second is new full name.
     */
    protected String[] getExistShortNames(
            final String originalString, final String oldFullName, final String newFullName) {
        if (matchAllToken(originalString, oldFullName)) {
            // The first way --- Qualified name.
            // We return qualified name in a string array like {"old qualified name", "new qualified name"}.
            return new String[] {oldFullName, newFullName};
        }

        String oldClassName = extractNameWithoutPackage(oldFullName);
        String newClassName = extractNameWithoutPackage(newFullName);
        String[] splitOldClassName = oldClassName.split("\\.");
        String[] splitNewClassName = newClassName.split("\\.");
        String oldReturnName;
        String newReturnName;

        if (matchAllToken(originalString, oldClassName)) {
            // The second way --- Full short name.
            // We return full short name in a string array like {"old full short name", "new full short name"}.
            return new String[] {oldClassName, newClassName};
        } else {
            // The third way --- Partial short name.
            // We should determine how many class this partial short name contains, then return the right partial short
            // name in a string array like {"old partial short name", "new partial short name"}.
            int length = 1;
            // Take the invoked class member.
            StringBuilder tempMethod = new StringBuilder(splitOldClassName[splitOldClassName.length - 1]);
            // Determine how many class the raw line invoke contains.
            for (int i = splitOldClassName.length - 2; i >= 0; i--) {
                if (matchAllToken(originalString, tempMethod.toString())) {
                    length++;
                    tempMethod.insert(0, ".").insert(0, splitOldClassName[i]);
                } else {
                    break;
                }
            }
            length--;
            oldReturnName = tempMethod.toString().substring(tempMethod.toString().indexOf(".") + 1);

            // Cut the newFullName to match the oldFullName, this means they have the same number of outer class.
            if (length >= splitNewClassName.length) {
                newReturnName = newClassName;
            } else {
                StringBuilder returnTemp = new StringBuilder();
                for (int j = splitNewClassName.length - 1; j >= 0; j--) {
                    returnTemp.insert(0, ".").insert(0, splitNewClassName[j]);
                    length--;
                    if (length <= 0) {
                        break;
                    }
                }
                newReturnName = returnTemp.substring(0, returnTemp.length() - 1);
            }
        }
        return new String[] {oldReturnName, newReturnName};
    }

    private String extractNameWithoutPackage(String qualifiedName) {
        String[] splitName = qualifiedName.split("\\.");
        StringBuilder returnString = new StringBuilder();
        for (int i = 0; i < splitName.length; i++) {
            if (Character.isUpperCase(splitName[i].charAt(0))) {
                for (int j = i; j < splitName.length; j++) {
                    returnString.append(splitName[j]).append(".");
                }
                break;
            }
        }
        return returnString.substring(0, returnString.length() - 1);
    }

    /**
     * Return the outermost Class by given class qualified name. e.g. com.google.fire.A.B.C  -> com.google.fire.A
     *
     * @param className Qualified name of class.
     * @return The outermost class name string.
     */
    protected String getOutClassPart(String className) {
        String[] args = className.split("\\.");
        StringBuilder outClassString = new StringBuilder();
        for (String arg : args) {
            if (Character.isUpperCase(arg.charAt(0))) {
                outClassString.append(arg);
                break;
            } else {
                outClassString.append(arg).append(".");
            }
        }
        return outClassString.toString();
    }

    /**
     * Return a short name  by given qualified name.
     * <br/>
     * A short name is just like {@code ClassSimpleName.FieldSimpleName}.
     *
     * @param qualifiedName Full name of field like com.hwconvention.Class.field .
     * @return Short name of field like Class.field .
     */
    public String getShortName(String qualifiedName) {
        String[] names = qualifiedName.split("\\.");
        if (names.length >= 2) {
            StringBuilder result = new StringBuilder(names[names.length - 1]);
            for (int i = names.length - 2; i >= 0; i--) {
                char c = names[i].charAt(0);
                if (Character.isUpperCase(c)) {
                    result.insert(0, ".").insert(0, names[i]);
                }
            }
            return result.toString();
        }
        return qualifiedName;
    }

    private boolean matchAllToken(final String originalString, final String oldQualifiedName) {
        List<String> origTokenList = toTokenList(originalString);
        List<String> oldTokenList = toTokenList(oldQualifiedName);

        int matchTokenIndex = 0;
        for (int i = origTokenList.size() - 1; i >= 0; i--) {
            if (oldTokenList.get(oldTokenList.size() - 1).equals(origTokenList.get(i))) {
                matchTokenIndex = i;
                break;
            }
        }

        List<String> helperOrigTokenList = origTokenList.subList(0, matchTokenIndex + 1);
        Collections.reverse(oldTokenList);
        Collections.reverse(helperOrigTokenList);

        if (helperOrigTokenList.size() < oldTokenList.size()) {
            return false;
        }
        for (int i = 0; i < oldTokenList.size(); i++) {
            if (!oldTokenList.get(i).equals(helperOrigTokenList.get(i))) {
                return false;
            }
        }

        return true;
    }

    private List<String> toTokenList(final String str) {
        IScanner sc = ToolFactory.createScanner(false, false, false, false);
        sc.setSource(str.toCharArray());

        List<String> tokenList = new ArrayList<>();
        try {
            while (sc.getNextToken() != TokenNameEOF) {
                tokenList.add(String.valueOf(sc.getRawTokenSource()));
            }
        } catch (InvalidInputException e) {
            LOGGER.error("Error when trans String to TokenList, it shouldn't continue");
            throw new IllegalStateException(e);
        }
        return tokenList;
    }
}
