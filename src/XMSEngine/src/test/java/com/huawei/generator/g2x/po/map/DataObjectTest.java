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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.huawei.generator.g2x.po.map.auto.Auto;
import com.huawei.generator.g2x.po.map.auto.AutoClass;
import com.huawei.generator.g2x.po.map.auto.AutoField;
import com.huawei.generator.g2x.po.map.auto.AutoMethod;
import com.huawei.generator.g2x.po.map.extension.ExPackage;
import com.huawei.generator.g2x.po.map.extension.G2XExtension;
import com.huawei.generator.g2x.po.map.manual.Manual;
import com.huawei.generator.g2x.po.map.manual.ManualClass;
import com.huawei.generator.g2x.po.map.manual.ManualField;
import com.huawei.generator.g2x.po.map.manual.ManualMethod;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * test data structures of g2x mapping
 *
 * @since 2020-01-07
 */
public class DataObjectTest {
    @Test
    public void testDesc() {
        Desc desc = buildDesc();
        // constructor
        Desc d = new Desc();
        d.setText(desc.getText());
        d.setUrl(desc.getUrl());
        d.setKit(desc.getKit());
        d.setDependencyName(desc.getDependencyName());
        d.setGmsVersion(desc.getGmsVersion());
        d.setHmsVersion(desc.getHmsVersion());
        d.setStatus(desc.getStatus());
        d.setSupport(desc.isSupport());
        Assert.assertTrue(sameDesc(desc, d));
        // write and build from json
        Gson gson = new GsonBuilder().create();
        String strObject = gson.toJson(desc);
        Desc desc1 = gson.fromJson(strObject, Desc.class);
        Assert.assertTrue(sameDesc(desc, desc1));
        desc1.setSupport(!desc1.isSupport());
        Assert.assertFalse(sameDesc(desc, desc1));
    }

    @Test
    public void testMDest() {
        MDesc mDesc = buildMDesc();
        MDesc d = new MDesc();
        d.setText(mDesc.getText());
        d.setUrl(mDesc.getUrl());
        d.setKit(mDesc.getKit());
        d.setDependencyName(mDesc.getDependencyName());
        d.setGmsVersion(mDesc.getGmsVersion());
        d.setHmsVersion(mDesc.getHmsVersion());
        d.setStatus(mDesc.getStatus());
        d.setSupport(mDesc.isSupport());
        d.setMethodName(mDesc.getMethodName());
        Assert.assertTrue(sameDesc(mDesc, d));
        // write and build from json
        Gson gson = new GsonBuilder().create();
        String strObject = gson.toJson(mDesc);
        MDesc desc1 = gson.fromJson(strObject, MDesc.class);
        Assert.assertTrue(sameDesc(mDesc, desc1));
        desc1.setSupport(!desc1.isSupport());
        Assert.assertFalse(sameDesc(mDesc, desc1));
    }

    @Test
    public void testExDest() {
        ExDesc mDesc = buildExDesc();
        // write and build from json
        Gson gson = new GsonBuilder().create();
        String strObject = gson.toJson(mDesc);
        ExDesc desc1 = gson.fromJson(strObject, ExDesc.class);
        Assert.assertTrue(sameDesc(mDesc, desc1));
        desc1.setSupport(!desc1.isSupport());
        Assert.assertFalse(sameDesc(mDesc, desc1));

        ExDesc desc2 = new ExDesc();
        desc2.setText("t");
        desc2.setUrl("u");
        desc2.setKit("k");
        desc2.setStatus("false");
        desc2.setSupport(false);
        Assert.assertFalse(sameDesc(mDesc, desc2));
    }

