package com.agaramtech.qualis.worklistpreparation.model;

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
@Table(name = "worklistsample")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class WorklistSample extends CustomizedResultsetRowMapper<WorklistSample> implements Serializable,RowMapper<WorklistSample> {
	
	private static final long serialVersionUID = 1L;

	
	@Id
	@Column(name = "nworklistsamplecode")
	private int nworklistsamplecode;
	
	@Column(name = "nworklistcode")
	private int nworklistcode;
	
	@Column(name = "ntransactiontestcode")
	private int ntransactiontestcode;
	
	@Column(name = "npreregno")
	private int npreregno;
	
	@Column(name = "ntransactionsamplecode")
	private int ntransactionsamplecode;
	
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;
	
	@Lob
	@Column(name = "jsonuidata", columnDefinition = "jsonb")
	private Map<String, Object> jsonuidata;
	
	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus= (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private transient String sarno;
	@Transient
	private transient String ssamplearno;
	@Transient
	private transient String stestname;
	@Transient
	private transient int nregtypecode;
	@Transient
	private transient int nregsubtypecode;
	@Transient
	private transient String ssamplename;
	@Transient
	private transient String sregistereddate;

	@Override
	public WorklistSample mapRow(ResultSet arg0, int arg1) throws SQLException {
		final WorklistSample objWorklistSample = new WorklistSample();
		
		objWorklistSample.setNworklistsamplecode(getInteger(arg0, "nworklistsamplecode", arg1));
		objWorklistSample.setNworklistcode(getInteger(arg0, "nworklistcode", arg1));
		objWorklistSample.setNtransactiontestcode(getInteger(arg0, "ntransactiontestcode", arg1));
		objWorklistSample.setNpreregno(getInteger(arg0, "npreregno", arg1));
		objWorklistSample.setNtransactionsamplecode(getInteger(arg0, "ntransactionsamplecode", arg1));
		objWorklistSample.setJsondata(unescapeString(getJsonObject(arg0,"jsondata",arg1)));
		objWorklistSample.setJsonuidata(unescapeString(getJsonObject(arg0,"jsonuidata",arg1)));
		objWorklistSample.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objWorklistSample.setNstatus(getShort(arg0, "nstatus", arg1));
		objWorklistSample.setSarno(getString(arg0, "sarno", arg1));
		objWorklistSample.setSsamplearno(getString(arg0, "ssamplearno", arg1));
		objWorklistSample.setStestname(getString(arg0, "stestname", arg1));
		objWorklistSample.setNregtypecode(getInteger(arg0, "nregtypecode", arg1));
		objWorklistSample.setNregsubtypecode(getInteger(arg0, "nregsubtypecode", arg1));
		objWorklistSample.setSsamplename(getString(arg0, "ssamplename", arg1));
		objWorklistSample.setSregistereddate(getString(arg0, "sregistereddate", arg1));
		return objWorklistSample;
	}

}
