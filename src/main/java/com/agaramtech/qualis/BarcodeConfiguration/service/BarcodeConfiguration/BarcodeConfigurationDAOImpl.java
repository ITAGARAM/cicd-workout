package com.agaramtech.qualis.BarcodeConfiguration.service.BarcodeConfiguration;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.SimpleDoc;
import javax.print.attribute.HashPrintServiceAttributeSet;
import javax.print.attribute.PrintServiceAttributeSet;
import javax.print.attribute.standard.PrinterName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import com.agaramtech.qualis.BarcodeConfiguration.model.BarcodeConfiguration;
import com.agaramtech.qualis.BarcodeConfiguration.model.BarcodeTemplate;
import com.agaramtech.qualis.basemaster.model.Barcode;
import com.agaramtech.qualis.basemaster.model.Printer;
import com.agaramtech.qualis.basemaster.service.unit.UnitDAOImpl;
import com.agaramtech.qualis.configuration.model.DesignTemplateMapping;
import com.agaramtech.qualis.dashboard.model.QueryBuilderTableColumns;
import com.agaramtech.qualis.dashboard.model.SQLQuery;
import com.agaramtech.qualis.dynamicpreregdesign.model.Combopojo;
import com.agaramtech.qualis.global.AuditUtilityFunction;
import com.agaramtech.qualis.global.CommonFunction;
import com.agaramtech.qualis.global.DateTimeUtilityFunction;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.FTPUtilityFunction;
import com.agaramtech.qualis.global.JdbcTemplateUtilityFunction;
import com.agaramtech.qualis.global.ProjectDAOSupport;
import com.agaramtech.qualis.global.StringUtilityFunction;
import com.agaramtech.qualis.global.UserInfo;
import com.agaramtech.qualis.global.ValidatorDel;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Repository
public class BarcodeConfigurationDAOImpl implements BarcodeConfigurationDAO {

private static final Logger LOGGER = LoggerFactory.getLogger(BarcodeConfigurationDAOImpl.class);
	
	private final StringUtilityFunction stringUtilityFunction;
	private final CommonFunction commonFunction;
	private final JdbcTemplate jdbcTemplate;
	private final JdbcTemplateUtilityFunction jdbcUtilityFunction;
	private final DateTimeUtilityFunction dateUtilityFunction;
	private final AuditUtilityFunction auditUtilityFunction;
	private final FTPUtilityFunction ftpUtilityFunction;

