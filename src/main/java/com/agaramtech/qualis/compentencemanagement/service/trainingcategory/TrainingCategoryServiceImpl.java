
package com.agaramtech.qualis.compentencemanagement.service.trainingcategory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.compentencemanagement.model.TrainingCategory;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

import lombok.RequiredArgsConstructor;

@Transactional(readOnly = true, rollbackFor = Exception.class)
@Service
@RequiredArgsConstructor
public class TrainingCategoryServiceImpl implements TrainingCategoryService {

	private final TrainingCategoryDAO TraningCategoryDAO;
	private final CommonFunction commonFunction;

	@Override
	public ResponseEntity<Object> getTrainingCategory(final UserInfo userInfo) throws Exception {

		return TraningCategoryDAO.getTrainingCategory(userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> createTrainingCategory(final TrainingCategory objTrainingCategory, final UserInfo userInfo)
			throws Exception {
		return TraningCategoryDAO.createTrainingCategory(objTrainingCategory, userInfo);
	}

	@Override
	public ResponseEntity<Object> getActiveTrainingCategoryById(int ntrainingcategorycode, final UserInfo userInfo)
			throws Exception {

		final TrainingCategory TraningCategory = TraningCategoryDAO.getActiveTrainingCategoryById(ntrainingcategorycode,
				userInfo);
		if (TraningCategory == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			return new ResponseEntity<>(TraningCategory, HttpStatus.OK);
		}
	}

	@Transactional
	@Override
	public ResponseEntity<Object> updateTrainingCategory(final TrainingCategory objTrainingCategory, final UserInfo userInfo)
			throws Exception {

		return TraningCategoryDAO.updateTrainingCategory(objTrainingCategory, userInfo);
	}

	@Transactional
	@Override
	public ResponseEntity<Object> deleteTrainingCategory(final TrainingCategory objTrainingCategory, final UserInfo userInfo)
			throws Exception {
		return TraningCategoryDAO.deleteTrainingCategory(objTrainingCategory, userInfo);
	}

}
