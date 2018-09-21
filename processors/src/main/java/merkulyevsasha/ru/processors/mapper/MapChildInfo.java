package merkulyevsasha.ru.processors.mapper;

import javax.lang.model.element.Element;

final class MapChildInfo {
    private final Element mainElement;
    private final String mainName;
    private final Element childElement;
    private final String childName;
    private final String methodName;
    private final boolean listType;

    MapChildInfo(Element mainElement, String mainName, Element childElement, String childName, String methodName, boolean listType) {
        this.mainElement = mainElement;
        this.mainName = mainName;
        this.childElement = childElement;
        this.childName = childName;
        this.methodName = methodName;
        this.listType = listType;
    }

    public Element getMainElement() {
        return mainElement;
    }

    public String getMainName() {
        return mainName;
    }

    public Element getChildElement() {
        return childElement;
    }

    public String getChildName() {
        return childName;
    }

    public String getMethodName() {
        return methodName;
    }

    public boolean isListType() {
        return listType;
    }
}
