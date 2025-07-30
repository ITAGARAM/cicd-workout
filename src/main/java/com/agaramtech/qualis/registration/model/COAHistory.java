package com.agaramtech.qualis.registration.model;

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
 * This class is used to map the fields of 'coahistory' table of the Database.
 */
@Entity
@Table(name = "coahistory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class COAHistory extends CustomizedResultsetRowMapper<COAHistory> implements Serializable, RowMapper<COAHistory> {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "ncoahistorycode")
	private int ncoahistorycode;
	
	@Column(name = "npreregno", nullable=false)
	private int npreregno;
	
	@Column(name = "nsectioncode", nullable=false)
	private int nsectioncode;
	
	@Column(name = "ncoareporttypecode", nullable=false)
	private short ncoareporttypecode;
	
	@Column(name = "nreportdetailcode", nullable=false)
	private int nreportdetailcode;
	
	@Column(name = "nusercode", nullable=false)
	private int nusercode;
	
	@Column(name = "nuserrolecode", nullable=false)
	private int nuserrolecode;
	
	@Column(name = "ndeputyusercode", nullable=false)
	private int ndeputyusercode;
	
	@Column(name = "ndeputyuserrolecode", nullable=false)
	private int ndeputyuserrolecode;
	
	@Column(name = "dgenerateddate")
	private Instant dgenerateddate;
	
	@Column(name = "ntzgenerateddate", nullable = false)
	private short ntzgenerateddate;
	
	@Column(name = "noffsetdgenerateddate", nullable = false)
	private int noffsetdgenerateddate;

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	
	@Column(name = "ntzmodifieddate", nullable = false)
	private short ntzmodifieddate;
	
	@Column(name = "noffsetdmodifieddate", nullable = false)
	private int noffsetdmodifieddate;

	@Column(name = "ssystemfilename",  length = 100)
	private String ssystemfilename;
	
	@Column(name = "sreportcomments",  length = 255)
	private String sreportcomments;
	
	@Column(name = "sreportno",  length = 30)
	private String sreportno;
	
	@ColumnDefault("-1")
	@Column	(name="nsitecode")
	private short  nsitecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column	(name="nstatus")
	private short nstatus =(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	@Transient
	private transient String sarno;
	
	@Transient
	private transient String ssectionname;
	
	@Transient
	private transient String scoareporttypename;
	
	@Transient
	private transient String susername;
	
	@Transient
	private transient String suserrolename;
	
	@Transient
	private transient String sgeneratedtime;

	
	@Override
	public COAHistory mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final COAHistory objCOAHistory = new COAHistory();

		objCOAHistory.setNcoahistorycode(getInteger(arg0,"ncoahistorycode",arg1));
		objCOAHistory.setNpreregno(getInteger(arg0,"npreregno",arg1));
		objCOAHistory.setNsectioncode(getInteger(arg0,"nsectioncode",arg1));
		objCOAHistory.setNcoareporttypecode(getShort(arg0,"ncoareporttypecode",arg1));
		objCOAHistory.setNreportdetailcode(getInteger(arg0,"nreportdetailcode",arg1));
		objCOAHistory.setNusercode(getInteger(arg0,"nusercode",arg1));
		objCOAHistory.setNuserrolecode(getInteger(arg0,"nuserrolecode",arg1));
		objCOAHistory.setNdeputyusercode(getInteger(arg0,"ndeputyusercode",arg1));
		objCOAHistory.setNdeputyuserrolecode(getInteger(arg0,"ndeputyuserrolecode",arg1));
		objCOAHistory.setDgenerateddate(getInstant(arg0,"dgenerateddate",arg1));
		objCOAHistory.setSsystemfilename(StringEscapeUtils.unescapeJava(getString(arg0,"ssystemfilename",arg1)));
		objCOAHistory.setSreportcomments(StringEscapeUtils.unescapeJava(getString(arg0,"sreportcomments",arg1)));
		objCOAHistory.setNstatus(getShort(arg0,"nstatus",arg1));
		objCOAHistory.setSarno(getString(arg0,"sarno",arg1));
		objCOAHistory.setSgeneratedtime(getString(arg0,"sgeneratedtime",arg1));
		objCOAHistory.setSsectionname(getString(arg0,"ssectionname",arg1));
		objCOAHistory.setScoareporttypename(getString(arg0,"scoareporttypename",arg1));
		objCOAHistory.setSusername(getString(arg0,"susername",arg1));
		objCOAHistory.setSuserrolename(getString(arg0,"suserrolename",arg1));
		objCOAHistory.setNoffsetdmodifieddate(getInteger(arg0,"noffsetdmodifieddate",arg1));
		objCOAHistory.setNtzmodifieddate(getShort(arg0,"ntzmodifieddate",arg1));
		objCOAHistory.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objCOAHistory.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objCOAHistory.setSreportno(StringEscapeUtils.unescapeJava(getString(arg0,"sreportno",arg1)));
		objCOAHistory.setNoffsetdgenerateddate(getInteger(arg0,"noffsetdgenerateddate",arg1));
		objCOAHistory.setNtzgenerateddate(getShort(arg0,"ntzgenerateddate",arg1));
		objCOAHistory.setDgenerateddate(getInstant(arg0,"dgenerateddate",arg1));

		return objCOAHistory;
	}

}
