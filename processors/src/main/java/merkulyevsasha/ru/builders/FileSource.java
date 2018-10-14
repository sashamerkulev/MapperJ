package merkulyevsasha.ru.builders;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class FileSource {

    private final String fileName;
    private final String packageName;
    private final ClassSpec classSpec;
    private final List<String> imports;

    private FileSource(String fileName, String packageName, ClassSpec classSpec, List<String> imports) {
        this.fileName = fileName;
        this.packageName = packageName;
        this.classSpec = classSpec;
        this.imports = imports;
    }

    public static ClassFileBuilder classFileBuilder(String fileName) {
        if (fileName.isEmpty()) throw new IllegalArgumentException("fileName is empty");
        return new ClassFileBuilder(fileName);
    }

    public void writeTo(PrintWriter out, SourceWriter sourceWriter) {
        sourceWriter.write(out, fileName, packageName, classSpec, imports);
    }

    public static class ClassFileBuilder {

        private String fileName;
        private String packageName;
        private ClassSpec classSpec;
        private List<String> importPackages = new ArrayList<>();

        ClassFileBuilder(String fileName) {
            this.fileName = fileName;
        }

        public ClassFileBuilder addImport(String importPackageName) {
            if (importPackageName.isEmpty()) throw new IllegalArgumentException("importPackageName is empty");
            importPackages.add(importPackageName);
            return this;
        }

        public ClassFileBuilder addImports(List<String> importPackageNames) {
            importPackages.addAll(importPackageNames);
            return this;
        }

        public ClassFileBuilder addPackage(String packageName) {
            if (packageName.isEmpty()) throw new IllegalArgumentException("packageName is empty");
            this.packageName = packageName;
            return this;
        }

        public ClassFileBuilder addClass(ClassSpec classSpec) {
            if (classSpec == null) throw new IllegalArgumentException();
            this.classSpec = classSpec;
            return this;
        }

        public FileSource build() {
            return new FileSource(fileName, packageName, classSpec, importPackages);
        }
    }

    public interface SourceWriter {
        void write(PrintWriter out, String fileName, String packageName, ClassSpec classSpec, List<String> packages);
    }

}
