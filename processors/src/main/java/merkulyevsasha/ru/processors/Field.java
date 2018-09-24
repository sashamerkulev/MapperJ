package merkulyevsasha.ru.processors;

import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

import merkulyevsasha.ru.annotations.Ignore;

public class Field {

    private final Element element;
    private final FieldType fieldType;
    private final Object listElementType;
    private final Ignore ignoreAnnotation;

    Field(Element element, FieldType fieldType, Object listElementType,
          Ignore ignoreAnnotation) {
        this.element = element;
        this.fieldType = fieldType;
        this.listElementType = listElementType;
        this.ignoreAnnotation = ignoreAnnotation;
    }

    public Element getElement() {
        return element;
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    public Object getListElementType() {
        return listElementType;
    }

    public Ignore getIgnoreAnnotation() {
        return ignoreAnnotation;
    }

    public enum FieldType {
        PrimitiveOrStringType,
        ArrayType,
        CustomType
    }
}

