package merkulyevsasha.ru.processors;

import com.google.auto.service.AutoService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.AbstractElementVisitor6;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import merkulyevsasha.ru.annotations.Mapper;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(MapperProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
public class MapperProcessor extends AbstractProcessor {

    final static String KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated";

    private final List<MapChildInfo> additionalMaps = new ArrayList<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Mapper.class);

        for (Element element : elements) {
            if (element.getKind() != ElementKind.CLASS) continue;
            TypeElement typeElement = (TypeElement) element;

            Mapper mapper = typeElement.getAnnotation(Mapper.class);
            List<TypeMirror> typeOneWayMirrors = getOneWayMapTypeMirrors(mapper);
            List<TypeMirror> typeTwoWayMirrors = getTwoWayMapTypeMirrors(mapper);

            additionalMaps.clear();
            prepareToGenerateClassFile(typeElement, convertTypeMirrorsToTypeElements(typeOneWayMirrors), convertTypeMirrorsToTypeElements(typeTwoWayMirrors), processingEnv.getElementUtils().getPackageOf(typeElement).toString());
        }
        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> result = new HashSet<>();
        result.add(Mapper.class.getCanonicalName());
        return result;
    }

    private void prepareToGenerateClassFile(TypeElement typeElement, List<TypeElement> typeOneWayElements, List<TypeElement> typeTwoWayElements, String packageOfMethod) {
        String generatedSourcesRoot = processingEnv.getOptions().get(KAPT_KOTLIN_GENERATED_OPTION_NAME);
        if (generatedSourcesRoot == null || generatedSourcesRoot.isEmpty()) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Can't find the target directory for generated Kotlin files.");
            return;
        }
        try {
            generateClassFile(packageOfMethod, typeElement, typeOneWayElements, typeTwoWayElements);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void generateClassFile(String packageName, TypeElement type, List<TypeElement> oneWayChilds, List<TypeElement> twoWayChilds) throws IOException {
        String className = type.getSimpleName().toString();
        String builderSimpleClassName = className + "Mapper";
        JavaFileObject builderFile;
        try {
            builderFile = processingEnv.getFiler()
                    .createSourceFile(builderSimpleClassName);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        LinkedHashMap<String, Element> mainElements = getMapTypeElementFields(type);

        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {

            if (packageName != null) {
                out.println("package " + packageName + ";");
                out.println();
            }
            out.println("import java.util.List;");
            out.println("import java.util.ArrayList;");
            out.println();

            out.println("public class " + builderSimpleClassName + " {");
            out.println();

            List<TypeElement> allTypes = new ArrayList<>();
            allTypes.addAll(twoWayChilds);
            allTypes.addAll(oneWayChilds);

            for (TypeElement typeElement : allTypes) {
                LinkedHashMap<String, Element> childElements = getMapTypeElementFields(typeElement);
                String childClassName = typeElement.getSimpleName().toString();

                out.println("    public " + className + " mapTo" + className + "(" + childClassName + " item) {");
                out.println("        return new " + className + "(" + getConstructorParameter(mainElements, childElements) + ");");
                out.println("    }");
                out.println();

                if (twoWayChilds.contains(typeElement)) {
                    out.println("    public " + childClassName + " mapTo" + childClassName + "(" + className + " item) {");
                    out.println("        return new " + childClassName + "(" + getConstructorParameter(childElements, mainElements) + ");");
                    out.println("    }");
                    out.println();
                }
            }

            for (int i = 0; i < additionalMaps.size(); i++) {

                MapChildInfo map = additionalMaps.get(i);

                LinkedHashMap<String, Element> mapMainElements = getMapTypeElementFields(map.mainElement);
                LinkedHashMap<String, Element> mapChildElements = getMapTypeElementFields(map.childElement);

                if (map.listType) {
                    out.println("    public List<" + map.mainName + "> " + map.methodName + "(List<" + map.childName + "> items) {");
                    out.println("        List<" + map.mainName + "> result = new ArrayList<>();");
                    out.println("        for (" + map.childName + " item : items) {");
                    out.println("            result.add(new " + map.mainName + "(");
                    out.println("                    " + getConstructorParameter(mapMainElements, mapChildElements));
                    out.println("            ));");
                    out.println("        }");
                    out.println("        return result;");
                } else {
                    out.println("    public " + map.mainName + " " + map.methodName + "(" + map.childName + " item) {");
                    out.println("        return new " + map.mainName + "(");
                    out.println("                " + getConstructorParameter(mapMainElements, mapChildElements));
                    out.println("        );");
                }
                out.println("    }");
                out.println();

            }
            out.println("}");
        }
    }

    private List<TypeElement> convertTypeMirrorsToTypeElements(List<TypeMirror> typeMirrors) {
        List<TypeElement> typeElements = new ArrayList<>();
        for (TypeMirror tm : typeMirrors) {
            Object mirrorType = acceptMirrorType(tm);
            DeclaredType declaredTpe = (DeclaredType) mirrorType;

            Element element = declaredTpe.asElement();
            Object abstractElement = acceptElement(element);

            TypeElement typeElement = (TypeElement) abstractElement;
            typeElements.add(typeElement);
        }
        return typeElements;
    }

    private List<TypeMirror> getOneWayMapTypeMirrors(Mapper mapper) {
        List<TypeMirror> typeMirrors = new ArrayList<>();
        try {
            for (Class<?> clazz : mapper.oneWayMapClasses()) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, clazz.getSimpleName());
            }
        } catch (MirroredTypesException mte) {
            typeMirrors.addAll(mte.getTypeMirrors());
        }
        return typeMirrors;
    }

    private List<TypeMirror> getTwoWayMapTypeMirrors(Mapper mapper) {
        List<TypeMirror> typeMirrors = new ArrayList<>();
        try {
            for (Class<?> clazz : mapper.twoWayMapClasses()) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, clazz.getSimpleName());
            }
        } catch (MirroredTypesException mte) {
            typeMirrors.addAll(mte.getTypeMirrors());
        }
        return typeMirrors;
    }

    private String getGetterByFieldName(String fieldName) {
        String firstUpper = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        return "get" + firstUpper + "()";
    }

    private String getConstructorParameter(LinkedHashMap<String, Element> mainElements, LinkedHashMap<String, Element> childElements) {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, Element> entry : mainElements.entrySet()) {

            Element mainElement = entry.getValue();
            String key = mainElement.getSimpleName().toString();

            if (sb.length() > 0) sb.append(", ");

            if (childElements.containsKey(key)) {

                Element childElement = childElements.get(key);

                Object mainElementType = acceptMirrorType(mainElement.asType());
                Object childElementType = acceptMirrorType(childElement.asType());

                if (mainElementType instanceof PrimitiveType || mainElement.asType().toString().contains("String")) {
                    sb.append("item.").append(getGetterByFieldName(mainElement.getSimpleName().toString()));
                } else {

                    DeclaredType mainDeclaredType = (DeclaredType) mainElementType;
                    DeclaredType childDeclaredType = (DeclaredType) childElementType;

                    Boolean isList = false;
                    String methodName = "mapTo" + getClassName(mainElement.asType().toString());
                    if (mainDeclaredType.toString().toLowerCase().contains("list")) {
                        methodName = methodName + "s";

                        TypeMirror mainArg = mainDeclaredType.getTypeArguments().get(0);
                        TypeMirror childArg = childDeclaredType.getTypeArguments().get(0);

                        Object mainListElementType = acceptMirrorType(mainArg);
                        Object childListElementType = acceptMirrorType(childArg);

                        mainDeclaredType = (DeclaredType) mainListElementType;
                        childDeclaredType = (DeclaredType) childListElementType;
                        isList = true;
                    }

                    sb.append(methodName).append("(item.").append(getGetterByFieldName(key)).append(")");

                    additionalMaps.add(new MapChildInfo(
                            mainDeclaredType.asElement(),
                            getClassName(mainElement.asType().toString()),
                            childDeclaredType.asElement(),
                            getClassName(childElement.asType().toString()),
                            methodName,
                            isList));
                }

            } else {
                sb.append(getDefaultValueForType(mainElement.asType()));
            }
        }
        return sb.toString();
    }

    private String getClassName(String elementTypeName) {
        int lastDot = elementTypeName.lastIndexOf(".");
        return elementTypeName.substring(lastDot + 1).replace(">", "");
    }

    private String getDefaultValueForType(TypeMirror typeMirror) {
        String typeName = typeMirror.toString().toLowerCase();

        boolean isList = typeName.contains("list");

        if (isList) {
            return "new ArrayList()";
        } else {
            switch (typeName) {
                case "int":
                    return "0";
                case "long":
                    return "0";
                case "boolean":
                    return "false";
                case "float":
                    return "0F";
                case "double":
                    return "0.0";
                case "short":
                    return "(short) 0";
                case "byte":
                    return "(byte) 0";
                case "java.lang.string":
                    return "\"\"";
                default:
                    return "null";
                //throw new IllegalArgumentException(typeName);
            }
        }
    }

    private LinkedHashMap<String, Element> getMapTypeElementFields(Element type) {
        LinkedHashMap<String, Element> typeElements = new LinkedHashMap<>();
        for (Element element : type.getEnclosedElements()) {
            if (element.getKind() != ElementKind.FIELD) continue;
            typeElements.put(element.toString(), element);
        }
        return typeElements;
    }

    private Object acceptMirrorType(TypeMirror typeMirror) {
        return typeMirror.accept(new TypeVisitor<Object, TypeMirror>() {
            @Override
            public Object visit(TypeMirror typeMirror, TypeMirror typeMirror2) {
                return typeMirror;
            }

            @Override
            public Object visit(TypeMirror typeMirror) {
                return typeMirror;
            }

            @Override
            public Object visitPrimitive(PrimitiveType primitiveType, TypeMirror typeMirror) {
                return primitiveType;
            }

            @Override
            public Object visitNull(NullType nullType, TypeMirror typeMirror) {
                return nullType;
            }

            @Override
            public Object visitArray(ArrayType arrayType, TypeMirror typeMirror) {
                return arrayType;
            }

            @Override
            public Object visitDeclared(DeclaredType declaredType, TypeMirror typeMirror) {
                return declaredType;
            }

            @Override
            public Object visitError(ErrorType errorType, TypeMirror typeMirror) {
                return errorType;
            }

            @Override
            public Object visitTypeVariable(TypeVariable typeVariable, TypeMirror typeMirror) {
                return typeVariable;
            }

            @Override
            public Object visitWildcard(WildcardType wildcardType, TypeMirror typeMirror) {
                return wildcardType;
            }

            @Override
            public Object visitExecutable(ExecutableType executableType, TypeMirror typeMirror) {
                return executableType;
            }

            @Override
            public Object visitNoType(NoType noType, TypeMirror typeMirror) {
                return noType;
            }

            @Override
            public Object visitUnknown(TypeMirror typeMirror, TypeMirror typeMirror2) {
                return typeMirror;
            }

            @Override
            public Object visitUnion(UnionType unionType, TypeMirror typeMirror) {
                return unionType;
            }

            @Override
            public Object visitIntersection(IntersectionType intersectionType, TypeMirror typeMirror) {
                return intersectionType;
            }
        }, typeMirror);
    }

    private Object acceptElement(Element element) {
        return element.accept(new AbstractElementVisitor6<Object, Element>() {
            @Override
            public Object visitPackage(PackageElement packageElement, Element element) {
                return null;
            }

            @Override
            public Object visitType(TypeElement typeElement, Element element) {
                return typeElement;
            }

            @Override
            public Object visitVariable(VariableElement variableElement, Element element) {
                return variableElement;
            }

            @Override
            public Object visitExecutable(ExecutableElement executableElement, Element element) {
                return null;
            }

            @Override
            public Object visitTypeParameter(TypeParameterElement variableElement, Element element) {
                return null;
            }
        }, element);
    }

    private class MapChildInfo {
        private final Element mainElement;
        private final String mainName;
        private final Element childElement;
        private final String childName;
        private final String methodName;
        private final boolean listType;

        MapChildInfo(Element mainElement, String mainName, Element childElement, String childName, String methodName, boolean listType) {
            this.mainElement = mainElement;
            this.mainName = mainName;
            this.childElement = childElement;
            this.childName = childName;
            this.methodName = methodName;
            this.listType = listType;
        }
    }

}
