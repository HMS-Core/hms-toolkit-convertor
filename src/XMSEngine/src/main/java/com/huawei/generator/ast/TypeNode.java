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

package com.huawei.generator.ast;

import static com.huawei.generator.gen.AstConstants.GENERIC_PREFIX;

import com.huawei.generator.gen.classes.WithoutReplacementClasses;
import com.huawei.generator.utils.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This is the TypeNode class.
 *
 * @since 2019-11-16
 */
public class TypeNode extends AstNode {
    private static final Logger LOGGER = LoggerFactory.getLogger(TypeNode.class);

    public static final TypeNode OBJECT_TYPE = TypeNode.create("java.lang.Object");

    // fully qualified type name, such as "java.lang.String"
    private String typeName;

    // simple name, without package, such as "String"
    private String typeNameWithoutPackage;

    // generic types associated with this type name, eg: genericType of "Map<K, V>" is ["K", "V"]
    private List<TypeNode> genericType;

    // generic type definitions associated with method return type, eg:
    // a function defined as "public static <T> T f(T t)"
    // has a return type of "<T> T", where "<T>" is its defTypes
    private List<TypeNode> defTypes;

    private List<TypeNode> superClass;

    private List<TypeNode> infClass;

    private TypeNode outerType;

    private boolean eraseGeneric;

    // if this type is an array, then this field indicates its array dimension, otherwise dimension = 0.
    private int dimension;

    // the paramter maybe a vararg, the typeName will endswith ...
    private boolean varArg;

    private TypeNode(String typeName, List<TypeNode> genericType, List<TypeNode> defTypes, List<TypeNode> superClass,
        List<TypeNode> infClass, int dimension) {
        this.typeName = typeName;
        this.genericType = genericType;
        this.defTypes = defTypes;
        this.superClass = superClass;
        this.infClass = infClass;
        this.eraseGeneric = false;
        this.dimension = dimension;
        this.setTypeName(typeName);
    }

    private TypeNode setTypeName(String typeName) {
        this.typeName = typeName;
        if (typeName != null && typeName.contains(".")) {
            this.typeNameWithoutPackage = typeName.substring(typeName.lastIndexOf(".") + 1);
        } else {
            this.typeNameWithoutPackage = typeName;
        }
        return this;
    }

    public String getTypeNameWithoutPackage() {
        return typeNameWithoutPackage;
    }

    private TypeNode setEraseGeneric(boolean eraseGeneric) {
        this.eraseGeneric = eraseGeneric;
        return this;
    }

    public String getTypeName() {
        return typeName;
    }

    public List<TypeNode> getSuperClass() {
        return superClass;
    }

    public List<TypeNode> getInfClass() {
        return infClass;
    }

    public List<TypeNode> getGenericType() {
        return genericType;
    }

    public List<TypeNode> getDefTypes() {
        return defTypes;
    }

    private void setOuterType(TypeNode outerType) {
        this.outerType = outerType;
    }

    /**
     * @return type node
     */
    public TypeNode getOuterType() {
        return this.outerType;
    }

    /**
     * Change generic definitions of type node.
     * This will make a generic definition into generic instantiation.
     *
     * @param genericType, generics used in type node.
     */
    public TypeNode setGenericType(List<TypeNode> genericType) {
        this.genericType = genericType;
        return this;
    }

    /**
     * Add a prefix to type name, in order to mangle the generic
     * definition.
     *
     * @param prefix, add a prefix to type name.
     * @return self
     */
    TypeNode addPrefix(String prefix) {
        this.typeName = prefix + this.typeName;
        return this;
    }

    /**
     * Deep copy. Make a copy of original TypeNode.
     * TypeNode may be transformed when running, so, we must
     * make a new TypeNode from old TypeNode's full name.
     *
     * @return the copy.
     */
    public TypeNode deepClone() {
        boolean erase = this.eraseGeneric;
        setEraseGeneric(false);
        String res = "";
        if (this.getDefTypes() != null) {
            res += typeListToString(this.getDefTypes()) + " ";
        }
        res += this.toString();
        TypeNode newNode = TypeNode.create(res, erase);
        this.setEraseGeneric(erase);
        return newNode;
    }

