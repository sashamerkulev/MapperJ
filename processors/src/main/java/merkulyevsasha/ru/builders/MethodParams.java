package merkulyevsasha.ru.builders;

import javax.lang.model.element.Element;

import merkulyevsasha.ru.processors.Values;

public class MethodParams {

    private final String paramName;
    private final Element element;
    private final Values values;
    private final String typeName;

    private MethodParams(String paramName, Element element, String typeName, Values values) {
        this.paramName = paramName;
        this.element = element;
        this.values = values;
        this.typeName = typeName;
    }

    MethodParams(String paramName, Element element, Values values) {
        this(paramName, element, "", values);
    }

    MethodParams(String paramName, String typeName) {
        this(paramName, null, typeName, new Values());
    }

    public String getParamName() {
        return paramName;
    }

    public String getTypeName() {
        return typeName;
    }

    public Element getElement() {
        return element;
    }

    public Values getValues() {
        return values;
    }
}