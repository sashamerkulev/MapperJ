package merkulyevsasha.ru.processors.mapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

import merkulyevsasha.ru.builders.ClassSpec;
import merkulyevsasha.ru.builders.FileSource;
import merkulyevsasha.ru.builders.KotlinWriter;
import merkulyevsasha.ru.builders.MethodSpec;
import merkulyevsasha.ru.processors.Field;

public class MapperKotlinCodeGenerator extends BaseMapperCodeGenerator {

    public MapperKotlinCodeGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    protected void generateClass(String packageName, String className, String mapperClassName, LinkedHashMap<String, Field> typeElementFields, LinkedHashMap<Element, LinkedHashMap<String, Field>> oneWayMapClasses, LinkedHashMap<Element, LinkedHashMap<String, Field>> twoWayMapClasses) throws IOException {

        File ktFile = new File(generatedSourcesRoot + File.separator + mapperClassName + ".kt");
        try (PrintWriter out = new PrintWriter(new FileWriter(ktFile))) {

            List<MethodSpec> methodSpecs = new ArrayList<>();

            LinkedHashMap<Element, LinkedHashMap<String, Field>> mapClasses = new LinkedHashMap<>();
            mapClasses.putAll(oneWayMapClasses);
            mapClasses.putAll(twoWayMapClasses);

            for (Map.Entry<Element, LinkedHashMap<String, Field>> mapClassElement : mapClasses.entrySet()) {
                String mapClassName = mapClassElement.getKey().getSimpleName().toString();
                LinkedHashMap<String, Field> fields = mapClassElement.getValue();
                methodSpecs.add(MethodSpec.methodBuilder("mapTo" + className)
                    .addParam("item", mapClassName)
                    .addReturnType(className)
                    .addStatement("return " + className + "(" + getConstructorParameter(typeElementFields, fields) + ")")
                    .build()
                );

                if (twoWayMapClasses.keySet().contains(mapClassElement.getKey())) {
                    methodSpecs.add(MethodSpec.methodBuilder("mapTo" + mapClassName)
                        .addParam("item", className)
                        .addReturnType(mapClassName)
                        .addStatement("return " + mapClassName + "(" + getConstructorParameter(fields, typeElementFields) + ")")
                        .build()
                    );
                }
            }

            for (int i = 0; i < additionalMaps.size(); i++) {

                MapChildInfo mapInfo = additionalMaps.get(i);

                LinkedHashMap<String, Field> mainFields = fieldParser.getElementFields(mapInfo.getMainElement());
                LinkedHashMap<String, Field> childFields = fieldParser.getElementFields(mapInfo.getChildElement());

                if (mapInfo.isListType()) {
                    methodSpecs.add(MethodSpec.methodBuilder(mapInfo.getMethodName())
                        .addParam("items", "List<" + mapInfo.getChildName() + ">")
                        .addReturnType("List<" + mapInfo.getMainName() + ">")
                        .addStatement("val result = mutableListOf<" + mapInfo.getMainName() + ">()")
                        .addStatement("for (item in items) {")
                        .addStatement("    result.add(" + mapInfo.getMainName() + "(")
                        .addStatement("        " + getConstructorParameter(mainFields, childFields))
                        .addStatement("    ))")
                        .addStatement("}")
                        .addStatement("return result;")
                        .build()
                    );
                } else {
                    methodSpecs.add(MethodSpec.methodBuilder(mapInfo.getMethodName())
                        .addParam("item", mapInfo.getChildName())
                        .addReturnType(mapInfo.getMainName())
                        .addStatement("return " + mapInfo.getMainName() + "(" + getConstructorParameter(mainFields, childFields) + ")")
                        .build()
                    );
                }
            }

            ClassSpec classSpec = ClassSpec.classBuilder(mapperClassName)
                .addMethods(methodSpecs)
                .build();

            FileSource.classFileBuilder(mapperClassName)
                .addPackage(packageName)
                .addClass(classSpec)
                .build()
                .writeTo(out, new KotlinWriter());
        }
    }

    @Override
    protected String getDefaultValueForType(TypeMirror typeMirror) {
        String typeName = typeMirror.toString().toLowerCase();

        boolean isList = typeName.contains("list");

        if (isList) {
            return "emptyList()";
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
                    return "0";
                case "byte":
                    return "0";
                case "java.lang.string":
                    return "\"\"";
                default:
                    throw new IllegalArgumentException(typeName);
            }
        }
    }

    @Override
    protected String getGetterByFieldName(String fieldName) {
        return fieldName;
    }
}
