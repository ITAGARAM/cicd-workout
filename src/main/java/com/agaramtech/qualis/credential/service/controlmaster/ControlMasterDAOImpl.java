package com.agaramtech.qualis.credential.service.controlmaster;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.basemaster.service.unit.UnitDAOImpl;
import com.agaramtech.qualis.credential.model.ControlMaster;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.UserInfo;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "controlmaster" table by
 * implementing methods from its interface.
 */
@AllArgsConstructor
@Repository
public class ControlMasterDAOImpl implements ControlMasterDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(UnitDAOImpl.class);

	private final JdbcTemplate jdbcTemplate;

	/**
	 * This method is used to retrieve list of all available control master data
	 * with respect to site and nformcode
	 * 
	 * @param userInfo [UserInfo] holding logged in user details and nmasterSiteCode
	 *                 [int] primary key of site object for which the list is to be
	 *                 fetched and nformcode [int] primary key of qualisforms object
	 *                 for which the list is to be fetched
	 * @return response entity object holding response status and list of
	 *         controlmaster records with respect to site and nformcode
	 * @throws Exception that are thrown from this DAO layer
	 */
	@Override
	public ResponseEntity<List<ControlMaster>> getUploadControlsByFormCode(final UserInfo userInfo) throws Exception {
		String strQuery = " select cm.ncontrolcode, cm.nformcode, cm.scontrolname, cm.nisesigncontrol, cm.nisdistributedsite, cm.nisprimarysyncsite, cm.dmodifieddate, cm.nstatus, cm.jsondata , fs.ssubfoldername from controlmaster cm, ftpsubfolder fs "
				+ " where cm.ncontrolcode=fs.ncontrolcode and cm.nformcode=" + userInfo.getNformcode()
				+ " and cm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and fs.nsitecode=" + +userInfo.getNmastersitecode() + " and fs.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

		LOGGER.info("At getUploadControlsByFormCode method");
		final List<ControlMaster> controlList = (List<ControlMaster>) jdbcTemplate.query(strQuery, new ControlMaster());
		return new ResponseEntity<>(controlList, HttpStatus.OK);
	}

}
