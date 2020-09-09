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

package com.huawei.generator.g2x.processor.map;

import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.g2x.po.map.ClassDesc;
import com.huawei.generator.g2x.po.map.Desc;
import com.huawei.generator.g2x.po.map.FieldDesc;
import com.huawei.generator.g2x.po.map.MethodDesc;
import com.huawei.generator.g2x.po.map.auto.Auto;
import com.huawei.generator.g2x.po.map.auto.AutoClass;
import com.huawei.generator.g2x.po.map.auto.AutoField;
import com.huawei.generator.g2x.po.map.auto.AutoMethod;
import com.huawei.generator.g2x.po.map.manual.Manual;
import com.huawei.generator.g2x.po.map.manual.ManualClass;
import com.huawei.generator.g2x.po.map.manual.ManualField;
import com.huawei.generator.g2x.po.map.manual.ManualMethod;
import com.huawei.generator.g2x.processor.GeneratorResult;
import com.huawei.generator.g2x.processor.XmsConstants;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JFieldOrMethod;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.json.JMethod;
import com.huawei.generator.json.JsonValidator;
import com.huawei.generator.json.Parser;
import com.huawei.generator.utils.G2HTables;
import com.huawei.generator.utils.G2XMappingUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * Base class for processor
 *
 * @since 2020-03-19
 */
public abstract class Processor {
    private static final Logger LOGGER = LoggerFactory.getLogger(Processor.class);

    protected String pluginPath;

    // unGenerify
    protected boolean unGenerified;

    // absolute package name in method signature
    protected boolean withClassName;

    boolean allJsonValid = true;

    List<String> autoGMethodList = new LinkedList<>();

    List<String> manualGMethodList = new LinkedList<>();

    public Processor(ProcessorBuilder builder) {
        this.unGenerified = builder.unGenerified;
        this.withClassName = builder.withClassName;
        this.pluginPath = builder.pluginPath;
    }

    /**
     * make the process from zip
     *
     * @param entry zip entry
     * @param zipFile zipfile
     * @param auto auto Map obj
     * @param manual manual Map obj
     * @throws IOException sth wrong with pluginPath
     */
    abstract void process(ZipEntry entry, ZipFile zipFile, Auto auto, Manual manual) throws IOException;

