package com.bigcompany;

import com.bigcompany.analyzer.OrgAnalyzer;
import com.bigcompany.model.Employee;
import com.bigcompany.parser.CsvParser;

import java.io.IOException;
import java.util.List;

/**
 * Entry point.
 *
 * Usage: java -jar app.jar <path-to-csv>
 *
 * Reads employee data from a CSV file, analyzes the org structure,
 * and prints any salary or reporting line policy violations to the console.
 */
public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Usage: java -jar app.jar <path-to-csv>");
            return;
        }

        List<Employee> employees = new CsvParser().parse(args[0]);

        if (employees.isEmpty()) {
            System.out.println("No employee data found in the file.");
            return;
        }

        OrgAnalyzer analyzer = new OrgAnalyzer(employees);

        printSection("Salary Issues", analyzer.findSalaryIssues());
        printSection("Reporting Line Issues", analyzer.findReportingLineIssues());
    }

    private static void printSection(String title, List<String> issues) {
        System.out.println("=== " + title + " ===");
        if (issues.isEmpty()) {
            System.out.println("No " + title.toLowerCase() + " found.");
        } else {
            for (String issue : issues) {
                System.out.println(issue);
            }
        }
        System.out.println();
    }
}
