
package com.agaramtech.qualis.restcontroller;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.agaramtech.qualis.compentencemanagement.model.TrainingDocuments;
import com.agaramtech.qualis.compentencemanagement.service.traininguserview.TrainingUserViewService;
import com.agaramtech.qualis.global.UserInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/userview")
@AllArgsConstructor
public class TrainingUserViewController {
	
	private final TrainingUserViewService traininguserviewService;
	private RequestContext requestContext;

	/**
	 * This method is used to retrieve list of available TrainingUserView(s).
	 * 
	 * @param inputMap [Map] map object with userInfo [UserInfo] holding logged in
	 *                 user details and nmasterSiteCode [int] primary key of site
	 *                 object for which the list is to be fetched Input :
	 *                 {"userinfo":{nmastersitecode": -1}}
	 * @return response entity object holding response status and list of all
	 *         TrainingUserViews
	 * @throws Exception exception
	 */
	@PostMapping(value = "/getUserView")
	public ResponseEntity<Object> getUserView(@RequestBody Map<String, Object> inputMap) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		String fromDate = "";
		String toDate = "";
		if (inputMap.get("fromDate") != null) {
			fromDate = (String) inputMap.get("fromDate");
		}
		if (inputMap.get("toDate") != null) {
			toDate = (String) inputMap.get("toDate");
		}
		final String currentUIDate = (String) inputMap.get("currentdate");
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), new TypeReference<UserInfo>() {
		});
		requestContext.setUserInfo(userInfo);
		return traininguserviewService.getUserView(fromDate, toDate, currentUIDate, userInfo);
	}

	@PostMapping(value = "/getTrainingUserViewDetails")
	public ResponseEntity<Object> getTrainingUserViewDetails(@RequestBody Map<String, Object> inputMap)
			throws Exception {

		final ObjectMapper objmapper = new ObjectMapper();
		final UserInfo userInfo = objmapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		requestContext.setUserInfo(userInfo);
		return traininguserviewService.getTrainingUserViewDetails(inputMap, userInfo);
	}

	@PostMapping(value = "/viewUserViewFile")
	public ResponseEntity<Object> viewUserViewFile(@RequestBody Map<String, Object> inputMap,
			HttpServletResponse response) throws Exception {
		final ObjectMapper objMapper = new ObjectMapper();
		final UserInfo userInfo = objMapper.convertValue(inputMap.get("userinfo"), UserInfo.class);
		final TrainingDocuments objUserViewFile = objMapper.convertValue(inputMap.get("userviewfile"),
				TrainingDocuments.class);
		requestContext.setUserInfo(userInfo);
		return traininguserviewService.viewUserViewFile(objUserViewFile, userInfo);
	}
}
