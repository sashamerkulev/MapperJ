package merkulyevsasha.ru.processors.mapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileObject;

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

            if (packageName != null) {
                out.println("package " + packageName + ";");
                out.println();
            }
            out.println("import java.util.List;");
            out.println("import java.util.ArrayList;");
            out.println();

            out.println("public class " + mapperClassName + " {");
            out.println();

            LinkedHashMap<Element, LinkedHashMap<String, Field>> mapClasses = new LinkedHashMap<>();
            mapClasses.putAll(oneWayMapClasses);
            mapClasses.putAll(twoWayMapClasses);

            for (Map.Entry<Element, LinkedHashMap<String, Field>> mapClassElement : mapClasses.entrySet()) {
                String mapClassName = mapClassElement.getKey().getSimpleName().toString();
                LinkedHashMap<String, Field> fields = mapClassElement.getValue();

                out.println("    public " + className + " mapTo" + className + "(" + mapClassName + " item) {");
                out.println("        return new " + className + "(" + getConstructorParameter(typeElementFields, fields) + ");");
                out.println("    }");
                out.println();

                if (twoWayMapClasses.keySet().contains(mapClassElement.getKey())) {
                    out.println("    public " + mapClassName + " mapTo" + mapClassName + "(" + className + " item) {");
                    out.println("        return new " + mapClassName + "(" + getConstructorParameter(fields, typeElementFields) + ");");
                    out.println("    }");
                    out.println();
                }
            }

            for (int i = 0; i < additionalMaps.size(); i++) {

                MapChildInfo mapInfo = additionalMaps.get(i);

                LinkedHashMap<String, Field> mainFields = fieldParser.getElementFields(mapInfo.getMainElement());
                LinkedHashMap<String, Field> childFields = fieldParser.getElementFields(mapInfo.getChildElement());

                if (mapInfo.isListType()) {
                    out.println("    public List<" + mapInfo.getMainName() + "> " + mapInfo.getMethodName() + "(List<" + mapInfo.getChildName() + "> items) {");
                    out.println("        List<" + mapInfo.getMainName() + "> result = new ArrayList<>();");
                    out.println("        for (" + mapInfo.getChildName() + " item : items) {");
                    out.println("            result.add(new " + mapInfo.getMainName() + "(");
                    out.println("                " + getConstructorParameter(mainFields, childFields));
                    out.println("            ));");
                    out.println("        }");
                    out.println("        return result;");
                } else {
                    out.println("    public " + mapInfo.getMainName() + " " + mapInfo.getMethodName() + "(" + mapInfo.getChildName() + " item) {");
                    out.println("        return new " + mapInfo.getMainName() + "(");
                    out.println("            " + getConstructorParameter(mainFields, childFields));
                    out.println("        );");
                }
                out.println("    }");
                out.println();

            }
            out.println("}");
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
