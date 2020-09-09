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

import static com.huawei.inquiry.utils.DocUtil.DECLARATION;

import com.huawei.inquiry.docs.Docs;
import com.huawei.inquiry.docs.EntireDoc;
import com.huawei.inquiry.docs.XDocs;
import com.huawei.inquiry.docs.ZDocs;
import com.huawei.inquiry.utils.DocUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * this class is used for displaying javadoc including x,h,g class/method/field
 * 1. return a entireDoc including x/h/g docs according to a input signature message
 * 2. return a linkedHashMap including x/h/g docs according to a input signature in input textField.
 *
 * @since 2020-07-25
 */
public class InquiryEntrances {
    private static final String SPECIAL_REX =
        "[`~!@#$%^&*+=|{}':;'/~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]|\n|\r|\t";

    private static final String DIGIT_REX = ".*\\d+.*";

    private static final int X_INDEX = 0; // index of XDocs in the docsArray

    private static final int G_INDEX = 1; // index of GDocs in the docsArray

    private static final int H_INDEX = 2; // index of HDocs in the docsArray

    private static final int EXCEPTION_INDEX = 3; // index of exception in the docsArray

    private static final String LEGAL_DIGIT = "org.xms.g.wallet.wobs.WalletObjectsConstants.State.convertGState2H(int)";

    private DocMapsCreator creator;

    private Set<XDocs> xDocsSet = new HashSet<>();

    private Set<ZDocs> gDocsSet = new HashSet<>();

    private Set<ZDocs> hDocsSet = new HashSet<>();

    private InquiryEntrances(String pluginPath, Map<String, String> gmsVersionMap) {
        creator = new DocMapsCreator(pluginPath, gmsVersionMap);
    }

    public static InquiryEntrances getInstance(String pluginPath, Map<String, String> gmsVersionMap) {
        return new InquiryEntrances(pluginPath, gmsVersionMap);
    }

    /**
     * this method is called when users clicked "search" button in IDE
     *
     * @param requestText the request text info, it may be just a text fragment of the name of class or field or method.
     * @return return object is a map containing all class, method, field that their signature,namely the key of map,
     *         contains the input fragment, sorted by the order:
     *         1. x: class, method, field
     *         2. h: class, method, field
     *         3. g: class, method, field
     */
    public Map<String, Docs> search(String requestText) {
        if (!isValidated(requestText)) {
            return Collections.EMPTY_MAP;
        }
        Map<String, Docs> resultMap = new LinkedHashMap<>();
        String requestTextTrim = requestText.trim();
        resultMap.putAll(creator.searchX(requestTextTrim));
        resultMap.putAll(creator.searchH(requestTextTrim));
        resultMap.putAll(creator.searchG(requestTextTrim));
        return resultMap;
    }

    /**
     * this method is called when users clicked "show details" menu in IDE
     * @param requestSignature the request message is a signature including class's, method's, and field's
     * @param scopeType namely CLASS, or METHOD, or FIELD.
     * @return entireDoc include all infos. If input scopeType is a class/method/field,then output entireDoc
     *         is a class/method/field also.
     */
    public EntireDoc getDocs(String requestSignature, EntireDoc.SCOPETYPE scopeType) {
        // input:
        // (1) G Class/Method/Field
        // (2) X Class/Method/Field
        // (3) H Class/Method/Field
        // (4) another such as "java.util.List.toString()"
        if (requestSignature == null) {
            return getEntireDocForNull();
        }
        String requestSignatureTrim = requestSignature.trim();
        EntireDoc entireDoc = creator.getEntireDocByRequestType(requestSignatureTrim);
        if (entireDoc.getStrategyType() == EntireDoc.STRATEGYTYPE.OTHER) {
            return entireDoc;
        }
        if (!DocUtil.isKitsScope(requestSignatureTrim)) {
            entireDoc.setExceptiontype(EntireDoc.EXCEPTIONTYPE.KITNOTSUPPORT);
            return entireDoc;
        }
        return getEntireDoc(requestSignatureTrim, entireDoc, scopeType);
    }

