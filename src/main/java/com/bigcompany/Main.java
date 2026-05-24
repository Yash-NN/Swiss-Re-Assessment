package com.bigcompany;

import com.bigcompany.analyzer.OrgAnalyzer;
import com.bigcompany.model.Employee;
import com.bigcompany.parser.CsvParser;
import com.bigcompany.report.ReportPrinter;

import java.io.IOException;
import java.util.List;

/**
 * Entry point.
 *
 *
 * Reads employee data from a CSV file, analyzes the org structure,
 * and prints any salary or reporting line to the console.
 */
public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Please provide the path to the CSV file.");
            return;
        }

        List<Employee> employees = new CsvParser().parse(args[0]);

        if (employees.isEmpty()) {
            System.out.println("No employee data found in the file.");
            return;
        }

        OrgAnalyzer   analyzer = new OrgAnalyzer(employees);
        ReportPrinter printer  = new ReportPrinter();

        printer.printSalaryIssues(analyzer.findSalaryIssues());
        printer.printReportingLineIssues(analyzer.findReportingLineIssues());
    }
}
