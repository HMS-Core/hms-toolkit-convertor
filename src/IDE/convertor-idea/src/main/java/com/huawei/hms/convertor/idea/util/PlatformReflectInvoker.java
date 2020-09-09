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

package com.huawei.hms.convertor.idea.util;

import io.netty.channel.ConnectTimeoutException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Platform reflect invoker
 *
 * @since 2020-01-15
 */
@Slf4j
public final class PlatformReflectInvoker {
    /**
     * Invoke platform method
     *
     * @param className The class name
     * @param methodName The method name
     * @return Invoke result
     */
    static InvokeResult invokeMethod(String className, String methodName) {
        try {
            Class targetClass = Class.forName(className);
            Method targetMethod = targetClass.getDeclaredMethod(methodName);
            return new InvokeResult(false, targetMethod.invoke(targetClass.newInstance()));
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            log.info("No sdk found");
            return new InvokeResult(true);
        } catch (Exception e) {
            log.error("Cannot access {}.{}", className, methodName, e);
            return new InvokeResult(false);
        }
    }

    /**
     * Invoke platform method
     *
     * @param className Class name
     * @param methodName Method name
     * @param parameterTypes Parameter types
     * @param parameters Parameters
     * @return Invoke result
     */
    static InvokeResult invokeMethod(String className, String methodName, Class<?>[] parameterTypes,
        Object[] parameters) {
        try {
            Class targetClass = Class.forName(className);
            Method targetMethod = targetClass.getDeclaredMethod(methodName, parameterTypes);
            return new InvokeResult(false, targetMethod.invoke(targetClass.newInstance(), parameters));
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            log.info("No sdk found");
            return new InvokeResult(true);
        } catch (Exception e) {
            log.error("Cannot access {}.{}", className, methodName, e);
            if (e instanceof ConnectTimeoutException) {
                throw new NetworkTimeoutException("Connection timeout", e);
            }
            return new InvokeResult(false);
        }
    }

    /**
     * Invoke platform static method
     *
     * @param className The class name
     * @param methodName The method name
     * @return Invoke result
     */
    public static InvokeResult invokeStaticMethod(String className, String methodName) {
        try {
            Class targetClass = Class.forName(className);
            Method targetMethod = targetClass.getDeclaredMethod(methodName);
            return new InvokeResult(false, targetMethod.invoke(null));
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            log.info("No sdk found");
            return new InvokeResult(true);
        } catch (Exception e) {
            log.error("Cannot access {}.{}", className, methodName, e);
            return new InvokeResult(false);
        }
    }

    public static InvokeResult invokeStaticMethod(String className, String methodName, Class<?>[] parameterTypes,
        Object[] parameters) {
        log.info("Begin invoke static method, className: {}, methodName: {}, parameterTypes: {}.", className,
            methodName, Arrays.toString(parameterTypes));

        try {
            Class targetClass = Class.forName(className);
            Method targetMethod = targetClass.getDeclaredMethod(methodName, parameterTypes);
            InvokeResult result = new InvokeResult(false, targetMethod.invoke(null, parameters));
            log.info("End invoke static method, sdkNotFound: {}.", result.isSdkNotFound());
            return result;
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            log.info("No sdk found, error: {}.", e.getMessage());
            return new InvokeResult(true);
        } catch (Exception e) {
            log.error("Cannot access class, error: {}.", e.getMessage());
            return new InvokeResult(false);
        }
    }

    public static class NetworkTimeoutException extends RuntimeException {
        private static final long serialVersionUID = 6250891878280402029L;

        public NetworkTimeoutException(String message, Throwable throwable) {
            super(message, throwable);
        }
    }

    /**
     * Reflect invoke result
     */
    public static class InvokeResult {
        private boolean sdkNotFound;

        private Object returnValue;

        /**
         * Construction method
         *
         * @param sdkNotFound Set to {@code true} if SDK not found, else {@code false}
         */
        InvokeResult(boolean sdkNotFound) {
            this.sdkNotFound = sdkNotFound;
        }

        /**
         * Construction method
         *
         * @param sdkNotFound Set to {@code true} if SDK not found, else {@code false}
         * @param returnValue Reflect return value
         */
        InvokeResult(boolean sdkNotFound, Object returnValue) {
            this.sdkNotFound = sdkNotFound;
            this.returnValue = returnValue;
        }

        public boolean isSdkNotFound() {
            return sdkNotFound;
        }

        public Object getReturnValue() {
            return returnValue;
        }
    }
}
