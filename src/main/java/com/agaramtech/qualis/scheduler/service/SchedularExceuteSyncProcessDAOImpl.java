// Sonia Commented this class for ALPD-4184
//package com.agaramtech.scheduler.services;
//
//import java.util.Properties;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.scheduling.annotation.Scheduled;
//
//import com.agaramtech.global.ProjectDAOSupport;
//import com.agaramtech.global.UserInfo;
//import com.agaramtech.lims.dao.support.bridge.RDBMSBridge;
//import com.agaramtech.lims.dao.support.bridge.RDBMSParameter;
//import com.agaramtech.lims.dao.support.enums.RDBMSEnum;
//import com.agaramtech.lims.dao.support.global.AgaramtechGeneralfunction;
//import com.agaramtech.resultentry.service.ResultEntryDAO;
//import com.agaramtech.synchronisation.service.SynchronizationDAO;
//
//public class SchedularExceuteSyncProcessDAOImpl extends ProjectDAOSupport implements SchedularExceuteSyncProcessDAO {
//	@Autowired
//	SynchronizationDAO synchronizationDAO;
//	private AgaramtechGeneralfunction objGeneral;
//	private static int dataBaseType = 0;
//
//	Logger logger = LoggerFactory.getLogger(this.getClass());
//
//	public SchedularExceuteSyncProcessDAOImpl(RDBMSParameter objParameter, AgaramtechGeneralfunction objGeneral,
//			Properties objProperties) {
//
//		super(objParameter, objGeneral);
//
//		if (objParameter.getsdatabasetype().equals(RDBMSEnum.DB_MYSQL.getDataBaseType())) {
//			dataBaseType = RDBMSEnum.DB_MYSQL.getType();
//		} else if (objParameter.getsdatabasetype().equals(RDBMSEnum.DB_POSTGRESQL.getDataBaseType())) {
//			dataBaseType = RDBMSEnum.DB_POSTGRESQL.getType();
//		} else if (objParameter.getsdatabasetype().equals(RDBMSEnum.DB_MICROSOFT_SQL_SERVER.getDataBaseType())) {
//			dataBaseType = RDBMSEnum.DB_MICROSOFT_SQL_SERVER.getType();
//		} else if (objParameter.getsdatabasetype().equals(RDBMSEnum.DB_ORACLE.getDataBaseType())) {
//			dataBaseType = RDBMSEnum.DB_ORACLE.getType();
//		}
//		this.objGeneral = objGeneral;
//		RDBMSBridge.dataBaseType = dataBaseType;
//	}
//
//	//second, minute, hour, day of month, month, day(s) of week
//	//Scheduled Every day at 00:02:00
//	@Override
//	@Scheduled(cron = "0 0 */6 * * *")
//	public void exceuteSyncProcess() {
//
//		logger.info("Sync Process:");
//		try
//		{
//			UserInfo userinfo=new UserInfo();
//			final int nsyncType = 44;//Auto
//			synchronizationDAO.syncRecords(userinfo, nsyncType);
//		}
//		catch(Exception e)
//		{
//			logger.error("error:"+ e.getMessage());
//		}
//
//	}
//	// Three months once delete sync history related tables  by using scheduler  --ALPD-4159 ,work done by Dhanushya R I
//	//second, minute, hour, day of month, month, day(s) of week
//	//Scheduled Every day at 00:00:00
//	@Override
//	@Scheduled(cron ="0 0 0 * * ?")
//	public void deleteSync() {
//		logger.info("Deletion of Sync:");
//		try
//		{
//		logger.info("Deletion of Records for every 3Months");
//		synchronizationDAO.deleteSync();
//		}
//		catch(Exception e)
//		{
//			logger.error("error:"+ e.getMessage());
//		}
//
//	}
//}
package com.agaramtech.qualis.scheduler.service;