    @Test
    public void testAutoField() {
        // preparing
        Desc desc = new Desc();
        String oldName = "oldName";
        String newName = "newName";

        // build with two constructors
        AutoField autoField = new AutoField();
        autoField.setDesc(desc);
        autoField.setNewFieldName(newName);
        autoField.setOldFieldName(oldName);
        AutoField autoField1 = new AutoField(oldName, newName, desc);
        // comparing
        Assert.assertEquals(autoField.getDesc(), autoField1.getDesc());
        Assert.assertEquals(autoField.getNewFieldName(), autoField1.getNewFieldName());
        Assert.assertEquals(autoField.getOldFieldName(), autoField1.getOldFieldName());

        // write and build from json
        Gson gson = new GsonBuilder().create();
        String strObject = gson.toJson(autoField);
        String strObject1 = gson.toJson(autoField1);
        AutoField autoField2 = gson.fromJson(strObject, AutoField.class);
        Assert.assertEquals(strObject, strObject1);
        Assert.assertEquals(autoField.getNewFieldName(), autoField2.getNewFieldName());
        Assert.assertEquals(autoField.getOldFieldName(), autoField2.getOldFieldName());
        Assert.assertTrue(sameDesc(autoField.getDesc(), autoField2.getDesc()));
    }

    @Test
    public void testAutoMethod() {
        // preparing
        MDesc desc = buildMDesc();
        AutoMethod autoMethod = new AutoMethod();
        autoMethod.setMDesc(desc);
        autoMethod.setOldMethodName("oldName");
        autoMethod.setNewMethodName("newName");
        autoMethod.setParamTypes(buildParams());
        // build with two constructors
        AutoMethod method =
            new AutoMethod("oldName", "newName", desc, autoMethod.getParamTypes(), autoMethod.getParamTypes());
        Assert.assertTrue(method.getNewMethodName().equals(autoMethod.getNewMethodName()));
        // write and build from json
        Gson gson = new GsonBuilder().create();
        String strObject = gson.toJson(autoMethod);
        AutoMethod autoMethod1 = gson.fromJson(strObject, AutoMethod.class);
        Assert.assertEquals(autoMethod.getNewMethodName(), autoMethod1.getNewMethodName());
        Assert.assertEquals(autoMethod.getOldMethodName(), autoMethod1.getOldMethodName());
        Assert.assertTrue(sameDesc(autoMethod.getDesc(), autoMethod1.getDesc()));
        Assert.assertTrue(autoMethod.getParamTypes().size() == autoMethod1.getParamTypes().size());
        autoMethod1.setNewMethodName("newName1");
        Assert.assertNotEquals(autoMethod.getNewMethodName(), autoMethod1.getNewMethodName());
    }

    @Test
    public void testAutoClass() {
        // preparing
        Desc desc = buildDesc();
        AutoClass autoClass = new AutoClass();
        autoClass.setDesc(desc);
        autoClass.setNewClassName("newName");
        autoClass.setOldClassName("oldName");
        // build from constructor
        AutoClass auto = new AutoClass("oldName", "newName", desc);
        Assert.assertTrue(auto.getOldClassName().equals(autoClass.getOldClassName()));
        // write and build from json
        Gson gson = new GsonBuilder().create();
        String strObject = gson.toJson(autoClass);
        AutoClass autoClass1 = gson.fromJson(strObject, AutoClass.class);
        Assert.assertEquals(autoClass.getNewClassName(), autoClass1.getNewClassName());
        Assert.assertEquals(autoClass.getOldClassName(), autoClass1.getOldClassName());
        Assert.assertTrue(sameDesc(autoClass.getDesc(), autoClass1.getDesc()));
        autoClass.setNewClassName("newName1");
        Assert.assertNotEquals(autoClass.getNewClassName(), autoClass1.getNewClassName());
    }

    @Test
    public void testAuto() {
        Auto auto = new Auto();
        auto.getAutoClasses().add(new AutoClass());
        auto.getAutoMethods().add(new AutoMethod());
        auto.getAutoFields().add(new AutoField());
        Assert.assertEquals(auto.getAutoClasses().size(), 1);
        Assert.assertEquals(auto.getAutoMethods().size(), 1);
        Assert.assertEquals(auto.getAutoFields().size(), 1);
    }

