package merkulyevsasha.ru.processors.args;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

import merkulyevsasha.ru.builders.ClassSpec;
import merkulyevsasha.ru.builders.FileSource;
import merkulyevsasha.ru.builders.KotlinWriter;
import merkulyevsasha.ru.builders.MethodSpec;
import merkulyevsasha.ru.processors.Field;
import merkulyevsasha.ru.processors.Values;

public class ArgsKotlinCodeGenerator extends BaseArgsCodeGenerator {

    public ArgsKotlinCodeGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    protected void generateClass(String packageName, String className, LinkedHashMap<String, Field> fields) {

        try {
            File ktFile = new File(generatedSourcesRoot + File.separator + className + ".kt");
            try (PrintWriter out = new PrintWriter(new FileWriter(ktFile))) {

                MethodSpec.Builder constructorSpecBuilder = MethodSpec.constructorBuilder();

                for (Map.Entry<String, Field> entry : fields.entrySet()) {
                    Field field = entry.getValue();
                    Element fieldElement = field.getElement();
                    String key = fieldElement.getSimpleName().toString();
                    constructorSpecBuilder.addParam(key, fieldElement, field.getValues());
                }

                ClassSpec classSpec = ClassSpec.classBuilder(className)
                    .addConstructor(constructorSpecBuilder.build())
                    .build();

                FileSource.classFileBuilder(className)
                    .addPackage(packageName)
                    .addImport("android.content.Intent")
                    .addImport("android.os.Bundle")
                    .addClass(classSpec)
                    .build().writeTo(out, new KotlinWriter());

            }
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
    }

    @Override
    protected String getCommaDefaultValue(Element element, Values values) {
        String typeName = element.asType().toString().toLowerCase();
        switch (typeName) {
            case "int":
                return ", " + values.intValue;
            case "long":
                return ", " + values.longValue;
            case "boolean":
                return ", " + values.booleanValue;
            case "float":
                return ", " + values.floatValue + "F";
            case "double":
                return ", " + values.doubleValue;
            case "short":
                return ", " + values.shortValue;
            case "byte":
                return ", " + values.byteValue;
            case "java.lang.string":
                return values.stringValue.isEmpty() ? "" : ", \"" + values.stringValue + "\"";
            default:
                //return ", null";
                throw new IllegalArgumentException(typeName);
        }
    }

}
