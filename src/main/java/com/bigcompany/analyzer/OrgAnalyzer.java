package com.bigcompany.analyzer;

import com.bigcompany.model.Employee;
import com.bigcompany.result.ReportingLineIssue;
import com.bigcompany.result.SalaryIssue;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
        this.employeesById = employees.stream()
                .collect(Collectors.toMap(Employee::getId, e -> e));

        this.directReports = employees.stream()
                .filter(e -> e.getManagerId() != null)
                .collect(Collectors.groupingBy(Employee::getManagerId));
    }

    // function to find salary issues
    // parallelStream used here so large datasets can be processed across multiple threads
    public List<SalaryIssue> findSalaryIssues() {
        return employeesById.values().parallelStream()
                .map(manager -> checkSalary(manager,
                        directReports.getOrDefault(manager.getId(), Collections.emptyList())))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    // parallelStream is safe here as both maps are read-only after construction
    public List<ReportingLineIssue> findReportingLineIssues() {
        return employeesById.values().parallelStream()
                .map(employee -> checkReportingLine(employee, computeDepth(employee)))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    // Core logic methods
    private Optional<SalaryIssue> checkSalary(Employee manager, List<Employee> reports) {
        if (reports.isEmpty()) return Optional.empty();

        double avg        = averageSalary(reports);
        double minAllowed = avg * MIN_SALARY_RATIO;
        double maxAllowed = avg * MAX_SALARY_RATIO;

        if (manager.getSalary() < minAllowed) {
            return Optional.of(new SalaryIssue(manager, SalaryIssue.Type.UNDERPAID,
                    manager.getSalary(), minAllowed, minAllowed - manager.getSalary()));
        }
        if (manager.getSalary() > maxAllowed) {
            return Optional.of(new SalaryIssue(manager, SalaryIssue.Type.OVERPAID,
                    manager.getSalary(), maxAllowed, manager.getSalary() - maxAllowed));
        }
        return Optional.empty();
    }

    private Optional<ReportingLineIssue> checkReportingLine(Employee employee, int depth) {
        if (depth <= MAX_REPORTING_DEPTH) return Optional.empty();
        return Optional.of(new ReportingLineIssue(employee, depth, depth - MAX_REPORTING_DEPTH));
    }

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
}
