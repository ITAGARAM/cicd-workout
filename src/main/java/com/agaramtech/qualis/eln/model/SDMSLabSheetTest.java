package com.agaramtech.qualis.eln.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.text.StringEscapeUtils;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SDMSLabSheetTest extends CustomizedResultsetRowMapper<SDMSLabSheetTest> implements Serializable ,RowMapper<SDMSLabSheetTest>{


	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntestcode")
	private int ntestcode;
	
	@Column(name = "ntestcategorycode", nullable = false)
	private int ntestcategorycode;
	
	@ColumnDefault("-1")
	@Column(name = "nchecklistversioncode", nullable = false)
	private int nchecklistversioncode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("71")
	@Column(name = "naccredited", nullable = false)
	private short naccredited;
	
	@ColumnDefault("1")
	@Column(name = "ntransactionstatus", nullable = false)
	private short ntransactionstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "stestname", length = 100, nullable = false)	
	private String stestname;
	
	@Column(name = "stestsynonym", length = 100)
	private String stestsynonym;
	//FRS-00251
	@Column(name = "sshortname", length = 20)
	private String sshortname;
	//FRS-00251
	@Column(name = "sdescription", length = 2000)
	private String sdescription;

	@Column(name = "ncost", nullable = false)
	private Double ncost;
	
	@Column(name = "stestplatform", length = 100, nullable = false)	
	private String stestplatform;

	//FRS-00251
	@Column(name = "ntatperiodcode", nullable = false)
	private int ntatperiodcode;
	
	@ColumnDefault("4")
	@Column(name = "ntrainingneed", nullable = false)
	private short ntrainingneed=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "ninterfacetypecode", nullable = false)
	private short ninterfacetypecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)	
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private String schecklistname;
	@Transient
	private String stransactionstatus;
	@Transient
	private String saccredited;
	@Transient
	private String stestcategoryname;
	@Transient
	private String sparametername;
	@Transient
	private String statperiodname;
	@Transient
	private String sdeltaunitname;
	@Transient
	private String sinterfacetypename;
	@Transient
	private String strainingneed;
	@Transient
	private String smodifieddate;
	@Transient
	private int ncomponentcode;
	@Transient
	private int nproductcatcode;
	@Transient
	private int ncontainertypecode;
	@Transient
	private String scontainertype;
	@Transient
	private int nallottedspeccode;
	@Transient
	private int ntestgrouptestcode;
	
	@Override
	public SDMSLabSheetTest mapRow(ResultSet arg0, int arg1) throws SQLException {
		SDMSLabSheetTest objTestMaster = new SDMSLabSheetTest();
		objTestMaster.setNtestcode(getInteger(arg0,"ntestcode",arg1));
		objTestMaster.setNtestcategorycode(getInteger(arg0,"ntestcategorycode",arg1));
		objTestMaster.setNchecklistversioncode(getInteger(arg0,"nchecklistversioncode",arg1));
		objTestMaster.setNaccredited(getShort(arg0,"naccredited",arg1));
		objTestMaster.setNtransactionstatus(getShort(arg0,"ntransactionstatus",arg1));
		objTestMaster.setStestname(StringEscapeUtils.unescapeJava(getString(arg0,"stestname",arg1)));
		objTestMaster.setStestsynonym(StringEscapeUtils.unescapeJava(getString(arg0,"stestsynonym",arg1)));
		objTestMaster.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0,"sdescription",arg1)));
		objTestMaster.setNcost(getDouble(arg0,"ncost",arg1));
		objTestMaster.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objTestMaster.setNstatus(getShort(arg0,"nstatus",arg1));
		objTestMaster.setSaccredited(StringEscapeUtils.unescapeJava(getString(arg0,"saccredited",arg1)));
		objTestMaster.setSchecklistname(StringEscapeUtils.unescapeJava(getString(arg0,"schecklistname",arg1)));
		objTestMaster.setSshortname(StringEscapeUtils.unescapeJava(getString(arg0,"sshortname",arg1)));
		objTestMaster.setStransactionstatus(StringEscapeUtils.unescapeJava(getString(arg0,"stransactionstatus",arg1)));
		objTestMaster.setStestcategoryname(StringEscapeUtils.unescapeJava(getString(arg0,"stestcategoryname",arg1)));
		objTestMaster.setSparametername(StringEscapeUtils.unescapeJava(getString(arg0,"sparametername",arg1)));
		objTestMaster.setStestplatform(StringEscapeUtils.unescapeJava(getString(arg0,"stestplatform",arg1)));
		objTestMaster.setNtatperiodcode(getInteger(arg0,"ntatperiodcode",arg1));
		objTestMaster.setStatperiodname(StringEscapeUtils.unescapeJava(getString(arg0,"statperiodname",arg1)));
		objTestMaster.setSdeltaunitname(StringEscapeUtils.unescapeJava(getString(arg0,"sdeltaunitname",arg1)));
		objTestMaster.setNtrainingneed(getShort(arg0,"ntrainingneed",arg1));
		objTestMaster.setNinterfacetypecode(getShort(arg0,"ninterfacetypecode",arg1));
		objTestMaster.setSinterfacetypename(StringEscapeUtils.unescapeJava(getString(arg0,"sinterfacetypename",arg1)));
		objTestMaster.setStrainingneed(StringEscapeUtils.unescapeJava(getString(arg0,"strainingneed",arg1)));
		objTestMaster.setSmodifieddate(StringEscapeUtils.unescapeJava(getString(arg0,"smodifieddate",arg1)));
		objTestMaster.setNcomponentcode(getInteger(arg0,"ncomponentcode",arg1));
		objTestMaster.setNproductcatcode(getInteger(arg0,"nproductcatcode",arg1));
		objTestMaster.setNcontainertypecode(getInteger(arg0,"ncontainertypecode",arg1));
		objTestMaster.setScontainertype(StringEscapeUtils.unescapeJava(getString(arg0,"scontainertype",arg1)));
		objTestMaster.setNallottedspeccode(getInteger(arg0,"nallottedspeccode",arg1));
		objTestMaster.setNtestgrouptestcode(getInteger(arg0,"ntestgrouptestcode",arg1));
		return objTestMaster;
	}
	
	

}
