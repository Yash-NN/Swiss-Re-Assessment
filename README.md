# Big Company Org Analyzer

Reads employee data from a CSV file and flags salary and reporting line issues.

## How to run

```bash
mvn clean package
java -jar target/big-company-analyzer-1.0-SNAPSHOT.jar employees.csv
```

## What it does

Checks two things for every employee:

**Salary** — every manager should earn between 20% and 50% more than the average salary of their direct reports. If they earn less or more, it gets flagged with the exact amount they are off by.

**Reporting line** — if an employee has more than 4 managers between them and the CEO, they get flagged with how many levels too deep they are.

## CSV format

First row is the header, one employee per line. CEO has no managerId.

```
Id,firstName,lastName,salary,managerId
123,Joe,Doe,60000,
124,Martin,Chekov,45000,123
```

## Assumptions

- Exactly one CEO exists, identified by an empty managerId column.
- Depth is counted as the number of managers strictly above the employee. CEO is depth 0, flagged when depth is greater than 4.
- Malformed or non-numeric rows are skipped with a warning, rest of the file still runs.
- File can have up to 1000 employees as per the spec.

## Running tests

```bash
mvn test
```
