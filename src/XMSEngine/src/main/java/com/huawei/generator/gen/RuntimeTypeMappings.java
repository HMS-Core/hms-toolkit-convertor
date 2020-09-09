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

package com.huawei.generator.gen;

import com.huawei.generator.build.ClassMappingManager;
import com.huawei.generator.g2x.processor.GeneratorStrategyKind;
import com.huawei.generator.json.meta.ClassRelation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Runtime type mappings generator.
 *
 * @since 2020-02-05
 */
public abstract class RuntimeTypeMappings {
    private static final Logger LOGGER = LoggerFactory.getLogger(RuntimeTypeMappings.class);

    private static final String LINE_DELIMITER = "\n        ";

    private static Map<String, List<ClassRelation>> jMapping;

    public static void generateMappingRelation(Map<String, String> kitVersion, String pluginPath) {
        jMapping = new ClassMappingManager().generateMap(kitVersion, pluginPath);
    }

    String dumpMappings(List<String> specifiedKitList) {
        List<ClassRelation> classRelations = new ArrayList<>();
        if (jMapping == null) {
            LOGGER.error("Mapping is null, Please initialize!");
        }
        if (specifiedKitList == null) {
            // When there is no specified kit, generate all kit
            jMapping.values().forEach(classRelations::addAll);
        } else {
            specifiedKitList.forEach(str -> classRelations.addAll(jMapping.get(str)));
        }
        classRelations.sort(Comparator.comparing(ClassRelation::getXmsClassName));
        return dumpClassRelations(classRelations);
    }

    /**
     * Dump class relations as a string.
     *
     * @param relations mapping relations
     * @return dumped string
     */
    protected abstract String dumpClassRelations(List<ClassRelation> relations);

    public static RuntimeTypeMappings create(GeneratorStrategyKind strategy) {
        switch (strategy) {
            case G:
            case XG:
                return new OnlyG();
            case H:
            case XH:
                return new OnlyH();
            case XAPI:
                return new Empty();
            default:
                return new GAndH();
        }
    }

    private static class GAndH extends RuntimeTypeMappings {
        @Override
        protected String dumpClassRelations(List<ClassRelation> relations) {
            String gh2x = relations.stream()
                .map(classRelation -> entryTemplate(classRelation).replace("${G}", classRelation.getGmsClassName())
                    .replace("${H}", classRelation.getHmsClassName())
                    .replace("${X}", classRelation.getXmsClassName()))
                .collect(Collectors.joining(LINE_DELIMITER));
            return String.join(LINE_DELIMITER, gh2x);
        }

        private String entryTemplate(ClassRelation classRelation) {
            String template = "map.put(\"${G}\", \"${X}\");";
            if (!classRelation.getHmsClassName().isEmpty()) {
                template += LINE_DELIMITER;
                template += "map.put(\"${H}\", \"${X}\");";
            }
            return template;
        }
    }

    private static class OnlyG extends RuntimeTypeMappings {
        @Override
        protected String dumpClassRelations(List<ClassRelation> relations) {
            return relations.stream()
                .map(classRelation -> "map.put(\"${G}\", \"${X}\");".replace("${G}", classRelation.getGmsClassName())
                    .replace("${X}", classRelation.getXmsClassName()))
                .collect(Collectors.joining(LINE_DELIMITER));
        }
    }

    private static class OnlyH extends RuntimeTypeMappings {
        @Override
        protected String dumpClassRelations(List<ClassRelation> relations) {
            return relations.stream()
                .map(classRelation -> "map.put(\"${H}\", \"${X}\");".replace("${H}", classRelation.getHmsClassName())
                    .replace("${X}", classRelation.getXmsClassName()))
                .collect(Collectors.joining(LINE_DELIMITER));
        }
    }

    private static class Empty extends RuntimeTypeMappings {
        @Override
        String dumpMappings(List<String> specifiedKitList) {
            return "";
        }

        @Override
        protected String dumpClassRelations(List<ClassRelation> relations) {
            throw new IllegalStateException();
        }
    }
}
