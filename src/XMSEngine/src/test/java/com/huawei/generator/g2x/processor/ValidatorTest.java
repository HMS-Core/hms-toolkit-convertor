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

package com.huawei.generator.g2x.processor;

import com.huawei.generator.g2x.processor.map.Validator;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * test KitValidator
 *
 * @since 2020-01-07
 */
public class ValidatorTest {
    private final String USR_DIR = System.getProperty("user.dir");

    private final String JSON_PATH =
        String.join(File.separator, USR_DIR, "target", "test-classes", "xms", "unittest", "kitvalidator");

    private final String KIT_INFO_PATH =
        String.join(File.separator, USR_DIR, "target", "classes", "xms", "g2x_config", "kit_info.json");

    private final String EMPTY_PATH = "emp";

    private final String WRONG_JSON = "wrong_json.json";

    private final String DEPENDENCY = "kit_validator_test.json";

    @Test
    public void testGenerateEssentialDependency() {
        List<String> kitList = Collections.singletonList("wallet");
        List<String> result = Validator.generateEssentialDependency(kitList);
        List<String> expectedResult = Arrays.asList("framework", "maps", "identity", "account", "wallet", "location");
        Assert.assertEquals(result.size(), 6);
        Assert.assertTrue(result.containsAll(expectedResult));
        Assert.assertTrue(expectedResult.containsAll(result));
    }

    @Test
    public void testValidateParam() {
        Assert.assertSame(GeneratorResult.INVALID_OUTPATH,
            Validator.validateParam(null, JSON_PATH, JSON_PATH));
        Assert.assertSame(GeneratorResult.INVALID_SUMMARYPATH,
            Validator.validateParam(JSON_PATH, null, JSON_PATH));
        Assert.assertSame(GeneratorResult.MISSING_PLUGIN,
            Validator.validateParam(JSON_PATH, JSON_PATH, JSON_PATH));
    }
}
