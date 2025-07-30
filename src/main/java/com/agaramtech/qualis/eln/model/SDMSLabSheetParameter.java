package com.agaramtech.qualis.eln.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import org.apache.commons.text.StringEscapeUtils;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;
import com.agaramtech.qualis.global.Enumeration;
import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.testmanagement.model.TestPredefinedParameter;

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
public class SDMSLabSheetParameter extends CustomizedResultsetRowMapper<SDMSLabSheetParameter> implements Serializable,RowMapper<SDMSLabSheetParameter>{

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ntestparametercode")
	private int ntestparametercode;
	
	@Column(name = "ntestcode", nullable = false)
	private int ntestcode;
	
	@Column(name = "nparametertypecode", nullable = false)
	private short nparametertypecode;
	
	@Column(name = "nunitcode", nullable = false)
	private int nunitcode;
	
	@Column(name = "sparametername", length = 100, nullable = false)
	private String sparametername;
	
	@Column(name = "sparametersynonym", length = 100, nullable = false)
	private String sparametersynonym;
	
	@Column(name = "nroundingdigits", nullable = false)	
	private short nroundingdigits;
	
	@ColumnDefault("4")
	@Column(name = "nisadhocparameter", nullable = false)
	private short nisadhocparameter=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@ColumnDefault("4")
	@Column(name = "nisvisible", nullable = false)
	private short nisvisible=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@Column(name = "ndeltachecklimitcode", nullable = false)
	private float ndeltachecklimitcode;
	
	@Column(name = "ndeltacheckframe", nullable = false)
	private int ndeltacheckframe;
	
	@Column(name = "ndeltaunitcode", nullable = false)
	private int ndeltaunitcode;
	
	@Column(name = "ndeltacheck", nullable = false)
	private short ndeltacheck;
	
	@Column(name="dmodifieddate")
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private String sdisplaystatus;
	@Transient
	private String sunitname;
	@Transient
	private TestPredefinedParameter objPredefinedParameter;
	@Transient
	private String stestname;
	@Transient
	private String sformulacalculationcode;
	@Transient
	private int isformula;
	@Transient
	private String sdeltaunitname;
	@Transient
	private String stestparametersynonym;
	@Transient
	private String stransdisplaystatus;

	@Override
	public SDMSLabSheetParameter mapRow(ResultSet arg0, int arg1) throws SQLException {
		SDMSLabSheetParameter objTestParameter = new SDMSLabSheetParameter();
		objTestParameter.setNtestparametercode(getInteger(arg0,"ntestparametercode",arg1));
		objTestParameter.setNtestcode(getInteger(arg0,"ntestcode",arg1));
		objTestParameter.setNparametertypecode(getShort(arg0,"nparametertypecode",arg1));
		objTestParameter.setNunitcode(getInteger(arg0,"nunitcode",arg1));
		objTestParameter.setSparametername(StringEscapeUtils.unescapeJava(getString(arg0,"sparametername",arg1)));
		objTestParameter.setSparametersynonym(StringEscapeUtils.unescapeJava(getString(arg0,"sparametersynonym",arg1)));
		objTestParameter.setNroundingdigits(getShort(arg0,"nroundingdigits",arg1));
		objTestParameter.setNisadhocparameter(getShort(arg0,"nisadhocparameter",arg1));
		objTestParameter.setNisvisible(getShort(arg0,"nisvisible",arg1));
		objTestParameter.setNstatus(getShort(arg0,"nstatus",arg1));
		objTestParameter.setSdisplaystatus(StringEscapeUtils.unescapeJava(getString(arg0,"sdisplaystatus",arg1)));
		objTestParameter.setSunitname(StringEscapeUtils.unescapeJava(getString(arg0,"sunitname",arg1)));
		objTestParameter.setStestname(StringEscapeUtils.unescapeJava(getString(arg0,"stestname",arg1)));
		objTestParameter.setSformulacalculationcode(StringEscapeUtils.unescapeJava(getString(arg0,"sformulacalculationcode",arg1)));
		objTestParameter.setIsformula(getInteger(arg0,"isformula",arg1));
		objTestParameter.setNdeltachecklimitcode(getFloat(arg0,"ndeltachecklimitcode",arg1));
		objTestParameter.setNdeltacheckframe(getInteger(arg0,"ndeltacheckframe",arg1));
		objTestParameter.setNdeltaunitcode(getInteger(arg0,"ndeltaunitcode",arg1));
		objTestParameter.setNdeltacheck(getShort(arg0,"ndeltacheck",arg1));
		objTestParameter.setSdeltaunitname(StringEscapeUtils.unescapeJava(getString(arg0,"sdeltaunitname",arg1)));
		objTestParameter.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objTestParameter.setStestparametersynonym(StringEscapeUtils.unescapeJava(getString(arg0,"stestparametersynonym",arg1)));
		objTestParameter.setStransdisplaystatus(StringEscapeUtils.unescapeJava(getString(arg0,"stransdisplaystatus",arg1)));
		objTestParameter.setNsitecode(getShort(arg0,"nsitecode",arg1));

		return objTestParameter;
	}
}
