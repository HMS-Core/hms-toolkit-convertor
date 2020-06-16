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

import com.huawei.codebot.framework.FixBotArguments;
import com.huawei.codebot.framework.dispatch.argparser.ArgParserImpl;
import com.huawei.codebot.framework.dispatch.argparser.CodeMigrateOptions;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;

import java.util.EnumSet;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * An argument parser of this project
 *
 * @since 2020-04-01
 */
public class ArgParserImpl4CodeMigrate extends ArgParserImpl {
    ArgParserImpl4CodeMigrate(String[] args) throws CodeBotRuntimeException {
        super(args);
    }

    @Override
    protected void setSystemProperties() {
        System.setProperty("user.codemigrate.dir", getFixBotArguments().getConfigPath());
    }

    @Override
    protected Map<CodeMigrateOptions, BiConsumer<FixBotArguments, Object>> getOption2SetterMap() {
        // with Java 9 it can be refactored to Map.of()
        Map<CodeMigrateOptions, BiConsumer<FixBotArguments, Object>> res = super.getOption2SetterMap();
        res.put(CodeMigrateOptions.X2Y_CONFIG_PATH, FixBotArguments::setConfigPath);
        res.put(CodeMigrateOptions.X2Y_ENGINE_PATH, FixBotArguments::setEnginePath);
        return res;
    }

    @Override
    protected EnumSet<CodeMigrateOptions> getCriticalOptions() {
        EnumSet<CodeMigrateOptions> enumSet = super.getCriticalOptions();
        enumSet.add(CodeMigrateOptions.X2Y_CONFIG_PATH);
        enumSet.add(CodeMigrateOptions.X2Y_ENGINE_PATH);
        return enumSet;
    }
}
