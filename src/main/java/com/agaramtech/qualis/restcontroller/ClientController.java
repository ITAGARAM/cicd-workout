package com.agaramtech.qualis.restcontroller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.agaramtech.qualis.contactmaster.model.Client;
import com.agaramtech.qualis.contactmaster.model.ClientContactInfo;
import com.agaramtech.qualis.contactmaster.model.ClientFile;
import com.agaramtech.qualis.contactmaster.model.ClientSiteAddress;
import com.agaramtech.qualis.contactmaster.service.client.ClientService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;

/**
 * This controller is used to dispatch the input request to its relevant method
 * to access the Client Master Service methods.
 * 
 * @author ATE090
 * @version
 * @since 26- Jun- 2020
 */
@RestController
@RequestMapping("/client")
public class ClientController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ClientController.class);

	private RequestContext requestContext;
	private final ClientService clientService;

	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * 
	 * @param requestContext RequestContext to hold the request
	 * @param clientService  ClientService
	 */
	public ClientController(RequestContext requestContext, ClientService clientService) {
		super();
		this.requestContext = requestContext;
		this.clientService = clientService;
	}

	/**
	 * This method is used to retrieve list of active countries for the specified
	 * site.
	 * 
	 * @param inputMap [Map] map object with "nsitecode" as key for which the list
	 *                 is to be fetched
	 * @return response object with list of active countries that are to be listed
	 *         for the specified site
	 */
	@PostMapping(value = "/getClient")
	public ResponseEntity<Object> getClient(@RequestBody Map<String, Object> inputMap) throws Exception {
		LOGGER.info("getClient");
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return clientService.getClient(userInfo);
	}

	/**
	 * This method is used to add new client for the specified Site.
	 * 
	 * @param inputMap [Map] object with keys of client entity.
	 * @return response entity of newly added client entity
	 */
	@PostMapping(value = "/createClient")
	public ResponseEntity<Object> createClient(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return clientService.createClient(inputMap, userInfo);
	}

	@PostMapping(value = "/getClientByCategory")
	public ResponseEntity<Object> getClientByCategory(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int nclientcatcode = (int) inputMap.get("nclientcatcode");
		requestContext.setUserInfo(userInfo);
		return clientService.getClientByCategory(nclientcatcode, userInfo);
	}

	/**
	 * This method is used to retrieve selected active edqm manufacturer detail.
	 * 
	 * @param inputMap [Map] map object with "nclientcode" and "userInfo" as keys
	 *                 for which the data is to be fetched
	 * @return response object with selected active edqm manufacturer
	 */
	@PostMapping(value = "/getActiveClientById")
	public ResponseEntity<Object> getActiveClientById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final int nclientcode = (Integer) inputMap.get("nclientcode");
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return clientService.getActiveClientById(nclientcode, userInfo);
	}

	/**
	 * This method is used to update client for the specified Site.
	 * 
	 * @param inputMap [Map] object with keys of client entity.
	 * @return response entity of updated client entity
	 */
	@PostMapping(value = "/updateClient")
	public ResponseEntity<Object> updateClient(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final Client client = objmapper.convertValue(inputMap.get("client"), Client.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return clientService.updateClient(client, userInfo);
	}

	/**
	 * This method is used to delete client for the specified Site.
	 * 
	 * @param mapObject [Map] object with keys of client entity.
	 * @return response entity of deleted client entity
	 */
	@PostMapping(value = "/deleteClient")
	public ResponseEntity<Object> deleteClient(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return clientService.deleteClient(inputMap, userInfo);
	}

	/**
	 * This method is used to delete client for the specified Site.
	 * 
	 * @param mapObject [Map] object with keys of client entity.
	 * @return response entity of deleted client entity
	 */
	@PostMapping(value = "/getActiveClient")
	public ResponseEntity<Object> getActiveClient(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return clientService.getActiveClient(userInfo);
	}

	@PostMapping(value = "/createClientSiteAddress")
	public ResponseEntity<Object> createClientSiteAddress(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final ClientSiteAddress clientSiteAddress = objmapper.convertValue(inputMap.get("clientsiteaddress"),
				ClientSiteAddress.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return clientService.createClientSiteAddress(clientSiteAddress, userInfo);
	}

	@PostMapping(value = "/getClientSiteAddressById")
	public ResponseEntity<Object> getClientSiteAddressById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final int nclientCode = (Integer) inputMap.get("nclientcode");
		final int nclientSiteCode = (Integer) inputMap.get("nclientsitecode");
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return clientService.getClientSiteAddressById(nclientCode, nclientSiteCode, userInfo);
	}

	@PostMapping(value = "/updateClientSiteAddress")
	public ResponseEntity<Object> updateClientSiteAddress(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final ClientSiteAddress clientSiteAddress = objmapper.convertValue(inputMap.get("clientsiteaddress"),
				ClientSiteAddress.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return clientService.updateClientSiteAddress(clientSiteAddress, userInfo);
	}

	/**
	 * This method is used to delete manufacturer site for the specified Site.
	 * 
	 * @param inputMap [Map] object with keys of "manufacturersiteaddress" and
	 *                 "userinfo" entities.
	 * @return response entity of newly added manufacturer entity
	 */
	@PostMapping(value = "/deleteClientSiteAddress")
	public ResponseEntity<Object> deleteClientSiteAddress(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final ClientSiteAddress clientSiteAddress = objmapper.convertValue(inputMap.get("clientsiteaddress"),
				ClientSiteAddress.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return clientService.deleteClientSiteAddress(clientSiteAddress, userInfo);
	}

	@PostMapping(value = "/createClientContactInfo")
	public ResponseEntity<Object> createClientContactInfo(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final ClientContactInfo clientContactInfo = objmapper.convertValue(inputMap.get("clientcontactinfo"),
				ClientContactInfo.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return clientService.createClientContactInfo(clientContactInfo, userInfo);
	}

	@PostMapping(value = "/getClientContactInfoById")
	public ResponseEntity<Object> getClientContactInfoById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final int nclientCode = (Integer) inputMap.get("nclientcode");
		final int nclientSiteCode = (Integer) inputMap.get("nclientsitecode");
		final int nclientContactCode = (Integer) inputMap.get("nclientcontactcode");
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return clientService.getClientContactInfoById(nclientCode, nclientSiteCode, nclientContactCode, userInfo);
	}

	@PostMapping(value = "/updateClientContactInfo")
	public ResponseEntity<Object> updateClientContactInfo(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final ClientContactInfo clientContactInfo = objmapper.convertValue(inputMap.get("clientcontactinfo"),
				ClientContactInfo.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return clientService.updateClientContactInfo(clientContactInfo, userInfo);
	}

	@PostMapping(value = "/deleteClientContactInfo")
	public ResponseEntity<Object> deleteClientContactInfo(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final ClientContactInfo clientContactInfo = objmapper.convertValue(inputMap.get("clientcontactinfo"),
				ClientContactInfo.class);
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return clientService.deleteClientContactInfo(clientContactInfo, userInfo);
	}

	@PostMapping(value = "/getClientContactInfoBySite")
	public List<ClientContactInfo> getClientContactInfoBySite(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final int nclientCode = (Integer) inputMap.get("nclientcode");
		final int nclientSiteCode = (Integer) inputMap.get("nclientsitecode");
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return clientService.getClientContactInfoBySite(nclientCode, nclientSiteCode, userInfo);
	}

	@PostMapping(value = "/getSelectedClientDetail")
	public ResponseEntity<Object> getSelectedClientDetail(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final int nclientCode = (Integer) inputMap.get("nclientcode");
		requestContext.setUserInfo(userInfo);
		return clientService.getSelectedClientDetail(userInfo, nclientCode);
	}

	@PostMapping(value = "/createClientFile")
	public ResponseEntity<? extends Object> createClientFile(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return clientService.createClientFile(request, userInfo);
	}

	@PostMapping(value = "/updateClientFile")
	public ResponseEntity<? extends Object> updateClientFile(MultipartHttpServletRequest request) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.readValue(request.getParameter("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return clientService.updateClientFile(request, userInfo);
	}

	@PostMapping(value = "/deleteClientFile")
	public ResponseEntity<? extends Object> deleteClientFile(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final ClientFile objClientFile = objMapper.convertValue(inputMap.get("clientfile"), ClientFile.class);
		requestContext.setUserInfo(userInfo);
		return clientService.deleteClientFile(objClientFile, userInfo);
	}

	@PostMapping(value = "/editClientFile")
	public ResponseEntity<Object> editClientFile(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final ClientFile objClientFile = objMapper.convertValue(inputMap.get("clientfile"), ClientFile.class);
		requestContext.setUserInfo(userInfo);
		return clientService.editClientFile(objClientFile, userInfo);
	}

	@PostMapping(value = "/viewAttachedClientFile")
	public ResponseEntity<Object> viewAttachedSupplierFile(@RequestBody Map<String, Object> inputMap,
			HttpServletResponse response) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final ClientFile objClientFile = objMapper.convertValue(inputMap.get("clientfile"), ClientFile.class);
		requestContext.setUserInfo(userInfo);
		final Map<String, Object> outputMap = clientService.viewAttachedClientFile(objClientFile, userInfo);
		return new ResponseEntity<Object>(outputMap, HttpStatus.OK);
	}

}