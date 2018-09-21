package merkulyevsasha.ru.processors;

import javax.lang.model.element.TypeElement;

public interface CodeGenerator {
    void generate(TypeElement typeElement, String packageName);
}
