package merkulyevsasha.ru.builders;

import java.util.ArrayList;
import java.util.List;

public class ClassSpec {

    private final String className;
    private final List<MethodSpec> staticMethodSpecs;
    private final List<MethodSpec> methodSpecs;
    private final MethodSpec constructorSpec;
    private final AccessModifier accessModifier;
    private final InheritanceModifier inheritanceModifier;

    private ClassSpec(String className, AccessModifier accessModifier, InheritanceModifier inheritanceModifier,
                      MethodSpec constructorSpec, List<MethodSpec> staticMethodSpecs, List<MethodSpec> methodSpecs) {
        this.className = className;
        this.accessModifier = accessModifier;
        this.inheritanceModifier = inheritanceModifier;
        this.constructorSpec = constructorSpec;
        this.staticMethodSpecs = staticMethodSpecs;
        this.methodSpecs = methodSpecs;
    }

    public String getClassName() {
        return className;
    }

    public AccessModifier getAccessModifier() {
        return accessModifier;
    }

    public InheritanceModifier getInheritanceModifier() {
        return inheritanceModifier;
    }

    public List<MethodSpec> getStaticMethodSpecs() {
        return staticMethodSpecs;
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
        private List<MethodSpec> staticMethodSpecs = new ArrayList<>();
        private List<MethodSpec> methodSpecs = new ArrayList<>();
        private MethodSpec constructorSpec;
        private AccessModifier accessModifier = AccessModifier.PUBLIC;
        private InheritanceModifier inheritanceModifier = InheritanceModifier.NOTHING;

        Builder(String className) {
            this.className = className;
        }

        public ClassSpec build() {
            return new ClassSpec(className, accessModifier, inheritanceModifier, constructorSpec, staticMethodSpecs, methodSpecs);
        }

        public Builder addMethod(MethodSpec methodSpec) {
            if (methodSpec == null) throw new IllegalArgumentException();
            if (methodSpec.getInheritanceModifier() == MethodSpec.InheritanceModifier.STATIC) {
                staticMethodSpecs.add(methodSpec);
            } else {
                methodSpecs.add(methodSpec);
            }
            return this;
        }

        public Builder addMethods(List<MethodSpec> methodSpecs) {
            if (methodSpecs == null) throw new IllegalArgumentException();
            for (MethodSpec methodSpec : methodSpecs) {
                if (methodSpec.getInheritanceModifier() == MethodSpec.InheritanceModifier.STATIC) {
                    this.staticMethodSpecs.add(methodSpec);
                } else {
                    this.methodSpecs.add(methodSpec);
                }
            }
            return this;
        }

        public Builder addConstructor(MethodSpec methodSpec) {
            if (methodSpec == null) throw new IllegalArgumentException();
            constructorSpec = methodSpec;
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
        OPEN,
        STATIC,
        DATA,
    }
}