    @Test
    public void testManualField() {
        // preparing
        Desc desc = buildDesc();
        // constructor
        ManualField manualField = new ManualField("fieldName", desc);
        // write and build from json
        Gson gson = new GsonBuilder().create();
        String strObject = gson.toJson(manualField);
        ManualField manualField1 = gson.fromJson(strObject, ManualField.class);
        Assert.assertTrue(manualField.getFieldName().equals(manualField1.getFieldName()));
        Assert.assertTrue(sameDesc(manualField.getDesc(), manualField1.getDesc()));
    }

    @Test
    public void testManualMethod() {
        // preparing
        MDesc desc = buildMDesc();
        // constructor
        ManualMethod manualMethod = new ManualMethod("methodName", buildParams(), buildParams(), desc);
        // write and build from json
        Gson gson = new GsonBuilder().create();
        String strObject = gson.toJson(manualMethod);
        ManualMethod manualMethod1 = gson.fromJson(strObject, ManualMethod.class);
        Assert.assertTrue(manualMethod.getMethodName().equals(manualMethod1.getMethodName()));
        Assert.assertTrue(sameDesc(manualMethod.getDesc(), manualMethod1.getDesc()));
        Assert.assertEquals(manualMethod.getParamTypes().size(), manualMethod1.getParamTypes().size());
    }

    @Test
    public void testManualClass() {
        // preparing
        Desc desc = buildDesc();
        // constructor
        ManualClass manualClass = new ManualClass("ClassName", desc);
        // write and build from json
        Gson gson = new GsonBuilder().create();
        String strObject = gson.toJson(manualClass);
        ManualClass manualClass1 = gson.fromJson(strObject, ManualClass.class);
        Assert.assertTrue(manualClass.getClassName().equals(manualClass1.getClassName()));
        Assert.assertTrue(sameDesc(manualClass.getDesc(), manualClass1.getDesc()));
    }

    @Test
    public void testManual() {
        Manual manual = new Manual();
        manual.getManualClasses().add(new ManualClass("class", buildDesc()));
        manual.getManualFields().add(new ManualField("Field", buildDesc()));
        manual.getManualMethods().add(new ManualMethod("Method", buildParams(), buildParams(), buildMDesc()));
        manual.getManualPackages().addAll(Collections.emptyList());
        Assert.assertTrue(manual.getManualClasses().size() == 1);
        Assert.assertTrue(manual.getManualMethods().size() == 1);
        Assert.assertTrue(manual.getManualFields().size() == 1);
        Assert.assertTrue(manual.getManualPackages().size() == 0);
    }

    @Test
    public void testExPackage() {
        // preparing
        ExDesc desc = buildExDesc();
        ExPackage exPackage = new ExPackage();
        exPackage.setDeletedPackageName("package name");
        exPackage.setDesc(desc);
        // constructor
        ExPackage e = new ExPackage(exPackage.getDeletedPackageName(), exPackage.getDesc());
        Assert.assertEquals(exPackage.getDeletedPackageName(), e.getDeletedPackageName());
        // write and build from json
        Gson gson = new GsonBuilder().create();
        String strObject = gson.toJson(exPackage);
        ExPackage exPackage1 = gson.fromJson(strObject, ExPackage.class);
        Assert.assertEquals(exPackage.getDeletedPackageName(), exPackage1.getDeletedPackageName());
        Assert.assertTrue(sameDesc(exPackage.getDesc(), exPackage1.getDesc()));
    }

