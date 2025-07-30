package com.agaramtech.qualis.quotation.service.vatband;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.quotation.model.VATBand;

/**
 * This interface provides declarations for CRUD operations on the 'VATBand'
 * table. It defines methods for retrieving, creating, updating, and deleting
 * VAT band records.
 */
public interface VATBandDAO {

	/**
	 * This method is used to retrieve all VAT bands associated with the logged-in
	 * user's site.
	 * 
	 * @param userInfo [UserInfo] containing details of the logged-in user,
	 *                 including their site code.
	 * @return [ResponseEntity<Object>] containing a list of VAT band records
	 *         associated with the site.
	 * @throws Exception if any error occurs while fetching the data from the
	 *                   database.
	 */
	public ResponseEntity<Object> getVATBand(final UserInfo userInfo) throws Exception;

	/**
	 * This method is used to create a new VAT band entry in the database.
	 * 
	 * @param vatband  [VATBand] object containing the details to be added to the
	 *                 'VATBand' table.
	 * @param userInfo [UserInfo] containing details of the logged-in user,
	 *                 including their site code.
	 * @return [ResponseEntity<Object>] containing the status and details of the
	 *         created VAT band.
	 * @throws Exception if any error occurs while inserting the data into the
	 *                   database.
	 */
	public ResponseEntity<Object> createVATBand(final VATBand vatband, final UserInfo userInfo) throws Exception;

	/**
	 * This method retrieves VAT band details based on the master site code.
	 * 
	 * @param nmasterSiteCode [int] representing the primary key of the site for
	 *                        which the VAT band is fetched.
	 * @return [ResponseEntity<Object>] containing the VAT band record associated
	 *         with the specified master site.
	 * @throws Exception if any error occurs while fetching the data from the
	 *                   database.
	 */
	public ResponseEntity<Object> getVATBand(final int nmasterSiteCode) throws Exception;

	/**
	 * This method retrieves the active VAT band record based on the specified
	 * discount band code.
	 * 
	 * @param ndiscountbandcode [int] representing the primary key of the VAT band
	 *                          to be fetched.
	 * @param userInfo          [UserInfo] containing details of the logged-in user.
	 * @return [ResponseEntity<Object>] containing the details of the active VAT
	 *         band.
	 * @throws Exception if any error occurs while fetching the data from the
	 *                   database.
	 */
	public ResponseEntity<Object> getActiveVATBandById(final int ndiscountbandcode, final UserInfo userInfo)
			throws Exception;

	/**
	 * This method is used to update an existing VAT band record in the database.
	 * 
	 * @param vatband  [VATBand] object containing the updated details of the VAT
	 *                 band.
	 * @param userInfo [UserInfo] containing details of the logged-in user.
	 * @return [ResponseEntity<Object>] containing the status and updated details of
	 *         the VAT band.
	 * @throws Exception if any error occurs while updating the data in the
	 *                   database.
	 */
	public ResponseEntity<Object> updateVATBand(final VATBand vatband, final UserInfo userInfo) throws Exception;

	/**
	 * This method is used to delete a VAT band record from the database.
	 * 
	 * @param vatband  [VATBand] object containing the details of the VAT band to be
	 *                 deleted.
	 * @param userInfo [UserInfo] containing details of the logged-in user.
	 * @return [ResponseEntity<Object>] containing the status and details of the
	 *         deleted VAT band.
	 * @throws Exception if any error occurs while deleting the data from the
	 *                   database.
	 */
	public ResponseEntity<Object> deleteVATBand(final VATBand vatband, final UserInfo userInfo) throws Exception;
}
