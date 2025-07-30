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
 * This class is used to map the fields in the table 'regsubtypeconfigversionrelease'
 *
 * @author ATE256
 *
 */

@Entity
@Table(name = "regsubtypeconfigversionrelease")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class RegSubTypeConfigVersionRelease extends CustomizedResultsetRowMapper<RegSubTypeConfigVersionRelease> implements Serializable, RowMapper<RegSubTypeConfigVersionRelease> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nregsubtypeversionreleasecode")
	private int nregsubtypeversionreleasecode;

	@Column(name = "napprovalconfigcode", nullable = false)
	private short napprovalconfigcode;
	
	@Column(name = "nseqnoreleasenogencode", nullable = false)
	private int nseqnoreleasenogencode;
	
	@Column(name = "nperiodcode", nullable = false)
	@ColumnDefault("4")
	private short nperiodcode =(short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	
	@Lob
	@Column(name="jsondata",columnDefinition = "jsonb")
	private Map<String,Object> jsondata;
	
	@Lob@Column(name = "jsonuidata", columnDefinition = "jsonb")
	private Map<String,Object> jsonuidata;

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Column(name = "ntransactionstatus", nullable = false)
	@ColumnDefault("8")
	private short ntransactionstatus=(short)Enumeration.TransactionStatus.DRAFT.gettransactionstatus();	

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

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
	private transient String sreleaseexampleformat;
	@Transient
	private transient String nneedsitewisearnorelease;	
	

	@Override
	public RegSubTypeConfigVersionRelease mapRow(ResultSet arg0, int arg1) throws SQLException {

		final RegSubTypeConfigVersionRelease objSeqNoFormatGenerator = new RegSubTypeConfigVersionRelease();
		objSeqNoFormatGenerator.setNregsubtypeversionreleasecode(getInteger(arg0,"nregsubtypeversionreleasecode",arg1));
		objSeqNoFormatGenerator.setNapprovalconfigcode(getShort(arg0,"napprovalconfigcode",arg1));
		objSeqNoFormatGenerator.setJsondata(unescapeString(getJsonObject(arg0,"jsondata",arg1)));
		objSeqNoFormatGenerator.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));
		objSeqNoFormatGenerator.setNtransactionstatus(getShort(arg0,"ntransactionstatus",arg1));
		objSeqNoFormatGenerator.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objSeqNoFormatGenerator.setNstatus(getShort(arg0,"nstatus",arg1));
		objSeqNoFormatGenerator.setNperiodcode(getShort(arg0,"nperiodcode",arg1));
		objSeqNoFormatGenerator.setSperiodname(getString(arg0,"speriodname",arg1));
		objSeqNoFormatGenerator.setStablename(getString(arg0,"stablename",arg1));
		objSeqNoFormatGenerator.setNsequenceno(getInteger(arg0,"nsequenceno",arg1));
		objSeqNoFormatGenerator.setSseqresetdate(getString(arg0,"sseqresetdate",arg1));
		objSeqNoFormatGenerator.setSresetduration((jsondata !=null && jsondata.get("nresetduration") != null && speriodname != null) ? (jsondata.get("nresetduration")+" "+speriodname) :"");
		objSeqNoFormatGenerator.setNseqnoreleasenogencode(getInteger(arg0,"nseqnoreleasenogencode",arg1));

		objSeqNoFormatGenerator.setNregtypecode(getShort(arg0,"nregtypecode",arg1));
		objSeqNoFormatGenerator.setNregsubtypecode(getShort(arg0,"nregsubtypecode",arg1));
		objSeqNoFormatGenerator.setNregsubtypecode(getShort(arg0,"nregsubtypecode",arg1));
		objSeqNoFormatGenerator.setJsonuidata(unescapeString(getJsonObject(arg0, "jsonuidata", arg1)));
		objSeqNoFormatGenerator.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objSeqNoFormatGenerator.setSsampleformat(getString(arg0, "sampleformat", arg1));
		objSeqNoFormatGenerator.setSreleaseformat(getString(arg0, "sreleaseformat", arg1));
		objSeqNoFormatGenerator.setSreleaseexampleformat(getString(arg0, "sreleaseexampleformat", arg1));
		objSeqNoFormatGenerator.setNneedsitewisearnorelease(getString(arg0, "nneedsitewisearnorelease", arg1));


		return objSeqNoFormatGenerator;
	}

}