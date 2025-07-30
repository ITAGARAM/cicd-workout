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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'diseasecategory' table of the Database.
 */
/**
 * @author sujatha.v
 * BGSI-1
 * 26/06/2025
 */
@Entity
@Table(name = "diseasecategory")
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DiseaseCategory extends CustomizedResultsetRowMapper<DiseaseCategory>
		implements Serializable, RowMapper<DiseaseCategory> {

	private final static long serialVersionUID=1L;

	@Id
	@Column(name="ndiseasecategorycode")
	private int ndiseasecategorycode;

	@Column(name= "sdiseasecategoryname", length= 200, nullable= false)
	private String sdiseasecategoryname;

	@Column(name= "sdescription", length= 300)
	private String sdescription="";

	@Column(name= "dmodifieddate", nullable= false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name="nsitecode", nullable= false)
	private short nsitecode= (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name= "nstatus", nullable= false)
	private short nstatus= (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public DiseaseCategory mapRow(ResultSet arg0, int arg1)throws SQLException {
		
		DiseaseCategory diseaseCategory= new DiseaseCategory();
		
		diseaseCategory.setNdiseasecategorycode(getInteger(arg0,"ndiseasecategorycode", arg1));
		diseaseCategory.setSdiseasecategoryname(getString(arg0,"sdiseasecategoryname", arg1));
		diseaseCategory.setSdescription(getString(arg0, "sdescription", arg1));
		diseaseCategory.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		diseaseCategory.setNsitecode(getShort(arg0, "nsitecode", arg1));
		diseaseCategory.setNstatus(getShort(arg0, "nstatus", arg1));
		return diseaseCategory;
	}
}
