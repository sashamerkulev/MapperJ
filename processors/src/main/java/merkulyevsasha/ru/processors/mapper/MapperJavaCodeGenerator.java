package merkulyevsasha.ru.processors.mapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

import merkulyevsasha.ru.annotations.params.Source;
import merkulyevsasha.ru.builders.ClassSpec;
import merkulyevsasha.ru.builders.FileSource;
import merkulyevsasha.ru.builders.JavaWriter;
import merkulyevsasha.ru.builders.MethodSpec;
import merkulyevsasha.ru.processors.Field;

public class MapperJavaCodeGenerator extends BaseMapperCodeGenerator {

    public MapperJavaCodeGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    protected void generateClass(String packageName, String className, String mapperClassName, LinkedHashMap<String, Field> typeElementFields,
                                 LinkedHashMap<Element, LinkedHashMap<String, Field>> oneWayMapClasses, LinkedHashMap<Element, LinkedHashMap<String, Field>> twoWayMapClasses) throws IOException {

        JavaFileObject builderFile = processingEnv.getFiler()
            .createSourceFile(mapperClassName);

        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {

            List<MethodSpec> methodSpecs = new ArrayList<>();

            LinkedHashMap<Element, LinkedHashMap<String, Field>> mapClasses = new LinkedHashMap<>();
            mapClasses.putAll(oneWayMapClasses);
            mapClasses.putAll(twoWayMapClasses);

            List<String> imports = new ArrayList<>();
            imports.add("java.util.List");
            imports.add("java.util.ArrayList");
            imports.add("java.util.Date");

            for (Map.Entry<Element, LinkedHashMap<String, Field>> mapClassElement : mapClasses.entrySet()) {
                String mapClassName = mapClassElement.getKey().getSimpleName().toString();
                LinkedHashMap<String, Field> fields = mapClassElement.getValue();

                imports.add(mapClassElement.getKey().toString());

                methodSpecs.add(MethodSpec.methodBuilder("mapTo" + className)
                    .addParam("item", mapClassName)
                    .addReturnType(className)
                    .addStatement("return new " + className + "(" + getConstructorParameter(Source.Java, typeElementFields, fields) + ");")
                    .build()
                );

                if (twoWayMapClasses.keySet().contains(mapClassElement.getKey())) {
                    methodSpecs.add(MethodSpec.methodBuilder("mapTo" + mapClassName)
                        .addParam("item", className)
                        .addReturnType(mapClassName)
                        .addStatement("return new " + mapClassName + "(" + getConstructorParameter(Source.Java, fields, typeElementFields) + ");")
                        .build()
                    );
                }
            }

            for (int i = 0; i < additionalMaps.size(); i++) {

                MapChildInfo mapInfo = additionalMaps.get(i);

                LinkedHashMap<String, Field> mainFields = fieldParser.getElementFields(mapInfo.getMainElement());
                LinkedHashMap<String, Field> childFields = fieldParser.getElementFields(mapInfo.getChildElement());

                imports.add(mapInfo.getChildElement().toString());

                if (mapInfo.isListType()) {
                    methodSpecs.add(MethodSpec.methodBuilder(mapInfo.getMethodName())
                        .addParam("items", "List<" + mapInfo.getChildName() + ">")
                        .addReturnType("List<" + mapInfo.getMainName() + ">")
                        .addStatement("List<" + mapInfo.getMainName() + "> result = new ArrayList<>();")
                        .addStatement("for (" + mapInfo.getChildName() + " item : items) {")
                        .addStatement("    result.add(new " + mapInfo.getMainName() + "(")
                        .addStatement("        " + getConstructorParameter(Source.Java, mainFields, childFields))
                        .addStatement("    ));")
                        .addStatement("}")
                        .addStatement("return result;")
                        .build()
                    );
                } else {
                    methodSpecs.add(MethodSpec.methodBuilder(mapInfo.getMethodName())
                        .addParam("item", mapInfo.getChildName())
                        .addReturnType(mapInfo.getMainName())
                        .addStatement("return new " + mapInfo.getMainName() + "(" + getConstructorParameter(Source.Java, mainFields, childFields) + ");")
                        .build()
                    );
                }
            }

            ClassSpec classSpec = ClassSpec.classBuilder(mapperClassName)
                .addMethods(methodSpecs)
                .build();

            FileSource.classFileBuilder(mapperClassName)
                .addPackage(packageName)
                .addImports(imports)
                .addClass(classSpec)
                .build()
                .writeTo(out, new JavaWriter());

        }
    }

    @Override
    protected String getDefaultValueForType(TypeMirror typeMirror) {
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

    @Override
    protected String getGetterByFieldName(String fieldName) {
        String firstUpper = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        return "get" + firstUpper + "()";
    }

}
