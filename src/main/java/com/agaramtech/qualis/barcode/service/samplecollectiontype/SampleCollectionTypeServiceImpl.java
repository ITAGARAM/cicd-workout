package com.agaramtech.qualis.barcode.service.samplecollectiontype;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.barcode.model.SampleCollectionType;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'samplecollectiontype'
 * table through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class SampleCollectionTypeServiceImpl implements SampleCollectionTypeService {

	private final SampleCollectionTypeDAO sampleCollectionTypeDAO;

	private final CommonFunction commonFunction;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class properties.
	 * @param samplecollectionTypeDAO SampleCollectionTypeDAO Interface
	 * @param commonFunction CommonFunction holding common utility functions
	 */
	public SampleCollectionTypeServiceImpl(SampleCollectionTypeDAO sampleCollectionTypeDAO,
			CommonFunction commonFunction) {
		super();
		this.sampleCollectionTypeDAO = sampleCollectionTypeDAO;
		this.commonFunction = commonFunction;
	}

	/**
	 * This method is used to retrieve list of all active samplecollectiontype for
	 * the specified site.
	 * 
	 * @param userInfo [UserInfo] primary key of site object for which the list is
	 *                 to be fetched
	 * @return response entity object holding response status and list of all active
	 *         samplecollectiontype
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getSampleCollectionType(final UserInfo userInfo) throws Exception {
		return sampleCollectionTypeDAO.getSampleCollectionType(userInfo);
	}

	/**
	 * This method is used to retrieve active samplecollectiontype object based on
	 * the specified nsamplecollectiontypecode.
	 * 
	 * @param nsamplecollectiontypecode [int] primary key of samplecollectiontype
	 *                                  object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of
	 *         samplecollectiontype object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Override
	public ResponseEntity<Object> getActiveSampleCollectionTypeById(final int nsamplecollectiontypecode,
			final UserInfo userInfo) throws Exception {
		final SampleCollectionType objSampleCollectionType = sampleCollectionTypeDAO
				.getActiveSampleCollectionTypeById(nsamplecollectiontypecode, userInfo);
		if (objSampleCollectionType == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objSampleCollectionType, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to add a new entry to samplecollectiontype table.
	 * 
	 * @param objSampleCollectionType [SampleCollectionType] object holding details
	 *                                to be added in samplecollectiontype table
	 * @param userInfo [UserInfo]
	 * @return inserted samplecollectiontype object with HTTP Status;
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createSampleCollectionType(SampleCollectionType objSampleCollectionType,
			UserInfo userInfo) throws Exception {
		return sampleCollectionTypeDAO.createSampleCollectionType(objSampleCollectionType, userInfo);
	}

	/**
	 * This method is used to update entry in samplecollectiontype table.
	 * 
	 * @param objSampleCollectionType [SampleCollectionType] object holding details
	 *                                to be updated in samplecollectiontype table
	 * @param userInfo [UserInfo]
	 * @return response entity object holding response status and data of updated
	 *         samplecollectiontype object
	 * @throws Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateSampleCollectionType(SampleCollectionType objSampleCollectionType,
			UserInfo userInfo) throws Exception {
		return sampleCollectionTypeDAO.updateSampleCollectionType(objSampleCollectionType, userInfo);
	}

	/**
	 * This method id used to delete an entry in samplecollectiontype table
	 * 
	 * @param objSampleCollectionType [SampleCollectionType] an Object holds the
	 *                                record to be deleted
	 * @param userInfo [UserInfo]
	 * @return a response entity with corresponding HTTP status and an
	 *         samplecollectiontype object
	 * @exception Exception that are thrown from this Service layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteSampleCollectionType(SampleCollectionType objSampleCollectionType,
			UserInfo userInfo) throws Exception {
		return sampleCollectionTypeDAO.deleteSampleCollectionType(objSampleCollectionType, userInfo);
	}

}
