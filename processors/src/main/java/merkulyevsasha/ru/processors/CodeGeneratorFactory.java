package merkulyevsasha.ru.processors;

import javax.annotation.processing.ProcessingEnvironment;

import merkulyevsasha.ru.annotations.Source;

public final class CodeGeneratorFactory {

    public static CodeGenerator create(Source source, ProcessingEnvironment processingEnv) {
        switch (source) {
            case Java:
                return new JavaCodeGenerator(processingEnv);
            case Kotlin:
                return new KotlinCodeGenerator(processingEnv);
        }
        throw new IllegalArgumentException(source.toString());
    }

}
