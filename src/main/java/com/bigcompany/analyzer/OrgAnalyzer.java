package com.bigcompany.analyzer;

import com.bigcompany.model.Employee;
import com.bigcompany.result.ReportingLineIssue;
import com.bigcompany.result.SalaryIssue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Analyzes org structure for salary and reporting line policy violations.
// Salary: manager must earn 20%-50% more than their direct reports average.
// Reporting line: flags employees with more than 4 managers above them.
public class OrgAnalyzer {

    public static final double MIN_SALARY_RATIO    = 1.20;
    public static final double MAX_SALARY_RATIO    = 1.50;
    public static final int    MAX_REPORTING_DEPTH = 4;

    private final Map<Integer, Employee>       employeesById;
    private final Map<Integer, List<Employee>> directReports;

    public OrgAnalyzer(List<Employee> employees) {
        this.employeesById = buildEmployeeMap(employees);
        this.directReports = buildDirectReportsMap(employees);
    }

    // function to find salary issues
    public List<SalaryIssue> findSalaryIssues() {
        List<SalaryIssue> issues = new ArrayList<>();

        for (Employee manager : employeesById.values()) {
            List<Employee> reports = directReports.getOrDefault(manager.getId(), Collections.emptyList());
            if (reports.isEmpty()) continue;

            double avg        = averageSalary(reports);
            double minAllowed = avg * MIN_SALARY_RATIO;
            double maxAllowed = avg * MAX_SALARY_RATIO;

            if (manager.getSalary() < minAllowed) {
                double shortfall = minAllowed - manager.getSalary();
                issues.add(new SalaryIssue(manager, SalaryIssue.Type.UNDERPAID,
                        manager.getSalary(), minAllowed, shortfall));

            } else if (manager.getSalary() > maxAllowed) {
                double excess = manager.getSalary() - maxAllowed;
                issues.add(new SalaryIssue(manager, SalaryIssue.Type.OVERPAID,
                        manager.getSalary(), maxAllowed, excess));
            }
        }

        return issues;
    }

    public List<ReportingLineIssue> findReportingLineIssues() {
        List<ReportingLineIssue> issues = new ArrayList<>();

        for (Employee employee : employeesById.values()) {
            int depth = computeDepth(employee);

            if (depth > MAX_REPORTING_DEPTH) {
                int excess = depth - MAX_REPORTING_DEPTH;
                issues.add(new ReportingLineIssue(employee, depth, excess));
            }
        }

        return issues;
    }

    // Core logic methods
    int computeDepth(Employee employee) {
        int depth = 0;
        Integer currentManagerId = employee.getManagerId();

        while (currentManagerId != null) {
            depth++;
            Employee manager = employeesById.get(currentManagerId);
            if (manager == null) break; // guard: unknown manager reference in data
            currentManagerId = manager.getManagerId();
        }

        return depth;
    }

    double averageSalary(List<Employee> employees) {
        return employees.stream()
                .mapToDouble(Employee::getSalary)
                .average()
                .orElse(0.0);
    }

    // Helper methods
    private Map<Integer, Employee> buildEmployeeMap(List<Employee> employees) {
        Map<Integer, Employee> map = new HashMap<>();
        for (Employee e : employees) map.put(e.getId(), e);
        return map;
    }

    private Map<Integer, List<Employee>> buildDirectReportsMap(List<Employee> employees) {
        Map<Integer, List<Employee>> map = new HashMap<>();
        for (Employee e : employees) {
            if (e.getManagerId() != null) {
                map.computeIfAbsent(e.getManagerId(), k -> new ArrayList<>()).add(e);
            }
        }
        return map;
    }
}
