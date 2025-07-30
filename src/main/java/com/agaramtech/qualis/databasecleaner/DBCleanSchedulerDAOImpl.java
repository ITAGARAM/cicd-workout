package com.agaramtech.qualis.databasecleaner;

import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform vacuum cleaning from java scheduler.
 * 
 * @author Gowtham R
 * @version 10.0.0.2
 * @Jira ALPD-5190
 * @since 14 - December -2024
 */
@AllArgsConstructor
public class DBCleanSchedulerDAOImpl implements DBCleanSchedulerDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(DBCleanSchedulerDAOImpl.class);

	private final JdbcTemplate jdbcTemplate;

	@Override
	@Scheduled(cron = "${vacuumMaintenanceCron.expression}")
	public void vacuumMaintenance() {
		boolean vacuum = true;
		try {
			LOGGER.info("Vacuum Analyize Process Started...");
			DataSource dataSource = jdbcTemplate.getDataSource();
			if (dataSource != null) {
				try (Connection connection = dataSource.getConnection()) {
					boolean originalAutoCommit = connection.getAutoCommit();
					connection.setAutoCommit(true);

					try (Statement statement = connection.createStatement()) {
						statement.execute("VACUUM ANALYZE");
					} finally {
						connection.setAutoCommit(originalAutoCommit);
					}
				} catch (Exception e) {
					LOGGER.error("Error while performing VACUUM ANALYZE", e);
				}
			}
		} catch (Exception e) {
			vacuum = false;
			LOGGER.error("error:" + e.getMessage());
		}
		LOGGER.info(vacuum ? "Vacuum completed" : "Vacuum Failed");
	}

}