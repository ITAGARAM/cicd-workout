package com.agaramtech.qualis.product.service.sampleappearance;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.product.model.SampleAppearance;

import lombok.RequiredArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "Sample Appearance" table by
 * implementing methods from its interface.
 * 
 * @author ATE235
 * @version 10.0.0.1
 * @since 3 - January -2024
 * @jira ALPD-3583
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class SampleAppearanceServiceImpl implements SampleAppearanceService {

	private final SampleAppearanceDAO sampleAppearanceDAO;
	private final CommonFunction commonFunction;

	/**
	 * This interface declaration is used to retrieve list of all active Sample
	 * Appearance for the specified site through its DAO layer
	 * 
	 * @param inputMap [Map] map object with "userinfo" as key for which the list is
	 *                 to be fetched
	 * @return response object with list of active Sample Appearance that are to be
	 *         listed for the specified site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getSampleAppearance(final UserInfo userInfo) throws Exception {
		return sampleAppearanceDAO.getSampleAppearance(userInfo);
	}

	@Transactional
	public ResponseEntity<Object> createSampleAppearance(Map<String, Object> inputMap,
			final SampleAppearance sampleAppearance, final UserInfo UserInfo) throws Exception {
		return sampleAppearanceDAO.createSampleAppearance(inputMap, sampleAppearance, UserInfo);
	}

	public ResponseEntity<Object> getActiveSampleAppearanceById(final int nsampleappearancecode,
			final UserInfo userinfo) throws Exception {
		final SampleAppearance sampleAppearance = sampleAppearanceDAO
				.getActiveSampleAppearanceById(nsampleappearancecode, userinfo);
		if (sampleAppearance == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userinfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(sampleAppearance, HttpStatus.OK);
		}
	}

	@Transactional
	public ResponseEntity<Object> updateSampleAppearance(final SampleAppearance sampleAppearance,
			final UserInfo userinfo) throws Exception {
		return sampleAppearanceDAO.updateSampleAppearance(sampleAppearance, userinfo);
	}

	@Transactional
	public ResponseEntity<Object> deleteSampleAppearance(final SampleAppearance objsample, final UserInfo userInfo)
			throws Exception {
		return sampleAppearanceDAO.deleteSampleAppearance(objsample, userInfo);
	}

}
