package merkulyevsasha.ru.processors.args;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

import merkulyevsasha.ru.processors.Field;
import merkulyevsasha.ru.processors.Values;

public class ArgsKotlinCodeGenerator extends BaseArgsCodeGenerator {

    public ArgsKotlinCodeGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    protected void generateClass(String packageName, String className, LinkedHashMap<String, Field> fields) {

        int size = fields.size() - 1;

        try {
            File ktFile = new File(generatedSourcesRoot + File.separator + className + ".kt");
            try (PrintWriter out = new PrintWriter(new FileWriter(ktFile))) {

                if (packageName != null) {
                    out.println("package " + packageName);
                }
                out.println();
                out.println("import android.content.Intent");
                out.println("import android.os.Bundle");
                out.println();

                // class
                out.println("data class " + className + "(");
                // primary constructor
                int count = 0;
                for (Map.Entry<String, Field> entry : fields.entrySet()) {
                    count++;
                    Field field = entry.getValue();
                    Element fieldElement = field.getElement();
                    String key = fieldElement.getSimpleName().toString();
                    out.print("    val " + key + ": " + getFirstUpperFieldTypeName(fieldElement));
                    if (count <= size) {
                        out.print(",");
                    }
                    out.println();
                }
                out.println(") {");

                // to Intent
                out.println("    fun toIntent(): Intent {");
                out.println("        val intent = Intent()");
                for (Map.Entry<String, Field> entry : fields.entrySet()) {
                    Field field = entry.getValue();
                    Element fieldElement = field.getElement();
                    String key = fieldElement.getSimpleName().toString();
                    out.println("        intent.putExtra(\"" + key + "\", " + key + ")");
                }
                out.println("        return intent");
                out.println("    }");
                out.println();

                // to Bundle
                out.println("    fun toBundle(): Bundle {");
                out.println("        val bundle = Bundle()");
                for (Map.Entry<String, Field> entry : fields.entrySet()) {
                    Field field = entry.getValue();
                    Element fieldElement = field.getElement();
                    String key = fieldElement.getSimpleName().toString();
                    out.println("        bundle.put" + getFirstUpperFieldTypeName(fieldElement) + "(\"" + key + "\", " + key + ")");
                }
                out.println("        return bundle");
                out.println("    }");
                out.println();

                // companion object
                out.print("    companion object {");
                out.print("\n");

                // from Intent
                out.println("        @JvmStatic");
                out.println("        fun fromIntent(intent: Intent): " + className + " {");
                out.println("            return " + className + "(");
                count = 0;
                for (Map.Entry<String, Field> entry : fields.entrySet()) {
                    count++;
                    Field field = entry.getValue();
                    Element fieldElement = field.getElement();
                    String key = fieldElement.getSimpleName().toString();
                    String type = getFirstUpperFieldTypeName(fieldElement);
                    String defaultValue = type.equals("String") ? "" : getCommaDefaultValue(fieldElement, field.getValues());
                    out.print("                intent.get" + getFirstUpperFieldTypeName(fieldElement) + "Extra(\"" + key + "\""
                        + defaultValue + ")");
                    if (count <= size) {
                        out.print(",");
                    }
                    out.println();
                }
                out.println("            )");
                out.println("        }");
                out.println();

                // from Bundle
                out.println("        @JvmStatic");
                out.println("        fun fromBundle(bundle: Bundle): " + className + " {");
                out.println("            return " + className + "(");
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
                out.println("            )");
                out.println("        }");
                out.println();

                out.println("    }");

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
                return ", " + values.shortValue;
            case "byte":
                return ", " + values.byteValue;
            case "java.lang.string":
                return values.stringValue.isEmpty() ? "" : ", \"" + values.stringValue + "\"";
            default:
                //return ", null";
                throw new IllegalArgumentException(typeName);
        }
    }

}
