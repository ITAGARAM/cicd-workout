package com.agaramtech.qualis.scheduler.service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.configuration.model.Settings;
import com.agaramtech.qualis.externalorder.model.ExternalOrderResult;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class SpringSchedularDAOImpl implements SpringSchedularDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(SpringSchedularDAOImpl.class);

	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final FTPUtilityFunction ftpUtilityFunction;

	public SpringSchedularDAOImpl(JdbcTemplate jdbcTemplate, JdbcTemplateUtilityFunction jdbcUtilityFunction,
			FTPUtilityFunction ftpUtilityFunction) {
		super();
		this.jdbcTemplate = jdbcTemplate;
		this.jdbcUtilityFunction = jdbcUtilityFunction;
		this.ftpUtilityFunction = ftpUtilityFunction;
	}

	@Override
	// @Scheduled(cron = "0 0 0 ? * ?")
	public void testGroupSpecExpiryTask() {
		LOGGER.info("Study plan spec expiry job");
		jdbcTemplate.execute("exec sp_qualis_testgpspecUpdateStatus");
	}

	/*
	 * @Override
	 *
	 * @Scheduled(fixedDelay = 5 * 60 * 1000) public void
	 * materialinventoryexpirecheck() { LOGGER.info("Study plan spec expiry job");
	 * jdbcTemplate.execute("call sp_material_inventory_expire_check()"); }
	 */

	/*
	 * @Override
	 *
	 * @Scheduled(fixedDelay = 5 * 60 * 1000) public void
	 * materialinventorynextvalidation() {
	 * LOGGER.info("Study plan spec expiry job");
	 * jdbcTemplate.execute("call sp_material_inventory_nextvalidation()"); }
	 */

	// @Override
	// @Scheduled(cron = "0 0/5 * * * *")
	// public void schedulerForPreregister() {
	// LOGGER.info("schedulerForPreregister");
	// jdbcTemplate.execute("select * from fn_schedulepreregistration()");
	// }
	//
	//

	@Override
	// @Scheduled(fixedDelay = 1 * 60 * 1000)
	public void schedulerprereg() {
		// LOGGER.info("Study plan spec expiry job");
		jdbcTemplate.execute("select * from  fn_schedulepreregistration()");
	}

	// @Override
	// @Scheduled(cron ="0 0 0 * * ?")
	// public void methodvalidityautoexpire() {
	//
	// LOGGER.info("method validity auto expiry");
	// jdbcTemplate.execute("select * from fn_methodvalidityautoexpire()");
	// }

	// @Override
	// @Scheduled(cron = "0 5 0 ? * *")
	// public void materialinventoryvalidation() {
	//
	// LOGGER.info("material validity validation");
	// jdbcTemplate.execute("call sp_material_inventory_expire_check()");
	// jdbcTemplate.execute("call sp_material_inventory_nextvalidation()");
	// }

	@Override
	public void schedulerForPreregister() {
		// TODO Auto-generated method stub

	}

	// @Override
	// @Scheduled(fixedDelay = 1 * 60 * 1000)
	// public void call() throws Exception {
	// // TODO Auto-generated method stub
	// resultEntryDAO.call();
	// }

	// @Scheduled(cron = "*/5 * * * * *")
	// Sonia Commented this code for ALPD-4184
	// @Scheduled(cron = "0 */5 * ? * *")
	@Override
	public void sentResultToPortal() throws Exception {
		jdbcTemplate.execute("delete from externalorderresult where nsentstatus="
				+ Enumeration.TransactionStatus.SENTSUCCESS.gettransactionstatus());
		String Str = "select ap.ntransactionresultcode,ap.nparametertypecode,eo.sorderseqno as serialnumber,eo.sexternalorderid as externalsampleid,er.nexternalorderresultcode,to_json(er.jsondata)::text as jsondata,rt.jsondata->>'stestsynonym' stestname,rt.ntestcode,rt.ntestrepeatno as nrepeatno,rt.ntestretestno as nretestno "
				+ " from externalorderresult er,registrationtest rt,externalorder eo,approvalparameter ap  "
				+ " where  ap.ntransactionresultcode=er.ntransactionresultcode and ap.ntransactiontestcode=rt.ntransactiontestcode and eo.nexternalordercode=er.nexternalordercode and er.ntransactiontestcode=rt.ntransactiontestcode and er.nsentstatus in ("
				+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ","
				+ Enumeration.TransactionStatus.SENTFAILED.gettransactionstatus() + ") and er.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		List<ExternalOrderResult> lst = jdbcTemplate.query(Str, new ExternalOrderResult());
		List lstcountcheck = jdbcTemplate.queryForList(Str);
		String Setting = "select ssettingvalue from settings where nsettingcode=24";
		Settings objSettings = (Settings) jdbcUtilityFunction.queryForObject(Setting, Settings.class, jdbcTemplate);

		for (int i = 0; i < lst.size(); i++) {
			try {
				int nexternalorderresultcode = lst.get(i).getNexternalorderresultcode();
				HttpClient httpClient = HttpClients.createDefault();
				HttpPost httpPost = new HttpPost(
						objSettings.getSsettingvalue() + "/portal/UpdateresultforLimsorderswithattachment");
				ObjectMapper objectMapper = new ObjectMapper();
				String jsonPayload = objectMapper.writeValueAsString(lstcountcheck.get(i));
				MultipartEntityBuilder builder = MultipartEntityBuilder.create();
				builder.addTextBody("Result", jsonPayload, ContentType.TEXT_PLAIN);
				if (lst.get(i).getNparametertypecode() == 4) {
					String Strfile = "select approvalparameter.jsondata->>'ssystemfilename' from approvalparameter where approvalparameter.ntransactionresultcode="
							+ lst.get(i).getNtransactionresultcode();
					List<String> ssystemfilename = jdbcTemplate.queryForList(Strfile, String.class);

					Map<String, Object> map = new HashMap<>();
					UserInfo objUserInfo = new UserInfo();
					objUserInfo.setNmastersitecode((short) -1);
					objUserInfo.setNformcode((short) 61);
					objUserInfo.setSloginid("PortalAttachment");
					map = ftpUtilityFunction.FileViewUsingFtp(ssystemfilename.get(0), -1, objUserInfo, "", "");

					// ALPD-4436
					// To get path value from system's environment variables instead of absolutepath
					final String homePath = ftpUtilityFunction.getFileAbsolutePath();
					String fileDownloadURL = System.getenv(homePath)// new File("").getAbsolutePath()
							+ Enumeration.FTP.UPLOAD_PATH.getFTP() + objUserInfo.getSloginid() + "\\";
					File textFile = new File((fileDownloadURL + ssystemfilename.get(0)).toString());

					builder.addPart("file", new FileBody(textFile))
					.addTextBody("filename", ssystemfilename.get(0).toString())
					.addTextBody("sampleid", lst.get(i).getExternalsampleid());// lst.get(i).getNexternalorderresultcode().toString());

				}
				HttpEntity entity = builder.build();
				httpPost.setEntity(entity);
				// StringEntity entity = new StringEntity(jsonPayload,
				// ContentType.APPLICATION_JSON);
				// httpPost.setEntity(entity);

				HttpResponse response = httpClient.execute(httpPost);

				int statusCode = response.getStatusLine().getStatusCode();
				if (statusCode == 200) {
					String str = "update externalorderresult set nsentstatus="
							+ Enumeration.TransactionStatus.SENTSUCCESS.gettransactionstatus()
							+ " where nexternalorderresultcode=" + nexternalorderresultcode;
					jdbcTemplate.execute(str);

				} else {
					String str = "update externalorderresult set nsentstatus="
							+ Enumeration.TransactionStatus.SENTFAILED.gettransactionstatus()
							+ " where nexternalorderresultcode=" + nexternalorderresultcode;
					jdbcTemplate.execute(str);
					System.out.println("Fail");

				}
			} catch (Exception e) {
				LOGGER.info(e.getMessage());
			}
		}

	}

	@Override
	@SuppressWarnings("unused")
	// @Scheduled(cron = "*/5 * * * * *")
	// Sonia Commented this code for ALPD-4184
	// @Scheduled(cron = "0 */5 * ? * *")
	public void sentExternalOrderStatus() throws Exception {

		final Map<String, Object> outputMap = new HashMap<String, Object>();
		jdbcTemplate.execute("delete from externalorderstatus where nsentstatus="
				+ Enumeration.TransactionStatus.SENTSUCCESS.gettransactionstatus());
		List<Map<String, Object>> lst = jdbcTemplate.queryForList(
				"select es.*,e.sorderseqno from externalorder e,externalorderstatus es where e.nexternalordercode=es.nexternalordercode and es.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and es.nsentstatus in ("
						+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ","
						+ Enumeration.TransactionStatus.SENTFAILED.gettransactionstatus() + ") order by 1 asc ");
		String Setting = "select ssettingvalue from settings where nsettingcode=24";
		Settings objSettings = (Settings) jdbcUtilityFunction.queryForObject(Setting, Settings.class, jdbcTemplate);
		for (int i = 0; i < lst.size(); i++) {
			int limsstatus = (int) lst.get(i).get("ntransactionstatus") == Enumeration.TransactionStatus.RECEIVED
					.gettransactionstatus()
					? Enumeration.TransactionStatus.PREREGISTER.gettransactionstatus()
							: (int) lst.get(i).get("ntransactionstatus") == Enumeration.TransactionStatus.PARTIALLY
							.gettransactionstatus()
							? Enumeration.TransactionStatus.REGISTERED.gettransactionstatus()
									: (int) lst.get(i).get("ntransactionstatus");
			int nexternalorderstatuscode = (int) lst.get(i).get("nexternalorderstatuscode");
			outputMap.put("orderstatus", lst.get(i));
			List<Map<String, Object>> lstPortalStatus = new ArrayList<>();
			Map<String, Object> mapPortalValues = new HashMap<>();
			mapPortalValues.put("serialnumber", lst.get(i).get("sorderseqno"));
			mapPortalValues.put("statuscode", (int) lst.get(i).get("ntransactionstatus"));
			lstPortalStatus.add(mapPortalValues);

			try {

				HttpClient httpClient = HttpClients.createDefault();
				HttpPost httpPost = new HttpPost(objSettings.getSsettingvalue() + "/portal/UpdateMultiSampleStatus");
				httpPost.setHeader("Content-Type", "application/json");
				JSONArray jsonArray = new JSONArray(lstPortalStatus);
				String jsonParams = jsonArray.toString();
				StringEntity entityValue = new StringEntity(jsonParams);
				httpPost.setEntity(entityValue);
				HttpResponse responseValue = httpClient.execute(httpPost);
				int statusCode = responseValue.getStatusLine().getStatusCode();
				if (statusCode == 200) {
					String str = "update externalorderstatus set nsentstatus="
							+ Enumeration.TransactionStatus.SENTSUCCESS.gettransactionstatus()
							+ " where nexternalorderstatuscode=" + nexternalorderstatuscode;
					// String str1="update externalorder set ntransactionstatus="+limsstatus+" where
					// nexternalordercode="+(int)lst.get(i).get("nexternalordercode");
					// jdbcTemplate.execute(str+";"+str1);
					jdbcTemplate.execute(str);

				} else {
					String str = "update externalorderstatus set nsentstatus="
							+ Enumeration.TransactionStatus.SENTFAILED.gettransactionstatus()
							+ " where nexternalorderstatuscode=" + nexternalorderstatuscode;
					jdbcTemplate.execute(str);

				}
			} catch (Exception e) {
				LOGGER.info(e.getMessage());
			}

		}

	}

	// Execute sync received data in every 5 minutes
	@Override
	//@Scheduled(cron = "0 */5 * ? * *")
	public void executeSyncReceivedData() {
		LOGGER.info("Sync received data execution:");
		try {
			jdbcTemplate.execute("call sp_sync_received_data_execution()");
		} catch (Exception e) {
			LOGGER.error("error:" + e.getMessage());
		}

	}

}
