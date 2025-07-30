package com.agaramtech.qualis.contactmaster.service.client;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.configuration.model.Settings;
import com.agaramtech.qualis.contactmaster.model.Client;
import com.agaramtech.qualis.contactmaster.model.ClientCategory;
import com.agaramtech.qualis.contactmaster.model.ClientContactInfo;
import com.agaramtech.qualis.contactmaster.model.ClientFile;
import com.agaramtech.qualis.contactmaster.model.ClientSiteAddress;
import com.agaramtech.qualis.contactmaster.service.clientcategory.ClientCategoryDAO;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CloudStorageUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.LinkMaster;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "client" table by
 * implementing methods from its interface.
 * 
 * @author ATE153
 * @version
 * @since 19- Jun- 2020
 */
@AllArgsConstructor
@Repository
public class ClientDAOImpl implements ClientDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClientDAOImpl.class);

	private final ClientCategoryDAO categoryDAO;
	private final StringUtilityFunction stringUtilityFunction;
	private final FTPUtilityFunction ftpUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel validatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final CloudStorageUtilityFunction cloudStorageUtilityFunction;
	
	/**
	 * This method is used to retrieve list of all active clients for the specified
	 * site.
	 * 
	 * @param nmasterSiteCode [int] primary key of site object for which the list is
	 *                        to be fetched
	 * @return response entity object holding response status and list of all active
	 *         clients
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getClient(final UserInfo userInfo) throws Exception {
		LOGGER.info("getClient");
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		final List<ClientCategory> lstClientCategory = categoryDAO.getClientCategory(userInfo).getBody();
		outputMap.put("filterClientCategory", lstClientCategory);

		if (!lstClientCategory.isEmpty()) {
			int nclientcategorycode = 0;
			final List<ClientCategory> defaultCategory = lstClientCategory.stream().filter(
					defTest -> defTest.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus())
					.collect(Collectors.toList());
			if (defaultCategory.isEmpty()) {
				nclientcategorycode = lstClientCategory.get(lstClientCategory.size() - 1).getNclientcatcode();
				outputMap.put("SelectedClientCat", lstClientCategory.get(lstClientCategory.size() - 1));
			} else {
				nclientcategorycode = defaultCategory.get(0).getNclientcatcode();
				outputMap.put("SelectedClientCat", defaultCategory.get(0));
			}
			final List<Client> lstClient = getClientbyClientCategory(nclientcategorycode, userInfo.getNmastersitecode(),
					0, userInfo);
			outputMap.put("Client", lstClient);
			if (lstClient.isEmpty()) {
				outputMap.put("selectedClient", null);
				outputMap.put("selectedClientSite", null);
				outputMap.put("ClientContact", lstClient);
				outputMap.put("ClientSite", lstClient);
			} else {
				outputMap.put("selectedClient", lstClient.get(lstClient.size() - 1));
				final int nclientcode = lstClient.get(lstClient.size() - 1).getNclientcode();
				final List<ClientSiteAddress> lstClientSiteGet = getClientSiteAddressDetails(userInfo, nclientcode);
				if (lstClientSiteGet.size() > 0) {
					final int nclientSiteCode = lstClientSiteGet.get(lstClientSiteGet.size() - 1).getNclientsitecode();
					outputMap.put("selectedClientSite", lstClientSiteGet.get(lstClientSiteGet.size() - 1));
					final List<ClientContactInfo> lstClientContactGet = getClientContactInfoBySite(nclientcode,
							nclientSiteCode, userInfo);
					if (lstClientContactGet.size() > 0) {
						outputMap.put("selectedClientContact", lstClientContactGet.get(lstClientContactGet.size() - 1));

					}
					outputMap.put("ClientContact", lstClientContactGet);

				}
				outputMap.put("ClientSite", lstClientSiteGet);

				outputMap.putAll(getClientFile(nclientcode, userInfo).getBody());
			}
		} else {
			outputMap.put("selectedClient", null);
			outputMap.put("selectedClientSite", null);
			outputMap.put("ClientContact", lstClientCategory);
			outputMap.put("ClientSite", lstClientCategory);
		}

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	/**
	 * This method is used to retrieve active client object based on the specified
	 * nclientcode.
	 * 
	 * @param nclientcode [int] primary key of client object
	 * @return client object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public Client getActiveClientById(final int nclientcode, final UserInfo userInfo) throws Exception {
		final String strQuery = "select cl.nclientcode, cl.nclientcatcode, cl.ncountrycode, cl.sclientname, cl.sclientid, cl.ntransactionstatus, "
				+ " cl.nsitecode, cl.nstatus,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,c.scountryname scountryname,cc.sclientcatname "
				+ " from client cl,transactionstatus ts,Country c,clientcategory cc where cc.nclientcatcode=cl.nclientcatcode"
				+ " and cl.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and cl.nclientcode > 0 and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
				+ " ts.ntranscode=cl.ntransactionstatus and c.nsitecode = cl.nsitecode and c.nstatus = cl.nstatus "
				+ " and c.nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and c.nsitecode="
				+ userInfo.getNmastersitecode() + " and c.ncountrycode = cl.ncountrycode and cl.nclientcode = "
				+ nclientcode;
		return (Client) jdbcUtilityFunction.queryForObject(strQuery, Client.class, jdbcTemplate);
	}

	/**
	 * This method is used to add a new entry to client table. Need to check for
	 * duplicate entry of client name for the specified site before saving into
	 * database.
	 * 
	 * @param contactmasterClient [Client] object holding details to be added in
	 *                            client table
	 * @return response entity object holding response status and data of added
	 *         client object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> createClient(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final String sQuery = " lock  table lockclient " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedClientList = new ArrayList<>();
		final ObjectMapper objmapper = new ObjectMapper();
		final Client contactmasterClient = objmapper.convertValue(inputMap.get("client"), Client.class);
		final ClientSiteAddress clientSiteAddress = objmapper.convertValue(inputMap.get("clientsiteaddress"),
				ClientSiteAddress.class);
		clientSiteAddress.setIsreadonly(true);
		final Client clientListByName = getClientListByName(contactmasterClient.getSclientname(),
				contactmasterClient.getNsitecode());
		final Client clientListById = getClientListById(contactmasterClient.getSclientid(),
				contactmasterClient.getNsitecode());
		if (clientListByName == null && clientListById == null) {
			final ClientCategory activecategory = getClientCatByclientcat(contactmasterClient.getNclientcatcode(),
					userInfo.getNmastersitecode());
			if (activecategory != null) {
				final String sequencenoquery = "select nsequenceno from seqnocontactmaster where stablename ='client' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				int nsequenceno = jdbcTemplate.queryForObject(sequencenoquery, Integer.class);
				nsequenceno++;
				final String addContactMasterClient = "Insert into client (nclientcode, nclientcatcode, ncountrycode, sclientname, sclientid, "
						+ "ntransactionstatus,dmodifieddate,nsitecode, nstatus) values (" + nsequenceno + ", "
						+ contactmasterClient.getNclientcatcode() + ", " + contactmasterClient.getNcountrycode()
						+ ", N'" + stringUtilityFunction.replaceQuote(contactmasterClient.getSclientname()) + "', N'"
						+ stringUtilityFunction.replaceQuote(contactmasterClient.getSclientid()) + "', "
						+ contactmasterClient.getNtransactionstatus() + ", '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + contactmasterClient.getNsitecode()
						+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
				jdbcTemplate.execute(addContactMasterClient);

				final String updatequery = "update seqnocontactmaster set nsequenceno =" + nsequenceno
						+ " where stablename='client'";
				jdbcTemplate.execute(updatequery);

				clientSiteAddress.setNclientcode(nsequenceno);

				contactmasterClient.setNclientcode(nsequenceno);
				savedClientList.add(contactmasterClient);

				multilingualIDList.add("IDS_ADDCLIENT");

				auditUtilityFunction.fnInsertAuditAction(savedClientList, 1, null, multilingualIDList, userInfo);
				clientSiteAddress.setNclientcatcode(contactmasterClient.getNclientcatcode());
				return createClientSiteAddress(clientSiteAddress, userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	/**
	 * This method is used to fetch the active client objects for the specified
	 * client name and site.
	 * 
	 * @param clientName      [String] client name for which the records are to be
	 *                        fetched
	 * @param nmasterSiteCode [int] primary key of site object
	 * @return list of active clients based on the specified client name and site
	 * @throws Exception that are thrown from this DAO layer
	 */
	private Client getClientListByName(final String clientName, final int nmasterSiteCode) throws Exception {
		final String strQuery = "select nclientcode from client where sclientname = N'"
				+ stringUtilityFunction.replaceQuote(clientName) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;
		return (Client) jdbcUtilityFunction.queryForObject(strQuery, Client.class, jdbcTemplate);
	}

	private Client getClientListById(final String clientId, final int nmasterSiteCode) throws Exception {
		final String strQuery = "select nclientcode from client where sclientid = N'"
				+ stringUtilityFunction.replaceQuote(clientId) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + nmasterSiteCode;
		return (Client) jdbcUtilityFunction.queryForObject(strQuery, Client.class, jdbcTemplate);
	}

	/**
	 * This method is used to update entry in client table. Need to validate that
	 * the client object to be updated is active before updating details in
	 * database. Need to check for duplicate entry of client name for the specified
	 * site before saving into database.
	 * 
	 * @param contactmasterClient [Client] object holding details to be updated in
	 *                            client table
	 * @return response entity object holding response status and data of updated
	 *         client object
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> updateClient(final Client contactmasterClient, final UserInfo userInfo)
			throws Exception {

		final Client client = getActiveClientById(contactmasterClient.getNclientcode(), userInfo);

		if (client == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final ClientCategory activecategory = getClientCatByclientcat(contactmasterClient.getNclientcatcode(),
					userInfo.getNmastersitecode());
			if (activecategory != null) {
				final String queryString = "select nclientcode from client where sclientname = N'"
						+ stringUtilityFunction.replaceQuote(contactmasterClient.getSclientname())
						+ "' and nclientcode <> " + contactmasterClient.getNclientcode() + " and  nstatus = "
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
						+ userInfo.getNmastersitecode();

				final List<Client> clientList = jdbcTemplate.query(queryString, new Client());

				if (clientList.isEmpty()) {
					final String updateQueryString = "update client set sclientname=N'"
							+ stringUtilityFunction.replaceQuote(contactmasterClient.getSclientname())
							+ "', sclientid = N'"
							+ stringUtilityFunction.replaceQuote(contactmasterClient.getSclientid())
							+ "', ntransactionstatus= " + contactmasterClient.getNtransactionstatus()
							+ ", nclientcatcode =" + contactmasterClient.getNclientcatcode() + ", dmodifieddate = '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' " + " where nclientcode="
							+ contactmasterClient.getNclientcode() + " and nsitecode=" + userInfo.getNmastersitecode();

					jdbcTemplate.execute(updateQueryString);

					final List<String> multilingualIDList = new ArrayList<>();
					multilingualIDList.add("IDS_EDITCLIENT");

					final List<Object> listAfterSave = new ArrayList<>();
					listAfterSave.add(contactmasterClient);

					final List<Object> listBeforeSave = new ArrayList<>();
					listBeforeSave.add(client);

					auditUtilityFunction.fnInsertAuditAction(listAfterSave, 2, listBeforeSave, multilingualIDList,
							userInfo);

					return getClientByClient(contactmasterClient.getNclientcode(), userInfo);

				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.CONFLICT);
				}
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	/**
	 * This method is used to delete entry in client table. Need to validate that
	 * the specified client object is active and is not associated with any of its
	 * child tables before updating its nstatus to -1.
	 * 
	 * @param contactmasterClient [Client] object holding detail to be deleted in
	 *                            client table
	 * @return response entity object holding response status and data of deleted
	 *         client object
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> deleteClient(final Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final Client contactmasterClient = objmapper.convertValue(inputMap.get("client"), Client.class);
		final Client client = getActiveClientById(contactmasterClient.getNclientcode(), userInfo);

		if (client == null) {

			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final String query = "select 'IDS_QUOTATION' as Msg from quotation where nclientcode= "
					+ client.getNclientcode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
					+ userInfo.getNtranssitecode() + "  union all "
					+ "select 'IDS_PROJECTMASTER' as Msg from  projectmaster pm where  pm.nclientcode = "
					+ client.getNclientcode() + " and pm.nsitecode = " + userInfo.getNtranssitecode()
					+ " and pm.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " union all "
					+ " select 'IDS_GOODSIN' as Msg from goodsin gi where gi.nclientcode = " + client.getNclientcode()
					+ " and gi.nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and gi.nsitecode = " + userInfo.getNtranssitecode();

			validatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);

			boolean validRecord = false;
			if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				final Map<String, Object> objOneToManyValidation = new HashMap<String, Object>();
				objOneToManyValidation.put("primaryKeyValue", Integer.toString(contactmasterClient.getNclientcode()));
				objOneToManyValidation.put("stablename", "client");
				validatorDel = projectDAOSupport.validateOneToManyDeletion(objOneToManyValidation, userInfo);
				if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}
			if (validRecord) {
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> AuditActionList = new ArrayList<>();
				final List<Object> deletedClientList = new ArrayList<>();
				List<ClientFile> lstclientFile = new ArrayList<>();
				List<ClientSiteAddress> lstclientSite = new ArrayList<>();
				List<ClientContactInfo> lstclientContact = new ArrayList<>();

				final String strFile = "select nclientfilecode,nclientcode,nlinkcode,nattachmenttypecode,sfilename,sdescription,nfilesize,dcreateddate,ntzcreateddate,noffsetdcreateddate,ssystemfilename,nsitecode,nstatus"
						+ " from clientfile where nclientcode = " + contactmasterClient.getNclientcode()
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				lstclientFile = jdbcTemplate.query(strFile, new ClientFile());

				final String strSite = "select nclientsitecode,nclientcode,ncountrycode,sclientsitename,saddress1,saddress2,saddress3,ndefaultstatus,nsitecode,nstatus "
						+ " from clientsiteaddress where nclientcode = " + contactmasterClient.getNclientcode()
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				lstclientSite = jdbcTemplate.query(strSite, new ClientSiteAddress());

				final String strContact = "select nclientcontactcode,nclientsitecode,nclientcode,scontactname,sphoneno,smobileno,semail,sfaxno,scomments,ndefaultstatus,nsitecode,nstatus "
						+ " from clientcontactinfo where nclientcode = " + contactmasterClient.getNclientcode()
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				lstclientContact = jdbcTemplate.query(strContact, new ClientContactInfo());

				String updateQueryString = "update client set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where nclientcode="
						+ contactmasterClient.getNclientcode() + ";";

				updateQueryString = updateQueryString + "update clientsiteaddress set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where nclientcode="
						+ contactmasterClient.getNclientcode() + ";";

				updateQueryString = updateQueryString + "update clientcontactinfo set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate = '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where nclientcode="
						+ contactmasterClient.getNclientcode() + ";";

				updateQueryString = updateQueryString + "update clientfile set" + "  dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + ", nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nclientcode = "
						+ contactmasterClient.getNclientcode();

				jdbcTemplate.execute(updateQueryString);

				contactmasterClient.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				deletedClientList.add(contactmasterClient);
				AuditActionList.add(deletedClientList);
				multilingualIDList.add("IDS_DELETECLIENT");
				AuditActionList.add(lstclientSite);
				multilingualIDList.add("IDS_DELETECLIENTSITE");
				AuditActionList.add(lstclientContact);
				multilingualIDList.add("IDS_DELETECLIENTCONTACT");

				if (!lstclientFile.isEmpty()) {

					final List<ClientFile> listDeleteFile = lstclientFile.stream()
							.filter(b -> b.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype())
							.collect(Collectors.toList());

					final List<ClientFile> listDeleteLink = lstclientFile.stream()
							.filter(b -> b.getNattachmenttypecode() == Enumeration.AttachmentType.LINK.gettype())
							.collect(Collectors.toList());
					AuditActionList.add(listDeleteFile);
					multilingualIDList.add("IDS_DELETECLIENTFILE");
					AuditActionList.add(listDeleteLink);
					multilingualIDList.add("IDS_DELETECLIENTLINK");
				}

				auditUtilityFunction.fnInsertListAuditAction(AuditActionList, 1, null, multilingualIDList, userInfo);
				return getClientByCategory(contactmasterClient.getNclientcatcode(), userInfo);
			} else {
				return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
			}
		}
	}

	/**
	 * This method is used to retrieve list of all active clients for the specified
	 * site.
	 * 
	 * @param nmasterSiteCode [int] primary key of site object for which the list is
	 *                        to be fetched
	 * @return response entity object holding response status and list of all active
	 *         clients
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getActiveClient(final UserInfo userInfo) throws Exception {

		final String strQuery = "select cl.nclientcode, cl.nclientcatcode, cl.ncountrycode, cl.sclientname, cl.sclientid, cl.ntransactionstatus, "
				+ " cl.nsitecode, cl.nstatus,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ "ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,c.scountryname scountryname ,cc.sclientcatname"
				+ " from client cl,transactionstatus ts,Country c,clientcategory cc where cc.nclientcatcode=cl.nclientcatcode"
				+ " and  cl.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and  cc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and cl.nclientcode > 0 and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
				+ " ts.ntranscode=cl.ntransactionstatus and c.nsitecode = cl.nsitecode and c.nstatus = cl.nstatus and "
				+ " c.ncountrycode = cl.ncountrycode and  cl.nsitecode = " + userInfo.getNmastersitecode();
		return new ResponseEntity<>(jdbcTemplate.query(strQuery, new Client()), HttpStatus.OK);
	}

	private List<Client> getClientbyClientCategory(final int nclinetcatcode, final int nmastersitecode,
			final int nclientcode, final UserInfo userInfo) throws Exception {
		final String strQuery = "select cl.nclientcode, cl.nclientcatcode, cl.ncountrycode, cl.sclientname, cl.sclientid, cl.ntransactionstatus, "
				+ " cl.nsitecode, cl.nstatus,coalesce(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'stransdisplaystatus'->>'en-US') as stransdisplaystatus,c.scountryname scountryname ,cc.sclientcatname"
				+ " from client cl,transactionstatus ts,Country c,clientcategory cc where cc.nclientcatcode=cl.nclientcatcode"
				+ " and cl.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and cc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and cl.nclientcode > 0 and ts.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and "
				+ " ts.ntranscode=cl.ntransactionstatus and c.nsitecode = cl.nsitecode and c.nstatus = cl.nstatus and "
				+ " c.ncountrycode = cl.ncountrycode and  cl.nsitecode = " + userInfo.getNmastersitecode()
				+ " and cl.nclientcatcode=" + nclinetcatcode + " order by cl.nclientcode";
		return jdbcTemplate.query(strQuery, new Client());
	}

	public ClientCategory getClientCatByclientcat(final int nclientCategoryCode, final int nmastersitecode)
			throws Exception {
		final String query = "select * from clientcategory where nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nclientcatcode="
				+ nclientCategoryCode + " and nsitecode=" + nmastersitecode;
		final ClientCategory lstClinetCat = (ClientCategory) jdbcUtilityFunction.queryForObject(query,
				ClientCategory.class, jdbcTemplate);
		return lstClinetCat;
	}

	public ClientCategory getClientCatByclientcode(final int nclientcode, final int nmastersitecode) throws Exception {
		final String query = "select cc.*, c.nclientcode, c.nclientcatcode, c.ncountrycode, c.sclientname, c.sclientid,"
				+ " c.ntransactionstatus, c.nsitecode, c.nstatus from clientcategory cc,client c where c.nclientcatcode=cc.nclientcatcode and "
				+ " c.nclientcode=" + nclientcode + " and c.nsitecode=" + nmastersitecode
				+ " and c.nsitecode=cc.nsitecode and cc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final ClientCategory lstClinetCat = (ClientCategory) jdbcUtilityFunction.queryForObject(query,
				ClientCategory.class, jdbcTemplate);
		return lstClinetCat;
	}

	public ResponseEntity<Object> getClientByClient(final int nclientcode, final UserInfo objUserInfo)
			throws Exception {
		final Map<String, Object> map = new LinkedHashMap<String, Object>();
		final ClientCategory lstClientCat = getClientCatByclientcode(nclientcode, objUserInfo.getNmastersitecode());

		if (lstClientCat != null) {
			map.put("SelectedClientCat", lstClientCat);
		} else {
			map.put("SelectedClientCat", null);
		}
		final List<Client> lstClient = getClientbyClientCategory(lstClientCat.getNclientcatcode(),
				objUserInfo.getNmastersitecode(), 0, objUserInfo);
		map.put("Client", lstClient);
		if (!lstClient.isEmpty()) {
			final Client client = getActiveClientById(nclientcode, objUserInfo);
			map.put("selectedClient", client);
			final List<ClientSiteAddress> lstClientSiteGet = getClientSiteAddressDetails(objUserInfo, nclientcode);
			if (lstClientSiteGet.size() > 0) {
				final int nclientSiteCode = lstClientSiteGet.get(lstClientSiteGet.size() - 1).getNclientsitecode();
				map.put("selectedClientSite", lstClientSiteGet.get(lstClientSiteGet.size() - 1));
				final List<ClientContactInfo> lstClientContactGet = getClientContactInfoBySite(nclientcode,
						nclientSiteCode, objUserInfo);
				if (lstClientContactGet.size() > 0) {
					map.put("selectedClientContact", lstClientContactGet.get(lstClientContactGet.size() - 1));

				}
				map.put("ClientContact", lstClientContactGet);

			}
			map.put("ClientSite", lstClientSiteGet);
		} else {
			map.put("ClientContact", Arrays.asList());
			map.put("ClientSite", Arrays.asList());
			map.put("selectedClient", null);
			map.put("selectedClientSite", null);
		}
		return new ResponseEntity<Object>(map, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getClientByCategory(final int nclientCategoryCode, final UserInfo objUserInfo)
			throws Exception {
		final Map<String, Object> map = new LinkedHashMap<String, Object>();
		final Map<String, Object> catfiltermap = new LinkedHashMap<String, Object>();
		final ClientCategory lstClientCat = getClientCatByclientcat(nclientCategoryCode,
				objUserInfo.getNmastersitecode());
		if (lstClientCat != null) {
			map.put("SelectedClientCat", lstClientCat);
			catfiltermap.put("value", lstClientCat.getNclientcatcode());
			catfiltermap.put("label", lstClientCat.getSclientcatname());
			map.put("nfilterClientCategory", catfiltermap);
		} else {
			map.put("SelectedClientCat", null);
		}
		final List<Client> lstClient = getClientbyClientCategory(nclientCategoryCode, objUserInfo.getNmastersitecode(),
				0, objUserInfo);
		map.put("Client", lstClient);
		if (!lstClient.isEmpty()) {
			map.put("selectedClient", lstClient.get(lstClient.size() - 1));
			final int nclientcode = lstClient.get(lstClient.size() - 1).getNclientcode();
			final List<ClientSiteAddress> lstClientSiteGet = getClientSiteAddressDetails(objUserInfo, nclientcode);
			if (lstClientSiteGet.size() > 0) {
				final int nclientSiteCode = lstClientSiteGet.get(lstClientSiteGet.size() - 1).getNclientsitecode();
				map.put("selectedClientSite", lstClientSiteGet.get(lstClientSiteGet.size() - 1));
				final List<ClientContactInfo> lstClientContactGet = getClientContactInfoBySite(nclientcode,
						nclientSiteCode, objUserInfo);
				if (lstClientContactGet.size() > 0) {
					map.put("selectedClientContact", lstClientContactGet.get(lstClientContactGet.size() - 1));

				}
				map.put("ClientContact", lstClientContactGet);

			}
			map.putAll(getClientFile(nclientcode, objUserInfo).getBody());
			map.put("ClientSite", lstClientSiteGet);
		} else {
			map.put("ClientContact", Arrays.asList());
			map.put("ClientSite", Arrays.asList());
			map.put("selectedClient", null);
			map.put("selectedClientSite", null);
		}
		return new ResponseEntity<Object>(map, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createClientSiteAddress(final ClientSiteAddress clientSiteAddress,
			final UserInfo userInfo) throws Exception {

		final String sQuery = " lock  table lockclient " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final List<Object> savedClientSiteAddressList = new ArrayList<>();
		final List<String> multilingualIDList = new ArrayList<>();

		final Client nclientcode = getClientByIdForInsert(clientSiteAddress.getNclientcode(), userInfo);

		if (nclientcode == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_CLIENTALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final ClientSiteAddress clientSiteAddressByName = getClientSiteAddressByName(
					clientSiteAddress.getSclientsitename(), clientSiteAddress.getNclientcode(),
					userInfo.getNmastersitecode());

			if (clientSiteAddressByName == null) {
				if (clientSiteAddress.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

					final ClientSiteAddress defaultClientSiteAddress = getClientSiteAddressByDefault(
							clientSiteAddress.getNclientcode(), userInfo.getNmastersitecode());

					if (defaultClientSiteAddress != null) {

						final ClientSiteAddress clientSiteAddressBeforeSave = SerializationUtils
								.clone(defaultClientSiteAddress);

						final List<Object> defaultListBeforeSave = new ArrayList<>();
						defaultListBeforeSave.add(clientSiteAddressBeforeSave);

						defaultClientSiteAddress
								.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());

						final String updateQueryString = " update clientsiteaddress set ndefaultstatus="
								+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", dmodifieddate = '"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' " + " where nclientsitecode ="
								+ defaultClientSiteAddress.getNclientsitecode() + " and nsitecode="
								+ userInfo.getNmastersitecode() + " and nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
						jdbcTemplate.execute(updateQueryString);

						final List<Object> defaultListAfterSave = new ArrayList<>();
						defaultListAfterSave.add(defaultClientSiteAddress);

						multilingualIDList.add("IDS_EDITCLIENTSITE");

						auditUtilityFunction.fnInsertAuditAction(defaultListAfterSave, 2, defaultListBeforeSave,
								multilingualIDList, userInfo);
						multilingualIDList.clear();
					}

				}
				String clientSiteSeq = "";
				clientSiteSeq = "select nsequenceno from seqnocontactmaster where stablename='clientsiteaddress' and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				int seqNo = jdbcTemplate.queryForObject(clientSiteSeq, Integer.class);
				seqNo = seqNo + 1;
				String clientSiteInsert = "";
				final String address2 = clientSiteAddress.getSaddress2() == null ? ""
						: stringUtilityFunction.replaceQuote(clientSiteAddress.getSaddress2());
				final String address3 = clientSiteAddress.getSaddress3() == null ? ""
						: stringUtilityFunction.replaceQuote(clientSiteAddress.getSaddress3());

				clientSiteInsert = "insert into clientsiteaddress(nclientsitecode,nclientcode,ncountrycode,sclientsitename,saddress1,"
						+ "saddress2,saddress3,ndefaultstatus,dmodifieddate,nsitecode,nstatus)values (" + seqNo + ","
						+ clientSiteAddress.getNclientcode() + "," + "" + clientSiteAddress.getNcountrycode() + ",'"
						+ stringUtilityFunction.replaceQuote(clientSiteAddress.getSclientsitename()) + "','"
						+ stringUtilityFunction.replaceQuote(clientSiteAddress.getSaddress1()) + "'," + "'" + address2
						+ "','" + address3 + "'," + clientSiteAddress.getNdefaultstatus() + ", '"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode()
						+ ",1)";
				jdbcTemplate.execute(clientSiteInsert);

				clientSiteSeq = "update seqnocontactmaster set nsequenceno=" + seqNo
						+ " where stablename='clientsiteaddress'";
				jdbcTemplate.execute(clientSiteSeq);

				multilingualIDList.add("IDS_ADDCLIENTSITE");
				clientSiteAddress.setNclientsitecode(seqNo);
				savedClientSiteAddressList.add(clientSiteAddress);

				auditUtilityFunction.fnInsertAuditAction(savedClientSiteAddressList, 1, null, multilingualIDList,
						userInfo);

				if (clientSiteAddress.isIsreadonly() != true) {
					return getClientByClient(clientSiteAddress.getNclientcode(), userInfo);
				} else {
					final ObjectMapper objmapper = new ObjectMapper();
					final Map<String, Object> clientContact = new HashMap<>();
					clientContact.put("nclientcode", clientSiteAddress.getNclientcode());
					clientContact.put("nclientsitecode", clientSiteAddress.getNclientsitecode());
					clientContact.put("scontactname", clientSiteAddress.getScontactname());
					clientContact.put("ndefaultstatus", clientSiteAddress.getNdefaultstatus());
					clientContact.put("isreadonly", clientSiteAddress.isIsreadonly());
					clientContact.put("nclientcatcode", clientSiteAddress.getNclientcatcode());
					final ClientContactInfo clientContactInfo = objmapper.convertValue(clientContact,
							ClientContactInfo.class);
					clientContactInfo.setNclientsitecode(seqNo);
					return createClientContactInfo(clientContactInfo, userInfo);
				}
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	private Client getClientByIdForInsert(final int nclientCode, final UserInfo userInfo) throws Exception {
		final String strQuery = " select a.nclientcode, a.nclientcatcode, a.ncountrycode,a.sclientname, a.sclientid, a.ntransactionstatus, a.nsitecode, a.nstatus ,"
				+ " coalesce(ts.jsondata->'sactiondisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'sactiondisplaystatus'->>'en-US') as stransdisplaystatus"
				+ " from client a,transactionstatus ts where ts.ntranscode = a.ntransactionstatus "
				+ " and a.nstatus = ts.nstatus and a.nsitecode=" + userInfo.getNmastersitecode() + " and a.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and a.nclientcode = " + nclientCode;
		return (Client) jdbcUtilityFunction.queryForObject(strQuery, Client.class, jdbcTemplate);
	}

	private ClientSiteAddress getClientSiteAddressByName(final String clientSiteAddressName, final int nclientCode,
			final int nmastersitecode) throws Exception {
		final String strQuery = "select  sclientsitename from clientsiteaddress where sclientsitename = N'"
				+ stringUtilityFunction.replaceQuote(clientSiteAddressName) + "' and nsitecode= " + nmastersitecode
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nclientcode = " + nclientCode;
		return (ClientSiteAddress) jdbcUtilityFunction.queryForObject(strQuery, ClientSiteAddress.class, jdbcTemplate);
	}

	private ClientSiteAddress getClientSiteAddressByDefault(final int nclientCode, final int nmastersitecode)
			throws Exception {
		final String strQuery = "select cd.nclientsitecode,cd.nclientcode,cd.ncountrycode,cd.sclientsitename,cd.saddress1,"
				+ " cd.saddress2,cd.saddress3,cd.ndefaultstatus,cd.nstatus from clientsiteaddress cd"
				+ " where cd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and cd.ndefaultstatus=" + Enumeration.TransactionStatus.YES.gettransactionstatus()
				+ " and cd.nclientcode = " + nclientCode + " and cd.nsitecode=" + nmastersitecode;
		return (ClientSiteAddress) jdbcUtilityFunction.queryForObject(strQuery, ClientSiteAddress.class, jdbcTemplate);
	}

	public List<ClientSiteAddress> getClientSiteAddressDetails(final UserInfo userInfo, final int nclientCode)
			throws Exception {
		final String strSiteQuery = " select cd.nclientsitecode,cd.nclientcode,cd.ncountrycode,cd.sclientsitename, "
				+ "cd.saddress1,cd.saddress2,cd.saddress3,cd.ndefaultstatus,cd.nstatus,c.scountryname, "
				+ "coalesce(ts1.jsondata->'sactiondisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "', "
				+ "ts1.jsondata->'sactiondisplaystatus'->>'en-US') as defaultstatus "
				+ "from clientsiteaddress cd,country c, transactionstatus ts1 where ts1.ntranscode = cd.ndefaultstatus "
				+ "and cd.ncountrycode = c.ncountrycode and c.nsitecode=" + userInfo.getNmastersitecode()
				+ " and ts1.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and cd.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and cd.nclientcode = " + nclientCode + " order by cd.nclientsitecode ";
		final List<ClientSiteAddress> lstclientSiteAddress = jdbcTemplate.query(strSiteQuery, new ClientSiteAddress());
		return lstclientSiteAddress;
	}

	public List<ClientSiteAddress> getClientSiteAddress(final int nclientCode, final UserInfo userInfo)
			throws Exception {
		final String strQuery = " select cd.nclientsitecode,cd.nclientcode,cd.ncountrycode,cd.sclientsitename,cd.saddress1,"
				+ "cd.saddress2,cd.saddress3,cd.ndefaultstatus,cd.nstatus,c.scountryname,"
				+ "coalesce(ts.jsondata->'sactiondisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ "ts.jsondata->'sactiondisplaystatus'->>'en-US') as defaultstatus"
				+ " from clientsiteaddress cd,country c, transactionstatus ts where ts.ntranscode = cd.ndefaultstatus "
				+ " and cd.ncountrycode = c.ncountrycode and cd.nstatus = c.nstatus and cd.nstatus = ts.nstatus and cd.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + "and cd.nclientcode = "
				+ nclientCode + " and c.nsitecode=" + userInfo.getNmastersitecode();
		final List<ClientSiteAddress> lstclientSiteAddress = jdbcTemplate.query(strQuery, new ClientSiteAddress());
		return lstclientSiteAddress;
	}

	@Override
	public ClientSiteAddress getClientSiteAddressById(final int nclientCode, final int nclientSiteCode,
			final UserInfo userInfo) throws Exception {
		final String strQuery = " select cd.nclientsitecode,cd.nclientcode,cd.ncountrycode,cd.sclientsitename,"
				+ " cd.saddress1,cd.saddress2,cd.saddress3,cd.ndefaultstatus,cd.nstatus, c.scountryname,"
				+ " coalesce(ts.jsondata->'sactiondisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'sactiondisplaystatus'->>'en-US') as defaultstatus"
				+ " from clientsiteaddress cd, country c, transactionstatus ts "
				+ " where ts.ntranscode = cd.ndefaultstatus and cd.ncountrycode = c.ncountrycode"
				+ " and cd.nstatus = c.nstatus and cd.nstatus = ts.nstatus and cd.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and cd.nsitecode="
				+ userInfo.getNmastersitecode() + " and cd.nclientcode = " + nclientCode + " and cd.nclientsitecode = "
				+ nclientSiteCode;
		return (ClientSiteAddress) jdbcUtilityFunction.queryForObject(strQuery, ClientSiteAddress.class, jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> updateClientSiteAddress(final ClientSiteAddress clientSiteAddress,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		final Client objClient = getClientByIdForInsert(clientSiteAddress.getNclientcode(), userInfo);

		if (objClient == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_CLIENTALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final ClientSiteAddress clientSiteAddressByID = getClientSiteAddressById(clientSiteAddress.getNclientcode(),
					clientSiteAddress.getNclientsitecode(), userInfo);

			final List<String> multilingualIDList = new ArrayList<>();
			final List<Object> listAfterUpdate = new ArrayList<>();
			final List<Object> listBeforeUpdate = new ArrayList<>();

			if (clientSiteAddressByID == null) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				final String queryString = "select sclientsitename from clientsiteaddress where sclientsitename = N'"
						+ stringUtilityFunction.replaceQuote(clientSiteAddress.getSclientsitename())
						+ "' and nclientcode = " + clientSiteAddress.getNclientcode() + " and nclientsitecode <> "
						+ clientSiteAddress.getNclientsitecode() + " and nsitecode=" + userInfo.getNmastersitecode()
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

				final List<ClientSiteAddress> objClientSiteAddress = jdbcTemplate.query(queryString,
						new ClientSiteAddress());

				if (clientSiteAddressByID.getNdefaultstatus() == Enumeration.TransactionStatus.YES
						.gettransactionstatus()
						&& clientSiteAddressByID.getNdefaultstatus() != clientSiteAddress.getNdefaultstatus()) {

					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.DEFAULTCANNOTCHANGED.getreturnstatus(),
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);

				} else {
					if (objClientSiteAddress.isEmpty()) {
						final ClientSiteAddress defaultClientSiteAddress = getClientSiteAddressByDefault(
								clientSiteAddress.getNclientcode(), userInfo.getNmastersitecode());

						if (clientSiteAddress.getNdefaultstatus() == Enumeration.TransactionStatus.YES
								.gettransactionstatus()) {

							if (defaultClientSiteAddress != null && defaultClientSiteAddress
									.getNclientsitecode() != clientSiteAddress.getNclientsitecode()) {

								final ClientSiteAddress clientSiteAddressBeforeSave = SerializationUtils
										.clone(defaultClientSiteAddress);

								listBeforeUpdate.add(clientSiteAddressBeforeSave);

								defaultClientSiteAddress.setNdefaultstatus(
										(short) Enumeration.TransactionStatus.NO.gettransactionstatus());

								final String updateQueryString = " update clientsiteaddress set ndefaultstatus="
										+ Enumeration.TransactionStatus.NO.gettransactionstatus()
										+ ", dmodifieddate = '" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'"
										+ " where nclientsitecode =" + defaultClientSiteAddress.getNclientsitecode()
										+ " and nsitecode=" + userInfo.getNmastersitecode();
								jdbcTemplate.execute(updateQueryString);

								listAfterUpdate.add(defaultClientSiteAddress);
								multilingualIDList.add("IDS_EDITCLIENTSITE");

							}
						}
						final String updateQueryString = "update clientsiteaddress set sclientsitename=N'"
								+ stringUtilityFunction.replaceQuote(clientSiteAddress.getSclientsitename())
								+ "', saddress1 =N'"
								+ stringUtilityFunction.replaceQuote(clientSiteAddress.getSaddress1())
								+ "',  saddress2 =N'"
								+ stringUtilityFunction.replaceQuote(clientSiteAddress.getSaddress2())
								+ "',saddress3 = N'"
								+ stringUtilityFunction.replaceQuote(clientSiteAddress.getSaddress3())
								+ "', nclientcode= " + clientSiteAddress.getNclientcode() + ",ncountrycode= "
								+ clientSiteAddress.getNcountrycode() + ", ndefaultstatus= "
								+ clientSiteAddress.getNdefaultstatus() + ", dmodifieddate = '"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where nclientcode= "
								+ clientSiteAddress.getNclientcode() + " and nclientsitecode="
								+ clientSiteAddress.getNclientsitecode() + " and nsitecode="
								+ userInfo.getNmastersitecode();

						jdbcTemplate.execute(updateQueryString);

						multilingualIDList.add("IDS_EDITCLIENTSITE");

						listAfterUpdate.add(clientSiteAddress);

						listBeforeUpdate.add(clientSiteAddressByID);

						auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate,
								multilingualIDList, userInfo);
						final List<ClientSiteAddress> lstClientSiteAddress = getClientSiteAddress(
								clientSiteAddress.getNclientcode(), userInfo);

						final List<ClientContactInfo> lstClientContactDetails = getClientContactInfo(
								clientSiteAddress.getNclientcode(), clientSiteAddress.getNclientsitecode(), userInfo);

						final ClientSiteAddress selectedClientSiteAddress = getClientSiteAddressById(
								clientSiteAddress.getNclientcode(), clientSiteAddress.getNclientsitecode(), userInfo);

						outputMap.put("ClientContact", lstClientContactDetails);
						outputMap.put("ClientSite", lstClientSiteAddress);
						outputMap.put("selectedClientSite", selectedClientSiteAddress);
					} else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(
								Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
					}
				}
			}
			return new ResponseEntity<>(outputMap, HttpStatus.OK);
		}
	}

	@Override
	public ResponseEntity<Object> deleteClientSiteAddress(final ClientSiteAddress clientSiteAddress,
			final UserInfo userInfo) throws Exception {

		final Client objClient = getClientByIdForInsert(clientSiteAddress.getNclientcode(), userInfo);
		if (objClient == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final ClientSiteAddress clientSiteAddressByID = getClientSiteAddressById(clientSiteAddress.getNclientcode(),
					clientSiteAddress.getNclientsitecode(), userInfo);

			if (clientSiteAddressByID == null) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				if (clientSiteAddress.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.DEFAULTCANNOTDELETE.getreturnstatus(),
							userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				} else {

					final String query = "select 'IDS_QUOTATION' as Msg from quotation where nclientsitecode = "
							+ clientSiteAddressByID.getNclientsitecode() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
							+ userInfo.getNtranssitecode();

					validatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);
					boolean validRecord = false;
					if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {

						validRecord = true;
						final Map<String, Object> objOneToManyValidation = new HashMap<String, Object>();
						objOneToManyValidation.put("primaryKeyValue",
								Integer.toString(clientSiteAddress.getNclientsitecode()));
						objOneToManyValidation.put("stablename", "clientsiteaddress");

						validatorDel = projectDAOSupport.validateOneToManyDeletion(objOneToManyValidation, userInfo);
						if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
							validRecord = true;
						} else {
							validRecord = false;
						}
					}
					if (validRecord) {

						final List<String> multilingualIDList = new ArrayList<>();
						final List<Object> savedClientSiteAddressList = new ArrayList<>();
						String updateQueryString = "update clientsiteaddress set nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate = '"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where nclientcode="
								+ clientSiteAddress.getNclientcode() + " and nclientsitecode="
								+ clientSiteAddress.getNclientsitecode() + " and nsitecode="
								+ userInfo.getNmastersitecode() + ";";

						jdbcTemplate.execute(updateQueryString);

						clientSiteAddress
								.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

						multilingualIDList.add("IDS_DELETECLIENTSITE");
						savedClientSiteAddressList.add(Arrays.asList(clientSiteAddress));

						final String sQuery = "select nclientcontactcode,nclientsitecode,nclientcode,scontactname,"
								+ " sphoneno,smobileno,semail,sfaxno,scomments,ndefaultstatus,nstatus from clientcontactinfo "
								+ " where nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and nsitecode=" + userInfo.getNmastersitecode() + " and nclientsitecode = "
								+ clientSiteAddress.getNclientsitecode();
						final List<ClientContactInfo> deletedContacts = jdbcTemplate.query(sQuery,
								new ClientContactInfo());

						updateQueryString += " update clientcontactinfo set nstatus = "
								+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate = '"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where nclientsitecode = "
								+ clientSiteAddress.getNclientsitecode() + " and nstatus = "
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
								+ userInfo.getNmastersitecode();

						jdbcTemplate.execute(updateQueryString);
						multilingualIDList.add("IDS_DELETECLIENTCONTACT");
						savedClientSiteAddressList.add(deletedContacts);

						auditUtilityFunction.fnInsertListAuditAction(savedClientSiteAddressList, 1, null,
								multilingualIDList, userInfo);

						return getClientByClient(clientSiteAddress.getNclientcode(), userInfo);
					} else {
						return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
					}
				}
			}
		}
	}

	@Override
	public ResponseEntity<Object> createClientContactInfo(final ClientContactInfo objClientContactInfo,
			final UserInfo userInfo) throws Exception {

		final String sQuery = " lock  table lockclient " + Enumeration.ReturnStatus.TABLOCK.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedClientContactInfoList = new ArrayList<>();
		final Client objClient = getClientByIdForInsert(objClientContactInfo.getNclientcode(), userInfo);

		if (objClient == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_CLIENTALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			final ClientSiteAddress clientSiteAddressByID = getClientSiteAddressById(
					objClientContactInfo.getNclientcode(), objClientContactInfo.getNclientsitecode(), userInfo);

			if (clientSiteAddressByID == null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CLIENTSITEALREADYDELETE",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else {

				final ClientContactInfo clientContactInfoByName = getClientContactByName(
						objClientContactInfo.getNclientcode(), objClientContactInfo.getScontactname(),
						objClientContactInfo.getNclientsitecode(), userInfo.getNmastersitecode());

				if (clientContactInfoByName == null) {
					if (objClientContactInfo.getNdefaultstatus() == Enumeration.TransactionStatus.YES
							.gettransactionstatus()) {

						final ClientContactInfo defaultClientContactInfo = getClientContactByDefault(
								objClientContactInfo.getNclientcode(), objClientContactInfo.getNclientsitecode(),
								userInfo.getNmastersitecode());

						if (defaultClientContactInfo != null) {

							final ClientContactInfo clientContactInfoBeforeSave = SerializationUtils
									.clone(defaultClientContactInfo);

							final List<Object> defaultListBeforeSave = new ArrayList<>();
							defaultListBeforeSave.add(clientContactInfoBeforeSave);

							defaultClientContactInfo
									.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());

							final String updateQueryString = " update clientcontactinfo set ndefaultstatus="
									+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", dmodifieddate = '"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where nclientcode ="
									+ defaultClientContactInfo.getNclientcode() + " and nclientsitecode = "
									+ defaultClientContactInfo.getNclientsitecode() + " and nsitecode="
									+ userInfo.getNmastersitecode();
							jdbcTemplate.execute(updateQueryString);

							final List<Object> defaultListAfterSave = new ArrayList<>();
							defaultListAfterSave.add(defaultClientContactInfo);

							multilingualIDList.add("IDS_EDITCLIENTCONTACT");

							auditUtilityFunction.fnInsertAuditAction(defaultListAfterSave, 2, defaultListBeforeSave,
									multilingualIDList, userInfo);
							multilingualIDList.clear();
						}

					}

					String clientContactInfoSeq = "";
					clientContactInfoSeq = "select nsequenceno from seqnocontactmaster where stablename='clientcontactinfo' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					int seqNo = jdbcTemplate.queryForObject(clientContactInfoSeq, Integer.class);
					seqNo = seqNo + 1;
					String clientContactInfoInsert = "";

					final String phoneno = objClientContactInfo.getSphoneno() == null ? ""
							: objClientContactInfo.getSphoneno();
					final String mobileno = objClientContactInfo.getSmobileno() == null ? ""
							: objClientContactInfo.getSmobileno();
					final String email = objClientContactInfo.getSemail() == null ? ""
							: objClientContactInfo.getSemail();
					final String faxno = objClientContactInfo.getSfaxno() == null ? ""
							: objClientContactInfo.getSfaxno();
					final String comments = objClientContactInfo.getScomments() == null ? ""
							: objClientContactInfo.getScomments();

					clientContactInfoInsert = "insert into clientcontactinfo(nclientcontactcode,nclientsitecode,nclientcode,scontactname,"
							+ "sphoneno,smobileno,semail,sfaxno,scomments,ndefaultstatus,dmodifieddate,nsitecode,nstatus)values ("
							+ seqNo + "," + objClientContactInfo.getNclientsitecode() + "," + ""
							+ objClientContactInfo.getNclientcode() + ",'"
							+ stringUtilityFunction.replaceQuote(objClientContactInfo.getScontactname()) + "','"
							+ stringUtilityFunction.replaceQuote(phoneno) + "'," + "'"
							+ stringUtilityFunction.replaceQuote(mobileno) + "','"
							+ stringUtilityFunction.replaceQuote(email) + "','"
							+ stringUtilityFunction.replaceQuote(faxno) + "','"
							+ stringUtilityFunction.replaceQuote(comments) + "'," + ""
							+ objClientContactInfo.getNdefaultstatus() + ", '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + userInfo.getNmastersitecode()
							+ "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";
					jdbcTemplate.execute(clientContactInfoInsert);

					clientContactInfoSeq = "update seqnocontactmaster set nsequenceno=" + seqNo
							+ " where stablename='clientcontactinfo'";
					jdbcTemplate.execute(clientContactInfoSeq);

					multilingualIDList.add("IDS_ADDCLIENTCONTACT");
					objClientContactInfo.setNclientcontactcode(seqNo);
					savedClientContactInfoList.add(objClientContactInfo);

					auditUtilityFunction.fnInsertAuditAction(savedClientContactInfoList, 1, null, multilingualIDList,
							userInfo);

					final List<ClientContactInfo> lstClientContactDetails = getClientContactInfo(
							objClientContactInfo.getNclientcode(), objClientContactInfo.getNclientsitecode(), userInfo);
					outputMap.put("ClientContact", lstClientContactDetails);

					if (objClientContactInfo.isIsreadonly() != true) {
						return new ResponseEntity<>(outputMap, HttpStatus.OK);
					} else {
						return getClientByCategory(objClientContactInfo.getNclientcatcode(), userInfo);
					}
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.CONFLICT);
				}
			}
		}
	}

	private ClientContactInfo getClientContactByDefault(final int nclientCode, final int nclientSiteCode,
			final int nmastersitecode) throws Exception {
		final String strQuery = "select cc.nclientcontactcode,cc.nclientsitecode,cc.nclientcode,cc.scontactname,cc.sphoneno,"
				+ " cc.smobileno,cc.semail,cc.sfaxno,cc.scomments,cc.ndefaultstatus,cc.nstatus from clientcontactinfo cc"
				+ " where cc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and cc.ndefaultstatus=" + Enumeration.TransactionStatus.YES.gettransactionstatus()
				+ " and cc.nclientsitecode = " + nclientSiteCode + " and cc.nclientcode = " + nclientCode
				+ " and cc.nsitecode=" + nmastersitecode;
		return (ClientContactInfo) jdbcUtilityFunction.queryForObject(strQuery, ClientContactInfo.class, jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> updateClientContactInfo(final ClientContactInfo objClientContactInfo,
			final UserInfo userInfo) throws Exception {
		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final Client objClient = getClientByIdForInsert(objClientContactInfo.getNclientcode(), userInfo);
		if (objClient == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_CLIENTALREADYDELETED", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final ClientSiteAddress clientSiteAddressByID = getClientSiteAddressById(
					objClientContactInfo.getNclientcode(), objClientContactInfo.getNclientsitecode(), userInfo);
			if (clientSiteAddressByID == null) {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage("IDS_SITEALREADYDELETE", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			} else {
				final ClientContactInfo clientContactInfoByID = getClientContactInfoById(
						objClientContactInfo.getNclientcode(), objClientContactInfo.getNclientsitecode(),
						objClientContactInfo.getNclientcontactcode(), userInfo.getNmastersitecode());
				final List<Object> listAfterUpdate = new ArrayList<>();
				final List<Object> listBeforeUpdate = new ArrayList<>();
				final List<String> multilingualIDList = new ArrayList<>();
				if (clientContactInfoByID == null) {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				} else {
					final String queryString = "select scontactname from clientcontactinfo where scontactname = N'"
							+ stringUtilityFunction.replaceQuote(objClientContactInfo.getScontactname())
							+ "' and nclientcode = " + objClientContactInfo.getNclientcode() + " and nclientsitecode = "
							+ objClientContactInfo.getNclientsitecode() + " and nclientcontactcode <> "
							+ objClientContactInfo.getNclientcontactcode() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
							+ userInfo.getNmastersitecode();

					final List<ClientContactInfo> clientContactInfo = jdbcTemplate.query(queryString,
							new ClientContactInfo());

					if (clientContactInfoByID.getNdefaultstatus() == Enumeration.TransactionStatus.YES
							.gettransactionstatus()
							&& clientContactInfoByID.getNdefaultstatus() != objClientContactInfo.getNdefaultstatus()) {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(
								Enumeration.ReturnStatus.DEFAULTCANNOTCHANGED.getreturnstatus(),
								userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}

					else {
						if (clientContactInfo.isEmpty()) {
							if (objClientContactInfo.getNdefaultstatus() == Enumeration.TransactionStatus.YES
									.gettransactionstatus()) {

								final ClientContactInfo defaultClientContactInfo = getClientContactByDefault(
										objClientContactInfo.getNclientcode(),
										objClientContactInfo.getNclientsitecode(), userInfo.getNmastersitecode());

								if (defaultClientContactInfo != null && (defaultClientContactInfo
										.getNclientcontactcode() != objClientContactInfo.getNclientcontactcode())) {

									final ClientContactInfo clientContactBeforeSave = SerializationUtils
											.clone(defaultClientContactInfo);

									listBeforeUpdate.add(clientContactBeforeSave);

									defaultClientContactInfo.setNdefaultstatus(
											(short) Enumeration.TransactionStatus.NO.gettransactionstatus());

									final String updateQueryString = " update clientcontactinfo set ndefaultstatus="
											+ Enumeration.TransactionStatus.NO.gettransactionstatus()
											+ ", dmodifieddate = '" + dateUtilityFunction.getCurrentDateTime(userInfo)
											+ "'" + " where nclientcontactcode ="
											+ defaultClientContactInfo.getNclientcontactcode();
									jdbcTemplate.execute(updateQueryString);

									listAfterUpdate.add(defaultClientContactInfo);
									multilingualIDList.add("IDS_EDITCLIENTCONTACT");
								}

							}

							final String updateQueryString = "update clientcontactinfo set scontactname=N'"
									+ stringUtilityFunction.replaceQuote(objClientContactInfo.getScontactname())
									+ "', sphoneno =N'"
									+ stringUtilityFunction.replaceQuote(objClientContactInfo.getSphoneno())
									+ "',  smobileno =N'"
									+ stringUtilityFunction.replaceQuote(objClientContactInfo.getSmobileno())
									+ "',semail = N'"
									+ stringUtilityFunction.replaceQuote(objClientContactInfo.getSemail())
									+ "', sfaxno = N'"
									+ stringUtilityFunction.replaceQuote(objClientContactInfo.getSfaxno())
									+ "', scomments = N'"
									+ stringUtilityFunction.replaceQuote(objClientContactInfo.getScomments())
									+ "', nclientcode= " + objClientContactInfo.getNclientcode() + ",nclientsitecode= "
									+ objClientContactInfo.getNclientsitecode() + ", ndefaultstatus= "
									+ objClientContactInfo.getNdefaultstatus() + ", dmodifieddate = '"
									+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' "
									+ " where nclientcontactcode=" + objClientContactInfo.getNclientcontactcode()
									+ " and nsitecode=" + userInfo.getNmastersitecode();

							jdbcTemplate.execute(updateQueryString);

							multilingualIDList.add("IDS_EDITCLIENTCONTACT");

							listAfterUpdate.add(objClientContactInfo);

							listBeforeUpdate.add(clientContactInfoByID);

							final List<ClientContactInfo> lstClientContactInfo = getClientContactInfo(
									objClientContactInfo.getNclientcode(), objClientContactInfo.getNclientsitecode(),
									userInfo);
							outputMap.put("ClientContact", lstClientContactInfo);
							outputMap.put("selectedClientContact", getClientContactInfoById(
									objClientContactInfo.getNclientcode(), objClientContactInfo.getNclientsitecode(),
									objClientContactInfo.getNclientcontactcode(), userInfo.getNmastersitecode()));

							auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate,
									multilingualIDList, userInfo);
						} else {
							return new ResponseEntity<>(commonFunction.getMultilingualMessage(
									Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
									userInfo.getSlanguagefilename()), HttpStatus.CONFLICT);
						}
					}
				}
			}
			return new ResponseEntity<>(outputMap, HttpStatus.OK);

		}

	}

	@Override
	public ResponseEntity<Object> deleteClientContactInfo(final ClientContactInfo objClientContactInfo,
			final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();

		final ClientContactInfo clientContactInfoByID = getClientContactInfoById(objClientContactInfo.getNclientcode(),
				objClientContactInfo.getNclientsitecode(), objClientContactInfo.getNclientcontactcode(),
				userInfo.getNmastersitecode());

		if (clientContactInfoByID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			if (clientContactInfoByID.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.DEFAULTCANNOTDELETE.getreturnstatus(),
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			} else {
				final String query = "select 'IDS_QUOTATION' as Msg from quotation where nclientcontactcode = "
						+ objClientContactInfo.getNclientcontactcode() + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
						+ userInfo.getNtranssitecode();

				validatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);

				if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					final List<String> multilingualIDList = new ArrayList<>();
					final List<Object> savedClientContactList = new ArrayList<>();
					final String updateQueryString = "update clientcontactinfo set nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate = '"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'" + " where nclientcontactcode="
							+ objClientContactInfo.getNclientcontactcode() + " and nclientcode="
							+ objClientContactInfo.getNclientcode() + " and nclientsitecode="
							+ objClientContactInfo.getNclientsitecode();

					jdbcTemplate.execute(updateQueryString);

					objClientContactInfo
							.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());

					savedClientContactList.add(objClientContactInfo);
					multilingualIDList.add("IDS_DELETECLIENTCONTACT");

					auditUtilityFunction.fnInsertAuditAction(savedClientContactList, 1, null, multilingualIDList,
							userInfo);
					final List<ClientContactInfo> objClientContactDetails = getClientContactInfo(
							objClientContactInfo.getNclientcode(), objClientContactInfo.getNclientsitecode(), userInfo);
					outputMap.put("ClientContact", objClientContactDetails);
					return new ResponseEntity<>(outputMap, HttpStatus.OK);
				} else {
					return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);
				}
			}
		}
	}

	private ClientContactInfo getClientContactByName(final int nclientCode, final String clientContactName,
			final int nclientSiteCode, final int nmastersitecode) throws Exception {
		final String strQuery = "select  scontactname from clientcontactinfo where scontactname = N'"
				+ stringUtilityFunction.replaceQuote(clientContactName) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nclientsitecode ="
				+ nclientSiteCode + " and nclientcode = " + nclientCode + " and nsitecode=" + nmastersitecode;
		return (ClientContactInfo) jdbcUtilityFunction.queryForObject(strQuery, ClientContactInfo.class, jdbcTemplate);
	}

	@Override
	public List<ClientContactInfo> getClientContactInfoBySite(final int nclientCode, final int nclientSiteCode,
			final UserInfo userInfo) throws Exception {
		final String strContactQuery = " select cc.nclientcontactcode,cc.nclientsitecode,cc.nclientcode,cc.scontactname,cc.sphoneno,cc.smobileno,"
				+ " cc.semail,cc.sfaxno,cc.scomments,cc.ndefaultstatus,cc.nstatus,"
				+ " coalesce(ts.jsondata->'sactiondisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "',"
				+ " ts.jsondata->'sactiondisplaystatus'->>'en-US') as sdefaultContact"
				+ " from clientcontactinfo cc, clientsiteaddress cd, transactionstatus ts where cc.ndefaultstatus = ts.ntranscode "
				+ "and cc.nclientsitecode = cd.nclientsitecode and cc.nstatus = cd.nstatus  and cc.nstatus = ts.nstatus and cc.nsitecode="
				+ userInfo.getNmastersitecode() + " and cc.nsitecode=cd.nsitecode and cc.nclientcode = " + nclientCode
				+ " and cc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and cc.nclientsitecode = " + nclientSiteCode + " order by cc.nclientcontactcode";
		final List<ClientContactInfo> lstClientContactInfo = jdbcTemplate.query(strContactQuery,
				new ClientContactInfo());
		return lstClientContactInfo;
	}

	@Override
	public ClientContactInfo getClientContactInfoById(final int nclientCode, final int nclientSiteCode,
			final int nclientContactCode, final int nmastersitecode) throws Exception {
		final String strQuery = " select nclientcontactcode,nclientsitecode,nclientcode,scontactname,sphoneno,smobileno,semail,sfaxno,"
				+ " scomments,ndefaultstatus,nstatus from clientcontactinfo where  nclientsitecode = " + nclientSiteCode
				+ " and nclientcontactcode = " + nclientContactCode + " and nsitecode=" + nmastersitecode
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nclientcode = " + nclientCode;
		return (ClientContactInfo) jdbcUtilityFunction.queryForObject(strQuery, ClientContactInfo.class, jdbcTemplate);

	}

	public List<ClientContactInfo> getClientContactInfo(final int nclientCode, final int nclientSiteCode,
			final UserInfo userInfo) throws Exception {
		final String strQuery = " select cc.nclientcontactcode,cc.nclientsitecode,cc.nclientcode,cc.scontactname,"
				+ "cc.sphoneno,cc.smobileno,cc.semail,cc.sfaxno,cc.scomments,cc.ndefaultstatus,cc.nstatus ,"
				+ "coalesce(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "',ts.jsondata->'stransdisplaystatus'->>'en-US') as sdefaultContact"
				+ " from clientcontactinfo cc,transactionstatus ts where  cc.ndefaultstatus =ts.ntranscode and "
				+ " cc.nclientsitecode =" + nclientSiteCode + " and cc.nclientcode=" + nclientCode + " and"
				+ " cc.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and ts.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode="
				+ userInfo.getNmastersitecode();
		return jdbcTemplate.query(strQuery, new ClientContactInfo());
	}

	@Override
	public ResponseEntity<Object> getSelectedClientDetail(final UserInfo userInfo, final int nclientCode)
			throws Exception {
		final Client lstClient = getActiveClientById(nclientCode, userInfo);
		if (lstClient != null) {
			final Map<String, Object> objMap = new LinkedHashMap<String, Object>();
			final List<ClientSiteAddress> lstClientSite = getClientSiteAddressDetails(userInfo, nclientCode);
			objMap.putAll(getClientFile(nclientCode, userInfo).getBody());
			objMap.put("ClientSite", lstClientSite);
			if (lstClientSite.size() > 0) {
				objMap.put("selectedClientSite", lstClientSite.get(lstClientSite.size() - 1));
				final int nclientSiteCode = lstClientSite.get(lstClientSite.size() - 1).getNclientsitecode();
				final List<ClientContactInfo> lstClientContactGet = getClientContactInfoBySite(nclientCode,
						nclientSiteCode, userInfo);
				objMap.put("ClientContact", lstClientContactGet);
			} else {
				objMap.put("ClientSite", null);
				objMap.put("ClientContact", null);
			}
			final Client lstClientGet = getClientByIdForInsert(nclientCode, userInfo);

			objMap.put("selectedClient", lstClientGet);

			return new ResponseEntity<Object>(objMap, HttpStatus.OK);
		} else {
			final String returnString = commonFunction.getMultilingualMessage(
					Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), userInfo.getSlanguagefilename());
			return new ResponseEntity<>(returnString, HttpStatus.EXPECTATION_FAILED);
		}

	}

	private ResponseEntity<Map<String, Object>> getClientFile(final int nclientcode, final UserInfo objUserInfo)
			throws Exception {
		final Map<String, Object> outputMap = new HashMap<String, Object>();
		final String query = "select tf.noffsetdcreateddate,tf.nclientfilecode,(select  count(nclientfilecode) from clientfile where nclientfilecode>0 and nclientcode = "
				+ nclientcode + " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ ") as ncount,tf.sdescription,"
				+ " tf.nclientfilecode as nprimarycode,tf.sfilename,tf.nclientcode,tf.ssystemfilename,"
				+ " tf.nattachmenttypecode,coalesce(at.jsondata->'sattachmenttype'->>'"
				+ objUserInfo.getSlanguagetypecode() + "',"
				+ "	at.jsondata->'sattachmenttype'->>'en-US') as sattachmenttype, case when tf.nlinkcode=-1 then '-' else lm.jsondata->>'slinkname'"
				+ " end slinkname, tf.nfilesize," + " case when tf.nattachmenttypecode= "
				+ Enumeration.AttachmentType.LINK.gettype() + " then '-' else" + " COALESCE(TO_CHAR(tf.dcreateddate,'"
				+ objUserInfo.getSpgdatetimeformat() + "'),'-') end  as screateddate, "
				+ " tf.nlinkcode, case when tf.nlinkcode = -1 then tf.nfilesize::varchar(1000) else '-' end sfilesize"
				+ " from clientfile tf,attachmenttype at, linkmaster lm  "
				+ " where at.nattachmenttypecode = tf.nattachmenttypecode and at.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and lm.nlinkcode = tf.nlinkcode and lm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tf.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tf.nclientcode=" + nclientcode
				+ " order by tf.nclientfilecode;";
		final ObjectMapper objMapper = new ObjectMapper();
		objMapper.registerModule(new JavaTimeModule());
		final List<ClientFile> clientFile = jdbcTemplate.query(query, new ClientFile());
		outputMap.put("clientFile", dateUtilityFunction.getSiteLocalTimeFromUTC(clientFile,
				Arrays.asList("screateddate"), null, objUserInfo, false, null, false));
		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	public Client checkClientIsPresent(final int nclientcode) throws Exception {
		final String strQuery = "select nclientcode from client where nclientcode = " + nclientcode + " and  nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final Client objClient = (Client) jdbcUtilityFunction.queryForObject(strQuery, Client.class, jdbcTemplate);
		return objClient;
	}

	@Override
	public ResponseEntity<? extends Object> createClientFile(final MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();

		final String sQuery = " lock  table clientfile " + Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);

		final List<ClientFile> lstReqClientFile = objMapper.readValue(request.getParameter("clientfile"),
				new TypeReference<List<ClientFile>>() {
				});

		if (lstReqClientFile != null && lstReqClientFile.size() > 0) {
			final Client objClient = checkClientIsPresent(lstReqClientFile.get(0).getNclientcode());

			if (objClient != null) {
				String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();
				if (lstReqClientFile.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {

					sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, objUserInfo);
				}

				if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(sReturnString)) {

					final Instant instantDate = dateUtilityFunction.getCurrentDateTime(objUserInfo)
							.truncatedTo(ChronoUnit.SECONDS);
					final String sattachmentDate = dateUtilityFunction.instantDateToString(instantDate);
					final int noffset = dateUtilityFunction.getCurrentDateTimeOffset(objUserInfo.getStimezoneid());

					lstReqClientFile.forEach(objtf -> {
						objtf.setDcreateddate(instantDate);
						if (objtf.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
							objtf.setDcreateddate(instantDate);
							objtf.setNoffsetdcreateddate(noffset);
							objtf.setScreateddate(sattachmentDate.replace("T", " "));
						}

					});

					final String sequencequery = "select nsequenceno from seqnocontactmaster where stablename ='clientfile' and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					int nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);
					nsequenceno++;
					final String insertquery = "Insert into clientfile(nclientfilecode,nclientcode,nlinkcode,nattachmenttypecode,sfilename,sdescription,nfilesize,dcreateddate,noffsetdcreateddate,ntzcreateddate,ssystemfilename,dmodifieddate,nsitecode,nstatus)"
							+ "values (" + nsequenceno + "," + lstReqClientFile.get(0).getNclientcode() + ","
							+ lstReqClientFile.get(0).getNlinkcode() + ","
							+ lstReqClientFile.get(0).getNattachmenttypecode() + "," + " N'"
							+ stringUtilityFunction.replaceQuote(lstReqClientFile.get(0).getSfilename()) + "',N'"
							+ stringUtilityFunction.replaceQuote(lstReqClientFile.get(0).getSdescription()) + "',"
							+ lstReqClientFile.get(0).getNfilesize() + "," + " '"
							+ lstReqClientFile.get(0).getDcreateddate() + "',"
							+ lstReqClientFile.get(0).getNoffsetdcreateddate() + "," + objUserInfo.getNtimezonecode()
							+ ",N'" + lstReqClientFile.get(0).getSsystemfilename() + "','"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "',"
							+ objUserInfo.getNmastersitecode() + ","
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ")";

					jdbcTemplate.execute(insertquery);

					final String updatequery = "update seqnocontactmaster set nsequenceno =" + nsequenceno
							+ " where stablename ='clientfile'";
					jdbcTemplate.execute(updatequery);

					final List<String> multilingualIDList = new ArrayList<>();

					multilingualIDList.add(
							lstReqClientFile.get(0).getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
									? "IDS_ADDCLIENTFILE"
									: "IDS_ADDCLIENTLINK");
					final List<Object> listObject = new ArrayList<Object>();
					final String auditqry = "select * from clientfile where nclientcode = "
							+ lstReqClientFile.get(0).getNclientcode() + " and nclientfilecode = " + nsequenceno
							+ " and nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					final List<ClientFile> lstvalidate = jdbcTemplate.query(auditqry, new ClientFile());

					listObject.add(lstvalidate);

					auditUtilityFunction.fnInsertListAuditAction(listObject, 1, null, multilingualIDList, objUserInfo);
				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(sReturnString, objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CLIENTALREADYDELETED",
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
			return getClientFile(lstReqClientFile.get(0).getNclientcode(), objUserInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<? extends Object> updateClientFile(final MultipartHttpServletRequest request,
			final UserInfo objUserInfo) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();

		final List<ClientFile> lstClientFile = objMapper.readValue(request.getParameter("clientfile"),
				new TypeReference<List<ClientFile>>() {
				});
		if (lstClientFile != null && lstClientFile.size() > 0) {
			final ClientFile objClientFile = lstClientFile.get(0);
			final Client objClient = checkClientIsPresent(objClientFile.getNclientcode());

			if (objClient != null) {
				final int isFileEdited = Integer.valueOf(request.getParameter("isFileEdited"));
				String sReturnString = Enumeration.ReturnStatus.SUCCESS.getreturnstatus();

				if (isFileEdited == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					if (objClientFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
						sReturnString = ftpUtilityFunction.getFileFTPUpload(request, -1, objUserInfo);
					}
				}

				if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus().equals(sReturnString)) {
					final String sQuery = "select * from clientfile where nclientfilecode = "
							+ objClientFile.getNclientfilecode() + " and nstatus = "
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					final ClientFile objTF = (ClientFile) jdbcUtilityFunction.queryForObject(sQuery, ClientFile.class,
							jdbcTemplate);

					if (objTF != null) {
						String ssystemfilename = "";
						if (objClientFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
							
							String oldFileExtension = objTF.getSfilename().contains(".") ? 
									objTF.getSfilename().substring(objTF.getSfilename().lastIndexOf('.') + 1) : "";
							String newFileExtension = objClientFile.getSfilename().contains(".") ? 
									objClientFile.getSfilename().substring(objClientFile.getSfilename().lastIndexOf('.') + 1) : "";
							if(!oldFileExtension.equals(newFileExtension)) {
								ftpUtilityFunction.deleteFTPFile(Arrays.asList(objTF.getSsystemfilename()), "", objUserInfo);
							}
							
							ssystemfilename = objClientFile.getSsystemfilename();
						}

						final String sUpdateQuery = "update clientfile set sfilename=N'"
								+ stringUtilityFunction.replaceQuote(objClientFile.getSfilename()) + "',"
								+ " sdescription=N'"
								+ stringUtilityFunction.replaceQuote(objClientFile.getSdescription())
								+ "', ssystemfilename= N'" + ssystemfilename + "'," + " nattachmenttypecode = "
								+ objClientFile.getNattachmenttypecode() + ", nlinkcode=" + objClientFile.getNlinkcode()
								+ "," + " nfilesize = " + objClientFile.getNfilesize() + ",dmodifieddate='"
								+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "' where nclientfilecode = "
								+ objClientFile.getNclientfilecode();
						objClientFile.setDcreateddate(objTF.getDcreateddate());
						jdbcTemplate.execute(sUpdateQuery);

						final List<String> multilingualIDList = new ArrayList<>();
						final List<Object> lstOldObject = new ArrayList<Object>();
						multilingualIDList
								.add(objClientFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
										? "IDS_EDITCLIENTFILE"
										: "IDS_EDITCLIENTLINK");
						lstOldObject.add(objTF);

						auditUtilityFunction.fnInsertAuditAction(lstClientFile, 2, lstOldObject, multilingualIDList,
								objUserInfo);
						return (getClientFile(objClientFile.getNclientcode(), objUserInfo));
					} else {
						return new ResponseEntity<>(commonFunction.getMultilingualMessage(
								Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
					}
				} else {
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage(sReturnString, objUserInfo.getSlanguagefilename()),
							HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_CLIENTALREADYDELETED",
						objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_FILENOTFOUND", objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<? extends Object> deleteClientFile(final ClientFile objClientFile, final UserInfo objUserInfo)
			throws Exception {
		final Client client = checkClientIsPresent(objClientFile.getNclientcode());
		if (client != null) {
			if (objClientFile != null) {
				final String sQuery = "select * from clientfile where nclientfilecode = "
						+ objClientFile.getNclientfilecode() + " and nsitecode=" + objUserInfo.getNmastersitecode()
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				final ClientFile objTF = (ClientFile) jdbcUtilityFunction.queryForObject(sQuery, ClientFile.class,
						jdbcTemplate);
				if (objTF != null) {
					if (objClientFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
					} else {
						objClientFile.setScreateddate(null);
					}
					
					ftpUtilityFunction.deleteFTPFile(Arrays.asList(objTF.getSsystemfilename()), "", objUserInfo);
					
					final String sUpdateQuery = "update clientfile set dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(objUserInfo) + "', nstatus = "
							+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + " where nclientfilecode = "
							+ objClientFile.getNclientfilecode();
					jdbcTemplate.execute(sUpdateQuery);
					final List<String> multilingualIDList = new ArrayList<>();
					final List<Object> lstObject = new ArrayList<>();
					multilingualIDList
							.add(objClientFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
									? "IDS_DELETECLIENTFILE"
									: "IDS_DELETECLIENTLINK");
					lstObject.add(objClientFile);
					auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, multilingualIDList, objUserInfo);
				} else {
					return new ResponseEntity<>(commonFunction.getMultilingualMessage(
							Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
				}
			}
			return getClientFile(objClientFile.getNclientcode(), objUserInfo);
		} else {
			return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SUPPLIERALREADYDELETED",
					objUserInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public ResponseEntity<Object> editClientFile(final ClientFile objClientFile, final UserInfo objUserInfo)
			throws Exception {
		final String sEditQuery = "select tf.nclientfilecode, tf.nclientcode, tf.nlinkcode, tf.nattachmenttypecode, tf.sfilename, tf.sdescription, tf.nfilesize,"
				+ " tf.ssystemfilename,  lm.jsondata->>'slinkname' as slinkname"
				+ " from clientfile tf, linkmaster lm where lm.nlinkcode = tf.nlinkcode and tf.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and lm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and tf.nclientfilecode = "
				+ objClientFile.getNclientfilecode() + " and tf.nsitecode=" + objUserInfo.getNmastersitecode();
		final ClientFile objTF = (ClientFile) jdbcUtilityFunction.queryForObject(sEditQuery, ClientFile.class,
				jdbcTemplate);
		if (objTF != null) {
			return new ResponseEntity<Object>(objTF, HttpStatus.OK);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							objUserInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}
	}

	@Override
	public Map<String, Object> viewAttachedClientFile(final ClientFile objClientFile, final UserInfo objUserInfo)
			throws Exception {
		Map<String, Object> map = new HashMap<>();
		final Client objClient = checkClientIsPresent(objClientFile.getNclientcode());
		if (objClient != null) {

			String sQuery = "select * from clientfile where nclientfilecode = " + objClientFile.getNclientfilecode()
					+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			final ClientFile objTF = (ClientFile) jdbcUtilityFunction.queryForObject(sQuery, ClientFile.class,
					jdbcTemplate);
			if (objTF != null) {
				if (objTF.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()) {
					map = ftpUtilityFunction.FileViewUsingFtp(objTF.getSsystemfilename(), -1, objUserInfo, "", "");
				} else {
					sQuery = "select jsondata->>'slinkname' as slinkname from linkmaster where nlinkcode="
							+ objTF.getNlinkcode() + " and nstatus="
							+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					final LinkMaster objlinkmaster = (LinkMaster) jdbcUtilityFunction.queryForObject(sQuery,
							LinkMaster.class, jdbcTemplate);
					map.put("AttachLink", objlinkmaster.getSlinkname() + objTF.getSfilename());
					objClientFile.setScreateddate(null);
				}
				final List<String> multilingualIDList = new ArrayList<>();
				final List<Object> lstObject = new ArrayList<>();
				multilingualIDList
						.add(objClientFile.getNattachmenttypecode() == Enumeration.AttachmentType.FTP.gettype()
								? "IDS_VIEWCLIENTFILE"
								: "IDS_VIEWCLIENTLINK");
				lstObject.add(objClientFile);
				auditUtilityFunction.fnInsertAuditAction(lstObject, 1, null, multilingualIDList, objUserInfo);
			} else {
				map.put(Enumeration.ReturnStatus.RETURNSTRING.getreturnstatus(), commonFunction.getMultilingualMessage(
						Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(), objUserInfo.getSlanguagefilename()));
				return map;
			}
		}
		return map;
	}
}
