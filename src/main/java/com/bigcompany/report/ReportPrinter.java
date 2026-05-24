package com.bigcompany.report;

import com.bigcompany.result.ReportingLineIssue;
import com.bigcompany.result.SalaryIssue;

import java.util.List;

// Handles all console output for salary and reporting line issues.
// Keeping print logic here means swapping output format only touches this class.
public class ReportPrinter {

    public void printSalaryIssues(List<SalaryIssue> issues) {
        System.out.println("=== Salary Issues ===");
        if (issues.isEmpty()) {
            System.out.println("No salary issues found.");
        } else {
            for (SalaryIssue issue : issues) {
                System.out.println(issue);
            }
        }
        System.out.println();
    }

    public void printReportingLineIssues(List<ReportingLineIssue> issues) {
        System.out.println("=== Reporting Line Issues ===");
        if (issues.isEmpty()) {
            System.out.println("No reporting line issues found.");
        } else {
            for (ReportingLineIssue issue : issues) {
                System.out.println(issue);
            }
        }
        System.out.println();
    }
}
