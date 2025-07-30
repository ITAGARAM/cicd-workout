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
//import com.agaramtech.lims.dao.support.bridge.RDBMSBridge;
//import com.agaramtech.lims.dao.support.bridge.RDBMSParameter;
//import com.agaramtech.lims.dao.support.enums.RDBMSEnum;
//import com.agaramtech.lims.dao.support.global.AgaramtechGeneralfunction;
//import com.agaramtech.resultentry.service.ResultEntryDAO;
//
//public class SchedularMethodValidityAutoexpireDAOImpl extends ProjectDAOSupport implements SchedularMethodValidityAutoexpireDAO {
//
//	private AgaramtechGeneralfunction objGeneral;
//	private static int dataBaseType = 0;
//
//	Logger logger = LoggerFactory.getLogger(this.getClass());
//
//	public SchedularMethodValidityAutoexpireDAOImpl(RDBMSParameter objParameter, AgaramtechGeneralfunction objGeneral,
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
//	@Autowired
//	ResultEntryDAO resultEntryDAO;
//
//	//second, minute, hour, day of month, month, day(s) of week
//	//Scheduled Every day at 00:00:00
//	@Scheduled(cron ="0 0 0 * * ?")
//	public void methodvalidityautoexpire() {
//
//		logger.info("method validity auto expiry");
//		getJdbcTemplate().execute("select * from  fn_methodvalidityautoexpire()");
//	}
//}
package com.agaramtech.qualis.scheduler.service;


