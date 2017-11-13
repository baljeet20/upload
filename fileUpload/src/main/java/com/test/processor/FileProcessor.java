package com.test.processor;


import com.test.model.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ParameterizedPreparedStatementSetter;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FileProcessor {
    private static final Logger log = LoggerFactory.getLogger(FileProcessor.class);

    @Autowired
    JdbcTemplate jdbcTemplate;

    private char seprator = ',';

    public void process(MultipartFile file) throws IOException {

        List<String> result = readFileContent(file);
        System.out.println(result);
        saveRecordsInDB(result);

    }

    private void saveRecordsInDB(List<String> result) {
        int batchSize=2;

        jdbcTemplate.execute("DROP TABLE employees IF EXISTS");
        jdbcTemplate.execute("CREATE TABLE employees(" +
                "id INT , first_name VARCHAR(255), last_name VARCHAR(255), birthdate VARCHAR(255), SALARY DOUBLE(10.2))");

        List<Object[]> splitRecord = result.stream().skip(1)
                .map(record -> record.split(","))
                .collect(Collectors.toList());

        for (int j = 0; j < splitRecord.size(); j += batchSize) {
            final List<Object[]> batchList = splitRecord.subList(j, j + batchSize > splitRecord.size() ? splitRecord.size() : j + batchSize);
            jdbcTemplate.batchUpdate("INSERT INTO employees(id,first_name, last_name,birthdate,salary) VALUES (?,?,?,?,?)", batchList);

        }

//        jdbcTemplate.batchUpdate("INSERT INTO employees(id,first_name, last_name,birthdate,salary) VALUES (?,?,?,?,?)", splitRecord);

        jdbcTemplate.query(
                "SELECT * from employees",
                (rs, rowNum) -> new Employee(rs.getInt("id"), rs.getString("first_name"), rs.getString("last_name"),rs.getString("birthdate"),rs.getDouble("salary"))
        ).forEach(employee -> log.info(employee.toString()));
        jdbcTemplate.query(
                "SELECT id, first_name, last_name,birthdate,salary FROM employees WHERE first_name = ?", new Object[]{"Dean"},
                (rs, rowNum) -> new Employee(rs.getInt("id"), rs.getString("first_name"), rs.getString("last_name"),rs.getString("birthdate"),rs.getDouble("salary"))
        ).forEach(employee -> log.info(employee.toString()));

    }

    private List<String> readFileContent(MultipartFile file) {
        BufferedReader br;
        List<String> result = new ArrayList<>();
        try {

            String line;
            InputStream is = file.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            while ((line = br.readLine()) != null) {
                result.add(line);
            }

        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
        return result;
    }

}
