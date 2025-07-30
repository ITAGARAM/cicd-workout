package com.agaramtech.qualis.configuration.service.sorting;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This interface declaration holds methods to perform CRUD operation.
 */
public interface SortingService {
	
	/**
	 * This service interface declaration will access the DAO layer that is used to
	 * get all the available Sortings with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of Sorting records with
	 *         respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	
	public ResponseEntity<Object> getSorting(UserInfo userinfo, Integer boolvalue) throws Exception;

	public ResponseEntity<Object> getFilter(UserInfo userinfo, Integer nmenucode, Integer boolvalue) throws Exception;

	public ResponseEntity<Object> getFilter1(UserInfo userinfo, Integer nmenucode, Integer nmodulecode,
			Integer boolvalue) throws Exception;

	public ResponseEntity<Object> updateForms(UserInfo userinfo, Map<String, Object> inputMap) throws Exception;

	public ResponseEntity<Object> updateModules(UserInfo userinfo, Map<String, Object> inputMap) throws Exception;

}
