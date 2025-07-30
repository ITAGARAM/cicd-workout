package com.agaramtech.qualis.credential.service.limselnusers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.credential.model.LimsElnUserMapping;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

import lombok.RequiredArgsConstructor;

/**
 * This class implements the service layer for CRUD operations on the
 * 'LimsElnUserMapping' table and interacts with the DAO layer for persistence
 * management.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class LimsElnUserMappingServiceImpl implements LimsElnUserMappingService {
	private final LimsElnUserMappingDAO LimsElnUserMappingDAO;
	private final CommonFunction commonFunction;

	/**
	 * This service implementation method will access the DAO layer to get the LIMS
	 * ELN user mappings for the logged-in user.
	 * 
	 * @param userInfo [UserInfo] holding details of the logged-in user.
	 * @return ResponseEntity containing the list of LIMS ELN user mappings.
	 * @throws Exception if an error occurs during the fetching process from the DAO
	 *                   layer.
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> getlimselnusermapping(final UserInfo userInfo) throws Exception {
		return LimsElnUserMappingDAO.getlimselnusermapping(userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer to get the list
	 * of LIMS users.
	 * 
	 * @param userInfo [UserInfo] holding details of the logged-in user.
	 * @return ResponseEntity containing the list of available LIMS users.
	 * @throws Exception if an error occurs during the fetching process from the DAO
	 *                   layer.
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> getLimsUsers(final UserInfo userInfo) throws Exception {
		return LimsElnUserMappingDAO.getLimsUsers(userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer to create a new
	 * LIMS ELN user mapping.
	 * 
	 * @param limselnuser [LimsElnUserMapping] object containing the details of the
	 *                    new user mapping to be created.
	 * @param userInfo    [UserInfo] holding details of the logged-in user.
	 * @return ResponseEntity containing the status and data of the newly created
	 *         user mapping.
	 * @throws Exception if an error occurs during the creation process in the DAO
	 *                   layer.
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createLimsElnUsermapping(final LimsElnUserMapping limselnuser,
			final UserInfo userInfo) throws Exception {
		return LimsElnUserMappingDAO.createLimsElnUsermapping(limselnuser, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer to delete an
	 * existing LIMS ELN user mapping.
	 * 
	 * @param limselnuser [LimsElnUserMapping] object containing the details of the
	 *                    user mapping to be deleted.
	 * @param userInfo    [UserInfo] holding details of the logged-in user.
	 * @return ResponseEntity containing the status and data of the deleted user
	 *         mapping.
	 * @throws Exception if an error occurs during the deletion process in the DAO
	 *                   layer.
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteLimsElnUsermapping(final LimsElnUserMapping limselnuser,
			final UserInfo userInfo) throws Exception {
		return LimsElnUserMappingDAO.deleteLimsElnUsermapping(limselnuser, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer to retrieve an
	 * active LIMS ELN user mapping based on the specified mapping code.
	 * 
	 * @param nelnusermappingcode [int] the unique identifier of the LIMS ELN user
	 *                            mapping.
	 * @param userInfo            [UserInfo] holding details of the logged-in user.
	 * @return ResponseEntity containing the status and data of the active user
	 *         mapping.
	 * @throws Exception if an error occurs during the fetching process from the DAO
	 *                   layer.
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> getActiveLimsElnUsermappingById(final int nelnusermappingcode,
			final UserInfo userInfo) throws Exception {
		final LimsElnUserMapping reason = (LimsElnUserMapping) LimsElnUserMappingDAO
				.getActiveLimsElnUsermappingById(nelnusermappingcode);
		if (reason == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(reason, HttpStatus.OK);
		}
	}

	/**
	 * This service implementation method will access the DAO layer to update an
	 * existing LIMS ELN user mapping.
	 * 
	 * @param limselnuser [LimsElnUserMapping] object containing the updated details
	 *                    of the user mapping.
	 * @param userInfo    [UserInfo] holding details of the logged-in user.
	 * @return ResponseEntity containing the status and updated data of the user
	 *         mapping.
	 * @throws Exception if an error occurs during the update process in the DAO
	 *                   layer.
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateLimsElnUsermapping(final LimsElnUserMapping limselnuser,
			final UserInfo userInfo) throws Exception {
		return LimsElnUserMappingDAO.updateLimsElnUsermapping(limselnuser, userInfo);
	}

}
