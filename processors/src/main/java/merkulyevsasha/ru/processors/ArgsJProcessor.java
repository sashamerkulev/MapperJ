package merkulyevsasha.ru.processors;

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

import merkulyevsasha.ru.annotations.ArgsJ;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions(ArgsJProcessor.KAPT_KOTLIN_GENERATED_OPTION_NAME)
public class ArgsJProcessor extends AbstractProcessor {

    final static String KAPT_KOTLIN_GENERATED_OPTION_NAME = "kapt.kotlin.generated";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(ArgsJ.class);
        for (Element element : elements) {
            if (element.getKind() != ElementKind.CLASS) continue;
            TypeElement typeElement = (TypeElement) element;
            ArgsJ args = typeElement.getAnnotation(ArgsJ.class);
            CodeGenerator processor = CodeGeneratorFactory.create(args.source(), processingEnv);
            processor.generate(typeElement, processingEnv.getElementUtils().getPackageOf(typeElement).toString());
        }
        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> result = new HashSet<>();
        result.add(ArgsJ.class.getCanonicalName());
        return result;
    }
}