    /**
     * For org.xms.Map<R extends Integer>, this method returns
     * "org.xms.Map<R>"
     *
     * @return a type name for instance not definition.
     */
    public String getInstanceName() {
        if (this.getGenericType() != null) {
            List<String> sb = new ArrayList<>(4);
            for (TypeNode tn : this.getGenericType()) {
                sb.add(tn.getTypeName());
            }
            return getTypeName() + "<" + String.join(", ", sb) + ">";
        }

        return getTypeName();
    }

    @Override
    public void accept(AstVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        if (outerType != null) {
            result = new StringBuilder(outerType.toString() + ".");
        }

        result.append(typeName);
        if (getGenericType() != null && !eraseGeneric) {
            result.append(typeListToString(getGenericType()));
        }

        if (getSuperClass() != null) {
            result.append(" extends ").append(typeListToString(getSuperClass(), " & ", false));
        }

        if (getInfClass() != null) {
            result.append(" super ").append(typeListToString(getInfClass(), " & ", false));
        }

        for (int i = 0; i < dimension; i++) {
            result.append("[]");
        }

        if (varArg) {
            result.append("...");
        }
        return result.toString();
    }

    private static String typeListToString(List<TypeNode> genericTypes, String delimiter, boolean needBrace) {
        List<String> result = new ArrayList<>();
        for (TypeNode n : genericTypes) {
            result.add(n.toString());
        }

        if (needBrace) {
            return "<" + String.join(delimiter, result) + ">";
        } else {
            return String.join(delimiter, result);
        }
    }

    static String typeListToString(List<TypeNode> genericTypes) {
        return TypeNode.typeListToString(genericTypes, ", ", true);
    }

    public TypeNode toX() {
        this.typeName = toX(typeName);
        this.typeNameWithoutPackage = toX(typeNameWithoutPackage);

        if (defTypes != null) {
            for (TypeNode t : defTypes) {
                t.addPrefix(GENERIC_PREFIX);
                t.toX();
            }
        }

        if (superClass != null) {
            for (TypeNode t : superClass) {
                t.toX();
            }
        }

        if (infClass != null) {
            for (TypeNode t : infClass) {
                t.toX();
            }
        }

        if (this.genericType != null) {
            for (TypeNode t : genericType) {
                t.toX();
            }
        }

        if (this.outerType != null) {
            this.outerType.toX();
        }

        return this;
    }

    public TypeNode toXWithGenerics(List<TypeNode> genericDefs) {
        return this.toX().renameGenericInstantiations(genericDefs);
    }

    public boolean isVarArg() {
        return varArg;
    }

    /**
     * Dimension of the array type.
     *
     * @return dimension of this array type
     */
    public int dimension() {
        return dimension;
    }

    /**
     * Sets this type node to an array type with a given dimension.
     *
     * @param dimen dimension of this array type
     * @return The same TypeNode instance
     */
    public TypeNode setDimension(int dimen) {
        dimension = dimen;
        return this;
    }

    private static class Parser {
        byte[] name;

        String strName;

        int index = 0;

        Token current;

        enum TokenType {
            NAME,
            LEFT,
            SEMI,
            RIGHT,
            AND,
            ELLIPSIS,
            LB,     // left bracket "["
            RB      // right bracket "]"
        }

        private static class Token {
            public String value;

            public TokenType type;

            Token(String value, TokenType type) {
                this.value = value;
                this.type = type;
            }
        }

        Parser(String name) {
            this.strName = name;
            this.name = name.getBytes(StandardCharsets.UTF_8);
            this.current = nextToken();
        }

        private boolean isAlpha(byte b) {
            return (b != '<') && (b != '>') && (b != ' ') && (b != ',') && (b != '&') && (b != '.') && (b != '[')
                && (b != ']');
        }

