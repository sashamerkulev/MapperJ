package merkulyevsasha.ru.builders;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;

import merkulyevsasha.ru.processors.Values;

public class MethodSpec {

    private final String name;
    private final String returnType;
    private final List<MethodParams> params;
    private final List<String> statements;
    private final AccessModifier accessModifier;
    private final InheritanceModifier inheritanceModifier;

    private MethodSpec(String name, AccessModifier accessModifier, InheritanceModifier inheritanceModifier,
                       String returnType, List<MethodParams> params, List<String> statements) {
        this.name = name;
        this.accessModifier = accessModifier;
        this.inheritanceModifier = inheritanceModifier;
        this.returnType = returnType;
        this.params = params;
        this.statements = statements;
    }

    public String getName() {
        return name;
    }

    public AccessModifier getAccessModifier() {
        return accessModifier;
    }

    public InheritanceModifier getInheritanceModifier() {
        return inheritanceModifier;
    }

    public String getReturnType() {
        return returnType;
    }

    public List<MethodParams> getParams() {
        return params;
    }

    public List<String> getStatements() {
        return statements;
    }

    public static MethodSpec.Builder methodBuilder(String methodName) {
        if (methodName.isEmpty()) throw new IllegalArgumentException("methodName is empty");
        return new MethodSpec.Builder(methodName);
    }

    public static MethodSpec.Builder constructorBuilder() {
        return new MethodSpec.Builder();
    }

    public static class Builder {

        private String name;
        private String returnType;
        private List<MethodParams> params = new ArrayList<>();
        private List<String> statements = new ArrayList<>();
        private AccessModifier accessModifier = AccessModifier.PUBLIC;
        private InheritanceModifier inheritanceModifier = InheritanceModifier.NOTHING;

        Builder(String name) {
            this.name = name;
        }

        Builder() {
            this("");
        }

        public MethodSpec build() {
            return new MethodSpec(name, accessModifier, inheritanceModifier, returnType, params, statements);
        }

        public Builder addReturnType(String returnType) {
            if (returnType.isEmpty()) throw new IllegalArgumentException("returnType is empty");
            this.returnType = returnType;
            return this;
        }

        public Builder addParam(String paramName, Element element, Values values) {
            if (paramName.isEmpty() || element == null)
                throw new IllegalArgumentException("Method paramName or typeName is empty");
            params.add(new MethodParams(paramName, element, values));
            return this;
        }

        public Builder addParam(String paramName, String typeName) {
            if (paramName.isEmpty() || typeName.isEmpty())
                throw new IllegalArgumentException("Method paramName or typeName is empty");
            params.add(new MethodParams(paramName, typeName));
            return this;
        }

        public Builder addStatement(String statement) {
            if (statement.isEmpty()) throw new IllegalArgumentException("Statement is empty");
            statements.add(statement);
            return this;
        }

        public Builder addAccessModifier(AccessModifier accessModifier) {
            this.accessModifier = accessModifier;
            return this;
        }

        public Builder addInheritanceModifier(InheritanceModifier inheritanceModifier) {
            this.inheritanceModifier = inheritanceModifier;
            return this;
        }

    }

    public enum AccessModifier {
        PRIVATE,
        PUBLIC
    }

    public enum InheritanceModifier {
        NOTHING,
        FINAL,
        STATIC
    }

}