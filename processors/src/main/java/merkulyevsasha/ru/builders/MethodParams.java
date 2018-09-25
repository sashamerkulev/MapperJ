package merkulyevsasha.ru.builders;

import javax.lang.model.element.Element;

import merkulyevsasha.ru.processors.Values;

public class MethodParams {

    private final String paramName;
    private final Element element;
    private final Values values;

    MethodParams(String paramName, Element element, Values values) {
        this.paramName = paramName;
        this.element = element;
        this.values = values;
    }

    public String getParamName() {
        return paramName;
    }

    public Element getTypeName() {
        return element;
    }

    public Values getValues() {
        return values;
    }
}