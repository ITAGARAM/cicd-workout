/**
 * 
 */
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
 * This class is used to map the fields of 'BioProject' table of the Database.
 * @author ATE-234 Janakumar R
 * BGSI-2
 * 26/06/2025 
 */
@Entity
@Table(name = "bioproject")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)

public class BioProject extends CustomizedResultsetRowMapper<BioProject> implements Serializable,
RowMapper<BioProject> {
	
	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nbioprojectcode")
	private int nbioprojectcode;
	
	@Column(name = "ndiseasecategorycode")
	private int ndiseasecategorycode;

	@Column(name = "ndiseasecode")
	private int ndiseasecode;
	
	@Column(name = "sprojecttitle ", length = 100, nullable = false)
	private String sprojecttitle;
	
	@Column(name = "sprojectcode ", length = 2, nullable = false)
	private String sprojectcode;

	@Column(name = "sdescription", length = 255)
	private String sdescription = "";
	
	@Column(name = "nusercode ", nullable = false)
	private int nusercode;

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
	
	@Transient
	private transient String sdiseasename;  

	@Transient
	private transient String suserName; 
	
	@Transient
	private transient boolean issprojecttitle; 
	
	@Transient
	private transient boolean issprojectcode; 
	
	@Override
	public BioProject mapRow(ResultSet arg0, int arg1) throws SQLException {
		final BioProject objbioproject = new BioProject();

		objbioproject.setNbioprojectcode(getInteger(arg0, "nbioprojectcode", arg1));
		objbioproject.setNdiseasecategorycode(getInteger(arg0, "ndiseasecategorycode", arg1));
		objbioproject.setNdiseasecode(getInteger(arg0, "ndiseasecode", arg1));
		objbioproject.setSprojecttitle(getString(arg0, "sprojecttitle", arg1));
		objbioproject.setSprojectcode(getString(arg0, "sprojectcode", arg1));
		objbioproject.setNusercode(getInteger(arg0, "nusercode", arg1));
		objbioproject.setSdescription(getString(arg0, "sdescription", arg1));
		objbioproject.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objbioproject.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objbioproject.setNstatus(getShort(arg0, "nstatus", arg1));
		objbioproject.setSdiseasecategoryname(getString(arg0, "sdiseasecategoryname", arg1));
		objbioproject.setSdiseasename(getString(arg0, "sdiseasename", arg1));
		objbioproject.setSuserName(getString(arg0, "suserName", arg1));
		objbioproject.setIssprojecttitle(getBoolean(arg0, "issprojecttitle", arg1));
		objbioproject.setIssprojectcode(getBoolean(arg0, "issprojectcode", arg1));

		return objbioproject;

	}
}
