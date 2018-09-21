package merkulyevsasha.ru.processors;

import javax.lang.model.element.TypeElement;

public interface CodeGenerator {
    void generate(String packageName, TypeElement typeElement);
}
