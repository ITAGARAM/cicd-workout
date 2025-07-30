package com.agaramtech.qualis.product.service.sampleappearance;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.product.model.SampleAppearance;

/**
 * This class is used to perform CRUD Operation on "Sample Appearance" table by
 * implementing methods from its interface.
 * @jira ALPD-3583
 */

public interface SampleAppearanceService {
	/**
	 * This interface declaration is used to retrieve list of all active Product
	 * Category for the specified site through its DAO layer
	 * 
	 * @param inputMap [Map] map object with "userinfo" as key for which the list is
	 *                 to be fetched
	 * @return response object with list of active Product Category that are to be
	 *         listed for the specified site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getSampleAppearance(final UserInfo userinfo) throws Exception;

	public ResponseEntity<Object> createSampleAppearance(Map<String, Object> inputMap,
			final SampleAppearance sampleAppearance, final UserInfo userinfo) throws Exception;

	public ResponseEntity<Object> getActiveSampleAppearanceById(final int nsampleappearancecode,
			final UserInfo userinfo) throws Exception;

	public ResponseEntity<Object> updateSampleAppearance(final SampleAppearance sampleAppearance,
			final UserInfo userinfo) throws Exception;

	public ResponseEntity<Object> deleteSampleAppearance(final SampleAppearance objsample, final UserInfo userInfo)
			throws Exception;
}
