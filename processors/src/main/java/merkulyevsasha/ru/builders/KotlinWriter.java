package merkulyevsasha.ru.builders;


import java.io.PrintWriter;
import java.util.List;

import javax.lang.model.element.Element;

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

        // class
        StringBuilder classSpecString = new StringBuilder();
        String s = getAccessModifier(classSpec.getAccessModifier());
        if (!s.isEmpty()) {
            classSpecString.append(s);
            classSpecString.append(" ");
        }
        s = getInheritanceModifier(classSpec.getInheritanceModifier());
        if (!s.isEmpty()) {
            classSpecString.append(s);
            classSpecString.append(" ");
        }
        classSpecString.append("class");
        classSpecString.append(" ");
        classSpecString.append(classSpec.getClassName());

        //constructor
        MethodSpec constructor = classSpec.getConstructorSpec();
        if (constructor != null) {
            int size = constructor.getParams().size() - 1;
            int count = 0;
            classSpecString.append("(");
            out.println(classSpecString.toString());

            for (MethodParams methodParams : constructor.getParams()) {
                count++;
                out.print("    val " + methodParams.getParamName() + ": " + getFirstUpperFieldTypeName(methodParams.getElement()));
                if (count <= size) {
                    out.print(",");
                }
                out.println();
            }
            out.println(") {");
        } else {
            classSpecString.append(" {");
            out.println(classSpecString.toString());
        }

        // methods
        writeMethodsTo(out, classSpec.getMethodSpecs(), 0);

        // static methods
        List<MethodSpec> staticMethods = classSpec.getStaticMethodSpecs();
        if (staticMethods.size() > 0) {
            // companion object
            out.println("    companion object {");
            writeMethodsTo(out, staticMethods, 4);
            out.println("    }");
        }

        out.println("}");
    }

    private String getFirstUpperFieldTypeName(Element element) {
        String typeName = element.asType().toString().toLowerCase().replace("java.lang.", "");
        return typeName.substring(0, 1).toUpperCase() + typeName.substring(1);
    }

    private String getAccessModifier(ClassSpec.AccessModifier accessModifier) {
        switch (accessModifier) {
            case PUBLIC:
                return "";
            case PRIVATE:
                return "private";
            default:
                return "";
        }
    }

    private String getInheritanceModifier(ClassSpec.InheritanceModifier inheritanceModifier) {
        switch (inheritanceModifier) {
            case DATA:
                return "data";
            case OPEN:
                return "open";
            case NOTHING:
                return "";
            default:
                return "";
        }
    }

    private String getAccessModifier(MethodSpec.AccessModifier accessModifier) {
        switch (accessModifier) {
            case PUBLIC:
                return "";
            case PRIVATE:
                return "private";
            default:
                return "";
        }
    }

    private String getInheritanceModifier(MethodSpec.InheritanceModifier inheritanceModifier) {
        return "";
    }

    private String indent(int spaces) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < spaces; i++) {
            sb.append(" ");
        }
        return sb.toString();
    }

    private void writeMethodsTo(PrintWriter out, List<MethodSpec> methodSpecs, int baseIndent) {
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
            msb.append("fun ");
            msb.append(methodSpec.getName());
            msb.append("(").append(getMethodParams(methodSpec.getParams())).append(")");
            String returnType = methodSpec.getReturnType();
            if (!returnType.isEmpty()) {
                msb.append(": ");
                msb.append(returnType);
            }
            if (methodSpec.getInheritanceModifier() == MethodSpec.InheritanceModifier.STATIC) {
                out.println(indent(baseIndent + 4) + "@JvmStatic");
            }
            out.print(indent(baseIndent + 4) + msb.toString());
            out.println(" {");

            for (String ss : methodSpec.getStatements()) {
                out.println(indent(baseIndent + 8) + ss);
            }
            out.println(indent(baseIndent + 4) + "}");
            out.println();
        }
    }

    private String getMethodParams(List<MethodParams> methodParams) {
        StringBuilder sb = new StringBuilder();
        for (MethodParams param : methodParams) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(param.getParamName());
            sb.append(": ");
            sb.append(param.getTypeName());
        }
        return sb.toString();
    }

}
