package com.agaramtech.qualis.product.service.sampleappearance;

import java.util.Map;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.product.model.SampleAppearance;

/**
 * This interface defines CRUD operations for interacting with the "Sample
 * Appearance" table in the database. It contains methods for retrieving,
 * creating, updating, and deleting sample appearance records. The methods
 * interact with the corresponding service classes that implement this
 * interface.
 * 
 * @jira ALPD-3583
 */
public interface SampleAppearanceDAO {

	/**
	 * Retrieves a list of all sample appearances associated with the given user.
	 * 
	 * @param userInfo [UserInfo] The user details, which include authentication and
	 *                 site context, used to retrieve the relevant sample
	 *                 appearances.
	 * @return ResponseEntity containing a list of sample appearance records.
	 * @throws Exception If an error occurs during the database operation.
	 */
	public ResponseEntity<Object> getSampleAppearance(final UserInfo userInfo) throws Exception;

	/**
	 * Creates a new sample appearance entry in the database based on the provided
	 * input data.
	 * 
	 * @param inputMap         [Map] A map containing the sample appearance details
	 *                         and other relevant information.
	 * @param sampleAppearance [SampleAppearance] The SampleAppearance object
	 *                         holding the data to be added.
	 * @param UserInfo         [UserInfo] The user details that are used to manage
	 *                         permissions and site context.
	 * @return ResponseEntity indicating the result of the create operation,
	 *         including the created sample appearance data.
	 * @throws Exception If an error occurs during the creation of the sample
	 *                   appearance.
	 */
	public ResponseEntity<Object> createSampleAppearance(final Map<String, Object> inputMap,
			final SampleAppearance sampleAppearance, final UserInfo UserInfo) throws Exception;

	/**
	 * Retrieves an active sample appearance record by its unique identifier
	 * (nsampleappearancecode).
	 * 
	 * @param nsampleappearancecode [int] The unique identifier for the sample
	 *                              appearance record to retrieve.
	 * @param userinfo              [UserInfo] The user details to verify
	 *                              permissions and site-specific context.
	 * @return SampleAppearance object containing the retrieved record.
	 * @throws Exception If an error occurs during the retrieval of the sample
	 *                   appearance.
	 */
	public SampleAppearance getActiveSampleAppearanceById(final int nsampleappearancecode, final UserInfo userinfo)
			throws Exception;

	/**
	 * Updates an existing sample appearance record in the database.
	 * 
	 * @param sampleAppearance [SampleAppearance] The SampleAppearance object
	 *                         containing updated details.
	 * @param UserInfo         [UserInfo] The user details that are used to verify
	 *                         permissions and manage updates.
	 * @return ResponseEntity indicating the result of the update operation,
	 *         including the updated sample appearance data.
	 * @throws Exception If an error occurs during the update of the sample
	 *                   appearance.
	 */
	public ResponseEntity<Object> updateSampleAppearance(final SampleAppearance sampleAppearance,
			final UserInfo UserInfo) throws Exception;

	/**
	 * Deletes an existing sample appearance record from the database.
	 * 
	 * @param objsample [SampleAppearance] The sample appearance object to be
	 *                  deleted.
	 * @param userInfo  [UserInfo] The user details that are used to verify
	 *                  permissions and delete the record.
	 * @return ResponseEntity indicating the result of the delete operation.
	 * @throws Exception If an error occurs during the deletion of the sample
	 *                   appearance.
	 */
	public ResponseEntity<Object> deleteSampleAppearance(final SampleAppearance objsample, final UserInfo userInfo)
			throws Exception;
}
