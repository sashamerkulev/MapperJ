package merkulyevsasha.ru.processors;

import java.util.LinkedHashMap;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;

public class BaseCodeGenerator {

    public final static String KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated";
    public final static String FOLDER_ERROR_MESSAGE = "Can't find the target directory for generated Kotlin files.";

    protected final ProcessingEnvironment processingEnv;
    protected final String generatedSourcesRoot;
    protected final ElementFieldParser fieldParser;

    public BaseCodeGenerator(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
        generatedSourcesRoot = processingEnv.getOptions().get(KAPT_KOTLIN_GENERATED_OPTION_NAME);
        fieldParser = new ElementFieldParser();
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

    protected String getFirstUpperFieldTypeName(String name) {
        String typeName = name.toLowerCase().replace("java.lang.", "");
        return typeName.substring(0, 1).toUpperCase() + typeName.substring(1);
    }
}
