package merkulyevsasha.ru.builders;


import java.io.PrintWriter;
import java.util.List;

import javax.lang.model.element.Element;

import merkulyevsasha.ru.processors.Values;

public class KotlinWriter implements FileSource.SourceWriter {
    @Override
    public void write(PrintWriter out, String fileName, String packageName, ClassSpec classSpec, List<String> packages) {
        if (packageName != null && !packageName.isEmpty()) {
            out.println("package " + packageName);
            out.println();
        }

        for (String importName : packages) {
            out.println("import " + importName);
        }
        if (packages.size() > 0) {
            out.println();
        }

        MethodSpec constructor = classSpec.getConstructorSpec();
        int size = constructor.getParams().size() - 1;
        int count = 0;

        // class
        out.println("data class " + classSpec.getClassName() + " (");

        for (MethodParams methodParams : constructor.getParams()) {
            count++;
            out.print("    val " + methodParams.getParamName() + ": " + getFirstUpperFieldTypeName(methodParams.getTypeName()));
            if (count <= size) {
                out.print(",");
            }
            out.println();
        }
        out.println(") {");

        // to Intent
        out.println("    fun toIntent(): Intent {");
        out.println("        val intent = Intent()");
        for (MethodParams methodParams : constructor.getParams()) {
            out.println("        intent.putExtra(\"" + methodParams.getParamName() + "\", " + methodParams.getParamName() + ")");
        }
        out.println("        return intent");
        out.println("    }");
        out.println();
        // to Bundle
        out.println("    fun toBundle(): Bundle {");
        out.println("        val bundle = Bundle()");
        for (MethodParams methodParams : constructor.getParams()) {
            out.println("        bundle.put" + getFirstUpperFieldTypeName(methodParams.getTypeName()) + "(\"" + methodParams.getParamName() + "\", " + methodParams.getParamName() + ")");
        }
        out.println("        return bundle");
        out.println("    }");
        out.println();

        // companion object
        out.println("    companion object {");
        // from Intent
        out.println("        @JvmStatic");
        out.println("        fun fromIntent(intent: Intent): " + classSpec.getClassName() + " {");
        out.println("            return " + classSpec.getClassName() + "(");
        count = 0;
        for (MethodParams methodParams : constructor.getParams()) {
            count++;
            String type = getFirstUpperFieldTypeName(methodParams.getTypeName());
            String defaultValue = type.equals("String") ? "" : getCommaDefaultValue(methodParams.getTypeName(), methodParams.getValues());
            out.print("                intent.get" + getFirstUpperFieldTypeName(methodParams.getTypeName()) + "Extra(\"" + methodParams.getParamName() + "\""
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
        out.println("        fun fromBundle(bundle: Bundle): " + classSpec.getClassName() + " {");
        out.println("            return " + classSpec.getClassName() + "(");
        count = 0;
        for (MethodParams methodParams : constructor.getParams()) {
            count++;
            out.print("                bundle.get" + getFirstUpperFieldTypeName(methodParams.getTypeName()) + "(\"" + methodParams.getParamName() + "\""
                + getCommaDefaultValue(methodParams.getTypeName(), methodParams.getValues()) + ")");
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

    private String getFirstUpperFieldTypeName(Element element) {
        String typeName = element.asType().toString().toLowerCase().replace("java.lang.", "");
        return typeName.substring(0, 1).toUpperCase() + typeName.substring(1);
    }

    private String getCommaDefaultValue(Element element, Values values) {
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
