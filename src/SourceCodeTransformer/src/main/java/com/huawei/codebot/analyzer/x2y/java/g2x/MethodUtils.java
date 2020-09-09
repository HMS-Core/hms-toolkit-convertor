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

package com.huawei.codebot.analyzer.x2y.java.g2x;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.huawei.codebot.analyzer.x2y.global.GlobalSettings;
import com.huawei.codebot.framework.model.DefectInstance;
import com.huawei.codebot.framework.parser.kotlin.KotlinParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Add onCreate method in Application class or insert init method in OnCreate method of Application class
 *
 * @since 2020-07-08
 */
public class MethodUtils {

    private static final String ON_CREATE = "onCreate";
    private static final String Q_GLOBAL_ENV_SETTING_INIT_PREFIX = "org.xms.g.utils.GlobalEnvSetting.init(";
    private static final String GLOBAL_ENV_SETTING_INIT_PREFIX = "GlobalEnvSetting.init(";

    private static final String Q_XLOADER_INIT_PREFIX = "org.xms.adapter.utils.XLoader.init(";
    private static final String XLOADER_INIT_PREFIX = "XLoader.init(";

    private static final String SUPER_ONCREATE = "super.onCreate(";
    private static Gson gson = new GsonBuilder().disableHtmlEscaping().create();

    private static boolean checkOnCreateMethod(CompilationUnit unit, MethodDeclaration[] methods, String buggyFilePath,
            AppFileCheckChanger checkChanger) {
        boolean hasOnCreateMethod = false;
        Map<String, Object> message = getMessage();
        for (MethodDeclaration method : methods) {
            if (method.getName().getIdentifier().equals(ON_CREATE)) {
                hasOnCreateMethod = true;
                List<Statement> statements = method.getBody().statements();
                boolean globalInit = false;
                boolean xloaderInit = false;
                int superOnCreateLine = -1;
                int initLine = -1;
                for (Statement statement: statements) {
                    if (statement instanceof ExpressionStatement) {
                        ExpressionStatement expressionStatement = (ExpressionStatement)statement;
                        if (expressionStatement.toString().startsWith(Q_GLOBAL_ENV_SETTING_INIT_PREFIX)
                                || expressionStatement.toString().startsWith(GLOBAL_ENV_SETTING_INIT_PREFIX)) {
                            globalInit = true;
                            initLine = unit.getLineNumber(expressionStatement.getStartPosition() - 1);
                        }
                        if (expressionStatement.toString().startsWith(XLOADER_INIT_PREFIX)
                                || expressionStatement.toString().startsWith(Q_XLOADER_INIT_PREFIX)) {
                            xloaderInit = true;
                        }
                        if (expressionStatement.toString().startsWith(SUPER_ONCREATE)) {
                            superOnCreateLine = unit.getLineNumber(expressionStatement.getStartPosition() - 1);
                        }
                    }
                }
                if (!GlobalSettings.isNeedClassloader() && !globalInit) {
                    DefectInstance defectInstance = checkChanger.createDefectInstance(buggyFilePath,
                            -(superOnCreateLine + 1), null,
                            "        org.xms.g.utils.GlobalEnvSetting.init(this, null);");
                    message.put("text", "Add GlobalEnvSetting.init method");
                    defectInstance.setMessage(gson.toJson(message));
                    checkChanger.defectInstances.add(defectInstance);
                }
                if (GlobalSettings.isNeedClassloader() && !xloaderInit && !globalInit) {
                    DefectInstance defectInstance = checkChanger.createDefectInstance(buggyFilePath,
                            -(superOnCreateLine + 1), null,
                            "        org.xms.g.utils.GlobalEnvSetting.init(this, null);\n"
                                    + "        org.xms.adapter.utils.XLoader.init(this);");
                    message.put("text", "Add GlobalEnvSetting.init and XLoader.init methods");
                    defectInstance.setMessage(gson.toJson(message));
                    checkChanger.defectInstances.add(defectInstance);
                }
                if (GlobalSettings.isNeedClassloader() && !xloaderInit && globalInit) {
                    DefectInstance defectInstance = checkChanger.createDefectInstance(buggyFilePath,
                            -(initLine + 1), null,
                            "        org.xms.adapter.utils.XLoader.init(this);");
                    message.put("text", "Add XLoader.init method");
                    defectInstance.setMessage(gson.toJson(message));
                    checkChanger.defectInstances.add(defectInstance);
                }
            }
        }
        return hasOnCreateMethod;
    }

