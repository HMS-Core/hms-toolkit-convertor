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

package com.huawei.inquiry.utils;

import static com.huawei.inquiry.docs.EntireDoc.STRATEGYTYPE.G;
import static com.huawei.inquiry.docs.EntireDoc.STRATEGYTYPE.H;
import static com.huawei.inquiry.docs.EntireDoc.STRATEGYTYPE.X;

import com.huawei.generator.ast.custom.XClassDoc;
import com.huawei.generator.ast.custom.XFieldDoc;
import com.huawei.generator.ast.custom.XMethodDoc;
import com.huawei.inquiry.docs.EntireDoc;
import com.huawei.inquiry.docs.ZClassDoc;
import com.huawei.inquiry.docs.ZFieldDoc;
import com.huawei.inquiry.docs.ZMethodDoc;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * read jsons in javadoc-json directory.
 *
 * @since 2020-07-25
 */
public class DocJsonReader {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocJsonReader.class);

    private List<FileInputStream> fileInputStreamList = new ArrayList<>();

    private Map<String, String> gmsVersionMap;

    private String pluginPath;

    public DocJsonReader(String pluginPath, Map<String, String> gmsVersionMap) {
        this.pluginPath = pluginPath;
        this.gmsVersionMap = gmsVersionMap;
    }

    /**
     * this method is used for reading kit_doc.json files in xms, gms, hms directory in plugin or resources.
     *
     * @param strategyType three types including X, H, G these mean reading jsons from xms, hms, gms directory
     * @param handleSignature whether to handle signature including class, method, field
     * @return result map, key is signature string such as "org.xms.g.wallet.wobs.WalletObjectsConstants" and value is
     *         classDoc including xClassDoc, hClassDoc, gClassDoc.
     */
    public Map readDocJsonsToMap(EntireDoc.STRATEGYTYPE strategyType, boolean handleSignature) {
        boolean isX = strategyType == X;
        boolean isZ = strategyType == G || strategyType == H;
        if ((!isX && !isZ) || gmsVersionMap.isEmpty()) {
            return Collections.EMPTY_MAP;
        }

        Map<String, XClassDoc> xClassDocMap = null;
        Map<String, ZClassDoc> zClassDocMap = null;

        if (isX) {
            xClassDocMap = pluginPath != null ? readDocJsonsFromPlugin(strategyType, pluginPath, gmsVersionMap)
                : readDocJsonsFromResources(strategyType, gmsVersionMap);
            if (!xClassDocMap.isEmpty() && handleSignature) {
                handleXDocSignature(xClassDocMap);
            }
            return xClassDocMap;
        } else {
            zClassDocMap = pluginPath != null ? readDocJsonsFromPlugin(strategyType, pluginPath, gmsVersionMap)
                : readDocJsonsFromResources(strategyType, gmsVersionMap);
            if (!zClassDocMap.isEmpty() && handleSignature) {
                handleZDocSignature(zClassDocMap);
            }
            return zClassDocMap;
        }
    }

    /**
     * this method is used for reading kit_doc.json files in xms, gms, hms directory in .jar of plugin.
     *
     * @param strategyType three types including X, H, G these mean reading jsons from xms, hms, gms directory in .jar
     * @param pluginPath the path of plugin, namely the path of xms-engine.jar, is from IDE
     * @param gmsVersionMap the map containing the kit name and version is from IDE
     * @return result map, key is signature string such as "org.xms.g.wallet.wobs.WalletObjectsConstants" and value is
     *         classDoc including xClassDoc, hClassDoc, gClassDoc.
     */
    private Map readDocJsonsFromPlugin(EntireDoc.STRATEGYTYPE strategyType, String pluginPath,
        Map<String, String> gmsVersionMap) {
        boolean isX = strategyType == X; // ture when read from directory “javadoc-json/xms/”
        String strategyTypeValue = strategyType.getValue();
        Map<String, XClassDoc> thisKitMapX;
        Map<String, ZClassDoc> thisKitMapZ;
        Map<String, XClassDoc> xClassDocMap = new HashMap<>();
        Map<String, ZClassDoc> zClassDocMap = new HashMap<>();
        Gson gson = new Gson();
        Type typeX;
        Type typeZ;
        try (ZipFile zipFile = new ZipFile(pluginPath);
            ZipInputStream zip = new ZipInputStream(new FileInputStream(new File(pluginPath)))) {
            ZipEntry nextEntry = zip.getNextEntry();
            while (nextEntry != null) {
                if (!nextEntry.getName().contains("javadoc-json")) {
                    nextEntry = zip.getNextEntry();
                    continue;
                }
                for (Map.Entry<String, String> entry : gmsVersionMap.entrySet()) {
                    String kitName = entry.getKey();
                    String version = entry.getValue();
                    String jsonPath = "javadoc-json/" + strategyTypeValue + "ms" + "/" + kitName + "/" + version;
                    JsonReader reader = null;
                    if (nextEntry.getName().contains(jsonPath) && nextEntry.getName().endsWith(".json")) {
                        InputStream inputstream = zipFile.getInputStream(nextEntry);
                        reader = new JsonReader(new InputStreamReader(inputstream, StandardCharsets.UTF_8));
                    }
                    if (reader == null) {
                        continue;
                    }
                    if (isX) {
                        typeX = new DocUtil.MapTypeTokenX().getType();
                        thisKitMapX = gson.fromJson(reader, typeX);
                        xClassDocMap.putAll(thisKitMapX);
                    } else {
                        typeZ = new DocUtil.MapTypeTokenZ().getType();
                        thisKitMapZ = gson.fromJson(reader, typeZ);
                        zClassDocMap.putAll(thisKitMapZ);
                    }
                }
                nextEntry = zip.getNextEntry();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Fail to unzip web javadoc to user path");
        }
        return isX ? xClassDocMap : zClassDocMap;
    }

    /**
     * this method is used for reading kit_doc.json files in xms, gms, hms directory in resources.
     *
     * @param strategyType three types including X, H, G these mean reading jsons from xms, hms, gms directory
     * @param gmsVersionMap the map containing the kit name and version is from IDE
     * @return result map, key is signature string such as "org.xms.g.wallet.wobs.WalletObjectsConstants" and value is
     *         classDoc including xClassDoc, hClassDoc, gClassDoc.
     */
    private Map readDocJsonsFromResources(EntireDoc.STRATEGYTYPE strategyType,
        Map<String, String> gmsVersionMap) {
        boolean isX = strategyType == X;
        Map<String, XClassDoc> xClassDocMap = new HashMap<>();
        Map<String, ZClassDoc> zClassDocMap = new HashMap<>();

        String rootPath = System.getProperty("user.dir") + "/src/main/resources/javadoc-json/";
        String strategyTypeValue = strategyType.getValue();
        Map<String, XClassDoc> thisKitMapX;
        Map<String, ZClassDoc> thisKitMapZ;
        Type typeX = new DocUtil.MapTypeTokenX().getType();
        Type typeZ = new DocUtil.MapTypeTokenZ().getType();
        Gson gson = new Gson();
        for (Map.Entry<String, String> entry : gmsVersionMap.entrySet()) {
            fileInputStreamList.clear();
            String kitName = entry.getKey();
            String version = entry.getValue();
            String jsonPath = strategyTypeValue + "ms/" + kitName + "/" + version;
            File jsonDirectory = new File(rootPath + jsonPath);
            if (jsonDirectory.exists()) {
                searchAllJsonFiles(jsonDirectory);
            }
            if (fileInputStreamList.isEmpty()) {
                continue;
            }
            for (FileInputStream fis : fileInputStreamList) {
                JsonReader reader = new JsonReader(new InputStreamReader(fis, StandardCharsets.UTF_8));
                if (isX) {
                    thisKitMapX = gson.fromJson(reader, typeX);
                    if (thisKitMapX != null) {
                        xClassDocMap.putAll(thisKitMapX);
                    }
                } else {
                    thisKitMapZ = gson.fromJson(reader, typeZ);
                    if (thisKitMapZ != null) {
                        zClassDocMap.putAll(thisKitMapZ);
                    }
                }
            }
        }
        return isX ? xClassDocMap : zClassDocMap;
    }

    /**
     * set the signature to corresponding xClassDoc, method and field if any.
     *
     * @param xClassDocMap key is class signature and value is xClassDoc
     */
    private void handleXDocSignature(Map<String, XClassDoc> xClassDocMap) {
        for (Map.Entry<String, XClassDoc> entryClass : xClassDocMap.entrySet()) {
            String className = entryClass.getKey();
            XClassDoc xClassDoc = entryClass.getValue();
            // set signature to xClassDoc
            xClassDoc.setSignature(className);
            // set signature to xMethodDoc
            Map<String, XMethodDoc> xMethodDocMap = xClassDoc.getMethods();
            if (!xMethodDocMap.isEmpty()) {
                for (Map.Entry<String, XMethodDoc> entryMethod : xMethodDocMap.entrySet()) {
                    XMethodDoc xMethodDoc = entryMethod.getValue();
                    xMethodDoc.setSignature(entryMethod.getKey());
                    xMethodDoc.settingsForIDE();
                }
            }
            // set signature to xFieldDoc
            // key is empty in this phase
            Map<String, XFieldDoc> xFieldDocMap = xClassDoc.getFields();
            if (xFieldDocMap.isEmpty()) {
                continue;
            }
            for (XFieldDoc xFieldDoc : xFieldDocMap.values()) {
                String simpleName = FieldDocUtil.simpleName(xFieldDoc.getFieldInfo());
                xFieldDoc.setSignature(className + "." + simpleName);
            }
        }
    }

    /**
     * set the signature to corresponding zClassDoc, method and field if any.
     *
     * @param zClassDocMap key is class signature and value is zClassDoc
     */
    private void handleZDocSignature(Map<String, ZClassDoc> zClassDocMap) {
        for (Map.Entry<String, ZClassDoc> entryClass : zClassDocMap.entrySet()) {
            ZClassDoc zClassDoc = entryClass.getValue();
            // set signature to zClassDoc
            zClassDoc.setSignature(entryClass.getKey());
            // set signature to zMethodDoc
            Map<String, ZMethodDoc> methodDocMap = zClassDoc.getMethods();
            if (!methodDocMap.isEmpty()) {
                for (Map.Entry<String, ZMethodDoc> entryMethod : methodDocMap.entrySet()) {
                    ZMethodDoc zMethodDoc = entryMethod.getValue();
                    zMethodDoc.setSignature(entryMethod.getKey());
                    zMethodDoc.settingForIDE();
                }
            }
            // set signature to zFieldDoc
            // key is not empty in this phase because it is from jsons
            Map<String, ZFieldDoc> zFieldDocMap = zClassDoc.getFields();
            if (zFieldDocMap.isEmpty()) {
                continue;
            }
            for (Map.Entry<String, ZFieldDoc> entryField : zFieldDocMap.entrySet()) {
                entryField.getValue().setSignature(entryField.getKey());
            }
        }
    }

    // search all files in the directory of file, and add them into fileInputStreamList.
    private void searchAllJsonFiles(File file) {
        File[] files = file.listFiles();
        if (files == null) {
            return ;
        }
        for (File f : files) {
            if (f.isDirectory()) {
                searchAllJsonFiles(f);
            }
            if (f.isFile()) {
                try {
                    fileInputStreamList.add(new FileInputStream(f));
                } catch (FileNotFoundException e) {
                    LOGGER.error("Can't find json when searching all Json files");
                }
            }
        }
    }
}