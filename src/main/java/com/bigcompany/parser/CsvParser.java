package com.bigcompany.parser;

import com.bigcompany.model.Employee;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Reads the CSV file and maps each row to an Employee object.
// Skips the header row, blank lines, and any rows having issuss with a warning.
public class CsvParser {

    private static final int EXPECTED_COLUMNS = 5;

    public List<Employee> parse(String filePath) throws IOException {
        List<Employee> employees = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                if (isHeader) { isHeader = false; continue; }

                line = line.trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(",", -1);
                if (parts.length != EXPECTED_COLUMNS) {
                    System.err.println("Skipping malformed row (unexpected columns): " + line);
                    continue;
                }

                try {
                    employees.add(parseRow(parts));
                } catch (NumberFormatException e) {
                    System.err.println("Skipping row with invalid number: " + line);
                }
            }
        }

        return employees;
    }

    private Employee parseRow(String[] parts) {
        int id            = Integer.parseInt(parts[0].trim());
        String firstName  = parts[1].trim();
        String lastName   = parts[2].trim();
        double salary     = Double.parseDouble(parts[3].trim());
        String managerRaw = parts[4].trim();
        Integer managerId = managerRaw.isEmpty() ? null : Integer.parseInt(managerRaw);

        return new Employee(id, firstName, lastName, salary, managerId);
    }
}
