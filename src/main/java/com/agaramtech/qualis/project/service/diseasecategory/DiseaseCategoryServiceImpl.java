package com.agaramtech.qualis.project.service.diseasecategory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.project.model.DiseaseCategory;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'diseasecategory' table through its DAO layer.
 */
/**
 * @author sujatha.v
 * BGSI-1
 * 26/06/2025
 */
@Service
@Transactional(readOnly= true, rollbackFor= Exception.class)
public class DiseaseCategoryServiceImpl implements DiseaseCategoryService{

	private final DiseaseCategoryDAO diseaseCategoryDAO;
	private final CommonFunction commonFunction;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class properties.
	 * @param diseaseCategoryDAO DiseaseCategoryDAO Interface
	 * @param commonFunction CommonFunction holding common utility functions
	 */
	public DiseaseCategoryServiceImpl(DiseaseCategoryDAO diseaseCategoryDAO, CommonFunction commonFunction) {
		super();
		this.diseaseCategoryDAO = diseaseCategoryDAO;
		this.commonFunction = commonFunction;
	}

	/**
	 * This service implementation method will access the DAO layer that is used 
	 * to get all the available diseasecategory with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of diseasecategory records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getDiseaseCategory(final UserInfo userInfo) throws Exception{
		return diseaseCategoryDAO.getDiseaseCategory(userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to retrieve active diseasecategory object based
	 * on the specified ndiseasecategorycode.
	 * @param ndiseasecategorycode [int] primary key of diseasecategory object
	 * @param userInfo [UserInfo] holding logged in user details based on 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and data of diseasecategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveDiseaseCategoryById(final int ndiseasecategorycode, final UserInfo userInfo)
			throws Exception{
		DiseaseCategory objDiseaseCategory= diseaseCategoryDAO.getActiveDiseaseCategoryById(ndiseasecategorycode, userInfo);
		if(objDiseaseCategory==null) {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
		else {
		    return new ResponseEntity<>(objDiseaseCategory, HttpStatus.OK);
		}
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * add a new entry to diseasecategory  table.
	 * @param objDiseaseCategory [DiseaseCategory] object holding details to be added in diseasecategory table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of added diseasecategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> createDiseaseCategory(final DiseaseCategory objDiseaseCategory,
			final UserInfo userInfo) throws Exception{
		return diseaseCategoryDAO.createDiseaseCategory(objDiseaseCategory, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 *  update entry in diseasecategory table.
	 * @param objDiseaseCategory [DiseaseCategory] object holding details to be updated in diseasecategory table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of updated diseasecategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> updateDiseaseCategory(final DiseaseCategory objDiseaseCategory,
			final UserInfo userInfo) throws Exception{
		return diseaseCategoryDAO.updateDiseaseCategory(objDiseaseCategory, userInfo);
	}

	/**
	 * This service implementation method will access the DAO layer that is used to delete an entry in diseasecategory table.
	 * @param objDiseaseCategory [DiseaseCategory] object holding detail to be deleted from diseasecategory table
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity object holding response status and data of deleted diseasecategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	@Transactional
	public ResponseEntity<Object> deleteDiseaseCategory(final DiseaseCategory objDiseaseCategory,
			final UserInfo userInfo) throws Exception{
		return diseaseCategoryDAO.deleteDiseaseCategory(objDiseaseCategory, userInfo);
	}
}
