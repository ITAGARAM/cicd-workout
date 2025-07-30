package com.agaramtech.qualis.digitalsignature.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.digitalsignature.model.DigitalSignature;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.submitter.service.city.CityDAOImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class DigitalSignatureDAOImpl implements DigitalSignatureDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(CityDAOImpl.class);

	private final FTPUtilityFunction ftpUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	@Override
	public ResponseEntity<Object> getDigitalSignature(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final int nusercode = (int) inputMap.get("nusercode");
		final String strDigitalSignature = "select nusercode, sdigisignname, sdigisignftp, ssecuritykey, nsitecode, nstatus from userdigitalsign where nusercode="
				+ nusercode + " and nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final DigitalSignature lstDigitalSignature = (DigitalSignature) jdbcUtilityFunction
				.queryForObject(strDigitalSignature, DigitalSignature.class, jdbcTemplate);
		final short screenFormcode = userInfo.getNformcode();
		userInfo.setNformcode((short) -1);
		if (lstDigitalSignature != null) {
			if (!lstDigitalSignature.getSsecuritykey().equals("null")
					&& !lstDigitalSignature.getSsecuritykey().equals("")) {
				final String securityKeyDecrypted = projectDAOSupport.decryptPassword("userdigitalsign", "nusercode",
						nusercode, "ssecuritykey");
				lstDigitalSignature.setSsecuritykey(securityKeyDecrypted);
			}
		}
		userInfo.setNformcode(screenFormcode);
		return new ResponseEntity<>(lstDigitalSignature, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> updateDigitalSignature(final MultipartHttpServletRequest request,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> mapObject = new HashMap<String, Object>();

		// ALPD-4436
		// To get path value from system's environment variables instead of absolutepath
		final String homePath = ftpUtilityFunction.getFileAbsolutePath();

		final ObjectMapper objMapper = new ObjectMapper();
		final int nusercode = userInfo.getNusercode();
		mapObject.put("nusercode", nusercode);
		mapObject.put("sprimarykeyvalue", "nusercode");
		mapObject.put("stablename", "userdigitalsign");
		final DigitalSignature digitalSignatureFileDetails = objMapper.readValue(request.getParameter("digisignfile"),
				new TypeReference<DigitalSignature>() {
				});
		final String sdigisignname = (digitalSignatureFileDetails.getSdigisignname() == null) ? ""
				: digitalSignatureFileDetails.getSdigisignname();
		final String sdigisignftp = (digitalSignatureFileDetails.getSdigisignftp() == null) ? ""
				: digitalSignatureFileDetails.getSdigisignftp();
		final String ssecuritykey = (digitalSignatureFileDetails.getSsecuritykey() == null) ? ""
				: digitalSignatureFileDetails.getSsecuritykey();
		String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();

		if ((!sdigisignname.isEmpty()) && (!sdigisignftp.isEmpty())) {
			final short screenFormcode = userInfo.getNformcode();
			userInfo.setNformcode((short) -1);
			sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, userInfo);
			// ALPD-4436
			final String sProfilePath = System.getenv(homePath)// new File("").getAbsolutePath()
					+ Enumeration.FTP.USERSIGN_PATH.getFTP();
			ftpUtilityFunction.FileViewUsingFtp(sdigisignftp, -1, userInfo, sProfilePath, "");
			userInfo.setNformcode(screenFormcode);
			final String[] args = new String[4];
			// ALPD-4436
			final String absolutePath = System.getenv(homePath)// new File("").getAbsolutePath()
					+ Enumeration.FTP.USERSIGN_PATH.getFTP();
			final String scertfilename = absolutePath + "\\" + sdigisignftp;

			args[0] = scertfilename;// curdir + "/test_assets/Saravanan.pfx"; //key store

			args[1] = ssecuritykey;// "123456";

			final File ksFile = new File(args[0]);
			final KeyStore keystore = KeyStore.getInstance("PKCS12");
			final char[] pin = args[1].toCharArray();
			try (InputStream is = new FileInputStream(ksFile)) {
				keystore.load(is, pin);
			} catch (final IOException e) {
				LOGGER.info(e.getMessage());
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_INCORRECTPASSWORD", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		}
		if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus() == sReturnString) {
			final DigitalSignature getDigitalSignatureByUserCode = (DigitalSignature) (getDigitalSignature(mapObject,
					userInfo)).getBody();
			String strQuery = "";
			if (getDigitalSignatureByUserCode != null) {
				strQuery = "update userdigitalsign set sdigisignname = '" + sdigisignname + "', sdigisignftp='"
						+ sdigisignftp + "'" + ", ssecuritykey='" + ssecuritykey + "', dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nusercode =" + nusercode;
			} else {
				strQuery = "insert into userdigitalsign (nusercode, sdigisignname, sdigisignftp, ssecuritykey, dmodifieddate, nsitecode, nstatus) values ("
						+ nusercode + ", '" + sdigisignname + "', '" + sdigisignftp + "', '" + ssecuritykey + "', '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "', " + userInfo.getNmastersitecode()
						+ ", " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
			}
			jdbcTemplate.execute(strQuery);
			if (ssecuritykey != null && !ssecuritykey.equals("")) {
				projectDAOSupport.encryptPassword("userdigitalsign", "nusercode", userInfo.getNusercode(), ssecuritykey,
						"ssecuritykey");
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(sReturnString, userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
		final List<String> listOfMultiLingual = commonFunction.getMultilingualMultipleMessage(
				Arrays.asList("IDS_DIGITALSIGNUPDATEDSUCCESFULLY", "IDS_LOGINID", "IDS_USERNAME", "IDS_USERROLE"),
				userInfo.getSlanguagefilename());
		final String scomments = listOfMultiLingual.get(0) + ": " + listOfMultiLingual.get(1) + ": "
				+ userInfo.getSloginid() + ";" + listOfMultiLingual.get(2) + ": " + userInfo.getSusername() + ";"
				+ listOfMultiLingual.get(3) + ": " + userInfo.getSuserrolename();
		final Map<String, Object> objMap = new HashMap<>();
		objMap.put(commonFunction.getMultilingualMessage("IDS_DIGITALSIGNATURE", userInfo.getSlanguagefilename()),
				sdigisignname);
		mapObject.put("additionalFields", objMap);
		auditUtilityFunction.insertAuditAction(userInfo, "IDS_DIGITALSIGNUPDATEDSUCCESFULLY", scomments, mapObject);
		return null;
	}
}