    /**
     * Create onCreate method or insert init method in onCreate method if there is in Application class of java
     *
     * @param unit compilationUnit unit
     * @param typeDeclaration typeDeclaration
     * @param buggyFilePath buggy file path
     * @param checkChanger check Changer
     */
    public static void createOnCreateMethodInJava(CompilationUnit unit, TypeDeclaration typeDeclaration,
            String buggyFilePath, AppFileCheckChanger checkChanger) {
        Map<String, Object> message = getMessage();
        MethodDeclaration[] methods = typeDeclaration.getMethods();
        boolean hasOnCreateMethod = checkOnCreateMethod(unit, methods, buggyFilePath, checkChanger);
        if (!hasOnCreateMethod) {
            int classStartLine = unit.getLineNumber(typeDeclaration.getStartPosition());
            DefectInstance defectInstance;
            if (GlobalSettings.isNeedClassloader()) {
                defectInstance = checkChanger.createDefectInstance(buggyFilePath, -(classStartLine + 1),
                        null, "    @Override\n" +
                                "    public void onCreate() {\n" +
                                "        super.onCreate();\n" +
                                "        org.xms.g.utils.GlobalEnvSetting.init(this, null);\n" +
                                "        org.xms.adapter.utils.XLoader.init(this);\n" +
                                "    }");
            } else {
                defectInstance = checkChanger.createDefectInstance(buggyFilePath, -(classStartLine + 1),
                        null, "    @Override\n" +
                                "    public void onCreate() {\n" +
                                "        super.onCreate();\n" +
                                "        org.xms.g.utils.GlobalEnvSetting.init(this, null);\n" +
                                "    }");
            }
            message.put("text", "Add onCreate method from its parent class");
            defectInstance.setMessage(gson.toJson(message));
            checkChanger.defectInstances.add(defectInstance);
        }
    }

    private static Map<String, Object> getMessage() {
        Map<String, Object> message = new HashMap<>();
        message.put("fieldName", "");
        message.put("hmsVersion", "");
        message.put("dependencyName", "Common");
        message.put("kit", "Common");
        message.put("support", true);
        message.put("url", "");
        message.put("type", "");
        message.put("gmsVersion", "");
        message.put("status", "AUTO");
        return message;
    }

    private static List<KotlinParser.StatementContext> getStatementContextList(
            KotlinParser.FunctionDeclarationContext functionDeclarationContext) {
        KotlinParser.FunctionBodyContext functionBodyContext
                = functionDeclarationContext.getRuleContext(KotlinParser.FunctionBodyContext.class, 0);
        KotlinParser.BlockContext blockContext
                = functionBodyContext.getRuleContext(KotlinParser.BlockContext.class, 0);
        KotlinParser.StatementsContext statementsContext
                = blockContext.getRuleContext(KotlinParser.StatementsContext.class, 0);
        return statementsContext.getRuleContexts(KotlinParser.StatementContext.class);
    }

    private static boolean hasOnCreateMethodKotlin(KotlinParser.SimpleIdentifierContext simpleIdentifierContext,
            String buggyFilePath, AppFileCheckChanger checkChanger,
            KotlinParser.FunctionDeclarationContext functionDeclarationContext) {
        Map<String, Object> message = getMessage();
        boolean hasOnCreateMethod = false;
        if (ON_CREATE.equals(simpleIdentifierContext.getText())) {
            hasOnCreateMethod = true;
            if (GlobalSettings.isHasApplication()) {
                boolean globalInit = false;
                boolean xloaderInit = false;
                int superOnCreateLine = -1;
                int initLine = -1;
                List<KotlinParser.StatementContext> statementContextList = getStatementContextList(functionDeclarationContext);
                for (KotlinParser.StatementContext statementContext: statementContextList) {
                    if (statementContext.getText().startsWith(Q_GLOBAL_ENV_SETTING_INIT_PREFIX)
                            || statementContext.getText().startsWith(GLOBAL_ENV_SETTING_INIT_PREFIX) ) {
                        globalInit = true;
                        initLine = statementContext.getStart().getLine();
                    }
                    if (statementContext.getText().startsWith(Q_XLOADER_INIT_PREFIX)
                            || statementContext.getText().startsWith(XLOADER_INIT_PREFIX)) {
                        xloaderInit = true;
                    }
                    if (statementContext.getText().startsWith(SUPER_ONCREATE)) {
                        superOnCreateLine = statementContext.getStart().getLine();
                    }
                }
                if (!GlobalSettings.isNeedClassloader() && !globalInit) {
                    DefectInstance defectInstance = checkChanger.createDefectInstance(buggyFilePath,
                            -(superOnCreateLine + 1), null,
                            "        org.xms.g.utils.GlobalEnvSetting.init(this, null)");
                    message.put("text", "Add GlobalEnvSetting.init method");
                    defectInstance.setMessage(gson.toJson(message));
                    checkChanger.defectInstances.add(defectInstance);
                }
                if (GlobalSettings.isNeedClassloader() && !xloaderInit && !globalInit) {
                    DefectInstance defectInstance = checkChanger.createDefectInstance(buggyFilePath,
                            -(superOnCreateLine + 1), null,
                            "        org.xms.g.utils.GlobalEnvSetting.init(this, null)\n"
                                    + "        org.xms.adapter.utils.XLoader.init(this)");
                    message.put("text", "Add GlobalEnvSetting.init and XLoader.init methods");
                    defectInstance.setMessage(gson.toJson(message));
                    checkChanger.defectInstances.add(defectInstance);
                }
                if (GlobalSettings.isNeedClassloader() && !xloaderInit && globalInit) {
                    DefectInstance defectInstance = checkChanger.createDefectInstance(buggyFilePath,
                            -(initLine + 1), null,
                            "        org.xms.adapter.utils.XLoader.init(this)");
                    message.put("text", "Add XLoader.init method");
                    defectInstance.setMessage(gson.toJson(message));
                    checkChanger.defectInstances.add(defectInstance);
                }
            }
        }
        return hasOnCreateMethod;
    }

