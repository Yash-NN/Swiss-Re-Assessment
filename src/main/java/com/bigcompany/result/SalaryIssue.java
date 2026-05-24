package com.bigcompany.result;

import com.bigcompany.model.Employee;

// Holds the result of a salary policy violation for a manager.
// Type tells whether they are underpaid or overpaid.
public class SalaryIssue {

    public enum Type {
        UNDERPAID,  // earns less than 120% of subordinate average
        OVERPAID    // earns more than 150% of subordinate average
    }

    private final Employee manager;
    private final Type type;
    private final double actualSalary;
    private final double expectedSalary;
    private final double difference;

    public SalaryIssue(Employee manager, Type type, double actualSalary,
                       double expectedSalary, double difference) {
        this.manager        = manager;
        this.type           = type;
        this.actualSalary   = actualSalary;
        this.expectedSalary = expectedSalary;
        this.difference     = difference;
    }

    public Employee getManager()       { return manager; }
    public Type getType()              { return type; }
    public double getActualSalary()    { return actualSalary; }
    public double getExpectedSalary()  { return expectedSalary; }
    public double getDifference()      { return difference; }

    @Override
    public String toString() {
        if (type == Type.UNDERPAID) {
            return String.format(
                    "%s earns less than required by %.2f (earns %.2f, should earn at least %.2f)",
                    manager.getFullName(), difference, actualSalary, expectedSalary);
        } else {
            return String.format(
                    "%s earns more than allowed by %.2f (earns %.2f, should earn at most %.2f)",
                    manager.getFullName(), difference, actualSalary, expectedSalary);
        }
    }
}
