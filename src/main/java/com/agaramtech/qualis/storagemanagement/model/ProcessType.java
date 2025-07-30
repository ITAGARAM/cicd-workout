package com.agaramtech.qualis.storagemanagement.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import org.apache.commons.text.StringEscapeUtils;
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

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@Table(name = "processtype")
public class ProcessType extends CustomizedResultsetRowMapper<ProcessType>
		implements Serializable, RowMapper<ProcessType> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nprocesstypecode")
	private int nprocesstypecode;

	@Column(name = "sprocesstypename", length = 100, nullable = false)
	private String sprocesstypename;

	@Column(name = "sdescription", length = 255)
	private String sdescription= "";

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Override
	public ProcessType mapRow(ResultSet arg0, int arg1) throws SQLException {

		final ProcessType objProcessType = new ProcessType();
		objProcessType.setNprocesstypecode(getInteger(arg0, "nprocesstypecode", arg1));
		objProcessType.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		objProcessType.setSprocesstypename(StringEscapeUtils.unescapeJava(getString(arg0, "sprocesstypename", arg1)));
		objProcessType.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objProcessType.setNstatus(getShort(arg0, "nstatus", arg1));
		objProcessType.setNsitecode(getShort(arg0, "nsitecode", arg1));
		return objProcessType;
	}
}
