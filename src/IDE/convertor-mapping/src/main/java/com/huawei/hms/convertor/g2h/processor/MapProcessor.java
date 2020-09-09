/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.hms.convertor.g2h.processor;

import com.huawei.hms.convertor.constants.Constant;
import com.huawei.hms.convertor.g2h.map.auto.Auto;
import com.huawei.hms.convertor.g2h.map.auto.AutoClass;
import com.huawei.hms.convertor.g2h.map.auto.AutoMethod;
import com.huawei.hms.convertor.g2h.map.auto.AutoMethodWithParam;
import com.huawei.hms.convertor.g2h.map.desc.ClassDesc;
import com.huawei.hms.convertor.g2h.map.desc.Desc;
import com.huawei.hms.convertor.g2h.map.desc.ExDesc;
import com.huawei.hms.convertor.g2h.map.desc.MethodDesc;
import com.huawei.hms.convertor.g2h.map.extension.G2XExtension;
import com.huawei.hms.convertor.g2h.map.manual.BlockList;
import com.huawei.hms.convertor.g2h.map.manual.Manual;
import com.huawei.hms.convertor.g2h.map.manual.ManualMethod;
import com.huawei.hms.convertor.g2h.map.manual.ManualPackage;
import com.huawei.hms.convertor.g2h.map.manual.TrustList;
import com.huawei.hms.convertor.handler.AutoClassHandler;
import com.huawei.hms.convertor.handler.AutoFieldHandler;
import com.huawei.hms.convertor.handler.AutoMethodHandler;
import com.huawei.hms.convertor.handler.Handler;
import com.huawei.hms.convertor.handler.ManualClassHandler;
import com.huawei.hms.convertor.handler.ManualFieldHandler;
import com.huawei.hms.convertor.handler.ManualMethodHandler;
import com.huawei.hms.convertor.handler.SpecialMethodHandler;
import com.huawei.hms.convertor.json.JClass;
import com.huawei.hms.convertor.json.JFieldOrMethod;
import com.huawei.hms.convertor.json.JMapping;
import com.huawei.hms.convertor.json.JMethod;
import com.huawei.hms.convertor.json.Parser;
import com.huawei.hms.convertor.utils.FileUtil;
import com.huawei.hms.convertor.utils.TextUtil;
import com.huawei.hms.convertor.utils.XMSUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

@Slf4j
public class MapProcessor {
    private Map<String, String> kitVersionMap;

    private String outPath;

    private String logPath;

    private String zipName;

    private static final int PATH_LENGTH_MINIMUM = 3;

    private G2XExtension g2XExtension = new G2XExtension();

    public MapProcessor(Map<String, String> kitVersionMap, String outPath, String logPath, String zipName) {
        this.kitVersionMap = kitVersionMap;
        this.outPath = outPath;
        this.logPath = logPath;
        this.zipName = zipName;
    }

    public static GeneratorResult outPutJson(Object obj, String outPath, String fileName) {
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().excludeFieldsWithoutExposeAnnotation().create();
        String str = gson.toJson(obj);
        return FileUtil.createJsonFile(str, outPath, fileName);
    }

    /**
     * add prefix for primary type
     *
     * @param type primary type
     * @return result
     */
    public static String enhancePrimaryType(String type) {
        String result = type;
        if (XMSUtils.isPrimitiveType(type)) {
            result = "#BUILT_IN." + type;
        }
        return result;
    }

    /**
     * process all configs
     *
     * @return generatorResult describes the result message
     */
    public GeneratorResult processAllTargetConfig() {
        GeneratorResult ret;
        Auto auto = new Auto();
        Manual manual = new Manual();
        String autoFileName;
        String manualFileName;

        autoFileName = "wisehub-auto-hms";
        manualFileName = "wisehub-manual-hms";
        ret = resolveAllClasses(auto, manual);
        List<Object> resultAutoMethodList = deleteRepeatMethods(auto);
        List<ManualMethod> resultManualMethodList = deleteRepeatMethodsManual(manual);
        auto.setAutoMethods(resultAutoMethodList);
        manual.setManualMethods(resultManualMethodList);
        addAutoClazz(auto);
        if (ret != GeneratorResult.SUCCESS) {
            FileUtil.outPutLog(logPath, ret);
            return ret;
        }

        ManualPackage pkgGms = new ManualPackage("com.google.android.gms", new ExDesc(
            "com.google.android.gms.* is not supported.Modify your code appropriately to ensure that the GMS function is normal.",
            "", "Common", "Manual", false));
        manual.getManualPackages().add(pkgGms);

        ManualPackage pkgFirebase = new ManualPackage("com.google.firebase", new ExDesc(
            "com.google.firebase* is not supported.Modify your code appropriately to ensure that the GMS function is normal.",
            "", "Common", "Manual", false));
        manual.getManualPackages().add(pkgFirebase);

        manual.getManualClasses().addAll(g2XExtension.getManualClasses());
        manual.getManualMethods().addAll(g2XExtension.getManualMethods());
        manual.getManualPackages().addAll(g2XExtension.getManualPackages());
        auto.getAutoPackages().addAll(g2XExtension.getAutoPackages());

        ret = outPutJson(auto, outPath, autoFileName);
        if (ret != GeneratorResult.SUCCESS) {
            FileUtil.outPutLog(logPath, ret);
            return ret;
        }
        ret = outPutJson(manual, outPath, manualFileName);
        if (ret != GeneratorResult.SUCCESS) {
            FileUtil.outPutLog(logPath, ret);
            return ret;
        }

        return ret;
    }

