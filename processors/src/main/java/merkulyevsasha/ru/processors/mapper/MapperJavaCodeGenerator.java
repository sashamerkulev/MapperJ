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
import merkulyevsasha.ru.processors.BaseCodeGenerator;
import merkulyevsasha.ru.processors.CodeGenerator;

public class MapperJavaCodeGenerator extends BaseMapperCodeGenerator implements CodeGenerator {
    public MapperJavaCodeGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    public void generate(String packageName, TypeElement typeElement) {
        if (generatedSourcesRoot == null || generatedSourcesRoot.isEmpty()) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, BaseCodeGenerator.FOLDER_ERROR_MESSAGE);
            return;
        }
        try {
            Mapper mapper = typeElement.getAnnotation(Mapper.class);
            List<TypeMirror> mapOneWayMirrors = getOneWayMapTypeMirrors(mapper);
            List<TypeMirror> typeTwoWayMirrors = getTwoWayMapTypeMirrors(mapper);

            additionalMaps.clear();
            generateClass(packageName, typeElement, convertTypeMirrorsToTypeElements(mapOneWayMirrors), convertTypeMirrorsToTypeElements(typeTwoWayMirrors));
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
    }

    private void generateClass(String packageName, TypeElement typeElement, List<TypeElement> mapOneWayElements, List<TypeElement> mapTwoWayElements) throws IOException {
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

        LinkedHashMap<String, Element> fileds = getTypeElementFields(typeElement);

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
            allTypes.addAll(mapTwoWayElements);
            allTypes.addAll(mapOneWayElements);

            for (TypeElement mapTypeElement : allTypes) {
                LinkedHashMap<String, Element> fields = getTypeElementFields(mapTypeElement);
                String mapClassName = mapTypeElement.getSimpleName().toString();

                out.println("    public " + className + " mapTo" + className + "(" + mapClassName + " item) {");
                out.println("        return new " + className + "(" + getConstructorParameter(fileds, fields) + ");");
                out.println("    }");
                out.println();

                if (mapTwoWayElements.contains(mapTypeElement)) {
                    out.println("    public " + mapClassName + " mapTo" + mapClassName + "(" + className + " item) {");
                    out.println("        return new " + mapClassName + "(" + getConstructorParameter(fields, fileds) + ");");
                    out.println("    }");
                    out.println();
                }
            }

            for (int i = 0; i < additionalMaps.size(); i++) {

                MapChildInfo mapInfo = additionalMaps.get(i);

                LinkedHashMap<String, Element> mainFields = getTypeElementFields(mapInfo.getMainElement());
                LinkedHashMap<String, Element> childFields = getTypeElementFields(mapInfo.getChildElement());

                if (mapInfo.isListType()) {
                    out.println("    public List<" + mapInfo.getMainName() + "> " + mapInfo.getMethodName() + "(List<" + mapInfo.getChildName() + "> items) {");
                    out.println("        List<" + mapInfo.getMainName() + "> result = new ArrayList<>();");
                    out.println("        for (" + mapInfo.getChildName() + " item : items) {");
                    out.println("            result.add(new " + mapInfo.getMainName() + "(");
                    out.println("                    " + getConstructorParameter(mainFields, childFields));
                    out.println("            ));");
                    out.println("        }");
                    out.println("        return result;");
                } else {
                    out.println("    public " + mapInfo.getMainName() + " " + mapInfo.getMethodName()+ "(" + mapInfo.getChildName() + " item) {");
                    out.println("        return new " + mapInfo.getMainName() + "(");
                    out.println("                " + getConstructorParameter(mainFields, childFields));
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
