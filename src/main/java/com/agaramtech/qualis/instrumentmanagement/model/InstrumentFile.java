package com.agaramtech.qualis.instrumentmanagement.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

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
 * This class is used to map the fields of 'instrumentfile' table of the Database.
 */
@Entity
@Table(name = "instrumentfile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InstrumentFile extends CustomizedResultsetRowMapper<InstrumentFile> implements Serializable, RowMapper<InstrumentFile> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ninstrumentfilecode")
	private int ninstrumentfilecode;
	
	@Column(name = "ninstrumentlogcode", nullable = false)
	private int ninstrumentlogcode;
	
	@Column(name = "ninstrumentlogtypecode", nullable = false)
	private int ninstrumentlogtypecode;
	
	@ColumnDefault("-1")
	@Column(name = "nlinkcode", nullable = false)
	private short nlinkcode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nattachmenttypecode", nullable = false)
	private short nattachmenttypecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "sfilename", nullable = false,length = 100)
	private String sfilename;
	
	@Column(name = "sfiledesc",length = 255)
	private String sfiledesc;
	
	@Column(name = "ssystemfilename",length = 100)
	private String ssystemfilename;
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "dcreateddate")
	private Instant dcreateddate;
	
	@Column(name="noffsetdcreateddate", nullable = false)
	private int noffsetdcreateddate;
	
	@Column(name="ntzcreateddate", nullable = false)
	private short ntzcreateddate;
	
	@Column(name = "nfilesize", nullable = false)
	private int nfilesize;
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	
	public void setSfilename(String sfilename) {	
		this.sfilename=StringEscapeUtils.unescapeJava(sfilename) ;
		
	}
	
	public void setSfiledesc(String sfiledesc) {	
		this.sfiledesc=StringEscapeUtils.unescapeJava(sfiledesc) ;
	}
	
	@Transient
	private transient int ninstrumentcode;
	
	@Transient
	private transient int ninstrumentvalidationcode;
	
	@Transient
	private transient String sdescription;
	
	@Transient
	private transient String screateddate;
	
	@Transient
	private transient String sfilesize;
	
	@Transient
	private transient String slinkname; 
	
	@Transient
	private transient String sdisplayname;
	
	@Transient
	private transient int ninstrumentcatcode;
	
	@Transient
	private transient String sattachmenttypename;
	
	@Transient
	private transient String sinstrumentcatname;

	
	
	@Override
	public InstrumentFile mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final InstrumentFile objInstFile = new InstrumentFile();
		
		objInstFile.setNinstrumentfilecode(getInteger(arg0,"ninstrumentfilecode",arg1));
		objInstFile.setNinstrumentlogcode(getInteger(arg0,"ninstrumentlogcode",arg1));
		objInstFile.setNinstrumentlogtypecode(getInteger(arg0,"ninstrumentlogtypecode",arg1));
		objInstFile.setNlinkcode(getShort(arg0,"nlinkcode",arg1));
		objInstFile.setNattachmenttypecode(getShort(arg0,"nattachmenttypecode",arg1));
		objInstFile.setSfilename(StringEscapeUtils.unescapeJava(getString(arg0,"sfilename",arg1)));
		objInstFile.setSfiledesc(StringEscapeUtils.unescapeJava(getString(arg0,"sfiledesc",arg1)));
		objInstFile.setSsystemfilename(getString(arg0,"ssystemfilename",arg1));
		objInstFile.setNstatus(getShort(arg0,"nstatus",arg1));
		objInstFile.setSdescription(getString(arg0,"sdescription",arg1));
		objInstFile.setNfilesize(getInteger(arg0,"nfilesize",arg1));
		objInstFile.setNinstrumentcode(getInteger(arg0,"ninstrumentcode",arg1));
		objInstFile.setNinstrumentvalidationcode(getInteger(arg0,"ninstrumentvalidationcode",arg1));
		objInstFile.setDcreateddate(getInstant(arg0,"dcreateddate",arg1));
		objInstFile.setNoffsetdcreateddate(getInteger(arg0,"noffsetdcreateddate",arg1));
		objInstFile.setNtzcreateddate(getShort(arg0,"ntzcreateddate",arg1));
		objInstFile.setScreateddate(getString(arg0,"screateddate",arg1));
		objInstFile.setSfilesize(getString(arg0,"sfilesize",arg1));
		objInstFile.setSlinkname(getString(arg0,"slinkname",arg1));
		objInstFile.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objInstFile.setSdisplayname(getString(arg0,"sdisplayname",arg1));
		objInstFile.setNinstrumentcatcode(getInteger(arg0,"ninstrumentcatcode",arg1));
		objInstFile.setSattachmenttypename(getString(arg0,"sattachmenttypename",arg1));
		objInstFile.setSinstrumentcatname(getString(arg0,"sinstrumentcatname",arg1));
		objInstFile.setNsitecode(getShort(arg0,"nsitecode",arg1));

		return objInstFile;
	}

}
