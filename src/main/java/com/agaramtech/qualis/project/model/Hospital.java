package com.agaramtech.qualis.project.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
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
 * This class is used to map the fields of 'unit' table of the Database.
 * 
 * @author Mullai Balaji.V BGSI-4 30/06/2025
 * 
 */
@Entity
@Table(name = "hospital")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class Hospital extends CustomizedResultsetRowMapper<Hospital> implements Serializable, RowMapper<Hospital> {
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nhospitalcode")
	private int nhospitalcode;

	@Column(name = "shospitalname ", length = 100, nullable = false)
	private String shospitalname;

	//@Column(name = "shospitalcode", length = 2, nullable = false)
	//private String shospitalcode;

	@Column(name = "sdescription", length = 255)
	private String sdescription = "";

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private transient int nsitehospitalmappingcode;

	@Transient
	private transient int nmappingsitecode;
	
	@Override
	public Hospital mapRow(ResultSet arg0, int arg1) throws SQLException {
		final Hospital objHospital = new Hospital();

		objHospital.setNhospitalcode(getInteger(arg0, "nhospitalcode", arg1));
		//objHospital.setShospitalcode(getString(arg0, "shospitalcode", arg1));
		objHospital.setShospitalname(getString(arg0, "shospitalname", arg1));
		objHospital.setSdescription(getString(arg0, "sdescription", arg1));
		objHospital.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objHospital.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objHospital.setNstatus(getShort(arg0, "nstatus", arg1));
		objHospital.setNsitehospitalmappingcode(getShort(arg0, "nsitehospitalmappingcode", arg1));
		objHospital.setNmappingsitecode(getShort(arg0, "nmappingsitecode", arg1));


		return objHospital;

	}

}