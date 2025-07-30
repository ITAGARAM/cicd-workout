package com.agaramtech.qualis.javaScheduler.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import  com.agaramtech.qualis.scheduler.service.SpringSchedularDAO;
import com.agaramtech.qualis.exception.service.ExceptionLogDAO;
import com.agaramtech.qualis.externalorder.service.ExternalOrderDAO;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.EmailDAOSupport;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.scheduler.service.SchedularGenerateReportDAO;
import com.agaramtech.qualis.synchronisation.service.SynchronizationDAO;
import lombok.AllArgsConstructor;

@Repository
@AllArgsConstructor
public class JavaSchedulerDAOImpl implements JavaSchedulerDAO{

	private static final Log LOGGER = LogFactory.getLog(JavaSchedulerDAOImpl.class);

	private final JdbcTemplate jdbcTemplate;

	private final DateTimeUtilityFunction dateUtilityFunction;

	private final SynchronizationDAO synchronizationDAO;

	private final ExternalOrderDAO externalOrderDAO;

	private final SchedularGenerateReportDAO schedularGenerateReportDAO;

	private final SpringSchedularDAO springSchedularDAO;

	private final ExceptionLogDAO exceptionLogDAO;

	private final EmailDAOSupport emailDAOSupport;



	//	public JavaSchedulerDAOImpl(SynchronizationDAO synchronizationDAO, ExternalOrderDAO externalOrderDAO,
	//			com.agaramtech.qualis.javaScheduler.service.schedularGenerateReportDAO schedularGenerateReportDAO,
	//			SpringSchedularDAO springSchedularDAO, ExceptionHandleDAO exceptionHandleDAO) {
	//		super();
	//		this.synchronizationDAO = synchronizationDAO;
	//		this.externalOrderDAO = externalOrderDAO;
	//		this.schedularGenerateReportDAO = schedularGenerateReportDAO;
	//		this.springSchedularDAO = springSchedularDAO;
	//		this.exceptionHandleDAO = exceptionHandleDAO;
	//	}



	/*	@Scheduled(cron = "${cron.expression}")
	 * //Fusion Sync public void exceuteFusionSyncProcess() {
	 *
	 * LOGGER.info("Fusion Sync Process:"); try { UserInfo userInfo=new UserInfo();
	 *
	 * userInfo.setSdatetimeformat("dd/MM/yyyy HH:mm:ss");
	 * userInfo.setSlanguagefilename("Msg_en_US"); userInfo.setNusercode(-1);
	 * userInfo.setNuserrole(-1); userInfo.setSusername("QuaLIS Admin");
	 * userInfo.setSuserrolename("QuaLIS Admin");
	 * userInfo.setSlanguagename("English"); userInfo.setSlanguagetypecode("en-US");
	 * userInfo.setNtranssitecode((short)-1);
	 *
	 * userInfo=fusionSiteDao.getUserInfo(userInfo,"Fusion Site");
	 * fusionSiteDao.syncFusionSite(userInfo);
	 *
	 * userInfo=fusionSiteDao.getUserInfo(userInfo,"Fusion Plant");
	 * fusionPlantDAO.syncFusionPlant(userInfo);
	 *
	 * userInfo=fusionSiteDao.getUserInfo(userInfo,"Fusion Users");
	 * fusionUsersDAO.syncFusionUsers(userInfo);
	 *
	 * userInfo=fusionSiteDao.getUserInfo(userInfo,"Fusion Plant User");
	 * fusionPlantUserDAO.syncFusionPlantUser(userInfo);
	 *
	 * } catch(Exception e) { LOGGER.error("error:"+ e.getMessage()); } }
	 */

	//Material Inventory Transaction
	@Override
	@Scheduled(cron = "${cron1.expression}")
	public void materialinventoryvalidation() {

		LOGGER.info("material validity validation");
		jdbcTemplate.execute("call sp_material_inventory_expire_check()");
		jdbcTemplate.execute("call sp_material_inventory_nextvalidation()");
	}

	//Site to Site Auto Sync
	@Override
	@Scheduled(cron = "${cron2.expression}")
	public void exceuteSyncProcess() {

		LOGGER.info("Distributed Sync Process:");
		try
		{
			UserInfo userinfo=new UserInfo();
			final int nsyncType = 44;//Auto
			synchronizationDAO.syncRecords(userinfo, nsyncType);
		}
		catch(Exception e)
		{
			LOGGER.error("error:"+ e.getMessage());
		}

	}

	//Site to Site delete Sync
	@Override
	@Scheduled(cron = "${cron3.expression}")
	public void deleteSync() {
		LOGGER.info("Deletion of Sync:");
		try
		{
			LOGGER.info("Deletion of Records for every 3Months");
			synchronizationDAO.deleteSync();
		}
		catch(Exception e)
		{
			LOGGER.error("error:"+ e.getMessage());
		}

	}

	//Report Generation
	@Override
	@Scheduled(cron = "${cron4.expression}")
	public void schedularGenerateReport()  {
		LOGGER.info("Report Generation");
		try {
			schedularGenerateReportDAO.schedularGenerateReport();
		} catch (Exception e) {
			LOGGER.info("cause==>", e.getCause());
			LOGGER.info(e.getMessage());
		}
	}

