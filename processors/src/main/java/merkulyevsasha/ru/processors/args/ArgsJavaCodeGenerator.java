package merkulyevsasha.ru.processors.args;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import merkulyevsasha.ru.processors.Field;
import merkulyevsasha.ru.processors.Values;

public class ArgsJavaCodeGenerator extends BaseArgsCodeGenerator {

    public ArgsJavaCodeGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    protected void generateClass(String packageName, String className, LinkedHashMap<String, Field> fields) {
        try {
            JavaFileObject builderFile = processingEnv.getFiler()
                .createSourceFile(className);

            try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {

                if (packageName != null) {
                    out.println("package " + packageName + ";");
                }
                out.println();
                out.println("import android.content.Intent;");
                out.println("import android.os.Bundle;");
                out.println();

                // class
                out.println("public class " + className + " {");
                out.println();

                // fields
                for (Map.Entry<String, Field> entry : fields.entrySet()) {
                    Field field = entry.getValue();
                    Element fieldElement = field.getElement();
                    String key = fieldElement.getSimpleName().toString();
                    out.println("    private final " + getFieldTypeName(fieldElement) + " " + key + ";");
                }
                out.println();

                // constructor's parameters
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, Field> entry : fields.entrySet()) {
                    Field field = entry.getValue();
                    Element fieldElement = field.getElement();
                    String key = fieldElement.getSimpleName().toString();
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(getFieldTypeName(fieldElement));
                    sb.append(" ");
                    sb.append(key);
                }

                // constructor
                out.println("    public " + className + "(" + sb.toString() + ") {");
                for (Map.Entry<String, Field> entry : fields.entrySet()) {
                    Field field = entry.getValue();
                    Element fieldElement = field.getElement();
                    String key = fieldElement.getSimpleName().toString();
                    out.println("        this." + key + " = " + key + ";");
                }
                out.println("    }");
                out.println();

                // getters
                for (Map.Entry<String, Field> entry : fields.entrySet()) {
                    Field field = entry.getValue();
                    Element fieldElement = field.getElement();
                    String key = fieldElement.getSimpleName().toString();
                    out.println("    public " + getFieldTypeName(fieldElement) + " " + getFieldNameGetter(key) + " {");
                    out.println("        return " + key + ";");
                    out.println("    }");
                    out.println();
                }

                // to Intent
                out.println("    public Intent toIntent() {");
                out.println("        Intent intent = new Intent();");
                for (Map.Entry<String, Field> entry : fields.entrySet()) {
                    Field field = entry.getValue();
                    Element fieldElement = field.getElement();
                    String key = fieldElement.getSimpleName().toString();
                    out.println("        intent.putExtra(\"" + key + "\", " + key + ");");
                }
                out.println("        return intent;");
                out.println("    }");
                out.println();

                // from Intent
                out.println("    public static " + className + " fromIntent(Intent intent) {");
                out.println("        return new " + className + "(");
                int size = fields.size() - 1;
                int count = 0;
                for (Map.Entry<String, Field> entry : fields.entrySet()) {
                    count++;
                    Field field = entry.getValue();
                    Element fieldElement = field.getElement();
                    String key = fieldElement.getSimpleName().toString();
                    String type = getFirstUpperFieldTypeName(fieldElement);
                    String defaultValue = type.equals("String") ? "" : getCommaDefaultValue(fieldElement, field.getValues());
                    out.print("                intent.get" + type + "Extra(\"" + key + "\""
                        + defaultValue + ")");
                    if (count <= size) {
                        out.print(",");
                    }
                    out.println();
                }
                out.println("        );");
                out.println("    }");
                out.println();

                // to Bundle
                out.println("    public Bundle toBundle() {");
                out.println("        Bundle bundle = new Bundle();");
                for (Map.Entry<String, Field> entry : fields.entrySet()) {
                    Field field = entry.getValue();
                    Element fieldElement = field.getElement();
                    String key = fieldElement.getSimpleName().toString();
                    out.println("        bundle.put" + getFirstUpperFieldTypeName(fieldElement) + "(\"" + key + "\", " + key + ");");
                }
                out.println("        return bundle;");
                out.println("    }");
                out.println();

                // from Bundle
                out.println("    public static " + className + " fromBundle(Bundle bundle) {");
                out.println("        return new " + className + "(");
                count = 0;
                for (Map.Entry<String, Field> entry : fields.entrySet()) {
                    count++;
                    Field field = entry.getValue();
                    Element fieldElement = field.getElement();
                    String key = fieldElement.getSimpleName().toString();
                    out.print("                bundle.get" + getFirstUpperFieldTypeName(fieldElement) + "(\"" + key + "\""
                        + getCommaDefaultValue(fieldElement, field.getValues()) + ")");
                    if (count <= size) {
                        out.print(",");
                    }
                    out.println();
                }
                out.println("        );");
                out.println("    }");
                out.println();

                out.println("}");
            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
    }

    @Override
    protected String getCommaDefaultValue(Element element, Values values) {
        String typeName = element.asType().toString().toLowerCase();
        switch (typeName) {
            case "int":
                return ", " + values.intValue;
            case "long":
                return ", " + values.longValue;
            case "boolean":
                return ", " + values.booleanValue;
            case "float":
                return ", " + values.floatValue + "F";
            case "double":
                return ", " + values.doubleValue;
            case "short":
                return ", (short) " + values.shortValue;
            case "byte":
                return ", (byte) " + values.byteValue;
            case "java.lang.string":
                return values.stringValue.isEmpty() ? "" : ", \"" + values.stringValue + "\"";
            default:
                return ", null";
            //throw new IllegalArgumentException(typeName);
        }
    }

    private String getFieldTypeName(Element element) {
        return element.asType().toString().replace("java.lang.", "");
    }

    private String getFieldNameGetter(String name) {
        return "get" + getFirstUpperFieldTypeName(name) + "()";
    }

}
