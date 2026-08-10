package testsmell;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestMethod extends SmellyElement {

    private String methodName;
    private String fullyQualifiedName;
    private boolean hasSmell;
    private Map<String, String> data;


    public TestMethod(String methodName, String fullyQualifiedName) {
        this.methodName = methodName;
        this.fullyQualifiedName = fullyQualifiedName;
        data = new HashMap<>();
    }

    public void setSmell(boolean hasSmell) {
        this.hasSmell = hasSmell;
    }

    public void addDataItem(String name, String value) {
        data.put(name, value);
    }


    @Override
    public String getElementName() {
        return methodName;
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
