package merkulyevsasha.ru.processors;

import java.util.LinkedHashMap;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

import merkulyevsasha.ru.annotations.Source;

public class BaseCodeGenerator {

    public final static String KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated";
    public final static String FOLDER_ERROR_MESSAGE = "Can't find the target directory for generated Kotlin files.";

    protected final ProcessingEnvironment processingEnv;
    protected final String generatedSourcesRoot;

    public BaseCodeGenerator(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
        generatedSourcesRoot = processingEnv.getOptions().get(KAPT_KOTLIN_GENERATED_OPTION_NAME);
    }

    protected LinkedHashMap<String, Element> getTypeElementFields(Element type) {
        LinkedHashMap<String, Element> typeElements = new LinkedHashMap<>();
        for (Element element : type.getEnclosedElements()) {
            if (element.getKind() != ElementKind.FIELD) continue;
            typeElements.put(element.toString(), element);
        }
        return typeElements;
    }

    protected String getFirstUpperFieldTypeName(Element element) {
        String typeName = element.asType().toString().toLowerCase().replace("java.lang.", "");
        return typeName.substring(0, 1).toUpperCase() + typeName.substring(1);
    }

    protected String getCommaDefaultValue(Element element, Source source) {
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
                return source == Source.Java ? ", (short) 0" : ", 0";
            case "byte":
                return source == Source.Java ? ", (byte) 0" : ", 0";
            case "java.lang.string":
                return "";
            default:
                return ", null";
            //throw new IllegalArgumentException(typeName);
        }
    }
}
