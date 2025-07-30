package com.agaramtech.qualis.stability.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;
import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "stbstudyplan")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class StbStudyPlan extends CustomizedResultsetRowMapper<StbStudyPlan> implements Serializable,RowMapper<StbStudyPlan>{

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nstbstudyplancode")
	private long nstbstudyplancode;

	@Column(name = "nprotocolcode", nullable = false)
	private int nprotocolcode;

	@Column(name = "nsampletypecode", nullable = false)
	private short nsampletypecode;

	@Column(name = "nregtypecode", nullable = false)
	private short nregtypecode;

	@Column(name = "nregsubtypecode", nullable = false)
	private short nregsubtypecode;

	@Column(name = "nproductcatcode", nullable = false)
	private int nproductcatcode;

	@Column(name = "nproductcode", nullable = false)
	private int nproductcode;

	@Column(name = "ninstrumentcatcode", nullable = false)
	private int ninstrumentcatcode;

	@Column(name = "ninstrumentcode", nullable = false)
	private int ninstrumentcode;

	@Column(name = "nmaterialcatcode", nullable = false)
	private int nmaterialcatcode;

	@Column(name = "nmaterialcode", nullable = false)
	private int nmaterialcode;

	@Column(name = "nisiqcmaterial", nullable = false)
	@ColumnDefault("4")
	private short nisiqcmaterial = (short)Enumeration.TransactionStatus.NO.gettransactionstatus();

	@Column(name = "nprojectmastercode", nullable = false)
	@ColumnDefault("-1")
	private int nprojectmastercode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "ndesigntemplatemappingcode", nullable = false)
	private int ndesigntemplatemappingcode;

	@Column(name = "nregsubtypeversioncode", nullable = false)
	private int nregsubtypeversioncode;

	@Column(name = "ntemplatemanipulationcode", nullable = false)
	private int ntemplatemanipulationcode;

	@Column(name = "nallottedspeccode", nullable = false)
	private int nallottedspeccode;

	@Column(name = "ntransactionstatus", nullable = false)
	private int ntransactionstatus;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Lob
	@Column(name = "jsonuidata", columnDefinition = "jsonb")
	private Map<String, Object> jsonuidata;

	@Column(name = "dtransactiondate", nullable = false)
	private Instant dtransactiondate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "napprovalversioncode", nullable = false)
	private int napprovalversioncode; 

	@Transient
	private transient int slno;
	
	public StbStudyPlan mapRow(ResultSet arg0, int arg1) throws SQLException {
		StbStudyPlan objStbStudyPlan = new StbStudyPlan();
		objStbStudyPlan.setNstbstudyplancode(getLong(arg0, "nstbstudyplancode", arg1));
		objStbStudyPlan.setNprotocolcode(getInteger(arg0, "nprotocolcode", arg1));
		objStbStudyPlan.setNsampletypecode(getShort(arg0, "nsampletypecode", arg1));
		objStbStudyPlan.setNregtypecode(getShort(arg0, "nregtypecode", arg1));
		objStbStudyPlan.setNregsubtypecode(getShort(arg0, "nregsubtypecode", arg1));
		objStbStudyPlan.setNproductcatcode(getInteger(arg0, "nproductcatcode", arg1));
		objStbStudyPlan.setNproductcode(getInteger(arg0, "nproductcode", arg1));
		objStbStudyPlan.setNinstrumentcatcode(getInteger(arg0, "ninstrumentcatcode", arg1));
		objStbStudyPlan.setNinstrumentcode(getInteger(arg0, "ninstrumentcode", arg1));
		objStbStudyPlan.setNmaterialcatcode(getInteger(arg0, "nmaterialcatcode", arg1));
		objStbStudyPlan.setNmaterialcode(getInteger(arg0, "nmaterialcode", arg1));
		objStbStudyPlan.setNtemplatemanipulationcode(getInteger(arg0, "ntemplatemanipulationcode", arg1));
		objStbStudyPlan.setNallottedspeccode(getInteger(arg0, "nallottedspeccode", arg1));
		objStbStudyPlan.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objStbStudyPlan.setNstatus(getShort(arg0, "nstatus", arg1));
		objStbStudyPlan.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objStbStudyPlan.setJsonuidata(unescapeString(getJsonObject(arg0, "jsonuidata", arg1)));
		objStbStudyPlan.setNtransactionstatus(getInteger(arg0, "ntransactionstatus", arg1));
		objStbStudyPlan.setNdesigntemplatemappingcode(getInteger(arg0, "ndesigntemplatemappingcode", arg1));
		objStbStudyPlan.setNregsubtypeversioncode(getInteger(arg0, "nregsubtypeversioncode", arg1));
		objStbStudyPlan.setNprojectmastercode(getInteger(arg0, "nprojectmastercode", arg1));
		objStbStudyPlan.setNisiqcmaterial(getShort(arg0, "nisiqcmaterial", arg1));
		objStbStudyPlan.setDtransactiondate(getInstant(arg0, "dtransactiondate", arg1));
		objStbStudyPlan.setSlno(getInteger(arg0, "slno", arg1));
		objStbStudyPlan.setNapprovalversioncode(getInteger(arg0, "napprovalversioncode", arg1));
		
		return objStbStudyPlan;
	}

}
