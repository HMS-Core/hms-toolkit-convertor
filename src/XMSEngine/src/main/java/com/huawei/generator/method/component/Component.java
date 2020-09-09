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

package com.huawei.generator.method.component;

import static com.huawei.generator.mirror.KClassUtils.getConstructorList;
import static com.huawei.generator.utils.SpecialClasses.isOnlyForWrapping;

import com.huawei.generator.ast.ClassNode;
import com.huawei.generator.ast.NewNode;
import com.huawei.generator.ast.StatementNode;
import com.huawei.generator.ast.TypeNode;
import com.huawei.generator.ast.custom.XClassDoc;
import com.huawei.generator.ast.custom.XFieldDoc;
import com.huawei.generator.ast.custom.XMethodDoc;
import com.huawei.generator.gen.AstConstants;
import com.huawei.generator.json.JClass;
import com.huawei.generator.json.JMapping;
import com.huawei.generator.json.JMethod;
import com.huawei.generator.mirror.KClass;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Class for Component
 *
 * @since 2020-02-19
 */
public abstract class Component {
    private ComponentContainer container;

    // used for some name generation, its value may be "G", "H", "Z", or something else.
    private String identifier;

    protected Component(String identifier) {
        if (identifier.isEmpty()) {
            throw new IllegalArgumentException("Identifier of a component should not be empty");
        }
        this.identifier = identifier;
    }

    public void setContainer(ComponentContainer container) {
        if (this.container != null) {
            throw new IllegalStateException("Components should associate with only one container");
        }
        this.container = container;
    }

    /**
     * zReturn
     *
     * @return name of method return value
     */
    public String retVarName() {
        return identifier.toLowerCase(Locale.ENGLISH) + "Return";
    }

    /**
     * GET_XMS_BY_HMS or GET_XMS_BY_GMS
     *
     * @return method name
     */
    public abstract String getXMethodName();

    /**
     * String of xtoH or xtoG
     *
     * @param xType type of xms
     * @return method name
     */
    public abstract String toZMethodName(String xType);

    /**
     * Parameter list for wrapper constructor
     *
     * @param paramName param name
     * @return list of parameters
     */
    public List<StatementNode> xWrapperParams(String paramName) {
        if (container == null) {
            throw new IllegalArgumentException();
        }
        return Collections.singletonList(
            NewNode.create(TypeNode.create(AstConstants.XMS_BOX), container.xWrapperParams(this, paramName)));
    }

    /**
     * Var name of return object
     *
     * @return gmsObj or hmsObj
     */
    public String zReturn() {
        return identifier.toLowerCase(Locale.ENGLISH) + "msObj";
    }

    /**
     * Check whether a given type name is of Z type.
     *
     * @param typeName type name
     * @return whether it's Z type or not.
     */
    public abstract boolean isZType(String typeName);

    /**
     * convert x type to z type
     *
     * @param xType type of x
     * @return type of z
     */
    public abstract String x2Z(String xType);

    /**
     * getZInstance
     *
     * @return method name
     */
    public String getZInstance() {
        return "get" + identifier.toUpperCase(Locale.ENGLISH) + "Instance";
    }

    /**
     * Whether the component is h.
     *
     * @return return true if is h, otherwise false
     */
    public abstract boolean isH();

    /**
     * Method name of getGmsClassWithXmsClass or getHmsClassWithXmsClass
     *
     * @return method name
     */
    public abstract String getZClassWithXClass();

    /**
     * zObj
     *
     * @return var name
     */
    public String zObj() {
        return identifier.toLowerCase(Locale.ENGLISH) + "Obj";
    }

    /**
     * Converts a Z type to a X type.
     *
     * @param typeName Z type name
     * @return the corresponding X type name
     */
    public abstract String toX(String typeName);

    /**
     * Converts a Z type to a X type.
     *
     * @param zType Z type.
     * @return the corresponding X type.
     */
    public abstract TypeNode toX(TypeNode zType);

    /**
     * ZImpl
     *
     * @return inner class name
     */
    public String zImpl() {
        return identifier.toUpperCase(Locale.ENGLISH) + "Impl";
    }

    /**
     * Get the collection of method to be completed in a non-abstract class, including all methods in this
     * class and all abstract methods inherited from the superclasses, interfaces.
     *
     * @param xNode class node
     * @return List of mapping
     */
    public abstract List<JMapping<JMethod>> wholeMapping(ClassNode xNode);

    /**
     * Whether add method to zimpl
     *
     * @param mapping jmapping
     * @param def jclass
     * @return true if need to add, otherwise false
     */
    public abstract boolean isZImplMethod(JMapping<JMethod> mapping, JClass def);

