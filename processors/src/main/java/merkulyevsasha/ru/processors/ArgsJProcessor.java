package merkulyevsasha.ru.processors;

import com.google.auto.service.AutoService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import merkulyevsasha.ru.annotations.ArgsJ;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(ArgsJProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
public class ArgsJProcessor extends AbstractProcessor {

    final static String KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(ArgsJ.class);
        for (Element element : elements) {
            if (element.getKind() != ElementKind.CLASS) continue;
            TypeElement typeElement = (TypeElement) element;
            generateClass(typeElement, processingEnv.getElementUtils().getPackageOf(typeElement).toString());
        }
        return false;
    }

    private void generateClass(TypeElement typeElement, String packageOfMethod) {
        String generatedSourcesRoot = processingEnv.getOptions().get(KAPT_KOTLIN_GENERATED_OPTION_NAME);
        if (generatedSourcesRoot == null || generatedSourcesRoot.isEmpty()) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Can't find the target directory for generated Kotlin files.");
            return;
        }

        try {
            writeBuilderFile(packageOfMethod, typeElement);
        } catch (IOException e) {
            e.printStackTrace();
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
    }

    private void writeBuilderFile(String packageName, TypeElement type) throws IOException {
        String className = type.getSimpleName().toString();
        String builderSimpleClassName = className + "Args";
        JavaFileObject builderFile;
        try {
            builderFile = processingEnv.getFiler()
                    .createSourceFile(builderSimpleClassName);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        LinkedHashMap<String, Element> mainElements = getMapElement(type);

        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {

            if (packageName != null) {
                out.println("package " + packageName + ";");
            }
            out.println();
            out.println("import android.content.Intent;");
            out.println("import android.os.Bundle;");
            out.println();

            // class
            out.println("public class " + builderSimpleClassName + " {");
            out.println();

            // fields
            for (Map.Entry<String, Element> entry : mainElements.entrySet()) {
                Element childElement = entry.getValue();
                String key = childElement.getSimpleName().toString();
                out.println("    private final " + asType(childElement) + " " + key + ";");
            }
            out.println();

            // constructor's parameters
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Element> entry : mainElements.entrySet()) {
                Element childElement = entry.getValue();
                String key = childElement.getSimpleName().toString();
                if (sb.length() > 0) sb.append(", ");
                sb.append(asType(childElement));
                sb.append(" ");
                sb.append(key);
            }

            // constructor
            out.println("    public " + builderSimpleClassName + "(" + sb.toString() + ") {");
            for (Map.Entry<String, Element> entry : mainElements.entrySet()) {
                Element childElement = entry.getValue();
                String key = childElement.getSimpleName().toString();
                out.println("        this." + key + " = " + key + ";");
            }
            out.println("    }");
            out.println();

            // getters
            for (Map.Entry<String, Element> entry : mainElements.entrySet()) {
                Element childElement = entry.getValue();
                String key = childElement.getSimpleName().toString();
                out.println("    public " + asType(childElement) + " " + getFieldNameGetter(key) + " {");
                out.println("        return " + key + ";");
                out.println("    }");
                out.println();
            }

            // to Intent
            out.println("    public Intent toIntent() {");
            out.println("        Intent intent = new Intent();");
            for (Map.Entry<String, Element> entry : mainElements.entrySet()) {
                Element childElement = entry.getValue();
                String key = childElement.getSimpleName().toString();
                out.println("        intent.putExtra(\"" + key + "\", " + key + ");");
            }
            out.println("        return intent;");
            out.println("    }");
            out.println();

            // from Intent
            out.println("    public static " + builderSimpleClassName + " fromIntent(Intent intent) {");
            out.println("        return new " + builderSimpleClassName + "(");
            int size = mainElements.size() - 1;
            int count = 0;
            for (Map.Entry<String, Element> entry : mainElements.entrySet()) {
                count++;
                Element childElement = entry.getValue();
                String key = childElement.getSimpleName().toString();
                out.print("                intent.get" + getBundlePutForType(childElement) + "Extra(\"" + key + "\"" + getCommaDefaultValue(childElement) + ")");
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
            for (Map.Entry<String, Element> entry : mainElements.entrySet()) {
                Element childElement = entry.getValue();
                String key = childElement.getSimpleName().toString();
                out.println("        bundle.put" + getBundlePutForType(childElement) + "(\"" + key + "\", " + key + ");");
            }
            out.println("        return bundle;");
            out.println("    }");
            out.println();

            // from Bundle
            out.println("    public static " + builderSimpleClassName + " fromBundle(Bundle bundle) {");
            out.println("        return new " + builderSimpleClassName + "(");
            count = 0;
            for (Map.Entry<String, Element> entry : mainElements.entrySet()) {
                count++;
                Element childElement = entry.getValue();
                String key = childElement.getSimpleName().toString();
                out.print("                bundle.get" + getBundlePutForType(childElement) + "(\"" + key + "\"" + getCommaDefaultValue(childElement) + ")");
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
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> result = new HashSet<>();
        result.add(ArgsJ.class.getCanonicalName());
        return result;
    }

    private String getCommaDefaultValue(Element element) {
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
                return ", (short) 0";
            case "byte":
                return ", (byte) 0";
            case "java.lang.string":
                return "";
            default:
                return ", null";
            //throw new IllegalArgumentException(typeName);
        }
    }

    private String getBundlePutForType(Element element) {
        String typeName = element.asType().toString().toLowerCase().replace("java.lang.", "");
        return typeName.substring(0, 1).toUpperCase() + typeName.substring(1);
    }

    private String asType(Element element) {
        return element.asType().toString().replace("java.lang.", "");
    }

    private String getFieldNameGetter(String fieldName) {
        String firstUpper = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
        return "get" + firstUpper + "()";
    }

    private LinkedHashMap<String, Element> getMapElement(TypeElement type) {
        LinkedHashMap<String, Element> typeElements = new LinkedHashMap<>();
        for (Element element : type.getEnclosedElements()) {
            if (element.getKind() != ElementKind.FIELD) continue;
            typeElements.put(element.toString(), element);
        }
        return typeElements;
    }

}
