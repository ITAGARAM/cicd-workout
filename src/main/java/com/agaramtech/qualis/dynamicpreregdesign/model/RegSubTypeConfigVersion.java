package com.agaramtech.qualis.dynamicpreregdesign.model;

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

/**
 * This class is used to map the fields in the table 'seqnoformatgenerator'
 *
 * @author ATE169
 *
 */

@Entity
@Table(name = "regsubtypeconfigversion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class RegSubTypeConfigVersion extends CustomizedResultsetRowMapper<RegSubTypeConfigVersion> implements Serializable, RowMapper<RegSubTypeConfigVersion> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nregsubtypeversioncode")
	private int nregsubtypeversioncode;

	@Column(name = "napprovalconfigcode", nullable = false)
	private short napprovalconfigcode;
	
	@Column(name = "nseqnoarnogencode", nullable = false)
	private int nseqnoarnogencode;
	
	@Column(name = "nperiodcode", nullable = false)
	@ColumnDefault("4")
	private short nperiodcode  =(short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@Lob
	@Column(name="jsondata",columnDefinition = "jsonb")
	Map<String,Object> jsondata;
	
	@Lob@Column(name = "jsonuidata", columnDefinition = "jsonb")
	private Map<String,Object> jsonuidata;

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Column(name = "ntransactionstatus", nullable = false)
	@ColumnDefault("8")
	private short ntransactionstatus =(short)Enumeration.TransactionStatus.DRAFT.gettransactionstatus();
	
	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode  =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus  =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String ssampleformat;
	@Transient
	private transient String stablename;
	@Transient
	private transient String stransdisplaystatus;
	@Transient
	private transient String speriodname;
	@Transient
	private transient String sresetduration;
	@Transient
	private transient String sseqresetdate;
	@Transient
	private transient int nsequenceno;
	@Transient
	private transient short nregtypecode;
	@Transient
	private transient short nregsubtypecode;
	@Transient
	private transient boolean nneedjoballocation;
	@Transient
	private transient String sreleaseformat;
	@Transient
	private transient String srelaeseexampleformat;
	@Transient
	private transient String sreleaseperiodname;
	@Transient
	private transient String nneedsitewisearnorelease;

	@Override
	public RegSubTypeConfigVersion mapRow(ResultSet arg0, int arg1) throws SQLException {

		final RegSubTypeConfigVersion objSeqNoFormatGenerator = new RegSubTypeConfigVersion();
		objSeqNoFormatGenerator.setNregsubtypeversioncode(getInteger(arg0,"nregsubtypeversioncode",arg1));
		objSeqNoFormatGenerator.setNapprovalconfigcode(getShort(arg0,"napprovalconfigcode",arg1));
		objSeqNoFormatGenerator.setNseqnoarnogencode(getInteger(arg0,"nseqnoarnogencode",arg1));
		objSeqNoFormatGenerator.setJsondata(getJsonObject(arg0,"jsondata",arg1));
		objSeqNoFormatGenerator.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));
		objSeqNoFormatGenerator.setNtransactionstatus(getShort(arg0,"ntransactionstatus",arg1));
		objSeqNoFormatGenerator.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objSeqNoFormatGenerator.setNstatus(getShort(arg0,"nstatus",arg1));
		objSeqNoFormatGenerator.setSsampleformat(getString(arg0,"ssampleformat",arg1));
		objSeqNoFormatGenerator.setNperiodcode(getShort(arg0,"nperiodcode",arg1));
		objSeqNoFormatGenerator.setSperiodname(getString(arg0,"speriodname",arg1));
		objSeqNoFormatGenerator.setStablename(getString(arg0,"stablename",arg1));
		objSeqNoFormatGenerator.setNsequenceno(getInteger(arg0,"nsequenceno",arg1));
		objSeqNoFormatGenerator.setSseqresetdate(getString(arg0,"sseqresetdate",arg1));
	//	objSeqNoFormatGenerator.setSresetduration((jsondata !=null && jsondata.get("nresetduration") != null && speriodname != null) ? (jsondata.get("nresetduration")+" "+speriodname) :"");
		objSeqNoFormatGenerator.setNregtypecode(getShort(arg0,"nregtypecode",arg1));
		objSeqNoFormatGenerator.setNregsubtypecode(getShort(arg0,"nregsubtypecode",arg1));
		objSeqNoFormatGenerator.setNregsubtypecode(getShort(arg0,"nregsubtypecode",arg1));
		objSeqNoFormatGenerator.setNneedjoballocation(getBoolean(arg0,"nneedjoballocation",arg1));
		objSeqNoFormatGenerator.setJsonuidata(getJsonObject(arg0, "jsonuidata", arg1));
		objSeqNoFormatGenerator.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));

		objSeqNoFormatGenerator.setSreleaseformat(getString(arg0, "sreleaseformat", arg1));
		objSeqNoFormatGenerator.setSrelaeseexampleformat(getString(arg0, "srelaeseexampleformat", arg1));
		objSeqNoFormatGenerator.setSreleaseperiodname(getString(arg0, "sreleaseperiodname", arg1));
		objSeqNoFormatGenerator.setNneedsitewisearnorelease(getString(arg0, "nneedsitewisearnorelease", arg1));

		return objSeqNoFormatGenerator;
	}

}