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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.huawei.generator.g2x.processor.GeneratorStrategyKind;
import com.huawei.generator.json.meta.ClassRelation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
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

    String dumpMappings(String src, List<String> specifiedKitList) {
        try (InputStream ins = RuntimeTypeMappings.class.getResourceAsStream(src);
            InputStreamReader isr = new InputStreamReader(ins, StandardCharsets.UTF_8)) {
            Type type = new TypeToken<Map<String, List<ClassRelation>>>() {}.getType();
            Gson gson = new Gson();
            Map<String, List<ClassRelation>> jMapping = gson.fromJson(isr, type);

            List<ClassRelation> classRelations = new ArrayList<>();
            if (specifiedKitList == null) {
                // When there is no specified kit, generate all kit
                jMapping.values().forEach(classRelations::addAll);
            } else {
                specifiedKitList.forEach(str -> classRelations.addAll(jMapping.get(str)));
            }
            classRelations.sort(Comparator.comparing(ClassRelation::getXmsClassName));
            return dumpClassRelations(classRelations);
        } catch (IOException e) {
            LOGGER.warn("Failed to dump mappings");
        }
        return "";
    }

    /**
     * Dump class relations as a string.
     *
     * @param relations mapping relations
     * @return dumped string
     */
    protected abstract String dumpClassRelations(List<ClassRelation> relations);

    public static RuntimeTypeMappings create(GeneratorStrategyKind strategy) {
        if (strategy == GeneratorStrategyKind.G || strategy == GeneratorStrategyKind.XG) {
            return new OnlyG();
        } else if (strategy == GeneratorStrategyKind.XH) {
            return new OnlyH();
        } else if (strategy == GeneratorStrategyKind.XAPI) {
            return new Empty();
        } else {
            return new GAndH();
        }
    }

    private static class GAndH extends RuntimeTypeMappings {
        @Override
        protected String dumpClassRelations(List<ClassRelation> relations) {
            String gh2x = relations.stream()
                .map(it -> entryTemplate(it).replace("${G}", it.getGmsClassName())
                    .replace("${H}", it.getHmsClassName())
                    .replace("${X}", it.getXmsClassName()))
                .collect(Collectors.joining(LINE_DELIMITER));
            String g2h = relations.stream()
                .filter(it -> !it.getHmsClassName().isEmpty())
                .map(it -> "G2H.put(\"${G}\", \"${H}\");".replace("${G}", it.getGmsClassName())
                    .replace("${H}", it.getHmsClassName()))
                .collect(Collectors.joining(LINE_DELIMITER));
            String h2g = relations.stream()
                .filter(it -> !it.getHmsClassName().isEmpty())
                .map(it -> "H2G.put(\"${H}\", \"${G}\");".replace("${G}", it.getGmsClassName())
                    .replace("${H}", it.getHmsClassName()))
                .collect(Collectors.joining(LINE_DELIMITER));
            return String.join(LINE_DELIMITER, gh2x, g2h, h2g);
        }

        private String entryTemplate(ClassRelation r) {
            String template = "map.put(\"${G}\", \"${X}\");";
            if (!r.getHmsClassName().isEmpty()) {
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
                .map(it -> "map.put(\"${G}\", \"${X}\");".replace("${G}", it.getGmsClassName())
                    .replace("${X}", it.getXmsClassName()))
                .collect(Collectors.joining(LINE_DELIMITER));
        }
    }

    private static class OnlyH extends RuntimeTypeMappings {
        @Override
        protected String dumpClassRelations(List<ClassRelation> relations) {
            return relations.stream()
                .map(it -> "map.put(\"${H}\", \"${X}\");".replace("${H}", it.getHmsClassName())
                    .replace("${X}", it.getXmsClassName()))
                .collect(Collectors.joining(LINE_DELIMITER));
        }
    }

    private static class Empty extends RuntimeTypeMappings {
        @Override
        String dumpMappings(String src, List<String> specifiedKitList) {
            return "";
        }

        @Override
        protected String dumpClassRelations(List<ClassRelation> relations) {
            throw new IllegalStateException();
        }
    }
}
