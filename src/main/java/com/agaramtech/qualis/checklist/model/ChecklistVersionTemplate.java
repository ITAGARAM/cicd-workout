package com.agaramtech.qualis.checklist.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

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
@Table(name="checklistversiontemplate")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ChecklistVersionTemplate extends CustomizedResultsetRowMapper<ChecklistVersionTemplate> implements Serializable,RowMapper<ChecklistVersionTemplate>   {

	private static final long serialVersionUID = 1L;

	@Id
    @Column(name = "nchecklistversiontempcode")
	private int nchecklistversiontempcode;
	
	@Column(name = "nchecklistversioncode", nullable=false)
	private int nchecklistversioncode;
	
	@Column(name = "nchecklistversionqbcode", nullable=false)
	private int nchecklistversionqbcode;
	
	@Column(name = "nchecklistqbcode", nullable=false)
	private int nchecklistqbcode;
	
    @Column(name = "sdefaultvalue",length = 255)
    private String  sdefaultvalue;
    
    @Column(name = "dmodifieddate", nullable=false)
    private Instant dmodifieddate;
    
	@ColumnDefault("-1")
	@Column(name="nsitecode", nullable=false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
    @ColumnDefault("1")
    @Column(name = "nstatus", nullable=false)
    private short  nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
    
    @Transient
     private transient Map<String,Object> jsondata;
    
    
	@Override
	public ChecklistVersionTemplate mapRow(ResultSet arg0, int arg1) throws SQLException {
		final ChecklistVersionTemplate objChecklistVersionTemplate = new ChecklistVersionTemplate();
		objChecklistVersionTemplate.setNchecklistversiontempcode(getInteger(arg0,"nchecklistversiontempcode",arg1));
		objChecklistVersionTemplate.setNchecklistversioncode(getInteger(arg0,"nchecklistversioncode",arg1));
		objChecklistVersionTemplate.setNchecklistversionqbcode(getInteger(arg0,"nchecklistversionqbcode",arg1));
		objChecklistVersionTemplate.setNchecklistqbcode(getInteger(arg0,"nchecklistqbcode",arg1));
		objChecklistVersionTemplate.setSdefaultvalue(StringEscapeUtils.unescapeJava(getString(arg0,"sdefaultvalue",arg1)));
		objChecklistVersionTemplate.setNstatus(getShort(arg0,"nstatus",arg1));
		objChecklistVersionTemplate.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objChecklistVersionTemplate.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		return objChecklistVersionTemplate;	
		}
	
	
	
}
