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

package com.huawei.codebot.analyzer.x2y.global;

import java.util.Observable;
import java.util.Observer;

/**
 * Abstract analyzer that implements Observer and perform
 *
 * @since 2019-07-14
 */
public abstract class AbstractAnalyzer implements Observer {
    @Override
    public void update(Observable observable, Object arg) {
        if (observable instanceof AnalyzerHub && arg == null) {
            postAnalyze((AnalyzerHub) observable);
        } else if (observable instanceof AnalyzerHub && arg instanceof AnalyzerHub.Context) {
            analyze((AnalyzerHub) observable,
                    ((AnalyzerHub.Context) arg).typeInferencer, ((AnalyzerHub.Context) arg).node);
        }
    }

    /**
     * Analyze node
     *
     * @param hub Observable
     * @param typeInferencer ASTNode: an inferencer to infer code elements' type.
     * @param node AstNode
     */
    public abstract void analyze(AnalyzerHub hub, TypeInferencer typeInferencer, Object node);

    /**
     * Called after analysis
     *
     * @param hub Observable
     */
    public abstract void postAnalyze(AnalyzerHub hub);

}
