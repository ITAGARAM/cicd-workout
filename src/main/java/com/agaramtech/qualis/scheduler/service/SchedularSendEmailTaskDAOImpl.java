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
//public class SchedularSendEmailTaskDAOImpl extends ProjectDAOSupport implements SchedularSendEmailTaskDAO {
//
//	private AgaramtechGeneralfunction objGeneral;
//	private static int dataBaseType = 0;
//
//	Logger logger = LoggerFactory.getLogger(this.getClass());
//
//	public SchedularSendEmailTaskDAOImpl(RDBMSParameter objParameter, AgaramtechGeneralfunction objGeneral,
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
//	//scheduled for every 5 minutes using cron expression(0 */5 * ? * *)
//		@Scheduled(cron =  "0 */5 * ? * *")
//
//		public void schedularSendEmailTask() {
////			SimpleDateFormat simpleDateFormat = new SimpleDateFormat("hh:mm:ss");
////			String time = simpleDateFormat.format(new Date(System.currentTimeMillis()));
////			System.out.println("Task for email---->" + time);
////			getJdbcTemplate().execute("exec sp_emailtrans");
//			try {
//				sendMailCommonFunction();
////				if(map!=null && map.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus())==Enumeration.ReturnStatus.FAILED.getreturnstatus()) {
////					sendMail(map);
////				}
//			} catch (Exception e) {
//				logger.info("cause==>", e.getCause());
//	     		logger.info(e.getMessage());
//
//	     	}
////
//		}
//}
package com.agaramtech.qualis.scheduler.service;


