package com.test.repository;

import com.test.model.BatchRunData;
import com.test.model.Employee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.BatchUpdateException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FileDataRepository {

    private static final Logger logger = LoggerFactory.getLogger(FileDataRepository.class);

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    BatchRunData batchRunData;

    @Value("${com.test.batch.size}")
    int batchSize;

    public void save(List<String> result) {

        jdbcTemplate.execute("DROP TABLE employees IF EXISTS");
        jdbcTemplate.execute("CREATE TABLE employees(" +
                "id INT PRIMARY KEY, first_name VARCHAR(255) UNIQUE , last_name VARCHAR(255) NOT NULL, birthdate VARCHAR(255), SALARY DOUBLE(10.2))");

        List<Object[]> splitRecord = result.stream().skip(1)
                .map(record -> record.split(","))
                .collect(Collectors.toList());


            for (int j = 0; j < splitRecord.size(); j += batchSize) {
                final List<Object[]> batchList = splitRecord.subList(j, j + batchSize > splitRecord.size() ? splitRecord.size() : j + batchSize);
                try {
                      jdbcTemplate.batchUpdate("INSERT INTO employees(id,first_name, last_name,birthdate,salary) VALUES (?,?,?,?,?)", batchList);
                      batchRunData.setSuccessRecords(batchRunData.getSuccessRecords()+batchSize);


            }catch (Exception e) {
                    if (e instanceof BatchUpdateException) {
                        BatchUpdateException be = (BatchUpdateException) e;
                        int[] batchRes = be.getUpdateCounts();
                        if (batchRes != null && batchRes.length > 0) {
                            for (int index = 0; index < batchRes.length; index++) {
                                if (batchRes[index] == Statement.EXECUTE_FAILED) {
                                    logger.error("Error execution >>>>>>>>>>>"
                                            + index + " --- , codeFail : " + batchRes[index]
                                            + "---, line " + splitRecord.get(index));
                                }
                            }
                        }
                    }
                    if (e instanceof DuplicateKeyException) {
                        DuplicateKeyException be = (DuplicateKeyException) e;
                        logger.error("Record already exist "+e.getMessage());
                    }
                    logger.error("An unexpected error occured "+e.getMessage());
                    batchRunData.increamentFailedRecords();

                    continue;
                }

        }

        jdbcTemplate.query(
                "SELECT id, first_name, last_name,birthdate,salary FROM employees WHERE id = ?", new Object[] { 4},
                (rs, rowNum) -> new Employee(rs.getInt("id"), rs.getString("first_name"), rs.getString("last_name"),rs.getString("birthdate"),rs.getDouble("salary"))
        ).forEach(employee -> logger.info("Read employee "+employee.toString()));

        logger.info("Batch Summary "+batchRunData.toString());
        batchRunData.reset();
    }
}
