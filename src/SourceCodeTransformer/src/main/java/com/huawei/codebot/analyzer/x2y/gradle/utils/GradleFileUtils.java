package com.huawei.codebot.analyzer.x2y.gradle.utils;

import java.io.File;
import java.io.IOException;

public class GradleFileUtils {

    public static final String MODULE_BUILD_GRADLE = "app";
    public static final String PROJECT_BUILD_GRADLE = "project";

    public static final String SETTING_GRADLE_FILE = "settings.gradle";
    public static final String BUILD_GRADLE_FILE = "build.gradle";

    public static final String REPOSITORIES ="repositories";
    public static final String DEPENDENCIES = "dependencies";

    public static final String CLASSPATH = "classpath";
    public static final String IMPLEMENTATIONS = "implementation";
    public static final String DEPENDENCIES_API = "api";
    public static final String RUNTIME_ONLY = "runtimeOnly";
    public static final String COMPILE_ONLY = "compileOnly";

    public static boolean isProjectBuildGradleFile(File file) {
        File parentFile = file.getParentFile();
        if (parentFile.isDirectory()) {
            String settingFile = null;
            try {
                settingFile = parentFile.getCanonicalPath() + File.separator + "settings.gradle";
            } catch (IOException e) {
                return false;
            }
            if (new File(settingFile).exists()) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public static boolean isProject(File file) {
        if (file.isDirectory()) {
            String settingFile = null;
            try {
                settingFile = file.getCanonicalPath() + File.separator + "settings.gradle";
            } catch (IOException e) {
                return false;
            }
            return new File(settingFile).exists();
        }
        return false;
    }
}
