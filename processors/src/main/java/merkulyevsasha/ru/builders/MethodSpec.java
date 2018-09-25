package merkulyevsasha.ru.builders;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.Element;

import merkulyevsasha.ru.processors.Values;

public class MethodSpec {

    private final String name;
    private final boolean isStatic;
    private final String returnType;
    private final List<MethodParams> params;
    private final List<String> statements;

    private MethodSpec(String name, boolean isStatic, String returnType, List<MethodParams> params, List<String> statements) {
        this.name = name;
        this.returnType = returnType;
        this.params = params;
        this.statements = statements;
        this.isStatic = isStatic;
    }

    public String getName() {
        return name;
    }

    public boolean isStatic() {
        return isStatic;
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
        return new MethodSpec.Builder(methodName, false);
    }

    public static MethodSpec.Builder staticMethodBuilder(String methodName) {
        if (methodName.isEmpty()) throw new IllegalArgumentException("methodName is empty");
        return new MethodSpec.Builder(methodName, true);
    }

    public static MethodSpec.Builder constructorBuilder() {
        return new MethodSpec.Builder();
    }

    public static class Builder {

        private String name;
        private boolean isStatic;
        private String returnType;
        private List<MethodParams> params = new ArrayList<>();
        private List<String> statements = new ArrayList<>();

        Builder(String name, boolean isStatic) {
            this.name = name;
            this.isStatic = isStatic;
        }

        Builder() {
            this("", false);
        }

        public MethodSpec build() {
            return new MethodSpec(name, isStatic, returnType, params, statements);
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

        public Builder addStatement(String statement) {
            if (statement.isEmpty()) throw new IllegalArgumentException("Statement is empty");
            statements.add(statement);
            return this;
        }
    }
}