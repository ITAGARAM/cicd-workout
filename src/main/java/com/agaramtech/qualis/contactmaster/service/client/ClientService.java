package com.agaramtech.qualis.contactmaster.service.client;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.contactmaster.model.Client;
import com.agaramtech.qualis.contactmaster.model.ClientContactInfo;
import com.agaramtech.qualis.contactmaster.model.ClientFile;
import com.agaramtech.qualis.contactmaster.model.ClientSiteAddress;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface declaration holds methods to perform CRUD operation on
 * 'client' table through its DAO layer.
 * 
 * @author ATE181
 * @version
 * @since 19- Jun- 2020
 */
public interface ClientService {

	/**
	 * This interface declaration is used to retrieve list of all active client for
	 * the specified site through its DAO layer
	 * 
	 * @param nmasterSiteCode [int] primary key of site object for which the list is
	 *                        to be fetched
	 * @return response entity object holding response status and list of all active
	 *         client
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getClient(final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to retrieve active client object based on
	 * the specified nclientcode through its DAO layer.
	 * 
	 * @param nclientcode [int] primary key of client object
	 * @param siteCode    [int] primary key of site object
	 * @return response entity object holding response status and data of client
	 *         object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getActiveClientById(final int nclientcode, final UserInfo userInfo) throws Exception;

	/**
	 * This interface declaration is used to add a new entry to client table through
	 * its DAO layer.
	 * 
	 * @param contactmasterClient [Client] object holding details to be added in
	 *                            client table
	 * @return response entity object holding response status and data of added
	 *         client object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> createClient(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to update entry in client table through
	 * its DAO layer.
	 * 
	 * @param contactmasterClient [Client] object holding details to be updated in
	 *                            client table
	 * @return response entity object holding response status and data of updated
	 *         client object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> updateClient(final Client contactmasterClient, final UserInfo userInfo)
			throws Exception;

	/**
	 * This interface declaration is used to delete entry in client table through
	 * its DAO layer.
	 * 
	 * @param contactmasterClient [Client] object holding detail to be deleted in
	 *                            client table
	 * @return response entity object holding response status and data of deleted
	 *         client object
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> deleteClient(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> getActiveClient(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createClientSiteAddress(final ClientSiteAddress clientSiteAddress,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getClientSiteAddressById(final int nclientCode, final int nclientSiteCode,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateClientSiteAddress(final ClientSiteAddress clientSiteAddress,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteClientSiteAddress(final ClientSiteAddress clientSiteAddress,
			final UserInfo userInfo) throws Exception;

	public List<ClientContactInfo> getClientContactInfoBySite(final int nclientCode, final int nclientSiteCode,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createClientContactInfo(final ClientContactInfo clientContactInfo,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getClientContactInfoById(final int nclientCode, final int nclientSiteCode,
			final int nclientContactCode, UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> updateClientContactInfo(final ClientContactInfo clientContactInfo,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteClientContactInfo(final ClientContactInfo clientContactInfo,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> getSelectedClientDetail(final UserInfo userInfo, final int nclientCode)
			throws Exception;

	public ResponseEntity<Object> getClientByCategory(final int nclientcatcode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<? extends Object> createClientFile(final MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception;

	public ResponseEntity<? extends Object> updateClientFile(final MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception;

	public ResponseEntity<? extends Object> deleteClientFile(final ClientFile objClientFile, final UserInfo objUserInfo)
			throws Exception;

	public ResponseEntity<Object> editClientFile(final ClientFile objClientFile, final UserInfo objUserInfo)
			throws Exception;

	public Map<String, Object> viewAttachedClientFile(final ClientFile objClientFile, final UserInfo objUserInfo)
			throws Exception;

}
