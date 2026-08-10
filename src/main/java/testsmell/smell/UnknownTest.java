package testsmell.smell;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
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
                Optional<AnnotationExpr> assertAnnotation = n.getAnnotationByName("Test");
                if (assertAnnotation.isPresent() && assertAnnotation.get() instanceof NormalAnnotationExpr) {
                    NormalAnnotationExpr normalAnnotation = (NormalAnnotationExpr) assertAnnotation.get();
                    for (MemberValuePair pair : normalAnnotation.getPairs()) {
                        if (pair.getNameAsString().equals("expected") && pair.getValue().toString().contains("Exception"))
                            ;
                        hasExceptionAnnotation = true;
                    }
                }

                currentMethod = n;
                testMethod = new TestMethod(n.getNameAsString(), Util.getMethodQualifiedName(n));
                testMethod.setSmell(false); //default value is false (i.e. no smell)
                super.visit(n, arg);

                // no assertions and no annotation
                if (!hasAssert && !hasExceptionAnnotation)
                    testMethod.setSmell(true);

                smellyElementsSet.add(testMethod);

                //reset values for next method
                currentMethod = null;
                assertMessage = new ArrayList<>();
                hasAssert = false;
            }
        }

        @Override
        public void visit(MethodCallExpr n, Void arg) {
            super.visit(n, arg);
            if (currentMethod != null) {
                // if the name of a method being called start with 'assert'
                if (n.getNameAsString().startsWith(("assert"))) {
                    hasAssert = true;
                }
                // if the name of a method being called is 'fail'
                else if (n.getNameAsString().equals("fail")) {
                    hasAssert = true;
                }
            }
        }
    }
}