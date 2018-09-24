package merkulyevsasha.ru.processors;

import javax.annotation.processing.ProcessingEnvironment;

import merkulyevsasha.ru.annotations.params.Source;
import merkulyevsasha.ru.processors.args.ArgsJavaCodeGenerator;
import merkulyevsasha.ru.processors.args.ArgsKotlinCodeGenerator;
import merkulyevsasha.ru.processors.mapper.MapperJavaCodeGenerator;
import merkulyevsasha.ru.processors.mapper.MapperKotlinCodeGenerator;

public final class CodeGeneratorFactory {

    public static CodeGenerator createArgsGenerator(Source source, ProcessingEnvironment processingEnv) {
        switch (source) {
            case Java:
                return new ArgsJavaCodeGenerator(processingEnv);
            case Kotlin:
                return new ArgsKotlinCodeGenerator(processingEnv);
        }
        throw new IllegalArgumentException(source.toString());
    }

    public static CodeGenerator createMapperGenerator(Source source, ProcessingEnvironment processingEnv) {
        switch (source) {
            case Java:
                return new MapperJavaCodeGenerator(processingEnv);
            case Kotlin:
                return new MapperKotlinCodeGenerator(processingEnv);
        }
        throw new IllegalArgumentException(source.toString());
    }
}
