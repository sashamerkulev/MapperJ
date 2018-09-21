package merkulyevsasha.ru.processors.mapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

public class MapperKotlinCodeGenerator extends BaseMapperCodeGenerator {

    public MapperKotlinCodeGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    protected void generateClass(String packageName, TypeElement typeElement, List<TypeElement> mapOneWayElements, List<TypeElement> mapTwoWayElements) throws IOException {
        String className = typeElement.getSimpleName().toString();
        String mapperClassName = className + "Mapper";
        LinkedHashMap<String, Element> fileds = getTypeElementFields(typeElement);

        try {
            File ktFile = new File(generatedSourcesRoot + File.separator + mapperClassName + ".kt");
            try (PrintWriter out = new PrintWriter(new FileWriter(ktFile))) {

                if (packageName != null) {
                    out.println("package " + packageName);
                    out.println();
                }

                out.println("class " + mapperClassName + " {");
                out.println();

                List<TypeElement> mapElements = new ArrayList<>();
                mapElements.addAll(mapTwoWayElements);
                mapElements.addAll(mapOneWayElements);

                for (TypeElement mapTypeElement : mapElements) {
                    LinkedHashMap<String, Element> fields = getTypeElementFields(mapTypeElement);
                    String mapClassName = mapTypeElement.getSimpleName().toString();

                    out.println("    fun mapTo" + className + "(item: " + mapClassName + "): " + className + " {");
                    out.println("        return " + className + "(" + getConstructorParameter(fileds, fields) + ")");
                    out.println("    }");
                    out.println();

                    if (mapTwoWayElements.contains(mapTypeElement)) {
                        out.println("    fun mapTo" + mapClassName + "(item: " + className + "): " + mapClassName + " {");
                        out.println("        return " + mapClassName + "(" + getConstructorParameter(fields, fileds) + ")");
                        out.println("    }");
                        out.println();
                    }
                }

                for (int i = 0; i < additionalMaps.size(); i++) {

                    MapChildInfo mapInfo = additionalMaps.get(i);

                    LinkedHashMap<String, Element> mainFields = getTypeElementFields(mapInfo.getMainElement());
                    LinkedHashMap<String, Element> childFields = getTypeElementFields(mapInfo.getChildElement());

                    if (mapInfo.isListType()) {
                        out.println("    fun " + mapInfo.getMethodName() + "(items: List<" + mapInfo.getChildName() + ">): List<" + mapInfo.getMainName() + "> {");
                        out.println("        val result = mutableListOf<" + mapInfo.getMainName() + ">()");
                        out.println("        for (item in items) {");
                        out.println("            result.add(" + mapInfo.getMainName() + "(");
                        out.println("                " + getConstructorParameter(mainFields, childFields));
                        out.println("            ))");
                        out.println("        }");
                        out.println("        return result");
                    } else {
                        out.println("    fun " + mapInfo.getMethodName() + "(item: " + mapInfo.getChildName() + "): " + mapInfo.getMainName() + " {");
                        out.println("        return " + mapInfo.getMainName() + "(");
                        out.println("            " + getConstructorParameter(mainFields, childFields));
                        out.println("        )");
                    }
                    out.println("    }");
                    out.println();

                }
                out.println("}");
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
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
