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

package com.huawei.generator.g2x.processor.module;

import static com.huawei.generator.g2x.processor.XmsConstants.XMS_MODULE_NAME;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import com.huawei.generator.g2x.processor.GeneratorResult;
import com.huawei.generator.g2x.processor.GeneratorStrategyKind;
import com.huawei.generator.g2x.processor.ProcessorUtils;
import com.huawei.generator.g2x.processor.XmsConstants;
import com.huawei.generator.utils.FileUtils;
import com.huawei.generator.utils.KitInfoRes;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Class for XmsAdaptorGenerator
 *
 * @since 2020-04-07
 */
public class XmsAdaptorGenerator extends ModuleGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(XmsAdaptorGenerator.class);

    private boolean onlyG;

    private GeneratorStrategyKind kind;

    private boolean sdk;

    public XmsAdaptorGenerator(ProcessorUtils processorUtils) {
        super(processorUtils.getPathMap(), processorUtils.getKitMap(), processorUtils.getSummaries(),
            processorUtils.getSummary());
        resolveDepMap(processorUtils.getAllDepMap());
        modulePath = String.join(File.separator, targetPath, XmsConstants.XMS_MODULE_NAME);
        summaryPath = String.join(File.separator, targetPath, XmsConstants.XMS_MODULE_NAME, "config");
        manifestPath = String.join(File.separator, modulePath, "src", "main");
        onlyG = processorUtils.getStrategyKindList().contains(GeneratorStrategyKind.G);
        kind = processorUtils.getStrategyKindList().contains(GeneratorStrategyKind.GOrH)
            ? GeneratorStrategyKind.GOrH
            : GeneratorStrategyKind.HOrG;
        sdk = processorUtils.isThirdSDK();
        newSummary.getStrategy().addAll(processorUtils.getStrategyKindList());
    }

    @Override
    void resolveDepMap(Map<String, Set<String>> allDepMap) {
        kitSet.forEach(kit -> {
            if (allDepMap.get(kit) != null) {
                depList.put(kit, allDepMap.get(kit));
            }
        });
    }

    @Override
    public boolean generateCode() {
        newSummary.setKitNames(kitSet);
        String ghJavaPath = String.join(File.separator, modulePath, "src", "main", "java");
        if (onlyG) {
            ghJavaPath = String.join(File.separator, modulePath, "src", "xmsgh", "java");
        }
        GeneratorResult result = primaryGenerate(pluginPath, summaryPath, ghJavaPath, new ArrayList<>(kitSet), kind);
        generateSummary.xmsCodePaths.add(ghJavaPath);

        if (result != GeneratorResult.SUCCESS) {
            generateSummary.setResult(result);
            return false;
        }

        if (onlyG) {
            String gJavaPath = String.join(File.separator, modulePath, "src", "xmsg", "java");
            result =
                primaryGenerate(pluginPath, summaryPath, gJavaPath, new ArrayList<>(kitSet), GeneratorStrategyKind.G);
            generateSummary.xmsCodePaths.add(ghJavaPath);
            if (result != GeneratorResult.SUCCESS) {
                generateSummary.setResult(result);
                return false;
            }
        }
        return true;
    }

    @Override
    public void createModule() {
        generateCode();
        generateGradle();
        generateReadme();
        createManifestFile();
        copyManifest();
    }

    private void generateReadme() {
        StringBuilder stringBuilder = new StringBuilder();
        createOverview(stringBuilder, sdk);
        createContent(stringBuilder, sdk);
        createDescription(stringBuilder);
        boolean dependencyIsNull;
        if (depList == null || depList.size() == 0) {
            dependencyIsNull = true;
            stringBuilder.append("2. ");
        } else {
            dependencyIsNull = false;
            createKitInfo(stringBuilder, depList, generateSummary.xmsCodePaths);
            stringBuilder.append("3. ");
        }
        createManifest(stringBuilder);
        createInstruction(stringBuilder);
        if (sdk) {
            createReleaseGuide(stringBuilder, dependencyIsNull);
        }

        FileUtils.createFile(stringBuilder.toString(), modulePath, "README.md");
    }

    @Override
    boolean generateGradle() {
        StringBuilder stringBuilder = new StringBuilder();
        fillApplyPart(stringBuilder);
        fillAndroidPart(stringBuilder);
        fillDependencyPart(stringBuilder);
        FileUtils.createFile(stringBuilder.toString(), modulePath, "build.gradle");
        return true;
    }

    @Override
    void fillApplyPart(StringBuilder stringBuilder) {
        // Can be extended in the future
        stringBuilder.append("apply plugin: 'com.android.library'").append(System.lineSeparator());
        stringBuilder.append("apply plugin: 'com.huawei.agconnect'").append(System.lineSeparator());
        stringBuilder.append(System.lineSeparator());
    }

    @Override
    void fillAndroidPart(StringBuilder stringBuilder) {
        Map<String, Integer> versionInfo = KitInfoRes.INSTANCE.getDefaultSdkVersion(new ArrayList<>(kitSet));
        String compileSdk = "";
        if (versionInfo.containsKey("compileSdkVersion")) {
            compileSdk = versionInfo.get("compileSdkVersion").toString();
        }
        String minSdk = "";
        String targetSdk = "";
        if (versionInfo.containsKey("minSdkVersion")) {
            minSdk = versionInfo.get("minSdkVersion").toString();
        }
        if (versionInfo.containsKey("targetSdkVersion")) {
            targetSdk = versionInfo.get("targetSdkVersion").toString();
        }
        stringBuilder.append("android {").append(System.lineSeparator());
        addSpace(stringBuilder, XmsConstants.LINE_SPACE);
        stringBuilder.append("compileSdkVersion ").append(compileSdk).append(System.lineSeparator());
        addSpace(stringBuilder, XmsConstants.LINE_SPACE);
        stringBuilder.append("defaultConfig {").append(System.lineSeparator());
        addSpace(stringBuilder, XmsConstants.LINE_SPACE * 2);
        stringBuilder.append("minSdkVersion ").append(minSdk).append(System.lineSeparator());
        addSpace(stringBuilder, XmsConstants.LINE_SPACE * 2);
        stringBuilder.append("targetSdkVersion ").append(targetSdk).append(System.lineSeparator());
        addSpace(stringBuilder, XmsConstants.LINE_SPACE);
        stringBuilder.append("}").append(System.lineSeparator());
        stringBuilder.append(System.lineSeparator());

        stringBuilder.append("    compileOptions {\n" + "        sourceCompatibility = 1.8\n"
            + "        targetCompatibility = 1.8\n" + "    }\n" + "\n");
        if (onlyG) {
            fillFlavorsPart(stringBuilder);
        }
        stringBuilder.append("}").append(System.lineSeparator());
        stringBuilder.append(System.lineSeparator());
    }

    @Override
    void fillDependencyPart(StringBuilder stringBuilder) {
        // add dependencyPart according to Kit-Information map
        stringBuilder.append("dependencies {").append(System.lineSeparator());
        for (Map.Entry<String, Set<String>> entry : depList.entrySet()) {
            String kitName = entry.getKey();
            stringBuilder.append("    //").append(kitName).append(System.lineSeparator());
            for (String s : entry.getValue()) {
                if (s.contains("huawei") && onlyG) {
                    stringBuilder.append("    xmsghImplementation ")
                        .append("'")
                        .append(s)
                        .append("'")
                        .append(System.lineSeparator());
                } else {
                    stringBuilder.append("    compileOnly ")
                        .append("'")
                        .append(s)
                        .append("'")
                        .append(System.lineSeparator());
                }
            }
        }
        stringBuilder.append("}").append(System.lineSeparator());
    }

    @Override
    void copyManifest() {
        if (backPath == null) {
            LOGGER.warn("no need to copy AndroidManifest.xml");
            return;
        }
        try {
            List<File> files = new ArrayList<>();
            FileUtils.findFileByName(new File(backPath), "AndroidManifest.xml", files);
            if (files.isEmpty()) {
                LOGGER.warn("copy AndroidManifest.xml failed");
                return;
            }
            File targetFile = null;
            for (File file : files) {
                if (file.getCanonicalPath()
                    .contains(String.join(File.separator, XMS_MODULE_NAME, "src", "main", "AndroidManifest.xml"))) {
                    targetFile = file;
                }
            }
            if (targetFile == null) {
                LOGGER.warn("copy AndroidManifest.xml failed, target xml do not exist !");
                return;
            }
            Path copied = Paths.get(targetPath, XMS_MODULE_NAME, "src", "main", "AndroidManifest.xml");
            String pathStr = targetFile.getCanonicalPath();
            Path path = Paths.get(pathStr);
            Files.copy(path, copied, REPLACE_EXISTING);
        } catch (IOException e) {
            LOGGER.warn("copy AndroidManifest.xml failed {}", e.getMessage());
        }
    }

    private void fillFlavorsPart(StringBuilder strBuilder) {
        addSpace(strBuilder, XmsConstants.LINE_SPACE);
        strBuilder.append("flavorDimensions \"xadaptor\"").append(System.lineSeparator());
        addSpace(strBuilder, XmsConstants.LINE_SPACE);
        strBuilder.append("productFlavors {").append(System.lineSeparator());
        addSpace(strBuilder, XmsConstants.LINE_SPACE * 2);
        strBuilder.append("xmsg {").append(System.lineSeparator());
        addSpace(strBuilder, XmsConstants.LINE_SPACE * 3);
        strBuilder.append("dimension \"xadaptor\"").append(System.lineSeparator());
        addSpace(strBuilder, XmsConstants.LINE_SPACE * 3);
        strBuilder.append("versionNameSuffix \"-xmsg\"").append(System.lineSeparator());
        addSpace(strBuilder, XmsConstants.LINE_SPACE * 2);
        strBuilder.append("}").append(System.lineSeparator());
        addSpace(strBuilder, XmsConstants.LINE_SPACE * 2);
        strBuilder.append("xmsgh {").append(System.lineSeparator());
        addSpace(strBuilder, XmsConstants.LINE_SPACE * 3);
        strBuilder.append("dimension \"xadaptor\"").append(System.lineSeparator());
        addSpace(strBuilder, XmsConstants.LINE_SPACE * 3);
        strBuilder.append("versionNameSuffix \"-xmsgh\"").append(System.lineSeparator());
        addSpace(strBuilder, XmsConstants.LINE_SPACE * 2);
        strBuilder.append("}").append(System.lineSeparator());
        addSpace(strBuilder, XmsConstants.LINE_SPACE);
        strBuilder.append("}").append(System.lineSeparator());
    }

    private void createOverview(StringBuilder stringBuilder, boolean thirdSDK) {
        if (thirdSDK) {
            stringBuilder.append("# This document provides brief instructions for Android Library SDK developers.\n");
        } else {
            stringBuilder.append("# This document provides brief instructions for app developers.\n");
        }
        stringBuilder.append("# More information can be obtained from the following URL:"
            + " https://developer.huawei.com/consumer/en/doc/development/Tools-Guides/90419706\n");
        stringBuilder.append("# Please read the \"*\" sections to apply our module.\n\n");
    }

    private void createContent(StringBuilder stringBuilder, boolean thirdSDK) {
        stringBuilder.append("Content:").append(System.lineSeparator());
        stringBuilder.append("1. Overview").append(System.lineSeparator());
        stringBuilder.append("2. Dependency *").append(System.lineSeparator());
        stringBuilder.append("3. AndroidManifest *").append(System.lineSeparator());
        stringBuilder.append("4. How to use *").append(System.lineSeparator());
        if (thirdSDK) {
            stringBuilder.append("5. Release Guide").append(System.lineSeparator());
        }
        stringBuilder.append(System.lineSeparator());
    }

    private void createManifest(StringBuilder stringBuilder) {
        stringBuilder.append("AndroidManifest").append(System.lineSeparator());
        stringBuilder.append("Please add permissions into \"AndroidManifest.xml\" of your own module"
            + " instead of the xmsadapter module!");
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append("For example, if you use \"Account\" kit, you have to add '<users-permission android:"
            + "name=\"android.permission.MANAGE_ACCOUNTS\">'. Otherwise, it will generate \"Missing permissions...\""
            + " error when compiling.").append(System.lineSeparator());
        stringBuilder
            .append("More information about permission you can refer to this URL: "
                + "https://developer.android.com/reference/android/Manifest.permission.")
            .append(System.lineSeparator());
        stringBuilder.append("NOTICE: If you want to build xmsadapter module itself instead of the whole project, "
            + "you may need copy permissions into \"AndroidManifest.xml\" of xmsadapter module.");
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append("Because our convertor code requires these permissions when compiling separately.");
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append(System.lineSeparator());
    }

    private void createDescription(StringBuilder stringBuilder) {
        stringBuilder.append("1. Overview\n");
        stringBuilder.append("Convertor is a code conversion tool supporting Java and Kotlin projects. It helps"
            + " developers to automatically convert GMS APIs called by apps into corresponding HMS APIs, "
            + "implementing quick conversion and HMS integration.");
        stringBuilder.append("\n");
        stringBuilder.append("Convertor tool generates code in a separate module (named as xmsadapter) "
            + "and provides it as a separate Android Library.\nIn order to ensure the normal use of the module, "
            + "it is strongly recommended not to modify the generated convertor code.\n\n");
    }

    private void createKitInfo(StringBuilder stringBuilder, Map<String, Set<String>> dependencyMap,
        List<String> xmsCodePaths) {
        stringBuilder.append("2. Dependency\n");
        stringBuilder.append("The following kit is identified as being used in your code:\n");
        stringBuilder.append("Kit Name            ");
        stringBuilder.append("Dependencies\n");
        String kitName;
        int spaceLength;
        Set<String> setDependencies;
        String dependency;
        for (Map.Entry<String, Set<String>> entry : dependencyMap.entrySet()) {
            kitName = entry.getKey();
            stringBuilder.append(kitName);
            setDependencies = entry.getValue();
            if (setDependencies == null || setDependencies.size() == 0) {
                stringBuilder.append("\n");
                continue;
            }
            List<String> kitDependencies = new ArrayList<>(setDependencies);
            if (setDependencies.size() == 1) {
                spaceLength = 20 - kitName.length();
                addSpace(stringBuilder, spaceLength);
                dependency = kitDependencies.get(0);
                stringBuilder.append(dependency).append("\n");
            } else {
                for (int i = 0; i < kitDependencies.size(); i++) {
                    if (i == 0) {
                        spaceLength = 20 - kitName.length();
                        addSpace(stringBuilder, spaceLength);
                    } else {
                        addSpace(stringBuilder, 20);
                    }
                    dependency = kitDependencies.get(i);
                    stringBuilder.append(dependency).append("\n");
                }
            }
        }
        stringBuilder.append("The generated convertor code are written into ");
        for (int i = 0; i < xmsCodePaths.size(); i++) {
            String codePath = xmsCodePaths.get(i);
            String rootPath = codePath.substring(0, codePath.indexOf("xmsadapter") - 1);
            codePath = codePath.replace(rootPath, "PROJECT_PATH");
            if (i == xmsCodePaths.size() - 1) {
                stringBuilder.append(codePath);
            } else {
                stringBuilder.append(codePath).append(", ");
            }
        }
        stringBuilder.append(", and we have added these dependencies to the build.gradle of xmsadapter module"
            + "(only for compile).\n\n");
    }

    private void createInstruction(StringBuilder stringBuilder) {
        stringBuilder.append("4. How to use a separate module\n");
        stringBuilder.append("Step 1: Please add \"implementation project (path: ':xmsadapter')\" into the dependency"
            + " part of build.gradle file of the other module that depends on the convertor code.\n");
        stringBuilder.append("Step 2: Add ‘xmsadapter’ to the settings.gradle file.\n");
        stringBuilder.append("Step 3(Optional): Remove GMS related dependencies in your original build.gradle file,"
            + " because we have added them in \"xmsadapter\" module.").append(System.lineSeparator());
        stringBuilder.append("Notice:").append(System.lineSeparator());
        stringBuilder.append("If you need more services(kits) and want to generate the corresponding convertor code, "
            + "please refer the following tips:").append(System.lineSeparator());
        stringBuilder.append("Choice 1: Please add the dependencies of new kit into build.gradle file of xmsadapter, "
            + "and start a new conversion.");
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append("Choice 2: Add the dependencies of new kit into build.gradle file of your own project;");
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append("After rescanning with our plugin(start a new conversion),"
            + " copy the newly generated dependencies to build.gradle of xmsadapter."
            + " Because the newly generated code will depend on the new kit dependencies.\n\n");
    }

    private void createReleaseGuide(StringBuilder stringBuilder, boolean dependencyIsNull) {
        if (dependencyIsNull) {
            stringBuilder.append("4. ");
        } else {
            stringBuilder.append("5. ");
        }
        stringBuilder.append("SDK Release Guide").append(System.lineSeparator());
        stringBuilder.append("For Android Library SDK developers, the usage of convertor code is consistent with GMS. "
            + "When packaging, please do not put the convertor code into the lib package, otherwise it will conflict"
            + " with the App developer's source code.").append(System.lineSeparator());
        stringBuilder
            .append(
                "We provide the following release templates to help you supplementthe original release instructions.")
            .append(System.lineSeparator())
            .append(System.lineSeparator());
        stringBuilder.append("# user manual template for SDK developer\n" + "\n"
            + "# template for original user manual\n" + "Add the SDK to your project\n"
            + "If you are using Maven, add the following to your build.gradle file:\n" + "{your sdk dependency}\n"
            + "\n" + "Add Google Play Services\n"
            + "To enable the Google Mobile Service our SDK, you must integrate Google Play Services. "
            + "If you haven't done this yet, please add dependency to the Google Play Services library by "
            + "adding the following dependecy to your dependencies block of app's build.gradle file:\n"
            + "{your dependency of GMS}\n" + "\n" + "# template for integration with Huawei Mobile Service\n"
            + "Integrate Google Mobile Service and Huawei Mobile Service together\n"
            + "For app developer, if you are integrating GMS and Huawei Mobile Service together in one app, "
            + "{SDK-name} requires the convertor code (Please refer:"
            + " https://developer.huawei.com/consumer/en/doc/development/Tools-Guides/90419706). \n"
            + "There are two ways to generate the convertor code by using HMS Toolkit"
            + "(https://developer.huawei.com/consumer/en/doc/development/Tools-Guides/05673260).\n" + "Choice One:\n"
            + "1. please add the GMS dependencies into dependencies block of app's build.gradle file.\n"
            + "2. use the HMS Toolkit - Convertor to start a new conversion"
            + "(https://developer.huawei.com/consumer/en/doc/development/Tools-Guides/90419706) \n" + "\n"
            + "Choice Two(App has been working with HMS Toolkit-Convertor already):\n"
            + "1. Use the HMS Toolkit - Repository by clicking HMS -> Repository menu\n"
            + "2. choose the kit you used and click apply, HMS Toolkit will generate convertor code automatically.\n"
            + "\n\n");
    }

    /**
     * infer g first or h first
     *
     * @param root G+H source code root folder
     * @return GorH or HorG
     */
    public static GeneratorStrategyKind inferGHFirst(String root) {
        // 1. obtain GlobalEvnSetting.java if any,else GorH
        // 2. if there exits code line"isHms = !gAvailable || hAvailable;", then HorG,else GorH
        List<File> files = new ArrayList<>();
        FileUtils.findFileByName(new File(root), XmsConstants.GLOBAL_ENV_SETTING, files);
        if (files.size() == 0) {
            return GeneratorStrategyKind.GOrH;
        }
        File file = files.get(0);
        // more than 2 settings, use gh version
        if (files.size() >= 2) {
            for (File f : files) {
                try {
                    if (f.getCanonicalPath().contains("xmsgh")) {
                        file = f;
                        break;
                    }
                } catch (IOException e) {
                    LOGGER.info("fail to getCanonicalPath");
                }
            }
        }
        // find GlobalEnvSetting.java
        // search isHms = !gAvailable || hAvailable;
        try (Stream<String> s = Files.lines(file.toPath())) {
            if (s.anyMatch(x -> x.contains("isHms = !gAvailable || hAvailable"))) {
                return GeneratorStrategyKind.HOrG;
            }
        } catch (IOException e) {
            return GeneratorStrategyKind.GOrH;
        }
        return GeneratorStrategyKind.GOrH;
    }
}
