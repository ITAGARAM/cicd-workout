package com.agaramtech.qualis.testmanagement.service.linkmaster;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.LinkMaster;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.testmanagement.model.TestFile;

@Repository
public class LinkMasterDAOImpl implements LinkMasterDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(LinkMasterDAOImpl.class);

	private final JdbcTemplate jdbcTemplate;

	
	/**
	 * This constructor injection method is used to pass the object dependencies to
	 * the class.
	 * @param JdbcTemplate jdbcTemplate
	 */
	
	public LinkMasterDAOImpl(JdbcTemplate jdbcTemplate) {
		super();
		this.jdbcTemplate = jdbcTemplate;
	}
	
	/**
	 * This method is used to retrieve list of all available linkMasters for the specified site.
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode [int] primary key of site object for 
	 * 							  which the list is to be fetched
	 * @return response entity  object holding response status and list of all active linkMasters
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<Object> getLinkMaster(final UserInfo objUserInfo) throws Exception {

		String sQuery = " select jsondata->>'slinkname' as slinkname,nlinkcode,ndefaultlink,nsitecode,nstatus from linkmaster"
				+ " where nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nlinkcode > 0  and nsitecode = " + objUserInfo.getNmastersitecode() + ";";
		
		LOGGER.info("Get Query -->"+sQuery);
		final List<LinkMaster> lstLinkMaster = jdbcTemplate.query(sQuery, new LinkMaster());

		sQuery = " select nattachmenttypecode from attachmenttype" + " where nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		
		LOGGER.info("Get Query Test File -->"+sQuery);

		final List<TestFile> lstTestFile = jdbcTemplate.query(sQuery, new TestFile());

		final Map<String, Object> objMap = new HashMap<>();
		objMap.put("LinkMaster", lstLinkMaster);
		objMap.put("AttachmentType", lstTestFile);

		return new ResponseEntity<Object>(objMap, HttpStatus.OK);
	}

}
