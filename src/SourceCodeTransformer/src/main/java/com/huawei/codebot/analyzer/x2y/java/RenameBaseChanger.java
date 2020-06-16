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
import com.huawei.codebot.framework.lazyfix.LazyFixUtil;
import com.huawei.codebot.framework.model.DefectInstance;
import com.huawei.codebot.framework.x2y.AndroidAppFixer;

import org.apache.commons.collections4.map.MultiKeyMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

/**
 * An abstract class for all rename changers. It contains common operation of all rename changers.
 *
 * @since 2020-04-22
 */
public abstract class RenameBaseChanger extends AndroidAppFixer {
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

    @Override
    protected void mergeDuplicateFixedLines(List<DefectInstance> defectInstances) {
        ListIterator<DefectInstance> it = defectInstances.listIterator(defectInstances.size());
        MultiKeyMap map = new MultiKeyMap();
        MultiKeyMap map2 = new MultiKeyMap();
        while (it.hasPrevious()) {
            DefectInstance defectInstance = it.previous();
            if (LazyFixUtil.isLazyFixDefectInstance(defectInstance)) {
                continue;
            }
            if (map.containsKey(
                defectInstance.mainBuggyFilePath, defectInstance.mainBuggyLineNumber, defectInstance.status)) {
                addDefectInstanceMessage(map, map2, defectInstance);
                it.remove();
            } else {
                map.put(defectInstance.mainBuggyFilePath, defectInstance.mainBuggyLineNumber, defectInstance.status,
                    defectInstance);
                Set<String> set = new HashSet<>();
                set.add(defectInstance.message);
                map2.put(defectInstance.mainBuggyFilePath, defectInstance.mainBuggyLineNumber, defectInstance.status,
                    set);
            }
        }
    }

    private void addDefectInstanceMessage(MultiKeyMap map, MultiKeyMap map2, DefectInstance defectInstance) {
        Object object =
            map2.get(
                defectInstance.mainBuggyFilePath,
                defectInstance.mainBuggyLineNumber,
                defectInstance.status);
        if (object instanceof HashSet) {
            Set<String> set = (HashSet<String>) object;
            if (!set.contains(defectInstance.message)) {
                Object objectTemp =
                    map.get(
                        defectInstance.mainBuggyFilePath,
                        defectInstance.mainBuggyLineNumber,
                        defectInstance.status);
                if (objectTemp instanceof DefectInstance) {
                    DefectInstance target = (DefectInstance) objectTemp;
                    if (defectInstance.message != null && target.message == null) {
                        target.message = defectInstance.message;
                    } else if (defectInstance.message != null) {
                        target.message = defectInstance.message + "," + target.message;
                    }
                    set.add(defectInstance.message);
                }
            }
        }
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
        if (originalString.contains(oldFullName)) {
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

        if (originalString.contains(oldClassName)) {
            // The second way --- Full short name.
            // We return full short name in a string array like {"old full short name", "new full short name"}.
            return new String[] {oldClassName, newClassName};
        }

        // The third way --- Partial short name.
        // We should determine how many class this partial short name contains, then return the right partial short
        // name in a string array like {"old partial short name", "new partial short name"}.
        int length = 1;
        // Take the invoked class member.
        StringBuilder tempMethod = new StringBuilder(splitOldClassName[splitOldClassName.length - 1]);
        // Determine how many class the raw line invoke contains.
        for (int i = splitOldClassName.length - 2; i >= 0; i--) {
            if (originalString.contains(tempMethod)) {
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
}
