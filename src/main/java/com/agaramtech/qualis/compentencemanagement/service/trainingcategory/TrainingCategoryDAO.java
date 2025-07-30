package com.agaramtech.qualis.compentencemanagement.service.trainingcategory;

import org.springframework.http.ResponseEntity;

import com.agaramtech.qualis.compentencemanagement.model.TrainingCategory;
import com.agaramtech.qualis.global.UserInfo;

public interface TrainingCategoryDAO {

	public ResponseEntity<Object> getTrainingCategory(final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> createTrainingCategory(final TrainingCategory objTrainingCategory,
			final UserInfo userInfo) throws Exception;

	public TrainingCategory getActiveTrainingCategoryById(final int ntrainingcategorycode, final UserInfo userInfo)
			throws Exception;

	public ResponseEntity<Object> updateTrainingCategory(final TrainingCategory objTrainingCategory,
			final UserInfo userInfo) throws Exception;

	public ResponseEntity<Object> deleteTrainingCategory(final TrainingCategory objTrainingCategory,
			final UserInfo userInfo) throws Exception;

}
