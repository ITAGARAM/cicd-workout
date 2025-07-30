package com.agaramtech.qualis.eln.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
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

@Entity
@Table(name = "sdmslabsheetdetails")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SDMSLabSheetDetails extends CustomizedResultsetRowMapper<SDMSLabSheetDetails> implements Serializable, RowMapper<SDMSLabSheetDetails>{

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "ntransactionresultcode")private int ntransactionresultcode;	
	@Column(name = "npreregno", nullable=false)	private int npreregno;	
	@Column(name = "sarno", length=30)private String sarno;		
	@Column(name = "ntransactionsamplecode", nullable=false)private int ntransactionsamplecode;
	@Column(name = "ntransactiontestcode", nullable=false)private int ntransactiontestcode;
	@Column(name = "ntestgrouptestcode", nullable=false)private int ntestgrouptestcode;
	@Column(name = "ntestcode", nullable=false)	private int ntestcode;	
	@Column(name = "nretestno", nullable=false)	private short nretestno;
	@Column(name = "ntestrepeatcount", nullable=false)private short ntestrepeatcount;
	@Column(name = "ntestgrouptestparametercode", nullable=false)private int ntestgrouptestparametercode;
	@Column(name = "ntestparametercode", nullable=false)private int ntestparametercode;	
	@Column(name = "nparametertypecode", nullable=false)private short nparametertypecode;
	@Column(name = "nroundingdigits", nullable=false)private short nroundingdigits;
	@Column(name = "sresult", length=255)private String sresult;
	@Column(name = "sllinterstatus", length=1)private String sllinterstatus;
	@Column(name = "sfileid", length=255)private String sfileid;	
	@Column(name = "nlinkcode", nullable=false)private short nlinkcode;
	@Column(name = "nattachedlink", nullable=false)	private short nattachedlink;
	@Column(name = "suuid", length=255)	private String suuid;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable=false)	private short nsitecode=(short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable=false)private short nstatus=(short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nbatchmastercode", nullable=false)	private int nbatchmastercode=(int) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	
	@Transient
	private transient String groupid;
	@Transient
	private transient String ssamplearno;
	@Transient
	private transient String stestname;
	@Transient
	private transient String sparametername;
	@Transient
	private transient String testsynonym;
	@Transient
	private transient int limsprimarycode;
	@Transient
	private transient String testparametersynonym;
	@Transient
	private transient int nelnsitecode;
	@Transient
	private transient int nlimssitecode;
	@Transient
	private transient String batchid;
	@Transient
	private transient int ninterfacetypecode;
	@Transient
	private transient String sexternalintrumnetid;
	
	
	public SDMSLabSheetDetails mapRow(ResultSet arg0, int arg1) throws SQLException {
		SDMSLabSheetDetails SDMSLabSheetDetails=new SDMSLabSheetDetails();
		SDMSLabSheetDetails.setNtransactionresultcode(getInteger(arg0,"ntransactionresultcode",arg1));		
		SDMSLabSheetDetails.setGroupid(getString(arg0,"groupid",arg1));
		SDMSLabSheetDetails.setBatchid(getString(arg0,"batchid",arg1));
		SDMSLabSheetDetails.setSarno(getString(arg0,"sarno",arg1));
		SDMSLabSheetDetails.setSsamplearno(getString(arg0,"ssamplearno",arg1));
		SDMSLabSheetDetails.setNtestcode(getInteger(arg0,"ntestcode",arg1));
		SDMSLabSheetDetails.setStestname(getString(arg0,"stestname",arg1));
		SDMSLabSheetDetails.setNtestparametercode(getInteger(arg0,"ntestparametercode",arg1));
		SDMSLabSheetDetails.setSparametername(getString(arg0,"sparametername",arg1));
		SDMSLabSheetDetails.setTestsynonym(getString(arg0,"testsynonym",arg1));
		SDMSLabSheetDetails.setNpreregno(getInteger(arg0,"npreregno",arg1));
		SDMSLabSheetDetails.setNsitecode(getShort(arg0,"nsitecode",arg1));
		SDMSLabSheetDetails.setSllinterstatus(getString(arg0,"sllinterstatus",arg1));
		SDMSLabSheetDetails.setNtransactionsamplecode(getInteger(arg0,"ntransactionsamplecode",arg1));
		SDMSLabSheetDetails.setNtransactiontestcode(getInteger(arg0,"ntransactiontestcode",arg1));
		SDMSLabSheetDetails.setTestparametersynonym(getString(arg0,"testparametersynonym",arg1));
		SDMSLabSheetDetails.setLimsprimarycode(getInteger(arg0,"limsprimarycode",arg1));
		SDMSLabSheetDetails.setNelnsitecode(getInteger(arg0,"nelnsitecode",arg1));
		SDMSLabSheetDetails.setNelnsitecode(getInteger(arg0,"nelnsitecode",arg1));
		SDMSLabSheetDetails.setNbatchmastercode(getInteger(arg0,"nbatchmastercode",arg1));
		SDMSLabSheetDetails.setNinterfacetypecode(getInteger(arg0,"ninterfacetypecode",arg1));
		SDMSLabSheetDetails.setSexternalintrumnetid(getString(arg0,"sexternalintrumnetid",arg1));
		return SDMSLabSheetDetails;
	}

}
