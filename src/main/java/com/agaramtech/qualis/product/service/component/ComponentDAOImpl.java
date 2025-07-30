package com.agaramtech.qualis.product.service.component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.agaramtech.qualis.basemaster.service.unit.UnitDAOImpl;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.agaramtech.qualis.product.model.Component;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class ComponentDAOImpl implements ComponentDAO {

	private static final Logger LOGGER = LoggerFactory.getLogger(UnitDAOImpl.class);

	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private ValidatorDel validatorDel;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final ProjectDAOSupport projectDAOSupport;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;

	@Override
	public ResponseEntity<Object> getComponent(final UserInfo userInfo) throws Exception {

		final String strQuery = " select a.ncomponentcode,a.scomponentname,a.ndefaultstatus,case when a.sdescription = 'null'"
				+ " then '' else a.sdescription end sdescription , "
				+ " COALESCE(ts.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "',ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus, " + "to_char(a.dmodifieddate, '"
				+ userInfo.getSpgsitedatetime().replace("'T'", " ") + "') as smodifieddate "
				+ " from component a, transactionstatus ts where a.ndefaultstatus = ts.ntranscode "
				+ " and a.nstatus  = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and  ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and a.nsitecode = " + userInfo.getNmastersitecode() + " and a.ncomponentcode > 0";

		return new ResponseEntity<>((List<Component>) jdbcTemplate.query(strQuery, new Component()), HttpStatus.OK);
	}

	public ResponseEntity<Object> getComponentPortal(final UserInfo userInfo) throws Exception {

		final Map<String, Object> outputMap = new LinkedHashMap<String, Object>();
		final String strQuery = " select a.ncomponentcode, a.scomponentname, a.sdescription, a.ndefaultstatus, a.nsitecode, a.nstatus, COALESCE(ts.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "',ts.jsondata->'stransdisplaystatus'->>'en-US') as sdisplaystatus,"
				+ "to_char(a.dmodifieddate, '" + userInfo.getSpgsitedatetime().replace("'T'", " ")
				+ "') as smodifieddate "
				+ " from component a, transactionstatus ts where a.ndefaultstatus = ts.ntranscode "
				+ " and a.nstatus  = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and  ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and a.nsitecode = " + userInfo.getNmastersitecode() + " and a.ncomponentcode > 0";
		outputMap.put("Component", jdbcTemplate.query(strQuery, new Component()));

		return new ResponseEntity<>(outputMap, HttpStatus.OK);
	}

	@Override
	public Component getActiveComponentById(final int ncomponentCode, final UserInfo userInfo) throws Exception {
		final String strQuery = " select a.ncomponentcode, a.scomponentname, a.sdescription, a.ndefaultstatus, a.nsitecode, a.nstatus, COALESCE(ts.jsondata->'sactiondisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode()
				+ "',ts.jsondata->'sactiondisplaystatus'->>'en-US') as sdisplaystatus "
				+ " from component a, transactionstatus ts where a.ndefaultstatus = ts.ntranscode"
				+ " and a.nstatus  = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and  ts.nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "  and a.ncomponentcode = " + ncomponentCode;

		final Component objComponent = (Component) jdbcUtilityFunction.queryForObject(strQuery, Component.class,
				jdbcTemplate);
		return objComponent;
	}

	@Override
	public ResponseEntity<Object> createComponent(Map<String, Object> inputMap, Component component, UserInfo userInfo)
			throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedComponentList = new ArrayList<>();
		final Component componentListByName = getComponentListByName(component.getScomponentname(),
				component.getNsitecode());
		if (componentListByName == null) {
			if (component.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				final Component defaultComponent = getComponentByDefaultStatus(component.getNsitecode());
				if (defaultComponent != null) {
					final Component componentBeforeSave = SerializationUtils.clone(defaultComponent);
					final List<Object> defaultListBeforeSave = new ArrayList<>();
					defaultListBeforeSave.add(componentBeforeSave);
					defaultComponent.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
					final String updateQueryString = " update component set ndefaultstatus="
							+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", dmodifieddate ='"
							+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ncomponentcode ="
							+ defaultComponent.getNcomponentcode();
					jdbcTemplate.execute(updateQueryString);
					final List<Object> defaultListAfterSave = new ArrayList<>();
					defaultListAfterSave.add(defaultComponent);

					Map<?, ?> obj = (Map<?, ?>) ((Map<?, ?>) ((Map<?, ?>) ((Map<?, ?>) inputMap.get("genericlabel")).get("EditComponent"))
							.get("jsondata")).get("sdisplayname");
					String editlabel = (String) obj.get(userInfo.getSlanguagetypecode());
					multilingualIDList.add(editlabel);

					auditUtilityFunction.fnInsertAuditAction(defaultListAfterSave, 2, defaultListBeforeSave,
							multilingualIDList, userInfo);
					multilingualIDList.clear();
				}
			}
			String sequencequery = "select nsequenceno from SeqNoProductManagement where stablename ='component'";
			int nsequenceno = jdbcTemplate.queryForObject(sequencequery, Integer.class);
			nsequenceno++;

			String insertquery = "Insert into component (ncomponentcode,scomponentname,sdescription,ndefaultstatus,dmodifieddate,nsitecode,nstatus)"
					+ "values(" + nsequenceno + ",N'"
					+ stringUtilityFunction.replaceQuote(component.getScomponentname()) + "',N'"
					+ stringUtilityFunction.replaceQuote(component.getSdescription()) + "',"
					+ component.getNdefaultstatus() + ",'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "', "
					+ userInfo.getNmastersitecode() + "," + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ ")";
			jdbcTemplate.execute(insertquery);
			String updatequery = "update SeqNoProductManagement set nsequenceno =" + nsequenceno
					+ " where stablename ='component'";
			jdbcTemplate.execute(updatequery);
			component.setNcomponentcode(nsequenceno);
			savedComponentList.add(component);
			Map<?, ?> obj = (Map<?, ?>) ((Map<?, ?>) ((Map<?, ?>) ((Map<?, ?>) inputMap.get("genericlabel")).get("AddComponent")).get("jsondata"))
					.get("sdisplayname");
			String addlabel = (String) obj.get(userInfo.getSlanguagetypecode());
			multilingualIDList.add(addlabel);

			auditUtilityFunction.fnInsertAuditAction(savedComponentList, 1, null, multilingualIDList, userInfo);
			return getComponent(userInfo);
		} else {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.CONFLICT);
		}
	}

	private Component getComponentByDefaultStatus(final int nsiteCode) throws Exception {
		final String strQuery = "select * from component a" + " where a.nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and a.ndefaultstatus="
				+ Enumeration.TransactionStatus.YES.gettransactionstatus() + " and a.nsitecode = " + nsiteCode;
		return (Component) jdbcUtilityFunction.queryForObject(strQuery, Component.class, jdbcTemplate);
	}

	private Component getComponentListByName(final String scomponentName, final int siteCode) throws Exception {
		final String strQuery = "select  scomponentname from component where scomponentname = N'"
				+ stringUtilityFunction.replaceQuote(scomponentName) + "' and nstatus = "
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and nsitecode =" + siteCode;

		return (Component) jdbcUtilityFunction.queryForObject(strQuery, Component.class, jdbcTemplate);
	}

	@Override
	public ResponseEntity<Object> updateComponent(Map<String, Object> inputMap, Component component, UserInfo userInfo)
			throws Exception {
		final Component componentByID = getActiveComponentById(component.getNcomponentcode(), userInfo);
		final List<Object> listAfterUpdate = new ArrayList<>();
		final List<Object> listBeforeUpdate = new ArrayList<>();
		if (componentByID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {
			final String queryString = "select ncomponentcode from component where scomponentname = N'"
					+ stringUtilityFunction.replaceQuote(component.getScomponentname()) + "' and ncomponentcode <> "
					+ component.getNcomponentcode() + " and  nstatus = "
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()+" and nsitecode="+userInfo.getNmastersitecode();
			final List<Component> componentList = (List<Component>) jdbcTemplate.query(queryString, new Component());
			if (componentList.isEmpty()) {
				if (component.getNdefaultstatus() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
					final Component defaultComponent = getComponentByDefaultStatus(component.getNsitecode());
					if (defaultComponent != null
							&& defaultComponent.getNcomponentcode() != component.getNcomponentcode()) {
						final Component componentBeforeSave = SerializationUtils.clone(defaultComponent);
						listBeforeUpdate.add(componentBeforeSave);
						defaultComponent
								.setNdefaultstatus((short) Enumeration.TransactionStatus.NO.gettransactionstatus());
						final String updateQueryString = " update component set ndefaultstatus="
								+ Enumeration.TransactionStatus.NO.gettransactionstatus() + ", dmodifieddate ='"
								+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ncomponentcode = "
								+ defaultComponent.getNcomponentcode();
						jdbcTemplate.execute(updateQueryString);
						listAfterUpdate.add(defaultComponent);
					}
				}
				final String updateQueryString = "update component set" + " scomponentname=N'"
						+ stringUtilityFunction.replaceQuote(component.getScomponentname()) + "',sdescription =N'"
						+ stringUtilityFunction.replaceQuote(component.getSdescription()) + "',ndefaultstatus ="
						+ component.getNdefaultstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ncomponentcode="
						+ component.getNcomponentcode();
				jdbcTemplate.execute(updateQueryString);
				final List<String> multilingualIDList = new ArrayList<>();

				Map<?, ?> obj = (Map<?, ?>) ((Map<?, ?>) ((Map<?, ?>) ((Map<?, ?>) inputMap.get("genericlabel")).get("EditComponent"))
						.get("jsondata")).get("sdisplayname");
				String editlabel = (String) obj.get(userInfo.getSlanguagetypecode());
				multilingualIDList.add(editlabel);
				listAfterUpdate.add(component);
				listBeforeUpdate.add(componentByID);
				
				auditUtilityFunction.fnInsertAuditAction(listAfterUpdate, 2, listBeforeUpdate, multilingualIDList,
						userInfo);
				return getComponent(userInfo);
			} else {
				return new ResponseEntity<>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYEXISTS.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.CONFLICT);
			}
		}
	}

	@Override
	public ResponseEntity<Object> deleteComponent(Map<String, Object> inputMap, Component component, UserInfo userInfo)
			throws Exception {
		final List<String> multilingualIDList = new ArrayList<>();
		final List<Object> savedComponentList = new ArrayList<>();
		final Component componentByID = getActiveComponentById(component.getNcomponentcode(), userInfo);
		if (componentByID == null) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		} else {

			Map<?, ?> obj = (Map<?, ?>) ((Map<?, ?>) ((Map<?, ?>) ((Map<?, ?>) inputMap.get("genericlabel")).get("TestGroup")).get("jsondata"))
					.get("sdisplayname");
			String msglabel = (String) obj.get(userInfo.getSlanguagetypecode());

			final String query = "select '" + msglabel + "' as Msg from testgroupspecsampletype where ncomponentcode= "
					+ component.getNcomponentcode() + " and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			validatorDel = projectDAOSupport.getTransactionInfo(query, userInfo);
			boolean validRecord = false;
			if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
				validRecord = true;
				validatorDel = projectDAOSupport.validateDeleteRecord(Integer.toString(component.getNproductcatcode()),
						userInfo);
				if (validatorDel.getNreturnstatus() == Enumeration.Deletevalidator.SUCCESS.getReturnvalue()) {
					validRecord = true;
				} else {
					validRecord = false;
				}
			}
			if (validRecord) {
				final String updateQueryString = "update component set nstatus = "
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus() + ", dmodifieddate ='"
						+ dateUtilityFunction.getCurrentDateTime(userInfo) + "' where ncomponentcode="
						+ component.getNcomponentcode();
				jdbcTemplate.execute(updateQueryString);
				component.setNstatus((short) Enumeration.TransactionStatus.DELETED.gettransactionstatus());
				savedComponentList.add(component);

				Map<?, ?> obj1 = (Map<?, ?>) ((Map<?, ?>) ((Map<?, ?>) ((Map<?, ?>) inputMap.get("genericlabel")).get("DeleteComponent"))
						.get("jsondata")).get("sdisplayname");
				String deletelabel = (String) obj1.get(userInfo.getSlanguagetypecode());
				multilingualIDList.add(deletelabel);

				auditUtilityFunction.fnInsertAuditAction(savedComponentList, 1, null, multilingualIDList, userInfo);
				return getComponent(userInfo);
			} else {
				return new ResponseEntity<>(validatorDel.getSreturnmessage(), HttpStatus.EXPECTATION_FAILED);

			}
		}
	}

}
