package com.agaramtech.qualis.eln.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.text.StringEscapeUtils;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

//import com.agaramtech.lims.dao.support.AgaramResultSetMapper;
//import com.agaramtech.lims.dao.support.AgaramRowMapper;
import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

//@SuppressWarnings("serial")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ELNOrders extends CustomizedResultsetRowMapper<ELNOrders> implements Serializable,RowMapper<ELNOrders>{

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "ntransactionresultcode")
		private int ntransactionresultcode;
	
	@Column(name = "npreregno", nullable=false)
	private int npreregno;
	
	@Column(name = "sarno", length=30)
	private String sarno;
		
	@Column(name = "ntransactionsamplecode", nullable=false)
	private int ntransactionsamplecode;
	
	@Column(name = "ntransactiontestcode", nullable=false)
	private int ntransactiontestcode;
	
	@Column(name = "ntestgrouptestcode", nullable=false)
	private int ntestgrouptestcode;
	
	@Column(name = "ntestcode", nullable=false)
	private int ntestcode;
	
	@Column(name = "nretestno", nullable=false)
	private short nretestno;
	
	@Column(name = "ntestrepeatcount", nullable=false)
	private short ntestrepeatcount;
	
	@Column(name = "ntestgrouptestparametercode", nullable=false)
	private int ntestgrouptestparametercode;
	
	@Column(name = "ntestparametercode", nullable=false)
	private int ntestparametercode;
	
	@Column(name = "nparametertypecode", nullable=false)
	private short nparametertypecode;
	
	@Column(name = "nroundingdigits", nullable=false)
	private short nroundingdigits;
	
	@Column(name = "sresult", length=255)
	private String sresult;
	
	@Column(name = "sllinterstatus", length=1)
	private String sllinterstatus;
	
	@Column(name = "sfileid", length=255)
	private String sfileid;
	
	@Column(name = "nlinkcode", nullable=false)
	private short nlinkcode;
	
	@Column(name = "nattachedlink", nullable=false)
	private short nattachedlink;
	
	@Column(name = "suuid", length=255)
	private String suuid;
	
	@ColumnDefault("-1")
	@Column(name = "nbatchmastercode", nullable=false)
	private int nbatchmastercode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable=false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable=false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private String groupid;
	@Transient
	private String ssamplearno;
	@Transient
	private String stestname;
	@Transient
	private String sparametername;
	@Transient
	private String testsynonym;
	@Transient
	private int limsprimarycode;
	@Transient
	private String testparametersynonym;
	@Transient
	private int nelnsitecode;
	@Transient
	private int nlimssitecode;
	@Transient
	private String batchid;

	@Override
	public ELNOrders mapRow(ResultSet arg0, int arg1) throws SQLException {
		// TODO Auto-generated method stub
		ELNOrders SDMSLabSheetDetails=new ELNOrders();
		SDMSLabSheetDetails.setNtransactionresultcode(getInteger(arg0,"ntransactionresultcode",arg1));		
		SDMSLabSheetDetails.setGroupid(StringEscapeUtils.unescapeJava(getString(arg0,"groupid",arg1)));
		SDMSLabSheetDetails.setBatchid(StringEscapeUtils.unescapeJava(getString(arg0,"batchid",arg1)));
		SDMSLabSheetDetails.setSarno(StringEscapeUtils.unescapeJava(getString(arg0,"sarno",arg1)));
		SDMSLabSheetDetails.setSsamplearno(StringEscapeUtils.unescapeJava(getString(arg0,"ssamplearno",arg1)));
		SDMSLabSheetDetails.setNtestcode(getInteger(arg0,"ntestcode",arg1));
		SDMSLabSheetDetails.setStestname(StringEscapeUtils.unescapeJava(getString(arg0,"stestname",arg1)));
		SDMSLabSheetDetails.setNtestparametercode(getInteger(arg0,"ntestparametercode",arg1));
		SDMSLabSheetDetails.setSparametername(StringEscapeUtils.unescapeJava(getString(arg0,"sparametername",arg1)));
		SDMSLabSheetDetails.setTestsynonym(StringEscapeUtils.unescapeJava(getString(arg0,"testsynonym",arg1)));
		SDMSLabSheetDetails.setNpreregno(getInteger(arg0,"npreregno",arg1));
		SDMSLabSheetDetails.setNsitecode(getShort(arg0,"nsitecode",arg1));
		SDMSLabSheetDetails.setSllinterstatus(StringEscapeUtils.unescapeJava(getString(arg0,"sllinterstatus",arg1)));
		SDMSLabSheetDetails.setNtransactionsamplecode(getInteger(arg0,"ntransactionsamplecode",arg1));
		SDMSLabSheetDetails.setNtransactiontestcode(getInteger(arg0,"ntransactiontestcode",arg1));
		SDMSLabSheetDetails.setTestparametersynonym(StringEscapeUtils.unescapeJava(getString(arg0,"testparametersynonym",arg1)));
		SDMSLabSheetDetails.setLimsprimarycode(getInteger(arg0,"limsprimarycode",arg1));
		SDMSLabSheetDetails.setNelnsitecode(getInteger(arg0,"nelnsitecode",arg1));
		SDMSLabSheetDetails.setNelnsitecode(getInteger(arg0,"nelnsitecode",arg1));
		SDMSLabSheetDetails.setNbatchmastercode(getInteger(arg0,"nbatchmastercode",arg1));
		return SDMSLabSheetDetails;
	}

}

