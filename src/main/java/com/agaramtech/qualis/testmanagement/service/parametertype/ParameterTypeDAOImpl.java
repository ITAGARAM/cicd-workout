package com.agaramtech.qualis.testmanagement.service.parametertype;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testmanagement.model.ParameterType;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class ParameterTypeDAOImpl implements ParameterTypeDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(ParameterTypeDAOImpl.class);
	
	  private final CommonFunction commonFunction; 
	  private final JdbcTemplate jdbcTemplate; 
	
	/**
	 * This method is used to retrieve list of all available parametertype for the specified site.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and list of all active parametertype
	 * @throws Exception that are thrown from this DAO layer
	 */
	public ResponseEntity<Object> getParameterType(final UserInfo userInfo) throws Exception {
		
		final String strQuery =" select nparametertypecode, nunitrequired, npredefinedrequired, ngraderequired, sdisplaystatus, nroundingrequired, ndefaultstatus "
							 + " from ParameterType where nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() 
							 + "  and nsitecode ="+userInfo.getNmastersitecode()+" and nparametertypecode>0 ";
		LOGGER.info("Get Method:"+ strQuery);
		return new ResponseEntity<Object>(commonFunction.getMultilingualMessageList((List<ParameterType>)jdbcTemplate.query(strQuery, new ParameterType()), Arrays.asList("sdisplaystatus"), userInfo.getSlanguagefilename()), HttpStatus.OK);
	}
	
}
