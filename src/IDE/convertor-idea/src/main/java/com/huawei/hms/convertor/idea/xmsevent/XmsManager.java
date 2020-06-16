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

package com.huawei.hms.convertor.idea.xmsevent;

import com.huawei.generator.g2x.po.summary.Summary;
import com.huawei.generator.g2x.processor.GeneratorStrategyKind;
import com.huawei.hms.convertor.core.config.ConfigKeyConstants;
import com.huawei.hms.convertor.core.engine.fixbot.model.RoutePolicy;
import com.huawei.hms.convertor.core.engine.xms.XmsConstants;
import com.huawei.hms.convertor.idea.util.StringUtil;
import com.huawei.hms.convertor.openapi.ConfigCacheService;
import com.huawei.hms.convertor.openapi.XmsGenerateService;

import com.intellij.openapi.project.Project;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Xms event manager
 *
 * @since 2020-03-09
 */
@Slf4j
public class XmsManager {
    private static final XmsManager XMS_MANAGER = new XmsManager();

    private static Map<String, XmsEventConsumer> consumerMap = new HashMap<>();

    private static Map<String, XmsEventQueue> queueMap = new HashMap<>();

    private XmsManager() {
    }

    /**
     * Get singleton instance of {@code XmsManager}
     *
     * @return The singleton instance of {@code XmsManager}
     */
    public static XmsManager getInstance() {
        return XMS_MANAGER;
    }

    /**
     * Create pipeline
     *
     * @param project the analyzed project
     */
    public void createPipeline(@NonNull Project project) {
        if (consumerMap.containsKey(project.getBasePath())) {
            log.warn("{} pipeline already exists， {}", project.getName(), project.getBasePath());
            return;
        }

        XmsEventQueue xmsEventQueue = new XmsEventQueue();
        XmsEventConsumer xmsEventConsumer = new XmsEventConsumer(project, xmsEventQueue);
        consumerMap.put(project.getBasePath(), xmsEventConsumer);
        queueMap.put(project.getBasePath(), xmsEventQueue);
        log.info("Create pipeline for {} {}", project.getName(), project.getBasePath());
    }

    /**
     * Remove pipeline
     *
     * @param project the analyzed project
     */
    public void removePipeline(@NonNull Project project) {
        if (consumerMap.containsKey(project.getBasePath())) {
            consumerMap.remove(project.getBasePath());
            log.info("Remove pipeline for {} {}", project.getName(), project.getBasePath());
            return;
        }
        log.warn("Pipeline for {} {} doesn't exist", project.getName(), project.getBasePath());
    }

    /**
     * Get the XmsEvent queue
     *
     * @param projectBasePath project basePath
     * @return XmsEvent queue
     */
    public Optional<XmsEventQueue> getQueue(String projectBasePath) {
        if (!queueMap.containsKey(projectBasePath)) {
            log.warn("No pipeline for {}", projectBasePath);
            return Optional.empty();
        }

        XmsEventQueue xmsEventQueue = queueMap.get(projectBasePath);
        return Optional.of(xmsEventQueue);
    }

    /**
     * Get XmsEvent consumer
     *
     * @param projectBasePath project base path
     * @return XmsEvent consumer
     */
    public Optional<XmsEventConsumer> getConsumer(String projectBasePath) {
        if (!consumerMap.containsKey(projectBasePath)) {
            log.warn("No pipeline for {}", projectBasePath);
            return Optional.empty();
        }

        XmsEventConsumer consumer = consumerMap.get(projectBasePath);
        return Optional.of(consumer);
    }

    /**
     * Init Queue, push event to the queue
     *
     * @param hmsKitItemsStirng message
     * @param project           current project for initialization
     */
    public void pushEvent(String hmsKitItemsStirng, Project project) {
        log.info("Receive project {} {}", project.getName(), project.getBasePath());
        log.info("Event str = {}", hmsKitItemsStirng);

        if (!consumerMap.containsKey(project.getBasePath())) {
            log.warn("No pipeline for {}", project.getBasePath());
            return;
        }

        XmsEventConsumer consumer = consumerMap.get(project.getBasePath());
        if (consumer == null) {
            log.warn("No consumer for {}", project.getBasePath());
            return;
        }

        if (isGAndHPolicy(project)) {
            consumer.getXmsEventQueue().push(hmsKitItemsStirng);
        }
    }

    private boolean isGAndHPolicy(Project project) {
        RoutePolicy routePolicy;
        routePolicy = ConfigCacheService.getInstance()
            .getProjectConfig(project.getBasePath(), ConfigKeyConstants.ROUTE_POLICY, RoutePolicy.class,
                RoutePolicy.UNKNOWN);
        log.info("RoutePolicy {}", routePolicy);
        if (routePolicy == RoutePolicy.UNKNOWN) {
            routePolicy = inferPolicy(project);
        }
        if (routePolicy != RoutePolicy.G_AND_H) {
            return false;
        }
        return true;
    }

    private RoutePolicy inferPolicy(Project project) {
        File file = new File(project.getBasePath() + XmsConstants.XMS_ADAPTER);
        if (!file.exists()) {
            return RoutePolicy.UNKNOWN;
        }

        String pluginJarPath = System.getProperty(XmsConstants.KEY_XMS_JAR);
        if (StringUtil.isEmpty(pluginJarPath)) {
            return RoutePolicy.UNKNOWN;
        }
        Summary summary =
            XmsGenerateService.inferStrategy(pluginJarPath, project.getBasePath() + XmsConstants.XMS_ADAPTER);
        if (summary.strategy == null || summary.strategy.isEmpty()) {
            return RoutePolicy.UNKNOWN;
        }

        for (GeneratorStrategyKind s : summary.strategy) {
            if (GeneratorStrategyKind.HOrG.equals(s)) {
                ConfigCacheService.getInstance()
                    .updateProjectConfig(project.getBasePath(), ConfigKeyConstants.HMS_FIRST, true);
            } else if (GeneratorStrategyKind.G.equals(s)) {
                ConfigCacheService.getInstance()
                    .updateProjectConfig(project.getBasePath(), ConfigKeyConstants.MULTI_APK, true);
            } else {
                log.debug("Strategy {}", s.toString());
            }
        }
        ConfigCacheService.getInstance()
            .updateProjectConfig(project.getBasePath(), ConfigKeyConstants.ROUTE_POLICY, RoutePolicy.G_AND_H);
        return RoutePolicy.G_AND_H;
    }
}