    public GeneratorResult resolveAllClasses(Auto auto, Manual manual) {
        try (ZipFile zipFile = new ZipFile(zipName);
            ZipInputStream zip = new ZipInputStream(new FileInputStream(new File(zipName)))) {
            while (true) {
                ZipEntry nextEntry = null;
                try {
                    nextEntry = zip.getNextEntry();
                } catch (IOException e) {
                    log.error("get zip entry fail.");
                    return GeneratorResult.MISSING_PLUGIN;
                }
                if (nextEntry == null) {
                    break;
                }
                process(nextEntry, zipFile, auto, manual);
            }
        } catch (IOException e) {
            log.error("resolve all classes failed, exception: {}.", e.getMessage());
            return GeneratorResult.MISSING_PLUGIN;
        }
        return GeneratorResult.SUCCESS;
    }

    private void fillResult(Auto auto, Manual manual, JClass jClass, String version) {
        Handler autoClassHandler = new AutoClassHandler(auto, manual, jClass, version);
        Handler autoFieldHandler = new AutoFieldHandler(auto, manual, jClass, version);
        Handler autoMethodHandler = new AutoMethodHandler(auto, manual, jClass, version);

        Handler manualClassHandler = new ManualClassHandler(auto, manual, jClass, version);
        Handler manualFieldHandler = new ManualFieldHandler(auto, manual, jClass, version);
        Handler manualMethodHandler = new ManualMethodHandler(auto, manual, jClass, version);

        Handler specialMethodHandler = new SpecialMethodHandler(auto, manual, jClass, version);

        autoClassHandler.setNextHandler(manualClassHandler);
        autoFieldHandler.setNextHandler(manualFieldHandler);
        autoMethodHandler.setNextHandler(manualMethodHandler);
        manualMethodHandler.setNextHandler(specialMethodHandler);

        autoClassHandler.handlerRequest();

        for (JMapping<JFieldOrMethod> mapping : jClass.fields()) {
            autoFieldHandler.handlerRequest(mapping);
        }

        for (JMapping<JMethod> mapping : jClass.methods()) {
            autoMethodHandler.handlerRequest(mapping);
        }

    }

    /**
     * convert classes that are not in excel
     *
     * @param auto support automatic convert
     */
    private void addAutoClazz(Auto auto) {
        TrustList trustList = new TrustList();
        for (String autoClass : trustList.addClass) {
            String[] addClassInfo = autoClass.split("[|]");
            String gmsClassName = addClassInfo[0];
            String hmsClassName = addClassInfo[1];
            String kitName = addClassInfo[2];
            String dependency = addClassInfo[3];
            String gmsVersion = addClassInfo[4];
            String hmsVersion = addClassInfo[5];
            String type = addClassInfo[8];
            String text = "\"" + gmsClassName + "\" will be replaced by \"" + hmsClassName + "\"";
            String url = "";
            Desc desc = ClassDesc.builder()
                .text(text)
                .url(url)
                .kit(kitName)
                .dependencyName(dependency)
                .gmsVersion(gmsVersion)
                .hmsVersion(hmsVersion)
                .type(type)
                .status("Auto")
                .support(true)
                .build();
            desc.setName(gmsClassName);
            AutoClass clazz = new AutoClass(TextUtil.degenerifyContains(gmsClassName),
                TextUtil.degenerifyContains(hmsClassName), desc);
            auto.getAutoClasses().add(clazz);
        }
    }

