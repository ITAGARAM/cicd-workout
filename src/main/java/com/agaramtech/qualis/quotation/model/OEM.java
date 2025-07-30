package com.agaramtech.qualis.quotation.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "OEM")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class OEM extends CustomizedResultsetRowMapper<OEM> implements Serializable,RowMapper<OEM> {

	
	
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "noemcode")
	private int noemcode;
	
	@Column(name = "soemname", length = 100, nullable = false)
	private String soemname;
	
	@Column(name = "sdescription", length = 250, nullable = false)
	private String sdescription;
	
	@Column(name = "dmodifieddate", nullable = false)
	private Date dmodifieddate;
	
	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();


  public OEM mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		OEM objOEM = new OEM();
		
		objOEM.setNoemcode(getInteger(arg0,"noemcode",arg1));
		objOEM.setSoemname(getString(arg0,"soemname",arg1));
		objOEM.setSdescription(getString(arg0,"sdescription",arg1));
		objOEM.setDmodifieddate(getDate(arg0,"dmodifieddate",arg1));
		objOEM.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objOEM.setNstatus(getShort(arg0,"nstatus",arg1));
		
		return objOEM;
		
	}

}