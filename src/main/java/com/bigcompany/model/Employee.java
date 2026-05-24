package com.bigcompany.model;

/**
 * Represents a single employee in the organization.
 * managerId is null for the CEO.
 */
public class Employee {

    private final int id;
    private final String firstName;
    private final String lastName;
    private final double salary;
    private final Integer managerId;

    public Employee(int id, String firstName, String lastName, double salary, Integer managerId) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.salary = salary;
        this.managerId = managerId;
    }

    public int getId()            { return id; }
    public String getFirstName()  { return firstName; }
    public String getLastName()   { return lastName; }
    public double getSalary()     { return salary; }
    public Integer getManagerId() { return managerId; }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String toString() {
        return String.format("Employee{id=%d, name='%s', salary=%.2f, managerId=%s}",
                id, getFullName(), salary, managerId);
    }
}
