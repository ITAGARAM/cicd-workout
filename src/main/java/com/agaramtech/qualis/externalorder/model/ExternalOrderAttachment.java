package com.agaramtech.qualis.externalorder.model;

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
 * This class is used to map the fields of 'externalorderattachment' table of the Database.
 */
@Entity
@Table(name = "externalorderattachment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ExternalOrderAttachment extends CustomizedResultsetRowMapper<ExternalOrderAttachment> implements Serializable, RowMapper<ExternalOrderAttachment>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="nexternalorderattachmentcode") 
	private int nexternalorderattachmentcode;
	
	@Column(name="nexternalordercode") 
	private int nexternalordercode;
	
	@Column(name="sexternalorderid",length=50,columnDefinition = "nvarchar")
	private String sexternalorderid;
	
	@Column(name="ncoaparentcode")
	@ColumnDefault("-1")
	private int ncoaparentcode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name="sreportcomments",length=255,columnDefinition = "nvarchar")
	private String sreportcomments;
	
	@Column(name="ssystemfilename",length=100,columnDefinition = "nvarchar")
	private String ssystemfilename;
	
	@Column(name="sreleaseno",length=30,columnDefinition = "nvarchar")
	private String sreleaseno;
	
	@Column(name="nversionno")
	@ColumnDefault("-1")
	private int nversionno=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name="nusercode")
	@ColumnDefault("-1")
	private int nusercode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name="nuserrolecode")
	@ColumnDefault("-1")
	private int nuserrolecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "dreleasedate")
	private Instant dreleasedate;
	
	@Column(name = "nreleasedatetimezonecode", nullable = false)
	private int nreleasedatetimezonecode;
	
	@Column(name = "noffsetdreleasedate", nullable = false)
	private int noffsetdreleasedate;
	
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	
	@Column(name = "ntzmodifieddate", nullable = false)
	private short ntzmodifieddate;
	
	@Column(name = "noffsetdmodifieddate", nullable = false)
	private int noffsetdmodifieddate;
	
	
	@Column(name = "nsourcecoaparentcode", nullable = false)
	@ColumnDefault("-1")
	private short nsourcecoaparentcode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "nsourcesitecode", nullable = false) 
	@ColumnDefault("1")
	private short nsourcesitecode=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "nparentsitecode", nullable = false) 
	@ColumnDefault("1")
	private short nparentsitecode=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	@Transient
	private transient String ssitename;
	
	@Transient
	private transient String susername;
	
	@Transient
	private transient String sreleasedate;

	@Override
	public ExternalOrderAttachment mapRow(ResultSet arg0, int arg1) throws SQLException {
		 
		final ExternalOrderAttachment objExternalOrderAttachment =new ExternalOrderAttachment();
		
		objExternalOrderAttachment.setNexternalorderattachmentcode(getInteger(arg0,"nexternalorderattachmentcode",arg1));
		objExternalOrderAttachment.setNexternalordercode(getInteger(arg0,"nexternalordercode",arg1));
		objExternalOrderAttachment.setSexternalorderid(StringEscapeUtils.unescapeJava(getString(arg0,"sexternalorderid",arg1)));
		objExternalOrderAttachment.setNcoaparentcode(getInteger(arg0,"ncoaparentcode",arg1));
		objExternalOrderAttachment.setSreportcomments(StringEscapeUtils.unescapeJava(getString(arg0,"sreportcomments",arg1)));
		objExternalOrderAttachment.setSsystemfilename(StringEscapeUtils.unescapeJava(getString(arg0,"ssystemfilename",arg1)));
		objExternalOrderAttachment.setSreleaseno(StringEscapeUtils.unescapeJava(getString(arg0,"sreleaseno",arg1)));
		objExternalOrderAttachment.setNversionno(getInteger(arg0,"nversionno",arg1));
		objExternalOrderAttachment.setNusercode(getInteger(arg0,"nusercode",arg1));
		objExternalOrderAttachment.setNuserrolecode(getInteger(arg0,"nuserrolecode",arg1));
		objExternalOrderAttachment.setDreleasedate(getInstant(arg0,"dreleasedate",arg1));
		objExternalOrderAttachment.setNoffsetdreleasedate(getInteger(arg0,"noffsetdreleasedate",arg1));
		objExternalOrderAttachment.setNreleasedatetimezonecode(getInteger(arg0,"nreleasedatetimezonecode",arg1));
		objExternalOrderAttachment.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objExternalOrderAttachment.setNtzmodifieddate(getShort(arg0,"ntzmodifieddate",arg1));
		objExternalOrderAttachment.setNoffsetdmodifieddate(getInteger(arg0,"noffsetdmodifieddate",arg1));
		objExternalOrderAttachment.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objExternalOrderAttachment.setNsourcecoaparentcode(getShort(arg0,"nsourcecoaparentcode",arg1));
		objExternalOrderAttachment.setNsourcesitecode(getShort(arg0,"nsourcesitecode",arg1));
		objExternalOrderAttachment.setNparentsitecode(getShort(arg0,"nparentsitecode",arg1));
		objExternalOrderAttachment.setNstatus(getShort(arg0,"nstatus",arg1));
		objExternalOrderAttachment.setSsitename(getString(arg0,"ssitename",arg1));
		objExternalOrderAttachment.setSusername(getString(arg0,"susername",arg1));
		objExternalOrderAttachment.setSreleasedate(getString(arg0,"sreleasedate",arg1));

		return objExternalOrderAttachment;
	}	
}