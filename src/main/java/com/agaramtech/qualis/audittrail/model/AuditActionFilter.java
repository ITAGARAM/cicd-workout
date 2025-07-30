package com.agaramtech.qualis.audittrail.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;
import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;
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
 * This class is used to map the fields of 'auditactionfilter' table of the
 * Database.
 * 
 * @author ATE234
 * @version 9.0.0.1
 * @since 15-04- 2025
 */
@Entity
@Table(name = "auditactionfilter")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class AuditActionFilter extends CustomizedResultsetRowMapper<AuditActionFilter>
		implements Serializable, RowMapper<AuditActionFilter> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nauditactionfiltercode")
	private short nauditactionfiltercode;

	@ColumnDefault("0")
	@Column(name = "nsorter", nullable = false)
	private short nsorter =( short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus =( short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@Column(name = "dauditdate", nullable = false)
	private Instant dauditdate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode=( short)Enumeration.TransactionStatus.NA.gettransactionstatus();	

	@Transient
	private transient String sdefaultname;
	@Transient
	private transient String sdisplayname;
	@Transient
	private transient String sauditactionfiltername;

	@Override
	public AuditActionFilter mapRow(ResultSet arg0, int arg1) throws SQLException {

		final AuditActionFilter auditactionfilter = new AuditActionFilter();

		auditactionfilter.setNauditactionfiltercode(getShort(arg0, "nauditactionfiltercode", arg1));
		auditactionfilter.setNsorter(getShort(arg0, "nsorter", arg1));
		auditactionfilter.setSdefaultname(getString(arg0, "sdefaultname", arg1));
		auditactionfilter.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		auditactionfilter.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		auditactionfilter.setNsitecode(getShort(arg0, "nsitecode", arg1));
		auditactionfilter.setNstatus(getShort(arg0, "nstatus", arg1));
		auditactionfilter.setSauditactionfiltername(getString(arg0, "sauditactionfiltername", arg1));
		auditactionfilter.setDauditdate(getInstant(arg0, "dauditdate", arg1));

		return auditactionfilter;
	}

}
