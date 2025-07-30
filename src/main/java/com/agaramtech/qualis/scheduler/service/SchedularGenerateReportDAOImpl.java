package com.agaramtech.qualis.scheduler.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.externalorder.service.ExternalOrderDAO;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.release.service.ReleaseDAO;

@Repository
public class SchedularGenerateReportDAOImpl implements SchedularGenerateReportDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(SchedularGenerateReportDAOImpl.class);

	private final ExternalOrderDAO externalOrderDAO;
	private final ReleaseDAO releaseDAO;

	public SchedularGenerateReportDAOImpl(ExternalOrderDAO externalOrderDAO, @Lazy ReleaseDAO releaseDAO) {
		super();
		this.externalOrderDAO = externalOrderDAO;
		this.releaseDAO = releaseDAO;
	}

	// @SuppressWarnings({ "unchecked" })
	@Override
	// @Scheduled(cron = "* 0/2 * * * *")
	// Sonia Commented this code for ALPD-4184
	// @Scheduled(cron = "0 */5 * ? * *")
	public void schedularGenerateReport() throws Exception {

		Map<String, Object> inputMap = new LinkedHashMap<>();

		inputMap.put("autoSync", "autoSync");

		ResponseEntity<Object> returnResp = releaseDAO.reportGenerationSync(inputMap);

		if (returnResp != null) {

			Map<String, Object> checkExist = (Map<String, Object>) returnResp.getBody();

			if (("Success").equals(checkExist.get("rtnStatus"))) {

				LOGGER.info("Scheduler based Report generated successfully.");

			} else {

				LOGGER.info("Value for the auto sync record is empty.");
			}

		} else {

			LOGGER.info("Record not available for scheduled based report generation");
		}
	}

	@SuppressWarnings({ "unchecked", "unused" })
	@Override
	public String AsyncReportGeneration(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		// syncaction=ManualSyncreport

		// @SuppressWarnings("unlikely-arg-type")
		CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
			try {
				Thread.sleep(5000);
				try {

					inputMap.put("manualsync", "manualsync");
					ResponseEntity<Object> returnResp = releaseDAO.reportGenerationSync(inputMap);

					if (returnResp != null) {

						Map<String, Object> checkExist = (Map<String, Object>) returnResp.getBody();

						if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(checkExist.get("rtnStatus"))) {

							LOGGER.error("Success: Report are Success excuted");
							LOGGER.error("Success: Report are ready to send to portal.");

							ResponseEntity<Object> portalRes = externalOrderDAO.SendToPortalReport(userInfo);

							Map<String, Object> checkPortal = (Map<String, Object>) returnResp.getBody();

							if (checkPortal.equals(Enumeration.FTPReplycodes.SERVER_RESPONSE_OK.getReplyCode())) {

								LOGGER.error("Success: Report are Success to portal");

							} else {

								LOGGER.error("Error: Report are not sent to portal");

							}

						} else {

							LOGGER.error("error: Value  coareporthistorygeneration");
						}

					} else {

						LOGGER.error("error:record not foundcoareporthistorygeneration");
					}
				} catch (Exception e) {
					LOGGER.error("error:" + e.getMessage());
				}
			} catch (InterruptedException e) {
				LOGGER.error("error:" + e.getMessage()); // tread calls
			}
		});
		return "Success";

	}

	// return code need to do

}
