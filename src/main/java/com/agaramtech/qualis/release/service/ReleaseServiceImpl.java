package com.agaramtech.qualis.release.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.configuration.model.FilterName;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.registration.model.COAHistory;
import com.agaramtech.qualis.release.model.COAParent;
import com.agaramtech.qualis.release.model.ReleaseComment;
import com.agaramtech.qualis.release.model.ReleaseOutsourceAttachment;
import com.agaramtech.qualis.release.model.ReleaseTestAttachment;
import com.agaramtech.qualis.release.model.ReleaseTestComment;
import com.agaramtech.qualis.release.model.ReportInfoRelease;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class ReleaseServiceImpl implements ReleaseService {

	private final ReleaseDAO releaseDAO;
	private final CommonFunction commonFunction;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class properties.
	 * 
	 * @param releaseDAO     ReleaseDAO Interface
	 * @param commonFunction CommonFunction holding common utility functions
	 */
	public ReleaseServiceImpl(ReleaseDAO releaseDAO, CommonFunction commonFunction) {
		this.releaseDAO = releaseDAO;
		this.commonFunction = commonFunction;
	}

	@Transactional
	@Override
	public ResponseEntity<Object> getRelease(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {

		return releaseDAO.getRelease(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getRegistrationSubType(Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return releaseDAO.getRegistrationSubType(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getRegistrationType(Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return releaseDAO.getRegistrationType(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Map<String, Object>> getReleaseSample(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return releaseDAO.getReleaseSample(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Map<String, Object>> getFilterStatus(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return releaseDAO.getFilterStatus(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getApprovalVersion(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		return releaseDAO.getApprovalVersion(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateRelease(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		Map<String, Object> objmap = new HashMap<String, Object>();
		final Map<String, Object> returnMap = new HashMap<>();

		final Map<String, Object> sNodeServerStart = releaseDAO.validationCheckForNodeServer(inputMap, userInfo);

		if (sNodeServerStart.get("rtn").equals("Failed")) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_STARTNODESERVER", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			objmap = releaseDAO.seqNoSampleSubSampleTestInsert(inputMap, userInfo);

			if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
					.equals(objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
				inputMap.putAll(objmap);
				
				//Added missed out code on migration from Java 8 to Java 21 --Start
				final List<Integer> coaParent = Arrays.stream(inputMap.get("ncoaparentcode").toString().split(","))
	                    .map(Integer::parseInt)
	                    .collect(Collectors.toList());
				//Added missed out code on migration from Java 8 to Java 21 --End
				
				final Map<String, Object> objReleasedMap = releaseDAO.updateRelease(inputMap, userInfo);

				if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
						.equals(objReleasedMap.get("ReportAvailable"))) {

					//Added missed out code on migration from Java 8 to Java 21 --Start
					
					if(coaParent.size() == 1) {
						
						if(!objReleasedMap.containsKey("PreventTb") ) {
							
							inputMap.putAll(objReleasedMap);
							
							inputMap.put("napproveconfversioncode", (int) inputMap.get("napprovalversioncode"));							

							return releaseDAO.releasedReportGeneration(inputMap,userInfo);

						}
						else {
								if((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()
										.equals(objReleasedMap.get("PreventTb")))) {
								
									inputMap.putAll(objReleasedMap);
									inputMap.put("napproveconfversioncode", (int) inputMap.get("napprovalversioncode"));
									//return new ResponseEntity<>(releaseDAO.releasedReportGeneration(inputMap,userInfo).getBody(), HttpStatus.OK);
								
									inputMap.put("PreventTb", commonFunction.getMultilingualMessage("IDS_PREVENTTBSENDRESULT",
										userInfo.getSlanguagefilename())+" "+objReleasedMap.get("PreventTb"));
								
									return releaseDAO.releasedReportGeneration(inputMap,userInfo);
								}
								else {
								
									if(objReleasedMap.get("PreventTb").equals("MappingNeeded")) {		
											
											returnMap.put("rtn",objReleasedMap.get("PreventTb"));
											
											return new ResponseEntity<>(returnMap,  HttpStatus.OK);
									}
									else {
										return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PREVENTTBSENDRESULT",
												userInfo.getSlanguagefilename())+" "+objReleasedMap.get("PreventTb"),  HttpStatus.EXPECTATION_FAILED);
										
									}
									
								}
						}
					} else {
						//End
						inputMap.putAll(objReleasedMap);
						inputMap.remove(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());
						return new ResponseEntity<>(inputMap, HttpStatus.OK);
					}

				} else {
					if (objReleasedMap.containsKey("ProjectTypeFlow")
							&& (int) objReleasedMap.get("ProjectTypeFlow") == Enumeration.TransactionStatus.YES
									.gettransactionstatus()) {
						returnMap.put("rtn", commonFunction.getMultilingualMessage(
								"IDS_CHECKCONFIGURATIONMAPPEDREPORTTEMPLATE", userInfo.getSlanguagefilename()));
					} else {
						returnMap.put("rtn", commonFunction.getMultilingualMessage("IDS_CONFIGUREREPORT",
								userInfo.getSlanguagefilename()));
					}
					return new ResponseEntity<>(returnMap, HttpStatus.OK);
				}

			} else {
				inputMap.putAll(objmap);
				return new ResponseEntity<>(objmap, HttpStatus.OK);
			}
		}
	}

	@Transactional
	@Override
	public ResponseEntity<Map<String, Object>> getReleaseSubSample(Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		return releaseDAO.getReleaseSubSample(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Map<String, Object>> getReleaseHistory(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		return releaseDAO.getReleaseHistory(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getCOAReportType(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		return releaseDAO.getCOAReportType(inputMap, userInfo);
	}

	/**
	 * This method definition is used to fetch a file/ link which need to view
	 * 
	 * @param objTestFile [TestFile] object holds the details of test file
	 * @param objUserInfo [UserInfo] object holds the loggedin user info
	 * @return response entity of 'testfile' entity
	 */
	@Transactional
	@Override
	public Map<String, Object> viewAttachedCOAHistoryFile(COAHistory objCOAHistory, final UserInfo objUserInfo)
			throws Exception {
		return releaseDAO.viewAttachedCOAHistoryFile(objCOAHistory, objUserInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Map<String, Object>> preliminaryRegenerateReport(Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		final Map<String, Object> objMap = new HashMap<>();
		final Map<String, Object> sNodeServerStart = releaseDAO.validationCheckForNodeServer(inputMap, userInfo);

		if (sNodeServerStart.get("rtn").equals("Failed")) {

			objMap.put("rtn",
					commonFunction.getMultilingualMessage("IDS_STARTNODESERVER", userInfo.getSlanguagefilename()));
			return new ResponseEntity<>(objMap, HttpStatus.EXPECTATION_FAILED);
		} else {
			final Map<String, Object> returnValue = (Map<String, Object>) releaseDAO
					.preliminaryRegenerateReport(inputMap, userInfo).getBody();

			if (returnValue.containsKey("rtn")
					&& (Enumeration.ReturnStatus.SUCCESS.getreturnstatus()).equals(returnValue.get("rtn"))) {

				inputMap.putAll(returnValue);
				return releaseDAO.reportGeneration(inputMap, userInfo);

			} else {
				objMap.put("rtn", returnValue.get("rtn"));
				return new ResponseEntity<Map<String, Object>>(objMap, HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	@Override
	public ResponseEntity<Object> getApprovedProjectType(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		return releaseDAO.getApprovedProjectType(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getApprovedProjectByProjectType(Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		return releaseDAO.getApprovedProjectByProjectType(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getReleaseConfigVersionRegTemplateDesign(Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return releaseDAO.getReleaseConfigVersionRegTemplateDesign(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> SendToPortalReport(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return releaseDAO.SendToPortalReport(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> saveAsDraft(Map<String, Object> inputMap, final UserInfo userInfo) throws Exception {
		return releaseDAO.saveAsDraft(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> DeleteApprovedSamples(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return releaseDAO.DeleteApprovedSamples(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> UpdateApprovedSamples(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return releaseDAO.UpdateApprovedSamples(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getStatusAlert(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return releaseDAO.getStatusAlert(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getSection(Map<String, Object> inputMap, UserInfo objUserInfo)
			throws Exception {

		return releaseDAO.getSection(inputMap, objUserInfo);
	}

	@Override
	public ResponseEntity<Object> getreportcomments(Map<String, Object> inputMap, final UserInfo objUserInfo)
			throws Exception {

		return releaseDAO.getreportcomments(inputMap, objUserInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveUnitById(final int nunitCode, final UserInfo userInfo) throws Exception {

		final ReportInfoRelease unit = releaseDAO.getActiveUnitById(nunitCode, userInfo);
		if (unit == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(unit, HttpStatus.OK);
		}
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateReportComment(final ReportInfoRelease selectedComment, final UserInfo userInfo)
			throws Exception {

		return releaseDAO.updateReportComment(selectedComment, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateReleaseParameter(MultipartHttpServletRequest request, final UserInfo userInfo)
			throws Exception {
		return releaseDAO.updateReleaseParameter(request, userInfo);
	}

	@Override
	public ResponseEntity<Object> getResultCorrection(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return releaseDAO.getResultCorrection(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getReleaseResults(final int ntransactionresultcode, final UserInfo userInfo)
			throws Exception {
		return releaseDAO.getReleaseResults(ntransactionresultcode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateCorrectionStatus(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return releaseDAO.updateCorrectionStatus(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateReleaseAfterCorrection(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		final Map<String, Object> sNodeServerStart = releaseDAO.validationCheckForNodeServer(inputMap, userInfo);

		if (sNodeServerStart.get("rtn").equals("Failed")) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_STARTNODESERVER", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final Map<String, Object> objReleasedMap = releaseDAO.updateReleaseAfterCorrection(inputMap, userInfo);
			
			if (objReleasedMap.containsKey("isSameCOAParentTransactionStatus")
					&& objReleasedMap.get("isSameCOAParentTransactionStatus").equals(false)) {

				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SELECTRECORDSWITHSAMESTATUS",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} 
			else {
				
				final List<COAParent> coaParent = (List<COAParent>) objReleasedMap.get("selectedReleaseHistory");
				
				if (objReleasedMap.containsKey("ProjectTypeFlow") && objReleasedMap.containsKey("ReportAvailable")
						&& (int) objReleasedMap.get("ProjectTypeFlow") == Enumeration.TransactionStatus.YES
								.gettransactionstatus()
						&& (Enumeration.ReturnStatus.FAILED.getreturnstatus())
								.equals(objReleasedMap.get("ReportAvailable"))) {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_CHECKCONFIGURATIONMAPPEDREPORTTEMPLATE",
									userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				} 
				else if(coaParent != null && coaParent.size() == 1) {
					//Report will be generated soon after release if the selected record
					// count is 1
//					if(!objReleasedMap.containsKey("PreventTb") ) {
				
							if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
									.equals(objReleasedMap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
								
								inputMap.putAll(objReleasedMap);

								return releaseDAO.releasedReportGeneration(inputMap,userInfo);								
							}
							else {
								return new ResponseEntity<>(objReleasedMap, HttpStatus.OK);
							}
//					}
//					else {
//				
//						if((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()
//								.equals(objReleasedMap.get("PreventTb")))) {
//						   
//							inputMap.putAll(objReleasedMap);
//				
//						   inputMap.put("PreventTb",commonFunction.getMultilingualMessage("IDS_PREVENTTBSENDRESULT",
//											userInfo.getSlanguagefilename())+" "+objReleasedMap.get("PreventTb"));
//						   
//						   return releaseDAO.releasedReportGeneration(inputMap,userInfo);
//						}
//						else {
//							if(objReleasedMap.get("PreventTb").equals("MappingNeeded")) {		
//							
//								final Map<String,Object> returnMap=new HashMap<String, Object>();
//								
//								returnMap.put("rtn",objReleasedMap.get("PreventTb"));
//								
//								return new ResponseEntity<>(returnMap,  HttpStatus.OK);
//							}
//							else {
//								return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PREVENTTBSENDRESULT",
//										userInfo.getSlanguagefilename())+" "+objReleasedMap.get("PreventTb"),  HttpStatus.EXPECTATION_FAILED);
//								
//							}	
//							
//						}					
//					}	
				}				
				else {

					inputMap.putAll(objReleasedMap);
					inputMap.remove(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus());

					return new ResponseEntity<>(inputMap, HttpStatus.OK);
				}
			}
		}
	}

	@Transactional
	@Override
	public ResponseEntity<Object> viewReportHistory(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return releaseDAO.viewReportHistory(inputMap, userInfo);
	}

	@Transactional
	@Override
	public Map<String, Object> viewReleasedCOAReport(ReleaseOutsourceAttachment objReleaseCOAReport,
			final int ncontrolCode, final UserInfo userInfo) throws Exception {
		return releaseDAO.viewReleasedCOAReport(objReleaseCOAReport, ncontrolCode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getReleaseTestAttachment(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return releaseDAO.getReleaseTestAttachment(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveReleaseTestAttachmentById(final int nreleaseTestAttachmentCode,
			final UserInfo userInfo) throws Exception {

		final ReleaseTestAttachment releaseTestAttachment = releaseDAO
				.getActiveReleaseTestAttachmentById(nreleaseTestAttachmentCode, userInfo);
		if (releaseTestAttachment == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(releaseTestAttachment, HttpStatus.OK);
		}
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createReleaseTestAttachment(MultipartHttpServletRequest request,
			final UserInfo userInfo) throws Exception {
		return releaseDAO.createReleaseTestAttachment(request, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateReleaseTestAttachment(MultipartHttpServletRequest request,
			final UserInfo userInfo) throws Exception {
		return releaseDAO.updateReleaseTestAttachment(request, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteReleaseTestAttachment(final ReleaseTestAttachment objReleaseTestAttachment,
			final UserInfo userInfo) throws Exception {
		return releaseDAO.deleteReleaseTestAttachment(objReleaseTestAttachment, userInfo);
	}

	@Transactional
	@Override
	public Map<String, Object> viewReleaseTestAttachment(final Map<String, Object> objReleaseTestAttachmentFile,
			final UserInfo userInfo, int ncontrolcode) throws Exception {
		return releaseDAO.viewReleaseTestAttachment(objReleaseTestAttachmentFile, userInfo, ncontrolcode);
	}

	@Override
	public ResponseEntity<Object> getVersionHistory(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return releaseDAO.getVersionHistory(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Map<String, Object>> downloadVersionHistory(Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return releaseDAO.downloadVersionHistory(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> downloadHistory(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return releaseDAO.downloadHistory(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> getPatientWiseSample(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return releaseDAO.getPatientWiseSample(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getReleaseTestComment(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return releaseDAO.getReleaseTestComment(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveReleaseTestCommentById(final int nreleaseTestCommentCode,
			final UserInfo userInfo) throws Exception {

		final ReleaseTestComment releaseTestComment = releaseDAO
				.getActiveReleaseTestCommentById(nreleaseTestCommentCode, userInfo);
		if (releaseTestComment == null) {
			// status code:417
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(releaseTestComment, HttpStatus.OK);
		}
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createReleaseTestComment(MultipartHttpServletRequest request, final UserInfo userInfo)
			throws Exception {
		return releaseDAO.createReleaseTestComment(request, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateReleaseTestComment(MultipartHttpServletRequest request, final UserInfo userInfo)
			throws Exception {
		return releaseDAO.updateReleaseTestComment(request, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteReleaseTestComment(final ReleaseTestComment objReleaseTestComment,
			final UserInfo userInfo) throws Exception {
		return releaseDAO.deleteReleaseTestComment(objReleaseTestComment, userInfo);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getPreliminaryReportHistory(Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return releaseDAO.getPreliminaryReportHistory(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getComboValues(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return releaseDAO.getComboValues(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> generateReport(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return releaseDAO.generateReport(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getTest(Map<String, Object> inputMap, final UserInfo objUserInfo) throws Exception {

		return releaseDAO.getTest(inputMap, objUserInfo);
	}

	@Override
	public ResponseEntity<List<Map<String, Object>>> getApprovedReportTemplate(Map<String, Object> inputMap,
			final UserInfo objUserInfo) throws Exception {

		return releaseDAO.getApprovedReportTemplate(inputMap, objUserInfo);
	}

	@Override
	public ResponseEntity<Object> getApprovedReportTemplateById(Map<String, Object> inputMap, UserInfo objUserInfo)
			throws Exception {

		return releaseDAO.getApprovedReportTemplateById(inputMap, objUserInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateReportTemplate(Map<String, Object> inputMap, final UserInfo objUserInfo)
			throws Exception {

		return releaseDAO.updateReportTemplate(inputMap, objUserInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteSamples(Map<String, Object> inputMap, final UserInfo objUserInfo)
			throws Exception {

		return releaseDAO.deleteSamples(inputMap, objUserInfo);
	}

	// Added by sonia on 11-06-2024 for JIRA ID:4122 Sample Count Validation
	@Override
	public ResponseEntity<Object> sampleCountValidation(Map<String, Object> inputMap, final UserInfo objUserInfo)
			throws Exception {

		return releaseDAO.sampleCountValidation(inputMap, objUserInfo);
	}

	// Added by Dhanushya RI for JIRA ID:ALPD-4878 Filter save detail --Start
	@Transactional
	@Override
	public ResponseEntity<Object> createFilterName(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return releaseDAO.createFilterName(inputMap, userInfo);
	}

	@Override
	public List<FilterName> getFilterName(final UserInfo userInfo) throws Exception {
		return releaseDAO.getFilterName(userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> getReleaseFilter(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return releaseDAO.getReleaseFilter(inputMap, userInfo);
	}

	// End
	// ALPD-5189 added by Dhanushya RI,To insert comments into releasecomment table
	@Transactional
	@Override
	public ResponseEntity<Object> createReleaseComment(Map<String, Object> inputMap, final UserInfo objUserInfo)
			throws Exception {

		return releaseDAO.createReleaseComment(inputMap, objUserInfo);
	}

	// ALPD-5189 added by Dhanushya RI,To get comment details for each release
	// number
	@Override
	public ReleaseComment getReleaseCommentDetails(Map<String, Object> inputMap, final UserInfo objUserInfo)
			throws Exception {

		return releaseDAO.getReleaseCommentDetails(inputMap, objUserInfo);
	}
}
