package merkulyevsasha.ru.processors.args;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.JavaFileObject;

import merkulyevsasha.ru.builders.ClassSpec;
import merkulyevsasha.ru.builders.FileSource;
import merkulyevsasha.ru.builders.JavaWriter;
import merkulyevsasha.ru.builders.MethodSpec;
import merkulyevsasha.ru.processors.Field;
import merkulyevsasha.ru.processors.Values;

public class ArgsJavaCodeGenerator extends BaseArgsCodeGenerator {

    public ArgsJavaCodeGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    protected void generateClass(String packageName, String className, LinkedHashMap<String, Field> fields) throws IOException {

        JavaFileObject builderFile = processingEnv.getFiler()
            .createSourceFile(className);

        try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {

            MethodSpec.Builder constructorSpecBuilder = MethodSpec.constructorBuilder();

            // constructor
            for (Map.Entry<String, Field> entry : fields.entrySet()) {
                Field field = entry.getValue();
                Element fieldElement = field.getElement();
                String key = fieldElement.getSimpleName().toString();

                constructorSpecBuilder.addParam(key, fieldElement, field.getValues());
            }

            // toIntent
            MethodSpec.Builder toIntent = MethodSpec.methodBuilder("toIntent")
                .addReturnType("Intent")
                .addStatement("Intent intent = new Intent();");
            for (Map.Entry<String, Field> entry : fields.entrySet()) {
                Field field = entry.getValue();
                Element fieldElement = field.getElement();
                String key = fieldElement.getSimpleName().toString();
                toIntent.addStatement("intent.putExtra(\"" + key + "\", " + key + ");");
            }
            toIntent.addStatement("return intent;");

            // toBundle
            MethodSpec.Builder toBundle = MethodSpec.methodBuilder("toBundle")
                .addReturnType("Bundle")
                .addStatement("Bundle bundle = new Bundle();");
            for (Map.Entry<String, Field> entry : fields.entrySet()) {
                Field field = entry.getValue();
                Element fieldElement = field.getElement();
                String key = fieldElement.getSimpleName().toString();
                toBundle.addStatement("bundle.put" + getFirstUpperFieldTypeName(fieldElement) + "(\"" + key + "\", " + key + ");");
            }
            toBundle.addStatement("return bundle;");

            // fromBundle
            MethodSpec.Builder fromBundle = MethodSpec.methodBuilder("fromBundle")
                .addInheritanceModifier(MethodSpec.InheritanceModifier.STATIC)
                .addParam("bundle", "Bundle")
                .addReturnType(className)
                .addStatement("return new " + className + "(");
            int count = 0;
            int size = fields.size() - 1;
            for (Map.Entry<String, Field> entry : fields.entrySet()) {
                count++;
                Field field = entry.getValue();
                Element fieldElement = field.getElement();
                String key = fieldElement.getSimpleName().toString();
                String comma = (count <= size) ? "," : "";
                fromBundle.addStatement("    bundle.get" + getFirstUpperFieldTypeName(fieldElement) + "(\"" + key + "\""
                    + getCommaDefaultValue(fieldElement, field.getValues()) + ")" + comma);
            }
            fromBundle.addStatement(");");

            // fromIntent
            MethodSpec.Builder fromIntent = MethodSpec.methodBuilder("fromIntent")
                .addInheritanceModifier(MethodSpec.InheritanceModifier.STATIC)
                .addParam("intent", "Intent")
                .addReturnType(className)
                .addStatement("return new " + className + "(");
            count = 0;
            for (Map.Entry<String, Field> entry : fields.entrySet()) {
                count++;
                Field field = entry.getValue();
                Element fieldElement = field.getElement();
                String key = fieldElement.getSimpleName().toString();
                String type = getFirstUpperFieldTypeName(fieldElement);
                String defaultValue = type.equals("String") ? "" : getCommaDefaultValue(fieldElement, field.getValues());
                String comma = (count <= size) ? "," : "";
                fromIntent.addStatement("    intent.get" + type + "Extra(\"" + key + "\""
                    + defaultValue + ")" + comma);
            }
            fromIntent.addStatement(");");

            ClassSpec classSpec = ClassSpec.classBuilder(className)
                .addConstructor(constructorSpecBuilder.build())
                .addAccessModifier(ClassSpec.AccessModifier.PUBLIC)
                .addInheritanceModifier(ClassSpec.InheritanceModifier.FINAL)
                .addMethod(toIntent.build())
                .addMethod(toBundle.build())
                .addMethod(fromBundle.build())
                .addMethod(fromIntent.build())
                .build();

            FileSource.classFileBuilder(className)
                .addPackage(packageName)
                .addImport("android.content.Intent")
                .addImport("android.os.Bundle")
                .addClass(classSpec)
                .build()
                .writeTo(out, new JavaWriter());

        }
    }

    private String getCommaDefaultValue(Element element, Values values) {
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
                return ", (short) " + values.shortValue;
            case "byte":
                return ", (byte) " + values.byteValue;
            case "java.lang.string":
                return values.stringValue.isEmpty() ? "" : ", \"" + values.stringValue + "\"";
            default:
                return ", null";
            //throw new IllegalArgumentException(typeName);
        }
    }

}
