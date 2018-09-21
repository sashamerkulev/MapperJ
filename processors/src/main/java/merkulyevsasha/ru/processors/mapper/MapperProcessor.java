package merkulyevsasha.ru.processors.mapper;

import com.google.auto.service.AutoService;

import java.util.HashSet;
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

import merkulyevsasha.ru.annotations.Mapper;
import merkulyevsasha.ru.processors.BaseCodeGenerator;
import merkulyevsasha.ru.processors.CodeGenerator;
import merkulyevsasha.ru.processors.CodeGeneratorFactory;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(BaseCodeGenerator.KAPT_KOTLIN_GENERATED_OPTION_NAME)
public class MapperProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(Mapper.class);

        for (Element element : elements) {
            if (element.getKind() != ElementKind.CLASS) continue;
            TypeElement typeElement = (TypeElement) element;

            Mapper mapper = typeElement.getAnnotation(Mapper.class);

            CodeGenerator processor = CodeGeneratorFactory.createMapperGenerator(mapper.source(), processingEnv);
            processor.generate(typeElement, processingEnv.getElementUtils().getPackageOf(typeElement).toString());
        }
        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> result = new HashSet<>();
        result.add(Mapper.class.getCanonicalName());
        return result;
    }
}
