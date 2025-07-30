package com.agaramtech.qualis.joballocation.service.JobAllocation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.configuration.model.FilterName;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class JobAllocationServiceImpl implements JobAllocationService {

	private final JobAllocationDAO jobAllocationDAO;

	public JobAllocationServiceImpl(JobAllocationDAO jobAllocationDAO) {
		this.jobAllocationDAO = jobAllocationDAO;
	}

	@Transactional
	@Override
	public ResponseEntity<Object> getJobAllocation(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return jobAllocationDAO.getJobAllocation(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Map<String,Object>> getSampleType(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return jobAllocationDAO.getSampleType(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Map<String,Object>> getRegistrationType(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return jobAllocationDAO.getRegistrationType(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Map<String, Object>> getRegistrationsubType(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return jobAllocationDAO.getRegistrationsubType(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getApprovalConfigVersion(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return jobAllocationDAO.getApprovalConfigVersion(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getFilterStatus(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return jobAllocationDAO.getFilterStatus(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getSection(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return jobAllocationDAO.getSection(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getTestCombo(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return jobAllocationDAO.getTestCombo(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getRegistrationTypeBySampleType(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return jobAllocationDAO.getRegistrationTypeBySampleType(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Map<String,Object>> getRegistrationsubTypeByRegType(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return jobAllocationDAO.getRegistrationsubTypeByRegType(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getApprovalConfigVersionByRegSubType(Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return jobAllocationDAO.getApprovalConfigVersionByRegSubType(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getFilterStatusByApproveVersion(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return jobAllocationDAO.getFilterStatusByApproveVersion(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getSectionByApproveVersion(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return jobAllocationDAO.getSectionByApproveVersion(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getTestComboBySection(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return jobAllocationDAO.getTestComboBySection(inputMap, userInfo);
	}

	public ResponseEntity<Object> getDesignTemplateByApprovalConfigVersion(Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return jobAllocationDAO.getDesignTemplateByApprovalConfigVersion(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> getJobAllocationDetails(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return jobAllocationDAO.getJobAllocationDetails(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> getJobAllocationSubSampleDetails(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return jobAllocationDAO.getJobAllocationSubSampleDetails(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> getJobAllocationTestDetails(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return jobAllocationDAO.getJobAllocationTestDetails(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> getTestView(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return new ResponseEntity<>(jobAllocationDAO.getTestView(inputMap, userInfo), HttpStatus.OK);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> CreateReceiveinLab(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Map<String, Object> objmap = jobAllocationDAO.insertSeqNoRegistration(inputMap, userInfo);
		if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()
				.equals(objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus())))) {
			inputMap.putAll(objmap);
			return new ResponseEntity<>(jobAllocationDAO.CreateReceiveinLab(inputMap, userInfo), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> getAllotDetails(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return jobAllocationDAO.getAllotDetails(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getAllotAnotherUserDetails(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return jobAllocationDAO.getAllotAnotherUserDetails(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getInstrumentNameBasedCategory(int instrumentCatCode, int calibrationRequired,
			UserInfo userInfo) throws Exception {
		return jobAllocationDAO.getInstrumentNameBasedCategory(instrumentCatCode, calibrationRequired, userInfo);
	}

	@Override
	public ResponseEntity<Object> getInstrumentIdBasedCategory(int instrumentCatCode, int instrumentNameCode,
			int calibrationRequired, UserInfo userInfo) throws Exception {
		return jobAllocationDAO.getInstrumentIdBasedCategory(instrumentCatCode, instrumentNameCode, calibrationRequired,
				userInfo);
	}

	@Override
	public ResponseEntity<Object> getUsersBasedTechnique(int techniqueCode, String sectionCode, int regTypeCode,
			int regSubTypeCode, UserInfo userInfo) throws Exception {
		return jobAllocationDAO.getUsersBasedTechnique(techniqueCode, sectionCode, regTypeCode, regSubTypeCode,
				userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> AllotJobCreate(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Map<String, Object> objmap1 = new HashMap<>();
		Map<String, Object> objmap = jobAllocationDAO.insertSeqNoAllotJob(inputMap, userInfo);
		if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()
				.equals(objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus())))) {
			inputMap.putAll(objmap);
			return new ResponseEntity<>(jobAllocationDAO.AllotJobCreate(inputMap, userInfo), HttpStatus.OK);
		} else {
			objmap1.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()));
			return new ResponseEntity<>(objmap1, HttpStatus.OK);
		}
	}

	@Transactional
	@Override
	public ResponseEntity<Object> AllotAnotherUserCreate(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> objmap1 = new HashMap<>();
		Map<String, Object> objmap = jobAllocationDAO.insertSeqNoAllotAnotherUser(inputMap, userInfo);
		if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()
				.equals(objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus())))) {
			inputMap.putAll(objmap);
			return new ResponseEntity<>(jobAllocationDAO.AllotAnotherUserCreate(inputMap, userInfo), HttpStatus.OK);
		} else {
			objmap1.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()));
			return new ResponseEntity<>(objmap1, HttpStatus.OK);
		}
	}

	@Transactional
	@Override
	public ResponseEntity<Object> RescheduleCreate(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Map<String, Object> objmap1 = new HashMap<>();
		Map<String, Object> objmap = jobAllocationDAO.insertSeqNoReschedule(inputMap, userInfo);
		if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()
				.equals(objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus())))) {
			inputMap.putAll(objmap);
			return new ResponseEntity<>(jobAllocationDAO.RescheduleCreate(inputMap, userInfo), HttpStatus.OK);
		} else {
			objmap1.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()));
			return new ResponseEntity<>(objmap1, HttpStatus.OK);
		}
	}

	@Transactional
	@Override
	public ResponseEntity<Object> viewAnalystCalendar(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return jobAllocationDAO.viewAnalystCalendar(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getUserScheduleCombo(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return jobAllocationDAO.getUserScheduleCombo(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> cancelTest(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Map<String, Object> objmap = jobAllocationDAO.insertSeqNoCancelRegistration(inputMap, userInfo);
		if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()
				.equals(objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus())))) {
			inputMap.putAll(objmap);
			return new ResponseEntity<>(jobAllocationDAO.cancelTest(inputMap, userInfo), HttpStatus.OK);
		} else {
			return new ResponseEntity<>(objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> getAnalystCalendarBasedOnUser(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return jobAllocationDAO.getAnalystCalendarBasedOnUser(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getInstrumentBasedCategoryForSchedule(int instrumentCatCode, int ninstrumentnamecode,
			UserInfo userInfo) throws Exception {
		return jobAllocationDAO.getInstrumentBasedCategoryForSchedule(instrumentCatCode, ninstrumentnamecode, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> AllotJobCreateForSchedule(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> objmap1 = new HashMap<>();
		Map<String, Object> objmap = jobAllocationDAO.insertSeqNoAllotJob(inputMap, userInfo);
		if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()
				.equals(objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus())))) {
			inputMap.putAll(objmap);
			return jobAllocationDAO.AllotJobCreateForSchedule(inputMap, userInfo);
		} else {
			objmap1.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()));
			return new ResponseEntity<>(objmap1, HttpStatus.OK);
		}

	}

	@Override
	public ResponseEntity<Object> getAnalystCalendarBasedOnUserWithDate(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return jobAllocationDAO.getAnalystCalendarBasedOnUserWithDate(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> AllotJobCalendarCreate(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> objmap1 = new HashMap<>();
		Map<String, Object> objmap = jobAllocationDAO.insertSeqNoAllotJob(inputMap, userInfo);
		if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus()
				.equals(objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus())))) {
			inputMap.putAll(objmap);
			return new ResponseEntity<>(jobAllocationDAO.AllotJobCalendarCreate(inputMap, userInfo), HttpStatus.OK);
		} else {
			objmap1.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
					objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()));
			return new ResponseEntity<>(objmap1, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> getRescheduleEdit(String npreregno, String ntransactionsamplecode,
			String ntransactiontestcode, UserInfo userInfo, Map<String, Object> inputMap) throws Exception {
		return jobAllocationDAO.getRescheduleEdit(npreregno, ntransactionsamplecode, ntransactiontestcode, userInfo,
				inputMap);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> calenderProperties(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return jobAllocationDAO.calenderProperties(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getInstrumentNameBasedCategoryForSchedule(int instrumentCatCode, UserInfo userInfo)
			throws Exception {
		return jobAllocationDAO.getInstrumentNameBasedCategoryForSchedule(instrumentCatCode, userInfo);
	}

	@Override
	public ResponseEntity<Object> getSectionChange(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		return jobAllocationDAO.getSectionChange(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateSectionJobAllocation(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		return jobAllocationDAO.updateSectionJobAllocation(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getUsersBySection(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return jobAllocationDAO.getUsersBySection(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> getJobAllocationFilter(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		return jobAllocationDAO.getJobAllocationFilter(inputMap, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createFilterName(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		return jobAllocationDAO.createFilterName(inputMap, userInfo);
	}

	@Override
	public List<FilterName> getFilterName(UserInfo userInfo) throws Exception {
		return jobAllocationDAO.getFilterName(userInfo);
	}

}
