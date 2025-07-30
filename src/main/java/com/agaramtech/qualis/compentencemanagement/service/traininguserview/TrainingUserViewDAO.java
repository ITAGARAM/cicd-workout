package com.agaramtech.qualis.compentencemanagement.service.traininguserview;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.compentencemanagement.model.TrainingDocuments;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface holds declarations to perform CRUD operation on
 * 'TrainingUserView' table
 */
public interface TrainingUserViewDAO {

	/**
	 * This interface declaration is used to get all the available TrainingUserViews
	 * with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of TrainingUserView records
	 *         with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	ResponseEntity<Object> getUserView(String fromDate, String toDate, String currentUIDate, UserInfo userInfo)
			throws Exception;

	ResponseEntity<Object> getTrainingUserViewDetails(Map<String, Object> inputMap, UserInfo userInfo) throws Exception;

	ResponseEntity<Object> viewUserViewFile(TrainingDocuments objUserViewFile, UserInfo userInfo) throws Exception;

}
