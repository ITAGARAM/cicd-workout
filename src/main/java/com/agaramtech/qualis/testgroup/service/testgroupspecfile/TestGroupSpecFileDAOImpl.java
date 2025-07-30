package com.agaramtech.qualis.testgroup.service.testgroupspecfile;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.configuration.model.TreeVersionTemplate;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.LinkMaster;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.TestGroupCommonFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecFile;
import com.agaramtech.qualis.testgroup.model.TestGroupSpecification;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class TestGroupSpecFileDAOImpl implements TestGroupSpecFileDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestGroupSpecFileDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private TestGroupCommonFunction testGroupCommonFunction;
	private final FTPUtilityFunction ftpUtilityFunction;

	@Override
	public ResponseEntity<Object> getSpecificationFile(final UserInfo userInfo,
			final TestGroupSpecification objSpecification) throws Exception {

		return testGroupCommonFunction.getSpecificationFile(userInfo, objSpecification);
	}

	@Override
	public ResponseEntity<Object> createSpecificationFile(final UserInfo userInfo,
			final MultipartHttpServletRequest request) throws Exception {

		final String sQuery = " lock  table locktestgroupspecfile "
				+ Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final ObjectMapper objMapper = new ObjectMapper();
		final List<TestGroupSpecFile> lstSpecFile = objMapper.readValue(request.getParameter("testgroupspecfile"),
				new TypeReference<List<TestGroupSpecFile>>() {
				});
		final TestGroupSpecification objSpecification = objMapper
				.readValue(request.getParameter("testgroupspecification"), TestGroupSpecification.class);

		if (lstSpecFile != null && lstSpecFile.size() > 0) {
			final TestGroupSpecification objTestGroupSpec = testGroupCommonFunction
					.getActiveSpecification(objSpecification.getNallottedspeccode(), userInfo);
			if (objTestGroupSpec != null) {
				if (objTestGroupSpec.getNapprovalstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
						|| objTestGroupSpec.getNapprovalstatus() == Enumeration.TransactionStatus.CORRECTION
								.gettransactionstatus()) {
					String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
					if (lstSpecFile.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
						sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, userInfo);// Folder Name -
																									// master
					}
					if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(sReturnString)) {
						final Instant instantDate = dateUtilityFunction.getCurrentDateTime(userInfo)
								.truncatedTo(ChronoUnit.SECONDS);
						final String sattachmentDate = dateUtilityFunction.instantDateToString(instantDate);
						final int offset = dateUtilityFunction.getCurrentDateTimeOffset(userInfo.getStimezoneid());
						lstSpecFile.forEach(objtf -> {
							objtf.setDcreateddate(instantDate);
							objtf.setNoffsetdcreateddate(offset);
							objtf.setScreateddate(sattachmentDate.replace("T", " "));
						});

						String sequencequery = "select nsequenceno from SeqNoTestGroupmanagement where stablename ='testgroupspecfile' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
						int nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);
						nsequenceno++;
						String insertquery = "Insert into testgroupspecfile(nspecfilecode,nallottedspeccode,nlinkcode,nattachmenttypecode,sfilename,sdescription,nfilesize,dcreateddate,noffsetdcreateddate,ntzcreateddate,ssystemfilename,dmodifieddate,nstatus,nsitecode)"
								+ "values (" + nsequenceno + "," + lstSpecFile.get(0).getNallottedspeccode() + ","
								+ lstSpecFile.get(0).getNlinkcode() + "," + lstSpecFile.get(0).getNattachmenttypecode()
								+ "," + " N'" + stringUtilityFunction.replaceQuote(lstSpecFile.get(0).getSfilename())
								+ "',N'" + stringUtilityFunction.replaceQuote(lstSpecFile.get(0).getSdescription())
								+ "'," + lstSpecFile.get(0).getNfilesize() + "," + " '"
								+ lstSpecFile.get(0).getDcreateddate() + "',"
								+ lstSpecFile.get(0).getNoffsetdcreateddate() + "," + userInfo.getNtimezonecode()
								+ ",N'" + lstSpecFile.get(0).getSsystemfilename() + "','"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + ""
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ","
								+ userInfo.getNmastersitecode() + ")";
						LOGGER.info(insertquery);
						jdbcTemplate.execute(insertquery);

						String updatequery = "update SeqNoTestGroupmanagement set nsequenceno =" + nsequenceno
								+ " where stablename ='testgroupspecfile' and nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+"";
						jdbcTemplate.execute(updatequery);

						final List<String> multilingualIDList = new ArrayList<>();
						multilingualIDList.add(
								lstSpecFile.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
										? "IDS_ADDSPECFILE"
										: "IDS_ADDSPECLINK");

						final List<Object> listObject = new ArrayList<Object>();
						lstSpecFile.get(0).setNspecfilecode(nsequenceno);
						listObject.add(lstSpecFile);

						auditUtilityFunction.fnInsertListAuditAction(listObject, 1, null, multilingualIDList, userInfo);

						return getSpecificationFile(userInfo, objSpecification);
					} else {
						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage(sReturnString, userInfo.getSlanguagefilename()),
								HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_SPECIFICATIONSTATUSMUSTBEDRAFTCORRECTION",
									userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SPECALREADYDELETED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> updateSpecificationFile(UserInfo userInfo, MultipartHttpServletRequest request)
			throws Exception {
		ObjectMapper objMapper = new ObjectMapper();

		final List<TestGroupSpecFile> lstSpecFile = objMapper.readValue(request.getParameter("testgroupspecfile"),
				new TypeReference<List<TestGroupSpecFile>>() {
				});
		final TestGroupSpecification objSpecification = objMapper
				.readValue(request.getParameter("testgroupspecification"), TestGroupSpecification.class);

		if (lstSpecFile != null && lstSpecFile.size() > 0) {

			final TestGroupSpecFile objTestFile = lstSpecFile.get(0);
			final TestGroupSpecification objTestGroupSpec = testGroupCommonFunction
					.getActiveSpecification(objSpecification.getNallottedspeccode(), userInfo);

			if (objTestGroupSpec != null) {

				if (objTestGroupSpec.getNapprovalstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
						|| objTestGroupSpec.getNapprovalstatus() == Enumeration.TransactionStatus.CORRECTION
								.gettransactionstatus()) {

					final int isFileEdited = Integer.valueOf(request.getParameter("isFileEdited"));
					String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();

					if (isFileEdited == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
						if (objTestFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
							sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, userInfo);// Folder Name -
																										// master
						}
					}

					if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(sReturnString)) {
						final String sQuery = "select * from testgroupspecfile where nspecfilecode = "
								+ objTestFile.getNspecfilecode() + " and nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
						final TestGroupSpecFile objTGSF = (TestGroupSpecFile) jdbcUtilityFunction.queryForObject(sQuery,
								TestGroupSpecFile.class, jdbcTemplate);

						if (objTGSF != null) {
							String ssystemfilename = "";
							if (objTestFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
								ssystemfilename = objTestFile.getSsystemfilename();
							}
							final String sUpdateQuery = "update testgroupspecfile set sfilename=N'"
									+ stringUtilityFunction.replaceQuote(objTestFile.getSfilename()) + "',"
									+ " sdescription=N'"
									+ stringUtilityFunction.replaceQuote(objTestFile.getSdescription())
									+ "', ssystemfilename= N'" + ssystemfilename + "'," + " nattachmenttypecode = "
									+ objTestFile.getNattachmenttypecode() + ", nlinkcode=" + objTestFile.getNlinkcode()
									+ "," + " nfilesize = " + objTestFile.getNfilesize() + ",dmodifieddate='"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where nspecfilecode = "
									+ objTestFile.getNspecfilecode();

							objTestFile.setDcreateddate(objTGSF.getDcreateddate());
							objTGSF.setScreateddate(objTestFile.getScreateddate());

							jdbcTemplate.execute(sUpdateQuery);

							final List<String> multilingualIDList = new ArrayList<>();
							final List<Object> lstOldObject = new ArrayList<Object>();
							final List<Object> lstNewObject = new ArrayList<Object>();
							lstNewObject.add(objTestFile);

							multilingualIDList.add(
									objTestFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
											? "IDS_EDITSPECFILE"
											: "IDS_EDITSPECLINK");
							lstOldObject.add(objTGSF);

							auditUtilityFunction.fnInsertAuditAction(lstNewObject, 2, lstOldObject, multilingualIDList,
									userInfo);
							return getSpecificationFile(userInfo, objSpecification);
						} else {
							// status code:417
							return new ResponseEntity<>(commonFunction.getMultilingualMessage(
									Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
									userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
						}
					} else {
						// status code:417
						return new ResponseEntity<>(
								commonFunction.getMultilingualMessage(sReturnString, userInfo.getSlanguagefilename()),
								HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_SPECIFICATIONSTATUSMUSTBEDRAFTCORRECTION",
									userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				// status code:417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SPECALREADYDELETED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> deleteSpecificationFile(UserInfo userInfo, TestGroupSpecFile objTestGroupSpecFile,
			final TestGroupSpecification objSpecification, final int ntreeversiontempcode) throws Exception {
		final TestGroupSpecification objTestGroupSpec = testGroupCommonFunction
				.getActiveSpecification(objTestGroupSpecFile.getNallottedspeccode(), userInfo);

		TreeVersionTemplate objRetiredTemplate = testGroupCommonFunction
				.checkTemplateIsRetiredOrNot(ntreeversiontempcode);
		if (objRetiredTemplate.getNtransactionstatus() != Enumeration.TransactionStatus.RETIRED
				.gettransactionstatus()) {

			if (objTestGroupSpec != null) {
				if (objTestGroupSpec.getNapprovalstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
						|| objTestGroupSpec.getNapprovalstatus() == Enumeration.TransactionStatus.CORRECTION
								.gettransactionstatus()) {
					final TestGroupSpecFile objSpecFile = getSpecificationFileById(userInfo, objTestGroupSpecFile);
					if (objSpecFile != null) {
						if (objSpecFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
							ftpUtilityFunction.deleteFTPFile(Arrays.asList(objSpecFile.getSsystemfilename()), "",
									userInfo);// Folder Name
							// - master
						} else {
							objSpecFile.setScreateddate(null);
						}
						final String sUpdateQuery = "update testgroupspecfile set nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ",dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where nspecfilecode = "
								+ objSpecFile.getNspecfilecode();
						jdbcTemplate.execute(sUpdateQuery);
						final List<String> multilingualIDList = new ArrayList<>();
						final List<Object> lstObject = new ArrayList<>();
						multilingualIDList
								.add(objSpecFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
										? "IDS_DELETESPECFILE"
										: "IDS_DELETESPECLINK");
						lstObject.add(objSpecFile);
						auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, multilingualIDList, userInfo);
						return getSpecificationFile(userInfo, objSpecification);
					} else {
						// status code:417
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(
								Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_SPECIFICATIONSTATUSMUSTBEDRAFTCORRECTION",
									userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				// status code:417
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SPECALREADYDELETED",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDTEMPLATEISRETIRED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	private TestGroupSpecFile getSpecificationFileById(UserInfo userInfo, final TestGroupSpecFile objSpecFile)
			throws Exception {
		final String sSpecFileQuery = "select tgsf.nspecfilecode, tgsf.nallottedspeccode, tgsf.nlinkcode, tgsf.nattachmenttypecode, tgsf.sfilename,"
				+ " tgsf.sdescription, tgsf.nfilesize, tgsf.dcreateddate, tgsf.ssystemfilename, coalesce(at.jsondata->'sattachmenttype'->>'"
				+ userInfo.getSlanguagetypecode() + "'"
				+ "	 ,at.jsondata->'sattachmenttype'->>'en-US') as stypename, lm.jsondata->>'slinkname' as slinkname"
				+ " from testgroupspecfile tgsf, attachmenttype at, linkmaster lm "
				+ " where at.nattachmenttypecode = tgsf.nattachmenttypecode and lm.nlinkcode = tgsf.nlinkcode"
				+ " and at.nstatus = tgsf.nstatus and lm.nstatus = tgsf.nstatus and tgsf.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tgsf.nspecfilecode = "
				+ objSpecFile.getNspecfilecode() + " and at.nsitecode=" + userInfo.getNmastersitecode()
				+ " and lm.nsitecode=" + userInfo.getNmastersitecode();
		return (TestGroupSpecFile) jdbcUtilityFunction.queryForObject(sSpecFileQuery, TestGroupSpecFile.class,
				jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> getActiveSpecFileById(final UserInfo userInfo, final TestGroupSpecFile objSpecFile,
			final int ntreeversiontempcode) throws Exception {
		final TestGroupSpecFile objTestGroupSpecFile = getSpecificationFileById(userInfo, objSpecFile);

		TreeVersionTemplate objRetiredTemplate = testGroupCommonFunction
				.checkTemplateIsRetiredOrNot(ntreeversiontempcode);
		if (objRetiredTemplate.getNtransactionstatus() != Enumeration.TransactionStatus.RETIRED
				.gettransactionstatus()) {
			if (objSpecFile != null) {
				return new ResponseEntity<Object>(objTestGroupSpecFile, HttpStatus.OK);
			} else {
				// status code:417
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTEDTEMPLATEISRETIRED",
					userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> viewTestGroupSpecFile(TestGroupSpecFile objSpecFile, UserInfo userInfo,
			TestGroupSpecification objSpecification) throws Exception {
		final TestGroupSpecification objTGS = testGroupCommonFunction
				.getActiveSpecification(objSpecification.getNallottedspeccode(), userInfo);
		if (objTGS != null) {
			Map<String, Object> map = new HashMap<String, Object>();
			String sQuery = "select * from testgroupspecfile where nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nspecfilecode = "
					+ objSpecFile.getNspecfilecode();
			final TestGroupSpecFile objTGTF = (TestGroupSpecFile) jdbcUtilityFunction.queryForObject(sQuery,
					TestGroupSpecFile.class, jdbcTemplate);
			if (objTGTF != null) {
				if (objTGTF.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
					map = ftpUtilityFunction.FileViewUsingFtp(objTGTF.getSsystemfilename(), -1, userInfo, "", "");
				} else {
					sQuery = "select jsondata->>'slinkname' as slinkname from linkmaster where nlinkcode="
							+ objTGTF.getNlinkcode() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					LinkMaster objlinkmaster = (LinkMaster) jdbcUtilityFunction.queryForObject(sQuery, LinkMaster.class,
							jdbcTemplate);
					map.put("AttachLink", objlinkmaster.getSlinkname() + objTGTF.getSfilename());
				}
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> lstObject = new ArrayList<>();
				multilingualIDList.add(objTGTF.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
						? "IDS_VIEWSPECFILE"
						: "IDS_VIEWSPECLINK");
				lstObject.add(objTGTF);
				auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, multilingualIDList, userInfo);
				return new ResponseEntity<Object>(map, HttpStatus.OK);
			} else {
				// status code:417
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			// status code:417
			return new ResponseEntity<Object>(
					commonFunction.getMultilingualMessage("IDS_SPECALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

}
