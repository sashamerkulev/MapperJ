package merkulyevsasha.ru.processors.args;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

public class ArgsKotlinCodeGenerator extends BaseArgsCodeGenerator {

    public ArgsKotlinCodeGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    protected void generateClass(String packageName, TypeElement typeElement) {
        String className = typeElement.getSimpleName().toString() + "Args";

        LinkedHashMap<String, Element> fields = getTypeElementFields(typeElement);
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
                for (Map.Entry<String, Element> entry : fields.entrySet()) {
                    count++;
                    Element field = entry.getValue();
                    String key = field.getSimpleName().toString();
                    out.print("    val " + key + ": " + getFirstUpperFieldTypeName(field));
                    if (count <= size) {
                        out.print(",");
                    }
                    out.println();
                }
                out.println(") {");

                // to Intent
                out.println("    fun toIntent(): Intent {");
                out.println("        val intent = Intent()");
                for (Map.Entry<String, Element> entry : fields.entrySet()) {
                    Element field = entry.getValue();
                    String key = field.getSimpleName().toString();
                    out.println("        intent.putExtra(\"" + key + "\", " + key + ")");
                }
                out.println("        return intent");
                out.println("    }");
                out.println();

                // to Bundle
                out.println("    fun toBundle(): Bundle {");
                out.println("        val bundle = Bundle()");
                for (Map.Entry<String, Element> entry : fields.entrySet()) {
                    Element field = entry.getValue();
                    String key = field.getSimpleName().toString();
                    out.println("        bundle.put" + getFirstUpperFieldTypeName(field) + "(\"" + key + "\", " + key + ")");
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
                for (Map.Entry<String, Element> entry : fields.entrySet()) {
                    count++;
                    Element field = entry.getValue();
                    String key = field.getSimpleName().toString();
                    out.print("                intent.get" + getFirstUpperFieldTypeName(field) + "Extra(\"" + key + "\""
                        + getCommaDefaultValue(field) + ")");
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
                for (Map.Entry<String, Element> entry : fields.entrySet()) {
                    count++;
                    Element field = entry.getValue();
                    String key = field.getSimpleName().toString();
                    out.print("                bundle.get" + getFirstUpperFieldTypeName(field) + "(\"" + key + "\""
                        + getCommaDefaultValue(field) + ")");
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
    protected String getCommaDefaultValue(Element element) {
        String typeName = element.asType().toString().toLowerCase();
        switch (typeName) {
            case "int":
                return ", 0";
            case "long":
                return ", 0";
            case "boolean":
                return ", false";
            case "float":
                return ", 0F";
            case "double":
                return ", 0.0";
            case "short":
                return ", 0";
            case "byte":
                return ", 0";
            case "java.lang.string":
                return "";
            default:
                return ", null";
            //throw new IllegalArgumentException(typeName);
        }
    }

}