    protected JClass getJClassFromEntry(ZipEntry entry, ZipFile zipFile) throws IOException {
        InputStream resourceAsStream = zipFile.getInputStream(entry);
        InputStreamReader isr = new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8);
        return Parser.parse(isr);
    }

    protected String[] getJsonPathFromEntry(ZipEntry entry, JClass jClass) {
        allJsonValid = JsonValidator.validate(jClass.gName() != null ? jClass.gName() : "", jClass) && allJsonValid;
        String[] pathStrs = entry.getName().split("/");
        if (pathStrs.length <= 3) {
            throw new IllegalStateException("invalid input json path");
        }
        return pathStrs;
    }

    // get jsons from jar
    protected GeneratorResult resolveAllClasses(Auto auto, Manual manual) {
        try (ZipFile zipFile = new ZipFile(pluginPath);
            ZipInputStream zip = new ZipInputStream(new FileInputStream(new File(pluginPath)))) {
            ZipEntry nextEntry = zip.getNextEntry();
            while (nextEntry != null) {
                process(nextEntry, zipFile, auto, manual);
                nextEntry = zip.getNextEntry();
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("Plugin path does not exist!");
            return GeneratorResult.MISSING_PLUGIN;
        } catch (IOException e) {
            LOGGER.error("Read or close resource failed!");
            return GeneratorResult.INNER_CRASH;
        }
        return GeneratorResult.SUCCESS;
    }

    protected Map<String, String> buildParamMap(JClass jClass, String kit, String dependencyName) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("tempDependencyName", G2XMappingUtils.normalizeKitName(dependencyName));
        paramMap.put("tempKit", G2XMappingUtils.normalizeKitName(kit));
        paramMap.put("gmsVersion", "");
        paramMap.put("hmsVersion", "");
        String gName = G2XMappingUtils.degenerifyContains(jClass.gName());
        String originGName = jClass.gName();
        String xName = TypeNode.create(G2XMappingUtils.degenerifyContains(jClass.gName())).toX().toString();
        paramMap.put("originXName", xName);
        paramMap.put("gName", gName);
        paramMap.put("xName", xName);
        paramMap.put("originGName", originGName);
        return paramMap;
    }

    protected void fillResult(Auto auto, Manual manual, JClass jClass, String kit, String dependencyName) {
        Map<String, String> paramMap = buildParamMap(jClass, kit, dependencyName);
        boolean inBlockList = G2HTables.inBlockList(paramMap.get("originXName"));

        // easy classloader resolve
        fillClFix(auto, jClass, paramMap);
        fillMethod(auto, manual, jClass, paramMap);
        fillField(auto, manual, jClass, paramMap);

        // resolve classes
        if (inBlockList) {
            Desc desc = new ClassDesc();
            desc.text = "HMS does not provide a corresponding class for \"" + jClass.gName() + "\", please delete it.";
            desc.url = paramMap.get("tmpURL");
            desc.kit = paramMap.get("tempKit");
            desc.dependencyName = paramMap.get("tempDependencyName");
            desc.gmsVersion = "";
            desc.hmsVersion = "";
            desc.status = XmsConstants.MANUAL;
            desc.support = false;
            desc.setName(jClass.gName());
            manual.getManualClasses().add(new ManualClass(paramMap.get("gName"), desc));
        } else {
            String descStr =
                "\"" + paramMap.get("gName") + "\"" + " will be replaced by " + "\"" + paramMap.get("xName") + "\"";
            Desc desc = new ClassDesc();
            desc.text = descStr;
            desc.url = "";
            desc.kit = paramMap.get("tempKit");
            desc.dependencyName = paramMap.get("tempDependencyName");
            desc.gmsVersion = "";
            desc.hmsVersion = "";
            desc.status = XmsConstants.AUTO;
            desc.support = true;
            desc.setName(jClass.gName());
            auto.getAutoClasses().add(new AutoClass(paramMap.get("gName"), paramMap.get("xName"), desc));
        }
    }

    private boolean fillClFix(Auto auto, JClass jClass, Map<String, String> paramMap) {
        if (jClass.gName().equals("com.google.android.gms.maps.MapFragment")
            || jClass.gName().equals("com.google.android.gms.maps.StreetViewPanoramaFragment")
            || jClass.gName().equals("com.google.android.gms.maps.SupportMapFragment")
            || jClass.gName().equals("com.google.android.gms.maps.SupportStreetViewPanoramaFragment")
            || jClass.gName().equals("com.google.firebase.messaging.FirebaseMessagingService")) {
            Set<JMethod> overloadedMethodSet = labelOverloadedMethods(jClass.methods());

            // for the following, check whether in overloadedMethodSet or not?
            // yes, replace weakTypes, desc'text and methodName
            for (JMapping<JMethod> mapping : jClass.methods()) {
                if (!mapping.status().equals("redundant")) {
                    String originGMName = paramMap.get("originGName") + "." + mapping.g().name();
                    mapping.g().setNameWithClass(originGMName);
                    fillClMethod(jClass, mapping, paramMap, overloadedMethodSet, auto);
                }
            }
            for (JMapping<JFieldOrMethod> mapping : jClass.fields()) {
                fillClField(paramMap, auto, mapping, jClass);
            }

            overloadedMethodSet.clear();
            String descStr =
                "\"" + paramMap.get("gName") + "\"" + " will be replaced by " + "\"" + paramMap.get("xName") + "\"";
            Desc desc = new FieldDesc();
            desc.text = descStr;
            desc.url = "";
            desc.kit = paramMap.get("tempKit");
            desc.dependencyName = paramMap.get("tempDependencyName");
            desc.gmsVersion = paramMap.get("gmsVersion");
            desc.hmsVersion = "";
            desc.status = XmsConstants.DUMMY;
            desc.support = true;
            AutoClass autoClass = new AutoClass(paramMap.get("gName"), paramMap.get("xName"), desc);
            auto.getAutoClasses().add(autoClass);
            return true;
        }
        return false;
    }

    private void fillClMethod(JClass jClass, JMapping<JMethod> mapping, Map<String, String> paramMap,
        Set<JMethod> overloadedMethodSet, Auto auto) {
        // auto
        String gMName = paramMap.get("gName") + "." + mapping.g().name();
        String xMName = paramMap.get("xName") + "." + mapping.g().name();
        String desc = "\"" + gMName + "\"" + " will be replaced by " + "\"" + xMName + "\"";
        gMName = jClass.gName() + "." + mapping.g().name();
        MethodDesc methodDesc = initMethodDesc(paramMap, mapping);
        methodDesc.text = desc;
        methodDesc.url = "";
        methodDesc.status = XmsConstants.DUMMY;
        methodDesc.support = true;
        AutoMethod method = new AutoMethod(G2XMappingUtils.degenerifyContains(gMName), xMName, methodDesc,
            mapping.g().getParaList(unGenerified, false), mapping.g().getParaList(unGenerified, false));

        // overloaded method and not for binary
        if (overloadedMethodSet.contains(mapping.g())) {
            method.setWeakTypes(G2XMappingUtils.eraseWeakTypes(method.getWeakTypes()));
            G2XMappingUtils.eraseMDesc(method.getDesc(), method.getParamTypes().size(), gMName, xMName);
        }
        auto.getAutoMethods().add(method);
        autoGMethodList.add(jClass.gName() + "." + mapping.g().name());
    }

    private void fillClField(Map<String, String> paramMap, Auto auto, JMapping<JFieldOrMethod> mapping, JClass jClass) {
        if (mapping.g().isJField()) {
            Desc desc = new FieldDesc();
            desc.kit = paramMap.get("tempKit");
            desc.dependencyName = paramMap.get("tempDependencyName");
            desc.gmsVersion = paramMap.get("gmsVersion");
            desc.hmsVersion = "";

            // judge status
            String gFName = paramMap.get("gName") + "." + mapping.g().asJField().name();
            String xGetName = paramMap.get("xName") + ".get" + mapping.g().asJField().name() + "()";
            String descStr = "\"" + gFName + "\"" + " will be replaced by " + "\"" + xGetName + "\"";
            if (((mapping.g().asJField().modifiers().contains("final")) && (!jClass.isInnerClass()))
                || jClass.isInterface()) {
                descStr += ", which is not available for constant expression after switch-case label. "
                    + "One possible solution is using if-else statements. "
                    + "For further details, refer our user manual.";
            }
            desc.text = descStr;
            desc.url = "";
            desc.status = XmsConstants.DUMMY;
            desc.support = true;
            String xFName = paramMap.get("xName") + "." + mapping.g().asJField().name();
            auto.getAutoFields().add(new AutoField(G2XMappingUtils.degenerifyContains(gFName), xFName, desc));
        }
    }

    public void fillMethod(Auto auto, Manual manual, JClass jClass, Map<String, String> paramMap) {
        // resolve methods
        Set<JMethod> overloadedMethodSet = labelOverloadedMethods(jClass.methods());

        // for the following, check whether in overloadedMethodSet or not?
        // yes, replace weakTypes, desc'text and methodName
        for (JMapping<JMethod> mapping : jClass.methods()) {
            if (!mapping.status().equals("redundant")) {
                String originGMName = paramMap.get("originGName") + "." + mapping.g().name();
                mapping.g().setNameWithClass(originGMName);

                // auto
                auto(jClass, mapping, paramMap, overloadedMethodSet, auto);

                // manual--support
                manualSupport(jClass, mapping, paramMap, overloadedMethodSet, manual);
            }
        }
        overloadedMethodSet.clear();
    }

    private MethodDesc initMethodDesc(Map<String, String> paramMap, JMapping<JMethod> mapping) {
        MethodDesc methodDesc = new MethodDesc();
        methodDesc.kit = paramMap.get("tempKit");
        methodDesc.dependencyName = paramMap.get("tempDependencyName");
        methodDesc.gmsVersion = paramMap.get("gmsVersion");
        methodDesc.hmsVersion = mapping.getHmsVersion();
        methodDesc.methodName = mapping.g().getSignature(unGenerified, withClassName);
        return methodDesc;
    }

    public void auto(JClass jClass, JMapping<JMethod> mapping, Map<String, String> paramMap,
        Set<JMethod> overloadedMethodSet, Auto auto) {
        String gMName = paramMap.get("gName") + "." + mapping.g().name();
        String xMName = paramMap.get("xName") + "." + mapping.g().name();
        ValidResult result = validateMethod(TypeNode.create(jClass.gName()).toX().toString(), mapping);
        MethodDesc methodDesc = initMethodDesc(paramMap, mapping);
        if (result == ValidResult.AUTO || result == ValidResult.AUTO_DUMMY) {
            String desc = "\"" + gMName + "\"" + " will be replaced by " + "\"" + xMName + "\"";
            gMName = jClass.gName() + "." + mapping.g().name();
            methodDesc.text = desc;
            methodDesc.url = "";
            methodDesc.status = XmsConstants.AUTO;
            methodDesc.support = true;
            if (result == ValidResult.AUTO_DUMMY) {
                methodDesc.text = mapping.getText();
                methodDesc.url = mapping.getUrl();
                methodDesc.status = XmsConstants.DUMMY;
            }
            AutoMethod method = new AutoMethod(G2XMappingUtils.degenerifyContains(gMName), xMName, methodDesc,
                mapping.g().getParaList(unGenerified, false), mapping.g().getParaList(unGenerified, false));

            // overloaded method and not for binary
            if (overloadedMethodSet.contains(mapping.g())) {
                method.setWeakTypes(G2XMappingUtils.eraseWeakTypes(method.getWeakTypes()));
                if (result == ValidResult.AUTO) {
                    G2XMappingUtils.eraseMDesc(method.getDesc(), method.getParamTypes().size(), gMName, xMName);
                } else {
                    G2XMappingUtils.eraseMDesc(method.getDesc(), method.getParamTypes().size());
                }
            }
            auto.getAutoMethods().add(method);
            autoGMethodList.add(jClass.gName() + "." + mapping.g().name());
        }
    }

    public void manualSupport(JClass jClass, JMapping<JMethod> mapping, Map<String, String> paramMap,
        Set<JMethod> overloadedMethodSet, Manual manual) {
        ValidResult result = validateMethod(TypeNode.create(jClass.gName()).toX().toString(), mapping);
        MethodDesc methodDesc = initMethodDesc(paramMap, mapping);
        if (result == ValidResult.MANUAL_SUPPORT || result == ValidResult.MANUAL_NOTSUPPORT) {
            String gMName = jClass.gName() + "." + mapping.g().name();
            methodDesc.text = mapping.getText();
            methodDesc.url = mapping.getUrl();
            methodDesc.status = XmsConstants.MANUAL;
            methodDesc.support = result != ValidResult.MANUAL_NOTSUPPORT;

            // overloaded method and not for binary
            ManualMethod method = new ManualMethod(G2XMappingUtils.degenerifyContains(gMName),
                mapping.g().getParaList(true, false), mapping.g().getParaList(true, false), methodDesc);
            if (overloadedMethodSet.contains(mapping.g())) {
                method.setWeakTypes(G2XMappingUtils.eraseWeakTypes(method.getWeakTypes()));
                G2XMappingUtils.eraseMDesc(method.getDesc(), method.getParamTypes().size());
            }
            manual.getManualMethods().add(method);
            if (paramMap.get("tempURL") == null || paramMap.get("tempURL").isEmpty()) {
                paramMap.put("tempURL", mapping.getUrl());
            }
            manualGMethodList.add(jClass.gName() + "." + mapping.g().name());
        }
    }

    public void fillField(Auto auto, Manual manual, JClass jClass, Map<String, String> paramMap) {
        // resolve fields
        for (JMapping<JFieldOrMethod> mapping : jClass.fields()) {
            if (mapping.g().isJField()) {
                Desc desc = new FieldDesc();
                desc.kit = paramMap.get("tempKit");
                desc.dependencyName = paramMap.get("tempDependencyName");
                desc.gmsVersion = paramMap.get("gmsVersion");
                desc.hmsVersion = "";

                String gFName = paramMap.get("gName") + "." + mapping.g().asJField().name();
                String xFName = paramMap.get("xName") + "." + mapping.g().asJField().name();
                String xGetName = paramMap.get("xName") + ".get" + mapping.g().asJField().name() + "()";
                desc.setName(gFName);
                ValidResult result = validateField(mapping);

                // judge status
                switch (result) {
                    case AUTO:
                        String descStr = "\"" + gFName + "\"" + " will be replaced by " + "\"" + xGetName + "\"";
                        if (((mapping.g().asJField().modifiers().contains("final")) && (!jClass.isInnerClass()))
                            || jClass.isInterface()) {
                            descStr += ", which is not available for constant expression after switch-case label. "
                                + "One possible solution is using if-else statements. For further details, "
                                + "refer our user manual.";
                        }
                        desc.text = descStr;
                        desc.url = "";
                        desc.status = XmsConstants.AUTO;
                        desc.support = true;
                        auto.getAutoFields()
                            .add(new AutoField(G2XMappingUtils.degenerifyContains(gFName), xFName, desc));
                        break;
                    case AUTO_DUMMY:
                        desc.text = mapping.getText();
                        desc.url = mapping.getUrl();
                        desc.status = XmsConstants.DUMMY;
                        desc.support = false;
                        auto.getAutoFields()
                            .add(new AutoField(G2XMappingUtils.degenerifyContains(gFName), xFName, desc));
                        if (paramMap.get("tempURL") == null || paramMap.get("tempURL").isEmpty()) {
                            paramMap.put("tempURL", mapping.getUrl());
                        }
                        break;
                    case MANUAL_SUPPORT:
                        desc.text = mapping.getText();
                        desc.url = mapping.getUrl();
                        desc.status = XmsConstants.MANUAL;
                        desc.support = true;
                        manual.getManualFields().add(new ManualField(G2XMappingUtils.degenerifyContains(gFName), desc));
                        if (paramMap.get("tempURL") == null || paramMap.get("tempURL").isEmpty()) {
                            paramMap.put("tempURL", mapping.getUrl());
                        }
                        break;
                    case MANUAL_NOTSUPPORT:
                        desc.text = mapping.getText();
                        desc.url = mapping.getUrl();
                        desc.status = XmsConstants.MANUAL;
                        desc.support = false;
                        manual.getManualFields().add(new ManualField(G2XMappingUtils.degenerifyContains(gFName), desc));
                        if (paramMap.get("tempURL") == null || paramMap.get("tempURL").isEmpty()) {
                            paramMap.put("tempURL", mapping.getUrl());
                        }
                        break;
                    case IGNORE:
                        break;
                }
            }
        }
    }

    private ValidResult validateField(JMapping<JFieldOrMethod> mapping) {
        // ignore when g is null
        if (mapping.g() == null) {
            return ValidResult.IGNORE;
        }
        return getValidResult(mapping.status());
    }

    private ValidResult validateMethod(String className, JMapping<JMethod> mapping) {
        // ignored when g is null
        if (mapping.g() == null) {
            return ValidResult.IGNORE;
        }

        // not support if method is in blocklist
        if (G2HTables.inBlockList(className, mapping.g().name())) {
            return ValidResult.MANUAL_NOTSUPPORT;
        }
        return getValidResult(mapping.status());
    }

    private ValidResult getValidResult(String status) {
        switch (status) {
            case JMapping.STATUS_MATCHING:
            case JMapping.STATUS_MANUALLY_ADAPT:
                return ValidResult.AUTO;
            case JMapping.STATUS_UNSUPPORTED:
                return ValidResult.MANUAL_NOTSUPPORT;
            case JMapping.STATUS_DUMMY:
                return ValidResult.AUTO_DUMMY;
            case JMapping.STATUS_REDUNDANT:
                return ValidResult.IGNORE;
            case JMapping.STATUS_DEVELOPER_MANUAL:
                return ValidResult.MANUAL_SUPPORT;
            default:
                LOGGER.debug("unexpected corresponding status {} detected !", status);
                return ValidResult.IGNORE;
        }
    }

    /**
     * label overloaded methods with same length of params.
     * For these methods, we have to fulfill weakTypes with "*" and fulfill desc'text and methodName with param#
     *
     * @param methods target methods
     * @return labeled result
     */
    protected Set<JMethod> labelOverloadedMethods(List<JMapping<JMethod>> methods) {
        Map<String, List<JMethod>> maps = new HashMap<>();
        Set<JMethod> polyMethods = new HashSet<>();
        for (JMapping<JMethod> item : methods) {
            JMethod method = item.g();
            if (method == null) {
                continue;
            }
            String key = method.name();
            if (!maps.containsKey(key)) {
                maps.put(key, new ArrayList<>());
            }
            maps.get(key).add(method);
        }

        for (Map.Entry<String, List<JMethod>> entry : maps.entrySet()) {
            if (entry.getValue().size() > 1) {
                polyMethods.addAll(entry.getValue());
            }
        }
        return polyMethods;
    }

    public abstract static class ProcessorBuilder {
        // unGenerify
        private boolean unGenerified = true;

        // absolute package name in method signature
        private boolean withClassName = true;

        private String pluginPath;

        public ProcessorBuilder(String pluginPath) {
            this.pluginPath = pluginPath;
        }

        public void setUnGenerified(boolean unGenerified) {
            this.unGenerified = unGenerified;
        }

        public void setWithClassName(boolean withClassName) {
            this.withClassName = withClassName;
        }

        public String getPluginPath() {
            return pluginPath;
        }

        public void setPluginPath(String pluginPath) {
            this.pluginPath = pluginPath;
        }

        /**
         * return the processor
         *
         * @return processor
         */
        public abstract Processor build();
    }
}
