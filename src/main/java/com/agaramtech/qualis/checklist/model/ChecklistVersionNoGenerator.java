package com.agaramtech.qualis.checklist.model;

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
@Table(name="checklistversionnogenerator")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class ChecklistVersionNoGenerator extends CustomizedResultsetRowMapper<ChecklistVersionNoGenerator>  implements RowMapper<ChecklistVersionNoGenerator>,Serializable{

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "stablename", length = 100)
	private String stablename;	
	
	@Column(name = "nsequenceno", nullable=false)
	private  int nsequenceno;
	
	@Column(name = "dmodifieddate", nullable=false)
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name="nsitecode", nullable=false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable=false)
	private short  nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus() ;
	
	@Transient
	private transient String stransdisplaysatatus;
	


	@Override
	public ChecklistVersionNoGenerator mapRow(ResultSet arg0, int arg1) throws SQLException {
		final ChecklistVersionNoGenerator objSeq=new ChecklistVersionNoGenerator();
		objSeq.setStablename(StringEscapeUtils.unescapeJava(getString(arg0,"stablename",arg1)));
		objSeq.setNsequenceno(getInteger(arg0,"nsequenceno",arg1));
		objSeq.setNstatus(getShort(arg0,"nstatus",arg1));
		objSeq.setStransdisplaysatatus(getString(arg0,"stransdisplaysatatus",arg1));
		objSeq.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objSeq.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		return objSeq;
	}
	

}