    private EntireDoc getEntireDocForNull() {
        EntireDoc entireDoc = new EntireDoc();
        entireDoc.setStrategyType(EntireDoc.STRATEGYTYPE.OTHER);
        entireDoc.setExceptiontype(EntireDoc.EXCEPTIONTYPE.NOTFOUND);
        return entireDoc;
    }

    private EntireDoc getEntireDoc(String requestSignature, EntireDoc entireDoc, EntireDoc.SCOPETYPE scopeType) {
        List<String[]> signaturesArraysList = creator.getSignaturesArray(requestSignature);
        clearSets();
        for (String[] signaturesArray : signaturesArraysList) {
            Docs[] docsArray = creator.getDocsArray(signaturesArray, scopeType);
            if (signaturesArray[EXCEPTION_INDEX] != null) { // there are errors in jsons
                if (DocUtil.getRequestType(requestSignature) == EntireDoc.STRATEGYTYPE.H) {
                    // this request signature H not match any G api
                    entireDoc.setExceptiontype(EntireDoc.EXCEPTIONTYPE.HMSNOTMATCHGMS);
                    if (docsArray[H_INDEX] instanceof ZDocs) {
                        ZDocs zDocs = (ZDocs) docsArray[H_INDEX];
                        hDocsSet.add(zDocs);
                        entireDoc.setHDocs(hDocsSet);
                    }
                } else {
                    // there are errors in jsons
                    entireDoc.setExceptiontype(EntireDoc.EXCEPTIONTYPE.NOTFOUND);
                }
                return entireDoc;
            }
            if (signaturesArray[G_INDEX].startsWith(DECLARATION)) { // g name is empty
                entireDoc.setExceptiontype(EntireDoc.EXCEPTIONTYPE.GMSNOTSUPPORT);
                if (docsArray[X_INDEX] instanceof XDocs) {
                    XDocs xDocs = (XDocs)docsArray[X_INDEX];
                    Set<XDocs> xDocsSet1 = new HashSet<>();
                    xDocsSet1.add(xDocs);
                    entireDoc.setXDocs(xDocsSet1);
                }
                return entireDoc;
            }
            if (signaturesArray[H_INDEX].startsWith(DECLARATION)) { // h name is empty
                entireDoc.setExceptiontype(EntireDoc.EXCEPTIONTYPE.HMSNOTSUPPORT);
            }
            if (entireDoc.getExceptiontype() == null
                    && (docsArray[X_INDEX] == null || docsArray[G_INDEX] == null || docsArray[H_INDEX] == null)) {
                // there are errors in jsons
                entireDoc.setExceptiontype(EntireDoc.EXCEPTIONTYPE.NOTFOUND);
            }
            addSets(docsArray);
        }
        entireDoc.setXDocs(xDocsSet);
        entireDoc.setGDocs(gDocsSet);
        entireDoc.setHDocs(hDocsSet);
        return entireDoc;
    }

    private void addSets(Docs[] docsArray) {
        if (docsArray[X_INDEX] instanceof XDocs) {
            XDocs xDocs = (XDocs)docsArray[X_INDEX];
            xDocsSet.add(xDocs);
        }
        if (docsArray[G_INDEX] instanceof ZDocs) {
            ZDocs gDocs = (ZDocs)docsArray[G_INDEX];
            gDocsSet.add(gDocs);
        }
        if (docsArray[H_INDEX] instanceof ZDocs) {
            ZDocs hDocs = (ZDocs)docsArray[H_INDEX];
            hDocsSet.add(hDocs);
        }
    }

    private void clearSets() {
        xDocsSet.clear();
        gDocsSet.clear();
        hDocsSet.clear();
    }

    private boolean isValidated(String requestStr) {
        if (requestStr == null || requestStr.equals("")) {
            return false;
        }
        if (requestStr.contains(" ") && !requestStr.contains("<? extends ") && !requestStr.contains("<? super ")) {
            return false;
        }

        Matcher digitMatcher = Pattern.compile(DIGIT_REX).matcher(requestStr);
        boolean containsDigit = digitMatcher.matches(); // request contains number

        Matcher specialMatcher = Pattern.compile(SPECIAL_REX).matcher(requestStr);
        boolean containsSpecialChar = specialMatcher.find();

        return requestStr.equals(LEGAL_DIGIT) || (!containsDigit && !containsSpecialChar);
    }
}