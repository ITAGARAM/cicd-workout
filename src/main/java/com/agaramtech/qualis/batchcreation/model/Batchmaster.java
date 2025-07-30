package com.agaramtech.qualis.batchcreation.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.text.StringEscapeUtils;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;
import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


/**
 * This class is used to map the fields of 'batchmaster' table of the
 * Database. 
 */

@Entity
@Data
@Table(name = "batchmaster")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class Batchmaster extends CustomizedResultsetRowMapper<Batchmaster> implements Serializable, RowMapper<Batchmaster> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbatchmastercode")
	private int nbatchmastercode;
	
	@Column(name = "nsampletypecode")
	private int nsampletypecode;
	
	@Column(name = "nregtypecode")
	private int nregtypecode;
	
	@Column(name = "nregsubtypecode")
	private int nregsubtypecode;
	
	@Column(name = "ntestcode")
	private int ntestcode;
	
	@Column(name = "ninstrumentcatcode")
	private int ninstrumentcatcode;
	
	@Column(name = "ninstrumentcode")
	private int ninstrumentcode;
	
	@Column(name = "sinstrumentid", length = 100, nullable = false)
	private String sinstrumentid;
	
	@Column(name = "nproductcode")
	private int nproductcode;
	
	@Column(name = "nsectioncode")
	private int nsectioncode;
	
	@Column(name = "napprovalversioncode")
	private int napprovalversioncode;
	
	@Column(name = "nprojectmastercode")
	private int nprojectmastercode;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode")
	private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus") 
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private transient String sinstrumentname;
	
	@Transient
	private transient String sinstrumentcatname;
	
	@Transient
	private transient String stestname;
	
	@Transient
	private transient String sregtypename;
	
	@Transient
	private transient String sregsubtypename;
	
	@Transient
	private transient String sproductname;
	
	@Transient
	private transient String stransdisplaystatus;
	
	@Transient
	private transient int nbatchhistorycode;
	
	@Transient
	private transient String sbatcharno;
	
	@Transient
	private transient int ntransactionstatus;
	
	@Transient
	private transient String username;
	
	@Transient
	private transient String stransactiondate;
	
	@Transient
	private transient String scomments;
	
	@Transient
	private transient String ssectionname;
	
	@Transient
	private transient String suserrolename;
	
	@Transient
	private transient String sarno;
	
	@Transient
	private transient String sprojectcode;
	
	
	@Override
	public Batchmaster mapRow(ResultSet arg0, int arg1) throws SQLException {
		Batchmaster objBatch = new Batchmaster();
		objBatch.setNbatchmastercode(getInteger(arg0,"nbatchmastercode",arg1));
		objBatch.setNsampletypecode(getShort(arg0,"nsampletypecode",arg1));
		objBatch.setNregtypecode(getShort(arg0,"nregtypecode",arg1));
		objBatch.setNregsubtypecode(getShort(arg0,"nregsubtypecode",arg1));
		objBatch.setNtestcode(getShort(arg0,"ntestcode",arg1));
		objBatch.setNinstrumentcatcode(getShort(arg0,"ninstrumentcatcode",arg1));
		objBatch.setNinstrumentcode(getShort(arg0,"ninstrumentcode",arg1));
		objBatch.setNproductcode(getShort(arg0,"nproductcode",arg1));
		objBatch.setNsectioncode(getShort(arg0,"nsectioncode",arg1));
		objBatch.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objBatch.setNstatus(getShort(arg0,"nstatus",arg1));
		objBatch.setSinstrumentname(getString(arg0,"sinstrumentname",arg1));
		objBatch.setSinstrumentcatname(getString(arg0,"sinstrumentcatname",arg1));
		objBatch.setStestname(getString(arg0,"stestname",arg1));
		objBatch.setSregtypename(getString(arg0,"sregtypename",arg1));
		objBatch.setSregsubtypename(getString(arg0,"sregsubtypename",arg1));
		objBatch.setSproductname(getString(arg0,"sproductname",arg1));
		objBatch.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));
		objBatch.setNbatchhistorycode(getInteger(arg0,"nbatchhistorycode",arg1));
		objBatch.setSbatcharno(getString(arg0,"sbatcharno",arg1));			
		objBatch.setNtransactionstatus(getInteger(arg0,"ntransactionstatus",arg1));
		objBatch.setUsername(getString(arg0,"username",arg1));
		objBatch.setStransactiondate(getString(arg0,"stransactiondate",arg1));
		objBatch.setScomments(getString(arg0,"scomments",arg1));
		objBatch.setSsectionname(getString(arg0,"ssectionname",arg1));
		objBatch.setSuserrolename(getString(arg0,"suserrolename",arg1));
		objBatch.setSinstrumentid(StringEscapeUtils.unescapeJava(getString(arg0,"sinstrumentid",arg1)));
		objBatch.setSarno(getString(arg0,"sarno",arg1));
		objBatch.setNapprovalversioncode(getInteger(arg0,"napprovalversioncode",arg1));
		objBatch.setNprojectmastercode(getInteger(arg0,"nprojectmastercode",arg1));
		objBatch.setSprojectcode(getString(arg0,"sprojectcode",arg1));
		return objBatch;
	}
}
