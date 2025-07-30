
package com.agaramtech.qualis.release.model;

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

@Entity
@Table(name = "releaseoutsourceattachment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ReleaseOutsourceAttachment extends CustomizedResultsetRowMapper<ReleaseOutsourceAttachment> implements Serializable, RowMapper<ReleaseOutsourceAttachment> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="nreleaseoutsourceattachcode") 
	private int nreleaseoutsourceattachcode;
	
	@ColumnDefault("-1")
	@Column(name="nexternalordercode", nullable=false) 
	private int nexternalordercode = Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name="sexternalorderid", length=50) 
	private String sexternalorderid;
	
	@ColumnDefault("-1")
	@Column(name="ncoaparentcode", nullable=false) 
	private int ncoaparentcode = Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name="nsourcecoaparentcode", nullable=false) 
	private int nsourcecoaparentcode = Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name="npreregno", nullable=false) 
	private int npreregno = Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name="nusercode", nullable=false) 
	private int nusercode = Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name="nuserrolecode", nullable=false) 
	private int nuserrolecode = Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "sreleaseno",  length = 30)
	private String sreleaseno;
	
	@ColumnDefault("-1")
	@Column(name="nversionno", nullable=false) 
	private int nversionno = Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name="ssystemfilename", length=100, nullable=false) 
	private String ssystemfilename;
	
	@Column(name = "dreleasedate", nullable=false)
	private Instant dreleasedate;
	
	@ColumnDefault("0")
	@Column(name = "noffsetdreleasedate", nullable=false) 
	private int noffsetdreleasedate = Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nreleasedatetimezonecode", nullable=false ) 
	private int nreleasedatetimezonecode = Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "dtransactiondate", nullable = false)
	private Instant dtransactiondate;
	
	@ColumnDefault("0")
	@Column(name = "noffsetdtransactiondate", nullable=false) 
	private int noffsetdtransactiondate = Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "ntransdatetimezonecode" , nullable=false) 
	private int ntransdatetimezonecode = Enumeration.TransactionStatus.NA.gettransactionstatus(); 
	
	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "nsourcesitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsourcesitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name ="nstatus", nullable=false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "nformcode", nullable = false)
	@ColumnDefault("-1")
	private short nformcode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Transient
	private transient int nattachmenttypecode;
	
	@Transient
	private transient String ssitename;
	
	@Transient
	private transient String sreleasedate;
	
	@Transient
	private transient String susername;
	
	@Override
	public ReleaseOutsourceAttachment mapRow(ResultSet arg0, int arg1) throws SQLException {

		ReleaseOutsourceAttachment objReleaseOutsourceAttachment = new ReleaseOutsourceAttachment();

		objReleaseOutsourceAttachment.setNreleaseoutsourceattachcode(getInteger(arg0,"nreleaseoutsourceattachcode",arg1));
		objReleaseOutsourceAttachment.setNexternalordercode(getInteger(arg0,"nexternalordercode",arg1));
		objReleaseOutsourceAttachment.setSexternalorderid(StringEscapeUtils.unescapeJava(getString(arg0,"sexternalorderid",arg1)));
		objReleaseOutsourceAttachment.setNcoaparentcode(getInteger(arg0,"ncoaparentcode",arg1));
		objReleaseOutsourceAttachment.setNsourcecoaparentcode(getInteger(arg0,"nsourcecoaparentcode",arg1));
		objReleaseOutsourceAttachment.setNpreregno(getInteger(arg0,"npreregno",arg1));
		objReleaseOutsourceAttachment.setNusercode(getInteger(arg0,"nusercode",arg1));
		objReleaseOutsourceAttachment.setNuserrolecode(getInteger(arg0,"nuserrolecode",arg1));
		objReleaseOutsourceAttachment.setSreleaseno(StringEscapeUtils.unescapeJava(getString(arg0,"sreleaseno",arg1)));
		objReleaseOutsourceAttachment.setNversionno(getInteger(arg0,"nversionno",arg1));
		objReleaseOutsourceAttachment.setSsystemfilename(StringEscapeUtils.unescapeJava(getString(arg0,"ssystemfilename",arg1)));
		objReleaseOutsourceAttachment.setDreleasedate(getInstant(arg0,"dreleasedate",arg1));
		objReleaseOutsourceAttachment.setNreleasedatetimezonecode(getInteger(arg0,"nreleasedatetimezonecode",arg1));
		objReleaseOutsourceAttachment.setNoffsetdreleasedate(getInteger(arg0,"noffsetdreleasedate",arg1));
		objReleaseOutsourceAttachment.setDtransactiondate(getInstant(arg0,"dtransactiondate",arg1));
		objReleaseOutsourceAttachment.setNtransdatetimezonecode(getInteger(arg0,"ntransdatetimezonecode",arg1));
		objReleaseOutsourceAttachment.setNoffsetdtransactiondate(getInteger(arg0,"noffsetdtransactiondate",arg1));
		objReleaseOutsourceAttachment.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objReleaseOutsourceAttachment.setNsourcesitecode(getShort(arg0,"nsourcesitecode",arg1));
		objReleaseOutsourceAttachment.setNstatus(getShort(arg0,"nstatus",arg1));
		objReleaseOutsourceAttachment.setNformcode(getShort(arg0,"nformcode",arg1));
		objReleaseOutsourceAttachment.setNattachmenttypecode(getInteger(arg0,"nattachmenttypecode",arg1));
		objReleaseOutsourceAttachment.setSsitename(getString(arg0,"ssitename",arg1));
		objReleaseOutsourceAttachment.setSreleasedate(getString(arg0,"sreleasedate",arg1));
		objReleaseOutsourceAttachment.setSusername(getString(arg0,"susername",arg1));
		return objReleaseOutsourceAttachment;
	}

}
