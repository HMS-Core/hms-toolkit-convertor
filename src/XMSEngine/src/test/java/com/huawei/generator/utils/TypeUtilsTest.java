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

import com.huawei.generator.ast.TypeNode;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;

/**
 * test of {@link TypeUtils}
 *
 * @since 2020-01-10
 */
public class TypeUtilsTest {
    @Rule
    public ExpectedException eRule = ExpectedException.none();

    @Test
    public void testPrimitiveTypes() {
        // we have 8 primitive types
        Assert.assertEquals(TypeUtils.getPrimitiveTypes().size(), 8);
        Assert.assertTrue(TypeUtils.getPrimitiveTypes().containsAll(Arrays.asList("boolean", "byte", "char")));
    }

    @Test
    public void testTypeChecker() {
        // isGmsType
        String gms = "com.google.android.gms.common.api.Api.ApiOptions.HasGoogleSignInAccountOptions";
        Assert.assertTrue(TypeUtils.isGmsType(gms));
        gms = "com.android.installreferrer.api.InstallReferrerClient.Builder";
        Assert.assertTrue(TypeUtils.isGmsType(gms));

        String hms = "com.huawei.hms.support.hwid.HuaweiIdAuthAPIManager";
        Assert.assertFalse(TypeUtils.isGmsType(hms));

        // isHmsType
        Assert.assertTrue(TypeUtils.isHmsType(hms));
        hms = "com.huawei.hmf.tasks.CancellationToken";
        Assert.assertTrue(TypeUtils.isHmsType(hms));
        Assert.assertFalse(TypeUtils.isGmsType(hms));

        // isAndroidType
        String android = "android.content.BroadcastReceiver";
        Assert.assertTrue(TypeUtils.isAndroidType(android));
        gms = "com.google.android.gms.common.api.Api.ApiOptions.HasGoogleSignInAccountOptions";
        Assert.assertFalse(TypeUtils.isAndroidType(gms));

        // isGMSInterface
        gms = "com.google.android.gms.common.api.Api.ApiOptions";
        Assert.assertTrue(TypeUtils.isGmsInterface(gms));
        Assert.assertFalse(TypeUtils.isGmsInterface(hms));

        // isHMSInterface
        hms = "com.huawei.hms.api.Api.ApiOptions.Optional";
        Assert.assertTrue(TypeUtils.isHmsInterface(hms));
        Assert.assertFalse(TypeUtils.isHmsInterface(gms));

        // isInterface
        String str = "com.google.android.gms.common.api.Api.ApiOptions";
        Assert.assertTrue(TypeUtils.isInterface(str));
        str = "com.huawei.hms.api.Api.ApiOptions.Optional";
        Assert.assertTrue(TypeUtils.isInterface(str));
        str = "java.nio.channels.Channel";
        Assert.assertTrue(TypeUtils.isInterface(str));
        str = "com.google.android.gms.common.images.Size";
        Assert.assertFalse(TypeUtils.isInterface(str));

        // isGmsAbstract
        gms = "com.google.android.gms.common.api.PendingResult";
        Assert.assertTrue(TypeUtils.isGmsAbstract(gms));
        gms = "com.google.android.gms.common.api.Api.ApiOptions";
        Assert.assertFalse(TypeUtils.isGmsAbstract(gms));
        Assert.assertFalse(TypeUtils.isGmsAbstract(hms));

        // isHmsAbstract
        hms = "com.huawei.hms.support.api.client.PendingResult";
        Assert.assertTrue(TypeUtils.isHmsAbstract(hms));
        Assert.assertFalse(TypeUtils.isGmsAbstract(gms));

        // isGmsNormalClass, isHmsNormalClass
        gms = "com.google.android.gms.common.GooglePlayServicesUtil";
        Assert.assertTrue(TypeUtils.isGmsNormalClass(gms));
        hms = "com.huawei.hms.utils.PackageManagerHelper";
        Assert.assertTrue(TypeUtils.isHmsNormalClass(hms));
        Assert.assertFalse(TypeUtils.isGmsNormalClass(hms));
        Assert.assertFalse(TypeUtils.isHmsNormalClass(gms));

        // isPrimitiveType
        Assert.assertTrue(TypeUtils.isPrimitiveType("short"));
        Assert.assertFalse(TypeUtils.isPrimitiveType("s"));

        // isVoidType
        Assert.assertTrue(TypeUtils.isVoidType("void"));
        Assert.assertFalse(TypeUtils.isVoidType("v"));

        // isPrimitiveTypeArray
        Assert.assertTrue(TypeUtils.isPrimitiveTypeArray("short[]"));
        Assert.assertFalse(TypeUtils.isPrimitiveTypeArray("short"));

        // isBooleanType
        Assert.assertTrue(TypeUtils.isBooleanType("boolean"));
        Assert.assertFalse(TypeUtils.isBooleanType("b"));

        // isArray
        Assert.assertTrue(TypeNode.create("boolean[]").isArray());
        Assert.assertFalse(TypeNode.create("boolean").isArray());
    }

    @Test
    public void testInterfaceException() {
        eRule.expect(IllegalStateException.class);
        TypeUtils.isInterface("com.guzuxing.test.Test");
    }

    @Test
    public void isIterable() {
        TypeNode typeNode = TypeNode.create("java.lang.Iterable<XT>");
        Assert.assertTrue(TypeUtils.isIterable(typeNode));
        typeNode = TypeNode.create("java.lang.Iterable<com.huawei.hms.data.DataBuffer>");
        Assert.assertTrue(TypeUtils.isIterable(typeNode));
    }

    @Test
    public void isGenericIdentifier() {
        Assert.assertTrue(TypeUtils.isGenericIdentifier(TypeNode.create("T")));
        Assert.assertTrue(TypeUtils.isGenericIdentifier(TypeNode.create("XE")));
        Assert.assertTrue(TypeUtils.isGenericIdentifier(TypeNode.create("TResult")));
    }
}
