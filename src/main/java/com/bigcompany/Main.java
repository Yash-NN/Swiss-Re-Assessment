package com.bigcompany;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Please add csv file to with arguments");
            return;
        }

        String filePath = args[0];

        // Read the CDV file
        List<Employee> employees = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        boolean firstLine = true;

        while ((line = reader.readLine()) != null) {
            if (firstLine) { firstLine = false; continue; }
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split(",", -1);
            int id = Integer.parseInt(parts[0].trim());
            String firstName = parts[1].trim();
            String lastName = parts[2].trim();
            double salary = Double.parseDouble(parts[3].trim());
            Integer managerId = parts[4].trim().isEmpty() ? null : Integer.parseInt(parts[4].trim());

            employees.add(new Employee(id, firstName, lastName, salary, managerId));
        }
        reader.close();

        // TODO: build maps for easy lookup
        // id -> Employee
        Map<Integer, Employee> byId = new HashMap<>();
        for (Employee e : employees) {
            byId.put(e.id, e);
        }

        // managerId -> list of direct reports
        Map<Integer, List<Employee>> directReports = new HashMap<>();
        for (Employee e : employees) {
            if (e.managerId != null) {
                directReports.computeIfAbsent(e.managerId, k -> new ArrayList<>()).add(e);
            }
        }

        // check salary issues
        System.out.println("=== Salary Issues ===");
        boolean foundSalaryIssue = false;

        for (Employee manager : employees) {
            List<Employee> reports = directReports.get(manager.id);

            // if not a manager, then skip
            if (reports == null || reports.isEmpty()) continue;

            // calculate average salary of direct reports
            double total = 0;
            for (Employee r : reports) total += r.salary;
            double avg = total / reports.size();

            double minExpected = avg * 1.20;
            double maxExpected = avg * 1.50;

            if (manager.salary < minExpected) {
                double shortfall = minExpected - manager.salary;
                System.out.printf("%s earns less than required by %.2f (earns %.2f, should earn at least %.2f)%n",
                        manager.fullName(), shortfall, manager.salary, minExpected);
                foundSalaryIssue = true;
            } else if (manager.salary > maxExpected) {
                double excess = manager.salary - maxExpected;
                System.out.printf("%s earns more than allowed by %.2f (earns %.2f, should earn at most %.2f)%n",
                        manager.fullName(), excess, manager.salary, maxExpected);
                foundSalaryIssue = true;
            }
        }

        if (!foundSalaryIssue) System.out.println("No salary issues found.");

        // check reporting line issues
        System.out.println("\n=== Reporting Line Issues ===");
        boolean foundDepthIssue = false;

        for (Employee e : employees) {
            // count how many managers are between this employee and the CEO
            int depth = 0;
            Integer currentManagerId = e.managerId;

            while (currentManagerId != null) {
                depth++;
                Employee mgr = byId.get(currentManagerId);
                currentManagerId = mgr.managerId;
            }
            // TODO: Handle case where there are more than 4 managers in between them.
            // depth = number of managers above the employee (CEO is at depth 0, their direct report is depth 1, etc....)
            if (depth > 4) {
                int excess = depth - 4;
                System.out.printf("%s has a manager reporting depth which is %d level(s) (depth: %d)%n",
                        e.fullName(), excess, depth);
                foundDepthIssue = true;
            }
        }

        if (!foundDepthIssue) System.out.println("No reporting line issues found.");
    }
}
