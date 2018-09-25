package merkulyevsasha.ru.builders;


import java.io.PrintWriter;
import java.util.List;

import javax.lang.model.element.Element;

import merkulyevsasha.ru.processors.Values;

public class JavaWriter implements FileSource.SourceWriter {
    @Override
    public void write(PrintWriter out, String fileName, String packageName, ClassSpec classSpec, List<String> packages) {

        if (packageName != null && !packageName.isEmpty()) {
            out.println("package " + packageName + ";");
            out.println();
        }

        for (String importName : packages) {
            out.println("import " + importName + ";");
        }
        if (packages.size() > 0) {
            out.println();
        }

        // class
        out.println("public class " + classSpec.getClassName() + " {");

        // fields
        MethodSpec constructor = classSpec.getConstructorSpec();
        for (MethodParams methodParams : constructor.getParams()) {
            out.println("    private final " + getFieldTypeName(methodParams.getTypeName()) + " " + methodParams.getParamName() + ";");
        }
        out.println();

        // constructor's parameters
        StringBuilder sb = new StringBuilder();
        for (MethodParams methodParams : constructor.getParams()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(getFieldTypeName(methodParams.getTypeName()));
            sb.append(" ");
            sb.append(methodParams.getParamName());
        }

        // constructor
        out.println("    public " + classSpec.getClassName() + "(" + sb.toString() + ") {");
        for (MethodParams methodParams : constructor.getParams()) {
            out.println("        this." + methodParams.getParamName() + " = " + methodParams.getParamName() + ";");
        }
        out.println("    }");
        out.println();

        // getters
        for (MethodParams methodParams : constructor.getParams()) {
            out.println("    public " + getFieldTypeName(methodParams.getTypeName()) + " " + getFieldNameGetter(methodParams.getParamName()) + " {");
            out.println("        return " + methodParams.getParamName() + ";");
            out.println("    }");
            out.println();
        }

        // to Intent
        out.println("    public Intent toIntent() {");
        out.println("        Intent intent = new Intent();");
        for (MethodParams methodParams : constructor.getParams()) {
            out.println("        intent.putExtra(\"" + methodParams.getParamName() + "\", " + methodParams.getParamName() + ");");
        }
        out.println("        return intent;");
        out.println("    }");
        out.println();

        // from Intent
        out.println("    public static " + classSpec.getClassName() + " fromIntent(Intent intent) {");
        out.println("        return new " + classSpec.getClassName() + "(");
        int size = constructor.getParams().size() - 1;
        int count = 0;
        for (MethodParams methodParams : constructor.getParams()) {
            count++;
            String type = getFirstUpperFieldTypeName(methodParams.getTypeName());
            String defaultValue = type.equals("String") ? "" : getCommaDefaultValue(methodParams.getTypeName(), methodParams.getValues());
            out.print("                intent.get" + type + "Extra(\"" + methodParams.getParamName() + "\""
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
        for (MethodParams methodParams : constructor.getParams()) {
            out.println("        bundle.put" + getFirstUpperFieldTypeName(methodParams.getTypeName()) +
                "(\"" + methodParams.getParamName() + "\", " + methodParams.getParamName() + ");");
        }
        out.println("        return bundle;");
        out.println("    }");
        out.println();

        // from Bundle
        out.println("    public static " + classSpec.getClassName() + " fromBundle(Bundle bundle) {");
        out.println("        return new " + classSpec.getClassName() + "(");
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
        out.println("        );");
        out.println("    }");
        out.println();

        out.println("}");
    }

    private String getFirstUpperFieldTypeName(Element element) {
        String typeName = element.asType().toString().toLowerCase().replace("java.lang.", "");
        return typeName.substring(0, 1).toUpperCase() + typeName.substring(1);
    }

    private String getFirstUpperFieldTypeName(String name) {
        String typeName = name.toLowerCase().replace("java.lang.", "");
        return typeName.substring(0, 1).toUpperCase() + typeName.substring(1);
    }

    private String getFieldNameGetter(String name) {
        return "get" + getFirstUpperFieldTypeName(name) + "()";
    }

    private String getFieldTypeName(Element element) {
        return element.asType().toString().replace("java.lang.", "");
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

}
