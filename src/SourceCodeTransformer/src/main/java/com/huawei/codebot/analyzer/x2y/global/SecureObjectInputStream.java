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

package com.huawei.codebot.analyzer.x2y.global;

import com.huawei.codebot.analyzer.x2y.global.bean.ClassInfo;
import com.huawei.codebot.analyzer.x2y.global.bean.FieldInfo;
import com.huawei.codebot.analyzer.x2y.global.bean.MethodInfo;
import com.huawei.codebot.analyzer.x2y.global.bean.TypeInfo;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;
import java.util.ArrayList;
import java.util.HashMap;

final class SecureObjectInputStream extends ObjectInputStream {
    SecureObjectInputStream(InputStream in) throws IOException {
        super(in);
    }

    @Override
    protected Class<?> resolveClass(ObjectStreamClass desc)
            throws IOException, ClassNotFoundException {
        if (!desc.getName().equals(HashMap.class.getCanonicalName())
                && !desc.getName().equals(ArrayList.class.getCanonicalName())
                && !desc.getName().equals(ClassInfo.class.getCanonicalName())
                && !desc.getName().equals(FieldInfo.class.getCanonicalName())
                && !desc.getName().equals(MethodInfo.class.getCanonicalName())
                && !desc.getName().equals(TypeInfo.class.getCanonicalName())) {  // white list check
            throw new ClassNotFoundException(desc.getName() + " not find");
        }
        return super.resolveClass(desc);
    }

}
