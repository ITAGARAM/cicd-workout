package com.agaramtech.qualis.credential.service.controlmaster;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.credential.model.ControlMaster;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'unit' table through
 * its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class ControlMasterServiceImpl implements ControlMasterService {

	private final ControlMasterDAO controlMasterDAO;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class properties.
	 * 
	 * @param unitDAO        UnitDAO Interface
	 * @param commonFunction CommonFunction holding common utility functions
	 */
	public ControlMasterServiceImpl(ControlMasterDAO controlMasterDAO) {
		this.controlMasterDAO = controlMasterDAO;
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available controlmaster data with respect to site and nformcode
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched and nformcode [int] primary key of qualisforms object
	 *                 for which the list is to be fetched
	 * @return a response entity which holds the list of controlmaster records with
	 *         respect to site and nformcode
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<List<ControlMaster>> getUploadControlsByFormCode(final UserInfo userInfo) throws Exception {
		return controlMasterDAO.getUploadControlsByFormCode(userInfo);
	}
}
