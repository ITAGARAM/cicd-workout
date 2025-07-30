package com.agaramtech.qualis.batchcreation.model;

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


/**
 * This class is used to map the fields of 'batchsample' table of the
 * Database. 
 */

@Entity
@Data
@Table(name = "batchsample")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class Batchsample extends CustomizedResultsetRowMapper<Batchsample> implements Serializable, RowMapper<Batchsample> {
	
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbatchsamplecode")
	private int nbatchsamplecode;
	
	@Column(name = "nbatchmastercode")
	private int nbatchmastercode;
	
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
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode")
	private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus") 
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private  String sarno;
	
	@Transient
	private  String ssamplearno;
	
	@Transient
	private  String stestsynonym;
	
	@Transient
	private  String stestname;
	
	@Override
	public Batchsample mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		Batchsample objBatchSample = new Batchsample();
		
		objBatchSample.setNbatchsamplecode(getInteger(arg0,"nbatchsamplecode",arg1));
		objBatchSample.setNbatchmastercode(getInteger(arg0,"nbatchmastercode",arg1));
		objBatchSample.setNtransactiontestcode(getInteger(arg0,"ntransactiontestcode",arg1));
		objBatchSample.setNpreregno(getInteger(arg0,"npreregno",arg1));
		objBatchSample.setNtransactionsamplecode(getInteger(arg0,"ntransactionsamplecode",arg1));
		objBatchSample.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objBatchSample.setJsonuidata(unescapeString(getJsonObject(arg0, "jsonuidata", arg1)));
		objBatchSample.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objBatchSample.setNstatus(getShort(arg0,"nstatus",arg1));
		objBatchSample.setSarno(getString(arg0,"sarno",arg1));
		objBatchSample.setSsamplearno(getString(arg0,"ssamplearno",arg1));
		objBatchSample.setStestsynonym(getString(arg0,"stestsynonym",arg1));
		objBatchSample.setStestname(getString(arg0,"stestname",arg1));
		return objBatchSample;
	}

}
