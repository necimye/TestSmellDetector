package testsmell;

import java.util.List;
import java.util.Map;

public abstract class SmellyElement {
    public abstract String getElementName();
    public abstract String getFullyQualifiedName();

    public abstract boolean isSmelly();

    public abstract Map<String, String> getData();
}
