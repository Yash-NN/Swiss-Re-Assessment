package com.bigcompany.parser;

import com.bigcompany.model.Employee;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.*;

public class CsvParserTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    private File writeCsv(String content) throws IOException {
        File file = tempFolder.newFile("employees.csv");
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
        return file;
    }

    @Test
    public void parsesAllValidRowsCorrectly() throws IOException {
        File csv = writeCsv(
                "Id,firstName,lastName,salary,managerId\n" +
                "123,Joe,Doe,60000,\n" +
                "124,Martin,Chekov,45000,123\n"
        );
        assertEquals(2, new CsvParser().parse(csv.getAbsolutePath()).size());
    }

    @Test
    public void ceoHasNullManagerId() throws IOException {
        File csv = writeCsv(
                "Id,firstName,lastName,salary,managerId\n" +
                "123,Joe,Doe,60000,\n"
        );
        assertNull(new CsvParser().parse(csv.getAbsolutePath()).get(0).getManagerId());
    }

    @Test
    public void subordinateHasCorrectManagerId() throws IOException {
        File csv = writeCsv(
                "Id,firstName,lastName,salary,managerId\n" +
                "123,Joe,Doe,60000,\n" +
                "124,Martin,Chekov,45000,123\n"
        );
        List<Employee> employees = new CsvParser().parse(csv.getAbsolutePath());
        Employee martin = employees.stream().filter(e -> e.getId() == 124).findFirst().get();
        assertEquals(Integer.valueOf(123), martin.getManagerId());
    }

    @Test
    public void parsesEmployeeSalaryCorrectly() throws IOException {
        File csv = writeCsv(
                "Id,firstName,lastName,salary,managerId\n" +
                "123,Joe,Doe,60000,\n"
        );
        assertEquals(60000.0, new CsvParser().parse(csv.getAbsolutePath()).get(0).getSalary(), 0.001);
    }

    @Test
    public void skipsBlankLines() throws IOException {
        File csv = writeCsv(
                "Id,firstName,lastName,salary,managerId\n" +
                "123,Joe,Doe,60000,\n" +
                "\n" +
                "124,Martin,Chekov,45000,123\n"
        );
        assertEquals(2, new CsvParser().parse(csv.getAbsolutePath()).size());
    }

    @Test
    public void skipsMalformedRowsWithWrongColumnCount() throws IOException {
        File csv = writeCsv(
                "Id,firstName,lastName,salary,managerId\n" +
                "123,Joe,Doe,60000,\n" +
                "bad,row\n" +
                "124,Martin,Chekov,45000,123\n"
        );
        assertEquals(2, new CsvParser().parse(csv.getAbsolutePath()).size());
    }

    @Test
    public void skipsRowsWithInvalidNumbers() throws IOException {
        File csv = writeCsv(
                "Id,firstName,lastName,salary,managerId\n" +
                "123,Joe,Doe,60000,\n" +
                "abc,Martin,Chekov,notanumber,123\n"
        );
        assertEquals(1, new CsvParser().parse(csv.getAbsolutePath()).size());
    }

    @Test
    public void returnsEmptyListForHeaderOnlyFile() throws IOException {
        File csv = writeCsv("Id,firstName,lastName,salary,managerId\n");
        assertTrue(new CsvParser().parse(csv.getAbsolutePath()).isEmpty());
    }
}
