package com.agaramtech.qualis.databasecleaner;

/**
 * This interface holds declarations to perform vacuum cleaning from java scheduler.
 * @author Gowtham R
 * @version 10.0.0.2
 * @Jira ALPD-5190
 * @since 14 - December -2024
 */
public interface DBCleanSchedulerDAO {

	public void vacuumMaintenance();
	
}