	//Auto Expire- Method Validity
	@Override
	@Scheduled(cron = "${cron5.expression}")
	public void methodvalidityautoexpire() {

		LOGGER.info("method validity auto expiry");
		jdbcTemplate.execute("select * from  fn_methodvalidityautoexpire()");
	}

	//Email
	@Override
	@Scheduled(cron = "${cron6.expression}")
	public void schedularSendEmailTask() {
		LOGGER.info("mail");
		try {
			emailDAOSupport.sendMailCommonFunction();
		} catch (Exception e) {
			LOGGER.info("cause==>", e.getCause());
			LOGGER.info(e.getMessage());

		}
	}

	//Send to Portal Report
	@Override
	@Scheduled(cron = "${cron7.expression}")
	public void schedulerSendToPortalReport () {
		LOGGER.info("Send to Portal Report");
		UserInfo userinfo=new UserInfo();
		try {
			externalOrderDAO.SendToPortalReport(userinfo);
		} catch (Exception e) {
			LOGGER.info("cause==>", e.getCause());
			LOGGER.info(e.getMessage());
		}
	}

	//Sent Result to Portal
	@Override
	@Scheduled(cron = "${cron8.expression}")
	public void schedulerSentResultToPortal() {
		LOGGER.info("Sent Result to Portal");
		try {
			springSchedularDAO.sentResultToPortal();
		} catch (Exception e) {
			LOGGER.info("cause==>", e.getCause());
			LOGGER.info(e.getMessage());
		}
	}

	//sent External Order Status
	@Override
	@Scheduled(cron = "${cron9.expression}")
	public void schedulerSentExternalOrderStatus() {
		LOGGER.info("Send External Order Status");
		try {
			springSchedularDAO.sentExternalOrderStatus();
		} catch (Exception e) {
			LOGGER.info("cause==>", e.getCause());
			LOGGER.info(e.getMessage());
		}
	}

	@Override
	@Scheduled(cron = "${cron10.expression}")
	public void deleteExceptionLogs() {
		LOGGER.info("Exception logs:");
		try
		{
			LOGGER.info("Deletion of Records for every 3Months");
			exceptionLogDAO.deleteExceptionLogs();
		}
		catch(Exception e)
		{
			LOGGER.error("error:"+ e.getMessage());
		}

	}

	//Execute sync received data
	@Override
	@Scheduled(cron = "${cron11.expression}")
	public void executeSyncReceivedData() {
		LOGGER.info("Sync received data execution:");
		try
		{
			jdbcTemplate.execute("call sp_sync_received_data_execution()");
		}
		catch(Exception e)
		{
			LOGGER.error("error:"+ e.getMessage());
		}

	}

	//Added by sonia on 10th Feb 2025 for jira id:ALPD-5332
	@Override
	@Scheduled(cron = "${cron_scheduler.expression}")
	public void scheduler() {
		LOGGER.info("scheduler:");
		try
		{
			LOGGER.info("scheduler");
			UserInfo userinfo=new UserInfo();
			String currentDate = dateUtilityFunction.getCurrentDateTime(userinfo).toString().replace("T", " ").replace("Z", "");
			jdbcTemplate.execute("call sp_scheduler('"+currentDate+"')");
		}
		catch(Exception e)
		{
			LOGGER.error("error:"+ e.getMessage());
		}

	}
	//Added by sonia on 11th Feb 2025 for jira id:ALPD-5317
	@Override
	@Scheduled(cron = "${cron_envirnomentalScheduler.expression}")
	public void envirnomentalScheduler() {
		LOGGER.info("environmental scheduler:");
		try
		{
			LOGGER.info("environmental scheduler:");
			UserInfo userinfo=new UserInfo();
			String currentDate = dateUtilityFunction.getCurrentDateTime(userinfo).toString().replace("T", " ").replace("Z", "");
			jdbcTemplate.execute("call sp_environmentalscheduler('"+currentDate+"')");
		}
		catch(Exception e)
		{
			LOGGER.error("error:"+ e.getMessage());
		}

	}

	//Added by sonia on 11th Feb 2025 for jira id:ALPD-5350
	@Override
	@Scheduled(cron = "${cron_stabilityScheduler.expression}")
	public void stabilityScheduler() {
		LOGGER.info("stability scheduler:");
		try
		{
			LOGGER.info("stability scheduler:");
			UserInfo userinfo=new UserInfo();
			String currentDate = dateUtilityFunction.getCurrentDateTime(userinfo).toString().replace("T", " ").replace("Z", "");
			jdbcTemplate.execute("call sp_stabilityscheduler('"+currentDate+"')");
		}
		catch(Exception e)
		{
			LOGGER.error("error:"+ e.getMessage());
		}

	}

	// below runs method [executeReleaseCOASync] for every 5 minutes
	@Override
	@Scheduled(cron = "${cron_12.expression}")
	@Transactional
	public void executeReleaseCOASync() {


		final String sLock = " lock table locksyncprocess " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus() + "";
		jdbcTemplate.execute(sLock);

		LOGGER.info("executeReleaseCOASync start:COA Sync Process");
		try
		{
			LOGGER.info("executeReleaseCOASync try: COA Sync Process");
			jdbcTemplate.execute("call sp_sync_send_record()");
		}
		catch(Exception e)
		{
			LOGGER.info("executeReleaseCOASync Error: COA Sync Process");
			LOGGER.error("error:"+ e.getMessage());
		}

	}

}