        private Token specialChar() {
            Token t = null;
            switch (name[index]) {
                case '<':
                    t = new Token(null, TokenType.LEFT);
                    break;
                case '>':
                    t = new Token(null, TokenType.RIGHT);
                    break;
                case ',':
                    t = new Token(null, TokenType.SEMI);
                    break;
                case '&':
                    t = new Token(null, TokenType.AND);
                    break;
                case '.':
                    if (name[index + 1] != '.' || name[index + 2] != '.') {
                        throw new IllegalStateException("unexpected token");
                    }
                    index += 3;
                    t = new Token(null, TokenType.ELLIPSIS);
                    break;
                case '[':
                    t = new Token(null, TokenType.LB);
                    break;
                case ']':
                    t = new Token(null, TokenType.RB);
                    break;
                default:
                    LOGGER.error("Unrecognized character: " + name[index]);
            }
            index++;
            return t;
        }

        private Token nextToken() {
            if (index >= name.length) {
                return null;
            }

            while (index < name.length && name[index] == ' ') {
                index++;
            }

            if (isAlpha(name[index])) {
                StringBuilder sb = new StringBuilder();
                while (index < name.length) {
                    if (isAlpha(name[index]) || (name[index] == '.' && isAlpha(name[index + 1]))) {
                        sb.append((char) name[index++]);
                    } else {
                        return new Token(sb.toString(), TokenType.NAME);
                    }
                }
                return new Token(sb.toString(), TokenType.NAME);
            } else {
                return specialChar();
            }
        }

        boolean matchOp(TokenType tokenType) {
            if (current == null || current.type != tokenType) {
                return false;
            }
            current = nextToken();
            return true;
        }

        String matchName() {
            if (current == null || current.type != TokenType.NAME) {
                return "";
            }
            String className = current.value;
            current = nextToken();
            return className;
        }

        public TypeNode parse() {
            List<TypeNode> defTypes = null;
            List<TypeNode> genericTypes = null;
            List<TypeNode> superClass = null;
            List<TypeNode> infClass = null;
            String temp;
            int dimension = 0;

            if (matchOp(TokenType.LEFT)) {
                defTypes = new ArrayList<>();
                TypeNode n = parse();
                defTypes.add(n);
                while (matchOp(TokenType.SEMI)) {
                    n = parse();
                    defTypes.add(n);
                }
                matchOp(TokenType.RIGHT);
            }

            String className = matchName();

            if (matchOp(TokenType.LEFT)) {
                genericTypes = new ArrayList<>();
                TypeNode n = parse();
                genericTypes.add(n);
                while (matchOp(TokenType.SEMI)) {
                    n = parse();
                    genericTypes.add(n);
                }
                matchOp(TokenType.RIGHT);
            }

            if ((temp = matchName()).length() != StringUtils.BLANK_STR) {
                if (temp.equals("extends")) {
                    superClass = new ArrayList<>();
                    superClass.add(parse());
                    while (matchOp(TokenType.AND)) {
                        superClass.add(parse());
                    }
                } else if (temp.equals("super")) {
                    infClass = new ArrayList<>();
                    infClass.add(parse());
                    while (matchOp(TokenType.AND)) {
                        infClass.add(parse());
                    }
                } else {
                    LOGGER.error("Name after generic defines: {}", strName);
                }
            }

            while (matchOp(TokenType.LB)) {
                if (!matchOp(TokenType.RB)) {
                    throw new IllegalStateException("expect \"]\" after \"[\"");
                }
                ++dimension;
            }

            TypeNode node = new TypeNode(className, genericTypes, defTypes, superClass, infClass, dimension);

            node.varArg = matchOp(TokenType.ELLIPSIS);

            return node;
        }
    }

    public static TypeNode create(String typeName) {
        return TypeNode.create(typeName, true);
    }

    public static TypeNode makeSureNotNull(TypeNode n) {
        return n == null ? OBJECT_TYPE : n;
    }

