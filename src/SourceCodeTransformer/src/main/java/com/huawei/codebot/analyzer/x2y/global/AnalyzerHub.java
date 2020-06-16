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

import com.huawei.codebot.analyzer.x2y.global.bean.ClassInfo;
import com.huawei.codebot.analyzer.x2y.global.bean.FieldInfo;
import com.huawei.codebot.analyzer.x2y.global.bean.MethodInfo;
import com.huawei.codebot.analyzer.x2y.global.java.ClassMemberAnalyzer;
import com.huawei.codebot.analyzer.x2y.global.service.ClassMemberService;
import com.huawei.codebot.codeparsing.java.JavaFileAnalyzer;
import com.huawei.codebot.framework.api.CodeBotResultCode;
import com.huawei.codebot.framework.exception.CodeBotFileException;
import com.huawei.codebot.framework.exception.CodeBotRuntimeException;
import com.huawei.codebot.framework.parser.kotlin.KotlinLexer;
import com.huawei.codebot.framework.parser.kotlin.KotlinParser;
import com.huawei.codebot.framework.parser.kotlin.KotlinParserBaseListener;
import com.huawei.codebot.utils.FileUtils;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.Dimension;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ExportsDirective;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.IntersectionType;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.ModuleDeclaration;
import org.eclipse.jdt.core.dom.ModuleModifier;
import org.eclipse.jdt.core.dom.NameQualifiedType;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.OpensDirective;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.ProvidesDirective;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.RequiresDirective;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.UsesDirective;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * A hub embedding one or more analyzer to analyze a list of files specified by file paths
 *
 * @since 2019-07-14
 */
