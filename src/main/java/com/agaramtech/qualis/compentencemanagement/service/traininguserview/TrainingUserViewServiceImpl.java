package com.agaramtech.qualis.compentencemanagement.service.traininguserview;

import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.agaramtech.qualis.compentencemanagement.model.TrainingDocuments;
import com.agaramtech.qualis.global.UserInfo;

/**
 * This class holds methods to perform CRUD operation on 'TrainingUserView'
 * table through its DAO layer.
 */
@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
public class TrainingUserViewServiceImpl implements TrainingUserViewService {

	private final TrainingUserViewDAO traininguserviewDAO;

	public TrainingUserViewServiceImpl(TrainingUserViewDAO traininguserviewDAO) {
		super();
		this.traininguserviewDAO = traininguserviewDAO;
	}

	/**
	 * This service implementation method will access the DAO layer that is used to
	 * get all the available TrainingUserViews with respect to site
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched
	 * @return a response entity which holds the list of TrainingUserView records
	 *         with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	public ResponseEntity<Object> getUserView(String fromDate, String toDate, String currentUIDate, UserInfo userInfo)
			throws Exception {
		return traininguserviewDAO.getUserView(fromDate, toDate, currentUIDate, userInfo);
	}

	public ResponseEntity<Object> getTrainingUserViewDetails(Map<String, Object> inputMap, final UserInfo userInfo)
			throws Exception {
		return traininguserviewDAO.getTrainingUserViewDetails(inputMap, userInfo);
	}

	public ResponseEntity<Object> viewUserViewFile(TrainingDocuments objUserViewFile, UserInfo userInfo)
			throws Exception {
		return traininguserviewDAO.viewUserViewFile(objUserViewFile, userInfo);
	}

}
