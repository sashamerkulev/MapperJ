package merkulyevsasha.ru.processors;

import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;

import merkulyevsasha.ru.annotations.Ignore;

public class Field {

    private final Element element;
    private final FieldType fieldType;
    private final DeclaredType elementType;
    private final Ignore ignoreAnnotation;
    private final Values values;
    private final boolean nullable;

    Field(Element element, FieldType fieldType, DeclaredType elementType,
          Ignore ignoreAnnotation, boolean nullable, Values values) {
        this.element = element;
        this.fieldType = fieldType;
        this.elementType = elementType;
        this.ignoreAnnotation = ignoreAnnotation;
        this.nullable = nullable;
        this.values = values;
    }

    public Element getElement() {
        return element;
    }

    public FieldType getFieldType() {
        return fieldType;
    }

    public DeclaredType getElementType() {
        return elementType;
    }

    public Ignore getIgnoreAnnotation() {
        return ignoreAnnotation;
    }

    public Values getValues() {
        return values;
    }

    public boolean isNullable() {
        return nullable;
    }

    public enum FieldType {
        PrimitiveOrStringType,
        ArrayType,
        CustomType
    }
}

