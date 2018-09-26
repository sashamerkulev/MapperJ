package merkulyevsasha.ru.processors.args;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import merkulyevsasha.ru.processors.BaseCodeGenerator;
import merkulyevsasha.ru.processors.CodeGenerator;
import merkulyevsasha.ru.processors.Field;

abstract class BaseArgsCodeGenerator extends BaseCodeGenerator implements CodeGenerator {

    BaseArgsCodeGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    public void generate(String packageName, TypeElement typeElement) {
        if (generatedSourcesRoot == null || generatedSourcesRoot.isEmpty()) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, BaseCodeGenerator.FOLDER_ERROR_MESSAGE);
            return;
        }

        final LinkedHashMap<String, Field> elementFields = fieldParser.getElementFields(typeElement);
        final LinkedHashMap<String, Field> fields = new LinkedHashMap<>();
        for (Map.Entry<String, Field> entry : elementFields.entrySet()) {
            Field field = entry.getValue();
            if (field.getIgnoreAnnotation() != null) continue;
            fields.put(entry.getKey(), field);
        }
        elementFields.clear();
        try {
            generateClass(packageName, typeElement.getSimpleName().toString() + "Args", fields);
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
    }

    protected abstract void generateClass(String packageName, String className, LinkedHashMap<String, Field> fields) throws IOException;
}
