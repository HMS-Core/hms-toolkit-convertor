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

package com.huawei.codebot.entry.codemigrate;

import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.versionvariable.GradleDependencyChanger;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.versionvariable.GradleGlobalChanger;
import com.huawei.codebot.analyzer.x2y.gradle.gradlechanger.versionvariable.GradleVersionService;
import com.huawei.codebot.analyzer.x2y.java.CompositeChangerFactory;
import com.huawei.codebot.framework.DefectFixerType;
import com.huawei.codebot.framework.FixBotArguments;
import com.huawei.codebot.framework.GenericDefectFixer;
import com.huawei.codebot.framework.api.CodeBotResult;
import com.huawei.codebot.framework.dispatch.argparser.CodeMigrateOptions;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;

import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Entry for obtaining version information of dependencies.
 *
 * @since 2020-06-04
 */
public class VersionInfoRetriever {
    private static final Logger LOGGER = LoggerFactory.getLogger(VersionInfoRetriever.class);
    private static Map<String, String> versionsMapping = new HashMap<>();

    public static FixBotArguments process(String[] args) {
        ArgParserImpl4CodeMigrate parser = null;
        try {
            parser = new ArgParserImpl4CodeMigrate(args);
        } catch (CodeBotRuntimeException ex) {
            LOGGER.error(String.valueOf(CodeBotResult.failure(ex)));
            return null;
        }
        FixBotArguments arguments = parser.getFixBotArguments();

        @SuppressWarnings("unchecked")
        List<String> repoPaths = (List<String>) parser.getParsedAttrs().get(CodeMigrateOptions.REPO_PATH.getOptLower());
        if (repoPaths == null) {
            LOGGER.error("please give repoPath value.");
            return null;
        }

        arguments.setRepoPath(repoPaths.get(0));

        return arguments;
    }

    /**
     * Scan build.gradle file and collect all versions of kits
     *
     * @param args arguments of command line
     * @return kits version mapping info
     */
    public static Map<String, String> getAllKitsVersion(String[] args) throws CodeBotRuntimeException {
        GradleVersionService.initAllVersionInfo();
        versionsMapping.clear();
        FixBotArguments arguments = process(args);
        assert (arguments != null);
        DefectFixerType fixerType = DefectFixerType.fromValue(arguments.getRuleSet());
        List<String> changerOrder = new ArrayList<>();
        changerOrder.add(GradleGlobalChanger.class.getName());
        changerOrder.add(GradleDependencyChanger.class.getName());
        Set<String> changerSet = new HashSet<>();
        changerSet.add(GradleGlobalChanger.class.getName());
        changerSet.add(GradleDependencyChanger.class.getName());
        GenericDefectFixer fixer =
                CompositeChangerFactory.newAutoChanger(fixerType, false, changerOrder, changerSet);
        fixer.fixPatternFolder = Paths.get(arguments.getFixedFilePath(), "fixpatterns").toString();
        fixer.preprocessAndAutoFix(arguments);
        if (!CompositeChangerFactory.isGeneralAutoChanger(fixer)) {
            LOGGER.error("error occurred, fixer type is not up to expectation.");
            return Collections.emptyMap();
        }
        ArrayList<GenericDefectFixer> list = CompositeChangerFactory.getGeneralAtomicFixers(fixer);
        if (list == null || list.isEmpty()) {
            LOGGER.error("error occurred, AtomicFixers is empty.");
            return Collections.emptyMap();
        }
        for (GenericDefectFixer onlyChanger : list) {
            if (onlyChanger instanceof GradleDependencyChanger) {
                for (Map.Entry<String, String> entry : ((GradleDependencyChanger) onlyChanger).versionInfo.entrySet()) {
                    versionsMapping.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return versionsMapping;
    }
}