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

package com.huawei.codebot.analyzer.x2y.io.config;

import com.huawei.codebot.analyzer.x2y.global.GlobalSettings;
import com.huawei.codebot.analyzer.x2y.gradle.coditionalchanger.StructGradleXml;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructGradleHeadquarter;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model.StructGradleManual;
import com.huawei.codebot.analyzer.x2y.java.method.MethodChangePattern;
import com.huawei.codebot.analyzer.x2y.java.other.complexchanger.FixAction;
import com.huawei.codebot.analyzer.x2y.java.other.specificchanger.SpecificJsonPattern;
import com.huawei.codebot.analyzer.x2y.xml.XmlJsonPattern;
import com.huawei.codebot.framework.DefectFixerType;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;
import com.huawei.codebot.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Config Service
 * this service is the framework of read jsons
 * include
 * wisehub-auto/wisehub-auto-hms      wisehub-complex/wisehub-complex-hms    wisehub-crossfile/wisehub-crossfile
 * wisehub-gradle/wisehub-gradle-hms  wisehub-gradle-manual/wisehub-gradle-manual-hms
 * wisehub-maunal/wisehub-maunal-hms  wisehub-specific/wisehub-specific-hms wisehub-xml/wisehub-xml-hms
 *
 * @since 2020-04-01
 */
