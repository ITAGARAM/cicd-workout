package com.agaramtech.qualis.quotation.service.vatband;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.quotation.model.VATBand;
/**
 * This interface provides declarations for CRUD operations on the 'VATBand'
 * service layer. It defines methods for retrieving, creating, updating, and
 * deleting VAT band records.
 */
public interface VATBandService {

	/**
	 * This method is used to retrieve all VAT band records associated with the site
	 * of the logged-in user.
	 * 
	 * @param userInfo [UserInfo] containing the details of the logged-in user,
	 *                 including their site code.
	 * @return [ResponseEntity<Object>] containing a list of VAT band records
	 *         associated with the site.
	 * @throws Exception if any error occurs while fetching the data from the
	 *                   service layer.
	 */
	public ResponseEntity<Object> getVATBand(final UserInfo userInfo) throws Exception;

	/**
	 * This method is used to create a new VAT band entry in the database.
	 * 
	 * @param vatBand  [VATBand] object containing the details to be added to the
	 *                 'VATBand' table.
	 * @param userInfo [UserInfo] containing the details of the logged-in user,
	 *                 including their site code.
	 * @return [ResponseEntity<Object>] containing the status and details of the
	 *         created VAT band.
	 * @throws Exception if any error occurs while inserting the data into the
	 *                   database.
	 */
	public ResponseEntity<Object> createVATBand(final VATBand vatBand, final UserInfo userInfo) throws Exception;

	/**
	 * This method is used to retrieve an active VAT band record based on the
	 * specified VAT band code.
	 * 
	 * @param nvatbandcode [int] representing the primary key of the VAT band to be
	 *                     fetched.
	 * @param userInfo     [UserInfo] containing details of the logged-in user.
	 * @return [ResponseEntity<Object>] containing the active VAT band record.
	 * @throws Exception if any error occurs while fetching the data from the
	 *                   database.
	 */
	public ResponseEntity<Object> getActiveVATBandById(final int nvatbandcode, final UserInfo userInfo)
			throws Exception;

	/**
	 * This method is used to update an existing VAT band record in the database.
	 * 
	 * @param vatBand  [VATBand] object containing the updated details of the VAT
	 *                 band.
	 * @param userInfo [UserInfo] containing details of the logged-in user.
	 * @return [ResponseEntity<Object>] containing the status and updated details of
	 *         the VAT band.
	 * @throws Exception if any error occurs while updating the data in the
	 *                   database.
	 */
	public ResponseEntity<Object> updateVATBand(final VATBand vatBand, final UserInfo userInfo) throws Exception;

	/**
	 * This method is used to delete a VAT band record from the database.
	 * 
	 * @param vatBand  [VATBand] object containing the details of the VAT band to be
	 *                 deleted.
	 * @param userInfo [UserInfo] containing details of the logged-in user.
	 * @return [ResponseEntity<Object>] containing the status and details of the
	 *         deleted VAT band.
	 * @throws Exception if any error occurs while deleting the data from the
	 *                   database.
	 */
	public ResponseEntity<Object> deleteVATBand(final VATBand vatBand, final UserInfo userInfo) throws Exception;
}
