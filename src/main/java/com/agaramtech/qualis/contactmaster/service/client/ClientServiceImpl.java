package com.agaramtech.qualis.contactmaster.service.client;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.contactmaster.model.Client;
import com.agaramtech.qualis.contactmaster.model.ClientContactInfo;
import com.agaramtech.qualis.contactmaster.model.ClientFile;
import com.agaramtech.qualis.contactmaster.model.ClientSiteAddress;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

import lombok.RequiredArgsConstructor;

/**
 * This class holds methods to perform CRUD operation on 'client' table through
 * its DAO layer.
 * 
 * @author ATE153
 * @version
 * @since 19- Jun- 2020
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class ClientServiceImpl implements ClientService {

	private final ClientDAO clientDAO;
	private final CommonFunction commonFunction;

	/**
	 * This method is used to retrieve list of all active clients for the specified
	 * site through its DAO layer
	 * 
	 * @param nmasterSiteCode [int] primary key of site object for which the list is
	 *                        to be fetched
	 * @return response entity object holding response status and list of all active
	 *         clients
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getClient(final UserInfo userInfo) throws Exception {
		return clientDAO.getClient(userInfo);
	}

	/**
	 * This method is used to retrieve active client object based on the specified
	 * nclientcode through its DAO layer.
	 * 
	 * @param nclientcode [int] primary key of client object
	 * @param siteCode    [int] primary key of site object
	 * @return response entity object holding response status and data of client
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveClientById(final int nclientcode, final UserInfo userInfo) throws Exception {
		final Client client = (Client) clientDAO.getActiveClientById(nclientcode, userInfo);
		if (client == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(client, HttpStatus.OK);
		}
	}

	/**
	 * This method is used to add a new entry to client table through its DAO layer.
	 * 
	 * @param contactmasterClient [Client] object holding details to be added in
	 *                            client table
	 * @return response entity object holding response status and data of added
	 *         client object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> createClient(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return clientDAO.createClient(inputMap, userInfo);
	}

	@Override
	public ResponseEntity<Object> getClientByCategory(final int nclientCategoryCode, final UserInfo objUserInfo)
			throws Exception {
		return clientDAO.getClientByCategory(nclientCategoryCode, objUserInfo);
	}

	/**
	 * This method is used to update entry in client table through its DAO layer.
	 * 
	 * @param contactmasterClient [Client] object holding details to be updated in
	 *                            client table
	 * @return response entity object holding response status and data of updated
	 *         client object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> updateClient(final Client contactmasterClient, final UserInfo userInfo)
			throws Exception {
		return clientDAO.updateClient(contactmasterClient, userInfo);
	}

	/**
	 * This method is used to delete entry in client table through its DAO layer.
	 * 
	 * @param contactmasterClient [Client] object holding detail to be deleted in
	 *                            client table
	 * @return response entity object holding response status and data of deleted
	 *         client object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Transactional
	@Override
	public ResponseEntity<Object> deleteClient(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return clientDAO.deleteClient(inputMap, userInfo);
	}

	/**
	 * This interface declaration is used to retrieve list of all active clients for
	 * the specified site.
	 * 
	 * @param userInfo -login data are stored for which the list is to be fetched
	 * @return response entity object holding response status and list of all active
	 *         clients
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveClient(final UserInfo userInfo) throws Exception {
		return clientDAO.getActiveClient(userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createClientSiteAddress(final ClientSiteAddress clientSiteAddress,
			final UserInfo userInfo) throws Exception {
		return clientDAO.createClientSiteAddress(clientSiteAddress, userInfo);
	}

	@Override
	public ResponseEntity<Object> getClientSiteAddressById(final int nclientCode, final int nclientSiteCode,
			final UserInfo userInfo) throws Exception {
		final ClientSiteAddress objClientSiteAddress = clientDAO.getClientSiteAddressById(nclientCode, nclientSiteCode,
				userInfo);
		if (objClientSiteAddress != null) {
			return new ResponseEntity<Object>(objClientSiteAddress, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateClientSiteAddress(final ClientSiteAddress clientSiteAddress,
			final UserInfo userInfo) throws Exception {
		return clientDAO.updateClientSiteAddress(clientSiteAddress, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteClientSiteAddress(final ClientSiteAddress clientSiteAddress,
			final UserInfo userInfo) throws Exception {
		return clientDAO.deleteClientSiteAddress(clientSiteAddress, userInfo);
	}

	@Override
	public List<ClientContactInfo> getClientContactInfoBySite(final int nclientCode, final int nclientSiteCode,
			UserInfo userInfo) throws Exception {
		return clientDAO.getClientContactInfoBySite(nclientCode, nclientSiteCode, userInfo);

	}

	@Override
	public ResponseEntity<Object> getClientContactInfoById(final int nclientCode, final int nclientSiteCode,
			final int nclientContactCode, final UserInfo userInfo) throws Exception {
		final ClientContactInfo objClientContactInfo = clientDAO.getClientContactInfoById(nclientCode, nclientSiteCode,
				nclientContactCode, userInfo.getNmastersitecode());
		if (objClientContactInfo != null) {
			return new ResponseEntity<Object>(objClientContactInfo, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteClientContactInfo(final ClientContactInfo objclientContactInfo,
			final UserInfo userInfo) throws Exception {
		return clientDAO.deleteClientContactInfo(objclientContactInfo, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateClientContactInfo(final ClientContactInfo objclientContactInfo,
			final UserInfo userInfo) throws Exception {
		return clientDAO.updateClientContactInfo(objclientContactInfo, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createClientContactInfo(final ClientContactInfo objclientContactInfo,
			final UserInfo userInfo) throws Exception {
		return clientDAO.createClientContactInfo(objclientContactInfo, userInfo);
	}

	public ResponseEntity<Object> getSelectedClientDetail(final UserInfo userInfo, final int nclientcode)
			throws Exception {
		return clientDAO.getSelectedClientDetail(userInfo, nclientcode);
	}

	@Transactional
	@Override
	public ResponseEntity<? extends Object> createClientFile(final MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception {
		return clientDAO.createClientFile(request, objUserInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<? extends Object> updateClientFile(final MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception {
		return clientDAO.updateClientFile(request, objUserInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<? extends Object> deleteClientFile(final ClientFile objClientFile, final UserInfo objUserInfo)
			throws Exception {
		return clientDAO.deleteClientFile(objClientFile, objUserInfo);
	}

	@Override
	public ResponseEntity<Object> editClientFile(final ClientFile objClientFile, final UserInfo objUserInfo)
			throws Exception {
		return clientDAO.editClientFile(objClientFile, objUserInfo);
	}

	@Transactional
	@Override
	public Map<String, Object> viewAttachedClientFile(final ClientFile objClientFile, final UserInfo objUserInfo)
			throws Exception {
		return clientDAO.viewAttachedClientFile(objClientFile, objUserInfo);
	}

}
