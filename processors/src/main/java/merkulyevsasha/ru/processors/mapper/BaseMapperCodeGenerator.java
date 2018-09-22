package merkulyevsasha.ru.processors.mapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.AbstractElementVisitor6;
import javax.tools.Diagnostic;

import merkulyevsasha.ru.annotations.Mapper;
import merkulyevsasha.ru.processors.BaseCodeGenerator;
import merkulyevsasha.ru.processors.CodeGenerator;

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
            List<TypeMirror> mapOneWayMirrors = getOneWayMapTypeMirrors(mapper);
            List<TypeMirror> typeTwoWayMirrors = getTwoWayMapTypeMirrors(mapper);

            additionalMaps.clear();
            generateClass(packageName, typeElement, convertTypeMirrorsToTypeElements(mapOneWayMirrors), convertTypeMirrorsToTypeElements(typeTwoWayMirrors));
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
        }
    }

    protected abstract void generateClass(String packageName, TypeElement typeElement, List<TypeElement> mapOneWayElements, List<TypeElement> mapTwoWayElements) throws IOException;

    protected abstract String getDefaultValueForType(TypeMirror typeMirror);

    protected abstract String getGetterByFieldName(String fieldName);

    String getConstructorParameter(LinkedHashMap<String, Element> mainElements, LinkedHashMap<String, Element> childElements) {
        StringBuilder sb = new StringBuilder();

        for (Map.Entry<String, Element> entry : mainElements.entrySet()) {

            Element mainElement = entry.getValue();
            String key = mainElement.getSimpleName().toString();

            if (sb.length() > 0) sb.append(", ");

            if (childElements.containsKey(key)) {

                Element childElement = childElements.get(key);

                Object mainElementType = acceptMirrorType(mainElement.asType());
                Object childElementType = acceptMirrorType(childElement.asType());

                if (mainElementType instanceof PrimitiveType || mainElement.asType().toString().contains("String")) {
                    sb.append("item.").append(getGetterByFieldName(mainElement.getSimpleName().toString()));
                } else {

                    DeclaredType mainDeclaredType = (DeclaredType) mainElementType;
                    DeclaredType childDeclaredType = (DeclaredType) childElementType;

                    Boolean isList = false;
                    String methodName = "mapTo" + getClassName(mainElement.asType().toString());
                    if (mainDeclaredType.toString().toLowerCase().contains("list")) {
                        methodName = methodName + "s";

                        TypeMirror mainArg = mainDeclaredType.getTypeArguments().get(0);
                        TypeMirror childArg = childDeclaredType.getTypeArguments().get(0);

                        Object mainListElementType = acceptMirrorType(mainArg);
                        Object childListElementType = acceptMirrorType(childArg);

                        mainDeclaredType = (DeclaredType) mainListElementType;
                        childDeclaredType = (DeclaredType) childListElementType;
                        isList = true;
                    }

                    sb.append(methodName).append("(item.").append(getGetterByFieldName(key)).append(")");

                    additionalMaps.add(new MapChildInfo(
                        mainDeclaredType.asElement(),
                        getClassName(mainElement.asType().toString()),
                        childDeclaredType.asElement(),
                        getClassName(childElement.asType().toString()),
                        methodName,
                        isList));
                }

            } else {
                sb.append(getDefaultValueForType(mainElement.asType()));
            }
        }
        return sb.toString();
    }

    private String getClassName(String elementTypeName) {
        int lastDot = elementTypeName.lastIndexOf(".");
        return elementTypeName.substring(lastDot + 1).replace(">", "");
    }

    private Object acceptMirrorType(TypeMirror typeMirror) {
        return typeMirror.accept(new TypeVisitor<Object, TypeMirror>() {
            @Override
            public Object visit(TypeMirror typeMirror, TypeMirror typeMirror2) {
                return typeMirror;
            }

            @Override
            public Object visit(TypeMirror typeMirror) {
                return typeMirror;
            }

            @Override
            public Object visitPrimitive(PrimitiveType primitiveType, TypeMirror typeMirror) {
                return primitiveType;
            }

            @Override
            public Object visitNull(NullType nullType, TypeMirror typeMirror) {
                return nullType;
            }

            @Override
            public Object visitArray(ArrayType arrayType, TypeMirror typeMirror) {
                return arrayType;
            }

            @Override
            public Object visitDeclared(DeclaredType declaredType, TypeMirror typeMirror) {
                return declaredType;
            }

            @Override
            public Object visitError(ErrorType errorType, TypeMirror typeMirror) {
                return errorType;
            }

            @Override
            public Object visitTypeVariable(TypeVariable typeVariable, TypeMirror typeMirror) {
                return typeVariable;
            }

            @Override
            public Object visitWildcard(WildcardType wildcardType, TypeMirror typeMirror) {
                return wildcardType;
            }

            @Override
            public Object visitExecutable(ExecutableType executableType, TypeMirror typeMirror) {
                return executableType;
            }

            @Override
            public Object visitNoType(NoType noType, TypeMirror typeMirror) {
                return noType;
            }

            @Override
            public Object visitUnknown(TypeMirror typeMirror, TypeMirror typeMirror2) {
                return typeMirror;
            }

            @Override
            public Object visitUnion(UnionType unionType, TypeMirror typeMirror) {
                return unionType;
            }

            @Override
            public Object visitIntersection(IntersectionType intersectionType, TypeMirror typeMirror) {
                return intersectionType;
            }
        }, typeMirror);
    }

    private Object acceptElement(Element element) {
        return element.accept(new AbstractElementVisitor6<Object, Element>() {
            @Override
            public Object visitPackage(PackageElement packageElement, Element element) {
                return null;
            }

            @Override
            public Object visitType(TypeElement typeElement, Element element) {
                return typeElement;
            }

            @Override
            public Object visitVariable(VariableElement variableElement, Element element) {
                return variableElement;
            }

            @Override
            public Object visitExecutable(ExecutableElement executableElement, Element element) {
                return null;
            }

            @Override
            public Object visitTypeParameter(TypeParameterElement variableElement, Element element) {
                return null;
            }
        }, element);
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

    private List<TypeElement> convertTypeMirrorsToTypeElements(List<TypeMirror> typeMirrors) {
        List<TypeElement> typeElements = new ArrayList<>();
        for (TypeMirror tm : typeMirrors) {
            Object mirrorType = acceptMirrorType(tm);
            DeclaredType declaredTpe = (DeclaredType) mirrorType;

            Element element = declaredTpe.asElement();
            Object abstractElement = acceptElement(element);

            TypeElement typeElement = (TypeElement) abstractElement;
            typeElements.add(typeElement);
        }
        return typeElements;
    }

}