	@Override
	public ResponseEntity<Object> getBarcodeConfiguration(Integer nbarcodeconfigurationcode, UserInfo userInfo)
			throws Exception {
		Map<String, Object> objMap = new HashMap<String, Object>();

		if (nbarcodeconfigurationcode == null) {

			final String query = " select bt.nformcode,max(bt.nquerybuildertablecode) nquerybuildertablecode,"
					+ " max(bt.ncontrolcode) ncontrolcode,max(bt.nbarcodetemplatecode),max(bt.jsondata->>'nneedconfiguration') nneedconfiguration, "
					+ " max(bt.jsondata->>'screenfilter') screenfilter,max(bt.jsondata->>'nbarcodeprint') nbarcodeprint,"
					+ " max(qf.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
					+ "') sformname,max(bt.stablename) stablename,max(bt.stableprimarykeyname)stableprimarykeyname,max(q.nisdynamic) nisdynamic "
					+ " from barcodetemplate bt,qualisforms qf,querybuildertables q "
					+ " where  bt.nformcode=qf.nformcode and q.nquerybuildertablecode=bt.nquerybuildertablecode "
					+ "  and bt.ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
					+ "  and bt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "  and qf.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "  and bt.nsitecode="+userInfo.getNmastersitecode()+" group by bt.nformcode order by 1 desc ";

			List<BarcodeTemplate> lstBarcodeTemplateForm = jdbcTemplate.query(query, new BarcodeTemplate());

			objMap.put("Screen", lstBarcodeTemplateForm);

			if (!lstBarcodeTemplateForm.isEmpty()) {
				objMap.put("SelecetedScreen", lstBarcodeTemplateForm.get(0));

				final String queryControl = " select bt.nformcode,bt.nquerybuildertablecode,"
						+ " bt.ncontrolcode,bt.nbarcodetemplatecode,bt.jsondata->'nneedconfiguration' nneedconfiguration,"
						+ " bt.jsondata->'nsqlqueryneed' nsqlqueryneed,bt.jsondata->'nfiltersqlqueryneed' nfiltersqlqueryneed,bt.jsondata->'nsqlquerycode' nsqlquerycode, "
						+ " bt.jsondata->'nbarcodeprint' nbarcodeprint," + " qf.jsondata->'scontrolids'->>'"
						+ userInfo.getSlanguagetypecode() + "' scontrolname,"
						+ " bt.stablename,bt.stableprimarykeyname ,bt.jsondata->'screenfilter' jnumericcolumns"
						+ " from barcodetemplate bt,controlmaster qf "
						+ " where  bt.ncontrolcode=qf.ncontrolcode  and bt.nformcode="
						+ lstBarcodeTemplateForm.get(0).getNformcode() + "" + " and ntransactionstatus="
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + "  and bt.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and qf.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bt.nsitecode="+userInfo.getNmastersitecode()+" order by 1 desc ";

				List<BarcodeTemplate> lstBarcodeTemplateControl = jdbcTemplate.query(queryControl,
						new BarcodeTemplate());

				objMap.put("Control", lstBarcodeTemplateControl);

				if (!lstBarcodeTemplateControl.isEmpty()) {
					objMap.put("SelecetedControl", lstBarcodeTemplateControl.get(0));

					if (lstBarcodeTemplateControl.get(0).isNneedconfiguration()) {

						List<Object> listScreenFilter = lstBarcodeTemplateControl.get(0).getJnumericcolumns();

						// JsonArray jsonArray = new JsonArray();

						Map<String, Object> compo = iterateComponent(listScreenFilter);
						List<Map<String, Object>> objComponent = (List<Map<String, Object>>) compo.get("comboComponent");
						objMap.put("ComboComponnet", objComponent);

						Map<String, Object> objData = new LinkedHashMap<>();

						objData = childCombo(objComponent, objData);

						Map<String, Object> filterData = (Map<String, Object>) getComboValuesForBarcode(objData,userInfo, true).getBody();

						objMap.putAll(filterData);

						Map<String, Object> data = (Map<String, Object>) objMap.get("Selected" + objComponent.get(objComponent.size() - 1).get("label"));

						if (data != null) {
							String str = "";

							if (!objComponent.isEmpty()) {
								Map<String, Object> obj = objComponent.get(objComponent.size() - 1);
								// Map<String, Object> objVal = (Map<String, Object>)
								// data.get(obj.get("valuemember"));
								str = "and bc.jsondata->'value'='" + data.get(obj.get("valuemember")) + "'";

							}
//						'

							final String barcodeConfig = "select bc.*,b.sbarcodename,t.jsondata->'stransdisplaystatus'->>'"
									+ userInfo.getSlanguagetypecode()
									+ "' stransdisplaystatus, t1.jsondata->'stransdisplaystatus'->>'"
									+ userInfo.getSlanguagetypecode() + "'  sneedconfiguration , "
									+ "t2.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
									+ "'  sbarcodeprint ," + " t3.jsondata->'stransdisplaystatus'->>'"
									+ userInfo.getSlanguagetypecode() + "'  ssqlqueryneed ,"
									+ " t4.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
									+ "' sfiltersqlqueryneed,"
									+ "sql.ssqlqueryname  from barcodeconfiguration bc,barcodetemplate bt,"
									+ " barcode b,transactionstatus t,transactionstatus t1,transactionstatus t2,transactionstatus t3,transactionstatus t4,sqlquery sql where"
									+ "  bc.nformcode=" + lstBarcodeTemplateForm.get(0).getNformcode()
									+ " and bc.ncontrolcode=" + lstBarcodeTemplateControl.get(0).getNcontrolcode() + ""
									+ " and bt.nbarcodetemplatecode=bc.nbarcodetemplatecode and bc.nbarcodetemplatecode="
									+ lstBarcodeTemplateControl.get(0).getNbarcodetemplatecode() + ""
									+ " and b.nbarcode=bc.nbarcode and bc.ntransactionstatus=t.ntranscode and bc.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and t1.ntranscode=case when cast (bt.jsondata->>'nneedconfiguration' as boolean) then 3 else 4 end "
									+ " and t2.ntranscode=case when cast (bt.jsondata->>'nbarcodeprint' as boolean)  then 3 else 4 end "
									+ " and t3.ntranscode=case when cast (bt.jsondata->>'nsqlqueryneed' as boolean)  then 3 else 4 end "
									+ " and t4.ntranscode=case when cast (bt.jsondata->>'nfiltersqlqueryneed' as boolean)  then 3 else 4 end "
									+ " and sql.nsqlquerycode=cast (bt.jsondata->>'nsqlquerycode'as int) " + str
									+ " and bt.nsitecode="+userInfo.getNmastersitecode()+" and bc.nsitecode="+userInfo.getNmastersitecode()+" "
									+ " and b.nsitecode="+userInfo.getNmastersitecode()+" and  sql.nsitecode="+userInfo.getNmastersitecode()+" order by nbarcodeconfigurationcode desc";

							List<BarcodeConfiguration> lstBarcodeConfig = jdbcTemplate.query(barcodeConfig,
									new BarcodeConfiguration());
							objMap.put("BarcodeConfiguration", lstBarcodeConfig);

							if (!lstBarcodeConfig.isEmpty()) {
								objMap.put("SelectedBarcodeConfiguration", lstBarcodeConfig.get(0));
							}

						} else {
							objMap.put("BarcodeConfiguration", new ArrayList<>());
							objMap.put("SelectedBarcodeConfiguration", null);

						}

					} else {

						objMap.put("ComboComponnet", new ArrayList<>());

						final String barcodeConfig = "select bc.*,b.sbarcodename,t.jsondata->'stransdisplaystatus'->>'"
								+ userInfo.getSlanguagetypecode()
								+ "' stransdisplaystatus, t1.jsondata->'stransdisplaystatus'->>'"
								+ userInfo.getSlanguagetypecode() + "'  sneedconfiguration , "
								+ "t2.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
								+ "'  sbarcodeprint ," + " t3.jsondata->'stransdisplaystatus'->>'"
								+ userInfo.getSlanguagetypecode() + "'  ssqlqueryneed ,"
								+ " t4.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
								+ "' sfiltersqlqueryneed,"
								+ " sql.ssqlqueryname  from barcodeconfiguration bc,barcodetemplate bt,"
								+ " barcode b,transactionstatus t,transactionstatus t1,transactionstatus t2,transactionstatus t3,transactionstatus t4,sqlquery sql where"
								+ "  bc.nformcode=" + lstBarcodeTemplateForm.get(0).getNformcode()
								+ " and bc.ncontrolcode=" + lstBarcodeTemplateControl.get(0).getNcontrolcode() + ""
								+ " and bt.nbarcodetemplatecode=bc.nbarcodetemplatecode and bc.nbarcodetemplatecode="
								+ lstBarcodeTemplateControl.get(0).getNbarcodetemplatecode() + ""
								+ " and b.nbarcode=bc.nbarcode and bc.ntransactionstatus=t.ntranscode and bc.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
								+ " and t1.ntranscode=case when cast (bt.jsondata->>'nneedconfiguration' as boolean) then 3 else 4 end "
								+ " and t2.ntranscode=case when cast (bt.jsondata->>'nbarcodeprint'as boolean)  then 3 else 4 end "
								+ " and t3.ntranscode=case when cast (bt.jsondata->>'nsqlqueryneed'as boolean)  then 3 else 4 end "
								+ " and t4.ntranscode=case when cast (bt.jsondata->>'nfiltersqlqueryneed' as boolean)  then 3 else 4 end "
								+ " and sql.nsqlquerycode=cast (bt.jsondata->>'nsqlquerycode'as int) "
								+ " and bt.nsitecode="+userInfo.getNmastersitecode()+" and bc.nsitecode="+userInfo.getNmastersitecode()+" "
								+ " and b.nsitecode="+userInfo.getNmastersitecode()+" and sql.nsitecode="+userInfo.getNmastersitecode()+" order by nbarcodeconfigurationcode desc";

						List<BarcodeConfiguration> lstBarcodeConfig = jdbcTemplate.query(barcodeConfig,
								new BarcodeConfiguration());

						objMap.put("BarcodeConfiguration", lstBarcodeConfig);

						if (!lstBarcodeConfig.isEmpty()) {
							objMap.put("SelectedBarcodeConfiguration", lstBarcodeConfig.get(0));
						} else {
							objMap.put("SelectedBarcodeConfiguration", null);
						}

					}

				} else {
					objMap.put("ComboComponnet", new ArrayList<>());
					objMap.put("BarcodeConfiguration", new ArrayList<>());
					objMap.put("SelectedBarcodeConfiguration", null);
					objMap.put("SelecetedControl", null);

				}

			} else {

				objMap.put("ComboComponnet", new ArrayList<>());
				objMap.put("BarcodeConfiguration", new ArrayList<>());
				objMap.put("SelectedBarcodeConfiguration", null);
				objMap.put("Control", new ArrayList<>());
				objMap.put("SelecetedScreen", null);
				objMap.put("SelecetedControl", null);

			}
		} else {

			final String barcodeConfig = "select bc.*,b.sbarcodename,t.jsondata->'stransdisplaystatus'->>'"
					+ userInfo.getSlanguagetypecode() + "' stransdisplaystatus, t1.jsondata->'stransdisplaystatus'->>'"
					+ userInfo.getSlanguagetypecode() + "'  sneedconfiguration , "
					+ "t2.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "'  sbarcodeprint ,"
					+ " t3.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
					+ "'  ssqlqueryneed ,sql.ssqlqueryname  from barcodeconfiguration bc,barcodetemplate bt,"
					+ " barcode b,transactionstatus t,transactionstatus t1,transactionstatus t2,transactionstatus t3,sqlquery sql where"
					+ "  bt.nbarcodetemplatecode=bc.nbarcodetemplatecode and bc.nbarcodeconfigurationcode="
					+ nbarcodeconfigurationcode + ""
					+ " and b.nbarcode=bc.nbarcode and bc.ntransactionstatus=t.ntranscode and bc.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and t1.ntranscode=case when cast (bt.jsondata->>'nneedconfiguration' as boolean) then 3 else 4 end "
					+ " and t2.ntranscode=case when cast (bt.jsondata->>'nbarcodeprint'as boolean)  then 3 else 4 end "
					+ " and t3.ntranscode=case when cast (bt.jsondata->>'nsqlqueryneed'as boolean)  then 3 else 4 end "
					+ " and bc.nsitecode="+userInfo.getNmastersitecode()+" and bt.nsitecode="+userInfo.getNmastersitecode()+" "
					+ " and b.nsitecode="+userInfo.getNmastersitecode()+" and sql.nsitecode="+userInfo.getNmastersitecode()+" and sql.nsqlquerycode=cast (bt.jsondata->>'nsqlquerycode'as int) ";


			BarcodeConfiguration lstBarcodeConfig = (BarcodeConfiguration) jdbcUtilityFunction.queryForObject(barcodeConfig,
					BarcodeConfiguration.class,jdbcTemplate);

			if (lstBarcodeConfig != null) {

				objMap.put("SelectedBarcodeConfiguration", lstBarcodeConfig);
			} else {
				return new ResponseEntity<Object>(
						commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
								userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		}

		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	public Map<String, Object> childCombo(List<Map<String, Object>> data, Map<String, Object> objChild) {
		Map<String, Object> returnMap = new HashMap<>();
		for (int i = 0; data.size() > i; i++) {

			if (!objChild.containsKey(data.get(i).get("label"))) {

				if (data.get(i).containsKey("child")) {

					List<Map<String, Object>> child = (List<Map<String, Object>>) data.get(i).get("child");

					List<Map<String, Object>> objData = new ArrayList<Map<String, Object>>();

					for (int j = 0; child.size() > j; j++) {
						final int l = j;

						List<Map<String, Object>> mapData = data.stream()
								.filter(x -> x.get("label").equals(child.get(l).get("label")))
								.collect(Collectors.toList());


						if (!mapData.isEmpty()) {
							objData.add(mapData.get(0));

							data = data.stream().filter(x -> !x.get("label").equals(mapData.get(0).get("label")))
									.collect(Collectors.toList());
						}
					}
					objChild.put((String) data.get(i).get("label"), objData);

					returnMap.put("childcolumnlist", objChild);
					returnMap.put("parentcolumnlist", data);

					if (!objData.isEmpty()) {

						for (int k = 0; objData.size() > k; k++) {

							if (objData.get(k).containsKey("child")) {
								Map<String, Object> objChildData = childCombo(data, objChild);
								objChild.putAll(
										(Map<? extends String, ? extends Object>) objChildData.get("childcolumnlist"));
								returnMap.put("parentcolumnlist", objChild.get("parentcolumnlist"));
								data = (List<Map<String, Object>>) objChild.get("parentcolumnlist");
							}
						}

					}

				} else {
					returnMap.put("parentcolumnlist", data);
				}

			} else {
				returnMap.put("parentcolumnlist", data);
			}
		}
		return returnMap;

	}

	public Map<String, Object> iterateComponent(List<Object> obj) {
		final Map<String, Object> objMap = new HashMap<>();

		List<Map<String, Object>> comboComponent = new ArrayList<Map<String, Object>>();

		List<Map<String, Object>> Component = new ArrayList<Map<String, Object>>();

		for (int i = 0; i < obj.size(); i++) {

			Map<String, Object> map = (Map<String, Object>) obj.get(i);

			List<Map<String, Object>> lstChildrean = (List<Map<String, Object>>) map.get("children");

			if (lstChildrean.size() > 0) {

				for (int j = 0; lstChildrean.size() > j; j++) {

					Map<String, Object> mapColumn = (Map<String, Object>) lstChildrean.get(j);

					List<Map<String, Object>> lstChildrenColumn = (List<Map<String, Object>>) mapColumn.get("children");

					for (int k = 0; lstChildrenColumn.size() > k; k++) {

						Map<String, Object> mapComponnet = (Map<String, Object>) lstChildrenColumn.get(k);

						if (mapComponnet.containsKey("children")) {

							List<Map<String, Object>> lstChildrencomponentrow = (List<Map<String, Object>>) mapComponnet
									.get("children");

							for (int z = 0; lstChildrencomponentrow.size() > z; z++) {

								Map<String, Object> mapComponnet1 = (Map<String, Object>) lstChildrenColumn.get(z);

								if (mapComponnet1.get("inputtype").equals("combo")) {
									comboComponent.add(mapComponnet1);
								}
								Component.add(mapComponnet1);

							}

						} else {

							if (mapComponnet.get("inputtype").equals("combo")) {
								comboComponent.add(mapComponnet);
							}
							Component.add(mapComponnet);

						}

					}

				}

			}

		}
		objMap.put("comboComponent", comboComponent);
		objMap.put("Component", Component);
		return objMap;
	}

	@SuppressWarnings("unchecked")
	public ResponseEntity<Object> getComboValuesForBarcode(Map<String, Object> inputMap, UserInfo userInfo,
			boolean selectedNeed) throws Exception {
		String tableName = "";
		List<Map<String, Object>> srcData = (List<Map<String, Object>>) inputMap.get("parentcolumnlist");
		Map<String, Object> childData = (Map<String, Object>) inputMap.get("childcolumnlist");
		String getJSONKeysQuery = "";

		Map<String, Object> returnObject = new HashMap<>();
		for (int i = 0; i < srcData.size(); i++) {
			String sourceName = (String) srcData.get(i).get("source");
			String conditionString = srcData.get(i).containsKey("conditionstring")
					? (String) srcData.get(i).get("conditionstring")
					: "";
			String Keysofparam = "";

			while (conditionString.contains("P$")) {
				StringBuilder sb = new StringBuilder(conditionString);
				int firstindex = sb.indexOf("P$");
				int lastindex = sb.indexOf("$P");
				Keysofparam = sb.substring(firstindex + 2, lastindex);
				if (Keysofparam.contains(".")) {
					int index = Keysofparam.indexOf(".");
					String tablename = Keysofparam.substring(0, index);
					String columnName = Keysofparam.substring(index + 1, Keysofparam.length());
					if (inputMap.containsKey(tablename)) {
						Map<String, Object> userInfoMap = (Map<String, Object>) inputMap.get(tablename);
						if (userInfoMap.containsKey(columnName)) {
							sb.replace(firstindex, lastindex + 2, userInfoMap.get(columnName).toString());
						}
					}
				}
				conditionString = sb.toString();
			}

			tableName = sourceName.toLowerCase();

			final String getJSONFieldQuery = "select string_agg(column_name ,'||')FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '"
					+ tableName + "' and data_type = 'jsonb'";
			String jsonField = jdbcTemplate.queryForObject(getJSONFieldQuery, String.class);
			jsonField = jsonField != null ? "||" + jsonField : "";

			final String getFieldQuery = "select string_agg(column_name ,'||')FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = '"
					+ tableName + "'";
			String fields = jdbcTemplate.queryForObject(getFieldQuery, String.class);
			if (fields.contains(srcData.get(i).get("valuemember").toString())) {
				getJSONKeysQuery = "select TO_JSON(" + tableName + ".*)::jsonb" + jsonField + " as jsondata" + " from "
						+ tableName + " where nstatus =" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " and " + "\"" + srcData.get(i).get("valuemember") + "\"" + " > '0' " + conditionString
						+ " ;";
			} else {
				getJSONKeysQuery = "select TO_JSON(" + tableName + ".*)::jsonb" + jsonField + " as jsondata" + " from "
						+ tableName + " where nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
						+ " " + conditionString + " ;";
			}

			List<Combopojo> data = jdbcTemplate.query(getJSONKeysQuery, new Combopojo());

			String label = (String) srcData.get(i).get("label");

			if (selectedNeed) {

				if (!data.isEmpty()) {
					Map<String, Object> childDatas = data.get(0).getJsondata();
					returnObject.put("Selected" + label, childDatas);
				}

				if (!data.isEmpty() && childData != null && childData.containsKey(label)) {

					Map<String, Object> childDatas = data.get(0).getJsondata();
					returnObject.put("Selected" + label, childDatas);

					Map<String, Object> datas = recursiveChildComboForBarcode(childDatas, childData, srcData.get(i),
							label, (String) srcData.get(i).get("valuemember"), tableName, userInfo, inputMap,
							selectedNeed);
					returnObject.putAll(datas);

				}
			}
			returnObject.put(label, data);
		}
		return new ResponseEntity<>(returnObject, HttpStatus.OK);
	}

	private Map<String, Object> recursiveChildComboForBarcode(Map<String, Object> childDatas,
			Map<String, Object> childData, Map<String, Object> map, String label, String valueMember, String tableName,
			UserInfo userInfo, Map<String, Object> inputMap, boolean selectedNeed) throws Exception {

		final String parentValumember = valueMember;
		final Map<String, Object> objReturn = new HashMap<>();
		List<Map<String, Object>> childcomboData = (List<Map<String, Object>>) childData.get(label);
		for (int z = 0; z < childcomboData.size(); z++) {
			if (!childcomboData.get(z).containsKey("readonly") || (childcomboData.get(z).containsKey("readonly")
					&& ((boolean) childcomboData.get(z).get("readonly")) == false)) {
				final String sourceName1 = (String) childcomboData.get(z).get("source");
				String labelchild = (String) childcomboData.get(z).get("label");
				List<Map<String, Object>> objChild = (List<Map<String, Object>>) ((List<Map<String, Object>>) map
						.get("child")).stream().filter(x -> x.get("label").equals(labelchild))
								.collect(Collectors.toList());

				final String valuememberchild = (String) childcomboData.get(z).get("valuemember");
				final String tableNamechild = sourceName1.toLowerCase();
				String Keysofparam = "";
				String conditionString = childcomboData.get(z).containsKey("conditionstring")
						? (String) childcomboData.get(z).get("conditionstring")
						: "";

				while (conditionString.contains("P$")) {
					StringBuilder sb = new StringBuilder(conditionString);
					int firstindex = sb.indexOf("P$");
					int lastindex = sb.indexOf("$P");
					Keysofparam = sb.substring(firstindex + 2, lastindex);
					if (Keysofparam.contains(".")) {
						int index = Keysofparam.indexOf(".");
						String tablename = Keysofparam.substring(0, index);
						String columnName = Keysofparam.substring(index + 1, Keysofparam.length());
						if (inputMap.containsKey(tablename)) {
							Map<String, Object> userInfoMap = (Map<String, Object>) inputMap.get(tablename);
							if (userInfoMap.containsKey(columnName)) {
								sb.replace(firstindex, lastindex + 2, userInfoMap.get(columnName).toString());
							}
						}
					}
					conditionString = sb.toString();

				}
				String valuememberData = valueMember;

				String getJSONFieldQuery = "select string_agg(column_name ,'||')FROM INFORMATION_SCHEMA. COLUMNS WHERE TABLE_NAME = '"
						+ tableNamechild + "' and data_type = 'jsonb'";
				String jsonField = jdbcTemplate.queryForObject(getJSONFieldQuery, String.class);

				getJSONFieldQuery = "select string_agg(column_name ,'||')FROM INFORMATION_SCHEMA. COLUMNS WHERE TABLE_NAME = '"
						+ tableNamechild + "' ";
				String jsonchildField = jdbcTemplate.queryForObject(getJSONFieldQuery, String.class);
				if (!(jsonchildField.contains(valueMember + "||") || jsonchildField.contains("||" + valueMember + "||")
						|| jsonchildField.contains("||" + valueMember))) {
					String tablecolumnname = objChild.isEmpty() ? "" : (String) objChild.get(0).get("tablecolumnname");

					String foreignPk = objChild.isEmpty() ? "" : (String) objChild.get(0).get("foriegntablePK");

					if (!childDatas.containsKey(tablecolumnname)) {
						valueMember = tablecolumnname;
						valuememberData = foreignPk;
					} else {
						valueMember = foreignPk;
						valuememberData = tablecolumnname;
					}
				}
				String defaultvalues = "";
				if (childcomboData.get(z).containsKey("defaultvalues")) {
					List<Map<String, Object>> defaulvalueslst = (List<Map<String, Object>>) childcomboData.get(z)
							.get("defaultvalues");
					for (int j = 0; j < defaulvalueslst.size(); j++) {
						if (defaulvalueslst.get(j).containsKey(
								childDatas.get(childcomboData.get(z).get("parentprimarycode")).toString())) {
							defaultvalues = " in ( " + defaulvalueslst.get(j)
									.get(childDatas.get(childcomboData.get(z).get("parentprimarycode")).toString())
									.toString() + " )";
						}
					}

				} else {
					if (!objChild.isEmpty() && objChild.get(0).containsKey("isDynamicMapping")
							&& (boolean) objChild.get(0).get("isDynamicMapping")) {
						if (childDatas.containsKey("jsondata")) {
							String tablecolumnname = (String) objChild.get(0).get("tablecolumnname");
							Map<String, Object> objJsonData = (Map<String, Object>) childDatas.get("jsondata");
							if (objJsonData.containsKey(tablecolumnname)) {
								Map<String, Object> objJsonData1 = (Map<String, Object>) objJsonData
										.get(tablecolumnname);
								defaultvalues = " = '" + objJsonData1.get("value") + "'";

							} else {
								defaultvalues = " = '" + childDatas.get(valuememberData) + "'";
							}
						} else {
							defaultvalues = " = '" + childDatas.get(valuememberData) + "'";
						}

					} else if (parentValumember.equals("ndynamicmastercode") && !objChild.isEmpty()
							&& (boolean) objChild.get(0).get("isDynamicMapping") == false) {
						if (childDatas.containsKey("jsondata")) {
							String tablecolumnname = (String) objChild.get(0).get("tablecolumnname");
							Map<String, Object> objJsonData = (Map<String, Object>) childDatas.get("jsondata");
							if (objJsonData.containsKey(tablecolumnname)) {
								Map<String, Object> objJsonData1 = (Map<String, Object>) objJsonData
										.get(tablecolumnname);
								defaultvalues = " = '" + objJsonData1.get("value") + "'";

							} else {
								defaultvalues = " = '" + childDatas.get(valuememberData) + "'";
							}
						} else {
							defaultvalues = " = '" + childDatas.get(valuememberData) + "'";
						}
					} else {
						defaultvalues = " = '" + childDatas.get(valuememberData) + "'";
					}
				}
				String name = "";
				if (childcomboData.get(z).containsKey("name")) {
					name = (String) childcomboData.get(z).get("name");
					if ((name.equals("manualorderid") || name.equals("manualsampleid"))
							&& childDatas.containsKey("nordertypecode")
							&& (int) childDatas.get("nordertypecode") == 1) {
						conditionString = conditionString + "  limit 1";
					} else if ((name.equals("manualorderid") || name.equals("manualsampleid"))
							&& childDatas.containsKey("nordertypecode")
							&& (int) childDatas.get("nordertypecode") == 2) {
						if (conditionString.contains("nusercode")) {
							String first = conditionString.substring(0, conditionString.indexOf("nusercode = ") + 12);
							String second = conditionString.substring(conditionString.indexOf("nusercode = ") + 12);
							if (second.contains("and")) {
								String third = second.substring(second.indexOf("and"));
								first = first + "-1 " + third;
								conditionString = first;
							} else {
								first = first + "-1";
								conditionString = first;
							}
						}
					}
				}
				if (!objChild.isEmpty() && objChild.get(0).containsKey("isDynamicMapping")
						&& (boolean) objChild.get(0).get("isDynamicMapping")) {
					String tablecolumnname = (String) objChild.get(0).get("tablecolumnname");
					if (!(jsonchildField.contains(tablecolumnname + "||")
							|| jsonchildField.contains("||" + tablecolumnname + "||")
							|| jsonchildField.contains("||" + tablecolumnname))) {
						valueMember = "ndynamicmastercode";
					} else {
						valueMember = "jsondata->" + "'" + tablecolumnname + "'->>'value'";
					}
				} else {
					valueMember = "\"" + valueMember + "\"";
				}
				jsonField = jsonField != null ? "||" + jsonField : "";
				getJSONFieldQuery = "select TO_JSON(" + tableNamechild + ".*)::jsonb" + jsonField + "  as jsondata"
						+ " from " + tableNamechild + " where nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  " + valueMember
						+ defaultvalues + conditionString + ";";
				List<Combopojo> data = jdbcTemplate.query(getJSONFieldQuery, new Combopojo());

				if (selectedNeed) {

					if (!data.isEmpty()) {
						Map<String, Object> child = data.get(0).getJsondata();
						objReturn.put("Selected" + labelchild, child);
					}

					if (!data.isEmpty() && childData != null && childData.containsKey(labelchild)) {

						Map<String, Object> child = data.get(0).getJsondata();

						final Map<String, Object> datas = recursiveChildComboForBarcode(child, childData,
								childcomboData.get(z), labelchild, valuememberchild, tableNamechild, userInfo, inputMap,
								selectedNeed);
						objReturn.putAll(datas);

					}
				}
				objReturn.put(labelchild, data);

			}
		}
		return objReturn;
	}

	@Override
	public ResponseEntity<Object> getBarcodeDynamicChange(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Map<String, Object> objMap = new HashMap<String, Object>();

		Integer nformcode = (Integer) inputMap.get("nformcode");

		final String query = " select bt.nformcode,max(bt.nquerybuildertablecode) nquerybuildertablecode,"
				+ " max(bt.ncontrolcode) ncontrolcode,max(bt.nbarcodetemplatecode),max(bt.jsondata->>'nneedconfiguration') nneedconfiguration, "
				+ " max(bt.jsondata->>'screenfilter') screenfilter,max(bt.jsondata->>'nbarcodeprint') nbarcodeprint,"
				+ " max(qf.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode()
				+ "') sformname,max(bt.stablename) stablename,max(bt.stableprimarykeyname)stableprimarykeyname,max(q.nisdynamic) nisdynamic "
				+ " from barcodetemplate bt,qualisforms qf,querybuildertables q "
				+ " where  bt.nformcode=qf.nformcode and q.nquerybuildertablecode=bt.nquerybuildertablecode "
				+ "  and bt.ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
				+ "  and bt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "  and qf.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and bt.nformcode=" + nformcode + " and bt.nsitecode="+userInfo.getNmastersitecode()+" group by bt.nformcode order by 1 desc ";

		BarcodeTemplate BarcodeTemplateForm = (BarcodeTemplate) jdbcUtilityFunction.queryForObject(query, BarcodeTemplate.class,jdbcTemplate);

		if (BarcodeTemplateForm != null) {
			final String queryControl = " select bt.nformcode,bt.nquerybuildertablecode,"
					+ " bt.ncontrolcode,bt.nbarcodetemplatecode,bt.jsondata->'nneedconfiguration' nneedconfiguration,"
					+ " bt.jsondata->'nsqlqueryneed' nsqlqueryneed,bt.jsondata->'nsqlquerycode' nsqlquerycode, "
					+ " bt.jsondata->'nbarcodeprint' nbarcodeprint," + " qf.jsondata->'scontrolids'->>'"
					+ userInfo.getSlanguagetypecode() + "' scontrolname,"
					+ " bt.stablename,bt.stableprimarykeyname ,bt.jsondata->'screenfilter' jnumericcolumns,"
					+ " bt.jsondata->'nfiltersqlqueryneed' nfiltersqlqueryneed "
					+ " from barcodetemplate bt,controlmaster qf "
					+ " where  bt.ncontrolcode=qf.ncontrolcode  and bt.nformcode=" + BarcodeTemplateForm.getNformcode()
					+ "" + " and ntransactionstatus=" + Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
					+ "  and bt.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ "  and qf.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
					+ " and bt.nsitecode="+userInfo.getNmastersitecode()+" order by 1 desc ";
			List<BarcodeTemplate> lstBarcodeTemplateControl = jdbcTemplate.query(queryControl, new BarcodeTemplate());
			objMap.put("Control", lstBarcodeTemplateControl);
		} else {
			objMap.put("ComboComponnet", new ArrayList<>());
			objMap.put("Control", new ArrayList<>());
		}
		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getBarcodeDynamicCombo(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		Integer ncontrolcode = (Integer) inputMap.get("ncontrolcode");
		Integer nbarcodetemplatecode = (Integer) inputMap.get("nbarcodetemplatecode");

		Map<String, Object> objMap = new HashMap<String, Object>();

		final String queryControl = " select bt.nformcode,bt.nquerybuildertablecode,"
				+ " bt.ncontrolcode,bt.nbarcodetemplatecode,bt.jsondata->'nneedconfiguration' nneedconfiguration, "
				+ " bt.jsondata->'nbarcodeprint' nbarcodeprint," + " qf.jsondata->'scontrolids'->>'"
				+ userInfo.getSlanguagetypecode() + "' scontrolname,"
				+ " bt.stablename,bt.stableprimarykeyname ,bt.jsondata->'screenfilter' jnumericcolumns"
				+ " from barcodetemplate bt,controlmaster qf "
				+ " where  bt.ncontrolcode=qf.ncontrolcode  and bt.ncontrolcode=" + ncontrolcode
				+ " and bt.nbarcodetemplatecode=" + nbarcodetemplatecode + " and bt.nsitecode="+userInfo.getNmastersitecode()+" order by 1 desc ";

		BarcodeTemplate BarcodeTemplateControl = (BarcodeTemplate) jdbcUtilityFunction.queryForObject(queryControl,
				BarcodeTemplate.class,jdbcTemplate);

		if (BarcodeTemplateControl != null) {
			if (BarcodeTemplateControl.isNneedconfiguration()) {

				List<Object> listScreenFilter = BarcodeTemplateControl.getJnumericcolumns();

				Map<String, Object> compo = iterateComponent(listScreenFilter);
				List<Map<String, Object>> objComponent = (List<Map<String, Object>>) compo.get("comboComponent");
				objMap.put("ComboComponnet", objComponent);

				// objComponent.get(objComponent.size() - 1);
				Map<String, Object> objData = new HashMap();

				objData = childCombo(objComponent, objData);

				Map<String, Object> filterData = (Map<String, Object>) getComboValuesForBarcode(objData, userInfo,
						false).getBody();

				objMap.putAll(filterData);

			} else {

				objMap.put("ComboComponnet", new ArrayList());

			}

		} else {
			objMap.put("ComboComponnet", null);
		}

		return new ResponseEntity<>(objMap, HttpStatus.OK);
	}

	public ResponseEntity<Object> getChildValuesForBarcodeConfiguration(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		String tableName = "";
		final List<Map<String, Object>> srcData = (List<Map<String, Object>>) inputMap.get("parentcolumnlist");
		Map<String, Object> childData = (Map<String, Object>) inputMap.get("childcolumnlist");
		Map<String, Object> parentData = (Map<String, Object>) inputMap.get("parentdata");
		final Map<String, Object> returnObject = new HashMap<>();
		String valuememberData = "";
		for (int i = 0; i < srcData.size(); i++) {

			if (!srcData.get(i).containsKey("readonly") || (srcData.get(i).containsKey("readonly")
					&& ((boolean) srcData.get(i).get("readonly")) == false)) {
				String valuemember = (String) inputMap.get("valuemember");
				final String sourceName = (String) srcData.get(i).get("source");
				final String valuememberChild = (String) srcData.get(i).get("valuemember");
				final String label = (String) srcData.get(i).get("label");

				List<Map<String, Object>> objChild = (List<Map<String, Object>>) ((List<Map<String, Object>>) inputMap
						.get("child")).stream().filter(x -> x.get("label").equals(label)).collect(Collectors.toList());

				String Keysofparam = "";
				String conditionString = srcData.get(i).containsKey("conditionstring")
						? (String) srcData.get(i).get("conditionstring")
						: "";

				while (conditionString.contains("P$")) {
					StringBuilder sb = new StringBuilder(conditionString);
					int firstindex = sb.indexOf("P$");
					int lastindex = sb.indexOf("$P");
					Keysofparam = sb.substring(firstindex + 2, lastindex);
					if (Keysofparam.contains(".")) {
						int index = Keysofparam.indexOf(".");
						String tablename = Keysofparam.substring(0, index);
						String columnName = Keysofparam.substring(index + 1, Keysofparam.length());
						if (inputMap.containsKey(tablename)) {
							Map<String, Object> userInfoMap = (Map<String, Object>) inputMap.get(tablename);
							if (userInfoMap.containsKey(columnName)) {
								sb.replace(firstindex, lastindex + 2, userInfoMap.get(columnName).toString());
							}
						}
					}
					conditionString = sb.toString();

				}
				tableName = sourceName.toLowerCase();
				valuememberData = valuemember;

				String getJSONFieldQuery = "select string_agg(column_name ,'||')FROM INFORMATION_SCHEMA. COLUMNS WHERE TABLE_NAME = '"
						+ tableName + "' and data_type = 'jsonb'";
				String jsonField = jdbcTemplate.queryForObject(getJSONFieldQuery, String.class);

				getJSONFieldQuery = "select string_agg(column_name ,'||')FROM INFORMATION_SCHEMA. COLUMNS WHERE TABLE_NAME = '"
						+ tableName + "' ";
				String jsonchildField = jdbcTemplate.queryForObject(getJSONFieldQuery, String.class);
				if (!(jsonchildField.contains(valuemember + "||") || jsonchildField.contains("||" + valuemember + "||")
						|| jsonchildField.contains("||" + valuemember))) {

					String tablecolumnname = objChild.isEmpty() ? "" : (String) objChild.get(0).get("tablecolumnname");
					String foreignPk = objChild.isEmpty() ? "" : (String) objChild.get(0).get("foriegntablePK");

					if (!parentData.containsKey(tablecolumnname)) {
						valuemember = tablecolumnname;
						valuememberData = foreignPk;
					} else {

						valuemember = foreignPk;
						valuememberData = tablecolumnname;
					}

				}

				jsonField = jsonField != null ? "||" + jsonField : "";

				String defaultvalues = "";
				if (srcData.get(i).containsKey("defaultvalues")) {
					List<Map<String, Object>> defaulvalueslst = (List<Map<String, Object>>) srcData.get(i)
							.get("defaultvalues");
					for (int j = 0; j < defaulvalueslst.size(); j++) {
						if (defaulvalueslst.get(j)
								.containsKey(parentData.get(srcData.get(i).get("parentprimarycode")).toString())) {
							defaultvalues = " in ( " + defaulvalueslst.get(j)
									.get(parentData.get(srcData.get(i).get("parentprimarycode")).toString()).toString()
									+ " )";
						}
					}

				} else {
					if (!objChild.isEmpty() && objChild.get(0).containsKey("isDynamicMapping")
							&& (boolean) objChild.get(0).get("isDynamicMapping")) {

						if (parentData.containsKey("jsondata")) {
							String tablecolumnname = (String) objChild.get(0).get("tablecolumnname");
							Map<String, Object> objJsonData = (Map<String, Object>) parentData.get("jsondata");
							if (objJsonData.containsKey(tablecolumnname)) {
								Map<String, Object> objJsonData1 = (Map<String, Object>) objJsonData
										.get(tablecolumnname);
								defaultvalues = " = '" + objJsonData1.get("value") + "'";

							} else {
								defaultvalues = " = '" + parentData.get(valuememberData) + "'";
							}
						} else {
							defaultvalues = " = '" + parentData.get(valuememberData) + "'";
						}

					} else if (((String) inputMap.get("valuemember")).equals("ndynamicmastercode")
							&& !objChild.isEmpty() && (boolean) objChild.get(0).get("isDynamicMapping") == false) {

						if (parentData.containsKey("jsondata")) {
							String tablecolumnname = (String) objChild.get(0).get("tablecolumnname");
							Map<String, Object> objJsonData = (Map<String, Object>) parentData.get("jsondata");
							if (objJsonData.containsKey(tablecolumnname)) {
								Map<String, Object> objJsonData1 = (Map<String, Object>) objJsonData
										.get(tablecolumnname);
								defaultvalues = " = '" + objJsonData1.get("value") + "'";

							} else {
								defaultvalues = " = '" + parentData.get(valuememberData) + "'";
							}
						} else {
							defaultvalues = " = '" + parentData.get(valuememberData) + "'";
						}

					}

					else {
						defaultvalues = " = '" + parentData.get(valuememberData) + "'";
					}

				}

				String name = "";
				if (srcData.get(i).containsKey("name")) {
					name = (String) srcData.get(i).get("name");
					if ((name.equals("manualorderid") || name.equals("manualsampleid"))
							&& parentData.containsKey("nordertypecode")
							&& (int) parentData.get("nordertypecode") == 1) {
						conditionString = conditionString + "  limit 1";
					} else if ((name.equals("manualorderid") || name.equals("manualsampleid"))
							&& parentData.containsKey("nordertypecode")
							&& (int) parentData.get("nordertypecode") == 2) {
						if (conditionString.contains("nusercode")) {
							String first = conditionString.substring(0, conditionString.indexOf("nusercode = ") + 12);
							String second = conditionString.substring(conditionString.indexOf("nusercode = ") + 12);
							if (second.contains("and")) {
								String third = second.substring(second.indexOf("and"));
								first = first + "-1 " + third;
								conditionString = first;
							} else {
								first = first + "-1";
								conditionString = first;
							}
						}
					}
				}
				if (!objChild.isEmpty() && objChild.get(0).containsKey("isDynamicMapping")
						&& (boolean) objChild.get(0).get("isDynamicMapping")) {
					String tablecolumnname = (String) objChild.get(0).get("tablecolumnname");
					if (!(jsonchildField.contains(tablecolumnname + "||")
							|| jsonchildField.contains("||" + tablecolumnname + "||")
							|| jsonchildField.contains("||" + tablecolumnname))) {
						valuemember = "ndynamicmastercode";
					} else {
						valuemember = "jsondata->" + "'" + tablecolumnname + "'->>'value'";
					}
				} else {
					valuemember = "\"" + valuemember + "\"";
				}

				getJSONFieldQuery = "select TO_JSON(" + tableName + ".*)::jsonb" + jsonField + "  as jsondata"
						+ " from " + tableName + " where nstatus ="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and  " + valuemember
						+ defaultvalues + conditionString + ";";

				List<Combopojo> data = jdbcTemplate.query(getJSONFieldQuery, new Combopojo());

//				if (!data.isEmpty() && childData != null && childData.containsKey(label)
//						&& (!srcData.get(i).containsKey("name")
//								|| !srcData.get(i).get("name").equals("manualsampleid"))) {
//					for (int j = 0; j < data.size(); j++) {
//						Map<String, Object> child = data.get(j).getJsondata();
//						if (child.containsKey("ndefaultstatus")) {
//							if ((int) child.get("ndefaultstatus") == Enumeration.TransactionStatus.YES
//									.gettransactionstatus()) {
//								Map<String, Object> datas = recursiveChildComboForBarcode(child, childData, srcData.get(i), label,
//										valuememberChild, tableName, userInfo, inputMap);
//								returnObject.putAll(datas);
//								break;
//							}
//						} else {
//							break;
//						}
//					}
//				}

				returnObject.put(label, data);
			}
		}
		return new ResponseEntity<>(returnObject, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getBarcodeConfigurationFilterSubmit(Map<String, Object> inputMap, boolean check,
			UserInfo userInfo) {
		Integer nformcode = (Integer) inputMap.get("nformcode");
		Integer ncontrolcode = (Integer) inputMap.get("ncontrolcode");
		Integer nbarcodetemplatecode = (Integer) inputMap.get("nbarcodetemplatecode");
		List<Map<String, Object>> lstCombo = (List<Map<String, Object>>) inputMap.get("ComboComponnet");

		String str = "";

		if (lstCombo != null && !lstCombo.isEmpty()) {
			Map<String, Object> obj = lstCombo.get(lstCombo.size() - 1);
			Map<String, Object> objVal = (Map<String, Object>) inputMap.get(obj.get("label"));

			if (check) {
				Map<String, Object> item = (Map<String, Object>) ((Map<String, Object>) objVal.get("item"))
						.get("jsondata");

				str = "and bc.jsondata->'value'='" + item.get(obj.get("valuemember")) + "'";

			} else {
				str = "and bc.jsondata->'value'='" + objVal.get(obj.get("valuemember")) + "'";
			}

		}
//		'

		final String query = "select bc.*,b.sbarcodename,t.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "' stransdisplaystatus, t1.jsondata->'stransdisplaystatus'->>'"
				+ userInfo.getSlanguagetypecode() + "'  sneedconfiguration , "
				+ "t2.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode() + "'  sbarcodeprint ,"
				+ " t3.jsondata->'stransdisplaystatus'->>'" + userInfo.getSlanguagetypecode()
				+ "'  ssqlqueryneed ,sql.ssqlqueryname  from barcodeconfiguration bc,barcodetemplate bt,"
				+ " barcode b,transactionstatus t,transactionstatus t1,transactionstatus t2,transactionstatus t3,sqlquery sql where"
				+ "  bc.nformcode=" + nformcode + " and bc.ncontrolcode=" + ncontrolcode + ""
				+ " and bt.nbarcodetemplatecode=bc.nbarcodetemplatecode and bc.nbarcodetemplatecode="
				+ nbarcodetemplatecode + ""
				+ " and b.nbarcode=bc.nbarcode and bc.ntransactionstatus=t.ntranscode and bc.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and t1.ntranscode=case when cast (bt.jsondata->>'nneedconfiguration' as boolean) then 3 else 4 end "
				+ " and t2.ntranscode=case when cast (bt.jsondata->>'nbarcodeprint'as boolean)  then 3 else 4 end "
				+ " and t3.ntranscode=case when cast (bt.jsondata->>'nsqlqueryneed'as boolean)  then 3 else 4 end "
				+ " and sql.nsqlquerycode=cast (bt.jsondata->>'nsqlquerycode'as int) " + str
				+ " and bc.nsitecode="+userInfo.getNmastersitecode()+" and bt.nsitecode="+userInfo.getNmastersitecode()+" "
				+ " and b.nsitecode="+userInfo.getNmastersitecode()+" and sql.nsitecode="+userInfo.getNmastersitecode()+" order by nbarcodeconfigurationcode desc";

//		final String query = "select bc.*,b.sbarcodename,t.jsondata->'stransdisplaystatus'->>'"
//				+ userInfo.getSlanguagetypecode()
//				+ "' stransdisplaystatus  from barcodeconfiguration bc,barcode b,transactionstatus t where  bc.nformcode="
//				+ nformcode + " and b.nbarcode=bc.nbarcode and t.ntranscode=bc.ntransactionstatus  and bc.ncontrolcode="
//				+ ncontrolcode + " and bc.nbarcodetemplatecode=" + nbarcodetemplatecode + " and bc.nstatus="
//				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + str
//				+ " order by nbarcodeconfigurationcode desc";

		List<BarcodeConfiguration> lstBarcodeConfiguration = jdbcTemplate.query(query, new BarcodeConfiguration());

		Map<String, Object> objMap = new HashMap<String, Object>();
		objMap.put("BarcodeConfiguration", lstBarcodeConfiguration);

		if (!lstBarcodeConfiguration.isEmpty()) {
			objMap.put("SelectedBarcodeConfiguration", lstBarcodeConfiguration.get(0));
		} else {
			objMap.put("SelectedBarcodeConfiguration", null);
		}

		return new ResponseEntity<Object>(objMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getOpenModalForBarcodeConfig(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		final Map<String, Object> objMap = new HashMap<String, Object>();
		final Integer nformcode = (Integer) inputMap.get("nformcode");
		final Integer ncontrolcode = (Integer) inputMap.get("ncontrolcode");
		final Integer nquerybuildertablecode = (Integer) inputMap.get("nquerybuildertablecode");
		final Integer nbarcodetemplatecode = (Integer) inputMap.get("nbarcodetemplatecode");
		final Integer lastLevelCode = (Integer) inputMap.get("lastLevelCode");

		String query1 = "";

		if (lastLevelCode != null && lastLevelCode != -1) {

			query1 = " and jsondata->'value'='" + lastLevelCode + "'";
		}

		final String qt = "select bt.*,bc.nisdynamic,bc.nissubsample from barcodetemplate bt,querybuildertables q,barcodecontrolconfig bc "
				+ " where bt.nbarcodetemplatecode=" + nbarcodetemplatecode + ""
				+ " and  bt.nquerybuildertablecode=q.nquerybuildertablecode " + " and  q.nquerybuildertablecode="
				+ nquerybuildertablecode + " and bt.ncontrolcode=bc.ncontrolcode"
				+ " and bc.nquerybuildertablecode=bc.nquerybuildertablecode and bt.nsitecode="+userInfo.getNmastersitecode()+" and bc.ncontrolcode=" + ncontrolcode;
		BarcodeTemplate bt = (BarcodeTemplate) jdbcUtilityFunction.queryForObject(qt, BarcodeTemplate.class,jdbcTemplate);

		String query = "select * from barcode "
				+ "where    nbarcode not in (select nbarcode as nbarcode from barcodeconfiguration where"
				+ "  ncontrolcode=" + ncontrolcode + " and nformcode=" + nformcode + " and nbarcodetemplatecode="
				+ nbarcodetemplatecode + " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ "" + " and ntransactionstatus!=" + Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + " "
				+ query1 + " ) and nsitecode="+userInfo.getNmastersitecode()+" and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and nbarcode>0";

		final List<Barcode> lstBarcode = jdbcTemplate.query(query, new Barcode());

		objMap.put("Barcode", lstBarcode);
		Map<String, Object> map = bt.getJsondata();

		if (map.containsKey("nsqlqueryneed") && (boolean) map.get("nsqlqueryneed")) {

			if (map.containsKey("nfiltersqlqueryneed") && (boolean) map.get("nfiltersqlqueryneed")) {

				String sqlQuery = "select * from   sqlquery q where  q.nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and q.nquerytypecode="
						+ Enumeration.QueryType.BARCODE.getQuerytype() + "  order by 1 desc";

				List<SQLQuery> lstSQLQuery = jdbcTemplate.query(sqlQuery, new SQLQuery());

				objMap.put("SqlQuery", lstSQLQuery);
				objMap.put("DesignTempateMapping", new DesignTemplateMapping(-1));

			} else {
				Integer nsqlquerycode = (Integer) map.get("nsqlquerycode");
				String sqlQuery = "select * from sqlquery where nsqlquerycode=" + nsqlquerycode + "" + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

				SQLQuery sqlQuery1 = (SQLQuery) jdbcUtilityFunction.queryForObject(sqlQuery, SQLQuery.class,jdbcTemplate);
				Set<String> sqlParam = new LinkedHashSet<String>();
				if (sqlQuery1 != null) {
					if (!sqlQuery1.getSsqlquery().isEmpty()) {

						String q = sqlQuery1.getSsqlquery();

						StringBuilder sbuilder1 = new StringBuilder();
						if (q != null) {
							sbuilder1.append(q);
							while (q.contains("<@")) {
								int nStart = q.indexOf("<@");
								int nEnd = q.indexOf("@>");
								sqlParam.add(sbuilder1.substring(nStart + 2, nEnd));
								sbuilder1.replace(nStart, nEnd + 2, "'-1'");
								q = sbuilder1.toString();
							}

							while (q.contains("<#")) {
								int nStart = q.indexOf("<#");
								int nEnd = q.indexOf("#>");
								sqlParam.add(sbuilder1.substring(nStart +2, nEnd));
								sbuilder1.replace(nStart, nEnd + 2,
										"'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'");
								q = sbuilder1.toString();
							}

							String createTable = "create table qualislimsbarcodecreation as " + q;
							jdbcTemplate.execute(createTable);

							createTable = "SELECT column_name as columnname, data_type FROM   information_schema.columns "
									+ "WHERE  table_name = 'qualislimsbarcodecreation' ORDER  BY ordinal_position; ";

							List<Map<String, Object>> lsData = jdbcTemplate.queryForList(createTable);

							createTable = "drop table qualislimsbarcodecreation";
							jdbcTemplate.execute(createTable);
							objMap.put("SqlQueryParam", sqlParam);
							objMap.put("MappingFileds", lsData);
							objMap.put("DesignTempateMapping", new DesignTemplateMapping(-1));
						}
					}
				}
			}
			if (bt.getNisdynamic() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
				String dynamicForm = "";
				if (bt.getStableprimarykeyname().equals("ndynamicmastercode")) {

					dynamicForm = " select r.*,qc.jsqlquerycolumns as jsqlquerycolumns,ds.ndesigntemplatemappingcode "
							+ " from designtemplatemapping ds,reactregistrationtemplate r,querybuildertablecolumns qc where "
							+ " ds.nreactregtemplatecode=r.nreactregtemplatecode and  ds.nformcode=" + nformcode
							+ " and ds.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and ds.ntransactionstatus="
							+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
							+ " and qc.nformcode=ds.nformcode and ds.nsitecode="+userInfo.getNmastersitecode()+" and r.nsitecode="+userInfo.getNmastersitecode()+" ";

				} else {
					if (bt.getNissubsample() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
						String valueMember = (String) map.get("valuemember");
						if (valueMember.equals("nregsubtypecode")) {

							dynamicForm = " select qc.jsqlquerycolumns as jsqlquerycolumns,max(dm.ndesigntemplatemappingcode) ndesigntemplatemappingcode "
									+ " from   approvalconfigautoapproval aca, approvalconfig ac,approvalconfigversion acv ,"
									+ " designtemplatemapping dm,reactregistrationtemplate rt,querybuildertablecolumns qc "
									+ " where   acv.napproveconfversioncode=aca.napprovalconfigversioncode "
									+ " and ac.napprovalconfigcode=aca.napprovalconfigcode and ac.napprovalconfigcode=acv.napprovalconfigcode  "
									+ " and ac.nregsubtypecode =" + lastLevelCode
									+ " and acv.ntransactionstatus not in ("
									+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ","
									+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + ")"
									+ " and acv.nsitecode =" + userInfo.getNmastersitecode() + " and" + " ac.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and acv.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and aca.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and acv.nsitecode=" + userInfo.getNmastersitecode()
									+ " and acv.ndesigntemplatemappingcode=dm.ndesigntemplatemappingcode "
									+ " and rt.nreactregtemplatecode=dm.nsubsampletemplatecode and (acv.sviewname||'querybuilder')=qc.stablename "
									+ " and rt.nsitecode="+userInfo.getNmastersitecode()+" and aca.nsitecode="+userInfo.getNmastersitecode()+" and ac.nsitecode="+userInfo.getNmastersitecode()+""
									+ " and acv.nsitecode="+userInfo.getNmastersitecode()+" and dm.nsitecode="+userInfo.getNmastersitecode()+" "
									+ " group by aca.napprovalconfigversioncode, "
									+ " acv.ntreeversiontempcode,aca.napprovalconfigcode,"
									+ " qc.jsqlquerycolumns  order by aca.napprovalconfigversioncode desc ";

						} else {

							dynamicForm = " select jsqlquerycolumns,-1 ndesigntemplatemappingcode  from querybuildertablecolumns where nquerybuildertablecode="
									+ nquerybuildertablecode + " and nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
						}

					} else {
						String valueMember = (String) map.get("valuemember");
						if (valueMember.equals("nregsubtypecode")) {

							dynamicForm = " select qc.jsqlquerycolumns as jsqlquerycolumns,max(dm.ndesigntemplatemappingcode) ndesigntemplatemappingcode "
									+ " from   approvalconfigautoapproval aca, approvalconfig ac,approvalconfigversion acv ,"
									+ " designtemplatemapping dm,reactregistrationtemplate rt , querybuildertablecolumns qc "
									+ " where   acv.napproveconfversioncode=aca.napprovalconfigversioncode "
									+ " and ac.napprovalconfigcode=aca.napprovalconfigcode and ac.napprovalconfigcode=acv.napprovalconfigcode  "
									+ " and ac.nregsubtypecode =" + lastLevelCode
									+ " and acv.ntransactionstatus not in ("
									+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ","
									+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + ")"
									+ " and acv.nsitecode =" + userInfo.getNmastersitecode() + " and" + " ac.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and acv.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and aca.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and acv.nsitecode=" + userInfo.getNmastersitecode()
									+ " and acv.ndesigntemplatemappingcode=dm.ndesigntemplatemappingcode "
									+ " and rt.nreactregtemplatecode=dm.nreactregtemplatecode and (acv.sviewname||'querybuilder')=qc.stablename"
									+ "  and aca.nsitecode="+userInfo.getNmastersitecode()+" and ac.nsitecode="+userInfo.getNmastersitecode()+" and acv.nsitecode="+userInfo.getNmastersitecode()+""
									+ " and dm.nsitecode="+userInfo.getNmastersitecode()+" and rt.nsitecode="+userInfo.getNmastersitecode()+""
									+ " group by aca.napprovalconfigversioncode, "
									+ " acv.ntreeversiontempcode,aca.napprovalconfigcode,qc.jsqlquerycolumns order by aca.napprovalconfigversioncode desc ";

						} else {

							dynamicForm = " select jsqlquerycolumns,-1 ndesigntemplatemappingcode  from querybuildertablecolumns where nquerybuildertablecode="
									+ nquerybuildertablecode + " and nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
						}
					}
				}
				if (dynamicForm != "") {
					QueryBuilderTableColumns obj = (QueryBuilderTableColumns) jdbcUtilityFunction.queryForObject(dynamicForm,
							QueryBuilderTableColumns.class,jdbcTemplate);

					if (obj != null) {
						objMap.put("SqlQueryParamMappingFileds", obj.getJsqlquerycolumns());
					} else {
						return new ResponseEntity<Object>("IDS_CONFIGURETHETEMPLATEMAPPING", HttpStatus.CONFLICT);
					}

				} else {
					return new ResponseEntity<Object>("IDS_CONFIGURETHETEMPLATE", HttpStatus.CONFLICT);
				}
			} else {
				query = " select jsqlquerycolumns,-1 ndesigntemplatemappingcode  from querybuildertablecolumns where nquerybuildertablecode="
						+ nquerybuildertablecode + " and nformcode=" + nformcode + "" + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

				final QueryBuilderTableColumns obj = (QueryBuilderTableColumns) jdbcUtilityFunction.queryForObject(query,
						QueryBuilderTableColumns.class,jdbcTemplate);

				objMap.put("SqlQueryParamMappingFileds", obj.getJsqlquerycolumns());
			}

		} else {
			if (bt.getNisdynamic() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

				String dynamicForm = "";
				if (bt.getStableprimarykeyname().equals("ndynamicmastercode")) {

					dynamicForm = " select r.*,qc.jsqlquerycolumns as jsqlquerycolumns,ds.ndesigntemplatemappingcode "
							+ " from designtemplatemapping ds,reactregistrationtemplate r,querybuildertablecolumns qc where "
							+ " ds.nreactregtemplatecode=r.nreactregtemplatecode and  ds.nformcode=" + nformcode
							+ " and ds.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
							+ " and ds.nsitecode="+userInfo.getNmastersitecode()+" and r.nsitecode="+userInfo.getNmastersitecode()+" and ds.ntransactionstatus="
							+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
							+ " and qc.nformcode=ds.nformcode ";

				} else {
					if (bt.getNissubsample() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
						String valueMember = (String) map.get("valuemember");
						if (valueMember.equals("nregsubtypecode")) {

							dynamicForm = " select qc.jsqlquerycolumns as jsqlquerycolumns,max(dm.ndesigntemplatemappingcode) ndesigntemplatemappingcode "
									+ " from   approvalconfigautoapproval aca, approvalconfig ac,approvalconfigversion acv ,"
									+ " designtemplatemapping dm,reactregistrationtemplate rt,querybuildertablecolumns qc "
									+ " where   acv.napproveconfversioncode=aca.napprovalconfigversioncode "
									+ " and ac.napprovalconfigcode=aca.napprovalconfigcode and ac.napprovalconfigcode=acv.napprovalconfigcode  "
									+ " and ac.nregsubtypecode =" + lastLevelCode
									+ " and acv.ntransactionstatus not in ("
									+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ","
									+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + ")"
									+ " and acv.nsitecode =" + userInfo.getNmastersitecode() + " and" + " ac.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and acv.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and aca.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and acv.nsitecode=" + userInfo.getNmastersitecode()
									+ " and acv.ndesigntemplatemappingcode=dm.ndesigntemplatemappingcode "
									+ " and rt.nreactregtemplatecode=dm.nsubsampletemplatecode "
									+ " and (acv.sviewname||'querybuilder')=qc.stablename "
									+ " and aca.nsitecode="+userInfo.getNmastersitecode()+" and ac.nsitecode="+userInfo.getNmastersitecode()+" and acv.nsitecode="+userInfo.getNmastersitecode()+" "
									+ " and dm.nsitecode="+userInfo.getNmastersitecode()+" and rt.nsitecode="+userInfo.getNmastersitecode()+" "
									+ " group by aca.napprovalconfigversioncode, "
									+ " acv.ntreeversiontempcode,aca.napprovalconfigcode,"
									+ "qc.jsqlquerycolumns order by aca.napprovalconfigversioncode desc ";

						} else {

							dynamicForm = " select jsqlquerycolumns,-1 ndesigntemplatemappingcode  from querybuildertablecolumns where nquerybuildertablecode="
									+ nquerybuildertablecode + " and nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
						}

					} else {
						String valueMember = (String) map.get("valuemember");
						if (valueMember.equals("nregsubtypecode")) {

							dynamicForm = " select qc.jsqlquerycolumns as jsqlquerycolumns,max(dm.ndesigntemplatemappingcode) ndesigntemplatemappingcode "
									+ " from   approvalconfigautoapproval aca, approvalconfig ac,approvalconfigversion acv ,"
									+ " designtemplatemapping dm,reactregistrationtemplate rt , querybuildertablecolumns qc "
									+ " where   acv.napproveconfversioncode=aca.napprovalconfigversioncode "
									+ " and ac.napprovalconfigcode=aca.napprovalconfigcode and ac.napprovalconfigcode=acv.napprovalconfigcode  "
									+ " and ac.nregsubtypecode =" + lastLevelCode
									+ " and acv.ntransactionstatus not in ("
									+ Enumeration.TransactionStatus.DRAFT.gettransactionstatus() + ","
									+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus() + ")"
									+ " and acv.nsitecode =" + userInfo.getNmastersitecode() + " and" + " ac.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "  and acv.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and aca.nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and acv.nsitecode=" + userInfo.getNmastersitecode()
									+ " and acv.ndesigntemplatemappingcode=dm.ndesigntemplatemappingcode "
									+ " and rt.nreactregtemplatecode=dm.nreactregtemplatecode and (acv.sviewname||'querybuilder')=qc.stablename"
									+ " and aca.nsitecode="+userInfo.getNmastersitecode()+" and ac.nsitecode="+userInfo.getNmastersitecode()+" and acv.nsitecode="+userInfo.getNmastersitecode()+" "
									+ " and dm.nsitecode="+userInfo.getNmastersitecode()+" and rt.nsitecode="+userInfo.getNmastersitecode()+"  group by aca.napprovalconfigversioncode, "
									+ " acv.ntreeversiontempcode,aca.napprovalconfigcode,qc.jsqlquerycolumns order by aca.napprovalconfigversioncode desc ";

						} else {

							dynamicForm = " select jsqlquerycolumns,-1 ndesigntemplatemappingcode  from querybuildertablecolumns where nquerybuildertablecode="
									+ nquerybuildertablecode + " and nstatus="
									+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

						}

					}

				}

				if (dynamicForm != "") {
					QueryBuilderTableColumns obj = (QueryBuilderTableColumns) jdbcUtilityFunction.queryForObject(dynamicForm,
							QueryBuilderTableColumns.class,jdbcTemplate);

					if (obj != null) {
						objMap.put("MappingFileds", obj.getJsqlquerycolumns());
						objMap.put("DesignTempateMapping", obj);
					} else {
						return new ResponseEntity<Object>("IDS_CONFIGURETHETEMPLATEMAPPING", HttpStatus.CONFLICT);
					}

				} else {
					return new ResponseEntity<Object>("IDS_CONFIGURETHETEMPLATE", HttpStatus.CONFLICT);
				}


			} else {

				query = " select jsqlquerycolumns,-1 ndesigntemplatemappingcode  from querybuildertablecolumns where nquerybuildertablecode="
						+ nquerybuildertablecode + " and nformcode=" + nformcode + "" + " and nstatus="
						+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

				final QueryBuilderTableColumns obj = (QueryBuilderTableColumns) jdbcUtilityFunction.queryForObject(query,
						QueryBuilderTableColumns.class,jdbcTemplate);

				objMap.put("MappingFileds", obj.getJsqlquerycolumns());
				objMap.put("DesignTempateMapping", obj);
			}

		}

		return new ResponseEntity<Object>(objMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> getBarcodeFileParameter(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final String ssytemfilename = (String) inputMap.get("ssystemfilename");

		final Map<String, Object> objMap = new HashMap<String, Object>();

		final String getBarcodePath = " select ssettingvalue from settings" + " where nsettingcode = 9 "
				+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
		final String FilePath = jdbcTemplate.queryForObject(getBarcodePath, String.class);

		final String PrnFile = FilePath + ssytemfilename;

		File fileSharedFolder = new File(PrnFile);

		if (!fileSharedFolder.exists()) {

			final UserInfo barcodeUserInfo = new UserInfo(userInfo);
			barcodeUserInfo.setNformcode((short) Enumeration.FormCode.BARCODE.getFormCode());
			Map<String, Object> objmap = ftpUtilityFunction.FileViewUsingFtp(ssytemfilename, -1, barcodeUserInfo, FilePath, "");
			if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus()==objmap.get("rtn") ) {
				fileSharedFolder = new File(PrnFile);
			} else {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PRNFILEDOESNOTEXIST",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}
		String line;
		final FileReader fr = new FileReader(PrnFile);
		final BufferedReader br = new BufferedReader(fr);
		List<String> lstParameter = new LinkedList<String>();
		while ((line = br.readLine()) != null) {
			if (line.contains("$")) {
				int start = line.indexOf("$");
				int end = line.lastIndexOf("$");
				final String valueSubstitutedToFilter1 = line.substring(start + 1, end);
				lstParameter.add(valueSubstitutedToFilter1);
			}
		}
		if (br.readLine() == null) {
			br.close();
		}
		objMap.put("Parameter", lstParameter);

		if (inputMap.containsKey("nfiltersqlqueryneed") && (boolean) inputMap.get("nfiltersqlqueryneed")) {

			Integer nsqlquerycode = (Integer) inputMap.get("nsqlquerycode");
			String sqlQuery = "select * from sqlquery where nsqlquerycode=" + nsqlquerycode + "" + " and nsitecode="+userInfo.getNmastersitecode()+" and nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

			SQLQuery sqlQuery1 = (SQLQuery) jdbcUtilityFunction.queryForObject(sqlQuery, SQLQuery.class,jdbcTemplate);

			Set<String> SqlQueryParam = new LinkedHashSet<>();

			if (sqlQuery1 != null) {
				if (!sqlQuery1.getSsqlquery().isEmpty()) {

					String q = sqlQuery1.getSsqlquery();

					StringBuilder sbuilder1 = new StringBuilder();
					if (q != null) {
						sbuilder1.append(q);
						while (q.contains("<@")) {
							int nStart = q.indexOf("<@");
							int nEnd = q.indexOf("@>");

							SqlQueryParam.add(sbuilder1.substring(nStart + 2, nEnd));

							sbuilder1.replace(nStart, nEnd + 2, "'-1'");
							q = sbuilder1.toString();
						}

						while (q.contains("<#")) {
							int nStart = q.indexOf("<#");
							int nEnd = q.indexOf("#>");

							SqlQueryParam.add(sbuilder1.substring(nStart + 2, nEnd));

							sbuilder1.replace(nStart, nEnd + 2,
									"'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'");
							q = sbuilder1.toString();
						}

						String createTable = "create table qualislimsbarcodecreation as " + q;
						jdbcTemplate.execute(createTable);

						createTable = "SELECT column_name as columnname, data_type FROM   information_schema.columns "
								+ "WHERE  table_name = 'qualislimsbarcodecreation' ORDER  BY ordinal_position; ";

						List<Map<String, Object>> lsData = jdbcTemplate.queryForList(createTable);

						createTable = "drop table qualislimsbarcodecreation";
						jdbcTemplate.execute(createTable);

						objMap.put("SqlQueryParam", SqlQueryParam);
						objMap.put("MappingFileds", lsData);
					}
				}
			}

		}

		return new ResponseEntity<Object>(objMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createBarcodeConfiguration(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		final String sQuery = " lock  table barcodeconfiguration "
				+ Enumeration.ReturnStatus.EXCLUSIVEMODE.getreturnstatus();
		jdbcTemplate.execute(sQuery);
		final String getSequenceNo = "select nsequenceno+1 from seqnobasemaster where stablename ='barcodeconfiguration'";
		final int seqNo = jdbcTemplate.queryForObject(getSequenceNo, Integer.class);

		ObjectMapper objMapper = new ObjectMapper();
		BarcodeConfiguration barcodeConfiguration = objMapper.convertValue(inputMap.get("BarcodeConfiguration"),
				new TypeReference<BarcodeConfiguration>() {
				});
		final String insertRegTemplate = "insert into barcodeconfiguration (nbarcodeconfigurationcode,nbarcodetemplatecode,nformcode,ncontrolcode,nbarcode,ntransactionstatus,jsondata,nstatus,nsitecode,dmodifieddate,ndesigntemplatemappingcode) values ("
				+ seqNo + "," + barcodeConfiguration.getNbarcodetemplatecode() + ","
				+ barcodeConfiguration.getNformcode() + "," + barcodeConfiguration.getNcontrolcode() + ","
				+ barcodeConfiguration.getNbarcode() + "," + Enumeration.TransactionStatus.DRAFT.gettransactionstatus()
				+ ",N'" + stringUtilityFunction.replaceQuote(barcodeConfiguration.getJsonstring()) + "'::jsonb,"
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "," + userInfo.getNsitecode() + ",'"
				+ dateUtilityFunction.getCurrentDateTime(userInfo) + "'," + barcodeConfiguration.getNdesigntemplatemappingcode() + ");";
		jdbcTemplate.execute(insertRegTemplate);

		String updatequery = "update seqnobasemaster set nsequenceno =" + seqNo
				+ " where stablename='barcodeconfiguration'";
		jdbcTemplate.execute(updatequery);
		barcodeConfiguration.setNbarcodeconfigurationcode(seqNo);
		final List<Object> savedList = new ArrayList<>();
		savedList.add(barcodeConfiguration);
		return getBarcodeConfigurationFilterSubmit(inputMap, false, userInfo);
	}

	@Override
	public ResponseEntity<Object> checkConfiguration(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {

		Map<String, Object> returnMap = new HashMap<>();
		boolean multiSelect = (boolean) inputMap.get("Multiselect");

		Map<String, Object> objMap = new HashMap<>();
		List<Map<String, Object>> objList = new ArrayList<>();

		if (multiSelect) {
			objList = (List<Map<String, Object>>) inputMap.get("selectedMaster");
			if (objList.isEmpty()) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SHOULDBEATLEASTONE",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			objMap = (Map<String, Object>) inputMap.get("selectedMaster");
			if (objMap == null) {
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_SHOULDBEATLEASTONE",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}
		}

		Integer ncontrolcode = (Integer) inputMap.get("ncontrolCode");

		String query = " select * from barcodetemplate bc " + " where nformcode=" + userInfo.getNformcode()
				+ " and ncontrolcode=" + ncontrolcode + " " + "and nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " " + " and bc.nsitecode="+userInfo.getNmastersitecode()+" and ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus();

		BarcodeTemplate bt = (BarcodeTemplate) jdbcUtilityFunction.queryForObject(query, BarcodeTemplate.class,jdbcTemplate);

		if (bt != null) {
			Map<String, Object> objMap1 = bt.getJsondata();
			String condition = "";
			if ((boolean) objMap1.get("nneedconfiguration")) {
				String valueMember = (String) objMap1.get("valuemember");

				Integer ncode = null;

				if (multiSelect) {
					ncode = (Integer) objList.get(0).get(valueMember);
				} else {
					ncode = (Integer) objMap.get(valueMember);
				}

				condition = " and bc.jsondata->>'value'='" + ncode + "'";
			}

			query = " select bc.*,b.sbarcodename from barcodeconfiguration bc,barcode b "
					+ " where bc.nbarcode=b.nbarcode and bc.nformcode=" + userInfo.getNformcode()
					+ " and bc.ncontrolcode=" + ncontrolcode + " " + "and bc.nstatus="
					+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + "and bc.ntransactionstatus="
					+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and bc.nsitecode="+userInfo.getNmastersitecode()+"  "
							+ " and b.nsitecode="+userInfo.getNmastersitecode()+" and bc.nbarcodetemplatecode="
					+ bt.getNbarcodetemplatecode() + condition;

			List<BarcodeConfiguration> lstBc = jdbcTemplate.query(query, new BarcodeConfiguration());

			returnMap.put("Barcode", lstBc);
			if (lstBc.isEmpty())
				return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_BARCODENOTAVAILABLE",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			else
				returnMap.put("SelectedBarcode", lstBc.get(0));
			if (objMap1.containsKey("nbarcodeprint"))
				returnMap.put("nbarcodeprint", objMap1.get("nbarcodeprint"));
			else
				returnMap.put("nbarcodeprint", false);
		} else {
			returnMap.put("Barcode", Arrays.asList());
			returnMap.put("nbarcodeprint", false);
		}
		return new ResponseEntity<Object>(returnMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> barcodeGeneration(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		
		List<Map<String, Object>> objList = (List<Map<String, Object>>) inputMap.get("selectedMaster");
		Integer ncontrolcode = (Integer) inputMap.get("ncontrolCode");
		Integer nbarcode = (Integer) inputMap.get("nbarcode");
		String sprintername = (String) inputMap.get("sprintername");

		if (objList.isEmpty()) {
			return new ResponseEntity<>(
					commonFunction.getMultilingualMessage("IDS_SHOULDBEATLEASTONE", userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		String query = " select bc.*,bt.jsondata jsondatabt from barcodeconfiguration bc,barcodetemplate bt,querybuildertables q "
				+ " where bc.nformcode="
				+ userInfo.getNformcode() + " and bc.ncontrolcode=" + ncontrolcode + " " + "and bc.nbarcode=" + nbarcode
				+ " and bc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and bt.nbarcodetemplatecode=bc.nbarcodetemplatecode"
				+ " and q.nquerybuildertablecode=bt.nquerybuildertablecode" + " and bc.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus() + " and bc.nsitecode="+userInfo.getNmastersitecode()+" and bt.nsitecode="+userInfo.getNmastersitecode()+" and " + "  bt.ntransactionstatus="
				+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus();

		BarcodeTemplate bt = (BarcodeTemplate) jdbcUtilityFunction.queryForObject(query, BarcodeTemplate.class,jdbcTemplate);

		if (bt != null) {

			BufferedWriter bufferedWriter = null;

			Map<String, Object> objData = null;
			Map<String, Object> objMap1 = bt.getJsondata();
			String condition = "";
			if (objMap1.containsKey("parameterMapping")) {

				objData = (Map<String, Object>) objMap1.get("parameterMapping");

				FileInputStream psStream = null;
				final String getBarcodePath = " select ssettingvalue from settings" + " where nsettingcode = 9 "
						+ " and nstatus = " + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
				String FilePath = (String) jdbcUtilityFunction.queryForObject(getBarcodePath, String.class,jdbcTemplate);

				final String barcodeQuery = "select * from barcode where nbarcode=" + nbarcode;

				Barcode b = (Barcode) jdbcUtilityFunction.queryForObject(barcodeQuery, Barcode.class,jdbcTemplate);

				if (b != null) {

					String PrnFile = FilePath + b.getSsystemfilename();

					File fileSharedFolder = new File(PrnFile);

					if (!fileSharedFolder.exists()) {
						LOGGER.info("PRN File Not Found in Path->" + PrnFile);
						LOGGER.info("Downloading from FTP");
						final UserInfo barcodeUserInfo = new UserInfo(userInfo);
						barcodeUserInfo.setNformcode((short) Enumeration.FormCode.BARCODE.getFormCode());
						Map<String, Object> objmap = ftpUtilityFunction.FileViewUsingFtp(b.getSsystemfilename(), -1, barcodeUserInfo,
								FilePath, "");
						if (Enumeration.ReturnStatus.SUCCESS.getreturnstatus()==objmap.get("rtn")) {

							LOGGER.info("File Downloaded from FTP");
							fileSharedFolder = new File(PrnFile);
						} else {
							LOGGER.info("Error in downloading file from FTP");
							return new ResponseEntity<>(commonFunction.getMultilingualMessage("IDS_PRNFILEDOESNOTEXIST",
									userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
						}
					}

					LOGGER.info("PRN File Found in Path->" + PrnFile);
					//ALPD-4436 
					//To get path value from system's environment variables instead of absolutepath
					final String homePath=ftpUtilityFunction.getFileAbsolutePath();
					String FileNamePath1 = System.getenv(homePath)//new File("").getAbsolutePath() 
							+ Enumeration.FTP.UPLOAD_PATH.getFTP()
							+ UUID.randomUUID().toString().trim() + ".prn";
					// File file = new File(FileNamePath1);
					Path path = Paths.get(FileNamePath1);
					LOGGER.info("New PRN File Created in Path->" + FileNamePath1);
					// bufferedWriter = new BufferedWriter(new FileWriter(FileNamePath1));
					bufferedWriter = Files.newBufferedWriter(path, StandardCharsets.UTF_8);
					String line = "";

					Map<String, Object> objMapSql = bt.getJsondatabt();

					if (objMapSql.containsKey("nsqlqueryneed") && (boolean) objMapSql.get("nsqlqueryneed")) {

						Integer nsqlquerycode = (Integer) objMapSql.get("nsqlquerycode");
						if (objMapSql.containsKey("nfiltersqlqueryneed")
								&& (boolean) objMapSql.get("nfiltersqlqueryneed")) {
							Map<String, Object> objMapSqlbc = bt.getJsondata();

							nsqlquerycode = (Integer) objMapSqlbc.get("nfiltersqlquerycode");
						}

						String squery1 = (String) jdbcUtilityFunction.queryForObject("Select ssqlquery from SqlQuery where nstatus="
										+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
										+ " and nsqlquerycode = " + nsqlquerycode + "", String.class,jdbcTemplate);
						
						Map<String,Object> SqlQuery=null;
						if(bt.getJsondata().containsKey("SqlQueryParamMapping")) {
							SqlQuery=(Map<String, Object>) bt.getJsondata().get("SqlQueryParamMapping");
						}
						
						
						for (int j = 0; j < objList.size(); j++) {
							String squery = squery1;
							String tempQry = "";
							// String[] query = squery.split(" ");
							tempQry = tempQry + squery;
							if (squery.contains("<@")) {
								int aa = squery.indexOf("<@");
								int bb = (squery.indexOf("@>") + 2);
								int b1 = (squery.indexOf("@>"));
								tempQry = tempQry.replace(squery.substring(aa, (bb)),
										objList.get(j).get(SqlQuery.get(squery.substring(aa + 2, b1))) + "");
							}

							List<Map<String, Object>> lstquery = jdbcTemplate.queryForList(tempQry);
							if (!lstquery.isEmpty()) {
								for (int i = 0; i < lstquery.size(); i++) {
									Integer nbarcodeprintcount = (Integer) inputMap.get("nbarcodeprintcount");
									printBarcode(nbarcodeprintcount, lstquery.get(i), PrnFile, line, bufferedWriter,
											objData, psStream, path, sprintername, FileNamePath1);
								}
							}
						}

					} else {
						for (int i = 0; i < objList.size(); i++) {
							Integer nbarcodeprintcount = (Integer) inputMap.get("nbarcodeprintcount");
							printBarcode(nbarcodeprintcount, objList.get(i), PrnFile, line, bufferedWriter, objData,
									psStream, path, sprintername, FileNamePath1);

						}
					}

					Map<String, Object> outputMap = new HashMap<String, Object>();
					outputMap.put("sprimarykeyvalue", -1);
					auditUtilityFunction.insertAuditAction(userInfo, "IDS_PRINTBARCODE",
							commonFunction.getMultilingualMessage("IDS_PRINTBARCODE", userInfo.getSlanguagefilename()),
							outputMap);
					return new ResponseEntity<>(
							commonFunction.getMultilingualMessage("IDS_SUCCESS", userInfo.getSlanguagefilename()),
							HttpStatus.OK);
				}

			}
		}
		return new ResponseEntity<Object>(
				commonFunction.getMultilingualMessage("IDS_BARCODEPRINTINGFAIL", userInfo.getSlanguagefilename()),
				HttpStatus.CONFLICT);
	}

	public Map<String, Object> printBarcode(Integer nbarcodeprintcount, Map<String, Object> objList, String PrnFile,
			String line, BufferedWriter bufferedWriter, Map<String, Object> objData, FileInputStream psStream,
			Path path, String sprintername, String filePath) throws IOException, PrintException {
		FileReader fr = null;

		BufferedReader br = null;
		fr = new FileReader(PrnFile);
		br = new BufferedReader(fr);

		Map<String, Object> objMap = objList;
		while ((line = br.readLine()) != null) {
			if (line.contains("$")) {
				int start = line.indexOf("$");
				int end = line.lastIndexOf("$");
				String valueSubstitutedToFilter1 = line.substring(start + 1, end);
				String keyToReplace = "\\$" + valueSubstitutedToFilter1 + "\\$";
				line = line.replaceAll(keyToReplace, objMap.get(objData.get(valueSubstitutedToFilter1)).toString());
				LOGGER.info("Value Replaced " + keyToReplace + "->"
						+ objMap.get(objData.get(valueSubstitutedToFilter1)).toString());
				bufferedWriter.write(line);
				((BufferedWriter) bufferedWriter).newLine();
				bufferedWriter.flush();
			} else {
				bufferedWriter.write(line);
				((BufferedWriter) bufferedWriter).newLine();
				bufferedWriter.flush();
			}
		}
		if (br.readLine() == null) {
			br.close();
		}
		while (nbarcodeprintcount > 0) {
			psStream = new FileInputStream(new File(filePath));
			String Printerpath = "";
			Printer objbarcode = null;
			DocFlavor psInFormat = DocFlavor.INPUT_STREAM.AUTOSENSE;
			DocFlavor psInFormat1 = DocFlavor.SERVICE_FORMATTED.PRINTABLE;
			Doc myDoc = new SimpleDoc(psStream, psInFormat, null);

			PrintServiceAttributeSet aset = new HashPrintServiceAttributeSet();
			aset.add(new PrinterName(Printerpath, null));//
			PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);

			for (PrintService printer : services) {
				if (printer.getName().equalsIgnoreCase(sprintername)) {

					DocPrintJob job = printer.createPrintJob();
					System.out.println("print");
					job.print(myDoc, null);
					String word = "";
					LOGGER.info("Printer Name =>" + printer.getName());
					LOGGER.info("Barcode Printed Successfully");
					nbarcodeprintcount -= 1;
				}

			}
		}

		return null;
	}

	@Override
	public ResponseEntity<Object> deleteBarcodeConfiguration(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {

		ObjectMapper om = new ObjectMapper();
		final BarcodeConfiguration barcodeConfiguration = om.convertValue(inputMap.get("BarcodeConfiguration"),
				BarcodeConfiguration.class);

		String query = "select bt.*,q.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode() + "' sformname,"
				+ " cm.jsondata->'scontrolids'->>'" + userInfo.getSlanguagetypecode() + "' scontrolname"
				+ " from barcodeconfiguration bt,qualisforms q,controlmaster cm where cm.ncontrolcode=bt.ncontrolcode and q.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and q.nformcode=bt.nformcode and bt.nbarcodeconfigurationcode="
				+ barcodeConfiguration.getNbarcodeconfigurationcode()
				+ " and bt.nsitecode="+userInfo.getNmastersitecode()+" order by bt.nbarcodeconfigurationcode desc";

		BarcodeConfiguration bc = (BarcodeConfiguration) jdbcUtilityFunction.queryForObject(query, BarcodeConfiguration.class,jdbcTemplate);

		if (bc != null) {

			if (bc.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {

				String update = "update BarcodeConfiguration set nstatus="
						+ Enumeration.TransactionStatus.DELETED.gettransactionstatus()
						+ " where nbarcodeconfigurationcode=" + barcodeConfiguration.getNbarcodeconfigurationcode();

				jdbcTemplate.execute(update);

			} else {
				return new ResponseEntity<Object>(commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORDTODELETE",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}

		} else {

			return new ResponseEntity<Object>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		return getBarcodeConfigurationFilterSubmit(inputMap, false, userInfo);
	}

	@Override
	public ResponseEntity<Object> retireBarcodeConfiguration(BarcodeConfiguration barcodeConfiguration,
			UserInfo userInfo) throws Exception {
		String query = "select bt.*,q.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode() + "' sformname,"
				+ " cm.jsondata->'scontrolids'->>'" + userInfo.getSlanguagetypecode() + "' scontrolname"
				+ " from barcodeconfiguration bt,qualisforms q,controlmaster cm where cm.ncontrolcode=bt.ncontrolcode and q.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and q.nformcode=bt.nformcode and bt.nbarcodeconfigurationcode="
				+ barcodeConfiguration.getNbarcodeconfigurationcode() + " and bt.nsitecode="+userInfo.getNmastersitecode()+" order by bt.nbarcodeconfigurationcode desc";

		BarcodeConfiguration bc = (BarcodeConfiguration) jdbcUtilityFunction.queryForObject(query, BarcodeConfiguration.class,jdbcTemplate);

		if (bc != null) {

			if (bc.getNtransactionstatus() == Enumeration.TransactionStatus.APPROVED.gettransactionstatus()) {

				String update = "update barcodeconfiguration set ntransactionstatus="
						+ Enumeration.TransactionStatus.RETIRED.gettransactionstatus()
						+ " where nbarcodeconfigurationcode=" + barcodeConfiguration.getNbarcodeconfigurationcode();

				jdbcTemplate.execute(update);

			} else {
				return new ResponseEntity<Object>(commonFunction.getMultilingualMessage("IDS_SELECTAPPROVEDRECORD",
						userInfo.getSlanguagefilename()), HttpStatus.EXPECTATION_FAILED);
			}

		} else {

			return new ResponseEntity<Object>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		return getBarcodeConfiguration(barcodeConfiguration.getNbarcodeconfigurationcode(), userInfo);
	}

	@Override
	public ResponseEntity<Object> approveBarcodeConfiguration(BarcodeConfiguration barcodeConfiguration,
			UserInfo userInfo) throws Exception {
		String query = "select bt.*,q.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode() + "' sformname,"
				+ " cm.jsondata->'scontrolids'->>'" + userInfo.getSlanguagetypecode() + "' scontrolname"
				+ " from barcodeconfiguration bt,qualisforms q,controlmaster cm where cm.ncontrolcode=bt.ncontrolcode and q.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and q.nformcode=bt.nformcode and bt.nbarcodeconfigurationcode="
				+ barcodeConfiguration.getNbarcodeconfigurationcode() + " and bt.nsitecode="+userInfo.getNmastersitecode()+" order by bt.nbarcodeconfigurationcode desc";

		BarcodeConfiguration bc = (BarcodeConfiguration) jdbcUtilityFunction.queryForObject(query, BarcodeConfiguration.class,jdbcTemplate);

		if (bc != null) {

			if (bc.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {

				String update = "update barcodeconfiguration set ntransactionstatus="
						+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus()
						+ " where nbarcodeconfigurationcode=" + bc.getNbarcodeconfigurationcode();

				jdbcTemplate.execute(update);

			} else {
				return new ResponseEntity<Object>(
						commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}

		} else {

			return new ResponseEntity<Object>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		return getBarcodeConfiguration(barcodeConfiguration.getNbarcodeconfigurationcode(), userInfo);
	}

	@Override
	public ResponseEntity<Object> updateBarcodeConfiguration(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		ObjectMapper om = new ObjectMapper();
		final BarcodeConfiguration bConfig = om.convertValue(inputMap.get("BarcodeConfiguration"),
				BarcodeConfiguration.class);

		String query = "select bt.*,q.jsondata->'sdisplayname'->>'" + userInfo.getSlanguagetypecode() + "' sformname,"
				+ " cm.jsondata->'scontrolids'->>'" + userInfo.getSlanguagetypecode() + "' scontrolname"
				+ " from barcodeconfiguration bt,qualisforms q,controlmaster cm where cm.ncontrolcode=bt.ncontrolcode and q.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and bt.nstatus="
				+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
				+ " and q.nformcode=bt.nformcode and bt.nbarcodeconfigurationcode="
				+ bConfig.getNbarcodeconfigurationcode() + " and bt.nsitecode="+userInfo.getNmastersitecode()+" order by bt.nbarcodetemplatecode desc";

		BarcodeConfiguration bc = (BarcodeConfiguration) jdbcUtilityFunction.queryForObject(query, BarcodeConfiguration.class,jdbcTemplate);

		if (bc != null) {

			if (bc.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {

				String update = "update barcodeconfiguration set jsondata='" + stringUtilityFunction.replaceQuote(bConfig.getJsonstring())
						+ "'::jsonb where nbarcodeconfigurationcode=" + bConfig.getNbarcodeconfigurationcode();

				jdbcTemplate.execute(update);

			} else {
				return new ResponseEntity<Object>(commonFunction
						.getMultilingualMessage("IDS_TEMPLATEALREADYCONFIGURED ", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);

			}

		} else {

			return new ResponseEntity<Object>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		return getBarcodeConfigurationFilterSubmit(inputMap, false, userInfo);
	}

	@Override
	public ResponseEntity<Object> getEditBarcodeConfigurationModal(Map<String, Object> inputMap, UserInfo userInfo)
			throws Exception {
		Integer nbarcodeconfigurationcode = (Integer) inputMap.get("nbarcodeconfigurationcode");

		Map<String, Object> objMap = new HashMap<>();
		String query = " select bc.*,bc.jsondata,bcc.nisdynamic,bcc.nquerybuildertablecode,"
				+ " bcc.nissubsample,q.jsqlquerycolumns jnumericcolumns,b.sbarcodename,"
				+ " bt.jsondata jsondatabt,sql.nsqlquerycode nfiltersqlquerycode,sql.ssqlqueryname,b.ssystemfilename "
				+ " from barcodeconfiguration bc,"
				+ " barcode b,barcodecontrolconfig bcc,querybuildertablecolumns q,barcodetemplate bt,sqlquery sql "
				+ " where bc.ncontrolcode=bcc.ncontrolcode and bt.nbarcodetemplatecode=bc.nbarcodetemplatecode"
				+ " and bc.nformcode=bcc.nformcode "
				+ " and bc.nbarcode=b.nbarcode and bcc.nquerybuildertablecode=q.nquerybuildertablecode and "
				+ " bc.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + ""
				+ " and bc.nbarcodeconfigurationcode=" + nbarcodeconfigurationcode
				+ " and bc.nsitecode="+userInfo.getNmastersitecode()+" and b.nsitecode="+userInfo.getNmastersitecode()+" "
				+ "  and bt.nsitecode="+userInfo.getNmastersitecode()+" "
				+ " and sql.nsitecode="+userInfo.getNmastersitecode()+" and sql.nsqlquerycode=cast (bc.jsondata->>'nfiltersqlquerycode'as int) ";

		BarcodeConfiguration bc = (BarcodeConfiguration) jdbcUtilityFunction.queryForObject(query, BarcodeConfiguration.class,jdbcTemplate);

		if (bc != null) {
			if (bc.getNtransactionstatus() == Enumeration.TransactionStatus.DRAFT.gettransactionstatus()) {

				if (bc.getJsondatabt().containsKey("nsqlqueryneed")
						&& (boolean) bc.getJsondatabt().get("nsqlqueryneed")) {
					String sqlquery = "";
					if (bc.getJsondatabt().containsKey("nfiltersqlqueryneed")
							&& (boolean) bc.getJsondatabt().get("nfiltersqlqueryneed")) {

						String sqlQuery = "select * from   sqlquery q where  q.nstatus="
								+ Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() + " and q.nquerytypecode="
								+ Enumeration.QueryType.BARCODE.getQuerytype() + "  order by 1 desc";

						List<SQLQuery> lstSQLQuery = jdbcTemplate.query(sqlQuery, new SQLQuery());

						objMap.put("SqlQuery", lstSQLQuery);

						Integer nsqlquerycode = (Integer) bc.getJsondata().get("nfiltersqlquerycode");

						sqlquery = lstSQLQuery.stream().filter(x -> x.getNsqlquerycode() == nsqlquerycode)
								.map(x -> x.getSsqlquery()).findFirst().orElse("");

					} else {

						Integer nsqlquerycode = (Integer) bc.getJsondatabt().get("nsqlquerycode");

						String sqlQuery = "select * from sqlquery where nsqlquerycode=" + nsqlquerycode + ""
								+ " and nsitecode="+userInfo.getNmastersitecode()+" and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

						SQLQuery sqlQuery1 = (SQLQuery) jdbcUtilityFunction.queryForObject(sqlQuery, SQLQuery.class,jdbcTemplate);

						if (sqlQuery1 != null) {
							sqlquery = sqlQuery1.getSsqlquery();
						}
					}

					if (!sqlquery.isEmpty()) {

						String q = sqlquery;
						Set<String> SqlQueryParam = new LinkedHashSet<>();
						StringBuilder sbuilder1 = new StringBuilder();
						if (q != null) {
							sbuilder1.append(q);
							while (q.contains("<@")) {
								int nStart = q.indexOf("<@");
								int nEnd = q.indexOf("@>");
								SqlQueryParam.add(sbuilder1.substring(nStart+2,nEnd));
								sbuilder1.replace(nStart, nEnd + 2, "'-1'");
								q = sbuilder1.toString();
							}

							while (q.contains("<#")) {
								int nStart = q.indexOf("<#");
								int nEnd = q.indexOf("#>");
								SqlQueryParam.add(sbuilder1.substring(nStart+2,nEnd));
								sbuilder1.replace(nStart, nEnd + 2,
										"'" + dateUtilityFunction.getCurrentDateTime(userInfo) + "'");
								q = sbuilder1.toString();
							}

							String createTable = "create table qualislimsbarcodecreation as " + q;

							jdbcTemplate.execute(createTable);

							createTable = "SELECT column_name as columnname, data_type FROM   information_schema.columns "
									+ "WHERE  table_name = 'qualislimsbarcodecreation' ORDER  BY ordinal_position; ";

							List<Map<String, Object>> lsData = jdbcTemplate.queryForList(createTable);

							createTable = "drop table qualislimsbarcodecreation";
							jdbcTemplate.execute(createTable);

							objMap.put("MappingFileds", lsData);
							objMap.put("SqlQueryParam", SqlQueryParam);
							objMap.put("DesignTempateMapping", new DesignTemplateMapping(-1));
											
							if (bc.getNisdynamic() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {
								String dynamicForm = "";

								if (bc.getNissubsample() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

									dynamicForm = " select r.*,ds.ndesigntemplatemappingcode from designtemplatemapping ds,reactregistrationtemplate r where "
											+ " ds.nsubsampletemplatecode=r.nreactregtemplatecode "
											+ " and  ds.ndesigntemplatemappingcode=" + bc.getNdesigntemplatemappingcode()
											+ " and ds.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " and ds.nsitecode="+userInfo.getNmastersitecode()+" and r.nsitecode="+userInfo.getNmastersitecode()+" and ds.ntransactionstatus="
											+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus();

								} else {

									dynamicForm = " select r.*,ds.ndesigntemplatemappingcode from designtemplatemapping ds,reactregistrationtemplate r where "
											+ " ds.nreactregtemplatecode=r.nreactregtemplatecode "
											+ " and  ds.ndesigntemplatemappingcode=" + bc.getNdesigntemplatemappingcode()
											+ " and ds.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
											+ " and ds.nsitecode="+userInfo.getNmastersitecode()+" and r.nsitecode="+userInfo.getNmastersitecode()+" and ds.ntransactionstatus="
											+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus();

								}

								if (dynamicForm != "") {
									QueryBuilderTableColumns obj = (QueryBuilderTableColumns) jdbcUtilityFunction.queryForObject(dynamicForm,
											QueryBuilderTableColumns.class,jdbcTemplate);

									if (obj != null) {
										Map<String, Object> compo = iterateComponent(obj.getJsqlquerycolumns());
										List<Map<String, Object>> objComponent = (List<Map<String, Object>>) compo
												.get("Component");
										objMap.put("SqlQueryParamMappingFileds", objComponent);
									
									} else {

										return new ResponseEntity<Object>("IDS_CONFIGURETHETEMPLATE", HttpStatus.CONFLICT);
									}

								} else {
									return new ResponseEntity<Object>("IDS_CONFIGURETHETEMPLATE", HttpStatus.CONFLICT);
								}

							} else {

								query = " select jsqlquerycolumns,-1 ndesigntemplatemappingcode  from querybuildertablecolumns where nquerybuildertablecode="
										+ bc.getNquerybuildertablecode() + " and nformcode=" + bc.getNformcode() + ""
										+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

								final QueryBuilderTableColumns obj = (QueryBuilderTableColumns) jdbcUtilityFunction.queryForObject(query,
										QueryBuilderTableColumns.class,jdbcTemplate);

								objMap.put("SqlQueryParamMappingFileds", obj.getJsqlquerycolumns());
							}
						}
					}
				}
				else {
					if (bc.getNisdynamic() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {


						String dynamicForm = "";

						if (bc.getNissubsample() == Enumeration.TransactionStatus.YES.gettransactionstatus()) {

							dynamicForm = " select r.*,ds.ndesigntemplatemappingcode from designtemplatemapping ds,reactregistrationtemplate r where "
									+ " ds.nsubsampletemplatecode=r.nreactregtemplatecode "
									+ " and  ds.ndesigntemplatemappingcode=" + bc.getNdesigntemplatemappingcode()
									+ " and ds.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and ds.nsitecode="+userInfo.getNmastersitecode()+" and r.nsitecode="+userInfo.getNmastersitecode()+" and ds.ntransactionstatus="
									+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus();

						} else {

							dynamicForm = " select r.*,ds.ndesigntemplatemappingcode from designtemplatemapping ds,reactregistrationtemplate r where "
									+ " ds.nreactregtemplatecode=r.nreactregtemplatecode "
									+ " and  ds.ndesigntemplatemappingcode=" + bc.getNdesigntemplatemappingcode()
									+ " and ds.nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus()
									+ " and ds.nsitecode="+userInfo.getNmastersitecode()+" and r.nsitecode="+userInfo.getNmastersitecode()+" and ds.ntransactionstatus="
									+ Enumeration.TransactionStatus.APPROVED.gettransactionstatus();

						}

						if (dynamicForm != "") {
							QueryBuilderTableColumns obj = (QueryBuilderTableColumns) jdbcUtilityFunction.queryForObject(dynamicForm,
									QueryBuilderTableColumns.class,jdbcTemplate);

							if (obj != null) {
								Map<String, Object> compo = iterateComponent(obj.getJsqlquerycolumns());
								List<Map<String, Object>> objComponent = (List<Map<String, Object>>) compo
										.get("Component");
								objMap.put("MappingFileds", objComponent);
								objMap.put("DesignTempateMapping", obj);
							} else {

								return new ResponseEntity<Object>("IDS_CONFIGURETHETEMPLATE", HttpStatus.CONFLICT);
							}

						} else {
							return new ResponseEntity<Object>("IDS_CONFIGURETHETEMPLATE", HttpStatus.CONFLICT);
						}

						// }

					} else {

						query = " select jsqlquerycolumns,-1 ndesigntemplatemappingcode  from querybuildertablecolumns where nquerybuildertablecode="
								+ bc.getNquerybuildertablecode() + " and nformcode=" + bc.getNformcode() + ""
								+ " and nstatus=" + Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

						final QueryBuilderTableColumns obj = (QueryBuilderTableColumns) jdbcUtilityFunction.queryForObject(query,
								QueryBuilderTableColumns.class,jdbcTemplate);

						objMap.put("MappingFileds", obj.getJsqlquerycolumns());
						objMap.put("DesignTempateMapping", obj);
					}
				}
			} else {
				return new ResponseEntity<Object>(
						commonFunction.getMultilingualMessage("IDS_SELECTDRAFTRECORD", userInfo.getSlanguagefilename()),
						HttpStatus.EXPECTATION_FAILED);
			}
		} else {
			return new ResponseEntity<Object>(
					commonFunction.getMultilingualMessage(Enumeration.ReturnStatus.ALREADYDELETED.getreturnstatus(),
							userInfo.getSlanguagefilename()),
					HttpStatus.EXPECTATION_FAILED);
		}

		objMap.put("SelectedBarcodeConfig", bc);

		return new ResponseEntity<Object>(objMap, HttpStatus.OK);
	}

	@Override
	public ResponseEntity<Object> createsqltable(Map<String, Object> inputMap, UserInfo userInfo) throws Exception {
		Map<String, Object> objMap = new HashMap<>();
		String createTable = "SELECT column_name as label, data_type FROM   information_schema.columns "
				+ "WHERE  table_name = 'qualislimsbarcodecreation' ORDER  BY ordinal_position; ";

		List<Map<String, Object>> lsData = jdbcTemplate.queryForList(createTable);

		createTable = "drop table QualisLimsBarcodeCreation";
		jdbcTemplate.execute(createTable);

		objMap.put("MappingFileds", lsData);

		return new ResponseEntity<Object>(objMap, HttpStatus.OK);
	}

}
