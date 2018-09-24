package merkulyevsasha.ru.processors;

import java.util.LinkedHashMap;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;

import merkulyevsasha.ru.annotations.DefaultValue;
import merkulyevsasha.ru.annotations.Ignore;

public class ElementFieldParser {

    public LinkedHashMap<String, Field> getElementFields(Element type) {
        LinkedHashMap<String, Field> typeElements = new LinkedHashMap<>();
        for (Element element : type.getEnclosedElements()) {
            if (element.getKind() != ElementKind.FIELD) continue;

            Field.FieldType fieldType;
            DeclaredType listElementType = null;
            Object objectType = acceptMirrorType(element.asType());
            if (objectType instanceof PrimitiveType || element.asType().toString().contains("String")) {
                fieldType = Field.FieldType.PrimitiveOrStringType;
            } else {
                DeclaredType declaredType = (DeclaredType) objectType;
                if (declaredType.toString().toLowerCase().contains("list")) {
                    fieldType = Field.FieldType.ArrayType;

                    TypeMirror arg = declaredType.getTypeArguments().get(0);
                    listElementType = (DeclaredType)acceptMirrorType(arg);
                } else {
                    fieldType = Field.FieldType.CustomType;
                }
            }

            Ignore ignore = element.getAnnotation(Ignore.class);
            DefaultValue defaultValues = element.getAnnotation(DefaultValue.class);
            Values values = getValues(defaultValues);
            typeElements.put(element.toString(), new Field(element, fieldType, listElementType, ignore, values));
        }
        return typeElements;
    }

    private Values getValues(DefaultValue defaultValues) {
        return defaultValues == null? new Values() : new Values(defaultValues.stringValue(), defaultValues.intValue(), defaultValues.floatValue(),
            defaultValues.shortValue(), defaultValues.longValue(), defaultValues.doubleValue(), defaultValues.booleanValue(), defaultValues.byteValue());
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


}
