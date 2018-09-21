package merkulyevsasha.ru.processors.mapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import merkulyevsasha.ru.annotations.Mapper;
import merkulyevsasha.ru.processors.CodeGenerator;

public class MapperJavaCodeGenerator extends BaseMapperCodeGenerator implements CodeGenerator {
    public MapperJavaCodeGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    public void generate(TypeElement typeElement, String packageName) {
        if (generatedSourcesRoot == null || generatedSourcesRoot.isEmpty()) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Can't find the target directory for generated Kotlin files.");
            return;
        }
        try {
            Mapper mapper = typeElement.getAnnotation(Mapper.class);
            List<TypeMirror> typeOneWayMirrors = getOneWayMapTypeMirrors(mapper);
            List<TypeMirror> typeTwoWayMirrors = getTwoWayMapTypeMirrors(mapper);

            additionalMaps.clear();
            generateClass(packageName, typeElement, convertTypeMirrorsToTypeElements(typeOneWayMirrors), convertTypeMirrorsToTypeElements(typeTwoWayMirrors));
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
    }

    private void generateClass(String packageName, TypeElement typeElement, List<TypeElement> typeOneWayElements, List<TypeElement> typeTwoWayElements) throws IOException {
        String className = typeElement.getSimpleName().toString();
        String builderSimpleClassName = className + "Mapper";
        JavaFileObject builderFile;
        try {
            builderFile = processingEnv.getFiler()
                .createSourceFile(builderSimpleClassName);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        LinkedHashMap<String, Element> mainElements = getTypeElementFields(typeElement);

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
            allTypes.addAll(typeTwoWayElements);
            allTypes.addAll(typeOneWayElements);

            for (TypeElement typeElement2 : allTypes) {
                LinkedHashMap<String, Element> childElements = getTypeElementFields(typeElement2);
                String childClassName = typeElement2.getSimpleName().toString();

                out.println("    public " + className + " mapTo" + className + "(" + childClassName + " item) {");
                out.println("        return new " + className + "(" + getConstructorParameter(mainElements, childElements) + ");");
                out.println("    }");
                out.println();

                if (typeTwoWayElements.contains(typeElement2)) {
                    out.println("    public " + childClassName + " mapTo" + childClassName + "(" + className + " item) {");
                    out.println("        return new " + childClassName + "(" + getConstructorParameter(childElements, mainElements) + ");");
                    out.println("    }");
                    out.println();
                }
            }

            for (int i = 0; i < additionalMaps.size(); i++) {

                MapChildInfo map = additionalMaps.get(i);

                LinkedHashMap<String, Element> mapMainElements = getTypeElementFields(map.getMainElement());
                LinkedHashMap<String, Element> mapChildElements = getTypeElementFields(map.getChildElement());

                if (map.isListType()) {
                    out.println("    public List<" + map.getMainName() + "> " + map.getMethodName() + "(List<" + map.getChildName() + "> items) {");
                    out.println("        List<" + map.getMainName() + "> result = new ArrayList<>();");
                    out.println("        for (" + map.getChildName() + " item : items) {");
                    out.println("            result.add(new " + map.getMainName() + "(");
                    out.println("                    " + getConstructorParameter(mapMainElements, mapChildElements));
                    out.println("            ));");
                    out.println("        }");
                    out.println("        return result;");
                } else {
                    out.println("    public " + map.getMainName() + " " + map.getMethodName()+ "(" + map.getChildName() + " item) {");
                    out.println("        return new " + map.getMainName() + "(");
                    out.println("                " + getConstructorParameter(mapMainElements, mapChildElements));
                    out.println("        );");
                }
                out.println("    }");
                out.println();

            }
            out.println("}");
        }
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

    private String getGetterByFieldName(String fieldName) {
        String firstUpper = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        return "get" + firstUpper + "()";
    }

    private String getClassName(String elementTypeName) {
        int lastDot = elementTypeName.lastIndexOf(".");
        return elementTypeName.substring(lastDot + 1).replace(">", "");
    }

}
