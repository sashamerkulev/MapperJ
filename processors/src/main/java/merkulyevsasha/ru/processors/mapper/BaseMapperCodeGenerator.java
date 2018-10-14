package merkulyevsasha.ru.processors.mapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

import merkulyevsasha.ru.annotations.Mapper;
import merkulyevsasha.ru.annotations.params.Source;
import merkulyevsasha.ru.processors.BaseCodeGenerator;
import merkulyevsasha.ru.processors.CodeGenerator;
import merkulyevsasha.ru.processors.Field;

abstract class BaseMapperCodeGenerator extends BaseCodeGenerator implements CodeGenerator {

    final List<MapChildInfo> additionalMaps = new ArrayList<>();

    BaseMapperCodeGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    @Override
    public void generate(String packageName, TypeElement typeElement) {
        if (generatedSourcesRoot == null || generatedSourcesRoot.isEmpty()) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, BaseCodeGenerator.FOLDER_ERROR_MESSAGE);
            return;
        }

        try {
            Mapper mapper = typeElement.getAnnotation(Mapper.class);

            String className = typeElement.getSimpleName().toString();
            String mapperClassName = className + "Mapper";
            LinkedHashMap<String, Field> typeElementFields = fieldParser.getElementFields(typeElement);

            List<TypeMirror> mapOneWayMirrors = getOneWayMapTypeMirrors(mapper);
            List<TypeMirror> typeTwoWayMirrors = getTwoWayMapTypeMirrors(mapper);

            LinkedHashMap<Element, LinkedHashMap<String, Field>> oneWayMapClasses = convertTypeMirrorsToTypeFieldsElements(mapOneWayMirrors);
            LinkedHashMap<Element, LinkedHashMap<String, Field>> twoWayMapClasses = convertTypeMirrorsToTypeFieldsElements(typeTwoWayMirrors);

            additionalMaps.clear();
            generateClass(packageName, className, mapperClassName,
                typeElementFields, oneWayMapClasses, twoWayMapClasses);
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
    }

    protected abstract void generateClass(String packageName, String className, String mapperClassName, LinkedHashMap<String, Field> typeElementFields,
                                          LinkedHashMap<Element, LinkedHashMap<String, Field>> oneWayMapClasses, LinkedHashMap<Element, LinkedHashMap<String, Field>> twoWayMapClasses
    ) throws IOException;

    protected abstract String getDefaultValueForType(TypeMirror typeMirror);

    protected abstract String getGetterByFieldName(String fieldName);

    String getConstructorParameter(Source source, LinkedHashMap<String, Field> mainFields, LinkedHashMap<String, Field> childFields) {

        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Field> entry : mainFields.entrySet()) {

            Field mainField = entry.getValue();
            String key = mainField.getElement().getSimpleName().toString();

            if (sb.length() > 0) sb.append(", ");

            if (childFields.containsKey(key)) {

                Field childField = childFields.get(key);

                if (mainField.getFieldType() == Field.FieldType.PrimitiveOrStringType) {
                    if (mainField.isNullable() == childField.isNullable() || mainField.isNullable()) {
                        sb.append("item.").append(getGetterByFieldName(mainField.getElement().getSimpleName().toString()));
                    } else {
                        StringBuilder getter = new StringBuilder();
                        if (source == Source.Kotlin) {
                            getter.append("item.").append(getGetterByFieldName(mainField.getElement().getSimpleName().toString()));
                            getter.append(" ?: ");
                            getter.append(getDefaultValueForType(mainField.getElement().asType()));
                        } else if (source == Source.Java) {
                            getter.append("item.").append(getGetterByFieldName(mainField.getElement().getSimpleName().toString()));
                            getter.append(" == null ? ");
                            getter.append("item.").append(getGetterByFieldName(mainField.getElement().getSimpleName().toString()));
                            getter.append(" : ");
                            getter.append(getDefaultValueForType(mainField.getElement().asType()));
                        }
                        sb.append(getter);
                    }
                } else {

                    DeclaredType mainDeclaredType = mainField.getElementType();
                    DeclaredType childDeclaredType = childField.getElementType();

                    Boolean isList = false;
                    String methodName = "mapTo" + getClassName(mainField.getElement().asType().toString());
                    if (mainField.getFieldType() == Field.FieldType.ArrayType) {
                        methodName = methodName + "s";
                        mainDeclaredType = mainField.getElementType();
                        childDeclaredType = childField.getElementType();
                        isList = true;
                    }

                    sb.append(methodName).append("(item.").append(getGetterByFieldName(key)).append(")");

                    additionalMaps.add(new MapChildInfo(
                        mainDeclaredType.asElement(),
                        getClassName(mainField.getElement().asType().toString()),
                        childDeclaredType.asElement(),
                        getClassName(childField.getElement().asType().toString()),
                        methodName,
                        isList));
                }

            } else {
                sb.append(getDefaultValueForType(mainField.getElement().asType()));
            }
        }
        return sb.toString();
    }

    private String getClassName(String elementTypeName) {
        int lastDot = elementTypeName.lastIndexOf(".");
        return elementTypeName.substring(lastDot + 1).replace(">", "");
    }

    private LinkedHashMap<Element, LinkedHashMap<String, Field>> convertTypeMirrorsToTypeFieldsElements(List<TypeMirror> typeMirrors) {
        LinkedHashMap<Element, LinkedHashMap<String, Field>> toMapClasses = new LinkedHashMap<>();
        for (TypeMirror typeMirror : typeMirrors) {

            Object mirrorType = fieldParser.acceptMirrorType(typeMirror);
            DeclaredType declaredTpe = (DeclaredType) mirrorType;

            Element toMapElement = declaredTpe.asElement();
            Object toMapAbstractElement = fieldParser.acceptElement(toMapElement);

            TypeElement toMapClass = (TypeElement) toMapAbstractElement;
            toMapClasses.put(toMapElement, fieldParser.getElementFields(toMapClass));
        }
        return toMapClasses;
    }

    private List<TypeMirror> getOneWayMapTypeMirrors(Mapper mapper) {
        List<TypeMirror> typeMirrors = new ArrayList<>();
        try {
            for (Class<?> clazz : mapper.oneWayMapClasses()) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, clazz.getSimpleName());
            }
        } catch (MirroredTypesException mte) {
            typeMirrors.addAll(mte.getTypeMirrors());
        }
        return typeMirrors;
    }

    private List<TypeMirror> getTwoWayMapTypeMirrors(Mapper mapper) {
        List<TypeMirror> typeMirrors = new ArrayList<>();
        try {
            for (Class<?> clazz : mapper.twoWayMapClasses()) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, clazz.getSimpleName());
            }
        } catch (MirroredTypesException mte) {
            typeMirrors.addAll(mte.getTypeMirrors());
        }
        return typeMirrors;
    }
}
