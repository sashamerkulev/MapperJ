package merkulyevsasha.ru.builders;

import java.util.ArrayList;
import java.util.List;

public class ClassSpec {

    private final String className;
    private final List<MethodSpec> methodSpecs;
    private final MethodSpec constructorSpec;

    private ClassSpec(String className, MethodSpec constructorSpec, List<MethodSpec> methodSpecs) {
        this.className = className;
        this.constructorSpec = constructorSpec;
        this.methodSpecs = methodSpecs;
    }

    public String getClassName() {
        return className;
    }

    public List<MethodSpec> getMethodSpecs() {
        return methodSpecs;
    }

    public MethodSpec getConstructorSpec() {
        return constructorSpec;
    }

    public static ClassSpec.Builder classBuilder(String className) {
        if (className.isEmpty()) throw new IllegalArgumentException("className is empty");
        return new ClassSpec.Builder(className);
    }

    public static class Builder {

        private String className;
        private List<MethodSpec> methodSpecs = new ArrayList<>();
        private MethodSpec constructorSpec;

        Builder(String className) {
            this.className = className;
        }

        public ClassSpec build() {
            return new ClassSpec(className, constructorSpec, methodSpecs);
        }

        public Builder addMethod(MethodSpec methodSpec) {
            if (methodSpec == null) throw new IllegalArgumentException();
            methodSpecs.add(methodSpec);
            return this;
        }

        public Builder addConstructor(MethodSpec methodSpec) {
            if (methodSpec == null) throw new IllegalArgumentException();
            constructorSpec = methodSpec;
            return this;
        }
    }
}