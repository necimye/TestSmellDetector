package testsmell.util;

public class TestSmellDetail {
    private String smellName;
    private String filePath;
    private int startLine;
    private int endLine;
    private int startColumn;
    private int endColumn;
    private String codeElementType;
    private String description;
    private String codeElementSignature;

    // Constructor
    public TestSmellDetail(String filePath, int startLine, int endLine, int startColumn,
                           int endColumn, String codeElementType, String description,
                           String codeElementSignature) {
        this.filePath = filePath;
        this.startLine = startLine;
        this.endLine = endLine;
        this.startColumn = startColumn;
        this.endColumn = endColumn;
        this.codeElementType = codeElementType;
        this.description = description;
        this.codeElementSignature = codeElementSignature;
    }

    // Getters and toString()
}