    /**
     * Rename generic definitions in TypeNode.
     * For example, "\<T\> T get()" will be renamed to \<XT\> XT get().
     * !!! ATTENTION
     * This method should only be called in definitions,
     * not instantiation.
     *
     * @return the generic definitions which has been renamed.
     */
    public TypeNode renameGenericDefinitions() {
        if (genericType != null) {
            genericType.forEach(t -> t.addPrefix(GENERIC_PREFIX));
        }
        return this;
    }

    /**
     * Rename generics, including generics defined in this TypeNode and generics used in this TypeNode,
     * with a list of given generic definitions.
     *
     * @param definitions Generics defined in enclosing context
     * @return the same TypeNode instance, with all generics been renamed
     */
    public TypeNode renameAllGenerics(List<TypeNode> definitions) {
        List<TypeNode> definedGenerics = new ArrayList<>();
        // defTypes have higher priority than definitions, which could be hidden.
        if (defTypes != null) {
            defTypes.forEach(it -> definedGenerics.add(it.deepClone()));
        }
        if (definitions != null) {
            definitions.stream()
                // filter generic types that are already defined
                .filter(it -> definedGenerics.stream().noneMatch(def -> def.getTypeName().equals(it.getTypeName())))
                .forEach(definedGenerics::add);
        }
        if (definedGenerics.isEmpty()) {
            return this;
        }
        if (defTypes != null) {
            defTypes.forEach(it -> {
                it.addPrefix(GENERIC_PREFIX);
                if (it.getSuperClass() != null) {
                    it.getSuperClass().forEach(sup -> sup.renameGenericInstantiations(definedGenerics));
                }
                if (it.getInfClass() != null) {
                    it.getInfClass().forEach(inf -> inf.renameGenericInstantiations(definedGenerics));
                }
            });
        }
        renameGenericInstantiations(definedGenerics);
        return this;
    }

    /**
     * Rename generic instantiations in TypeNode.
     *
     * @param definitions generic definitions defined in this class, if instantiations appears in definitions,
     *        they will be renamed.
     * @return the same TypeNode, with generic instantiations been renamed.
     */
    private TypeNode renameGenericInstantiations(List<TypeNode> definitions) {
        if (definitions == null) {
            return this;
        }
        List<String> definedNames = definitions.stream().map(TypeNode::getTypeName).collect(Collectors.toList());
        if (genericType == null) {
            return this;
        }
        genericType.stream()
            .filter(it -> definedNames.contains(it.getTypeName()))
            .forEach(it -> it.addPrefix(GENERIC_PREFIX));
        return this;
    }

    public static TypeNode create(String typeName, boolean eraseGeneric) {
        if (typeName == null || typeName.isEmpty()) {
            return null;
        }

        String tmpTypeName = typeName.trim();
        Parser parser = new Parser(tmpTypeName);
        TypeNode n = parser.parse();
        n.setEraseGeneric(eraseGeneric);

        return n;
    }

    public static TypeNode create(String typeName, TypeNode outerType) {
        TypeNode tn = TypeNode.create(typeName);
        tn.setOuterType(outerType);
        return tn;
    }

    /**
     * transform packageName from G or H to X.
     * This is a internal method, and all transformations should use TypeNode.toX instead of it.
     * We don't allow to use it directly to do transformation.
     *
     * @param s is packageName name of G or H.
     * @return s is packageName of X.
     */
    private String toX(String s) {
        return (WithoutReplacementClasses.noReplace(s) ? s : s.replace("Google", "Extension"))
            .replace("com.google.android.gms", "org.xms.g")
            .replace("com.google.firebase", "org.xms.f")
            .replace("com.google.ads", "org.xms.ads")
            .replace("com.android.installreferrer", "org.xms.installreferrer")
            .replace("com.google.android.libraries", "org.xms.libraries")
            .replace("com.google.api", "org.xms.api")
            .replace("Firebase", "Extension");
    }

    /**
     * check if the type is array
     *
     * @return true or false
     */
    public boolean isArray() {
        return dimension() > 0;
    }
}
