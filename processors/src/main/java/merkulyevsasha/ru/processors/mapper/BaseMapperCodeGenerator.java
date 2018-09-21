package merkulyevsasha.ru.processors.mapper;

import java.util.ArrayList;
import java.util.List;

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

class BaseMapperCodeGenerator extends BaseCodeGenerator {

    final List<MapChildInfo> additionalMaps = new ArrayList<>();

    BaseMapperCodeGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);
    }

    List<TypeMirror> getOneWayMapTypeMirrors(Mapper mapper) {
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

    List<TypeMirror> getTwoWayMapTypeMirrors(Mapper mapper) {
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

    List<TypeElement> convertTypeMirrorsToTypeElements(List<TypeMirror> typeMirrors) {
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

    String getDefaultValueForType(TypeMirror typeMirror) {
        String typeName = typeMirror.toString().toLowerCase();

        boolean isList = typeName.contains("list");

        if (isList) {
            return "new ArrayList()";
        } else {
            switch (typeName) {
                case "int":
                    return "0";
                case "long":
                    return "0";
                case "boolean":
                    return "false";
                case "float":
                    return "0F";
                case "double":
                    return "0.0";
                case "short":
                    return "(short) 0";
                case "byte":
                    return "(byte) 0";
                case "java.lang.string":
                    return "\"\"";
                default:
                    return "null";
                //throw new IllegalArgumentException(typeName);
            }
        }
    }

    Object acceptMirrorType(TypeMirror typeMirror) {
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
}
