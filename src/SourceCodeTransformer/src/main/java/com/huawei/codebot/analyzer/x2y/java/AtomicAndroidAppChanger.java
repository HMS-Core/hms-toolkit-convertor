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

import com.huawei.codebot.codeparsing.Shielder;
import com.huawei.codebot.framework.FixerInfo;
import com.huawei.codebot.framework.lazyfix.LazyFixUtil;
import com.huawei.codebot.framework.model.DefectInstance;
import com.huawei.codebot.framework.x2y.AndroidAppFixer;
import org.apache.commons.collections4.map.MultiKeyMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.Iterator;

/**
 * Atomic Android App Changer
 * this class is used to add method -> mergeDuplicateFixedLines
 *
 * @since 3.0.2
 */
public class AtomicAndroidAppChanger extends AndroidAppFixer {

    @Override
    protected void mergeDuplicateFixedLines(List<DefectInstance> defectInstances) {
        ListIterator<DefectInstance> it = defectInstances.listIterator(defectInstances.size());
        MultiKeyMap map = new MultiKeyMap();
        MultiKeyMap map2 = new MultiKeyMap();
        while (it.hasPrevious()) {
            DefectInstance defectInstance = it.previous();
            if (!LazyFixUtil.isLazyFixDefectInstance(defectInstance)) {
                if (map.containsKey(
                        defectInstance.mainBuggyFilePath, defectInstance.mainBuggyLineNumber, defectInstance.status)) {
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
                    it.remove();
                } else {
                    map.put(
                            defectInstance.mainBuggyFilePath,
                            defectInstance.mainBuggyLineNumber,
                            defectInstance.status,
                            defectInstance);
                    Set<String> set = new HashSet<>();
                    set.add(defectInstance.message);
                    map2.put(
                            defectInstance.mainBuggyFilePath,
                            defectInstance.mainBuggyLineNumber,
                            defectInstance.status,
                            set);
                }
            }
        }
    }

    @Override
    protected List<DefectInstance> detectDefectsInJavaFile(String s) {
        return null;
    }

    @Override
    protected List<DefectInstance> detectDefectsInXMLFile(String s) {
        return null;
    }

    @Override
    protected List<DefectInstance> detectDefectsInGradleFile(String s) {
        return null;
    }

    @Override
    protected List<DefectInstance> detectDefectsInKotlinFile(String s) {
        return null;
    }

    @Override
    protected void generateFixCode(DefectInstance defectInstance) {
    }

    @Override
    protected void extractFixInstancesForSingleCodeFile(String s) {
    }

    @Override
    public FixerInfo getFixerInfo() {
        return null;
    }

    /**
     * Check the line number of defectInstance, if this line need to be ignore, then remove it.
     *
     * @param defectInstances defectInstances
     * @param shielder shielder
     */
    public void removeIgnoreBlocks(List<DefectInstance> defectInstances, Shielder shielder) {
        Iterator<DefectInstance> defectInstanceIterator = defectInstances.listIterator();
        while (defectInstanceIterator.hasNext()){
            DefectInstance defectInstance = defectInstanceIterator.next();
            if (shielder.shouldIgnore(defectInstance.getMainBuggyLineNumber())){
                defectInstanceIterator.remove();
            }
        }
    }
}
