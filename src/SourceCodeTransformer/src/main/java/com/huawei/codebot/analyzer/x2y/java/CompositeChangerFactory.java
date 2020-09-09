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

import com.huawei.codebot.analyzer.x2y.global.GlobalSettings;
import com.huawei.codebot.analyzer.x2y.gradle.coditionalchanger.ConditionalChanger;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.GradleModificationChanger;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.GradleWarningChanger;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.versionvariable.GradleDependencyChanger;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.versionvariable.GradleGlobalChanger;
import com.huawei.codebot.analyzer.x2y.io.config.ConfigService;
import com.huawei.codebot.analyzer.x2y.java.clazz.delete.ClassDeleteChanger;
import com.huawei.codebot.analyzer.x2y.java.clazz.rename.ClassRenameChanger;
import com.huawei.codebot.analyzer.x2y.java.field.access.FieldAccessChanger;
import com.huawei.codebot.analyzer.x2y.java.field.delete.FieldDeleteChanger;
import com.huawei.codebot.analyzer.x2y.java.g2x.AppFileCheckChanger;
import com.huawei.codebot.analyzer.x2y.java.lazyfix.LazyFixChanger;
import com.huawei.codebot.analyzer.x2y.java.member.MemberReplaceChanger;
import com.huawei.codebot.analyzer.x2y.java.method.delete.MethodDeleteChanger;
import com.huawei.codebot.analyzer.x2y.java.method.replace.MethodReplaceChanger;
import com.huawei.codebot.analyzer.x2y.java.other.complexchanger.ComplexStartupActivityChanger;
import com.huawei.codebot.analyzer.x2y.java.other.objectequals.ObjectEqualsChanger;
import com.huawei.codebot.analyzer.x2y.java.other.specificchanger.SpecificModificationChanger;
import com.huawei.codebot.analyzer.x2y.java.pkg.delete.PackageDeleteChanger;
import com.huawei.codebot.analyzer.x2y.java.pkg.rename.PackageRenameChanger;
import com.huawei.codebot.analyzer.x2y.java.reflection.ReflectRenameChanger;
import com.huawei.codebot.analyzer.x2y.xml.G2XXmlModificationChanger;
import com.huawei.codebot.analyzer.x2y.xml.XmlModificationChanger;
import com.huawei.codebot.framework.AsyncedCompositeDefectFixer;
import com.huawei.codebot.framework.DefectFixerType;
import com.huawei.codebot.framework.FixBotArguments;
import com.huawei.codebot.framework.FixerInfo;
import com.huawei.codebot.framework.GenericDefectFixer;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;
import com.huawei.codebot.framework.model.DefectInstance;
import com.huawei.codebot.utils.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This factory create a composite changer that composes a series of child changer in a specific order
 * so that accomplishes process correctly.
 *
 * @author sirnple
 * @since 2020/5/6
 */
