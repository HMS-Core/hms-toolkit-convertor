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

package com.huawei.generator.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.huawei.generator.g2x.po.map.extension.G2XExtension;

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;

/**
 * JSON parser.
 *
 * @since 2019-11-12
 */
public class Parser {
    private static Gson GSON = createGson();

    public static JClass parse(Reader reader) {
        Type type = new TypeToken<JClass>() {}.getType();
        return GSON.fromJson(reader, type);
    }

    public static G2XExtension parseEx(Reader reader) {
        Type type = new TypeToken<G2XExtension>() {}.getType();
        return GSON.fromJson(reader, type);
    }

    // set default value for text，url，hmsversion
    static Gson createGson() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(String.class, new StringDefaultAdapter());
        return builder.create();
    }

    static class StringDefaultAdapter extends TypeAdapter<String> {
        @Override
        public void write(JsonWriter jsonWriter, String s) throws IOException {
            jsonWriter.value(s);
        }

        @Override
        public String read(JsonReader jsonReader) throws IOException {
            if ((jsonReader.getPath().endsWith("hmsVersion") || jsonReader.getPath().endsWith("url"))) {
                if (jsonReader.peek() == JsonToken.NULL) {
                    jsonReader.nextNull();
                    return "";
                } else {
                    String str = jsonReader.nextString();
                    if (str.trim().isEmpty()) {
                        return "";
                    }
                    return str;
                }
            }

            if (jsonReader.getPath().endsWith("text")) {
                if (jsonReader.peek() == JsonToken.NULL) {
                    jsonReader.nextNull();
                    return "Documents to be completed";
                } else {
                    String str = jsonReader.nextString();
                    if (str.trim().isEmpty()) {
                        return "Documents to be completed";
                    }
                    return str;
                }
            }
            return jsonReader.nextString();
        }
    }
}
