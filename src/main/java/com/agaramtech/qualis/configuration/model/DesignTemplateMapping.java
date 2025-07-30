package com.agaramtech.qualis.configuration.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

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
 * This class is used to map the fields of 'designtemplatemapping' table of the Database.
 */
@Entity
@Table(name="designtemplatemapping")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DesignTemplateMapping extends CustomizedResultsetRowMapper<DesignTemplateMapping>  implements Serializable,RowMapper<DesignTemplateMapping>{
	
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "ndesigntemplatemappingcode")
	private int ndesigntemplatemappingcode;
	
	@Column(name = "nsampletypecode" )
	private short nsampletypecode;
	
	@Column(name = "nformcode")
	private short nformcode;
	
	@ColumnDefault("-1")
	@Column(name = "nregtypecode")
	private short nregtypecode =(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nregsubtypecode")
	private short nregsubtypecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nformwisetypecode")
	private short nformwisetypecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nregsubtypeversioncode")
	private int nregsubtypeversioncode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "nreactregtemplatecode")
	private int nreactregtemplatecode;
	
	@ColumnDefault("8")
	@Column(name = "ntransactionstatus")
	private short ntransactionstatus=(short)Enumeration.TransactionStatus.DRAFT.gettransactionstatus();
	
	@Column(name = "nversionno")
	private short nversionno;
	
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable=false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode",nullable=false)
	private short  nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Transient
	private transient String stransdisplaystatus;
	@Transient
	private transient String sregtemplatename;
	@Transient
	private transient List<Object> jsondata;
	@Transient
	private transient  Map<String,Object> jsondataobj;
	@Transient
	private transient String jsonString;
	@Transient
	private transient String sversionno;
	@Transient
	private transient String sformname;
	@Transient
	private transient String smodulename;
	@Transient
	private transient String stablename;
	@Transient
	private transient int nmodulecode;
	@Transient
	private transient boolean nneedsubsample;
	@Transient
	private transient int nsubsampletemplatecode;
	@Transient
	private transient String sviewname;
	@Transient
	private transient String stemplatetypesname;
	@Transient
	private transient List<Object> subsamplejsondata;	
	
	 
//	public List<Object> getSubsamplejsondata() {
//		return subsamplejsondata;
//	}
//	public void setSubsamplejsondata(List<Object> subsamplejsondata) {
//		this.subsamplejsondata = subsamplejsondata;
//	}
	
//	public Map<String, Object> getJsondataobj() {
//		return jsondataobj;
//	}
//	public void setJsondataobj(Map<String, Object> jsondataobj) {
//		this.jsondataobj = jsondataobj;
//	}
	
	 


	@Override
	public DesignTemplateMapping mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final DesignTemplateMapping designtemplatemapping = new DesignTemplateMapping();	
        designtemplatemapping.setNstatus(getShort(arg0, "nstatus", arg1));
        designtemplatemapping.setNdesigntemplatemappingcode(getInteger(arg0, "ndesigntemplatemappingcode", arg1));
        designtemplatemapping.setNsampletypecode(getShort(arg0, "nsampletypecode", arg1));
        designtemplatemapping.setNformcode(getShort(arg0, "nformcode", arg1));
        designtemplatemapping.setNregtypecode(getShort(arg0, "nregtypecode", arg1));
        designtemplatemapping.setNregsubtypecode(getShort(arg0, "nregsubtypecode", arg1));
        designtemplatemapping.setNformwisetypecode(getShort(arg0, "nformwisetypecode", arg1));
        designtemplatemapping.setNreactregtemplatecode(getInteger(arg0, "nreactregtemplatecode", arg1));
        designtemplatemapping.setNtransactionstatus(getShort(arg0, "ntransactionstatus", arg1));
        designtemplatemapping.setNversionno(getShort(arg0, "nversionno", arg1));
        designtemplatemapping.setSregtemplatename(getString(arg0, "sregtemplatename", arg1));
        designtemplatemapping.setStransdisplaystatus(getString(arg0, "stransdisplaystatus", arg1));
        designtemplatemapping.setJsondata(getJSONArray(arg0, "jsondata", arg1));
        designtemplatemapping.setJsondataobj(getJsonObject(arg0, "jsondataobj", arg1));
        designtemplatemapping.setJsonString(jsonString);
        designtemplatemapping.setSversionno(getString(arg0, "sversionno", arg1));
        designtemplatemapping.setSformname(getString(arg0,"sformname", arg1));
        designtemplatemapping.setSmodulename(getString(arg0,"smodulename", arg1));
        designtemplatemapping.setStablename(getString(arg0,"stablename", arg1));
        designtemplatemapping.setNneedsubsample(getBoolean(arg0,"nneedsubsample", arg1));
        designtemplatemapping.setNregsubtypeversioncode(getInteger(arg0,"nregsubtypeversioncode",arg1));
        designtemplatemapping.setSubsamplejsondata(getJSONArray(arg0, "subsamplejsondata", arg1));
        designtemplatemapping.setNmodulecode(getInteger(arg0, "nmodulecode", arg1));
        designtemplatemapping.setNsitecode(getShort(arg0,"nsitecode",arg1));
        designtemplatemapping.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
        designtemplatemapping.setNsubsampletemplatecode(getInteger(arg0,"nsubsampletemplatecode",arg1));
        designtemplatemapping.setSviewname(getString(arg0,"sviewname", arg1));
        designtemplatemapping.setStemplatetypesname(getString(arg0,"stemplatetypesname", arg1));

        
		return designtemplatemapping;
	}
	
	public DesignTemplateMapping(int ndesigntemplatemappingcode) {
		this.ndesigntemplatemappingcode=ndesigntemplatemappingcode;
	}




}
