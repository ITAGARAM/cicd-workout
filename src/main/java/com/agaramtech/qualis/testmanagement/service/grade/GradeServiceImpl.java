package com.agaramtech.qualis.testmanagement.service.grade;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.UserInfo;



@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class GradeServiceImpl implements GradeService {
	
	private final  GradeDAO gradeDAO;

	/**
	 * This constructor injection method is used to pass the object dependencies to the class properties.
	 * @param methodCategoryDAO MethodCategoryDAO Interface
	 * @param commonFunction CommonFunction holding common utility functions
	 */
	
	public GradeServiceImpl(GradeDAO gradeDAO) {
		this.gradeDAO = gradeDAO;
	}

	@Override
	public ResponseEntity<Object> getGrade(UserInfo objUserInfo) throws Exception {
		return gradeDAO.getGrade(objUserInfo);
	}
}
