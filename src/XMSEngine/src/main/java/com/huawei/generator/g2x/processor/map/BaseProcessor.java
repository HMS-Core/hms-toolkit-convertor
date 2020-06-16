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
import com.huawei.generator.g2x.po.map.Desc;
import com.huawei.generator.g2x.po.map.MDesc;
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
import com.huawei.generator.utils.ConvertorUtils;
import com.huawei.generator.utils.G2HTables;
import com.huawei.generator.utils.G2XMappingUtils;
import com.huawei.generator.utils.Modifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
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
public abstract class BaseProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(BaseProcessor.class);

    boolean allJsonValid = true;

    List<String> autoGMethodList = new LinkedList<>();

    List<String> manualGMethodList = new LinkedList<>();

    protected String pluginPath;

    // "$" separator for inner class
    private boolean dollar;

    // unGenerify
    private boolean unGenerified;

    // absolute package name in method signature
    private boolean withClassName;

    BaseProcessor(ProcessorBuilder builder) {
        this.dollar = builder.dollar;
        this.unGenerified = builder.unGenerified;
        this.withClassName = builder.withClassName;
        this.pluginPath = builder.pluginPath;
    }

    /**
     * make the process from zip
     *
     * @param entry zip entry
     * @param zipFile zip file
     * @param auto auto Map obj
     * @param manual manual Map obj
     * @return null
     * @throws IOException sth wrong with pluginPath
     */
    abstract void process(ZipEntry entry, ZipFile zipFile, Auto auto, Manual manual) throws IOException;

    // read file "*.json" from jar package
    GeneratorResult resolveAllClasses(Auto auto, Manual manual) {
        try (ZipFile zipFile = new ZipFile(pluginPath);
            ZipInputStream zip = new ZipInputStream(new FileInputStream(new File(pluginPath)))) {
            while (true) {
                ZipEntry nextEntry;
                try {
                    nextEntry = zip.getNextEntry();
                } catch (IOException e) {
                    e.printStackTrace();
                    return GeneratorResult.MISSING_PLUGIN;
                }
                if (nextEntry == null) {
                    break;
                } else {
                    process(nextEntry, zipFile, auto, manual);
                }
            }
        } catch (IOException e) {
            return GeneratorResult.MISSING_PLUGIN;
        }
        return GeneratorResult.SUCCESS;
    }

    Map<String, String> buildParamMap(JClass jClass, String kit, String dependencyName) {
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("tempDependencyName", G2XMappingUtils.normalizeKitName(dependencyName));
        paramMap.put("tempKit", G2XMappingUtils.normalizeKitName(kit));
        paramMap.put("gmsVersion", "");
        paramMap.put("hmsVersion", "");
        String gName = G2XMappingUtils.degenerifyContains(jClass.gName());
        String xName = TypeNode.create(G2XMappingUtils.degenerifyContains(jClass.gName())).toX().toString();
        paramMap.put("originXName", xName);
        if (dollar && jClass.isInnerClass()) {
            xName = G2XMappingUtils.dot2Dollar(xName);
            gName = G2XMappingUtils.dot2Dollar(gName);
        }
        paramMap.put("gName", gName);
        paramMap.put("xName", xName);
        String originGName = jClass.gName();
        paramMap.put("originGName", originGName);
        return paramMap;
    }

    protected void fillResult(Auto auto, Manual manual, JClass jClass, String kit, String dependencyName,
        String version) {
        Map<String, String> paramMap = buildParamMap(jClass, kit, dependencyName);

        fillMethod(auto, manual, jClass, paramMap);
        fillField(auto, manual, jClass, paramMap);

        // classes in black-list or unsupported should be put in manual
        // resolve classes
        Desc desc = new Desc();
        desc.kit = paramMap.get("tempKit");
        desc.dependencyName = paramMap.get("tempDependencyName");
        desc.gmsVersion = version;
        desc.hmsVersion = "";

        boolean inBlackList = G2HTables.inBlackList(paramMap.get("originXName"));
        if (inBlackList) {
            String descStr =
                "HMS does not provide a corresponding class for \"" + jClass.gName() + "\", please delete it.";
            desc.text = descStr;
            desc.url = paramMap.get("tmpURL");
            desc.status = XmsConstants.MANUAL;
            desc.support = false;
            ManualClass manualClass = new ManualClass(paramMap.get("gName"), desc);
            manual.getManualClasses().add(manualClass);
        } else {
            String descStr =
                "\"" + paramMap.get("gName") + "\"" + " will be replaced by " + "\"" + paramMap.get("xName") + "\"";
            desc.text = descStr;
            desc.url = "";
            desc.status = XmsConstants.AUTO;
            desc.support = true;
            AutoClass autoClass = new AutoClass(paramMap.get("gName"), paramMap.get("xName"), desc);
            auto.getAutoClasses().add(autoClass);
        }
    }

    JClass getJClassFromEntry(ZipEntry entry, ZipFile zipFile) throws IOException {
        InputStream resourceAsStream = zipFile.getInputStream(entry);
        InputStreamReader isr = new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8);
        return Parser.parse(isr);
    }

    String[] getJsonPathFromEntry(ZipEntry entry, JClass jClass) {
        allJsonValid = JsonValidator.validate(jClass.gName() != null ? jClass.gName() : "", jClass) && allJsonValid;
        String[] pathStrs = entry.getName().split("/");
        if (pathStrs.length <= 3) {
            throw new IllegalStateException("invalid input json path");
        }
        return pathStrs;
    }

    void fillMethod(Auto auto, Manual manual, JClass jClass, Map<String, String> paramMap) {
        // for the following, check whether in overloadedMethodSet or not?
        // yes, replace weakTypes, desc'text and methodName
        for (JMapping<JMethod> mapping : jClass.methods()) {
            if (mapping.status().equals("redundant")) {
                continue;
            }
            String originGMName = paramMap.get("originGName") + "." + mapping.g().name();
            mapping.g().setNameWithClass(originGMName);
            MDesc mDesc = new MDesc();
            mDesc.kit = paramMap.get("tempKit");
            mDesc.dependencyName = paramMap.get("tempDependencyName");
            mDesc.gmsVersion = paramMap.get("gmsVersion");
            mDesc.hmsVersion = mapping.getHmsVersion();
            mDesc.methodName = dollar ? ConvertorUtils.getSignatureForConvertor(mapping.g(), jClass.gName())
                : mapping.g().getSignature(unGenerified, withClassName);

            // auto
            auto(jClass, mapping, paramMap, mDesc, auto);

            // manual--support
            manualSupport(jClass, mapping, paramMap, mDesc, manual);
        }
    }

    public void auto(JClass jClass, JMapping<JMethod> mapping, Map<String, String> paramMap, MDesc mDesc, Auto auto) {
        ValidResult result = validateMethod(TypeNode.create(jClass.gName()).toX().toString(), mapping);
        if (result != ValidResult.AUTO && result != ValidResult.AUTO_DUMMY) {
            return;
        }

        String gMName = paramMap.get("gName") + "." + mapping.g().name();
        String xMName = paramMap.get("xName") + "." + mapping.g().name();
        String desc = "\"" + gMName + "\"" + " will be replaced by " + "\"" + xMName + "\"";
        gMName = jClass.gName() + "." + mapping.g().name();
        mDesc.text = desc;
        mDesc.url = "";
        mDesc.status = XmsConstants.AUTO;
        mDesc.support = true;
        if (result == ValidResult.AUTO_DUMMY) {
            mDesc.text = mapping.getText();
            mDesc.url = mapping.getUrl();
            mDesc.status = XmsConstants.DUMMY;
        }
        AutoMethod method = new AutoMethod(G2XMappingUtils.degenerifyContains(gMName), xMName, mDesc,
            mapping.g().getParaList(unGenerified, dollar), mapping.g().getParaList(unGenerified, dollar));

        // overloaded method and not for binary
        Set<JMethod> overloadedMethodSet = labelOverloadedMethods(jClass.methods());
        if (overloadedMethodSet.contains(mapping.g()) && !dollar) {
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

    private void manualSupport(JClass jClass, JMapping<JMethod> mapping, Map<String, String> paramMap, MDesc mDesc,
        Manual manual) {
        ValidResult result = validateMethod(TypeNode.create(jClass.gName()).toX().toString(), mapping);
        if (result == ValidResult.MANUAL_SUPPORT || result == ValidResult.MANUAL_NOTSUPPORT) {
            mDesc.text = mapping.getText();
            mDesc.url = mapping.getUrl();
            mDesc.status = XmsConstants.MANUAL;
            mDesc.support = result != ValidResult.MANUAL_NOTSUPPORT;
            String gMName = jClass.gName() + "." + mapping.g().name();
            ManualMethod method = new ManualMethod(G2XMappingUtils.degenerifyContains(gMName),
                mapping.g().getParaList(true, dollar), mapping.g().getParaList(true, dollar), mDesc);

            // overloaded method and not for binary
            Set<JMethod> overloadedMethodSet = labelOverloadedMethods(jClass.methods());
            if (overloadedMethodSet.contains(mapping.g()) && !dollar) {
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

    void fillField(Auto auto, Manual manual, JClass jClass, Map<String, String> paramMap) {
        // resolve fields
        for (JMapping<JFieldOrMethod> mapping : jClass.fields()) {
            if (mapping.status().equals("redundant")) {
                continue;
            }
            if (mapping.g().isJField()) {
                Desc desc = new Desc();
                desc.kit = paramMap.get("tempKit");
                desc.dependencyName = paramMap.get("tempDependencyName");
                desc.gmsVersion = paramMap.get("gmsVersion");
                desc.hmsVersion = "";
                desc.text = mapping.getText();
                desc.url = mapping.getUrl();
                desc.status = XmsConstants.MANUAL;
                desc.support = true;

                String gFName = paramMap.get("gName") + "." + mapping.g().asJField().name();
                String xFName = paramMap.get("xName") + "." + mapping.g().asJField().name();
                String xGetName = paramMap.get("xName") + ".get" + mapping.g().asJField().name() + "()";
                ValidResult result = validateField(mapping);

                // judge status
                switch (result) {
                    case AUTO:
                        String descStr = "\"" + gFName + "\"" + " will be replaced by " + "\"" + xGetName + "\"";
                        if (((mapping.g().asJField().modifiers().contains(Modifier.FINAL.getName()))
                            && (!jClass.isInnerClass())) || jClass.isInterface()) {
                            descStr += ", which is not available for constant expression after switch-case label. "
                                + "One possible solution is using if-else statements. "
                                + "For further details, refer our user manual.";
                        }
                        desc.text = descStr;
                        desc.url = "";
                        desc.status = XmsConstants.AUTO;
                        auto.getAutoFields()
                            .add(new AutoField(G2XMappingUtils.degenerifyContains(gFName), xFName, desc));
                        break;
                    case AUTO_DUMMY:
                        desc.status = XmsConstants.DUMMY;
                        desc.support = false;
                        auto.getAutoFields()
                            .add(new AutoField(G2XMappingUtils.degenerifyContains(gFName), xFName, desc));
                        if (paramMap.get("tempURL") == null || paramMap.get("tempURL").isEmpty()) {
                            paramMap.put("tempURL", mapping.getUrl());
                        }
                        break;
                    case MANUAL_SUPPORT:
                        manual.getManualFields().add(new ManualField(G2XMappingUtils.degenerifyContains(gFName), desc));
                        if (paramMap.get("tempURL") == null || paramMap.get("tempURL").isEmpty()) {
                            paramMap.put("tempURL", mapping.getUrl());
                        }
                        break;
                    case MANUAL_NOTSUPPORT:
                        desc.support = false;
                        manual.getManualFields().add(new ManualField(G2XMappingUtils.degenerifyContains(gFName), desc));
                        if (paramMap.get("tempURL") == null || paramMap.get("tempURL").isEmpty()) {
                            paramMap.put("tempURL", mapping.getUrl());
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }

    private static ValidResult getValidResult(String status) {
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

    private ValidResult validateField(JMapping<JFieldOrMethod> mapping) {
        // ignored when g is null
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

        // not support if method is in blacklist
        if (G2HTables.inBlackList(className, mapping.g().name())) {
            return ValidResult.MANUAL_NOTSUPPORT;
        }
        return getValidResult(mapping.status());
    }

    /**
     * label overloaded methods with same length of params.
     * For these methods, we have to fulfill weakTypes with "*" and fulfill desc'text and methodName with param#
     *
     * @param methods target methods
     * @return labeled result
     */
    private Set<JMethod> labelOverloadedMethods(List<JMapping<JMethod>> methods) {
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
        // "$" separator for inner class
        private boolean dollar = false;

        // unGenerify
        private boolean unGenerified = true;

        // absolute package name in method signature
        private boolean withClassName = true;

        private String pluginPath;

        public ProcessorBuilder(String pluginPath) {
            this.pluginPath = pluginPath;
        }

        public void setDollar(boolean dollar) {
            this.dollar = dollar;
        }

        public String getPluginPath() {
            return pluginPath;
        }

        public void setPluginPath(String pluginPath) {
            this.pluginPath = pluginPath;
        }

        public ProcessorBuilder dollar(boolean dollar) {
            this.dollar = dollar;
            return this;
        }

        /**
         * return the processor
         *
         * @return processor
         */
        public abstract BaseProcessor build();
    }
}
