package merkulyevsasha.ru.builders;


import java.io.PrintWriter;
import java.util.List;

import javax.lang.model.element.Element;

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
        out.println(getClassSpec(classSpec));

        MethodSpec constructor = classSpec.getConstructorSpec();
        if (constructor != null) {
            // fields
            for (MethodParams methodParams : constructor.getParams()) {
                out.println("    private final " + getFieldTypeName(methodParams.getElement()) + " " + methodParams.getParamName() + ";");
            }
            out.println();

            // constructor's parameters
            StringBuilder sb = new StringBuilder();
            for (MethodParams methodParams : constructor.getParams()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(getFieldTypeName(methodParams.getElement()));
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
                out.println("    public " + getFieldTypeName(methodParams.getElement()) + " " + getFieldNameGetter(methodParams.getParamName()) + " {");
                out.println("        return " + methodParams.getParamName() + ";");
                out.println("    }");
                out.println();
            }
        }

        writeMethodsTo(out, classSpec.getMethodSpecs());
        writeMethodsTo(out, classSpec.getStaticMethodSpecs());

        out.println("}");
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

    private String getAccessModifier(ClassSpec.AccessModifier accessModifier) {
        switch (accessModifier) {
            case PUBLIC:
                return "public";
            case PRIVATE:
                return "private";
            default:
                return "";
        }
    }

    private String getInheritanceModifier(ClassSpec.InheritanceModifier inheritanceModifier) {
        switch (inheritanceModifier) {
            case FINAL:
                return "final";
            case OPEN:
                return "open";
            case STATIC:
                return "static";
            case NOTHING:
                return "";
            default:
                return "";
        }
    }

    private String getClassSpec(ClassSpec classSpec) {
        StringBuilder sb = new StringBuilder();
        String s = getAccessModifier(classSpec.getAccessModifier());
        if (!s.isEmpty()) {
            sb.append(s);
            sb.append(" ");
        }
        s = getInheritanceModifier(classSpec.getInheritanceModifier());
        if (!s.isEmpty()) {
            sb.append(s);
            sb.append(" ");
        }
        sb.append("class");
        sb.append(" ");
        sb.append(classSpec.getClassName());
        sb.append(" ");
        sb.append("{");
        return sb.toString();
    }

    private String getAccessModifier(MethodSpec.AccessModifier accessModifier) {
        switch (accessModifier) {
            case PUBLIC:
                return "public";
            case PRIVATE:
                return "private";
            default:
                return "";
        }
    }

    private String getInheritanceModifier(MethodSpec.InheritanceModifier inheritanceModifier) {
        switch (inheritanceModifier) {
            case FINAL:
                return "final";
            case STATIC:
                return "static";
            case NOTHING:
                return "";
            default:
                return "";
        }
    }

    private String indent(int spaces) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < spaces; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }

    private void writeMethodsTo(PrintWriter out, List<MethodSpec> methodSpecs) {
        for (MethodSpec methodSpec : methodSpecs) {
            StringBuilder msb = new StringBuilder();
            String s = getAccessModifier(methodSpec.getAccessModifier());
            if (!s.isEmpty()) {
                msb.append(s);
                msb.append(" ");
            }
            s = getInheritanceModifier(methodSpec.getInheritanceModifier());
            if (!s.isEmpty()) {
                msb.append(s);
                msb.append(" ");
            }
            msb.append(methodSpec.getReturnType());
            msb.append(" ");
            msb.append(methodSpec.getName());
            msb.append("(").append(getMethodParams(methodSpec.getParams())).append(")");
            out.print(indent(4) + msb.toString());
            out.println(" {");

            for (String ss : methodSpec.getStatements()) {
                out.println(indent(8) + ss);
            }
            out.println(indent(4) + "}");
            out.println();
        }
    }

    private String getMethodParams(List<MethodParams> methodParams) {
        StringBuilder sb = new StringBuilder();
        for (MethodParams param : methodParams) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(param.getTypeName());
            sb.append(" ");
            sb.append(param.getParamName());
        }
        return sb.toString();
    }

}
