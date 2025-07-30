package com.agaramtech.qualis.quotation.service.vatband;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.quotation.model.VATBand;

import lombok.RequiredArgsConstructor;

/**
 * This class provides the implementation of service methods for performing CRUD
 * operations on the VAT Band table using the DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class VATBandServiceImpl implements VATBandService {
	
	private final VATBandDAO vatbandDAO;

	/**
	 * This service method is used to retrieve all VAT Band records related to the
	 * site of the logged-in user. It interacts with the DAO layer to fetch the
	 * data.
	 * 
	 * @param userInfo [UserInfo] contains the logged-in user details and site
	 *                 information
	 * @return ResponseEntity object containing the list of VAT Band records
	 * @throws Exception any exception thrown from the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> getVATBand(final UserInfo userInfo) throws Exception {
		return vatbandDAO.getVATBand(userInfo);
	}

	/**
	 * This service method is used to create a new VAT Band record in the database.
	 * It accesses the DAO layer to perform the insert operation.
	 * 
	 * @param vatband  [VATBand] object containing the details of the VAT Band to be
	 *                 created
	 * @param userInfo [UserInfo] contains the logged-in user details and site
	 *                 context
	 * @return ResponseEntity object containing the created VAT Band and status
	 * @throws Exception any exception thrown from the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createVATBand(final VATBand vatband, final UserInfo userInfo) throws Exception {
		return vatbandDAO.createVATBand(vatband, userInfo);
	}

	/**
	 * This service method is used to fetch an active VAT Band by its ID. It calls
	 * the DAO method to get the VAT Band object if it is active.
	 * 
	 * @param nvatbandcode [int] the primary key of the VAT Band to be retrieved
	 * @param userInfo     [UserInfo] contains the logged-in user details
	 * @return ResponseEntity object containing the VAT Band details or an
	 *         appropriate status
	 * @throws Exception any exception thrown from the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> getActiveVATBandById(final int nvatbandcode, final UserInfo userInfo)
			throws Exception {
		return vatbandDAO.getActiveVATBandById(nvatbandcode, userInfo);
	}

	/**
	 * This service method is used to update an existing VAT Band record. It
	 * interacts with the DAO layer to apply changes to the VAT Band table.
	 * 
	 * @param basemasterProjectType [VATBand] object containing updated details of
	 *                              the VAT Band
	 * @param userInfo              [UserInfo] contains the logged-in user details
	 *                              and site information
	 * @return ResponseEntity object containing the updated VAT Band and status
	 * @throws Exception any exception thrown from the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateVATBand(final VATBand basemasterProjectType, final UserInfo userInfo)
			throws Exception {
		return vatbandDAO.updateVATBand(basemasterProjectType, userInfo);
	}

	/**
	 * This service method is used to delete an existing VAT Band record. It
	 * delegates the delete operation to the DAO layer.
	 * 
	 * @param basemasterProjectType [VATBand] object representing the VAT Band to be
	 *                              deleted
	 * @param userInfo              [UserInfo] contains the logged-in user details
	 *                              and site information
	 * @return ResponseEntity object containing the result of the deletion and
	 *         status
	 * @throws Exception any exception thrown from the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteVATBand(final VATBand basemasterProjectType, final UserInfo userInfo)
			throws Exception {
		return vatbandDAO.deleteVATBand(basemasterProjectType, userInfo);
	}
}
