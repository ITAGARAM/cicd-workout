package com.agaramtech.qualis.stability.model;

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
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'packagecategory' table of the Database.
 * @author ATE113
 * @version 9.0.0.1
 * @since   26- Sep- 2022
 */
/**
 * @author corpagaramATE113
 *
 */
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "packagecategory")
@Data
public class PackageCategory extends CustomizedResultsetRowMapper<PackageCategory>
		implements Serializable, RowMapper<PackageCategory> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "npackagecategorycode")
	private int npackagecategorycode;
	@Column(name = "spackagecategoryname", length = 100, nullable = false)
	private String spackagecategoryname;
	@Column(name = "sdescription", length = 255)
	private String sdescription = "";
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String sdisplaystatus;

	@Override
	public PackageCategory mapRow(ResultSet arg0, int arg1) throws SQLException {
		PackageCategory objpackcategory = new PackageCategory();
		objpackcategory.setNpackagecategorycode(getInteger(arg0, "npackagecategorycode", arg1));
		objpackcategory.setSpackagecategoryname(getString(arg0, "spackagecategoryname", arg1));
		objpackcategory.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objpackcategory.setSdescription(getString(arg0, "sdescription", arg1));
		objpackcategory.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objpackcategory.setNstatus(getShort(arg0, "nstatus", arg1));
		return objpackcategory;
	}
}