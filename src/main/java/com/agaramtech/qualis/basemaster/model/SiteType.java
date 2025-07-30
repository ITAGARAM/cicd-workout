package com.agaramtech.qualis.basemaster.model;

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
 * This class is used to map the fields of 'sitetype' table of the Database.
 */
/**
* @author sujatha.v
* BGSI-5
* 02/07/2025
*/

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "sitetype")
@Data
@EqualsAndHashCode(callSuper = false)
public class SiteType extends CustomizedResultsetRowMapper<SiteType>
		implements Serializable, RowMapper<SiteType> {

	private final static long serialVersionUID = 1L;

	@Id
	@Column(name = "nsitetypecode")
	private int nsitetypecode;

	@Column(name = "ssitetypename", length = 150, nullable = false)
	private String ssitetypename;

	@Column(name = "nhierarchicalorderno", length = 2, nullable = false)
	private short nhierarchicalorderno;

	@Column(name = "sdescription", length = 300)
	private String sdescription = "";

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public SiteType mapRow(ResultSet arg0, int arg1) throws SQLException {
		SiteType objSiteType = new SiteType();
		objSiteType.setNsitetypecode(getInteger(arg0, "nsitetypecode", arg1));
		objSiteType.setSsitetypename(getString(arg0, "ssitetypename", arg1));
		objSiteType.setNhierarchicalorderno(getShort(arg0, "nhierarchicalorderno", arg1));
		objSiteType.setSdescription(getString(arg0, "sdescription", arg1));
		objSiteType.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objSiteType.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objSiteType.setNstatus(getShort(arg0, "nstatus", arg1));

		return objSiteType;
	}

}
