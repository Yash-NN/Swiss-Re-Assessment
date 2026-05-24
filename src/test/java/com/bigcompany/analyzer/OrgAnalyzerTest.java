package com.bigcompany.analyzer;

import com.bigcompany.model.Employee;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class OrgAnalyzerTest {

    private Employee emp(int id, String name, double salary, Integer managerId) {
        return new Employee(id, name, name, salary, managerId);
    }

    // -------------------------------------------------------------------------
    // Salary tests
    // -------------------------------------------------------------------------

    @Test
    public void underpaidManagerIsFlagged() {
        // avg subordinate salary = 50000, min required = 60000, manager earns 55000
        Employee ceo    = emp(1, "CEO", 55000, null);
        Employee report = emp(2, "Bob", 50000, 1);
        List<String> issues = new OrgAnalyzer(Arrays.asList(ceo, report)).findSalaryIssues();
        assertEquals(1, issues.size());
        assertTrue(issues.get(0).contains("less than required by"));
    }

    @Test
    public void overpaidManagerIsFlagged() {
        // avg = 50000, max = 75000, manager earns 80000
        Employee ceo    = emp(1, "CEO", 80000, null);
        Employee report = emp(2, "Bob", 50000, 1);
        List<String> issues = new OrgAnalyzer(Arrays.asList(ceo, report)).findSalaryIssues();
        assertEquals(1, issues.size());
        assertTrue(issues.get(0).contains("more than allowed by"));
    }

    @Test
    public void managerAtExactMinimumIsNotFlagged() {
        // avg = 50000, min = 60000, manager earns exactly 60000
        Employee ceo    = emp(1, "CEO", 60000, null);
        Employee report = emp(2, "Bob", 50000, 1);
        assertTrue(new OrgAnalyzer(Arrays.asList(ceo, report)).findSalaryIssues().isEmpty());
    }

    @Test
    public void managerAtExactMaximumIsNotFlagged() {
        // avg = 50000, max = 75000, manager earns exactly 75000
        Employee ceo    = emp(1, "CEO", 75000, null);
        Employee report = emp(2, "Bob", 50000, 1);
        assertTrue(new OrgAnalyzer(Arrays.asList(ceo, report)).findSalaryIssues().isEmpty());
    }

    @Test
    public void averageIsComputedAcrossAllDirectReports() {
        // avg of 40000 and 60000 = 50000 -> min=60000, max=75000, earns 70000 -> OK
        Employee ceo = emp(1, "CEO", 70000, null);
        Employee a   = emp(2, "Ann", 40000, 1);
        Employee b   = emp(3, "Ben", 60000, 1);
        assertTrue(new OrgAnalyzer(Arrays.asList(ceo, a, b)).findSalaryIssues().isEmpty());
    }

    // -------------------------------------------------------------------------
    // Reporting line tests
    // -------------------------------------------------------------------------

    @Test
    public void ceoHasDepthZero() {
        Employee ceo = emp(1, "CEO", 60000, null);
        assertEquals(0, new OrgAnalyzer(Collections.singletonList(ceo)).computeDepth(ceo));
    }

    @Test
    public void directReportOfCeoHasDepthOne() {
        Employee ceo    = emp(1, "CEO", 60000, null);
        Employee report = emp(2, "Bob", 40000, 1);
        assertEquals(1, new OrgAnalyzer(Arrays.asList(ceo, report)).computeDepth(report));
    }

    @Test
    public void employeeAtDepthFourIsNotFlagged() {
        // CEO(1) -> A(2) -> B(3) -> C(4) -> D(5), D has depth 4
        Employee ceo = emp(1, "CEO", 100000, null);
        Employee a   = emp(2, "A",   80000,  1);
        Employee b   = emp(3, "B",   60000,  2);
        Employee c   = emp(4, "C",   50000,  3);
        Employee d   = emp(5, "D",   40000,  4);
        assertTrue(new OrgAnalyzer(Arrays.asList(ceo, a, b, c, d)).findReportingLineIssues().isEmpty());
    }

    @Test
    public void employeeAtDepthFiveIsFlagged() {
        // CEO(1) -> A(2) -> B(3) -> C(4) -> D(5) -> E(6), E has depth 5
        Employee ceo = emp(1, "CEO", 100000, null);
        Employee a   = emp(2, "A",   80000,  1);
        Employee b   = emp(3, "B",   60000,  2);
        Employee c   = emp(4, "C",   50000,  3);
        Employee d   = emp(5, "D",   40000,  4);
        Employee e   = emp(6, "E",   30000,  5);
        List<String> issues = new OrgAnalyzer(Arrays.asList(ceo, a, b, c, d, e)).findReportingLineIssues();
        assertEquals(1, issues.size());
        assertTrue(issues.get(0).contains("E"));
    }

    @Test
    public void multipleEmployeesFlaggedWhenTooDeep() {
        // CEO -> A -> B -> C -> D -> E -> F: both E (depth 5) and F (depth 6) flagged
        Employee ceo = emp(1, "CEO", 100000, null);
        Employee a   = emp(2, "A",   80000,  1);
        Employee b   = emp(3, "B",   60000,  2);
        Employee c   = emp(4, "C",   50000,  3);
        Employee d   = emp(5, "D",   40000,  4);
        Employee e   = emp(6, "E",   30000,  5);
        Employee f   = emp(7, "F",   20000,  6);
        assertEquals(2, new OrgAnalyzer(Arrays.asList(ceo, a, b, c, d, e, f)).findReportingLineIssues().size());
    }

    // -------------------------------------------------------------------------
    // Edge cases
    // -------------------------------------------------------------------------

    @Test
    public void singleCeoHasNoIssues() {
        Employee ceo = emp(1, "CEO", 60000, null);
        OrgAnalyzer analyzer = new OrgAnalyzer(Collections.singletonList(ceo));
        assertTrue(analyzer.findSalaryIssues().isEmpty());
        assertTrue(analyzer.findReportingLineIssues().isEmpty());
    }

    @Test
    public void sampleDataFromAssignment() {
        Employee joe    = new Employee(123, "Joe",    "Doe",      60000, null);
        Employee martin = new Employee(124, "Martin", "Chekov",   45000, 123);
        Employee bob    = new Employee(125, "Bob",    "Ronstad",  47000, 123);
        Employee alice  = new Employee(300, "Alice",  "Hasacat",  50000, 124);
        Employee brett  = new Employee(305, "Brett",  "Hardleaf", 34000, 300);

        OrgAnalyzer analyzer = new OrgAnalyzer(Arrays.asList(joe, martin, bob, alice, brett));

        // Martin manages Alice (avg=50000, min=60000). Martin earns 45000 -> underpaid
        List<String> salaryIssues = analyzer.findSalaryIssues();
        assertEquals(1, salaryIssues.size());
        assertTrue(salaryIssues.get(0).contains("Martin Chekov"));
        assertTrue(salaryIssues.get(0).contains("less than required by"));

        // Max depth is Brett at depth 3 -> no issue
        assertTrue(analyzer.findReportingLineIssues().isEmpty());
    }
}
