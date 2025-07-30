package com.agaramtech.qualis.barcode.service.samplepunchsite;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.barcode.model.SamplePunchSite;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'SamplePunchSite' table
 * through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class SamplePunchSiteServiceImpl implements SamplePunchSiteService {

	private final SamplePunchSiteDAO samplePunchSiteDAO;
	private final CommonFunction commonFunction;

	/**
	 * This constructor injection method is used to pass the object dependencies to the class properties.
	 * @param SamplePunchSiteDAO samplePunchSiteDAO Interface
	 * @param commonFunction CommonFunction holding common utility functions
	 */
	public SamplePunchSiteServiceImpl(SamplePunchSiteDAO samplePunchSiteDAO, CommonFunction commonFunction) {
		super();
		this.samplePunchSiteDAO = samplePunchSiteDAO;
		this.commonFunction = commonFunction;
	}

	/**
	 * This service implementation method will access the DAO layer that is used 
	 * to get all the available samplepunchsites with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of samplepunchsite records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSamplePunchSite(UserInfo userInfo) throws Exception {
		return samplePunchSiteDAO.getSamplePunchSite(userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used 
	 * to get all the available projecttypes with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of projecttype records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getProjecttype(UserInfo userinfo) throws Exception {
		return samplePunchSiteDAO.getProjecttype(userinfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used 
	 * to get all the available sampletypes with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of sampletype records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getSampleType(final int nprojecttypecode, UserInfo userinfo) throws Exception {
		return samplePunchSiteDAO.getSampleType(nprojecttypecode, userinfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to samplepunchsite  table.
	 * @param samplePunchSite [SamplePunchSite] object holding details to be added in samplepunchsite table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of added samplepunchsite object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createSamplePunchSite(SamplePunchSite samplePunchSite, UserInfo userInfo)
			throws Exception {
		return samplePunchSiteDAO.createSamplePunchSite(samplePunchSite, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to retrieve active samplepunchsite object based
	 * on the specified nsamplepunchsitecode.
	 * @param nsamplepunchsitecode [int] primary key of samplepunchsite object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and data of samplepunchsite object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveSamplePunchSiteById(int nsamplepunchsitecode, UserInfo userInfo)
			throws Exception {
		final SamplePunchSite objsampleno = samplePunchSiteDAO.getActiveSamplePunchSiteById(nsamplepunchsitecode,
				userInfo);
		if (objsampleno == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(objsampleno, HttpStatus.OK);
		}
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 *  update entry in samplepunchsite  table.
	 * @param samplePunchSite [SamplePunchSite] object holding details to be updated in samplepunchsite table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated samplepunchsite object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateSamplePunchSite(SamplePunchSite samplePunchSite, UserInfo userInfo)
			throws Exception {
		return samplePunchSiteDAO.updateSamplePunchSite(samplePunchSite, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to delete an entry in samplepunchsite table.
	 * @param samplePunchSite [SamplePunchSite] object holding detail to be deleted from samplepunchsite table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted samplepunchsite object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteSamplePunchSite(SamplePunchSite samplePunchSite, UserInfo userInfo)
			throws Exception {
		return samplePunchSiteDAO.deleteSamplePunchSite(samplePunchSite, userInfo);
	}

}
