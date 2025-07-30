package com.agaramtech.qualis.joballocation.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
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
@Table(name = "joballocation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class JobAllocation extends CustomizedResultsetRowMapper<JobAllocation>
		implements Serializable, RowMapper<JobAllocation> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "njoballocationcode")
	private int njoballocationcode;
	@Column(name = "npreregno")
	private int npreregno;
	@Column(name = "ntransactionsamplecode")
	private int ntransactionsamplecode;
	@Column(name = "ntransactiontestcode")
	private int ntransactiontestcode;
	@Column(name = "nsectioncode")
	private int nsectioncode;
	@Column(name = "ntechniquecode")
	private int ntechniquecode;
	@Column(name = "nuserrolecode")
	private int nuserrolecode;
	@Column(name = "nusercode")
	private int nusercode;
	@ColumnDefault("-1")
	@Column(name = "nuserperiodcode")
	private short nuserperiodcode= (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	@Column(name = "ninstrumentcategorycode")
	private int ninstrumentcategorycode;
	@Column(name = "ninstrumentcode")
	private int ninstrumentcode;
	@Column(name = "ninstrumentnamecode")
	private int ninstrumentnamecode;
	@ColumnDefault("-1")
	@Column(name = "ninstrumentperiodcode")
	private short ninstrumentperiodcode= (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	@ColumnDefault("-1")
	@Column(name = "ntimezonecode")
	private short ntimezonecode= (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	@ColumnDefault("0")
	@Column(name = "ntestrescheduleno")
	private short ntestrescheduleno= (short) Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;
	@Lob
	@Column(name = "jsonuidata", columnDefinition = "jsonb")
	private Map<String, Object> jsonuidata;
	@ColumnDefault("-1")
	@Column(name = "nsitecode")
	private short nsitecode= (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	@ColumnDefault("1")
	@Column(name = "nstatus")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient int ntransactionstatus;
	@Transient
	private transient String susername;
	@Transient
	private transient String stransdisplaystatus;
	@Transient
	private transient String sarno;
	@Transient
	private transient String ssamplearno;
	@Transient
	private transient String stestsynonym;
	@Transient
	private transient String userstartdate;
	@Transient
	private transient String userenddate;
	@Transient
	private transient String instrumentstartdate;
	@Transient
	private transient String instrumentenddate;
	@Transient
	private transient int ncount;
	@Transient
	private transient String comments;
	@Transient
	private transient String userstartdatejson;
	@Transient
	private transient String userenddatejson;
	@Transient
	private transient String stechniquename;
	@Transient
	private transient String sinstrumentcatname;
	@Transient
	private transient String sinstrumentname;
	@Transient
	private transient String sinstrumentid;
	@Transient
	private transient String sinstrumentperiodname;
	@Transient
	private transient String suserperiodname;
	@Transient
	private transient String suserholdduration;
	@Transient
	private transient String sinstrumentholdduration;
	@Transient
	private transient String grouping;
	@Transient
	private transient String ssectionname;

	@Override
	public JobAllocation mapRow(ResultSet arg0, int arg1) throws SQLException {

		JobAllocation objJobAllocation = new JobAllocation();

		objJobAllocation.setNjoballocationcode(getInteger(arg0, "njoballocationcode", arg1));
		objJobAllocation.setNpreregno(getInteger(arg0, "npreregno", arg1));
		objJobAllocation.setNtransactionsamplecode(getInteger(arg0, "ntransactionsamplecode", arg1));
		objJobAllocation.setNtransactiontestcode(getInteger(arg0, "ntransactiontestcode", arg1));
		objJobAllocation.setNsectioncode(getInteger(arg0, "nsectioncode", arg1));
		objJobAllocation.setNtechniquecode(getInteger(arg0, "ntechniquecode", arg1));
		objJobAllocation.setNuserrolecode(getInteger(arg0, "nuserrolecode", arg1));
		objJobAllocation.setNusercode(getInteger(arg0, "nusercode", arg1));
		objJobAllocation.setNuserperiodcode(getShort(arg0, "nuserperiodcode", arg1));
		objJobAllocation.setNinstrumentcategorycode(getInteger(arg0, "ninstrumentcategorycode", arg1));
		objJobAllocation.setNinstrumentcode(getInteger(arg0, "ninstrumentcode", arg1));
		objJobAllocation.setNinstrumentnamecode(getInteger(arg0, "ninstrumentnamecode", arg1));
		objJobAllocation.setNinstrumentperiodcode(getShort(arg0, "ninstrumentperiodcode", arg1));
		objJobAllocation.setNtimezonecode(getShort(arg0, "ntimezonecode", arg1));
		objJobAllocation.setNtestrescheduleno(getShort(arg0, "ntestrescheduleno", arg1));
		objJobAllocation.setJsondata(getJsonObject(arg0, "jsondata", arg1));
		objJobAllocation.setJsonuidata(getJsonObject(arg0, "jsonuidata", arg1));
		objJobAllocation.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objJobAllocation.setNstatus(getShort(arg0, "nstatus", arg1));
		objJobAllocation.setNtransactionstatus(getInteger(arg0, "ntransactionstatus", arg1));
		objJobAllocation.setSusername(getString(arg0, "susername", arg1));
		objJobAllocation.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
		objJobAllocation.setSarno(getString(arg0, "sarno", arg1));
		objJobAllocation.setSsamplearno(getString(arg0, "ssamplearno", arg1));
		objJobAllocation.setStestsynonym(getString(arg0, "stestsynonym", arg1));
		objJobAllocation.setUserstartdate(getString(arg0, "userstartdate", arg1));
		objJobAllocation.setUserenddate(getString(arg0, "userenddate", arg1));
		objJobAllocation.setInstrumentstartdate(getString(arg0, "instrumentstartdate", arg1));
		objJobAllocation.setInstrumentenddate(getString(arg0, "instrumentenddate", arg1));
		objJobAllocation.setNcount(getInteger(arg0, "ncount", arg1));
		objJobAllocation.setComments(getString(arg0, "comments", arg1));
		objJobAllocation.setUserenddatejson(getString(arg0, "userenddatejson", arg1));
		objJobAllocation.setUserstartdatejson(getString(arg0, "userstartdatejson", arg1));
		objJobAllocation.setStechniquename(getString(arg0, "stechniquename", arg1));
		objJobAllocation.setSusername(getString(arg0, "susername", arg1));
		objJobAllocation.setSinstrumentcatname(getString(arg0, "sinstrumentcatname", arg1));
		objJobAllocation.setSinstrumentname(getString(arg0, "sinstrumentname", arg1));
		objJobAllocation.setSinstrumentid(getString(arg0, "sinstrumentid", arg1));
		objJobAllocation.setSinstrumentperiodname(getString(arg0, "sinstrumentperiodname", arg1));
		objJobAllocation.setSuserperiodname(getString(arg0, "suserperiodname", arg1));
		objJobAllocation.setSuserholdduration(getString(arg0, "suserholdduration", arg1));
		objJobAllocation.setSinstrumentholdduration(getString(arg0, "sinstrumentholdduration", arg1));
		objJobAllocation.setGrouping(getString(arg0, "grouping", arg1));
		objJobAllocation.setSsectionname(getString(arg0, "ssectionname", arg1));

		return objJobAllocation;
	}

}
