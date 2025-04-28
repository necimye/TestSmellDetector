package testsmell;

import java.util.Objects;

public class SmellDetail {
    public String codeElement;
    public String filePath;
    public int startLine;
    public int endLine;
    public int startColumn;
    public int endColumn;
    public String smellType;
    public String description;

    public SmellDetail(String filePath, int startLine, int endLine, int startColumn, int endColumn,
                       String smellType, String codeElement, String string) {
        this.filePath = filePath;
        this.startLine = startLine;
        this.endLine = endLine;
        this.startColumn = startColumn;
        this.endColumn = endColumn;
        this.smellType = smellType;
        this.codeElement = codeElement;
        this.description = "Assertion without explanatory message";
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SmellDetail)) return false;
        SmellDetail that = (SmellDetail) o;
        return startLine == that.startLine &&
                endLine == that.endLine &&
                startColumn == that.startColumn &&
                endColumn == that.endColumn &&
                Objects.equals(filePath, that.filePath) &&
                Objects.equals(codeElement, that.codeElement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filePath, startLine, endLine, startColumn, endColumn, codeElement);
    }

    // getters, setters, toString()
}
