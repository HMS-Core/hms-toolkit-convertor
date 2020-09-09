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

package com.huawei.inquiry;

import static com.huawei.inquiry.utils.DocUtil.ERROR_IN_JSON;
import static com.huawei.inquiry.utils.DocUtil.DECLARATION;
import static com.huawei.inquiry.utils.DocUtil.CANNOT_FIND;

import com.huawei.generator.ast.custom.XClassDoc;
import com.huawei.generator.ast.custom.XFieldDoc;
import com.huawei.generator.ast.custom.XMethodDoc;
import com.huawei.generator.g2x.po.kit.KitMapping;
import com.huawei.generator.exception.UnExpectedProcessException;
import com.huawei.inquiry.docs.Docs;
import com.huawei.inquiry.docs.EntireDoc;
import com.huawei.inquiry.docs.XDocs;
import com.huawei.inquiry.docs.ZClassDoc;
import com.huawei.inquiry.docs.ZDocs;
import com.huawei.inquiry.docs.ZFieldDoc;
import com.huawei.inquiry.docs.ZMethodDoc;
import com.huawei.inquiry.utils.DocJsonReader;
import com.huawei.inquiry.utils.DocUtil;
import com.huawei.inquiry.utils.MethodDocUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * this class create maps follows three steps:
 * 1. read x/h/g javadoc jsons
 * 2. generate xClassDocMap/hClassDocMap/gClassDocMap
 * 3. generate x2gh/g2x/h2x mapping
 *
 * @since 2020-07-25
 */
