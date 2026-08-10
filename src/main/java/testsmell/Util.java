package testsmell;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Util {

    public static boolean isValidTestMethod(MethodDeclaration n) {
        boolean valid = false;

        if (!n.getAnnotationByName("Ignore").isPresent()) {
            //only analyze methods that either have a @test annotation (Junit 4) or the method name starts with 'test'
            if (n.getAnnotationByName("Test").isPresent() || n.getNameAsString().toLowerCase().startsWith("test")) {
                //must be a public method
                if (n.isPublic()) {
                    valid = true;
                }
            }
        }

        return valid;
    }

    public static boolean isValidSetupMethod(MethodDeclaration n) {
        boolean valid = false;

        if (!n.getAnnotationByName("Ignore").isPresent()) {
            //only analyze methods that either have a @Before annotation (Junit 4) or the method name is 'setUp'
            if (n.getAnnotationByName("Before").isPresent() || n.getNameAsString().equals("setUp")) {
                //must be a public method
                if (n.isPublic()) {
                    valid = true;
                }
            }
        }

        return valid;
    }

    public static boolean isInt(String s)
    {
        try
        { int i = Integer.parseInt(s); return true; }

        catch(NumberFormatException er)
        { return false; }
    }

    public static boolean isNumber(String str) {
        try {
            double v = Double.parseDouble(str);
            return true;
        } catch (NumberFormatException nfe) {
        }
        return false;
    }

    /**
     * Build a method FQN from the parsed declaration. FQN reporting is output
     * instrumentation and must never invoke symbol resolution or affect the
     * original detector's control flow.
     */
    public static String getMethodQualifiedName(MethodDeclaration method) {
        String typeName = getTypeQualifiedName(method);
        return typeName.isEmpty()
                ? method.getNameAsString()
                : typeName + "." + method.getNameAsString();
    }

    /**
     * Build the FQN of the enclosing type (or of {@code node} itself when it
     * is a type declaration) without requiring a symbol solver.
     */
    public static String getTypeQualifiedName(Node node) {
        String packageName = node.findCompilationUnit()
                .flatMap(CompilationUnit::getPackageDeclaration)
                .map(declaration -> declaration.getNameAsString())
                .orElse("");

        List<String> typeNames = new ArrayList<>();
        Node current = node;
        while (current != null) {
            if (current instanceof TypeDeclaration) {
                typeNames.add(((TypeDeclaration<?>) current).getNameAsString());
            }
            current = current.getParentNode().orElse(null);
        }
        Collections.reverse(typeNames);

        String nestedTypeName = String.join(".", typeNames);
        if (packageName.isEmpty()) {
            return nestedTypeName;
        }
        return nestedTypeName.isEmpty()
                ? packageName
                : packageName + "." + nestedTypeName;
    }
}
