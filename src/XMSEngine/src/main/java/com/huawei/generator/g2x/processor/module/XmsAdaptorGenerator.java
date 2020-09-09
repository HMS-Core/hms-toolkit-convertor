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
import com.huawei.generator.utils.PropertyUtils;
import com.huawei.generator.utils.StaticPatcher;

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
import java.util.HashSet;
import java.util.stream.Stream;

/**
 * Class for XmsAdaptorGenerator
 *
 * @since 2020-04-07
 */
public final class XmsAdaptorGenerator extends ModuleGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger(XmsAdaptorGenerator.class);

    private final GeneratorStrategyKind kind;

    private final boolean sdk;

    private final boolean isG;

    private final boolean isH;

    public XmsAdaptorGenerator(ProcessorUtils processorUtils) {
        super(processorUtils);
        resolveDepMap(processorUtils.getAllDepMap());
        modulePath = String.join(File.separator, targetPath, XmsConstants.XMS_MODULE_NAME);
        summaryPath = String.join(File.separator, targetPath, XmsConstants.XMS_MODULE_NAME, "config");
        manifestPath = String.join(File.separator, modulePath, "src", "main");
        List<GeneratorStrategyKind> kindList = processorUtils.getStrategyKindList();
        kind = kindList.contains(GeneratorStrategyKind.GOrH) ? GeneratorStrategyKind.GOrH : GeneratorStrategyKind.HOrG;
        sdk = processorUtils.isThirdSDK();
        newSummary.getStrategy().addAll(kindList);
        isG = kindList.contains(GeneratorStrategyKind.G);
        isH = kindList.contains(GeneratorStrategyKind.H);
    }

    @Override
    void resolveDepMap(Map<String, Set<String>> allDepMap) {
        kitSet.forEach(kitName -> {
            if (allDepMap.get(kitName) != null) {
                depList.put(kitName, allDepMap.get(kitName));
            }
        });
    }

    @Override
    public boolean generateCode() {
        newSummary.setKitNames(kitSet);
        String ghJavaPath = String.join(File.separator, modulePath, "src", "main", "java");
        if (isG || isH) {
            ghJavaPath = String.join(File.separator, modulePath, "src", "xmsgh", "java");
        }
        GeneratorResult result = primaryGenerate(pluginPath, summaryPath, ghJavaPath, new ArrayList<>(kitSet), kind);
        generateSummary.xmsCodePaths.add(ghJavaPath);

        if (result != GeneratorResult.SUCCESS) {
            generateSummary.setResult(result);
            return false;
        }

        if (isG) {
            String gJavaPath = String.join(File.separator, modulePath, "src", "xmsg", "java");
            result =
                primaryGenerate(pluginPath, summaryPath, gJavaPath, new ArrayList<>(kitSet), GeneratorStrategyKind.G);
            generateSummary.xmsCodePaths.add(gJavaPath);
            if (result != GeneratorResult.SUCCESS) {
                generateSummary.setResult(result);
                return false;
            }
        }
        if (isH) {
            String hJavaPath = String.join(File.separator, modulePath, "src", "xmsh", "java");
            result =
                primaryGenerate(pluginPath, summaryPath, hJavaPath, new ArrayList<>(kitSet), GeneratorStrategyKind.H);
            generateSummary.xmsCodePaths.add(hJavaPath);
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
        checkFlavor();
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
        createAgcGuide(stringBuilder);
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

        List<String> excludes = new ArrayList<>();
        StaticPatcher.copyResourceDir(pluginPath, modulePath, "xms/scripts/", excludes);
        return true;
    }

    @Override
    void fillApplyPart(StringBuilder stringBuilder) {
        // Can be extended in the future
        stringBuilder.append("apply plugin: 'com.android.library'").append(System.lineSeparator());
        stringBuilder.append("apply plugin: 'com.huawei.agconnect'").append(System.lineSeparator());
        stringBuilder.append("apply from: 'scripts/productFlavor.gradle'").append(System.lineSeparator());
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
        stringBuilder.append("}").append(System.lineSeparator());
        stringBuilder.append(System.lineSeparator());
    }

    @Override
    void fillDependencyPart(StringBuilder stringBuilder) {
        // add dependencyPart according to Kit-Information map
        stringBuilder.append("dependencies {").append(System.lineSeparator());
        Set<String> addedDependency = new HashSet<>();
        for (Map.Entry<String, Set<String>> entry : depList.entrySet()) {
            String kitName = entry.getKey();
            stringBuilder.append("    //").append(kitName).append(System.lineSeparator());
            for (String s : entry.getValue()) {
                if (!addedDependency.contains(s)) {
                    addedDependency.add(s);
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
            LOGGER.warn("copy AndroidManifest.xml failed");
        }
    }

    private void createOverview(StringBuilder stringBuilder, boolean thirdSDK) {
        if (thirdSDK) {
            stringBuilder.append("# This document provides brief instructions for Android Library SDK developers.\n");
        } else {
            stringBuilder.append("# This document provides brief instructions for app developers.\n");
        }
        stringBuilder
            .append("# To learn more, visit the following link:" + PropertyUtils.getProperty("createOverview"));
        stringBuilder.append("# Please read the sections marked with asterisks(*) to apply the xmsadapter module.\n\n");
    }

    private void createContent(StringBuilder stringBuilder, boolean thirdSDK) {
        stringBuilder.append("Content:").append(System.lineSeparator());
        stringBuilder.append("1. Overview").append(System.lineSeparator());
        stringBuilder.append("2. Dependency *").append(System.lineSeparator());
        stringBuilder.append("3. AndroidManifest *").append(System.lineSeparator());
        stringBuilder.append("4. How to Use the xmsadapter Module *").append(System.lineSeparator());
        stringBuilder.append("5. About AppGallery Connect").append(System.lineSeparator());
        if (thirdSDK) {
            stringBuilder.append("6. SDK Release Guide").append(System.lineSeparator());
        }
        stringBuilder.append(System.lineSeparator());
    }

    private void createManifest(StringBuilder stringBuilder) {
        stringBuilder.append("AndroidManifest").append(System.lineSeparator());
        stringBuilder.append(
            "Add permissions to the \"AndroidManifest.xml\" of your own module" + " instead of the xmsadapter module!");
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append("For example, if you are integrating \"Account\" Kit, add '<users-permission android:"
            + "name=\"android.permission.MANAGE_ACCOUNTS\"/>'. Otherwise, an error \"Missing permissions...\""
            + " will occur during compilation.").append(System.lineSeparator());
        stringBuilder.append("To learn more about the permissions, visit the following link: "
            + PropertyUtils.getProperty("createManifest")).append(System.lineSeparator());
        stringBuilder.append("NOTICE: If you want to build only the xmsadapter module rather than the whole project,"
            + " you may need copy permissions to the \"AndroidManifest.xml\" of the xmsadapter module.");
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append("Because the generated code requires these permissions when compiling separately.");
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append(System.lineSeparator());
    }

    private void createDescription(StringBuilder stringBuilder) {
        stringBuilder.append("1. Overview\n");
        stringBuilder.append("HMS Convertor is a code conversion tool that supports Java and Kotlin projects."
            + " It helps developers automatically convert GMS APIs called by apps into HMS APIs,"
            + " implementing quick conversion and HMS integration.").append(System.lineSeparator());
        stringBuilder.append("HMS Convertor generates code in a separate module (named as xmsadapter)"
            + " and provides it as a separate Android Library.\nTo ensure the proper use of the module,"
            + " it is recommended that you do not modify the generated code.\n\n");
    }

    private void createKitInfo(StringBuilder stringBuilder, Map<String, Set<String>> dependencyMap,
        List<String> xmsCodePaths) {
        stringBuilder.append("2. Dependency\n");
        stringBuilder.append("The following kits are identified in your code:\n");
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
        stringBuilder.append("The generated code is written into ");
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
        stringBuilder.append(" and we have added these dependencies to the build.gradle of xmsadapter module"
            + " (only for compile).\n\n");
    }

    private void createInstruction(StringBuilder stringBuilder) {
        stringBuilder.append("4. How to Use the xmsadapter Module\n");
        stringBuilder.append("Step 1: Add \"implementation project (path: ':xmsadapter')\" to the \"dependencies\""
            + " block in the build.gradle file of your module that depends on the generated code.\n");
        stringBuilder.append("Step 2: Add \"xmsadapter\" to the settings.gradle file.\n");
        stringBuilder.append("(Optional) Step 3: Remove GMS-related dependencies from your original build.gradle file,"
            + " because they have been added in the \"xmsadapter\" module.").append(System.lineSeparator());
        stringBuilder.append("Notice:").append(System.lineSeparator());
        stringBuilder
            .append("If you need more kits and want to generate related code," + " please refer the following tips:")
            .append(System.lineSeparator());
        stringBuilder.append("Choice 1: Add the dependencies of the new kit to the build.gradle file of xmsadapter,"
            + " and start a new conversion;");
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append("Choice 2: Add the dependencies of the new kit to the build.gradle file of your project.");
        stringBuilder.append(System.lineSeparator());
        stringBuilder.append("After rescanning with our plugin(start a new conversion),"
            + " copy the newly generated dependencies to build.gradle of xmsadapter."
            + " Because the newly generated code will depend on the new kit dependencies.\n\n");
    }

    private void createAgcGuide(StringBuilder stringBuilder) {
        stringBuilder.append("5. About AppGallery Connect").append(System.lineSeparator());
        stringBuilder.append("Before accessing AppGallery Connect,");
        stringBuilder.append(" make preparations to ensure that you are familiar with AppGallery Connect services.\n");
        stringBuilder.append("The services provided by AGC may be different from those provided by Firebase.");
        stringBuilder.append(
            " For details about related documents and operation processes," + " please refer to the following:\n");
        stringBuilder.append("1. Authentication:\n");
        stringBuilder.append(PropertyUtils.getProperty("createAgc")
            + "agc-conversion-auth-0000001050157270#EN-US_TOPIC_0000001050157270__section104191036162614\n");
        stringBuilder.append("2. Crashlytics:\n");
        stringBuilder.append(PropertyUtils.getProperty("createAgc")
            + "agc-conversion-crash-0000001050159223#EN-US_TOPIC_0000001050159223__section6170175914464\n");
        stringBuilder.append("3. DynamicLinks:\n");
        stringBuilder.append(PropertyUtils.getProperty("createAgc")
            + "agc-conversion-dyna-0000001050157272#EN-US_TOPIC_0000001050157272__section567611378481\n");
        stringBuilder.append("4. Functions:\n");
        stringBuilder.append(PropertyUtils.getProperty("createAgc")
            + "agc-conversion-func-0000001050159225#EN-US_TOPIC_0000001050159225__section974018491170\n");
        stringBuilder.append("5. RemoteConfig:\n");
        stringBuilder.append(PropertyUtils.getProperty("createAgc")
            + "agc-conversion-remote-0000001050157274#EN-US_TOPIC_0000001050157274__section1897102414127\n");
        stringBuilder.append("6. Performance:\n");
        stringBuilder.append(PropertyUtils.getProperty("createAgc")
            + "agc-conversion-perf-0000001050773636#EN-US_TOPIC_0000001050773636__section0147124193414\n");
        stringBuilder.append("7. Storage:\n");
        stringBuilder.append(PropertyUtils.getProperty("createAgcStorage")
            + "agc-cloudstorage-introduction\n");
        stringBuilder.append("8. InAppMessaging:\n");
        stringBuilder.append(PropertyUtils.getProperty("createAgc")
            + "agc-conversion-inapp-0000001051053689#EN-US_TOPIC_0000001051053689__section16275141675911\n\n");
    }

    private void createReleaseGuide(StringBuilder stringBuilder, boolean dependencyIsNull) {
        if (dependencyIsNull) {
            stringBuilder.append("5. ");
        } else {
            stringBuilder.append("6. ");
        }
        stringBuilder.append("SDK Release Guide").append(System.lineSeparator());
        stringBuilder
            .append("For Android Library SDK developers," + " use the generated code in the same way as using GMS code."
                + " When packaging, don't put the generated code into the lib package."
                + " Otherwise, it will conflict with the app developer's source code.")
            .append(System.lineSeparator());
        stringBuilder.append("We provide the following release templates to help supplement your release instructions.")
            .append(System.lineSeparator())
            .append(System.lineSeparator());
        stringBuilder.append("# Template for your user manual\n" + "Add the SDK to your project\n"
            + "If you are using the Maven repository, add the following to your build.gradle file:\n"
            + "{your sdk dependency}\n" + "\n" + "Add Google Play Services\n"
            + "To enable GMS in our SDK, you must integrate Google Play Services. "
            + "If you haven't done this yet, add the dependency of the Google Play Services library to the"
            + " \"dependencies\" block in your app's build.gradle file:\n" + "{your dependency of GMS}\n" + "\n"
            + "# Template for integration with Huawei Mobile Service\n" + "Integrate both GMS and HMS\n"
            + "For app developer, if you are integrating GMS and HMS in one app, "
            + "you will need to call the generated SDK when developing your SDK {SDK-name}. \n"
            + "The method below is provided for you to generate code using HMS ToolKit. "
            + "(For more information, please visit the following URL: "
            + PropertyUtils.getProperty("createReleaseGuideUsingHMSToolKit") + "\nMethod 1:\n"
            + "1. Add the GMS dependencies to the dependencies block of your app's build.gradle file.\n"
            + "2. Use HMS Convertor to start a new conversion. "
            + "(For more information, please visit the following URL: "
            + PropertyUtils.getProperty("createReleaseGuideNewConversion") + "\n\n" + "\n\n");
    }

    /**
     * infer g first or h first
     *
     * @param root G+H source code root folder
     * @return GorH or HorG
     */
    public static GeneratorStrategyKind inferGHFirst(String root) {
        // 1. get GlobalEvnSetting.java, if failed, set GorH
        // 2. if there is 'isHms = !gAvailable || hAvailable;', set HorG, others set GorH
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
        try (Stream<String> lines = Files.lines(file.toPath())) {
            if (lines.anyMatch(line -> line.contains("isHms = !gAvailable || hAvailable"))) {
                return GeneratorStrategyKind.HOrG;
            }
        } catch (IOException e) {
            return GeneratorStrategyKind.GOrH;
        }
        return GeneratorStrategyKind.GOrH;
    }

    private void checkFlavor() {
        String targetScriptPath = String.join(File.separator, modulePath, "scripts");
        String gProductFlavor = String.join(File.separator, targetScriptPath, "gproductFlavor.gradle");
        String hProductFlavor = String.join(File.separator, targetScriptPath, "hproductFlavor.gradle");
        String ghProductFlavor = String.join(File.separator, targetScriptPath, "xproductFlavor.gradle");
        String productFlavor = String.join(File.separator, targetScriptPath, "productFlavor.gradle");
        if (!isG && !isH) {
            FileUtils.delFile(new File(targetScriptPath));
            FileUtils.createFile("", targetScriptPath, "productFlavor.gradle");
        }
        if (!isG && isH) {
            FileUtils.delFile(new File(gProductFlavor));
            FileUtils.delFile(new File(ghProductFlavor));
            FileUtils.delFile(new File(productFlavor));
            File hFile = new File(hProductFlavor);
            if (hFile.exists()) {
                if (hFile.renameTo(new File(productFlavor))) {
                    LOGGER.info("renaming productFlavour.gradle!");
                } else {
                    LOGGER.error("rename productFlavour.gradle Failed!");
                }
            }
        }
        if (isG && !isH) {
            FileUtils.delFile(new File(hProductFlavor));
            FileUtils.delFile(new File(ghProductFlavor));
            FileUtils.delFile(new File(productFlavor));
            File gFile = new File(gProductFlavor);
            if (gFile.exists()) {
                if (gFile.renameTo(new File(productFlavor))) {
                    LOGGER.info("renaming productFlavour.gradle");
                } else {
                    LOGGER.error("rename productFlavour.gradle Failed!");
                }
            }
        }
        if (isG && isH) {
            FileUtils.delFile(new File(gProductFlavor));
            FileUtils.delFile(new File(hProductFlavor));
            FileUtils.delFile(new File(productFlavor));
            File ghFile = new File(ghProductFlavor);
            if (ghFile.exists()) {
                if (ghFile.renameTo(new File(productFlavor))) {
                    LOGGER.info("renaming productFlavour.gradle");
                } else {
                    LOGGER.error("rename productFlavour.gradle Failed!");
                }
            }
        }
    }
}