public class ConfigService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigService.class);

    private static ConfigService G2HInstance;
    private static ConfigService G2XInstance;
    private Set<String> specialMethodSet;

    // wisehub-auto-hms
    private Map<String, String> classRenamePatterns = new HashMap<>();
    private Map<String, Map> classRenameDescriptions = new HashMap<>();

    public Map<String, String> getClassRenamePatterns() {
        return this.classRenamePatterns;
    }

    public Map<String, Map> getClassRenameDescriptions() {
        return this.classRenameDescriptions;
    }

    private Map<String, List<MethodChangePattern>> methodRenamePattern = new HashMap<>();

    public Map<String, List<MethodChangePattern>> getMethodRenamePattern() {
        return this.methodRenamePattern;
    }

    private Map<String, String> fieldRenamePattern = new HashMap<>();
    private Map<String, Map> fieldRenameDescriptions = new HashMap<>();

    public Map<String, String> getFieldRenamePattern() {
        return this.fieldRenamePattern;
    }

    public Map<String, Map> getFieldRenameDescriptions() {
        return this.fieldRenameDescriptions;
    }

    private Map<String, String> packageRenamePatterns = new HashMap<>();
    private Map<String, Map> packageRenameDescriptions = new HashMap<>();

    public Map<String, String> getPackageRenamePatterns() {
        return this.packageRenamePatterns;
    }

    public Map<String, Map> getPackageRenameDescriptions() {
        return this.packageRenameDescriptions;
    }

    // wisehub-complex-hms
    private Map<String, FixAction> complexSpecificPatterns = new HashMap<>();

    public Map<String, FixAction> getComplexSpecificPatterns() {
        return this.complexSpecificPatterns;
    }

    private StructGradleHeadquarter complexGradleHeadquarter;

    public StructGradleHeadquarter getComplexGradleHeadquarter() {
        return this.complexGradleHeadquarter;
    }

    private XmlJsonPattern complexXmlPatterns;

    public XmlJsonPattern getComplexXmlPatterns() {
        return this.complexXmlPatterns;
    }

    // wisehub-crossfile-hms
    private List<StructGradleXml> conditionalConfig = new ArrayList<>();

    public List<StructGradleXml> getConditionalConfig() {
        return this.conditionalConfig;
    }

    // wisehub-gradle-hms
    private StructGradleHeadquarter gradleHeadquarter = new StructGradleHeadquarter();

    public StructGradleHeadquarter getGradleHeadquarter() {
        return this.gradleHeadquarter;
    }

    // wisehub-gradle-manual
    private List<StructGradleManual> gradleManuals = new ArrayList<>();

    public List<StructGradleManual> getGradleManuals() {
        return this.gradleManuals;
    }

    // wisehub-manual
    private Set<String> classDeletePatterns = new HashSet<>();
    private Map<String, Map> classDeleteDescriptions = new HashMap<>();

    public Set<String> getClassDeletePatterns() {
        return this.classDeletePatterns;
    }

    public Map<String, Map> getClassDeleteDescriptions() {
        return this.classDeleteDescriptions;
    }

    private HashMap<String, List<MethodChangePattern>> methodDelete = new HashMap<>();

    public HashMap<String, List<MethodChangePattern>> getMethodDelete() {
        return this.methodDelete;
    }

    private List<String> fieldDeletePatterns = new ArrayList<>();
    private Map<String, Map> fieldDeleteDescriptions = new HashMap<>();

    public List<String> getFieldDeletePatterns() {
        return this.fieldDeletePatterns;
    }

    public Map<String, Map> getFieldDeleteDescriptions() {
        return this.fieldDeleteDescriptions;
    }

    private List<String> packageDeletePatterns = new ArrayList<>();
    private Map<String, Map> packageDeleteDescriptions = new HashMap<>();

    public List<String> getPackageDeletePatterns() {
        return this.packageDeletePatterns;
    }

    public Map<String, Map> getPackageDeleteDescriptions() {
        return this.packageDeleteDescriptions;
    }


    // wisehub-xml-hms
    private XmlJsonPattern xmlJsonPattern = new XmlJsonPattern();

    public XmlJsonPattern getXmlJsonPattern() {
        return this.xmlJsonPattern;
    }

    // wisehub-specific
    private SpecificJsonPattern specificJsonPattern = new SpecificJsonPattern();

    public SpecificJsonPattern getSpecificJsonPattern() {
        return this.specificJsonPattern;
    }


    private ConfigService(String fixerType) throws CodeBotRuntimeException {
        readAllJsons(fixerType);
    }


    /**
     * engine should determine to use G2H or G2X
     *
     * @param fixerType is "libadaption" use G2H ; fixerType is "wisehub" use G2X ;
     * @return dataStruct G2HInstance or G2XInstance
     */
    public static synchronized ConfigService getInstance(String fixerType) throws CodeBotRuntimeException {
        if ("libadaption".equals(fixerType)) {
            G2XInstance = null;
            if (G2HInstance == null) {
                G2HInstance = new ConfigService(fixerType);
            }
            return G2HInstance;
        } else {
            G2HInstance = null;
            if (G2XInstance == null) {
                G2XInstance = new ConfigService(fixerType);
            }
            return G2XInstance;
        }
    }

    /**
     * return G2H config or G2X config
     * if both of them are empty return initialized G2H config
     *
     * @return G2H config or G2X config
     */
    public static synchronized ConfigService getInstance()  {
        if(G2HInstance !=null){
            return G2HInstance;
        }
        if(G2XInstance != null){
            return G2XInstance;
        }
        ConfigService initializeConfigService = null;
        try {
            initializeConfigService =  new ConfigService("libadaption");
        } catch (CodeBotRuntimeException e) {
            LOGGER.info("getInstance() is used when G2HInstance and G2HInstance both empty, we start it by G2H", e);
        }
        return initializeConfigService;
    }

    /**
     * fill special method set from method rename pattern and method delete list
     *
     * @return special method set
     */
    public Set<String> getSpecialMethodSet() {
        Map<String, List<MethodChangePattern>> autoMethod = this.getMethodRenamePattern();
        HashMap<String, List<MethodChangePattern>> manualMethod = this.getMethodDelete();
        Set<String> retainSet = new HashSet<>();
        retainSet.addAll(autoMethod.keySet());
        retainSet.retainAll(manualMethod.keySet());
        specialMethodSet = new HashSet<>();
        for (String retainString : retainSet) {
            if (autoMethod.get(retainString).size() == 1 || manualMethod.get(retainString).size() == 1) {
                specialMethodSet.add(retainString);
            }
        }
        return specialMethodSet;
    }

    /**
     * Config file enum.
     */
    public enum ConfigFileName {
        WISEHUB_AUTO(DefectFixerType.WISEHUB, "wisehub-auto.json"),
        WISHHUB_AUTO_HMS(DefectFixerType.LIBADAPTION, "wisehub-auto-hms.json"),
        WISEHUB_COMPLEX(DefectFixerType.WISEHUB, "wisehub-complex.json"),
        WISEHUB_COMPLEX_HMS(DefectFixerType.LIBADAPTION, "wisehub-complex-hms.json"),
        WISEHUB_CROSSFILE(DefectFixerType.WISEHUB, "wisehub-crossfile.json"),
        WISEHUB_CROSSFILE_HMS(DefectFixerType.LIBADAPTION, "wisehub-crossfile-hms.json"),
        WISEHUB_GRADLE(DefectFixerType.WISEHUB, "wisehub-gradle.json"),
        WISEHUB_GRADLE_HMS(DefectFixerType.LIBADAPTION, "wisehub-gradle-hms.json"),
        WISEHUB_GRADLE_MANUAL(DefectFixerType.WISEHUB, "wisehub-gradle-manual.json"),
        WISEHUB_GRADLE_MANUAL_HMS(DefectFixerType.LIBADAPTION, "wisehub-gradle-manual-hms.json"),
        WISEHUB_MANUAL(DefectFixerType.WISEHUB, "wisehub-manual.json"),
        WISEHUB_MANUAL_HMS(DefectFixerType.LIBADAPTION, "wisehub-manual-hms.json"),
        WISEHUB_SPECIFIC(DefectFixerType.WISEHUB, "wisehub-specific.json"),
        WISEHUB_SPECIFIC_HMS(DefectFixerType.LIBADAPTION, "wisehub-specific-hms.json"),
        WISEHUB_XML(DefectFixerType.WISEHUB, "wisehub-xml.json"),
        WISEHUB_XML_HMS(DefectFixerType.LIBADAPTION, "wisehub-xml-hms.json");

        private DefectFixerType type;
        private String fileName;

        ConfigFileName(DefectFixerType type, String fileName) {
            this.type = type;
            this.fileName = fileName;
        }

        public DefectFixerType getType() {
            return type;
        }

        public String getFileName() {
            return fileName;
        }
    }

    /**
     * Find config file in user.codemigrate.dir and classpath.
     *
     * @param type Check type, valid value {@link DefectFixerType#LIBADAPTION} and {@link DefectFixerType#WISEHUB}.
     * @return All config files path.
     * @throws IllegalStateException If can't find any config file in this two directories.
     */
    public static List<String> listAllConfigFilesByType(DefectFixerType type) {
        // Check config path
        String configPath = System.getProperty("user.codemigrate.dir");
        List<String> configPathList;
        configPathList = FileUtils.listAllFiles(
                configPath,
                new String[]{"json"}, null, ignoreFilesWithType(type, configPath), Long.MAX_VALUE);

        // Check classpath
        if (configPathList.size() == 0) {
            String[] classpaths = System.getProperty("java.class.path").split(System.getProperty("path.separator"));
            for (String classpath : classpaths) {
                if (classpath.endsWith(".jar")) {
                    Path jarPath = Paths.get(classpath);
                    Path parentPath = jarPath.getParent();
                    if (parentPath != null) {
                        configPath = Paths.get(parentPath.toString(), "config").toString();
                        configPathList = FileUtils.listAllFiles(configPath, new String[] { "json" }, null,
                                ignoreFilesWithType(type, configPath), Long.MAX_VALUE);
                    }
                }
            }
        }
        return configPathList;
    }

    private static String[] ignoreFilesWithType(DefectFixerType type, String configPath) {
        String[] ignoreFiles;
        List<String> list = new ArrayList<>();
        if (DefectFixerType.LIBADAPTION.equals(type)) {
            Arrays.stream(ConfigFileName.values())
                    .filter(configFileName -> DefectFixerType.WISEHUB.equals(configFileName.type))
                    .forEach(configFileName ->
                            list.add(Paths.get(configPath, configFileName.getFileName()).toString()));
            ignoreFiles = list.toArray(new String[0]);
        } else if (DefectFixerType.WISEHUB.equals(type)) {
            Arrays.stream(ConfigFileName.values())
                    .filter(configFileName -> DefectFixerType.LIBADAPTION.equals(configFileName.type))
                    .forEach(configFileName ->
                            list.add(Paths.get(configPath, configFileName.getFileName()).toString()));
            ignoreFiles = list.toArray(new String[0]);
        } else if (type == null) {
            ignoreFiles = new String[]{};
        } else {
            throw new IllegalArgumentException("Parameter [type] is illegal, " +
                    "valid value are enum DefectFixerType.LIBADAPTION and DefectFixerType.WISEHUB or null");
        }
        return ignoreFiles;
    }

    /**
     * Read all jsons and Store all data in specific data structures for each changer
     *
     * @param fixerType is "libadaption" use G2H ; fixerType is "wisehub" use G2X ;
     */
    private void readAllJsons(String fixerType) throws CodeBotRuntimeException {
        DefectFixerType type = DefectFixerType.fromValue(fixerType);
        // wisehub-auto-hms  or  wisehub-auto
        if (isInstantiatable(X2YAutoJSONConfig.class, type)) {
            X2YAutoJSONConfig configAutoJson = new X2YAutoJSONConfig(fixerType);
            this.classRenamePatterns = configAutoJson.getClassRenamePatterns();
            this.classRenameDescriptions = configAutoJson.getClassRenameDescription();
            this.methodRenamePattern = configAutoJson.getMethodReplacePatterns();
            this.fieldRenamePattern = configAutoJson.getFieldRenamePatterns();
            this.fieldRenameDescriptions = configAutoJson.getFieldDescription();
            this.packageRenamePatterns = configAutoJson.getPackageRenamePatterns();
            this.packageRenameDescriptions = configAutoJson.getPackageRenameDescription();
        }

        // wisehub-complex-hms
        if (isInstantiatable(ComplexChangerJSONConfig.class, type)) {
            ComplexChangerJSONConfig configComplex = new ComplexChangerJSONConfig(fixerType);
            this.complexSpecificPatterns = configComplex.getComplexSpecificPatterns();
            this.complexGradleHeadquarter = configComplex.getComplexGradleModificationPatterns();
            this.complexXmlPatterns = configComplex.getComplexXmlPatterns();
        }

        // wisehub-crossfile-hms  or  wisehub-crossfile
        if (isInstantiatable(CrossfileChangerJSONConfig.class, type)) {
            CrossfileChangerJSONConfig configCrossFile = new CrossfileChangerJSONConfig(fixerType);
            this.conditionalConfig = configCrossFile.getGradleXMLPatterns();
        }

        // wisehub-gradle-hms  or  wisehub-gradle
        if (isInstantiatable(GradleJSONConfig.class, type)) {
            GradleJSONConfig configGradle = new GradleJSONConfig(fixerType);
            this.gradleHeadquarter = configGradle.getGradleModificationPatterns();
        }

        // wisehub-gradle-manual-hms   or   wisehub-gradle-manual
        if (isInstantiatable(GradleManualJSONConfig.class, type)) {
            GradleManualJSONConfig configGradleManual = new GradleManualJSONConfig(fixerType);
            this.gradleManuals = configGradleManual.getGradleManual();
        }

        // wisehub-manual-hms  or  wisehub-manual
        if (isInstantiatable(X2YManualJSONConfig.class, type)) {
            X2YManualJSONConfig configManual = new X2YManualJSONConfig(fixerType);
            this.classDeletePatterns = configManual.getClassDeletePatterns();
            this.classDeleteDescriptions = configManual.getClassDeleteDescription();
            this.methodDelete = configManual.getDeleteMethodPatterns();
            this.fieldDeletePatterns = configManual.getFieldDeletePatterns();
            this.fieldDeleteDescriptions = configManual.getFieldDeleteDescriptions();
            this.packageDeletePatterns = configManual.getPackageDeletePatterns();
            this.packageDeleteDescriptions = configManual.getPackageDeleteDescriptions();
        }

        // wisehub-specific-hms
        if (isInstantiatable(SpecificChangerJSONConfig.class, type)) {
            SpecificChangerJSONConfig configSpecific = new SpecificChangerJSONConfig(fixerType);
            this.specificJsonPattern = configSpecific.getSpecificPatterns();
        }

        // wisehub-xml-hms
        if (isInstantiatable(XMLJSONConfig.class, type)) {
            XMLJSONConfig configXML = new XMLJSONConfig(fixerType);
            this.xmlJsonPattern = configXML.getXmlPatterns();
        }

        // process for xms SDK convertor
        if ("wisehub".equals(fixerType)) {
            // handle class
            Set<String> retainedClassPattern = new HashSet<>(this.classDeletePatterns);
            retainedClassPattern.retainAll(this.classRenamePatterns.keySet());
            if (GlobalSettings.isIsSDK()) {
                retainedClassPattern.forEach(x -> {
                    this.classRenamePatterns.remove(x);
                    this.classRenameDescriptions.remove(x);
                });

            } else {
                this.classDeletePatterns.removeAll(retainedClassPattern);
                retainedClassPattern.forEach(x -> this.classDeleteDescriptions.remove(x));

            }
            // handle field
            Set<String> retainedFeildPattern = new HashSet<>(this.fieldDeletePatterns);
            retainedFeildPattern.retainAll(this.fieldRenamePattern.keySet());
            if (GlobalSettings.isIsSDK()) {
                retainedFeildPattern.forEach(x -> {
                    this.fieldRenamePattern.remove(x);
                    this.fieldRenameDescriptions.remove(x);
                });
            } else {
                this.fieldDeletePatterns.removeAll(retainedFeildPattern);
                retainedFeildPattern.forEach(x -> this.fieldDeletePatterns.remove(x));
            }

            // handle method
            Set<String> retainedMethodPattern = new HashSet<>(this.methodRenamePattern.keySet());
            retainedMethodPattern.retainAll(this.methodDelete.keySet());
            HashMap<String, List<MethodChangePattern>> uselessPatterns = new HashMap<>();
            // record useless MethodChangePattern
            retainedMethodPattern.forEach(p -> this.methodDelete.get(p).forEach(d -> this.methodRenamePattern.get(p).forEach(r -> {
                if (d.getParamTypes().containsAll(r.getParamTypes()) && d.getParamTypes().size() == r.getParamTypes().size()) {
                    uselessPatterns.putIfAbsent(p, new ArrayList<>());
                    uselessPatterns.get(p).add(r);
                    uselessPatterns.get(p).add(d);
                }
            })));

            // removed useless MethodChangePattern
            uselessPatterns.keySet().forEach(p -> {
                if (GlobalSettings.isIsSDK()) {
                    this.methodRenamePattern.get(p).removeAll(uselessPatterns.get(p));
                } else {
                    this.methodDelete.get(p).removeAll(uselessPatterns.get(p));
                }
            });

            // remove empty pattern
            retainedMethodPattern.forEach(p -> {
                if (this.methodRenamePattern.get(p).size() == 0) {
                    this.methodRenamePattern.remove(p);
                }
                if (this.methodDelete.get(p).size() == 0) {
                    this.methodDelete.remove(p);
                }
            });

            // handle xml
            if (GlobalSettings.isIsSDK()) {
                xmlJsonPattern.getXmlChangerJsonTargets().remove("servicecom.google.firebase.messaging.FirebaseMessagingService");
                retainedClassPattern.forEach(p -> xmlJsonPattern.getLayoutOperationJsonTargets().remove(p));
            }
        }
    }

    private static boolean isInstantiatable(Class clazz, DefectFixerType type) {
        if (DefectFixerType.WISEHUB.equals(type)) {
            String canonicalName = clazz.getCanonicalName();
            if (X2YAutoJSONConfig.class.getCanonicalName().equals(canonicalName)) {
                return isConfigFileExist(ConfigFileName.WISEHUB_AUTO.getFileName());
            } else if (ComplexChangerJSONConfig.class.getCanonicalName().equals(canonicalName)) {
                return isConfigFileExist(ConfigFileName.WISEHUB_COMPLEX.getFileName());
            } else if (CrossfileChangerJSONConfig.class.getCanonicalName().equals(canonicalName)) {
                return isConfigFileExist(ConfigFileName.WISEHUB_CROSSFILE.getFileName());
            } else if (GradleJSONConfig.class.getCanonicalName().equals(canonicalName)) {
                return isConfigFileExist(ConfigFileName.WISEHUB_GRADLE.getFileName());
            } else if (GradleManualJSONConfig.class.getCanonicalName().equals(canonicalName)) {
                return isConfigFileExist(ConfigFileName.WISEHUB_GRADLE_MANUAL.getFileName());
            } else if (SpecificChangerJSONConfig.class.getCanonicalName().equals(canonicalName)) {
                return isConfigFileExist(ConfigFileName.WISEHUB_SPECIFIC.getFileName());
            } else if (X2YManualJSONConfig.class.getCanonicalName().equals(canonicalName)) {
                return isConfigFileExist(ConfigFileName.WISEHUB_MANUAL.getFileName());
            } else if (XMLJSONConfig.class.getCanonicalName().equals(canonicalName)) {
                return isConfigFileExist(ConfigFileName.WISEHUB_XML.getFileName());
            }
            return false;
        }
        if (DefectFixerType.LIBADAPTION.equals(type)) {
            String canonicalName = clazz.getCanonicalName();
            if (X2YAutoJSONConfig.class.getCanonicalName().equals(canonicalName)) {
                return isConfigFileExist(ConfigFileName.WISHHUB_AUTO_HMS.getFileName());
            } else if (ComplexChangerJSONConfig.class.getCanonicalName().equals(canonicalName)) {
                return isConfigFileExist(ConfigFileName.WISEHUB_COMPLEX_HMS.getFileName());
            } else if (CrossfileChangerJSONConfig.class.getCanonicalName().equals(canonicalName)) {
                return isConfigFileExist(ConfigFileName.WISEHUB_CROSSFILE_HMS.getFileName());
            } else if (GradleJSONConfig.class.getCanonicalName().equals(canonicalName)) {
                return isConfigFileExist(ConfigFileName.WISEHUB_GRADLE_HMS.getFileName());
            } else if (GradleManualJSONConfig.class.getCanonicalName().equals(canonicalName)) {
                return isConfigFileExist(ConfigFileName.WISEHUB_GRADLE_MANUAL_HMS.getFileName());
            } else if (SpecificChangerJSONConfig.class.getCanonicalName().equals(canonicalName)) {
                return isConfigFileExist(ConfigFileName.WISEHUB_SPECIFIC_HMS.getFileName());
            } else if (X2YManualJSONConfig.class.getCanonicalName().equals(canonicalName)) {
                return isConfigFileExist(ConfigFileName.WISEHUB_MANUAL_HMS.getFileName());
            } else if (XMLJSONConfig.class.getCanonicalName().equals(canonicalName)) {
                return isConfigFileExist(ConfigFileName.WISEHUB_XML_HMS.getFileName());
            }
            return false;
        }
        return false;
    }

    private static boolean isConfigFileExist(String configFileName) {
        return getConfigFilePathByName(configFileName) != null;
    }

    private static String getConfigFilePathByName(String configFileName) {
        // Check config path
        Path configFilePath = Paths.get(System.getProperty("user.codemigrate.dir"), configFileName);
        if (Files.exists(configFilePath)) {
            return configFilePath.toString();
        }
        // Check classpath
        String[] classpaths = System.getProperty("java.class.path").split(System.getProperty("path.separator"));
        for (String classpath : classpaths) {
            if (classpath.endsWith(".jar")) {
                Path jarPath = Paths.get(classpath);
                Path parentPath = jarPath.getParent();
                if (jarPath.getParent() != null) {
                    configFilePath = Paths.get(parentPath.toString(), "config", configFileName);
                    if (Files.exists(configFilePath)) {
                        return configFilePath.toString();
                    }
                }
            }
        }
        return null;
    }

}
