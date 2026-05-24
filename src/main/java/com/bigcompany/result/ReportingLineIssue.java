package com.bigcompany.result;

import com.bigcompany.model.Employee;

// Holds the result of a reporting line violation for an employee.
// Captures actual depth and how many levels over the limit of 4 they are.
public class ReportingLineIssue {

    private final Employee employee;
    private final int depth;
    private final int excessLevels;

    public ReportingLineIssue(Employee employee, int depth, int excessLevels) {
        this.employee     = employee;
        this.depth        = depth;
        this.excessLevels = excessLevels;
    }

    public Employee getEmployee()  { return employee; }
    public int getDepth()          { return depth; }
    public int getExcessLevels()   { return excessLevels; }

    @Override
    public String toString() {
        return String.format(
                "%s has a manager reporting depth which is %d level(s) too long (depth: %d)",
                employee.getFullName(), excessLevels, depth);
    }
}
