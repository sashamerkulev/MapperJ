package merkulyevsasha.ru.processors.args;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import merkulyevsasha.ru.annotations.Source;

public class ArgsJavaCodeGenerator extends BaseArgsCodeGenerator {

    public ArgsJavaCodeGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    void generateClass(String packageName, TypeElement typeElement) {
        String className = typeElement.getSimpleName().toString() + "Args";
        try {
            JavaFileObject builderFile = processingEnv.getFiler()
                .createSourceFile(className);

            LinkedHashMap<String, Element> fields = getTypeElementFields(typeElement);

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
                for (Map.Entry<String, Element> entry : fields.entrySet()) {
                    Element field = entry.getValue();
                    String key = field.getSimpleName().toString();
                    out.println("    private final " + getFieldTypeName(field) + " " + key + ";");
                }
                out.println();

                // constructor's parameters
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, Element> entry : fields.entrySet()) {
                    Element field = entry.getValue();
                    String key = field.getSimpleName().toString();
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(getFieldTypeName(field));
                    sb.append(" ");
                    sb.append(key);
                }

                // constructor
                out.println("    public " + className + "(" + sb.toString() + ") {");
                for (Map.Entry<String, Element> entry : fields.entrySet()) {
                    Element field = entry.getValue();
                    String key = field.getSimpleName().toString();
                    out.println("        this." + key + " = " + key + ";");
                }
                out.println("    }");
                out.println();

                // getters
                for (Map.Entry<String, Element> entry : fields.entrySet()) {
                    Element field = entry.getValue();
                    String key = field.getSimpleName().toString();
                    out.println("    public " + getFieldTypeName(field) + " " + getFieldNameGetter(field) + " {");
                    out.println("        return " + key + ";");
                    out.println("    }");
                    out.println();
                }

                // to Intent
                out.println("    public Intent toIntent() {");
                out.println("        Intent intent = new Intent();");
                for (Map.Entry<String, Element> entry : fields.entrySet()) {
                    Element field = entry.getValue();
                    String key = field.getSimpleName().toString();
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
                for (Map.Entry<String, Element> entry : fields.entrySet()) {
                    count++;
                    Element field = entry.getValue();
                    String key = field.getSimpleName().toString();
                    out.print("                intent.get" + getFirstUpperFieldTypeName(field) + "Extra(\"" + key + "\""
                        + getCommaDefaultValue(field, Source.Java) + ")");
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
                for (Map.Entry<String, Element> entry : fields.entrySet()) {
                    Element field = entry.getValue();
                    String key = field.getSimpleName().toString();
                    out.println("        bundle.put" + getFirstUpperFieldTypeName(field) + "(\"" + key + "\", " + key + ");");
                }
                out.println("        return bundle;");
                out.println("    }");
                out.println();

                // from Bundle
                out.println("    public static " + className + " fromBundle(Bundle bundle) {");
                out.println("        return new " + className + "(");
                count = 0;
                for (Map.Entry<String, Element> entry : fields.entrySet()) {
                    count++;
                    Element field = entry.getValue();
                    String key = field.getSimpleName().toString();
                    out.print("                bundle.get" + getFirstUpperFieldTypeName(field) + "(\"" + key + "\""
                        + getCommaDefaultValue(field, Source.Java) + ")");
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

    private String getFieldTypeName(Element element) {
        return element.asType().toString().replace("java.lang.", "");
    }

    private String getFieldNameGetter(Element element) {
        return "get" + getFirstUpperFieldTypeName(element) + "()";
    }

}