public class AnalyzerHub extends Observable {
    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyzerHub.class);
    private static AnalyzerHub analyzerHub;
    private int javaFileNum = 0;
    private int javaTotalLineNum = 0;
    private int kotlinFileNum = 0;
    private int kotlinTotalLineNum = 0;

    private AnalyzerHub() throws CodeBotRuntimeException {
        super();
        Map<String, ClassInfo> classInfoMap = ClassMemberService.getInstance().getClassInfoMap();
        Map<String, FieldInfo> fieldInfoMap = ClassMemberService.getInstance().getFieldInfoMap();
        Map<String, List<MethodInfo>> methodInfoMap = ClassMemberService.getInstance().getMethodInfoMap();
        Map<String, List<Object>> offlineRecord = null;
        try {
            ClassLoader classloader = ClassMemberAnalyzer.class.getClassLoader();
            InputStream is = classloader.getResourceAsStream("data/android.api");
            if (is != null) {
                SecureObjectInputStream in = new SecureObjectInputStream(is);
                offlineRecord = (Map<String, List<Object>>) in.readObject();
                in.close();
                is.close();
            }
        } catch (IOException | ClassNotFoundException e1) {
            throw new CodeBotRuntimeException(CodeBotResultCode.FILE_OPERATION, "fail to read android.api", null, e1);
        }

        if (offlineRecord != null) {
            List<Object> classInfos = offlineRecord.get("ClassInfo");
            if (classInfos != null) {
                for (Object object : classInfos) {
                    if (object instanceof ClassInfo) {
                        ClassInfo classInfo = (ClassInfo) object;
                        classInfoMap.put(classInfo.getQualifiedName(), classInfo);
                    }
                }
                LOGGER.info("{} classInfos have been loaded.", classInfos.size());
            }
            List<Object> methodInfos = offlineRecord.get("MethodInfo");
            if (methodInfos != null) {
                for (Object object : methodInfos) {
                    if (object instanceof MethodInfo) {
                        MethodInfo methodInfo = (MethodInfo) object;
                        methodInfoMap.computeIfAbsent(methodInfo.getQualifiedName(), element -> new ArrayList<>())
                            .add(methodInfo);
                    }
                }
                LOGGER.info("{} methodInfos have been loaded.", methodInfos.size());
            }

            List<Object> fieldInfos = offlineRecord.get("FieldInfo");
            if (fieldInfos != null) {
                for (Object object : fieldInfos) {
                    if (object instanceof FieldInfo) {
                        FieldInfo fieldInfo = (FieldInfo) object;
                        fieldInfoMap.put(fieldInfo.getQualifiedName(), fieldInfo);
                    }
                }
                LOGGER.info("{} fieldInfos have been loaded.", fieldInfos.size());
            }
        }
    }

    /**
     * @return instance of AnalyzerHub
     * @throws CodeBotRuntimeException
     */
    public static synchronized AnalyzerHub getInstance() throws CodeBotRuntimeException {
        if (analyzerHub == null) {
            analyzerHub = new AnalyzerHub();
        }
        return analyzerHub;
    }

    /**
     * @param filePaths paths of files which need to be analyzed
     * @throws CodeBotFileException
     */
    public void analyze(List<String> filePaths) throws CodeBotFileException {
        for (String filePath : filePaths) {
            String codeContent;
            try {
                codeContent = FileUtils.getFileContent(filePath);
            } catch (IOException ex) {
                throw new CodeBotFileException("can not get file content of " + filePath, null, ex);
            }
            List<String> fileLines = FileUtils.cutStringToList(codeContent);
            if (filePath.endsWith(".java")) {
                javaTotalLineNum += fileLines.size();
                final CompilationUnit cu = JavaFileAnalyzer.generateAST(codeContent);
                UniqueVisitor visitor = new UniqueVisitor();
                cu.accept(visitor);
                javaFileNum++;
            } else if (filePath.endsWith(".kt")) {
                kotlinTotalLineNum += fileLines.size();
                KotlinLexer kotlinLexer = new KotlinLexer(CharStreams.fromString(codeContent));
                CommonTokenStream commonTokenStream = new CommonTokenStream(kotlinLexer);
                KotlinParser kotlinParser = new KotlinParser(commonTokenStream);
                KotlinParser.KotlinFileContext tree = kotlinParser.kotlinFile();
                ParseTreeWalker walker = new ParseTreeWalker();
                GlobalKotlinListener listener = new GlobalKotlinListener();
                walker.walk(listener, tree);
                kotlinFileNum++;
            }
        }
    }

    /**
     * analyzeByMultiThread
     *
     * @param filePaths        paths of files which need to be analyzed
     * @param threadNum        number of threads to launch parallelism
     * @param threadExeFileNum files in each group
     */
    public void analyzeByMultiThread(List<String> filePaths, int threadNum, int threadExeFileNum) {
        // 2 dimensions to 3 dimensions
        List<List<String>> spliceList = spliceListByStep(filePaths, threadExeFileNum);
        List<Callable<Boolean>> threadList = new ArrayList<>();
        for (List<String> fileList : spliceList) {
            threadList.add(() -> {
                // analyze each group of files
                analyze(fileList);
                return true;
            });
        }

        LOGGER.info("execute thread amount is {}", threadList.size());

        long start = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);
        int normalFinish = 0;
        try {
            List<Future<Boolean>> futures = executorService.invokeAll(threadList);
            for (Future<Boolean> future : futures) {
                try {
                    future.get();
                    normalFinish++;
                } catch (ExecutionException e) {
                    LOGGER.error("execute exception", e);
                }
            }
        } catch (InterruptedException e) {
            LOGGER.error("execute invoke exception", e);
        } finally {
            executorService.shutdown();
        }
        long end = System.currentTimeMillis();
        LOGGER.info("execute thread end, normal finished size is {}, cost time {}s",
                normalFinish, ((end - start) * 1.0 / 1000));
    }

    private <T> List<List<T>> spliceListByStep(List<T> fileList, int stepLen) {
        List<List<T>> fileListLs = new ArrayList<List<T>>();

        if (CollectionUtils.isEmpty(fileList)) {
            return fileListLs;
        }
        if (stepLen <= 0) {
            fileListLs.add(fileList);
            return fileListLs;
        }

        int step = 0;
        while (true) {
            int currIndex = step * stepLen;
            int nextIndex = (step + 1) * stepLen;

            if (nextIndex >= fileList.size()) {
                fileListLs.add(fileList.subList(currIndex, fileList.size()));
                break;
            }
            fileListLs.add(fileList.subList(currIndex, nextIndex));
            step++;
        }
        return fileListLs;
    }

    /**
     * Notice Analyzers' postAnalyze method and log some data.
     */
    public void postAnalyze() {
        setChanged();
        notifyObservers();
        LOGGER.info("Total java files: {}", javaFileNum);
        LOGGER.info("Total java lines: {}", javaTotalLineNum);
        LOGGER.info("Total kotlin files: {}", kotlinFileNum);
        LOGGER.info("Total kotlin lines: {}", kotlinTotalLineNum);
    }

    private class GlobalKotlinListener extends KotlinParserBaseListener {
        @Override
        public void enterKotlinFile(KotlinParser.KotlinFileContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterScript(KotlinParser.ScriptContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterFileAnnotation(KotlinParser.FileAnnotationContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterPackageHeader(KotlinParser.PackageHeaderContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterImportList(KotlinParser.ImportListContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterImportHeader(KotlinParser.ImportHeaderContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterImportAlias(KotlinParser.ImportAliasContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterTopLevelObject(KotlinParser.TopLevelObjectContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterClassDeclaration(KotlinParser.ClassDeclarationContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterPrimaryConstructor(KotlinParser.PrimaryConstructorContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterClassParameters(KotlinParser.ClassParametersContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterClassParameter(KotlinParser.ClassParameterContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterDelegationSpecifiers(KotlinParser.DelegationSpecifiersContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterDelegationSpecifier(KotlinParser.DelegationSpecifierContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterConstructorInvocation(KotlinParser.ConstructorInvocationContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterExplicitDelegation(KotlinParser.ExplicitDelegationContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterClassBody(KotlinParser.ClassBodyContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterClassMemberDeclaration(KotlinParser.ClassMemberDeclarationContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterAnonymousInitializer(KotlinParser.AnonymousInitializerContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterSecondaryConstructor(KotlinParser.SecondaryConstructorContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterConstructorDelegationCall(KotlinParser.ConstructorDelegationCallContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterEnumClassBody(KotlinParser.EnumClassBodyContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterEnumEntries(KotlinParser.EnumEntriesContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterEnumEntry(KotlinParser.EnumEntryContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterFunctionDeclaration(KotlinParser.FunctionDeclarationContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterFunctionValueParameters(KotlinParser.FunctionValueParametersContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterFunctionValueParameter(KotlinParser.FunctionValueParameterContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterParameter(KotlinParser.ParameterContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterFunctionBody(KotlinParser.FunctionBodyContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterObjectDeclaration(KotlinParser.ObjectDeclarationContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterCompanionObject(KotlinParser.CompanionObjectContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterPropertyDeclaration(KotlinParser.PropertyDeclarationContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterMultiVariableDeclaration(KotlinParser.MultiVariableDeclarationContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterVariableDeclaration(KotlinParser.VariableDeclarationContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterGetter(KotlinParser.GetterContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterSetter(KotlinParser.SetterContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterTypeAlias(KotlinParser.TypeAliasContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterTypeParameters(KotlinParser.TypeParametersContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterTypeParameter(KotlinParser.TypeParameterContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterType(KotlinParser.TypeContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterParenthesizedType(KotlinParser.ParenthesizedTypeContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterNullableType(KotlinParser.NullableTypeContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterTypeReference(KotlinParser.TypeReferenceContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterFunctionType(KotlinParser.FunctionTypeContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterUserType(KotlinParser.UserTypeContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterSimpleUserType(KotlinParser.SimpleUserTypeContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterFunctionTypeParameters(KotlinParser.FunctionTypeParametersContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterTypeConstraints(KotlinParser.TypeConstraintsContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterTypeConstraint(KotlinParser.TypeConstraintContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterBlock(KotlinParser.BlockContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterStatements(KotlinParser.StatementsContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterStatement(KotlinParser.StatementContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterDeclaration(KotlinParser.DeclarationContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterAssignment(KotlinParser.AssignmentContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterExpression(KotlinParser.ExpressionContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterDisjunction(KotlinParser.DisjunctionContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterConjunction(KotlinParser.ConjunctionContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterEquality(KotlinParser.EqualityContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterComparison(KotlinParser.ComparisonContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterInfixOperation(KotlinParser.InfixOperationContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterElvisExpression(KotlinParser.ElvisExpressionContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterInfixFunctionCall(KotlinParser.InfixFunctionCallContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterRangeExpression(KotlinParser.RangeExpressionContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterAdditiveExpression(KotlinParser.AdditiveExpressionContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterMultiplicativeExpression(KotlinParser.MultiplicativeExpressionContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterAsExpression(KotlinParser.AsExpressionContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterPrefixUnaryExpression(KotlinParser.PrefixUnaryExpressionContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterPostfixUnaryExpression(KotlinParser.PostfixUnaryExpressionContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterShebangLine(KotlinParser.ShebangLineContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterAnnotatedDelegationSpecifier(KotlinParser.AnnotatedDelegationSpecifierContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterClassMemberDeclarations(KotlinParser.ClassMemberDeclarationsContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterPropertyDelegate(KotlinParser.PropertyDelegateContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterParametersWithOptionalType(KotlinParser.ParametersWithOptionalTypeContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterParameterWithOptionalType(KotlinParser.ParameterWithOptionalTypeContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterQuest(KotlinParser.QuestContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterTypeProjectionModifiers(KotlinParser.TypeProjectionModifiersContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterTypeProjectionModifier(KotlinParser.TypeProjectionModifierContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterReceiverType(KotlinParser.ReceiverTypeContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterParenthesizedUserType(KotlinParser.ParenthesizedUserTypeContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterLabel(KotlinParser.LabelContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterLoopStatement(KotlinParser.LoopStatementContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterForStatement(KotlinParser.ForStatementContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterWhileStatement(KotlinParser.WhileStatementContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterDoWhileStatement(KotlinParser.DoWhileStatementContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterSemis(KotlinParser.SemisContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterElvis(KotlinParser.ElvisContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterUnaryPrefix(KotlinParser.UnaryPrefixContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterPostfixUnarySuffix(KotlinParser.PostfixUnarySuffixContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterDirectlyAssignableExpression(KotlinParser.DirectlyAssignableExpressionContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterParenthesizedDirectlyAssignableExpression(
                KotlinParser.ParenthesizedDirectlyAssignableExpressionContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterParenthesizedAssignableExpression(KotlinParser.ParenthesizedAssignableExpressionContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterAssignableSuffix(KotlinParser.AssignableSuffixContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterIndexingSuffix(KotlinParser.IndexingSuffixContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterNavigationSuffix(KotlinParser.NavigationSuffixContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterLambdaLiteral(KotlinParser.LambdaLiteralContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterAnonymousFunction(KotlinParser.AnonymousFunctionContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterWhenSubject(KotlinParser.WhenSubjectContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterAssignmentAndOperator(KotlinParser.AssignmentAndOperatorContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterExcl(KotlinParser.ExclContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterSafeNav(KotlinParser.SafeNavContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterModifiers(KotlinParser.ModifiersContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterParameterModifiers(KotlinParser.ParameterModifiersContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterTypeModifiers(KotlinParser.TypeModifiersContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterTypeModifier(KotlinParser.TypeModifierContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterVarianceModifier(KotlinParser.VarianceModifierContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterTypeParameterModifiers(KotlinParser.TypeParameterModifiersContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterReificationModifier(KotlinParser.ReificationModifierContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterPlatformModifier(KotlinParser.PlatformModifierContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterSingleAnnotation(KotlinParser.SingleAnnotationContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterMultiAnnotation(KotlinParser.MultiAnnotationContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterAssignableExpression(KotlinParser.AssignableExpressionContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterCallSuffix(KotlinParser.CallSuffixContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterAnnotatedLambda(KotlinParser.AnnotatedLambdaContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterValueArguments(KotlinParser.ValueArgumentsContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterTypeArguments(KotlinParser.TypeArgumentsContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterTypeProjection(KotlinParser.TypeProjectionContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterValueArgument(KotlinParser.ValueArgumentContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterPrimaryExpression(KotlinParser.PrimaryExpressionContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterParenthesizedExpression(KotlinParser.ParenthesizedExpressionContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterLiteralConstant(KotlinParser.LiteralConstantContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterStringLiteral(KotlinParser.StringLiteralContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterLineStringLiteral(KotlinParser.LineStringLiteralContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterMultiLineStringLiteral(KotlinParser.MultiLineStringLiteralContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterLineStringContent(KotlinParser.LineStringContentContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterLineStringExpression(KotlinParser.LineStringExpressionContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterMultiLineStringContent(KotlinParser.MultiLineStringContentContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterMultiLineStringExpression(KotlinParser.MultiLineStringExpressionContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterFunctionLiteral(KotlinParser.FunctionLiteralContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterLambdaParameters(KotlinParser.LambdaParametersContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterLambdaParameter(KotlinParser.LambdaParameterContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterObjectLiteral(KotlinParser.ObjectLiteralContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterCollectionLiteral(KotlinParser.CollectionLiteralContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterThisExpression(KotlinParser.ThisExpressionContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterSuperExpression(KotlinParser.SuperExpressionContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterIfExpression(KotlinParser.IfExpressionContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterControlStructureBody(KotlinParser.ControlStructureBodyContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterWhenExpression(KotlinParser.WhenExpressionContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterWhenEntry(KotlinParser.WhenEntryContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterWhenCondition(KotlinParser.WhenConditionContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterRangeTest(KotlinParser.RangeTestContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterTypeTest(KotlinParser.TypeTestContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterTryExpression(KotlinParser.TryExpressionContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterCatchBlock(KotlinParser.CatchBlockContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterFinallyBlock(KotlinParser.FinallyBlockContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterJumpExpression(KotlinParser.JumpExpressionContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterCallableReference(KotlinParser.CallableReferenceContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterEqualityOperator(KotlinParser.EqualityOperatorContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterComparisonOperator(KotlinParser.ComparisonOperatorContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterInOperator(KotlinParser.InOperatorContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterIsOperator(KotlinParser.IsOperatorContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterAdditiveOperator(KotlinParser.AdditiveOperatorContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterMultiplicativeOperator(KotlinParser.MultiplicativeOperatorContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterAsOperator(KotlinParser.AsOperatorContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterPrefixUnaryOperator(KotlinParser.PrefixUnaryOperatorContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterPostfixUnaryOperator(KotlinParser.PostfixUnaryOperatorContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterMemberAccessOperator(KotlinParser.MemberAccessOperatorContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterModifier(KotlinParser.ModifierContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterClassModifier(KotlinParser.ClassModifierContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterMemberModifier(KotlinParser.MemberModifierContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterVisibilityModifier(KotlinParser.VisibilityModifierContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterFunctionModifier(KotlinParser.FunctionModifierContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterPropertyModifier(KotlinParser.PropertyModifierContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterInheritanceModifier(KotlinParser.InheritanceModifierContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterParameterModifier(KotlinParser.ParameterModifierContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterTypeParameterModifier(KotlinParser.TypeParameterModifierContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterAnnotation(KotlinParser.AnnotationContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterAnnotationUseSiteTarget(KotlinParser.AnnotationUseSiteTargetContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterUnescapedAnnotation(KotlinParser.UnescapedAnnotationContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterIdentifier(KotlinParser.IdentifierContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterSimpleIdentifier(KotlinParser.SimpleIdentifierContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterSemi(KotlinParser.SemiContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }

        @Override
        public void enterEveryRule(ParserRuleContext ctx) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(ctx);
        }
    }

    private class UniqueVisitor extends ASTVisitor {
        @Override
        public boolean visit(AnnotationTypeDeclaration node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(AnnotationTypeMemberDeclaration node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(AnonymousClassDeclaration node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(ArrayAccess node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(ArrayCreation node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(ArrayInitializer node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(ArrayType node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(AssertStatement node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(Assignment node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(Block node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(BlockComment node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(BooleanLiteral node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(BreakStatement node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(CastExpression node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(CatchClause node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(CharacterLiteral node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(ClassInstanceCreation node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(CompilationUnit node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(ConditionalExpression node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(ConstructorInvocation node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(ContinueStatement node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(CreationReference node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(Dimension node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(DoStatement node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(EmptyStatement node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(EnhancedForStatement node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(EnumConstantDeclaration node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(EnumDeclaration node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(ExportsDirective node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(ExpressionMethodReference node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(ExpressionStatement node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(FieldAccess node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(FieldDeclaration node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(ForStatement node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(IfStatement node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(ImportDeclaration node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(InfixExpression node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(Initializer node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(InstanceofExpression node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(IntersectionType node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(Javadoc node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(LabeledStatement node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(LambdaExpression node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(LineComment node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(MarkerAnnotation node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(MemberRef node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(MemberValuePair node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(MethodRef node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(MethodRefParameter node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(MethodDeclaration node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(MethodInvocation node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(Modifier node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(ModuleDeclaration node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(ModuleModifier node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(NameQualifiedType node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(NormalAnnotation node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(NullLiteral node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(NumberLiteral node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(OpensDirective node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(PackageDeclaration node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(ParameterizedType node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(ParenthesizedExpression node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(PostfixExpression node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(PrefixExpression node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(ProvidesDirective node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(PrimitiveType node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(QualifiedName node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(QualifiedType node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(RequiresDirective node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(ReturnStatement node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(SimpleName node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(SimpleType node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(SingleMemberAnnotation node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(SingleVariableDeclaration node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(StringLiteral node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(SuperConstructorInvocation node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(SuperFieldAccess node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(SuperMethodInvocation node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(SuperMethodReference node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(SwitchCase node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(SwitchStatement node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(SynchronizedStatement node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(TagElement node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(TextElement node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(ThisExpression node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(ThrowStatement node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(TryStatement node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(TypeDeclaration node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(TypeDeclarationStatement node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(TypeLiteral node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(TypeMethodReference node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(TypeParameter node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(UnionType node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(UsesDirective node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(VariableDeclarationExpression node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(VariableDeclarationStatement node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(VariableDeclarationFragment node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(WhileStatement node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }

        @Override
        public boolean visit(WildcardType node) {
            AnalyzerHub.this.setChanged();
            AnalyzerHub.this.notifyObservers(node);
            return super.visit(node);
        }
    }

}
