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

package com.huawei.generator.g2x.po.map;

import com.huawei.generator.utils.G2XMappingUtils;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * test of G2XMappingUtils
 *
 * @since 2020-01-09
 */
public class G2XMappingUtilsTest {
    @Rule
    public ExpectedException eRule = ExpectedException.none();

    @Test
    public void testNormalizeKitName() {
        String[] sources = new String[] {
            "drm", "maps", "mlfirebase", "mlgms", "safety", "framework", "account", "ml"};
        String[] targets = new String[] {
            "DRM", "Map", "MLfirebase", "MLgms", "Safetynet", "Basement", "Account", "ML"};
        for (int index = 0; index < sources.length; index++) {
            Assert.assertEquals(targets[index], G2XMappingUtils.normalizeKitName(sources[index]));
        }
    }

    @Test
    public void testUnNormalizeKitName() {
        String[] targets = new String[] {"drm", "maps", "mlfirebase", "mlgms", "safety", "framework", "account", "ml"};
        String[] sources = new String[] {"DRM", "Map", "MLfirebase", "MLgms", "Safetynet", "Basement", "Account", "ML"};
        for (int index = 0; index < sources.length; index++) {
            Assert.assertEquals(targets[index], G2XMappingUtils.unNormalizeKitName(sources[index]));
        }
    }

    @Test
    public void testMethodNameBuilder() {
        String org = "com.google.api.services.drive.Drive.Files.Watch "
            + "com.google.api.services.drive.Drive.Files.Watch.set(" + "java.lang.String,java.lang.Object)";
        String result = G2XMappingUtils.buildMDescMethodName(org, 2);
        Assert.assertTrue(result.equals("com.google.api.services.drive.Drive.Files.Watch "
            + "com.google.api.services.drive.Drive.Files.Watch.set(param0,param1)"));

        org = "com.google.api.services.drive.Drive.Files.Watch.set()";
        Assert.assertEquals(org, G2XMappingUtils.buildMDescMethodName(org, 0));
    }

    @Test
    public void testEraseMDesc() {
        MDesc desc = new MDesc();
        desc.setText("\"com.google.android.gms.measurement.AppMeasurementReceiver."
            + "onReceive(android.content.Context,android.content.Intent)\" "
            + "will be replaced by \"com.huawei.hms.analytics.HiAnalyticsReceiver."
            + "onReceive(android.content.Context,android.content.Intent)\"");
        desc.setMethodName("void com.google.android.gms.measurement."
            + "AppMeasurementReceiver.onReceive(android.content.Context,android.content.Intent)");
        G2XMappingUtils.eraseMDesc(desc, 2);
        Assert.assertEquals(desc.getMethodName(),
            "void com.google.android.gms.measurement.AppMeasurementReceiver.onReceive(param0,param1)");

        MDesc d1 = new MDesc();
        d1.setText("\"com.google.android.gms.measurement.AppMeasurementReceiver"
            + ".onReceive(android.content.Context,android.content.Intent)\" "
            + "will be replaced by \"com.huawei.hms.analytics.HiAnalyticsReceiver"
            + ".onReceive(android.content.Context,android.content.Intent)\"");
        d1.setMethodName("void com.google.android.gms.measurement.AppMeasurementReceiver"
            + ".onReceive(android.content.Context,android.content.Intent)");
        G2XMappingUtils.eraseMDesc(d1, 2,
            "com.google.android.gms.measurement.AppMeasurementReceiver"
                + ".onReceive(android.content.Context,android.content.Intent)",
            "com.huawei.hms.analytics.HiAnalyticsReceiver"
                + ".onReceive(android.content.Context,android.content.Intent)");
        Assert.assertEquals(d1.getMethodName(),
            "void com.google.android.gms.measurement.AppMeasurementReceiver.onReceive(param0,param1)");
        String result = "\"com.google.android.gms.measurement.AppMeasurementReceiver.onReceive(param0,param1)\""
            + " will be replaced by " + "\"com.huawei.hms.analytics.HiAnalyticsReceiver.onReceive(param0,param1)\"";
        Assert.assertTrue(d1.getText().equals(result));
    }

    @Test
    public void testSimplifySignature() {
        String str = "ret name(p1,p2)";
        Assert.assertEquals(G2XMappingUtils.simplifySignature(str), "name(p1,p2)");
        str = "name(p1)";
        Assert.assertEquals(G2XMappingUtils.simplifySignature(str), "name(p1)");
        str = "ret name(r1, r2)";
        eRule.expect(IllegalArgumentException.class);
        G2XMappingUtils.simplifySignature(str);
    }
}
