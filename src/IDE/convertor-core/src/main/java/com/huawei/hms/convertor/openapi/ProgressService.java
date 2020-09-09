/*
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.hms.convertor.openapi;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Endow a number between 0.0 and 1.0 reflecting the ratio of fixbot analysis
 *
 * @since 2020-05-25
 */
public class ProgressService {
    private double currentFraction = 0;

    private volatile boolean cancel;

    public boolean isCancel() {
        return cancel;
    }

    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }

    /**
     * Define the specific fraction in pre-process(e.g backup stage) and post-process(e.g summery the result)
     */
    public enum ProgressStage {
        /**
         * the fraction of start stage
         */
        START_ANALYSIS(0.0),

        /**
         * the fraction of clear data stage
         */
        CLEAR_DATA(0.05),

        /**
         * finish generates an XMS generator for current project.
         */
        NEW_XMS_GENERATOR(0.05),

        /**
         * the fraction of backup stage
         */
        BACKUP(0.15),

        /**
         * has finish generates an new module for G_AND_H route policy.
         */
        GENERATED_NEW_MODULE(0.18),

        /**
         * the fraction of start stage
         */
        FINISHED(1.0);

        ProgressStage(double fraction) {
            this.fraction = fraction;
        }

        private double fraction;

        /**
         * the getter of fraction
         *
         * @return the fraction
         */
        public double getFraction() {
            return fraction;
        }
    }

    /**
     * Define the specific fraction in different stage of fixbot analysis
     */
    public enum EngineStage {
        /**
         * the faction in stage of class AnalyzerHub
         */
        ANALYZER_HUB(0.20, "AnalyzerHub"),

        /**
         * the faction in stage of class ClassMemberAnalyzer
         */
        CLASS_MEMBER_ANALYZER(0.38, "ClassMemberAnalyzer"),

        /**
         * the faction in stage of class AndroidAppFixer
         */
        ANDROID_APP_FIXER(0.56, "AndroidAppFixer"),

        /**
         * the faction in stage of class GenericDefectFixer
         */
        GENERIC_DEFECT_FIXER(0.74, "GenericDefectFixer"),

        /**
         * the faction in stage of class CodeMigrateEntry
         */
        CODE_MIGRATE_ENTRY(0.95, "CodeMigrateEntry");

        EngineStage(Double fraction, String stage) {
            this.fraction = fraction;
            classStage = stage;
        }

        private Double fraction;

        private String classStage;

        /**
         * the getter of fraction
         *
         * @return the fraction
         */
        public Double getFraction() {
            return fraction;
        }

        /**
         * the getter of class stage
         *
         * @return the class stage
         */
        public String getClassStage() {
            return classStage;
        }
    }

    /**
     * get class stages in fixbot
     *
     * @return the specific in current fixbot analysis stage
     */
    public Collection<String> getClassStages() {
        return Stream.of(EngineStage.values()).map(EngineStage::getClassStage).collect(Collectors.toList());
    }

    /**
     * update engine fraction
     *
     * @param classStage the stage of fixbot analysis
     */
    public void updateEngineFraction(String classStage) {
        Double fraction = getFractionByStage(classStage);
        currentFraction = fraction;
    }

    /**
     * get the fraction of current fixbot analysis stage
     *
     * @return the fraction of current fixbot analysis stage
     */
    public double getCurrentFraction() {
        return currentFraction;
    }

    private double getFractionByStage(String classStage) {
        for (EngineStage engineStage : EngineStage.values()) {
            if (engineStage.getClassStage().equals(classStage)) {
                return engineStage.getFraction();
            }
        }
        return 0;
    }
}