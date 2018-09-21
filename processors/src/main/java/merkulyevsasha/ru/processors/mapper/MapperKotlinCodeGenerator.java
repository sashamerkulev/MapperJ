package merkulyevsasha.ru.processors.mapper;

import java.io.IOException;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

import merkulyevsasha.ru.processors.CodeGenerator;

public class MapperKotlinCodeGenerator extends BaseMapperCodeGenerator implements CodeGenerator {

    public MapperKotlinCodeGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    public void generate(String packageName, TypeElement typeElement) {

    }

    @Override
    protected void generateClass(String packageName, TypeElement typeElement, List<TypeElement> mapOneWayElements, List<TypeElement> mapTwoWayElements) throws IOException {

    }
}