    /**
     * Whether the mapping has g or h
     *
     * @param mapping jmapping
     * @param def jclass
     * @return true if mapping has z, otherwise false
     */
    public abstract boolean hasZ(JMapping<JMethod> mapping, JClass def);

    /**
     * mapping.g() or mapping.h()
     *
     * @param mapping jMapping
     * @return JMethod of z
     */
    public abstract <T> T jMethod(JMapping<T> mapping);

    /**
     * Get collection of z classes from KClassReader
     *
     * @return Collection of z KClasses
     */
    public abstract Map<String, KClass> getZClassList();

    /**
     * getGType or getHType
     *
     * @param node class node
     * @return Type node of z
     */
    public abstract TypeNode getZType(ClassNode node);

    /**
     * def.g() or def.h()
     *
     * @param def jClass
     * @return z name
     */
    public abstract String zName(JClass def);

    /**
     * Whether the z class is inheritable
     *
     * @param classNode class node
     * @param def jclass
     * @return true if is inheritable, otherwise false
     */
    public boolean isZInheritable(ClassNode classNode, JClass def) {
        return classNode.isInheritable() && !getConstructorList(zName(def), getZClassList()).isEmpty()
            && !isOnlyForWrapping(classNode.longName());
    }

    /**
     * setZInstance
     *
     * @return method name
     */
    public String setZInstance() {
        return "set" + identifier.toUpperCase(Locale.ENGLISH) + "Instance";
    }

    /**
     * Z instance field name in each XObject.
     *
     * @return name of the Z instance field
     */
    public String zInstanceFieldName() {
        return identifier.toLowerCase(Locale.ENGLISH) + "Instance";
    }

    /**
     * Whether the status is matching between x and z
     *
     * @param mapping JMapping
     * @return if matching return true, otherwise false
     */
    public abstract boolean isMatching(JMapping mapping);

    /**
     * Whether the class definition is matching
     *
     * @param def JClass
     * @return true if matching, otherwise false.
     */
    public abstract boolean isMatching(JClass def);

    /**
     * Whether the ZImpl need to-do block
     *
     * @return true if need, otherwise false
     */
    public abstract boolean needToDoBlockInZImpl();

    /**
     * prefix_name of getZInstance method
     *
     * @return prefix_name of getZInstance method
     */
    public abstract String getInstancePrefix();

    /**
     * get the componentContainer
     *
     * @return container
     */
    public ComponentContainer getContainer() {
        return container;
    }

    /**
     * get the gClassName or hClassName used in javadoc
     *
     * @param classDocNode the classDocNode
     * @return gClassName or hClassName
     */
    public abstract String getZClassNameForDoc(XClassDoc classDocNode);

    /**
     * get the gClassInfo or hClassInfo used in javadoc
     *
     * @param classDocNode the classDocNode
     * @return gClassInfo or hClassInfo
     */
    public abstract String getZClassInfoForDoc(XClassDoc classDocNode);

    /**
     * get the gMethodName or hMethodName used in javadoc
     *
     * @param methodDocNode the methodDocNode
     * @return gMethodName or hMethodName
     */
    public abstract String getZMethodNameForDoc(XMethodDoc methodDocNode);

    /**
     * get the gMethodInfo or hMethodInfo used in javadoc
     *
     * @param methodDocNode the methodDocNode
     * @return gMethodInfo or hMethodInfo
     */
    public abstract String getZMethodInfoForDoc(XMethodDoc methodDocNode);

    /**
     * get the attribute of component used in javadoc
     *
     * @return the attribute of component
     */
    public abstract String componentAttribute();

    /**
     * methodDoc's name and information form like 'methodName : <a href=\"https://....">'
     *
     * @param methodDocNode method javadoc node
     * @return method name and info
     */
    public List<String> getMethodDocNameAndInfo(XMethodDoc methodDocNode) {
        String doc = getZMethodNameForDoc(methodDocNode);
        if (doc.isEmpty()) {
            return Collections.EMPTY_LIST;
        }

        List<String> displayInfoList = new LinkedList<>();
        String zMethodInfoForDoc = getZMethodInfoForDoc(methodDocNode);
        if (zMethodInfoForDoc.isEmpty()) {
            displayInfoList.add(" * " + doc);
            return displayInfoList;
        }

        displayInfoList.add(" * " + doc + ": <a href=\"" + zMethodInfoForDoc + "\">" + zMethodInfoForDoc + "</a><br/>");
        return displayInfoList;
    }

    // classDoc's form like 'className : classInfo'
    public String classNameAndInfo(XClassDoc node) {
        return getZClassNameForDoc(node) + ": " + getZClassInfoForDoc(node);
    }

    /**
     * get the information of fieldDoc
     *
     * @param fieldDoc the fieldDoc
     * @return the information of fieldDoc
     */
    public abstract String getFieldInfo(XFieldDoc fieldDoc);
}