    /**
     * create hash map, key is the method name of gms or hms, value is array index
     *
     * @param auto support automatic convert
     * @return method list
     */
    private List<Object> deleteRepeatMethods(Auto auto) {
        List<Object> autoMethodList = auto.getAutoMethods();
        Integer counter = 0;
        Map<String, List<Integer>> methodNameMap = new HashMap<>();
        for (Object key : autoMethodList) {
            String allMethodName = "";
            if (key instanceof AutoMethodWithParam) {
                AutoMethodWithParam newMethod = (AutoMethodWithParam) key;
                allMethodName = newMethod.getOldMethodName() + "|" + newMethod.getNewMethodName();
            } else {
                AutoMethod newMethod = (AutoMethod) key;
                allMethodName = newMethod.getOldMethodName() + "|" + newMethod.getNewMethodName();
            }
            counter = getInteger(counter, methodNameMap, allMethodName);
        }
        return deleteFromJson(auto, methodNameMap);
    }

    private List<Object> deleteFromJson(Auto auto, Map<String, List<Integer>> methodNameMap) {
        List<Object> remainMethodList = new ArrayList<>();
        for (Map.Entry<String, List<Integer>> entry : methodNameMap.entrySet()) {
            List<Integer> counterList = entry.getValue();
            if (counterList.size() > 1) {
                Auto autoMerge = new Auto();
                for (Integer integer : counterList) {
                    Object oneAutoMethod = auto.getOneAutoMethod(integer);
                    autoMerge.setOneAutoMethod(oneAutoMethod);
                }
                remainMethodList.addAll(addWeakParam(autoMerge));
            } else {
                Object oneAutoMethod = auto.getOneAutoMethod(counterList.get(0));
                remainMethodList.add(oneAutoMethod);
            }
        }

        return remainMethodList;
    }

    private List<Object> addWeakParam(Auto auto) {
        List<Object> autoMergeList = auto.getAutoMethods();
        List<Object> returnMergeList = new ArrayList<>();
        for (Object key : autoMergeList) {
            AutoMethod autoMethod = (AutoMethod) key;

            Desc methodDesc = autoMethod.getDesc();
            int paramSize = autoMethod.getParamTypes().size();
            String gmsMethodName = autoMethod.getOldMethodName();
            String hmsMethodName = autoMethod.getNewMethodName();
            List<String> paramTypes = autoMethod.getParamTypes();
            List<String> weakParamTypes = new LinkedList<>();
            StringBuilder paramNumName = new StringBuilder("(");
            String descMethodName = methodDesc.getName();
            String[] descLastMethod = descMethodName.split("[(]");
            if (paramSize > 0) {
                for (int i = 0; i < paramSize; i++) {
                    if (paramTypes.get(i).contains("...")) {
                        weakParamTypes.add("*...");
                    } else {
                        weakParamTypes.add("*");
                    }

                    if (i == paramSize - 1) {
                        paramNumName.append("param").append(i).append(")");
                    } else {
                        paramNumName.append("param").append(i).append(",");
                    }
                }
            } else {
                paramNumName = new StringBuilder("()");
            }
            String text = "\"" + gmsMethodName + paramNumName + "\"" + " will be replaced by " + "\"" + hmsMethodName
                + paramNumName + "\"";

            Desc desc = MethodDesc.builder()
                .text(text)
                .url(methodDesc.getUrl())
                .kit(methodDesc.getKit())
                .dependencyName(methodDesc.getDependencyName())
                .gmsVersion(methodDesc.getGmsVersion())
                .hmsVersion(methodDesc.getHmsVersion())
                .type(methodDesc.getType())
                .status(methodDesc.isAutoConvert())
                .support(methodDesc.isSupport())
                .build();
            desc.setName(descLastMethod[0] + paramNumName);
            if (key instanceof AutoMethodWithParam) {
                AutoMethodWithParam lastMethod = (AutoMethodWithParam) AutoMethodWithParam.builder()
                    .newParams(((AutoMethodWithParam) autoMethod).getNewParams())
                    .oldMethodName(gmsMethodName)
                    .newMethodName(hmsMethodName)
                    .desc(desc)
                    .paramTypes(paramTypes)
                    .weakTypes(weakParamTypes)
                    .build();
                returnMergeList.add(lastMethod);
            } else {
                AutoMethod lastMethod = AutoMethod.builder()
                    .oldMethodName(gmsMethodName)
                    .newMethodName(hmsMethodName)
                    .desc(desc)
                    .paramTypes(paramTypes)
                    .weakTypes(weakParamTypes)
                    .build();
                returnMergeList.add(lastMethod);
            }
        }
        return returnMergeList;
    }

    /**
     * delete repeat methods from json
     *
     * @param manual store all classes, fields and methods that not support automatic convert
     * @return method list
     */
    private List<ManualMethod> deleteRepeatMethodsManual(Manual manual) {
        ArrayList manualMethodList = manual.getManualMethods();
        Integer counter = 0;
        Map<String, List<Integer>> methodNameMap = new HashMap<>();
        for (Object key : manualMethodList) {
            ManualMethod newMethod = (ManualMethod) key;
            String allMethodName = newMethod.getMethodName();
            counter = getInteger(counter, methodNameMap, allMethodName);
        }

        List<ManualMethod> mergeMethodList = deleteFromJsonManual(manual, methodNameMap);

        return mergeMethodList;
    }

