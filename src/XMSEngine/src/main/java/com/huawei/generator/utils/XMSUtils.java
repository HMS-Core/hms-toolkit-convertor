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

package com.huawei.generator.utils;

import static com.huawei.generator.gen.AstConstants.INNER_CLASS_NAME;

import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.gen.AstConstants;
import com.huawei.generator.json.JField;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utils for XMS
 *
 * @since 2019-11-12
 */

public class XMSUtils {
    public static GlobalMapping getXGlobalMapping(String s) {
        return GlobalMapping.getXmappings().get(s);
    }

    public static GlobalMapping getHGlobalMapping(String s) {
        return GlobalMapping.getHmappings().get(s);
    }

    public static String htoX(String s) {
        GlobalMapping hGlobalMapping = getHGlobalMapping(s);
        if (hGlobalMapping == null) {
            return s;
        }
        return hGlobalMapping.getX();
    }

    public static String htoG(String s) {
        GlobalMapping hGlobalMapping = getHGlobalMapping(s);
        if (hGlobalMapping == null) {
            return s;
        }
        return hGlobalMapping.getG();
    }

    public static String xtoH(String s) {
        GlobalMapping xGlobalMapping = getXGlobalMapping(s);
        if (xGlobalMapping == null) {
            return s;
        }
        return xGlobalMapping.getH();
    }

    public static String xtoG(String s) {
        GlobalMapping xGlobalMapping = getXGlobalMapping(s);
        if (xGlobalMapping == null) {
            return s;
        }
        return TypeNode.create(xGlobalMapping.getG()).getTypeName();
    }

    public static String gtoH(String s) {
        GlobalMapping xGlobalMapping = getXGlobalMapping(gtoX(s));
        if (xGlobalMapping == null) {
            return s;
        }
        return xGlobalMapping.getH();
    }

    public static String gtoX(String s) {
        return TypeNode.create(s).toX().toString();
    }

    public static <T, U> List<T> listMap(List<U> list, Function<? super U, ? extends T> mapper) {
        return list.stream().map(mapper).collect(Collectors.toList());
    }

    public static String outerClassOf(String name) {
        if (name == null) {
            return "";
        }
        return name.substring(0, name.lastIndexOf("."));
    }

    public static String degenerify(String name) {
        if (name == null) {
            return "";
        }
        return name.contains("<") ? name.substring(0, name.indexOf("<")) : name;
    }

    public static String deArray(String name) {
        if (name == null) {
            return "";
        }
        return name.contains("[") ? name.substring(0, name.indexOf("[")) : name;
    }

    public static String capitialize(String s) {
        if (s == null || s.isEmpty()) {
            return "";
        }
        return s.substring(0, 1).toUpperCase(Locale.forLanguageTag(s.substring(0, 1))) + s.substring(1);
    }

    /**
     * check one type name whether starts with "org.xms"
     *
     * @param typeName to be checked
     * @return true if the type including "org.xms"
     */
    public static boolean isX(String typeName) {
        return typeName != null && typeName.contains("org.xms");
    }

    public static String removePackage(String name) {
        if (name == null) {
            return "";
        }
        return name.contains(".") ? name.substring(name.lastIndexOf(".") + 1) : name;
    }

    public static String getImplConstructor(String xmsI) {
        return xmsI + "." + INNER_CLASS_NAME;
    }

    public static List<TypeNode> createXBoxTypeParam() {
        return Collections.singletonList(TypeNode.create(AstConstants.XMS_BOX));
    }

    public static boolean isCreatorField(JField jField) {
        TypeNode fieldType = TypeNode.create(jField.type());
        return jField.name().equals(AstConstants.CREATOR) && fieldType.getTypeName().equals(AstConstants.CREATOR_TYPE);
    }
}
