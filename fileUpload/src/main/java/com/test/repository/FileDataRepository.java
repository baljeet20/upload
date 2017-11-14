package com.test.repository;

import com.test.model.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FileDataRepository {

    private static final Logger log = LoggerFactory.getLogger(FileDataRepository.class);

    @Autowired
    JdbcTemplate jdbcTemplate;

    public void save(List<String> result) {

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

        jdbcTemplate.query(
                "SELECT id, first_name, last_name,birthdate,salary FROM employees WHERE id = ?", new Object[] { 1},
                (rs, rowNum) -> new Employee(rs.getInt("id"), rs.getString("first_name"), rs.getString("last_name"),rs.getString("birthdate"),rs.getDouble("salary"))
        ).forEach(employee -> log.info("Read employee "+employee.toString()));

    }
}
