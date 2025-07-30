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
 * @author Mullai Balaji.V 
 * BGSI-2
 * 26/06/2025 
 */
@Entity
@Table(name = "disease")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class Disease extends CustomizedResultsetRowMapper<Disease> implements Serializable, RowMapper<Disease> {
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "ndiseasecode")
	private int ndiseasecode;


	
	@Column(name = "ndiseasecategorycode")
	private int ndiseasecategorycode;

	@Column(name = "sdiseasename ", length = 100, nullable = false)
	private String sdiseasename;

	@Column(name = "sdescription", length = 255)
	private String sdescription = "";

	@Column(name = "dmodifieddate",nullable=false	)
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String sdiseasecategoryname;

	
	@Override
	public Disease mapRow(ResultSet arg0, int arg1) throws SQLException {
		final Disease objDisease = new Disease();

		objDisease.setNdiseasecode(getInteger(arg0, "ndiseasecode", arg1));
		objDisease.setNdiseasecategorycode(getInteger(arg0, "ndiseasecategorycode", arg1));
		objDisease.setSdiseasename(getString(arg0, "sdiseasename", arg1));
		objDisease.setSdescription(getString(arg0, "sdescription", arg1));
		objDisease.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objDisease.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objDisease.setNstatus(getShort(arg0, "nstatus", arg1));
        objDisease.setSdiseasecategoryname(getString(arg0,"sdiseasecategoryname", arg1));
		return objDisease;

	}

}