    /**
     * Create onCreate method or insert init method in onCreate method if there is in Application class of kotlin
     *
     * @param classDeclarationContext class declaration context
     * @param buggyFilePath buggy file path
     * @param checkChanger check changer
     */
    public static void crateOnCreateMethodInKotlin(KotlinParser.ClassDeclarationContext classDeclarationContext,
            String buggyFilePath, AppFileCheckChanger checkChanger) {
        Map<String, Object> message = getMessage();
        boolean hasOnCreateMethod = false;
        KotlinParser.ClassBodyContext classBodyContext
                = classDeclarationContext.getRuleContext(KotlinParser.ClassBodyContext.class, 0);
        KotlinParser.ClassMemberDeclarationsContext classMemberDeclarationsContext
                = classBodyContext.getRuleContext(KotlinParser.ClassMemberDeclarationsContext.class, 0);
        List<KotlinParser.ClassMemberDeclarationContext> classMemberDeclarationContextList
                = classMemberDeclarationsContext.getRuleContexts(KotlinParser.ClassMemberDeclarationContext.class);
        for (KotlinParser.ClassMemberDeclarationContext classMemberDeclarationContext : classMemberDeclarationContextList) {
            KotlinParser.DeclarationContext declarationContext
                    = classMemberDeclarationContext.getRuleContext(KotlinParser.DeclarationContext.class, 0);
            if (declarationContext == null) {
                continue;
            }
            KotlinParser.FunctionDeclarationContext functionDeclarationContext
                    = declarationContext.getRuleContext(KotlinParser.FunctionDeclarationContext.class, 0);
            KotlinParser.SimpleIdentifierContext simpleIdentifierContext
                    = functionDeclarationContext.getRuleContext(KotlinParser.SimpleIdentifierContext.class, 0);
            if (hasOnCreateMethodKotlin(simpleIdentifierContext,
                    buggyFilePath, checkChanger,functionDeclarationContext)) {
                hasOnCreateMethod = true;
                break;
            }
        }

        if (!hasOnCreateMethod) {
            int classStartLine = classBodyContext.getStart().getLine();
            DefectInstance defectInstance;
            if (GlobalSettings.isNeedClassloader()) {
                defectInstance = checkChanger.createDefectInstance(buggyFilePath, -(classStartLine + 1),
                        null, "    override fun onCreate() {\n" +
                                "        super.onCreate()\n" +
                                "        org.xms.g.utils.GlobalEnvSetting.init(this, null)\n" +
                                "        org.xms.adapter.utils.XLoader.init(this)\n" +
                                "    }");
            } else {
                defectInstance = checkChanger.createDefectInstance(buggyFilePath, -(classStartLine + 1),
                        null, "    override fun onCreate() {\n" +
                                "        super.onCreate()\n" +
                                "        org.xms.g.utils.GlobalEnvSetting.init(this, null)\n" +
                                "    }");
            }
            message.put("text", "Add onCreate method from its parent class");
            defectInstance.setMessage(gson.toJson(message));
            checkChanger.defectInstances.add(defectInstance);
        }
    }
}
