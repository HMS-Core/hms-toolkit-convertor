package com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.json.model;

import java.util.HashMap;
import java.util.Map;

public class StructGradleInfo {
    private String addString;
    private Map desc;

    public StructGradleInfo() {
        this.desc = new HashMap();
    }

    public String getAddString() {
        return addString;
    }

    public void setAddString(String addString) {
        this.addString = addString;
    }

    public Map getDesc() {
        return desc;
    }

    public void setDesc(Map desc) {
        this.desc = desc;
    }
}
