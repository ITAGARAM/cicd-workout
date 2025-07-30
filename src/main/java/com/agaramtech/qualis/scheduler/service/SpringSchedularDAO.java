package com.agaramtech.qualis.scheduler.service;

public interface SpringSchedularDAO {

	//	public void scheduleFixedDelayTask();

	public void testGroupSpecExpiryTask();

	public void schedulerprereg();

	//	public void methodvalidityautoexpire();

	public void schedulerForPreregister();

	//public void materialinventorynextvalidation();
	//public void materialinventoryexpirecheck();
	//	public void materialinventoryvalidation();

	//	public void exceuteSyncProcess();
	public void sentResultToPortal() throws Exception;
	public void sentExternalOrderStatus() throws Exception;

	public void executeSyncReceivedData();

}
