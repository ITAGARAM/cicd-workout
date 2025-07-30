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
 * This class is used to map the fields of 'instrumentvalidation' table of the Database.
 */
@Entity
@Table(name = "instrumentvalidation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class InstrumentValidation extends CustomizedResultsetRowMapper<InstrumentValidation>  implements Serializable, RowMapper<InstrumentValidation> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ninstrumentvalidationcode")
	private int ninstrumentvalidationcode;
	
	@Column(name = "ninstrumentcode", nullable = false)
	private int ninstrumentcode;
	
	@Column(name = "nusercode", nullable = false)
	private int nusercode;
	
	@Column(name = "dvalidationdate")
	private Instant dvalidationdate;
	
	@ColumnDefault("-1")
	@Column(name = "ntzvalidationdate", nullable = false)
	private short ntzvalidationdate=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("55")
	@Column(name = "nvalidationstatus", nullable = false)
	private short nvalidationstatus=(short)Enumeration.TransactionStatus.UNDERVALIDATION.gettransactionstatus();;
	
	@Column(name = "sremark", length = 255)
	private String sremark;
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();;
	
	@Column(name = "noffsetdvalidationdate", nullable = false)
	private int noffsetdvalidationdate;
  
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name="dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@Transient
	private transient String stransdisplaystatus;
	
	@Transient
	private transient String svalidationdate;
	
	@Transient
	private transient String sinstrumentid;
	
	@Transient
	private transient short ntranscode;
	
	@Transient
	private transient String stransstatus;
	
	@Transient
	private transient String stzvalidationdate;
	
	@Transient
	private transient String sinstrumentname;
	
	@Transient
	private transient String svalidationstatus;
	
	@Transient
	private transient String stimezoneid;
	
	@Transient
	private transient short ntimezonecode;
	
	@Transient
	private transient boolean isreadonly;
	
	@Transient
	private transient String sheadername;



	@Override
	public InstrumentValidation mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final InstrumentValidation objInstValidation = new InstrumentValidation();
		
		objInstValidation.setNinstrumentvalidationcode(getInteger(arg0,"ninstrumentvalidationcode",arg1));
		objInstValidation.setNinstrumentcode(getInteger(arg0,"ninstrumentcode",arg1));
		objInstValidation.setNusercode(getInteger(arg0,"nusercode",arg1));
		objInstValidation.setDvalidationdate(getInstant(arg0,"dvalidationdate",arg1));
		objInstValidation.setNvalidationstatus(getShort(arg0,"nvalidationstatus",arg1));
		objInstValidation.setSremark(StringEscapeUtils.unescapeJava(getString(arg0,"sremark",arg1)));
		objInstValidation.setStransdisplaystatus(getString(arg0,"stransdisplaystatus",arg1));
		objInstValidation.setSinstrumentid(getString(arg0,"sinstrumentid",arg1));
		objInstValidation.setNtranscode(getShort(arg0,"ntranscode",arg1));
		objInstValidation.setStransstatus(getString(arg0,"stransstatus",arg1));
		objInstValidation.setSvalidationdate(getString(arg0,"svalidationdate",arg1));
		objInstValidation.setNtzvalidationdate(getShort(arg0,"ntzvalidationdate",arg1));
		objInstValidation.setStzvalidationdate(getString(arg0,"stzvalidationdate",arg1));
		objInstValidation.setSinstrumentname(getString(arg0,"sinstrumentname",arg1));
		objInstValidation.setSvalidationstatus(getString(arg0,"svalidationstatus",arg1));
		objInstValidation.setStimezoneid(getString(arg0,"stimezoneid",arg1));
		objInstValidation.setNtimezonecode(getShort(arg0,"ntimezonecode",arg1));
		objInstValidation.setIsreadonly(getBoolean(arg0,"isreadonly", arg1));
		objInstValidation.setNoffsetdvalidationdate(getInteger(arg0,"noffsetdvalidationdate", arg1));
		objInstValidation.setSheadername(getString(arg0,"sheadername",arg1));
		objInstValidation.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objInstValidation.setNsitecode(getShort(arg0,"nsitecode",arg1));

		return objInstValidation;
	}

}
