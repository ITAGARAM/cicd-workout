package com.agaramtech.qualis.javaScheduler.service;

//Added by sonia for ALPD-4184
public interface JavaSchedulerDAO {

	//Fusion Sync
	//public void exceuteFusionSyncProcess() ;

	//Material Inventory Transaction
	public void materialinventoryvalidation();

	//Site to Site Auto Sync
	public void exceuteSyncProcess() throws Exception;

	//Site to Site delete Sync
	public void deleteSync();

	//Report Generation
	public void schedularGenerateReport();

	//Auto Expire- Method Validity
	public void methodvalidityautoexpire();

	//Email
	public void schedularSendEmailTask();

	//Send to Portal Report
	public void schedulerSendToPortalReport();

	//Sent Result to Portal
	public void schedulerSentResultToPortal();

	//Sent External Order Status
	public void schedulerSentExternalOrderStatus();

	//Exception Logs Delete
	public void deleteExceptionLogs();

	public void executeSyncReceivedData();

	//Added by sonia on 10th Feb 2025 for jira id:ALPD-5332
	public void scheduler() throws Exception;

	//Added by sonia on 11th Feb 2025 for jira id:ALPD-5317
	public void envirnomentalScheduler() throws Exception;

	//Added by sonia on 11th Feb 2025 for jira id:ALPD-5350
	public void stabilityScheduler() throws Exception;

	// below runs method [executeReleaseCOASync] for every 5 minutes
	public void executeReleaseCOASync();
}
