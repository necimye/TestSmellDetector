package testsmell.smell;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import testsmell.AbstractSmell;
import testsmell.TestMethod;
import testsmell.Util;
import thresholds.Thresholds;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UnknownTest extends AbstractSmell {

    public UnknownTest(Thresholds thresholds) {
        super(thresholds);
    }

    @Override
    public String getSmellName() {
        return "Unknown Test";
    }

    @Override
    public void runAnalysis(CompilationUnit testFileCompilationUnit, CompilationUnit productionFileCompilationUnit,
                            String testFileName, String productionFileName) throws FileNotFoundException {
        UnknownTest.ClassVisitor classVisitor = new UnknownTest.ClassVisitor();
        classVisitor.visit(testFileCompilationUnit, null);
    }

    private class ClassVisitor extends VoidVisitorAdapter<Void> {
        private MethodDeclaration currentMethod = null;
        private TestMethod testMethod;
        private List<String> assertMessage = new ArrayList<>();
        private boolean hasAssert = false;
        private boolean hasExceptionAnnotation = false;

        @Override
        public void visit(MethodDeclaration n, Void arg) {
            if (Util.isValidTestMethod(n)) {
                // Get test class FQN
                String testClassFQN = n.findAncestor(ClassOrInterfaceDeclaration.class)
                        .map(cls -> cls.getFullyQualifiedName()
                                .orElse(cls.getNameAsString()))
                        .orElse("UnknownClass");

                // Check for @Test(expected = ...)
                Optional<AnnotationExpr> assertAnnotation = n.getAnnotationByName("Test");
                if (assertAnnotation.isPresent() && assertAnnotation.get() instanceof NormalAnnotationExpr) {
                    NormalAnnotationExpr normalAnnotation = (NormalAnnotationExpr) assertAnnotation.get();
                    for (MemberValuePair pair : normalAnnotation.getPairs()) {
                        if (pair.getNameAsString().equals("expected") && pair.getValue().toString().contains("Exception")) {
                            hasExceptionAnnotation = true;
                            break;
                        }
                    }
                }

                currentMethod = n;
                String methodFQN;
                try {
                    methodFQN = n.resolve().getQualifiedName();
                } catch (Exception e) {
                    methodFQN = testClassFQN + "." + n.getNameAsString();
                    System.err.println("Failed to resolve method " + n.getNameAsString() + ": " + e.getMessage());
                }

                testMethod = new TestMethod(n.getNameAsString(), methodFQN);
                testMethod.setSmell(false);

                // Visit method body to check for assertions
                super.visit(n, arg);

                // Mark as smelly if no assertions and no exception annotation
                if (!hasAssert && !hasExceptionAnnotation) {
                    testMethod.setSmell(true);
                }

                smellyElementsSet.add(testMethod);

                // Reset for next method
                currentMethod = null;
                assertMessage = new ArrayList<>();
                hasAssert = false;
                hasExceptionAnnotation = false;
            }
        }

        @Override
        public void visit(MethodCallExpr n, Void arg) {
            super.visit(n, arg);
            if (currentMethod != null) {
                if (n.getNameAsString().startsWith("assert") || n.getNameAsString().equals("fail")) {
                    hasAssert = true;
                }
            }
        }
    }
}