    private Integer getInteger(Integer counter, Map<String, List<Integer>> methodNameMap, String allMethodName) {
        List<Integer> counterList = new LinkedList<>();
        counterList.add(counter);
        if (methodNameMap.get(allMethodName) != null) {
            counterList.addAll(methodNameMap.get(allMethodName));
        }
        methodNameMap.put(allMethodName, counterList);
        counter++;
        return counter;
    }

    private List<ManualMethod> deleteFromJsonManual(Manual manual, Map<String, List<Integer>> methodNameMap) {
        List<ManualMethod> remainMethodList = new ArrayList<>();
        for (Map.Entry<String, List<Integer>> entry : methodNameMap.entrySet()) {
            List<Integer> counterList = entry.getValue();
            if (counterList.size() > 1) {
                Manual manualMerge = new Manual();
                for (Integer integer : counterList) {
                    ManualMethod oneManualMethod = manual.getOneManualMethod(integer);
                    manualMerge.setOneManualMethod(oneManualMethod);
                }
                remainMethodList.addAll(addWeakParamManual(manualMerge));
            } else {
                ManualMethod oneManualMethod = manual.getOneManualMethod(counterList.get(0));
                remainMethodList.add(oneManualMethod);
            }
        }
        return remainMethodList;
    }

    private List<ManualMethod> addWeakParamManual(Manual manual) {
        List<ManualMethod> manualMergeList = manual.getManualMethods();
        List<ManualMethod> returnMergeList = new ArrayList<>();
        BlockList blocklist = new BlockList();
        for (ManualMethod manualMethod : manualMergeList) {
            boolean isBlockListEnable = false;
            Desc methodDesc = manualMethod.getDesc();
            int paramSize = manualMethod.getParamTypes().size();
            List<String> weakParamTypes = new LinkedList<>();
            StringBuilder paramNumName = new StringBuilder("(");
            String gmsMethodName = manualMethod.getMethodName();
            String descMethodName = methodDesc.getName();
            String[] descLastMethod = descMethodName.split("[(]");
            for (String bfield : blocklist.manualExcludeOverWrite) {
                if (bfield.equals(gmsMethodName)) {
                    isBlockListEnable = true;
                }
            }
            if (paramSize > 0) {
                for (int i = 0; i < paramSize; i++) {

                    weakParamTypes.add("*");
                    if (i == paramSize - 1) {
                        paramNumName.append("param").append(i).append(")");
                    } else {
                        paramNumName.append("param").append(i).append(",");
                    }
                }
            } else {
                paramNumName = new StringBuilder("()");
            }
            String text = methodDesc.getText();
            String methodName = "";
            if (!isBlockListEnable) {
                methodName = descLastMethod[0] + paramNumName;
            } else {
                methodName = methodDesc.getName();
                weakParamTypes = manualMethod.getParamTypes();
            }

            Desc desc = MethodDesc.builder()
                .text(text)
                .url(methodDesc.getUrl())
                .kit(methodDesc.getKit())
                .dependencyName(methodDesc.getDependencyName())
                .gmsVersion(methodDesc.getGmsVersion())
                .hmsVersion(methodDesc.getHmsVersion())
                .type(methodDesc.getType())
                .status(methodDesc.isAutoConvert())
                .support(methodDesc.isSupport())
                .build();
            desc.setName(methodName);
            ManualMethod lastMethod =
                new ManualMethod(gmsMethodName, manualMethod.getParamTypes(), weakParamTypes, desc);
            returnMergeList.add(lastMethod);

        }
        return returnMergeList;
    }

    private void process(ZipEntry entry, ZipFile zipFile, Auto auto, Manual manual) throws IOException {
        if (!entry.getName().startsWith("json/") || !entry.getName().endsWith(".json")) {
            return;
        }

        InputStream resourceAsStream = zipFile.getInputStream(entry);
        InputStreamReader isr = new InputStreamReader(resourceAsStream, StandardCharsets.UTF_8);

        JClass jClass = Parser.parse(isr);
        String[] pathStrs = entry.getName().split("/");
        if (pathStrs.length <= PATH_LENGTH_MINIMUM ) {
            throw new IllegalStateException("invalid input json path");
        }
        String kitName = pathStrs[Constant.KIT_NAME_INDEX];
        String dependencyName = kitName;
        String version = kitVersionMap.get(kitName);
        if (kitVersionMap.containsKey(kitName) && !entry.getName().contains(version)) {
            return;
        }

        fillResult(auto, manual, jClass, version);
    }
}
