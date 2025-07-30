package com.agaramtech.qualis.instrumentmanagement.service.instrumentcategory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.instrumentmanagement.model.InstrumentCategory;

import lombok.RequiredArgsConstructor;

/**
 * This class is used to perform CRUD operation on 'InstrumentCategory' table
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class InstrumentCategoryServiceImpl implements InstrumentCategoryService {

	private final CommonFunction commonFunction;
	private final InstrumentCategoryDAO objInstrumentCategoryDAO;

	/**
	 * This interface declaration is used to retrieve list of all active Instrument
	 * Category for the specified site through its DAO layer
	 * 
	 * @param objUserInfo object is used for fetched the list of active records
	 *                    based on site
	 * @return response entity object holding response status and list of all active
	 *         Instrument Category
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> fetchInstrumentCategory(final UserInfo objUserInfo) throws Exception {
		return objInstrumentCategoryDAO.fetchInstrumentCategory(objUserInfo);
	}

	/**
	 * This interface declaration is used to retrieve list of all active Instrument
	 * Category for the specified site through its DAO layer
	 * 
	 * @param objUserInfo object is used for fetched the list of active records
	 *                    based on site
	 * @return response entity object holding response status and list of all active
	 *         Instrument Category
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getInstrumentCategory(final UserInfo objUserInfo) throws Exception {
		return objInstrumentCategoryDAO.getInstrumentCategory(objUserInfo);
	}

	/**
	 * This interface declaration is used to add a new entry to Instrument Category
	 * table through its DAO layer.
	 * 
	 * @param UserInfo           object is used for fetched the list of active
	 *                           records based on site
	 * @param InstrumentCategory [Instrument Category] and UserInfo object holding
	 *                           details to be added in Instrument Category table
	 * @return response entity object holding response status and data of added
	 *         Sample Category object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createInstrumentCategory(final InstrumentCategory instrumentCategory, final UserInfo UserInfo)
			throws Exception {
		return objInstrumentCategoryDAO.createInstrumentCategory(instrumentCategory, UserInfo);
	}

	/**
	 * This interface declaration is used to update entry in Instrument Category
	 * table through its DAO layer.
	 * 
	 * @param UserInfo           object is used for fetched the list of active
	 *                           records based on site
	 * @param InstrumentCategory [Instrument Category] and UserInfo object holding
	 *                           details to be updated in Instrument Category table
	 * @return response entity object holding response status and data of updated
	 *         Instrument Category object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateInstrumentCategory(final InstrumentCategory instrumentCategory, final UserInfo UserInfo)
			throws Exception {
		return objInstrumentCategoryDAO.updateInstrumentCategory(instrumentCategory, UserInfo);
	}

	/**
	 * This interface declaration is used to delete entry in Instrument Category
	 * table through its DAO layer.
	 * 
	 * @param UserInfo           object is used for fetched the list of active
	 *                           records based on site
	 * @param InstrumentCategory [Instrument Category] and UserInfo object holding
	 *                           detail to be deleted in Instrument Category table
	 * @return response entity object holding response status and data of deleted
	 *         Instrument Category object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteInstrumentCategory(final InstrumentCategory instrumentCategory, final UserInfo UserInfo)
			throws Exception {
		return objInstrumentCategoryDAO.deleteInstrumentCategory(instrumentCategory, UserInfo);
	}

//	/**
//	 * This method is used to get the Interfacetype from Interfacetype table
//	 * @param userInfo object is used for fetched the list of active records based on site
//	 * @return response entity object holding response status and data of deleted Instrument Category object
//	 * @throws Exception that are thrown in the DAO layer
//	 */
//	@Override
//	public ResponseEntity<Object> getInterfacetype(UserInfo userInfo) throws Exception {
//		
//		return objInstrumentCategoryDAO.getInterfacetype(userInfo);
//	}
	/**
	 * This interface declaration is used to retrieve active Instrument Category
	 * object based on the specified ninstrumentcatcode through its DAO layer.
	 * 
	 * @param userInfo           object is used for fetched the list of active
	 *                           records based on site
	 * @param ninstrumentcatcode [int] primary key of Instrument Category object
	 * @return response entity object holding response status and data of Instrument
	 *         Category object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveInstrumentCategoryById(final int ninstrumentcatcode, final UserInfo userInfo)
			throws Exception {
		final InstrumentCategory instrumentcategory = objInstrumentCategoryDAO
				.getActiveInstrumentCategoryById(ninstrumentcatcode,userInfo);
		if (instrumentcategory == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(instrumentcategory, HttpStatus.OK);
		}
	}
	
	//ALPDJ21-31-->Added by Vignesh(23-07-2025)-->Added the new dropdown Instrument Type-Standard, Storage
		/**
		 * This method is used to get the InstrumentType from instrumenttype table
		 * 
		 * @param userInfo object is used for fetched the list of active records based
		 *                 on site
		 * @return response entity object holding response status and data of deleted
		 *         InstrumentType object
		 * @throws Exception that are thrown in the DAO layer
		 */
	@Override
	public ResponseEntity<Object> getInstrumentType(final UserInfo objUserInfo)
			throws Exception {
		return objInstrumentCategoryDAO.getInstrumentType(objUserInfo);
	}
}
