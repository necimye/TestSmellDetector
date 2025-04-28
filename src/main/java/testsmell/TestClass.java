package testsmell;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestClass extends SmellyElement {

    private String className;
    private final String fullyQualifiedName;
    private boolean hasSmell;
    private Map<String, String> data;

    public TestClass(String className, String fullyQualifiedName) {
        this.className = className;
        this.fullyQualifiedName = fullyQualifiedName;
        data = new HashMap<>();
    }

    public void setHasSmell(boolean hasSmell) {
        this.hasSmell = hasSmell;
    }

    public void addDataItem(String name, String value) {
        data.put(name, value);
    }

    @Override
    public String getElementName() {
        return className;
    }

    @Override
    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

    @Override
    public boolean isSmelly() {
        return hasSmell;
    }

    @Override
    public Map<String, String> getData() {
        return data;
    }
}