public class DocMapsCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocMapsCreator.class);

    private static final int DECLARATION_NUM = 1; // used for hms name is empty in json

    private final int MULTI_MATCH = 1; // used for multi g or h

    // Key: X signature  Value: String[0]: G signature  String[1]: H signature
    private Map<String, String[]> x2ghSignatureMap = new HashMap<>();

    private Map<String, String> g2xSignatureMapTemp = new HashMap<>();

    private Map<SignatureStruct, String> g2xSignatureMap = new HashMap<>();

    private Map<String, String> h2xSignatureMapTemp = new HashMap<>();

    private Map<SignatureStruct, String> h2xSignatureMap = new HashMap<>();

    private Map<String, XClassDoc> xClassDocMap;

    private Map<String, ZClassDoc> gClassDocMap;

    private Map<String, ZClassDoc> hClassDocMap;

    private Map<String, String> gmsVersionMap; // key is simple kit name

    private String pluginPath;

    private int gClassIsEmpty = DECLARATION_NUM; // used for gms class's name is empty in json

    private int hClassIsEmpty = DECLARATION_NUM; // used for hms class's name is empty in json

    private int gMethodIsEmpty = DECLARATION_NUM; // used for gms class's name is empty in json

    private int hMethodIsEmpty = DECLARATION_NUM; // used for hms method's name is empty in json

    public DocMapsCreator(String pluginPath, Map<String, String> gmsVersionMap) {
        this.pluginPath = pluginPath;
        this.gmsVersionMap = KitMapping.processGmsVersion(gmsVersionMap);
        generateClassDocMap();
        generateXGHMapping();
    }

    private void generateClassDocMap() {
        DocJsonReader reader = new DocJsonReader(pluginPath, gmsVersionMap);
        xClassDocMap = reader.readDocJsonsToMap(EntireDoc.STRATEGYTYPE.X,  true);
        gClassDocMap = reader.readDocJsonsToMap(EntireDoc.STRATEGYTYPE.G, true);
        hClassDocMap = reader.readDocJsonsToMap(EntireDoc.STRATEGYTYPE.H, true);
    }

    // get xms information including class, method, field.
    public Map<String, Docs> searchX(String requestText) {
        return searchXZ(requestText, true, false);
    }

    // get hms information including class, method, field.
    public Map<String, Docs> searchH(String requestText) {
        return searchXZ(requestText, false, true);
    }

    // get gms information including class, method, field.
    public Map<String, Docs> searchG(String requestText) {
        return searchXZ(requestText, false, false);
    }

    // get x and z namely h and g, including class, method, field, when search.
    private Map<String, Docs> searchXZ(String requestText, boolean isX, boolean isH) {
        Map resultDocsMap = new LinkedHashMap<>();
        Map docsMap = isX ? this.getAllXDocsMap() : this.getAllZDocsMap(isH);

        Iterator<String> iterator = isX ? getAllXSignaturesFiltered(requestText).iterator()
            : getAllZSignaturesFiltered(requestText, isH).iterator();
        while (iterator.hasNext()) {
            String signature = iterator.next();
            Object doc = docsMap.get(signature);
            if (doc != null) {
                resultDocsMap.put(signature, doc);
            }
        }
        return resultDocsMap;
    }

    // generate xms, gms and hms mapping relation.
    private void generateXGHMapping() {
        if (xClassDocMap == null || xClassDocMap.isEmpty()) {
            return;
        }
        for (XClassDoc xClassDoc : xClassDocMap.values()) {
            // first: class mapping
            handleClassSignatureMapping(xClassDoc);

            // second: method mapping
            handleMethodSignatureMapping(xClassDoc);

            // third: field mapping
            handleFieldSignatureMapping(xClassDoc);
        }
    }

    // generate class signature mapping relation between xms, gms and hms
    private void handleClassSignatureMapping(XClassDoc xClassDoc) {
        String xClassName = xClassDoc.getSignature();
        String gClassName = xClassDoc.getGClassName();
        String hClassName = xClassDoc.getHClassName();
        // gName is ""
        if (gClassName.isEmpty()) {
            gClassName = DECLARATION + gClassIsEmpty + ":no corresponding gms class can match the " + xClassName;
            gClassIsEmpty++; // ensure the hClassName as the key in h2xMap is unique
        }

        // hName is ""
        if (hClassName.isEmpty()) {
            hClassName = DECLARATION + hClassIsEmpty + ":no corresponding Hms class can match the " + xClassName;
            hClassIsEmpty++; // ensure the hClassName as the key in h2xMap is unique
        }
        x2ghSignatureMap.put(xClassName, new String[] {gClassName, hClassName});
        handleMultiG(gClassName, xClassName);
        handleMultiH(hClassName, xClassName);
    }

    // generate method signature mapping relation between xms, gms and hms
    private void handleMethodSignatureMapping(XClassDoc xClassDoc) {
        for (XMethodDoc xMethodDoc : xClassDoc.getMethods().values()) {
            String xMethodName = xMethodDoc.getXMethodName();
            String gMethodName = xMethodDoc.getGName();
            String hMethodName = xMethodDoc.getHName();

            // gMethodName is ""
            if (gMethodName.isEmpty()) {
                gMethodName = DECLARATION + gMethodIsEmpty + ":no corresponding G method can match" + xMethodName;
                gMethodIsEmpty++; // ensure the gMethodName as the key in g2xMap is unique
            }
            // hMethodName is ""
            if (hMethodName.isEmpty()) {
                hMethodName = DECLARATION + hMethodIsEmpty + ":no corresponding H method can match" + xMethodName;
                hMethodIsEmpty++; // ensure the hMethodName as the key in h2xMap is unique
            }
            x2ghSignatureMap.put(xMethodName, new String[] {gMethodName, hMethodName});
            handleMultiG(gMethodName, xMethodName);
            handleMultiH(hMethodName, xMethodName);
        }
    }

    // generate field signature mapping relation between xms, gms and hms
    private void handleFieldSignatureMapping(XClassDoc xClassDoc) {
        Map<String, XFieldDoc> fieldDocMap = xClassDoc.getFields();
        if (fieldDocMap.isEmpty()) {
            return;
        }

        for (Map.Entry<String, XFieldDoc> entry : fieldDocMap.entrySet()) {
            XFieldDoc xFieldDoc = entry.getValue();
            String xFieldName = entry.getKey();
            String gFieldName = xFieldDoc.getGName();
            String hFieldName = xFieldDoc.getHName();

            x2ghSignatureMap.put(xFieldName, new String[] {gFieldName, hFieldName});
            handleMultiG(gFieldName, xFieldName);
            handleMultiH(hFieldName, xFieldName);
        }
    }

    // multi gms api match one xms api
    private void handleMultiG(String gName, String xName) {
        if (g2xSignatureMapTemp.containsKey(gName)) {
            int index = MULTI_MATCH;
            while (true) {
                SignatureStruct temp = new SignatureStruct(gName, index);
                index++;
                if (g2xSignatureMap.containsKey(temp)) {
                    continue;
                }
                g2xSignatureMap.put(temp, xName);
                break;
            }
        } else {
            g2xSignatureMapTemp.put(gName, xName);
            g2xSignatureMap.put(new SignatureStruct(gName, 0), xName);
        }
    }

    // multi hms api match one xms api
    private void handleMultiH(String hName, String xName) {
        if (h2xSignatureMapTemp.containsKey(hName)) {
            int index = MULTI_MATCH;
            while (true) {
                SignatureStruct temp = new SignatureStruct(hName, index);
                index++;
                if (h2xSignatureMap.containsKey(temp)) {
                    continue;
                }
                h2xSignatureMap.put(temp, xName);
                break;
            }
        } else {
            h2xSignatureMapTemp.put(hName, xName);
            h2xSignatureMap.put(new SignatureStruct(hName, 0), xName);
        }
    }

    // key is signature, value is XDocs including XClassDoc, XMethodDoc, XFieldDoc
    private Map<String, XDocs> getAllXDocsMap() {
        Map<String, XDocs> allXDocsMap = new HashMap<>();
        for (Map.Entry<String, XClassDoc> entryClass : this.xClassDocMap.entrySet()) {
            String className = entryClass.getKey();
            XClassDoc classDoc = entryClass.getValue();
            allXDocsMap.put(className, classDoc);
            Map<String, XMethodDoc> methodDocs = classDoc.getMethods();
            if (!methodDocs.isEmpty()) {
                for (Map.Entry<String, XMethodDoc> entryMethod : methodDocs.entrySet()) {
                    allXDocsMap.put(entryMethod.getKey(), entryMethod.getValue());
                }
            }
            Map<String, XFieldDoc> fieldDocs = classDoc.getFields();
            if (!fieldDocs.isEmpty()) {
                for (Map.Entry<String, XFieldDoc> entryField : fieldDocs.entrySet()) {
                    allXDocsMap.put(entryField.getKey(), entryField.getValue());
                }
            }
        }
        return allXDocsMap;
    }

    // key is signature, value is ZDocs including ZClassDoc, ZMethodDoc, ZFieldDoc
    private Map<String, ZDocs> getAllZDocsMap(boolean isH) {
        Map<String, ZClassDoc> zClassDocMap = isH ? this.hClassDocMap : this.gClassDocMap;
        Map<String, ZDocs> allZDocsMap = new HashMap<>();
        for (Map.Entry<String, ZClassDoc> entryClass : zClassDocMap.entrySet()) {
            String className = entryClass.getKey();
            ZClassDoc classDoc = entryClass.getValue();
            allZDocsMap.put(className, classDoc);
            Map<String, ZMethodDoc> methodDocs = classDoc.getMethods();
            if (!methodDocs.isEmpty()) {
                for (Map.Entry<String, ZMethodDoc> entryMethod : methodDocs.entrySet()) {
                    allZDocsMap.put(entryMethod.getKey(), entryMethod.getValue());
                }
            }
            Map<String, ZFieldDoc> fieldDocs = classDoc.getFields();
            if (!fieldDocs.isEmpty()) {
                for (Map.Entry<String, ZFieldDoc> entryField : fieldDocs.entrySet()) {
                    allZDocsMap.put(entryField.getKey(), entryField.getValue());
                }
            }
        }
        return allZDocsMap;
    }

    // get all xms signatures including class, method, field from x2ghSignatureMap
    private Set<String> getXSignaturesFromX2Z(String requestText) {
        Set<String> allXSignatures = new HashSet<>();
        for (String xSignature : this.x2ghSignatureMap.keySet()) {
            if (xSignature.contains(requestText)) {
                allXSignatures.add(xSignature);
            }
        }
        return allXSignatures;
    }

    // get all xms signatures including class, method, field from h2xSignatureMap and g2xSignatureMap.
    private Set<String> getXSignaturesFromZ2X(String requestText, boolean isH) {
        Set<String> allXSignatures = new HashSet<>();
        Map<SignatureStruct, String> z2xSignatureMap = isH ? this.h2xSignatureMap : this.g2xSignatureMap;
        for (Map.Entry<SignatureStruct, String> entry : z2xSignatureMap.entrySet()) {
            if (entry.getKey().getSignatureName().contains(requestText)) {
                allXSignatures.add(entry.getValue());
            }
        }
        return allXSignatures;
    }

    /**
     * get all xms signatures, including class, method, field, filtered by request text.
     * @param requestText the request text, may be a fragment text of signature, also may be a full one
     * @return list contains all full signature containing the request text
     */
    public Set<String> getAllXSignaturesFiltered(String requestText) {
        Set<String> allXSignatures = new HashSet<>();
        boolean isX = DocUtil.isXType(requestText);
        boolean isH = DocUtil.isHType(requestText);
        boolean isG = DocUtil.isGType(requestText);

        if (isX) {
            allXSignatures = this.getXSignaturesFromX2Z(requestText);
        } else if (isH) {
            allXSignatures = this.getXSignaturesFromZ2X(requestText, true);
        } else if (isG) {
            allXSignatures = this.getXSignaturesFromZ2X(requestText, false);
        } else {
            allXSignatures.addAll(this.getXSignaturesFromX2Z(requestText));
            allXSignatures.addAll(this.getXSignaturesFromZ2X(requestText, true));
            allXSignatures.addAll(this.getXSignaturesFromZ2X(requestText, false));
        }
        return allXSignatures;
    }

    /**
     * get all gms or hms signatures, including class, method, field, filtered by request text.
     * @param requestText the request text, may be a fragment text of signature, also may be a full one
     * @param isH get hms signatures if true, gms else.
     * @return list contains all full signature containing the request text
     */
    private Set<String> getAllZSignaturesFiltered(String requestText, boolean isH) {
        Set<String> allXSignatures = getAllXSignaturesFiltered(requestText);
        Set<String> allGSignatures = new HashSet<>();
        Set<String> allHSignatures = new HashSet<>();

        Iterator<String> iterator = allXSignatures.iterator();
        while (iterator.hasNext()) {
            for (String[] ghSignatures : getSignaturesArray(iterator.next())) {
                if (isH) {
                    if (!ghSignatures[2].startsWith(DECLARATION)) {
                        allHSignatures.add(ghSignatures[2]);
                    }
                } else {
                    if (!ghSignatures[1].startsWith(DECLARATION)) {
                        allGSignatures.add(ghSignatures[1]);
                    }
                }
            }
        }
        return isH ? allHSignatures : allGSignatures;
    }

    // judge input source: X or G or H and find all signature
    public EntireDoc getEntireDocByRequestType(String requestSignature) {
        EntireDoc entireDoc = new EntireDoc();
        switch (DocUtil.getRequestType(requestSignature)) {
            case X:
                entireDoc.setStrategyType(EntireDoc.STRATEGYTYPE.X);
                break;
            case G:
                entireDoc.setStrategyType(EntireDoc.STRATEGYTYPE.G);
                break;
            case H:
                entireDoc.setStrategyType(EntireDoc.STRATEGYTYPE.H);
                break;
            default:
                entireDoc.setStrategyType(EntireDoc.STRATEGYTYPE.OTHER);
        }
        return entireDoc;
    }

    // according to the requestSignature, search the maps and get the corresponding three signature
    public List<String[]> getSignaturesArray(String requestSignature) {
        String xSignature = CANNOT_FIND + "x signature for " + requestSignature;
        String gSignature = CANNOT_FIND + "g signature for " + requestSignature;
        String hSignature = CANNOT_FIND + "h signature for " + requestSignature;
        String[] defaultSignatures = new String[]{xSignature, gSignature, hSignature};

        switch (DocUtil.getRequestType(requestSignature)) {
            case X:
                return getSignaturesArrayInCaseX(requestSignature, defaultSignatures);
            case G:
                return getSignaturesArrayInCaseG(requestSignature, defaultSignatures);
            case H:
                return getSignaturesArrayInCaseH(requestSignature, defaultSignatures);
            // default case is not need because OTHER will never reach here.
        }
        throw new UnExpectedProcessException();
    }

    /**
     * get Docs array that elements are xDocs, gDocs, hDocs.
     * @param entireSignatures the request signatures array that elements are signatures of xms, gms, hms
     * @param scopeType three types including class, method, field
     * @return target docs according to request including xDocs, gDocs, hDocs
     */
    public Docs[] getDocsArray(String[] entireSignatures, EntireDoc.SCOPETYPE scopeType) {
        String xSignature = entireSignatures[0];
        String gSignature = entireSignatures[1];
        String hSignature = entireSignatures[2];

        XDocs xDocs = null;
        ZDocs gDocs = null;
        ZDocs hDocs = null;
        // judge input Type:Class or Method or Field and find all docs
        switch (scopeType) {
            case CLASS:
                xDocs = this.xClassDocMap.get(xSignature);
                gDocs = this.gClassDocMap.get(gSignature);
                hDocs = this.hClassDocMap.get(hSignature);
                break;
            case FIELD:
            case METHOD:
                xDocs = getXMethodOrFieldDocs(xSignature);
                gDocs = getZMethodOrFieldDocs(gSignature, false);
                hDocs = getZMethodOrFieldDocs(hSignature, true);
                break;
            default:
                break;
        }
        Docs[] docsArray = {xDocs, gDocs, hDocs};
        return docsArray;
    }

    /**
     * get z, namely g and h, methodDocs or fieldDocs.
     * @param zSignature the signature may be g or h
     * @param isH hms if true
     * @return target ZDocs according to request signature
     */
    private ZDocs getZMethodOrFieldDocs(String zSignature, boolean isH) {
        boolean isMethod = MethodDocUtil.isMethod(zSignature);
        // get the classDoc which contains target method or field
        String className = DocUtil.getParentName(zSignature);

        ZClassDoc zClassDoc = isH ? this.hClassDocMap.get(className) : this.gClassDocMap.get(className);

        if (zClassDoc == null) {
            LOGGER.error("can not find zClassDoc for " + className);
            return null;
        }

        if (isMethod) {
            Map<String, ZMethodDoc> zMethodDocMap = zClassDoc.getMethods();
            return zMethodDocMap.isEmpty() ? null : zMethodDocMap.get(zSignature);
        } else {
            Map<String, ZFieldDoc> zFieldDocMap = zClassDoc.getFields();
            return zFieldDocMap.isEmpty() ? null : zFieldDocMap.get(zSignature);
        }
    }

    // find the methodDoc or fieldDoc in methodsList or fieldsList
    private XDocs getXMethodOrFieldDocs(String xSignature) {
        boolean isMethod = MethodDocUtil.isMethod(xSignature);
        String className = DocUtil.getParentName(xSignature);
        XClassDoc xClassDocs = this.xClassDocMap.get(className);
        if (xClassDocs == null) {
            LOGGER.error("can not find xClassDoc for " + className);
            return null;
        }

        if (isMethod) {
            Map<String, XMethodDoc> xMethodDocMap = xClassDocs.getMethods();
            return xMethodDocMap.isEmpty() ? null : xMethodDocMap.get(xSignature);
        } else {
            Map<String, XFieldDoc> xFieldDocMap = xClassDocs.getFields();
            return xFieldDocMap.isEmpty() ? null : xFieldDocMap.get(xSignature);
        }
    }

    private List<String[]> getSignaturesArrayInCaseX(String xSignature, String[] defaultSignatures) {
        String gSignature = defaultSignatures[0];
        String hSignature = defaultSignatures[1];
        String exception = null;
        String[] ghSignature = this.x2ghSignatureMap.get(xSignature);
        if (ghSignature != null && ghSignature.length > 1) {
            gSignature = ghSignature[0];
            hSignature = ghSignature[1];
        } else {
            exception = ERROR_IN_JSON;
            LOGGER.error("can not find ghSignature for " + xSignature);
        }
        return Collections.singletonList(new String[]{xSignature, gSignature, hSignature, exception});
    }

    private List<String[]> getSignaturesArrayInCaseG(String gSignature, String[] defaultSignatures) {
        String xSignature = defaultSignatures[0];
        String hSignature = defaultSignatures[1];
        String exception = null;
        List<String[]> signaturesArraysList = new ArrayList<>();
        int index = 0;
        while (true) {
            SignatureStruct gSignatureStruct = new SignatureStruct(gSignature, index);
            index ++;
            String tempX = this.g2xSignatureMap.get(gSignatureStruct);
            if (tempX == null) {
                if (index == 1) {
                    exception = ERROR_IN_JSON;
                    LOGGER.error("can not find xSignature for " + gSignature);
                    return Collections.singletonList(new String[]{xSignature, gSignature, hSignature, exception});
                }
                return signaturesArraysList;
            }

            xSignature = tempX;
            String[] ghArray = this.x2ghSignatureMap.get(xSignature);
            if (ghArray != null && ghArray.length > 1) {
                hSignature = ghArray[1];
            } else {
                exception = ERROR_IN_JSON;
                LOGGER.error("can not find ghSignature for " + xSignature);
            }
            signaturesArraysList.add(new String[]{xSignature, gSignature, hSignature, exception});
        }
    }

    private List<String[]> getSignaturesArrayInCaseH(String hSignature, String[] defaultSignatures) {
        String xSignature = defaultSignatures[0];
        String gSignature = defaultSignatures[1];
        String exception = null;
        List<String[]> signaturesArraysList = new ArrayList<>();
        int index = 0;
        while (true) {
            SignatureStruct hSignatureStruct = new SignatureStruct(hSignature, index);
            index ++;
            String tempX = this.h2xSignatureMap.get(hSignatureStruct);
            if (tempX == null) {
                if (index == 1) {
                    exception = ERROR_IN_JSON;
                    LOGGER.error("can not find xSignature for " + hSignature);
                    return Collections.singletonList(new String[]{xSignature, gSignature, hSignature, exception});
                }
                return signaturesArraysList;
            }
            xSignature = tempX;
            String[] ghArray = this.x2ghSignatureMap.get(xSignature);
            if (ghArray != null && ghArray.length > 1) {
                if (ghArray[0] != null) {
                    gSignature = ghArray[0];
                }
            } else {
                exception = ERROR_IN_JSON;
                LOGGER.error("can not find ghSignature for " + xSignature);
            }
            signaturesArraysList.add(new String[]{xSignature, gSignature, hSignature, exception});
        }
    }
}