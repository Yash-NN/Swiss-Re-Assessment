package com.bigcompany.analyzer;

import com.bigcompany.model.Employee;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Analyzes the organizational structure for policy violations.
 *
 * Salary policy:
 *   - A manager must earn at least 20% more than the average salary of their direct reports
 *   - A manager must earn no more than 50% more than that average
 *
 * Reporting line policy:
 *   - An employee must not have more than 4 managers between them and the CEO
 *
 * Assumption: depth is the count of managers strictly above an employee.
 *   CEO -> depth 0, CEO's direct report -> depth 1. Flagged when depth > 4.
 */
public class OrgAnalyzer {

    static final double MIN_SALARY_RATIO    = 1.20;
    static final double MAX_SALARY_RATIO    = 1.50;
    static final int    MAX_REPORTING_DEPTH = 4;

    private final Map<Integer, Employee>       employeesById;
    private final Map<Integer, List<Employee>> directReports;

    public OrgAnalyzer(List<Employee> employees) {
        this.employeesById = buildEmployeeMap(employees);
        this.directReports = buildDirectReportsMap(employees);
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    public List<String> findSalaryIssues() {
        List<String> issues = new ArrayList<>();

        for (Employee manager : employeesById.values()) {
            List<Employee> reports = directReports.getOrDefault(manager.getId(), Collections.emptyList());
            if (reports.isEmpty()) continue;

            double avg        = averageSalary(reports);
            double minAllowed = avg * MIN_SALARY_RATIO;
            double maxAllowed = avg * MAX_SALARY_RATIO;

            if (manager.getSalary() < minAllowed) {
                double shortfall = minAllowed - manager.getSalary();
                issues.add(String.format(
                        "%s earns less than required by %.2f (earns %.2f, should earn at least %.2f)",
                        manager.getFullName(), shortfall, manager.getSalary(), minAllowed));

            } else if (manager.getSalary() > maxAllowed) {
                double excess = manager.getSalary() - maxAllowed;
                issues.add(String.format(
                        "%s earns more than allowed by %.2f (earns %.2f, should earn at most %.2f)",
                        manager.getFullName(), excess, manager.getSalary(), maxAllowed));
            }
        }

        return issues;
    }

    public List<String> findReportingLineIssues() {
        List<String> issues = new ArrayList<>();

        for (Employee employee : employeesById.values()) {
            int depth = computeDepth(employee);

            if (depth > MAX_REPORTING_DEPTH) {
                int excess = depth - MAX_REPORTING_DEPTH;
                issues.add(String.format(
                        "%s has a manager reporting depth which is %d level(s) too long (depth: %d)",
                        employee.getFullName(), excess, depth));
            }
        }

        return issues;
    }

    // -------------------------------------------------------------------------
    // Package-private for testing
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

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
