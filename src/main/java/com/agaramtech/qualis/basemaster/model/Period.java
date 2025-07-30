package com.agaramtech.qualis.basemaster.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


/**
 * This class is used to map the fields of 'period' table of the Database.
 * @author ATE113
 * @version 9.0.0.1
 * @since   12- Sep- 2020
 */

@Entity
@Table(name = "period")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Period extends CustomizedResultsetRowMapper<Object> implements Serializable, RowMapper<Period>{

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "nperiodcode")
	private short nperiodcode;
	
	@Lob
	@Column(name="jsondata",columnDefinition = "jsonb")
	private Map<String,Object> jsondata;
	
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

//	@ColumnDefault("4")
//	@Column(name = "ndefaultstatus", nullable=false)
//	private short ndefaultstatus=4;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable=false)
	private short nsitecode = -1;
	
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable=false)
	private short nstatus = 1;

	@Transient
	private transient String speriodname;

	@Transient
	private transient String sdescription;	

	@Transient
	private transient int ndata;

	@Transient
	private transient short nmaxlength;

	@Transient
	private transient String sdisplayname;

	@Transient
	private transient String sdefaultname;

	@Transient
	private transient String statunitname;

	@Transient
	private transient int ntatunitcode;

	@Transient
	private transient String sdeltaunitname;

	@Transient
	private transient int ndeltaunitcode;

	@Transient
	private transient short ndefaultstatus;

	@Transient
	private transient int ninstrumentperiodcode;

	@Transient
	private transient int nuserperiodcode;

	public Period mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final Period period = new Period();
		
		period.setNperiodcode(getShort(arg0,"nperiodcode",arg1));
		period.setSperiodname(getString(arg0,"speriodname",arg1));
		period.setSdescription(getString(arg0,"sdescription",arg1));        
		period.setNdata(getInteger(arg0,"ndata",arg1));
		period.setNmaxlength(getShort(arg0,"nmaxlength",arg1));
		period.setNdefaultstatus(getShort(arg0,"ndefaultstatus",arg1));
		period.setNsitecode(getShort(arg0,"nsitecode",arg1));
		period.setNstatus(getShort(arg0,"nstatus",arg1));
		period.setJsondata(unescapeString(getJsonObject(arg0,"jsondata",arg1)));
		period.setSdisplayname(getString(arg0,"sdisplayname",arg1));
		period.setSdefaultname(getString(arg0,"sdefaultname",arg1));
		period.setStatunitname(getString(arg0,"statunitname",arg1));
		period.setNtatunitcode(getShort(arg0,"ntatunitcode",arg1));
		period.setSdeltaunitname(getString(arg0,"sdeltaunitname",arg1));
		period.setNdeltaunitcode(getShort(arg0,"ndeltaunitcode",arg1));
		period.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		period.setNinstrumentperiodcode(getShort(arg0,"ninstrumentperiodcode",arg1));
		period.setNuserperiodcode(getShort(arg0,"nuserperiodcode",arg1));

		return period;
	}
	

}