public final class CompositeChangerFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompositeChangerFactory.class);

    private static final List<String> G2H_CHANGER_ORDER = new ArrayList<>();
    private static final List<String> G2X_CHANGER_ORDER = new ArrayList<>();
    private static final List<String> G2H_ONLY_CHECK_ORDER = new ArrayList<>();

    static {
        initOrder();
    }

    private static void initOrder() {
        initG2XOrder();
        initG2HOrder();
        initG2HOnlyCheck();
    }

    private static void initG2HOrder() {
        G2H_CHANGER_ORDER.add(G2HFirstPhaseChanger.class.getCanonicalName());
        G2H_CHANGER_ORDER.add(GradleWarningChanger.class.getCanonicalName());
        G2H_CHANGER_ORDER.add(ComplexStartupActivityChanger.class.getCanonicalName());
        G2H_CHANGER_ORDER.add(ConditionalChanger.class.getCanonicalName());
        G2H_CHANGER_ORDER.add(GradleModificationChanger.class.getCanonicalName());
        G2H_CHANGER_ORDER.add(XmlModificationChanger.class.getCanonicalName());
        G2H_CHANGER_ORDER.add(SpecificModificationChanger.class.getCanonicalName());
        G2H_CHANGER_ORDER.add(MemberReplaceChanger.class.getCanonicalName());
        G2H_CHANGER_ORDER.add(ClassRenameChanger.class.getCanonicalName());
        G2H_CHANGER_ORDER.add(PackageRenameChanger.class.getCanonicalName());
        G2H_CHANGER_ORDER.add(ReflectRenameChanger.class.getCanonicalName());
        G2H_CHANGER_ORDER.add(LazyFixChanger.class.getCanonicalName());
        G2H_CHANGER_ORDER.add(PackageDeleteChanger.class.getCanonicalName());
    }

    private static void initG2XOrder() {
        G2X_CHANGER_ORDER.add(G2HFirstPhaseChanger.class.getCanonicalName());
        if (!GlobalSettings.isIsSDK()) {
            G2X_CHANGER_ORDER.add(AppFileCheckChanger.class.getCanonicalName());
        }
        G2X_CHANGER_ORDER.add(GradleWarningChanger.class.getCanonicalName());
        G2X_CHANGER_ORDER.add(ComplexStartupActivityChanger.class.getCanonicalName());
        G2X_CHANGER_ORDER.add(ConditionalChanger.class.getCanonicalName());
        G2X_CHANGER_ORDER.add(GradleModificationChanger.class.getCanonicalName());
        G2X_CHANGER_ORDER.add(G2XXmlModificationChanger.class.getCanonicalName());
        G2X_CHANGER_ORDER.add(SpecificModificationChanger.class.getCanonicalName());
        G2X_CHANGER_ORDER.add(ObjectEqualsChanger.class.getCanonicalName());
        G2X_CHANGER_ORDER.add(MethodReplaceChanger.class.getCanonicalName());
        G2X_CHANGER_ORDER.add(FieldAccessChanger.class.getCanonicalName());
        G2X_CHANGER_ORDER.add(ClassRenameChanger.class.getCanonicalName());
        G2X_CHANGER_ORDER.add(PackageRenameChanger.class.getCanonicalName());
        G2X_CHANGER_ORDER.add(ReflectRenameChanger.class.getCanonicalName());
        G2X_CHANGER_ORDER.add(LazyFixChanger.class.getCanonicalName());
        G2X_CHANGER_ORDER.add(PackageDeleteChanger.class.getCanonicalName());
    }

    private static void initG2HOnlyCheck() {
        G2H_ONLY_CHECK_ORDER.add(FieldDeleteChanger.class.getCanonicalName());
        G2H_ONLY_CHECK_ORDER.add(ClassDeleteChanger.class.getCanonicalName());
        G2H_ONLY_CHECK_ORDER.add(MethodDeleteChanger.class.getCanonicalName());
        G2H_ONLY_CHECK_ORDER.add(GradleGlobalChanger.class.getCanonicalName());
        G2H_ONLY_CHECK_ORDER.add(GradleDependencyChanger.class.getCanonicalName());
        G2H_ONLY_CHECK_ORDER.add(GradleWarningChanger.class.getCanonicalName());
        G2H_ONLY_CHECK_ORDER.add(GradleModificationChanger.class.getCanonicalName());
        G2H_ONLY_CHECK_ORDER.add(XmlModificationChanger.class.getCanonicalName());
        G2H_ONLY_CHECK_ORDER.add(SpecificModificationChanger.class.getCanonicalName());
        G2H_ONLY_CHECK_ORDER.add(MemberReplaceChanger.class.getCanonicalName());
        G2H_ONLY_CHECK_ORDER.add(ClassRenameChanger.class.getCanonicalName());
        G2H_ONLY_CHECK_ORDER.add(PackageRenameChanger.class.getCanonicalName());
        G2H_ONLY_CHECK_ORDER.add(ReflectRenameChanger.class.getCanonicalName());
    }

    /**
     * Create an AutoChanger as a top changer for this program.
     *
     * @param type        Identify this AutoChanger's type, only 3 valid value, {@link DefectFixerType#WISEHUB}
     *                    or {@link DefectFixerType#LIBADAPTION} or {@code null}.
     * @param isOnlyCheck Identify if return an only check changer.
     * @return An AutoChanger.
     */
    public static GenericDefectFixer newAutoChanger(DefectFixerType type, boolean isOnlyCheck) {
        if (DefectFixerType.LIBADAPTION.equals(type)) {
            return isOnlyCheck ?
                    newAutoChanger(type,
                            true, G2H_ONLY_CHECK_ORDER, new ChangerSetParser().filterChangerSet(type)) :
                    newAutoChanger(type,
                            false, G2H_CHANGER_ORDER, new ChangerSetParser().filterChangerSet(type));
        } else {
            return isOnlyCheck ?
                    newAutoChanger(type,
                            true, G2H_ONLY_CHECK_ORDER, new ChangerSetParser().filterChangerSet(type)) :
                    newAutoChanger(type,
                            false, G2X_CHANGER_ORDER, new ChangerSetParser().filterChangerSet(type));
        }
    }

    /**
     * Create an AutoChanger as a top changer for this program.
     *
     * @param type         Identify this AutoChanger's type, only 3 valid value, {@link DefectFixerType#WISEHUB}
     *                     or {@link DefectFixerType#LIBADAPTION} or {@code null}.
     * @param isOnlyCheck  Identify if return an only check changer.
     * @param changerOrder Child changer order of this AutoChanger, identified by class qualified name.
     * @param changerSet   Child changer that need to create, also identified by class qualified name.
     * @return An AutoChanger.
     * @throws IllegalArgumentException If the type is not {@link DefectFixerType#WISEHUB} or
     *                                  {@link DefectFixerType#LIBADAPTION} or {@code null}.
     */
    public static GenericDefectFixer
    newAutoChanger(DefectFixerType type, boolean isOnlyCheck, List<String> changerOrder, Set<String> changerSet) {
        if (type != null && !DefectFixerType.WISEHUB.equals(type) && !DefectFixerType.LIBADAPTION.equals(type)) {
            throw new IllegalArgumentException("Fail to create AutoChanger, unsupported type - " + type.toString());
        }
        if (isOnlyCheck) {
            return new G2HOnlyCheckChanger(type == null ? null : type.toString())
                    .setAtomicFixers(createAtomicFixers(changerOrder, changerSet, type));
        } else {
            return new GeneralAutoChanger(type).setAtomicFixers(createAtomicFixers(changerOrder, changerSet, type));
        }
    }

    /**
     * Create changers of candidateChangers in an order like changerOrder by reflect.
     * <br/>
     * Note that you need update this method if a new changer is added and this new changer hasn't a blank constructor
     * or a constructor with a string parameter. ({@code NewChanger()} or {@code NewChanger(String s)})
     *
     * @param changerOrder      A list of class qualified name.
     * @param candidateChangers A set of class qualified name.
     * @param type              Fix type.
     * @return A ordered list of changers.
     * @throws NullPointerException If changerOrder or candidateChangers is null.
     */
    private static ArrayList<GenericDefectFixer>
    createAtomicFixers(List<String> changerOrder, Set<String> candidateChangers, DefectFixerType type) {
        if (changerOrder == null || candidateChangers == null || type == null) {
            throw new NullPointerException("changerOrder and candidateChangers and type can't be null");
        }
        ArrayList<GenericDefectFixer> atomicFixers = new ArrayList<>();
        for (String changerQualifiedName : changerOrder) {
            if (!candidateChangers.contains(changerQualifiedName)) {
                continue;
            }
            try {
                Class<GenericDefectFixer> clazz = (Class<GenericDefectFixer>) Class.forName(changerQualifiedName);
                GenericDefectFixer atomicFixer;
                if (LazyFixChanger.class.getCanonicalName().equals(changerQualifiedName)
                        || GradleDependencyChanger.class.getCanonicalName().equals(changerQualifiedName)
                        || GradleGlobalChanger.class.getCanonicalName().equals(changerQualifiedName)) {
                    atomicFixer = clazz.newInstance();
                } else {
                    Constructor<GenericDefectFixer> constructor = clazz.getDeclaredConstructor(String.class);
                    constructor.setAccessible(true);
                    atomicFixer = constructor.newInstance(type.toString());
                }
                atomicFixers.add(atomicFixer);
            } catch (ClassNotFoundException
                    | InstantiationException
                    | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                LOGGER.error("Fail to create instance for class [{}]", changerQualifiedName, e);
            }
        }
        // Log changer order reader-friendly
        StringBuilder sb = new StringBuilder();
        atomicFixers.forEach(fixer -> sb.append(fixer.getClass().getCanonicalName()).append(System.lineSeparator()));
        if (atomicFixers.size() > 0) {
            LOGGER.debug("Actually changer order:{}{}", System.lineSeparator(), sb);
        }

        return atomicFixers;
    }

    /**
     * Identify the type of fixer
     *
     * @param fixer GenericDefectFixer instance
     * @return true: fixer is an instance of GeneralAutoChanger; false: on the opposite.
     */
    public static boolean isGeneralAutoChanger(GenericDefectFixer fixer) {
        return fixer instanceof GeneralAutoChanger;
    }

    /**
     * Get the atomicFixers of the GenericDefectFixer instance
     *
     * @param fixer GenericDefectFixer instance
     * @return the result of getAtomicFixers method
     */
    public static ArrayList<GenericDefectFixer> getGeneralAtomicFixers(GenericDefectFixer fixer) {
        return isGeneralAutoChanger(fixer) ? ((GeneralAutoChanger) fixer).getAtomicFixers() : null;
    }

    private static class GeneralAutoChanger extends BaseAutoChanger {
        private DefectFixerType type;

        /**
         * get Atomic Fixers
         *
         * @return ArrayList<GenericDefectFixer> atomicFixers
         */
        public ArrayList<GenericDefectFixer> getAtomicFixers() {
            return this.atomicFixers;
        }

        public GeneralAutoChanger(DefectFixerType type) {
            this.type = type;
        }

        private GeneralAutoChanger setAtomicFixers(List<GenericDefectFixer> changerList) {
            if (changerList instanceof ArrayList) {
                this.atomicFixers = (ArrayList<GenericDefectFixer>) changerList;
            } else {
                throw new IllegalArgumentException("changerList show be ArrayList");
            }
            return this;
        }

        @Override
        protected void initializeAtomicFixers() {
        }

        @Override
        public FixerInfo getFixerInfo() {
            if (this.info == null) {
                FixerInfo info = new FixerInfo();
                info.type = this.type;
                info.description = null;
                this.info = info;
            }
            return this.info;
        }
    }

    private static class G2HOnlyCheckChanger extends AsyncedCompositeDefectFixer {
        public G2HOnlyCheckChanger(String fixerType) {
            focusedFileExtensions = new String[]{"java", "kt", "gradle", "xml"};
            defaultIgnoreList = new String[]{".google", ".opensource", ".git"};
        }

        private G2HOnlyCheckChanger setAtomicFixers(List<GenericDefectFixer> changerList) {
            if (changerList instanceof ArrayList) {
                this.atomicFixers = (ArrayList<GenericDefectFixer>) changerList;
            } else {
                throw new IllegalArgumentException("changerList show be ArrayList");
            }
            return this;
        }

        @Override
        protected void initializeAtomicFixers() throws CodeBotRuntimeException {
        }

        @Override
        public FixerInfo getFixerInfo() {
            return null;
        }

        @Override
        public void preprocessAndAutoFix(FixBotArguments args) throws CodeBotRuntimeException {
            super.preprocessAndAutoFix(args);
        }

        @Override
        public void mergeDuplicateFixedLines(List<DefectInstance> defectInstances) {
            if (defectInstances != null) {
                changeDefect2OnlyChange(defectInstances);
                super.mergeDuplicateFixedLines(defectInstances);
            }
        }

        private void changeDefect2OnlyChange(List<DefectInstance> defectWarnings) {
            for (DefectInstance instanceTemp : defectWarnings) {
                if (instanceTemp.lazyBuggyLines.size() != 0) {
                    instanceTemp.buggyLines = instanceTemp.lazyBuggyLines;
                    instanceTemp.fixedLines = instanceTemp.lazyFixedLines;
                    instanceTemp.lazyBuggyLines = null;
                    instanceTemp.lazyFixedLines = null;
                }
                instanceTemp.isFixed = false;
            }
        }
    }

    /**
     * Parse a changer set that contains all changers we need to create according to given config json files.
     *
     * @author sirnple
     * @since 2020/4/15
     */
    private static final class ChangerSetParser {
        private static final String DEFAULT_MAPPING_FILE = "app_config/ConfigChangerMapping.json";

        private static final Logger LOGGER = LoggerFactory.getLogger(ChangerSetParser.class);
        private Map<List<String>, List<String>> configChangerMapping = new HashMap<>();

        public ChangerSetParser() {
            JSONObject jsonObject;
            try (InputStream in = ChangerSetParser.class.getClassLoader().getResourceAsStream(DEFAULT_MAPPING_FILE)){
                assert in != null;
                jsonObject = new JSONObject(IOUtils.toString(in, Charset.defaultCharset()));
            } catch (IOException e) {
                throw new IllegalStateException("Fail to read config mapping file in class path: " + DEFAULT_MAPPING_FILE);
            }

            JSONArray jsonArray = jsonObject.getJSONArray("configChangerMapping");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject configChangerPair = jsonArray.getJSONObject(i);
                JSONArray configs = configChangerPair.getJSONArray("configs");
                JSONArray changers = configChangerPair.getJSONArray("changers");
                List<String> configList = new ArrayList<>();
                List<String> changerList = new ArrayList<>();
                configs.forEach(config -> configList.add((String) config));
                changers.forEach(changer -> changerList.add((String) changer));
                this.configChangerMapping.put(configList, changerList);
            }
        }

        private Set<String> filterChangerSet(DefectFixerType type) {
            List<String> givenConfigPathList = ConfigService.listAllConfigFilesByType(type);
            Set<String> changerSet = new HashSet<>();
            if (givenConfigPathList.size() == 0) {
                LOGGER.warn("Can't parse any changer, check if there are corresponding config file for check type {}",
                        type.toString());
                return changerSet;
            }
            List<String> givenConfigFileNameList = new ArrayList<>(givenConfigPathList.size());
            for (String filePath : givenConfigPathList) {
                String fileName = FileUtils.getFileName(filePath);
                givenConfigFileNameList.add(fileName);
            }
            this.configChangerMapping.keySet().forEach(key -> {
                if (givenConfigFileNameList.containsAll(key)) {
                    changerSet.addAll(this.configChangerMapping.get(key));
                    LOGGER.debug("Config : Changer mapping - {} : {}", key, this.configChangerMapping.get(key));
                }
            });
            LOGGER.debug("Detect changers(size: {}) - {}", changerSet.size(), changerSet);
            return changerSet;
        }
    }


}
