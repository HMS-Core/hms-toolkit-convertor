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

import com.huawei.generator.g2x.processor.GeneratorStrategyKind;
import com.huawei.generator.method.component.GComponent;
import com.huawei.generator.method.component.HComponent;
import com.huawei.generator.method.factory.GHMethodGeneratorFactory;
import com.huawei.generator.method.factory.GOMethodGeneratorFactory;
import com.huawei.generator.method.factory.MethodGeneratorFactory;
import com.huawei.generator.method.factory.SingleTargetGeneratorFactory;
import com.huawei.generator.method.factory.StubGeneratorFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Function description
 *
 * @since 2020-03-27
 */
public class GeneratorConfiguration {
    private static Map<GeneratorStrategyKind, GeneratorConfiguration> configurationMap = new HashMap<>();

    private MethodGeneratorFactory factory;

    private String codePath;

    private String staticPath;

    private boolean enableTodo;

    private GeneratorConfiguration(MethodGeneratorFactory factory, String codePath, String staticPath,
        boolean enableTodo) {
        this.factory = factory;
        this.codePath = codePath;
        this.staticPath = staticPath;
        this.enableTodo = enableTodo;
    }

    static {
        configurationMap.put(GeneratorStrategyKind.GOrH,
            new GeneratorConfiguration(new GHMethodGeneratorFactory(), "gh", "gh", true));
        configurationMap.put(GeneratorStrategyKind.HOrG,
            new GeneratorConfiguration(new GHMethodGeneratorFactory(), "gh", "gh", true));
        configurationMap.put(GeneratorStrategyKind.G,
            new GeneratorConfiguration(new GOMethodGeneratorFactory(), "gh", "g", false));
        configurationMap.put(GeneratorStrategyKind.XG,
            new GeneratorConfiguration(new SingleTargetGeneratorFactory(new GComponent()), "z", "xg", false));
        configurationMap.put(GeneratorStrategyKind.XH,
            new GeneratorConfiguration(new SingleTargetGeneratorFactory(new HComponent()), "z", "xh", true));
        configurationMap.put(GeneratorStrategyKind.XAPI,
            new GeneratorConfiguration(new StubGeneratorFactory(), "z", "xapi", false));
    }

    public static GeneratorConfiguration getConfiguration(GeneratorStrategyKind strategyKind) {
        return configurationMap.get(strategyKind);
    }

    public MethodGeneratorFactory getFactory() {
        return factory;
    }

    public String getCodePath() {
        return codePath;
    }

    public String getStaticPath() {
        return staticPath;
    }

    public boolean isEnableTodo() {
        return enableTodo;
    }
}
