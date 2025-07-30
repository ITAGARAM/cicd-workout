package com.agaramtech.qualis.restcontroller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.compentencemanagement.model.Technique;
import com.agaramtech.qualis.compentencemanagement.model.TechniqueTest;
import com.agaramtech.qualis.compentencemanagement.service.technique.TechniqueService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/technique")
public class TechniqueController {

	private static final Logger LOGGER = LoggerFactory.getLogger(TechniqueController.class);

	private final RequestContext requestContext;

	private final TechniqueService techniqueService;

	public TechniqueController(RequestContext requestContext, TechniqueService techniqueService) {
		super();
		this.requestContext = requestContext;
		this.techniqueService = techniqueService;
	}

	/**
	 * This Method is used to get the over all Technique with respect to site
	 * 
	 * @param inputMap [Map] contains key nmasterSiteCode which holds the value of
	 *                 respective site code
	 * @return a response entity which holds the list of Technique with respect to
	 *         site and also have the HTTP response code
	 * @throws Exception that are thrown
	 */
	@PostMapping(value = "/getTechnique")
	public ResponseEntity<Object> getTechnique(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		Integer ntechniquecode = null;

		if (inputMap.get("ntechniquecode") != null) {
			ntechniquecode = (Integer) inputMap.get("ntechniquecode");
		}
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		LOGGER.info("getTechnique called");
		requestContext.setUserInfo(userInfo);
		return techniqueService.getTechnique(ntechniquecode, userInfo);

	}

	/**
	 * This method is used to add a new entry to Technique table.
	 * 
	 * @param inputMap [Map] holds the Technique object to be inserted
	 * @return inserted Technique object and HTTP Status on successive insert
	 *         otherwise corresponding HTTP Status
	 * @throws Exception that are thrown
	 */
	@PostMapping(value = "/createTechnique")
	public ResponseEntity<Object> createTechnique(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final Technique objTechnique = objMapper.convertValue(inputMap.get("technique"),
				new TypeReference<Technique>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		requestContext.setUserInfo(userInfo);
		return techniqueService.createTechnique(objTechnique, userInfo);
	}

	/**
	 * This method is used to update entry in Technique table.
	 * 
	 * @param inputMap [Map] holds the Technique object to be updated
	 * @return response entity object holding response status and data of updated
	 *         Technique object
	 * @throws Exception that are thrown
	 */
	@PostMapping(value = "/updateTechnique")
	public ResponseEntity<Object> updateTechnique(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		final Technique objTechnique = objMapper.convertValue(inputMap.get("technique"),
				new TypeReference<Technique>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		requestContext.setUserInfo(userInfo);
		return techniqueService.updateTechnique(objTechnique, userInfo);

	}

	/**
	 * This method id used to delete an entry in Technique table
	 * 
	 * @param inputMap [Map] holds the Technique object to be deleted
	 * @return response entity object holding response status and data of deleted
	 *         Technique object
	 * @throws Exception that are thrown
	 */
	@PostMapping(value = "/deleteTechnique")
	public ResponseEntity<Object> deleteTechnique(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final Technique objTechnique = objMapper.convertValue(inputMap.get("technique"),
				new TypeReference<Technique>() {
				});
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		requestContext.setUserInfo(userInfo);
		return techniqueService.deleteTechnique(objTechnique, userInfo);
	}

	/**
	 * This method is used to get the single record in Technique table
	 * 
	 * @param inputMap [Map] holds the Technique code to get
	 * @return response entity object holding response status and data of single
	 *         Technique object
	 * @throws Exception that are thrown
	 */
	@PostMapping(value = "/getActiveTechniqueById")
	public ResponseEntity<Object> getActiveTechniqueById(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int ntechniqueCode = (int) inputMap.get("ntechniquecode");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		requestContext.setUserInfo(userInfo);
		return techniqueService.getActiveTechniqueById(ntechniqueCode, userInfo);

	}

	@PostMapping(value = "/getTechniqueTest")
	public ResponseEntity<Object> getTechniqueTest(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		Integer ntechniquecode = null;

		if (inputMap.get("ntechniquecode") != null) {
			ntechniquecode = (Integer) inputMap.get("ntechniquecode");
		}
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		requestContext.setUserInfo(userInfo);
		return techniqueService.getTechniqueTest(ntechniquecode, userInfo);

	}

	@PostMapping(value = "/getTechniqueConducted")
	public ResponseEntity<Object> getTechniqueConducted(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		Integer ntechniquecode = null;

		if (inputMap.get("ntechniquecode") != null) {
			ntechniquecode = (Integer) inputMap.get("ntechniquecode");
		}
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		requestContext.setUserInfo(userInfo);
		return techniqueService.getTechniqueConducted(ntechniquecode, userInfo);

	}

	@PostMapping(value = "/getTechniqueScheduled")
	public ResponseEntity<Object> getTechniqueScheduled(@RequestBody Map<String, Object> inputMap) throws Exception {

		final ObjectMapper objMapper = new ObjectMapper();
		Integer ntechniquecode = null;

		if (inputMap.get("ntechniquecode") != null) {
			ntechniquecode = (Integer) inputMap.get("ntechniquecode");
		}
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});

		requestContext.setUserInfo(userInfo);
		return techniqueService.getTechniqueScheduled(ntechniquecode, userInfo);

	}

	@PostMapping(value = "/createTechniqueTest")
	public ResponseEntity<Object> createTechniqueTest(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final List<TechniqueTest> techniqueTestList = objectMapper.convertValue(inputMap.get("techniquetestlist"),
				new TypeReference<List<TechniqueTest>>() {
				});
		final Integer ntechniqueCode = (Integer) inputMap.get("ntechniquecode");
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return techniqueService.createTechniqueTest(techniqueTestList, ntechniqueCode, userInfo);
	}

	@PostMapping(value = "/deleteTechniqueTest")
	public ResponseEntity<Object> deleteTechniqueTest(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objectMapper = new ObjectMapper();
		final TechniqueTest techniqueTest = objectMapper.convertValue(inputMap.get("techniquetest"),
				new TypeReference<TechniqueTest>() {
				});
		final Integer ntechniqueCode = (Integer) inputMap.get("ntechniquecode");
		final UserInfo userInfo = objectMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return techniqueService.deleteTechniqueTest(techniqueTest, ntechniqueCode, userInfo);
	}
}
