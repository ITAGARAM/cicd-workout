package com.agaramtech.qualis.login.service.useruiconfig;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.login.model.UserUiConfig;

import lombok.AllArgsConstructor;

/**
 * This class is used to perform CRUD Operation on "city" table by implementing
 * methods from its interface.
 */
@AllArgsConstructor
@Repository
public class UserUiConfigDAOImpl implements UserUiConfigDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserUiConfigDAOImpl.class);

	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;

	@Override
	public ResponseEntity<Object> getcolormastertheme(final UserInfo userInfo) throws Exception {
		LOGGER.info("getcolormastertheme");
		final Map<String, Object> returnMap = new HashMap<String, Object>();
		final String strQuery = "select nthemecolorcode,sthemecolorname,sthemecolorhexcode from themecolormaster tcm where  "
				+ " nthemecolorcode > 0 and tcm.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		returnMap.put("colortheme", jdbcTemplate.queryForList(strQuery));
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createUserUiConfig(final UserUiConfig useruiconfig, final UserInfo userInfo)
			throws Exception {
		final UserUiConfig validateUserTheme = (UserUiConfig) ((Map<String, Object>) getActiveuseruiconfigById(
				userInfo.getNusercode(), userInfo).getBody()).get("selectedUserUiConfig");
		if (validateUserTheme == null) {
			final String insertquery = "INSERT INTO public.useruiconfig( nusercode,nfontsize, nthemecolorcode, "
					+ "sthemecolorhexcode, dmodifieddate, nsitecode, nstatus) VALUES (" + userInfo.getNusercode() + ","
					+ useruiconfig.getNfontsize() + "," + useruiconfig.getNthemecolorcode() + "," + "'"
					+ useruiconfig.getSthemecolorhexcode() + "','" + dateUtilityFunction.getCurrentDateTime(userInfo)
					+ "'," + userInfo.getNmastersitecode() + ","
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ");";
			jdbcTemplate.execute(insertquery);
		} else {
			return updateUserUiConfig(useruiconfig, userInfo);
		}
		return new ResponseEntity<>(getActiveuseruiconfigById(userInfo.getNusercode(), userInfo).getBody(),
				HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> updateUserUiConfig(final UserUiConfig useruiconfig, final UserInfo userInfo)
			throws Exception {
		final UserUiConfig validateUserTheme = (UserUiConfig) ((Map<String, Object>) getActiveuseruiconfigById(
				userInfo.getNusercode(), userInfo).getBody()).get("selectedUserUiConfig");
		if (validateUserTheme != null) {
			final String updateQuery = "UPDATE useruiconfig SET" + " nfontsize = " + useruiconfig.getNfontsize()
					+ ", nthemecolorcode=" + useruiconfig.getNthemecolorcode() + ",sthemecolorhexcode =N'"
					+ useruiconfig.getSthemecolorhexcode() + "', dmodifieddate='"
					+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' " + " WHERE nusercode= "
					+ userInfo.getNusercode() + " and nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ";";
			jdbcTemplate.execute(updateQuery);
		}
		return new ResponseEntity<>(getActiveuseruiconfigById(userInfo.getNusercode(), userInfo).getBody(),
				HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getActiveuseruiconfigById(final int nusercode, final UserInfo userInfo)
			throws Exception {
		final Map<String, Object> returnMap = new HashMap<String, Object>();
		final String strQuery = "select tcm.sthemecolorname,ut.* from useruiconfig ut,themecolormaster tcm where ut.nthemecolorcode = tcm.nthemecolorcode "
				+ " and ut.nusercode = " + nusercode + " " + " and ut.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and tcm.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		returnMap.put("selectedUserUiConfig",
				jdbcUtilityFunction.queryForObject(strQuery, UserUiConfig.class, jdbcTemplate));
		return new ResponseEntity<>(returnMap, HttpStatus.OK);
	}
}