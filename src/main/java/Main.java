import testsmell.*;
import thresholds.DefaultThresholds;
import thresholds.Thresholds;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws IOException {
        for (String arg: args) {
            System.out.println(arg);
        }
        if (args == null) {
            System.out.println("Please provide the file containing the paths to the collection of test files");
            return;
        }
        if (!args[0].isEmpty()) {
            File inputFile = new File(args[0]);
            if (!inputFile.exists() || inputFile.isDirectory()) {
                System.out.println("Please provide a valid file containing the paths to the collection of test files");
                return;
            }
        }
        TestSmellDetector testSmellDetector = new TestSmellDetector(new DefaultThresholds());
        String outputFileName = args[1];

        // Read the input file and build the TestFile objects
        BufferedReader in = new BufferedReader(new FileReader(args[0]));
        String str;

        String[] lineItem;
        TestFile testFile;
        List<TestFile> testFiles = new ArrayList<>();
        while ((str = in.readLine()) != null) {
            // Handle CSV fields properly (ensure any commas inside quotes are preserved)
            lineItem = str.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);  // This regex handles quoted commas

            if (lineItem.length == 2) {
                testFile = new TestFile(lineItem[0], lineItem[1], "");
            } else {
                testFile = new TestFile(lineItem[0], lineItem[1], lineItem[2]);
            }

            testFiles.add(testFile);
        }

        // Initialize the output file - Create the output file and add the column names
        ResultsWriter resultsWriter = ResultsWriter.createResultsWriter(outputFileName);
        List<String> columnNames;
        List<String> columnValues;

        columnNames = testSmellDetector.getTestSmellNames();
        columnNames.add(0, "App");
        columnNames.add(1, "TestClass");
        columnNames.add(2, "TestFilePath");
        columnNames.add(3, "ProductionFilePath");
        columnNames.add(4, "RelativeTestFilePath");
        columnNames.add(5, "RelativeProductionFilePath");
        columnNames.add(6, "NumberOfMethods");

        resultsWriter.writeColumnName(columnNames);

        // Iterate through all test files to detect smells and then write the output
        TestFile tempFile;
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date;
        for (TestFile file : testFiles) {
            date = new Date();
            System.out.println(dateFormat.format(date) + " Processing: " + file.getTestFilePath());
            System.out.println("Processing: " + file.getTestFilePath());

            // Detect smells
            tempFile = testSmellDetector.detectSmells(file);

            // Write output
            columnValues = new ArrayList<>();
            columnValues.add(escapeCSVValue(file.getApp()));
            columnValues.add(escapeCSVValue(file.getTestFileName()));
            columnValues.add(escapeCSVValue(file.getTestFilePath()));
            columnValues.add(escapeCSVValue(file.getProductionFilePath()));
            columnValues.add(escapeCSVValue(file.getRelativeTestFilePath()));
            columnValues.add(escapeCSVValue(file.getRelativeProductionFilePath()));
            columnValues.add(String.valueOf(file.getNumberOfTestMethods()));

            // Handle smells and write them correctly
            for (AbstractSmell smell : tempFile.getTestSmells()) {
                try {
                    String smellDetails = String.valueOf(smell.getNumberOfSmellyTests()) + "###" + Arrays.toString(smell.getSmellyElements().stream()
                            .filter(SmellyElement::isSmelly)
                            .map(SmellyElement::getFullyQualifiedName)
                            .collect(Collectors.toList()).toArray());

                    // Escape the smell data to avoid breaking CSV structure
                    columnValues.add(escapeCSVValue(smellDetails));

                } catch (NullPointerException e) {
                    columnValues.add("");
                }
            }
            resultsWriter.writeLine(columnValues);
        }

        System.out.println("end");
    }

    // Method to escape CSV special characters
    private static String escapeCSVValue(String value) {
        if (value == null) {
            return "\"\""; // Handle null values
        }
        // Escape quotes by doubling them and wrap the value in quotes if necessary
        value = value.replace("\"", "\"\"");

        // If the value contains commas, newlines, or quotes, wrap it in quotes
        if (value.contains(",") || value.contains("\n") || value.contains("\"")) {
            value = "\"" + value + "\"";
        }
        return value;
    }
}
