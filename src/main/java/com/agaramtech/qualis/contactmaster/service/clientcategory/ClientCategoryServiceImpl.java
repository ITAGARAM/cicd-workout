package com.agaramtech.qualis.contactmaster.service.clientcategory;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.contactmaster.model.ClientCategory;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

import lombok.RequiredArgsConstructor;

/**
 * This class holds methods to perform CRUD operation on 'clientcategory' table
 * through its DAO layer.
 * 
 * @author ATE113
 * @version 9.0.0.1
 * @since 08- Sep- 2020
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class ClientCategoryServiceImpl implements ClientCategoryService {

	private final ClientCategoryDAO clientCategoryDAO;
	private final CommonFunction commonFunction;

	/**
	 * This method is used to retrieve list of all active clientcategory for the
	 * specified site through its DAO layer
	 * 
	 * @param nmasterSiteCode [int] primary key of site object for which the list is
	 *                        to be fetched
	 * @return response entity object holding response status and list of all active
	 *         clientcategory
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<List<ClientCategory>> getClientCategory(final UserInfo userInfo) throws Exception {
		return clientCategoryDAO.getClientCategory(userInfo);
	}

	/**
	 * This method is used to retrieve list of all active clientcategory for the
	 * specified site through its DAO layer
	 * 
	 * @param nmasterSiteCode [int] primary key of site object for which the list is
	 *                        to be fetched
	 * @return response entity object holding response status and list of all active
	 *         clientcategory
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getClientCategoryForPortal(final UserInfo userInfo) throws Exception {
		return clientCategoryDAO.getClientCategoryForPortal(userInfo);
	}

	/**
	 * This method is used to retrieve active clientcategory object based on the
	 * specified nclientcatcode through its DAO layer.
	 * 
	 * @param nclientcatcode [int] primary key of clientcategory object
	 * @param siteCode       [int] primary key of site object
	 * @return response entity object holding response status and data of
	 *         clientcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveClientCategoryById(final int nclientcatcode, final UserInfo userInfo)
			throws Exception {
		final ClientCategory clientCategory = (ClientCategory) clientCategoryDAO
				.getActiveClientCategoryById(nclientcatcode);
		if (clientCategory == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(clientCategory, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to add a new entry to clientcategory table through its
	 * DAO layer.
	 * 
	 * @param clientcategory [ClientCategory] object holding details to be added in
	 *                       clientcategory table
	 * @return response entity object holding response status and data of added
	 *         clientcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<? extends Object> createClientCategory(final ClientCategory clientCategory, final UserInfo userInfo)
			throws Exception {
		return clientCategoryDAO.createClientCategory(clientCategory, userInfo);
	}

	/**
	 * This method is used to update entry in clientcategory table through its DAO
	 * layer.
	 * 
	 * @param clientcategory [ClientCategory] object holding details to be updated
	 *                       in clientcategory table
	 * @return response entity object holding response status and data of updated
	 *         clientcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<? extends Object> updateClientCategory(final ClientCategory clientCategory, final UserInfo userInfo)
			throws Exception {
		return clientCategoryDAO.updateClientCategory(clientCategory, userInfo);
	}

	/**
	 * This method is used to delete entry in clientcategory table through its DAO
	 * layer.
	 * 
	 * @param clientcategory [ClientCategory] object holding detail to be deleted in
	 *                       clientcategory table
	 * @return response entity object holding response status and data of deleted
	 *         clientcategory object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<? extends Object> deleteClientCategory(final ClientCategory clientCategory, final UserInfo userInfo)
			throws Exception {
		return clientCategoryDAO.deleteClientCategory(clientCategory, userInfo);
	}
}
