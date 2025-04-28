package testsmell;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.apache.commons.lang3.StringUtils;
import testsmell.smell.*;
import thresholds.Thresholds;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TestSmellDetector {

    private List<AbstractSmell> testSmells;
    private final Thresholds thresholds;

    /**
     * Instantiates the various test smell analyzer classes and loads the objects into an list.
     * Each smell analyzer is initialized with a threshold object to set the most appropriate rule for the detection
     *
     * @param thresholds it could be the default threshold of the ones defined by Spadini
     */
    public TestSmellDetector(Thresholds thresholds) {
        this.thresholds = thresholds;
        initializeSmells();
    }

    private void initializeSmells() {
        testSmells = new ArrayList<>();
        testSmells.add(new AssertionRoulette(thresholds));
        testSmells.add(new ConditionalTestLogic(thresholds));
        testSmells.add(new ConstructorInitialization(thresholds));
        testSmells.add(new DefaultTest(thresholds));
        testSmells.add(new EmptyTest(thresholds));
        testSmells.add(new ExceptionCatchingThrowing(thresholds));
        testSmells.add(new GeneralFixture(thresholds));
        testSmells.add(new MysteryGuest(thresholds));
        testSmells.add(new PrintStatement(thresholds));
        testSmells.add(new RedundantAssertion(thresholds));
        testSmells.add(new SensitiveEquality(thresholds));
        testSmells.add(new VerboseTest(thresholds));
        testSmells.add(new SleepyTest(thresholds));
        testSmells.add(new EagerTest(thresholds));
        testSmells.add(new LazyTest(thresholds));
        testSmells.add(new DuplicateAssert(thresholds));
        testSmells.add(new UnknownTest(thresholds));
        testSmells.add(new IgnoredTest(thresholds));
        testSmells.add(new ResourceOptimism(thresholds));
        testSmells.add(new MagicNumberTest(thresholds));
        testSmells.add(new DependentTest(thresholds));
    }

    public void setTestSmells(List<AbstractSmell> testSmells) {
        this.testSmells = testSmells;
    }

    /**
     * Provides the names of the smells that are being checked for in the code
     *
     * @return list of smell names
     */
    public List<String> getTestSmellNames() {
        return testSmells.stream().map(AbstractSmell::getSmellName).collect(Collectors.toList());
    }

    /**
     * Loads the java source code file into an AST and then analyzes it for the existence of the different types of
     * test smells
     */
    public TestFile detectSmells(TestFile testFile) throws IOException {
        initializeSmells();
        CompilationUnit testFileCompilationUnit = null;
        CompilationUnit productionFileCompilationUnit = null;
        FileInputStream testFileInputStream=null, productionFileInputStream = null;


        // Configure symbol solver
        CombinedTypeSolver typeSolver = new CombinedTypeSolver();
        typeSolver.add(new ReflectionTypeSolver(false));
        if (!StringUtils.isEmpty(testFile.getTestFilePath())) {
            File testDir = new File(testFile.getTestFilePath()).getParentFile();
            typeSolver.add(new JavaParserTypeSolver(testDir));
        }
        if (!StringUtils.isEmpty(testFile.getProductionFilePath())) {
            File prodDir = new File(testFile.getProductionFilePath()).getParentFile();
            typeSolver.add(new JavaParserTypeSolver(prodDir));
        }
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        ParserConfiguration parserConfiguration = new ParserConfiguration();
        parserConfiguration.setSymbolResolver(symbolSolver);
        JavaParser parser = new JavaParser(parserConfiguration);


        // Parse test file
        if (!StringUtils.isEmpty(testFile.getTestFilePath())) {
            try {
                testFileInputStream = new FileInputStream(testFile.getTestFilePath());
                ParseResult<CompilationUnit> testParseResult = parser.parse(testFileInputStream);
                if (testParseResult.isSuccessful()) {
                    testFileCompilationUnit = testParseResult.getResult().get();
                    TypeDeclaration<?> typeDeclaration = testFileCompilationUnit.getTypes().get(0);
                    testFile.setNumberOfTestMethods(typeDeclaration.getMethods().size());
                } else {
                    throw new IOException("Failed to parse test file: " + testFile.getTestFilePath());
                }
            } finally {
                if (testFileInputStream != null) {
                    try {
                        testFileInputStream.close();
                    } catch (IOException e) {
                        // Log or handle closing error
                    }
                }
            }
        }

        // Parse production file
        if (!StringUtils.isEmpty(testFile.getProductionFilePath())) {
            try {
                productionFileInputStream = new FileInputStream(testFile.getProductionFilePath());
                ParseResult<CompilationUnit> prodParseResult = parser.parse(productionFileInputStream);
                if (prodParseResult.isSuccessful()) {
                    productionFileCompilationUnit = prodParseResult.getResult().get();
                } else {
                    throw new IOException("Failed to parse production file: " + testFile.getProductionFilePath());
                }
            } finally {
                if (productionFileInputStream != null) {
                    try {
                        productionFileInputStream.close();
                    } catch (IOException e) {
                        // Log or handle closing error
                    }
                }
            }
        }

        // Run smell analysis
        for (AbstractSmell smell : testSmells) {
            try {
                smell.runAnalysis(testFileCompilationUnit, productionFileCompilationUnit,
                        testFile.getTestFileNameWithoutExtension(),
                        testFile.getProductionFileNameWithoutExtension());
                testFile.addSmell(smell);
            } catch (Exception e) {
                testFile.addSmell(null);
                continue;
            }
        }
        return testFile;

    }
}
