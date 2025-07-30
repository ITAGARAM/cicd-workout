package com.agaramtech.qualis.stability.service.stabilitystudyplan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.dynamicpreregdesign.service.dynamicpreregdesign.DynamicPreRegDesignDAO;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testgroup.model.TestGroupTest;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class StabilityStudyPlanServiceImpl implements StabilityStudyPlanService {

	private final StabilityStudyPlanDAO stabilityStudyPlanDAO;
	private final DynamicPreRegDesignDAO dynamicPreRegDesignDAO;

	public StabilityStudyPlanServiceImpl(StabilityStudyPlanDAO stabilityStudyPlanDAO,
			DynamicPreRegDesignDAO dynamicPreRegDesignDAO) {
		this.stabilityStudyPlanDAO = stabilityStudyPlanDAO;
		this.dynamicPreRegDesignDAO = dynamicPreRegDesignDAO;
	}

	public ResponseEntity<Object> getStabilityStudyPlan(final UserInfo userInfo, final String currentUIDate)
			throws Exception {
		return stabilityStudyPlanDAO.getStabilityStudyPlan(userInfo, currentUIDate);
	}

	@Transactional
	public ResponseEntity<Object> insertStbStudyPlan(final Map<String, Object> inputMap) throws Exception {
		return stabilityStudyPlanDAO.createSample(inputMap);
	}

	@Transactional
	public ResponseEntity<Object> importStabilityStudyPlan(final MultipartHttpServletRequest request,
			final UserInfo userInfo) throws Exception {
		return stabilityStudyPlanDAO.importStabilityStudyPlan(request, userInfo);
	}

	@Transactional
	public ResponseEntity<Object> createTest(final UserInfo userInfo, final List<String> listSample,
			final List<TestGroupTest> listTest, final int nregtypecode, final int nregsubtypecode,
			final Map<String, Object> inputMap) throws Exception {
		Map<String, Object> objmap = stabilityStudyPlanDAO.getCreateTestSequenceNo(userInfo, listSample, listTest,
				nregtypecode, nregsubtypecode, inputMap);
		if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
				.equals(objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
			inputMap.putAll(objmap);
			return stabilityStudyPlanDAO.createTest(userInfo, listSample, listTest, nregtypecode, nregsubtypecode,
					inputMap);
		} else {
			inputMap.putAll(objmap);
			return new ResponseEntity<>(objmap, HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Transactional
	public ResponseEntity<Object> createTimePoint(final Map<String, Object> inputMap) throws Exception {
		Map<String, Object> objmap1 = new HashMap<>();
		Map<String, Object> objmap = stabilityStudyPlanDAO.validateSeqnoSubSampleNo(inputMap);
		if ((Enumeration.ReturnStatus.SUCCESS.getreturnstatus())
				.equals(objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()))) {
			inputMap.putAll(objmap);
			objmap1 = stabilityStudyPlanDAO.createTimePoint(inputMap);
			return new ResponseEntity<>(objmap1, HttpStatus.OK);

		} else {
			if (objmap.containsKey("NeedConfirmAlert") && (Boolean) objmap.get("NeedConfirmAlert") == true) {
				return new ResponseEntity<>(objmap, HttpStatus.EXPECTATION_FAILED);
			} else {
				objmap1.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(),
						objmap.get(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus()));
				return new ResponseEntity<>(objmap1, HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	public Map<String, Object> getRegistrationSubSample(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return stabilityStudyPlanDAO.getRegistrationSubSample(inputMap, userInfo);
	}

	public Map<String, Object> getRegistrationTest(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return stabilityStudyPlanDAO.getRegistrationTest(inputMap, userInfo);
	}

	public ResponseEntity<Object> getStabilityStudyPlanByFilterSubmit(final Map<String, Object> objmap)
			throws Exception {
		return stabilityStudyPlanDAO.getStabilityStudyPlanByFilterSubmit(objmap);
	}

	@Transactional
	public ResponseEntity<Object> approveStabilityStudyPlan(final Map<String, Object> inputMap) throws Exception {
		return stabilityStudyPlanDAO.approveStabilityStudyPlan(inputMap);
	}

	@Transactional
	public ResponseEntity<Object> deleteTest(final Map<String, Object> inputMap) throws Exception {
		return stabilityStudyPlanDAO.deleteTest(inputMap);
	}

	@Transactional
	public ResponseEntity<Object> deleteStbStudyPlan(final Map<String, Object> inputMap) throws Exception {
		return stabilityStudyPlanDAO.deleteStbStudyPlan(inputMap);
	}

	public Map<String,Object> getEditStbTimePointDetails(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {

		Map<String, Object> map = (Map<String, Object>) stabilityStudyPlanDAO.getEditStbTimePointDetails(inputMap, userInfo);
		map.putAll((Map<? extends String, ? extends Object>) dynamicPreRegDesignDAO.getComboValuesForEdit(map, inputMap, userInfo).getBody());
		return map;
	}

	@Transactional
	public ResponseEntity<Object> updateStbTimePoint(final Map<String, Object> inputMap) throws Exception {
		return stabilityStudyPlanDAO.updateStbTimePoint(inputMap);
	}

	@Transactional
	public ResponseEntity<Object> deleteStbTimePoint(final Map<String, Object> inputMap) throws Exception {
		return stabilityStudyPlanDAO.deleteStbTimePoint(inputMap);
	}

	public ResponseEntity<Object> getApprovalConfigBasedTemplateDesign(final Map<String, Object> inputMap,
			final UserInfo userInfo) throws Exception {
		return stabilityStudyPlanDAO.getApprovalConfigBasedTemplateDesign(inputMap, userInfo);
	}

	public ResponseEntity<Object> getRegTemplateTypeByRegSubType(final Map<String, Object> inputMap) throws Exception {
		return stabilityStudyPlanDAO.getRegTemplateTypeByRegSubType(inputMap);
	}

	public ResponseEntity<Object> getRegTypeBySampleType(final Map<String, Object> inputMap) throws Exception {
		return stabilityStudyPlanDAO.getRegTypeBySampleType(inputMap);
	}

	public ResponseEntity<Object> getRegSubTypeByRegType(final Map<String, Object> inputMap) throws Exception {
		return stabilityStudyPlanDAO.getRegSubTypeByRegType(inputMap);
	}

	public ResponseEntity<Object> getRegistrationParameter(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return stabilityStudyPlanDAO.getRegistrationParameter(inputMap, userInfo);
	}

	public ResponseEntity<Object> getTimePointHistory(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {

		return stabilityStudyPlanDAO.getTimePointHistory(inputMap, userInfo);
	}

}
