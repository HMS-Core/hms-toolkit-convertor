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

package com.huawei.codebot.analyzer.x2y.java.other.complexchanger;

import com.huawei.codebot.analyzer.x2y.io.config.ConfigService;
import com.huawei.codebot.analyzer.x2y.java.other.specificchanger.SpecificJsonPattern;
import com.huawei.codebot.analyzer.x2y.java.other.specificchanger.SpecificModificationChanger;
import com.huawei.codebot.codeparsing.java.JavaFile;
import com.huawei.codebot.codeparsing.java.JavaFileAnalyzer;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;
import com.huawei.codebot.framework.model.DefectInstance;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A changer used to process Complex Specific Modification.
 *
 * @since 2020-04-17
 */
public class ComplexSpecificModificationChanger extends SpecificModificationChanger {
    /**
     * initialize fixActions
     */
    protected Map<String, FixAction> fixActions = new HashMap<String, FixAction>();

    public ComplexSpecificModificationChanger(String fixerType) throws CodeBotRuntimeException {
        ConfigService configService = ConfigService.getInstance(fixerType);
        fixActions = configService.getComplexSpecificPatterns();
        SpecificJsonPattern specificJsonPattern = new SpecificJsonPattern();
        initConfig(specificJsonPattern);
    }

    @Override
    protected List<DefectInstance> detectDefectsInJavaFile(String buggyFilePath) {
        FixAction addImport = fixActions.get("importDeclaration" + "add" + "import");
        FixAction addJosAppsClient = fixActions.get("functionDeclaration" + "add" + "onCreate");
        List<DefectInstance> defectInstances = new ArrayList<>();
        JavaFileAnalyzer codeAnalyzer = new JavaFileAnalyzer();
        JavaFile javaFile = codeAnalyzer.extractJavaFileInfo(buggyFilePath);
        List<NodeDescription> nodeDes = new ArrayList<NodeDescription>();
        CompilationUnit cu = javaFile.compilationUnit;
        for (Object im : cu.imports()) {
            if (im instanceof ImportDeclaration) {
                ImportDeclaration imd = (ImportDeclaration) im;
                NodeDescription nodeDescription = new NodeDescription();
                nodeDescription.setNodeType("import");
                nodeDescription.setNodeContent("import " + imd.getName().toString() + ";");
                nodeDescription.setStartLine(javaFile.compilationUnit.getLineNumber(imd.getStartPosition()));
                nodeDescription.setEndLine(javaFile.compilationUnit.getLineNumber(imd.getStartPosition() + imd.getLength()));
                nodeDes.add(nodeDescription);
            }
        }

        List types = cu.types();
        if (types.get(0) instanceof TypeDeclaration) {
            TypeDeclaration typeDec = (TypeDeclaration) types.get(0);
            MethodDeclaration[] methodDecs = typeDec.getMethods();
            for (MethodDeclaration method : methodDecs) {
                SimpleName methodName = method.getName();
                List params = method.parameters();
                if (isTargetFunctionDeclaration(methodName.toString()) && isTargetFunctionParams(params)) {
                    method.getBody()
                            .accept(
                                    new ASTVisitor() {
                                        @Override
                                        public boolean visit(SuperMethodInvocation node) {
                                            if ("onCreate".equals(node.getName().toString())) {
                                                if (addJosAppsClient != null) {
                                                    int nodeEndLine =
                                                            cu.getLineNumber(
                                                                    node.getStartPosition() + node.getLength());
                                                    DefectInstance addMethoddefectInstance =
                                                            createDefectInstance(
                                                                    buggyFilePath,
                                                                    -(nodeEndLine + 1),
                                                                    null,
                                                                    addJosAppsClient.getNewContent());
                                                    addMethoddefectInstance.setMessage(addJosAppsClient.getDesc());
                                                    addMethoddefectInstance.isFixed = true;
                                                    defectInstances.add(addMethoddefectInstance);
                                                }
                                                if (addImport != null) {
                                                    DefectInstance addImportdefectInstance =
                                                            createDefectInstance(
                                                                    buggyFilePath,
                                                                    -(nodeDes.get(nodeDes.size() - 1).getEndLine() + 1),
                                                                    null,
                                                                    addImport.getNewContent());
                                                    addImportdefectInstance.setMessage(addImport.getDesc());
                                                    addImportdefectInstance.isFixed = true;
                                                    defectInstances.add(addImportdefectInstance);
                                                }
                                            }

                                            return super.visit(node);
                                        }
                                    });
                }
            }
        }
        removeIgnoreBlocks(defectInstances, javaFile.shielder);
        return defectInstances;
    }

    /**
     * judege target function declaration
     *
     * @param funcName target function name
     * @return determine whether it is target function declaration
     */
    public static Boolean isTargetFunctionDeclaration(String funcName) {
        if ("onCreate".equals(funcName)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * judge target function params
     */
    public static Boolean isTargetFunctionParams(List params) {
        if (params.size() == 1) {
            if (params.get(0) instanceof SingleVariableDeclaration) {
                SingleVariableDeclaration declarationParam = (SingleVariableDeclaration) params.get(0);
                if ("Bundle".equals(declarationParam.getType().toString())) {
                    return true;
                }
            }
        }
        return false;
    }
}
