package com.agaramtech.qualis.restcontroller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.agaramtech.qualis.basemaster.model.SampleTestComments;
import com.agaramtech.qualis.basemaster.service.sampletestcomments.SampleTestCommentsService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * This controller handles requests related to sample test comments and delegates them to the appropriate service methods.
 */
@RestController
@RequestMapping("/sampletestcomments")
public class SampleTestCommentsController {

	private static final Logger LOGGER = LoggerFactory.getLogger(SampleTestCommentsController.class);
	
	private final SampleTestCommentsService sampleTestCommentsService;
	private RequestContext requestContext;

	/**
	 * Constructor to initialize SampleTestCommentsController with dependencies.
	 * @param requestContext Holds the request context, including user details.
	 * @param sampleTestCommentsService Service layer to handle sample test comment logic.
	 */
	public SampleTestCommentsController(RequestContext requestContext,
			SampleTestCommentsService sampleTestCommentsService) {
		super();
		this.requestContext = requestContext;
		this.sampleTestCommentsService = sampleTestCommentsService;
	}

	/**
	 * Retrieves a list of sample test comments for a specific user.
	 * @param inputMap A map containing user information (userinfo).
	 * @return ResponseEntity containing the status and the list of sample test comments.
	 * @throws Exception If an error occurs while fetching the comments.
	 */
	@PostMapping(value = "/getSampleTestComments")
	public ResponseEntity<Object> getSampleTestComments(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		LOGGER.info("getSampleTestComments called");
		return sampleTestCommentsService.getSampleTestComments(userInfo);
	}

	/**
	 * Creates a new sample test comment entry in the database.
	 * @param inputMap A map containing the sample test comment data and user information.
	 * @return ResponseEntity containing the status and the created sample test comment.
	 * @throws Exception If an error occurs while creating the comment.
	 */
	@PostMapping(value = "/createSampleTestComments")
	public ResponseEntity<Object> createSampleTestComments(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final SampleTestComments objSampleTestComments = objMapper.convertValue(inputMap.get("sampletestcomments"),
				new TypeReference<SampleTestComments>() {
				});
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleTestCommentsService.createSampleTestComments(objSampleTestComments, userInfo);
	}

	/**
	 * Updates an existing sample test comment entry.
	 * @param inputMap A map containing the sample test comment data and user information.
	 * @return ResponseEntity containing the status and the updated sample test comment.
	 * @throws Exception If an error occurs while updating the comment.
	 */
	@PostMapping(value = "/updateSampleTestComments")
	public ResponseEntity<Object> updateSampleTestComments(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final SampleTestComments objSampleTestComments = objMapper.convertValue(inputMap.get("sampletestcomments"),
				new TypeReference<SampleTestComments>() {
				});
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleTestCommentsService.updateSampleTestComments(objSampleTestComments, userInfo);
	}

	/**
	 * Deletes a specific sample test comment entry.
	 * @param inputMap A map containing the sample test comment data and user information.
	 * @return ResponseEntity containing the status of the deletion process.
	 * @throws Exception If an error occurs while deleting the comment.
	 */
	@PostMapping(value = "/deleteSampleTestComments")
	public ResponseEntity<Object> deleteSampleTestComments(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final SampleTestComments objSampleTestComments = objMapper.convertValue(inputMap.get("sampletestcomments"),
				new TypeReference<SampleTestComments>() {
				});
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleTestCommentsService.deleteSampleTestComments(objSampleTestComments, userInfo);
	}

	/**
	 * Retrieves a specific sample test comment by its ID.
	 * @param inputMap A map containing the sample test comment ID and user information.
	 * @return ResponseEntity containing the status and the requested sample test comment.
	 * @throws Exception If an error occurs while fetching the comment.
	 */
	@PostMapping(value = "/getActiveSampleTestCommentsById")
	public ResponseEntity<Object> getActiveSampleTestCommentsById(@RequestBody Map<String, Object> inputMap)
			throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final int nsampletestcommentcode = (int) inputMap.get("nsampletestcommentscode");
		UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return sampleTestCommentsService.getActiveSampleTestCommentsById(nsampletestcommentcode, userInfo);
	}

	/**
	 * Retrieves a list of comment types available for sample test comments.
	 * @param inputMap A map containing user information (userinfo).
	 * @return ResponseEntity containing the list of available comment types.
	 * @throws Exception If an error occurs while fetching the comment types.
	 */
	@PostMapping(value = "/getCommentType")
	public ResponseEntity<Object> getCommentType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return sampleTestCommentsService.getCommentType(userInfo);
	}

	/**
	 * Retrieves a list of comment subtypes available for sample test comments.
	 * @param inputMap A map containing user information (userinfo).
	 * @return ResponseEntity containing the list of available comment subtypes.
	 * @throws Exception If an error occurs while fetching the comment subtypes.
	 */
	@PostMapping(value = "/getCommentSubType")
	public ResponseEntity<Object> getCommentSubType(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return sampleTestCommentsService.getCommentSubType(userInfo);
	}
}
