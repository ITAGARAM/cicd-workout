package com.agaramtech.qualis.credential.service.userroleconfiguration;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.credential.model.UserRoleConfig;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;

import lombok.AllArgsConstructor;


@AllArgsConstructor
@Repository
public class UserRoleConfigurationDAOImpl  implements UserRoleConfigurationDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserRoleConfigurationDAOImpl.class);
	
	
	private final JdbcTemplate jdbcTemplate;
	
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	
	@Override
	public ResponseEntity<Object> getUserRoleConfiguration(UserInfo userInfo) throws Exception {
		String strQuery="";
		
		if(userInfo.getNlogintypecode()== Enumeration.LoginType.INTERNAL.getnlogintype()) {
			strQuery = "select urc.*,ur.suserrolename from userrole ur "
					 + "join userrolepolicy urp on urp.nuserrolecode =ur.nuserrolecode "
					 + "join userroleconfig urc on urc.nuserrolecode=urp.nuserrolecode "
					 + "where urp.ntransactionstatus="+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()+" and urp.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					 + "and ur.nstatus="+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and urc.nstatus="+Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" "
					 + "and urc.nuserrolecode> 0 and ur.nsitecode=" +userInfo.getNmastersitecode() ;
			
		}else if(userInfo.getNlogintypecode()== Enumeration.LoginType.ADS.getnlogintype()) {
			 strQuery = "select a.*, b.suserrolename from userroleconfig a, userrole b where a.nuserrolecode = b.nuserrolecode and a.nstatus = b.nstatus and a.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
			  		  + "and a.nuserrolecode > 0 and b.nsitecode = " +userInfo.getNmastersitecode() ;
		}
		LOGGER.info("Get Method:"+ strQuery);
	    return new ResponseEntity<Object>(jdbcTemplate.query(strQuery, new UserRoleConfig()), HttpStatus.OK);
	}
	
	@Override
	public UserRoleConfig getActiveUserRoleConfigurationById(final int nuserRoleCode) throws Exception {
			final String strQuery = "select a.*, b.suserrolename from userroleconfig a, userrole b where a.nuserrolecode = b.nuserrolecode and a.nstatus = "+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
			  					  + " and a.nuserrolecode = " + nuserRoleCode;
			
			return (UserRoleConfig) jdbcUtilityFunction.queryForObject(strQuery, UserRoleConfig.class, jdbcTemplate);
	}
		
	public ResponseEntity<Object> updateUserRoleConfiguration(UserRoleConfig userRoleConfig, UserInfo userInfo)	throws Exception {
		
		    final UserRoleConfig oldUserRoleList=getActiveUserRoleConfigurationById(userRoleConfig.getNuserrolecode());
		    
			if(userRoleConfig.getNneedresultflow() == Enumeration.TransactionStatus.YES.gettransactionstatus() ) {
				final String updateQueryString1 = "update userroleconfig set nneedresultflow = " + Enumeration.TransactionStatus.NO.gettransactionstatus() 
											+ " where nstatus= " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
					
				jdbcTemplate.execute(updateQueryString1);
			}
			final String updateQueryString = "update userroleconfig set dmodifieddate='"+dateUtilityFunction.getCurrentDateTime(userInfo)+"',nneedapprovalflow = " + userRoleConfig.getNneedapprovalflow() + " , nneedresultflow = " + userRoleConfig.getNneedresultflow()+", " 
	                   					    + " nneedprojectflow ="+userRoleConfig.getNneedprojectflow()+" where nuserrolecode=" + userRoleConfig.getNuserrolecode();
			jdbcTemplate.execute(updateQueryString);
	        final List<String> multilingualIDList = new ArrayList<>();
	        multilingualIDList.add("IDS_EDITUSERROLECONFIG");
			final List<Object> savedUserRoleList = new ArrayList<>();	        
			savedUserRoleList.add(userRoleConfig);	
			final List<Object> prevUserRoleList = new ArrayList<>();	        
			prevUserRoleList.add(oldUserRoleList);
			auditUtilityFunction.fnInsertAuditAction(savedUserRoleList, 2, prevUserRoleList, multilingualIDList, userInfo);
			return getUserRoleConfiguration(userInfo);
	
	}

	
	
	
}
 