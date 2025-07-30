package com.agaramtech.qualis.testmanagement.service.parametertype;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.agaramtech.qualis.global.UserInfo;

@Transactional(readOnly = true, rollbackFor=Exception.class)
@Service
public class ParameterTypeServiceImpl implements ParameterTypeService{

	private final ParameterTypeDAO parameterTypeDAO;
	
	/**
	 * This constructor injection method is used to pass the object dependencies to the class properties.
	 * @param parameterTypeDAO ParameterTypeDAO Interface
	 */
	public ParameterTypeServiceImpl(ParameterTypeDAO parameterTypeDAO) {
		this.parameterTypeDAO = parameterTypeDAO;
	}
	
	/**
	 * This service implementation method will access the DAO layer that is used 
	 * to get all the available parametertype with respect to site
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return a response entity which holds the list of parametertype records with respect to site
	 * @throws Exception that are thrown in the DAO layer
	 */
	@Override
	public ResponseEntity<Object> getParameterType(final UserInfo userInfo) throws Exception {		
		return parameterTypeDAO.getParameterType(userInfo);
	}
}
