package com.agaramtech.qualis.barcode.service.sampledonor;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.barcode.model.SampleDonor;
import com.agaramtech.qualis.global.UserInfo;
import lombok.RequiredArgsConstructor;

/**
 * This class holds methods to perform CRUD operation on 'sampledonor' table
 * through its DAO layer.
 * 
 * @author ATE236
 * @version 10.0.0.2
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class SampleDonorServiceImpl implements SampleDonorService {

	private final SampleDonorDAO sampleDonorDAO;

	/**
	 * This service implementation method will access the DAO layer that is used 
	 * to get all the available sampledonor's with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of sampledonor records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSampleDonor(final UserInfo userInfo) throws Exception {
		return sampleDonorDAO.getSampleDonor(userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to retrieve active sampledonor object based
	 * on the specified nsampledonorcode.
	 * @param nsampledonorcode [int] primary key of sampledonor object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of sampledonor object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveSampleDonorById(final int nsampledonorcode, UserInfo userInfo)
			throws Exception {
		return sampleDonorDAO.getActiveSampleDonorById(nsampledonorcode, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to sampledonor table.
	 * @param objSampleDonor [SampleDonor] object holding details to be added in sampledonor table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of added sampledonor object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createSampleDonor(final SampleDonor objSampleDonor, UserInfo userInfo)
			throws Exception {
		return sampleDonorDAO.createSampleDonor(objSampleDonor, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 *  update entry in sampledonor table.
	 * @param objSampleDonor [SampleDonor] object holding details to be updated in sampledonor table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated sampledonor object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateSampleDonor(final SampleDonor objSampleDonor, UserInfo userInfo)
			throws Exception {
		return sampleDonorDAO.updateSampleDonor(objSampleDonor, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to delete an entry in sampledonor table.
	 * @param objSampleDonor [SampleDonor] object holding detail to be deleted from sampledonor table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted sampledonor object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteSampleDonor(final SampleDonor objSampleDonor, UserInfo userInfo)
			throws Exception {
		return sampleDonorDAO.deleteSampleDonor(objSampleDonor, userInfo);
	}

}
