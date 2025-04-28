//package testsmell.util;
//
//import com.github.javaparser.JavaParser;
//import com.github.javaparser.ast.CompilationUnit;
//import com.github.javaparser.ast.body.MethodDeclaration;
//import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
//import com.github.javaparser.ast.body.VariableDeclarator;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//
//public class CodeElementExtractor {
//
//    public static List<CodeElementInfo> extractCodeElements(File javaFile) {
//        List<CodeElementInfo> elements = new ArrayList<>();
//        try {
//            CompilationUnit cu = JavaParser.parse(javaFile);
//
//            String filePath = javaFile.getPath();
//
//            // Extract Method Declarations
//            cu.findAll(MethodDeclaration.class).forEach(md -> {
//                md.getRange().ifPresent(range -> {
//                    String signature = md.getDeclarationAsString(false, false, true);
//                    elements.add(new CodeElementInfo(
//                            filePath,
//                            range.begin.line,
//                            range.end.line,
//                            range.begin.column,
//                            range.end.column,
//                            "METHOD_DECLARATION",
//                            "original method declaration",
//                            signature
//                    ));
//                });
//            });
//
//            // Extract Class Declarations
//            cu.findAll(ClassOrInterfaceDeclaration.class).forEach(cd -> {
//                cd.getRange().ifPresent(range -> {
//                    String signature = (cd.isInterface() ? "interface " : "class ") + cd.getNameAsString();
//                    elements.add(new CodeElementInfo(
//                            filePath,
//                            range.begin.line,
//                            range.end.line,
//                            range.begin.column,
//                            range.end.column,
//                            "CLASS_DECLARATION",
//                            "original class/interface declaration",
//                            signature
//                    ));
//                });
//            });
//
//            // Extract Variables Declarations
//            cu.findAll(VariableDeclarator.class).forEach(vd -> {
//                vd.getRange().ifPresent(range -> {
//                    String varDecl = vd.getTypeAsString() + " " + vd.getNameAsString();
//                    elements.add(new CodeElementInfo(
//                            filePath,
//                            range.begin.line,
//                            range.end.line,
//                            range.begin.column,
//                            range.end.column,
//                            "VARIABLE_DECLARATION",
//                            "original variable declaration",
//                            varDecl
//                    ));
//                });
//            });
//
//        } catch (Exception e) {
//            System.err.println("Parsing error in file " + javaFile.getPath() + ": " + e.getMessage());
//        }
//        return elements;
//    }
//}
