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
 * This class is used to map the fields of 'packagemaster' table of the Database.
 * @author ATE113
 * @version 9.0.0.1
 * @since   26- Sep- 2022
 */
/**
 */
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "packagemaster")
@Data
public class PackageMaster extends CustomizedResultsetRowMapper<PackageMaster>
		implements Serializable, RowMapper<PackageMaster> {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "npackagemastercode")
	private int npackagemastercode;
	
	@Column(name = "npackagecategorycode")
	private int npackagecategorycode;
	
	@Column(name = "spackagename", length = 100, nullable = false)
	private String spackagename;
	
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
	private transient String spackagecategoryname;
	
	@Transient
	private transient String smodifieddate;

	@Override
	public PackageMaster mapRow(ResultSet arg0, int arg1) throws SQLException {
		PackageMaster packageMaster = new PackageMaster();
		packageMaster.setNpackagemastercode(getInteger(arg0, "npackagemastercode", arg1));
		packageMaster.setNpackagecategorycode(getInteger(arg0, "npackagecategorycode", arg1));
		packageMaster.setSpackagename(getString(arg0, "spackagename", arg1));
		packageMaster.setSpackagecategoryname(getString(arg0, "spackagecategoryname", arg1));
		packageMaster.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		packageMaster.setSdescription(getString(arg0, "sdescription", arg1));
		packageMaster.setNsitecode(getShort(arg0, "nsitecode", arg1));
		packageMaster.setNstatus(getShort(arg0, "nstatus", arg1));
		packageMaster.setSmodifieddate(getString(arg0, "smodifieddate", arg1));
		return packageMaster;
	}
}