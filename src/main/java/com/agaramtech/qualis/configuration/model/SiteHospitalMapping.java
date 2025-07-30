package com.agaramtech.qualis.configuration.model;

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
 * This class is used to map the fields of 'sitehospitalmapping' table of the Database.
 */
@Entity
@Table(name = "sitehospitalmapping")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SiteHospitalMapping extends CustomizedResultsetRowMapper<SiteHospitalMapping> implements Serializable,RowMapper<SiteHospitalMapping>  {
	

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsitehospitalmappingcode")
	private int nsitehospitalmappingcode;

	@Column(name = "nmappingsitecode", nullable = false)
	private int nmappingsitecode;
	
	@Column(name = "nhospitalcode", nullable = false)
	private int nhospitalcode;

	@Column(name = "sdescription",length=255)
	private String sdescription="";

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String ssitename;	
	
	@Transient
	private transient String ssitecode;	
	
	@Transient
	private transient String ssitetypename;	
	
	@Transient
	private transient int nsitetypecode;	
	
	@Transient
	private transient String shospitalname;	
	
	
	@Override
		public SiteHospitalMapping mapRow(ResultSet arg0, int arg1) throws SQLException {
			
		// TODO Auto-generated method stub
		
		final SiteHospitalMapping objSiteHospitalMapping = new SiteHospitalMapping();

		objSiteHospitalMapping.setNsitehospitalmappingcode(getInteger(arg0, "nsitehospitalmappingcode",arg1));
		objSiteHospitalMapping.setNmappingsitecode(getInteger(arg0, "nmappingsitecode",arg1));
		objSiteHospitalMapping.setNhospitalcode(getInteger(arg0, "nhospitalcode", arg1));
		objSiteHospitalMapping.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0,"sdescription",arg1)));
		objSiteHospitalMapping.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objSiteHospitalMapping.setNstatus(getShort(arg0,"nstatus",arg1));
		objSiteHospitalMapping.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objSiteHospitalMapping.setSsitename(StringEscapeUtils.unescapeJava(getString(arg0,"ssitename",arg1)));
		objSiteHospitalMapping.setSsitecode(StringEscapeUtils.unescapeJava(getString(arg0,"ssitecode",arg1)));
		objSiteHospitalMapping.setSsitetypename(StringEscapeUtils.unescapeJava(getString(arg0,"ssitetypename",arg1)));
		objSiteHospitalMapping.setNsitetypecode(getInteger(arg0, "nsitetypecode",arg1));
		objSiteHospitalMapping.setShospitalname(StringEscapeUtils.unescapeJava(getString(arg0,"shospitalname",arg1)));
		
		return objSiteHospitalMapping;
	}	
	
	
	
	

}
