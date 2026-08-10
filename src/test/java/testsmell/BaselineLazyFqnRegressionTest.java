package testsmell;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.Test;
import testsmell.smell.LazyTest;
import thresholds.DefaultThresholds;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BaselineLazyFqnRegressionTest {

    @Test
    void fqnInstrumentationPreservesOriginalLazyCountWhenCallsCannotResolve()
            throws Exception {
        CompilationUnit production = StaticJavaParser.parse(
                "package example; public class Subject { public void only() {} }");
        CompilationUnit test = StaticJavaParser.parse(
                "package example;"
                        + "import org.junit.Test;"
                        + "public class SubjectTest {"
                        + "  private final Subject subject = new Subject();"
                        + "  @Test public void testFirst() {"
                        + "    subject.only(); subject.only();"
                        + "  }"
                        + "  @Test public void testSecond() { subject.only(); }"
                        + "}");

        LazyTest smell = new LazyTest(new DefaultThresholds());
        smell.runAnalysis(test, production, "SubjectTest", "Subject");

        // The original detector reports one occurrence per qualifying call.
        // Preserve that I12 behavior in the baseline; only add safe FQNs.
        assertEquals(3, smell.getNumberOfSmellyTests());
        Set<String> fqns = smell.getSmellyElements().stream()
                .map(SmellyElement::getFullyQualifiedName)
                .collect(Collectors.toSet());
        assertEquals(Set.of(
                "example.SubjectTest.testFirst",
                "example.SubjectTest.testSecond"), fqns);
    }
}
