package merkulyevsasha.ru.processors.mapper;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

import merkulyevsasha.ru.processors.CodeGenerator;

public class MapperKotlinCodeGenerator extends BaseMapperCodeGenerator implements CodeGenerator {

    public MapperKotlinCodeGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    public void generate(TypeElement typeElement, String packageName) {

    }
}