    @Test
    public void testExtension() {
        String str = "{\n" + "\t\"manualPackages\": [\n" + "\t\t{\n"
            + "\t\t\t\"deletedPackageName\" : \"com.google.android.gms\",\n" + "\t\t\t\"desc\": {\n"
            + "\t\t\t\t\"text\": \"com.google.android.gms.* is not supported. Determine a code excution environment. "
            + "When the GMS development framework is supported for execution, invoke this GMS API.\",\n"
            + "\t\t\t\t\"url\": \"\",\n" + "\t\t\t\t\"kit\": \"Common\",\n" + "\t\t\t\t\"autoConvert\": false,\n"
            + "\t\t\t\t\"support\": false\n" + "\t\t\t}\n" + "\t\t},\n" + "\t\t{\n"
            + "\t\t\t\"deletedPackageName\" : \"com.google.firebase\",\n" + "\t\t\t\"desc\": {\n"
            + "\t\t\t\t\"text\":\"com.google.firebase.* is not supported. Determine a code excution environment. "
            + "When the GMS development framework is supported for execution, invoke this GMS API.\",\n"
            + "\t\t\t\t\"url\": \"\",\n" + "\t\t\t\t\"kit\": \"Common\",\n" + "\t\t\t\t\"autoConvert\": false,\n"
            + "\t\t\t\t\"support\": false\n" + "\t\t\t}\n" + "\t\t}\n" + "\t]\n" + "}";
        // write and build from json
        Gson gson = new GsonBuilder().create();
        G2XExtension extension = gson.fromJson(str, G2XExtension.class);
        Assert.assertTrue(extension.getManualPackages().size() == 2);
        G2XExtension extension1 = new G2XExtension();
        extension1.setManualPackages(extension.getManualPackages());
        Assert.assertFalse(extension.getManualPackages().size() == 1);
    }

    private Desc buildDesc() {
        Desc desc = new Desc();
        desc.setText("text");
        desc.setUrl("url");
        desc.setKit("kit");
        desc.setDependencyName("dependencyName");
        desc.setGmsVersion("gmsVersion");
        desc.setHmsVersion("hmsVersion");
        desc.setStatus("true");
        desc.setSupport(true);

        return desc;
    }

    private MDesc buildMDesc() {
        MDesc desc = new MDesc();
        desc.setText("text");
        desc.setUrl("url");
        desc.setKit("kit");
        desc.setDependencyName("dependencyName");
        desc.setGmsVersion("gmsVersion");
        desc.setHmsVersion("hmsVersion");
        desc.setStatus("true");
        desc.setSupport(true);
        desc.setMethodName("methodName");
        return desc;
    }

    private ExDesc buildExDesc() {
        ExDesc desc = new ExDesc();
        desc.setText("text");
        desc.setUrl("url");
        desc.setKit("kit");
        desc.setStatus("true");
        desc.setSupport(true);
        return desc;
    }

    private boolean sameDesc(Desc source, Desc target) {
        if (source == target) {
            return true;
        }

        if (source == null) {
            return false;
        }

        if (target == null) {
            return false;
        }

        return  source.isSupport() == target.isSupport()
            && Objects.equals(source.getText(), target.getText()) && Objects.equals(source.getUrl(), target.getUrl())
            && Objects.equals(source.getKit(), target.getKit())
            && Objects.equals(source.getDependencyName(), target.getDependencyName())
            && Objects.equals(source.getGmsVersion(), target.getGmsVersion())
            && Objects.equals(source.getHmsVersion(), target.getHmsVersion());
    }

    private boolean sameDesc(MDesc source, MDesc target) {
        if (source == target) {
            return true;
        }

        if (source == null) {
            return false;
        }

        if (target == null) {
            return false;
        }

        return source.getStatus().equals(target.getStatus()) && source.isSupport() == target.isSupport()
            && Objects.equals(source.getText(), target.getText()) && Objects.equals(source.getUrl(), target.getUrl())
            && Objects.equals(source.getKit(), target.getKit())
            && Objects.equals(source.getDependencyName(), target.getDependencyName())
            && Objects.equals(source.getGmsVersion(), target.getGmsVersion())
            && Objects.equals(source.getHmsVersion(), target.getHmsVersion())
            && Objects.equals(source.getMethodName(), target.getMethodName());
    }

    private boolean sameDesc(ExDesc source, ExDesc target) {
        if (source == target) {
            return true;
        }

        if (source == null) {
            return false;
        }

        if (target == null) {
            return false;
        }

        return source.getStatus().equals(target.getStatus()) && source.isSupport() == target.isSupport()
            && Objects.equals(source.getText(), target.getText()) && Objects.equals(source.getUrl(), target.getUrl())
            && Objects.equals(source.getKit(), target.getKit());
    }

    private List<String> buildParams() {
        List<String> params = new ArrayList<>();
        params.add("android.app.Activity");
        params.add("#BUILT_IN.int");
        return params;
    }
}
