package merkulyevsasha.ru.processors.args;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import merkulyevsasha.ru.annotations.Source;
import merkulyevsasha.ru.processors.BaseCodeGenerator;
import merkulyevsasha.ru.processors.CodeGenerator;

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
        generateClass(packageName, typeElement);
    }

    abstract void generateClass(String packageName, TypeElement typeElement);

    String getCommaDefaultValue(Element element, Source source) {
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
