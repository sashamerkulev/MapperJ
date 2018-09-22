package merkulyevsasha.ru.processors.args;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

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

    protected abstract void generateClass(String packageName, TypeElement typeElement);

    protected abstract String getCommaDefaultValue(Element element);
}
