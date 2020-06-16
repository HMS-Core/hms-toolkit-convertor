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

import com.huawei.codebot.analyzer.x2y.gradle.coditionalchanger.StructGradleXml;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.StructGradleHeadquarter;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.StructGradleManual;
import com.huawei.codebot.analyzer.x2y.java.method.MethodChangePattern;
import com.huawei.codebot.analyzer.x2y.java.other.complexchanger.FixAction;
import com.huawei.codebot.analyzer.x2y.java.other.specificchanger.SpecificJsonPattern;
import com.huawei.codebot.analyzer.x2y.xml.XmlJsonPattern;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;

import java.util.ArrayList;
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
    private static ConfigService G2HInstance;
    private static ConfigService G2XInstance;

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
        if (fixerType.equals("libadaption")) {
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
     * Read all jsons and Store all data in specific data structures for each changer
     *
     * @param fixerType is "libadaption" use G2H ; fixerType is "wisehub" use G2X ;
     * @return specific data structures
     */
    public void readAllJsons(String fixerType) throws CodeBotRuntimeException {
        X2YAutoJSONConfig configAutoJson = new X2YAutoJSONConfig(fixerType);
        this.classRenamePatterns = configAutoJson.getClassRenamePatterns();
        this.classRenameDescriptions = configAutoJson.getClassRenameDescription();
        this.methodRenamePattern = configAutoJson.getMethodReplacePatterns();
        this.fieldRenamePattern = configAutoJson.getFieldRenamePatterns();
        this.fieldRenameDescriptions = configAutoJson.getFieldDescription();
        this.packageRenamePatterns = configAutoJson.getPackageRenamePatterns();
        this.packageRenameDescriptions = configAutoJson.getPackageRenameDescription();

        // wisehub-complex-hms
        ComplexChangerJSONConfig configComplex = new ComplexChangerJSONConfig(fixerType);
        this.complexSpecificPatterns = configComplex.getComplexSpecificPatterns();
        this.complexGradleHeadquarter = configComplex.getComplexGradleModificationPatterns();
        this.complexXmlPatterns = configComplex.getComplexXmlPatterns();

        // wisehub-crossfile-hms  or  wisehub-crossfile
        CrossfileChangerJSONConfig configCrossFile = new CrossfileChangerJSONConfig(fixerType);
        this.conditionalConfig = configCrossFile.getGradleXMLPatterns();

        // wisehub-gradle-hms  or  wisehub-gradle
        GradleJSONConfig configGradle = new GradleJSONConfig(fixerType);
        this.gradleHeadquarter = configGradle.getGradleModificationPatterns();

        // wisehub-gradle-manual-hms   or   wisehub-gradle-manual
        GradleManualJSONConfig configGradleManual = new GradleManualJSONConfig(fixerType);
        this.gradleManuals = configGradleManual.getGradleManual();

        // wisehub-manual-hms  or  wisehub-manual
        X2YManualJSONConfig configManual = new X2YManualJSONConfig(fixerType);
        this.classDeletePatterns = configManual.getClassDeletePatterns();
        this.classDeleteDescriptions = configManual.getClassDeleteDescription();
        this.methodDelete = configManual.getDeleteMethodPatterns();
        this.fieldDeletePatterns = configManual.getFieldDeletePatterns();
        this.fieldDeleteDescriptions = configManual.getFieldDeleteDescriptions();
        this.packageDeletePatterns = configManual.getPackageDeletePatterns();
        this.packageDeleteDescriptions = configManual.getPackageDeleteDescriptions();

        // wisehub-specific-hms
        SpecificChangerJSONConfig configSpecific = new SpecificChangerJSONConfig(fixerType);
        this.specificJsonPattern = configSpecific.getSpecificPatterns();

        // wisehub-xml-hms
        XMLJSONConfig configXML = new XMLJSONConfig(fixerType);
        this.xmlJsonPattern = configXML.getXmlPatterns();
    }

}
