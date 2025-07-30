package com.agaramtech.qualis.basemaster.model;

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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "releasesamplefilterfields")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ReleaseSampleFilterFields extends CustomizedResultsetRowMapper<ReleaseSampleFilterFields>
		implements Serializable, RowMapper<ReleaseSampleFilterFields> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nrelsampfiltercode ")
	private short nrelsampfiltercode;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@ColumnDefault("4")
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus = (short) Enumeration.TransactionStatus.NO.gettransactionstatus();

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Override
	public ReleaseSampleFilterFields mapRow(ResultSet arg0, int arg1) throws SQLException {
		final ReleaseSampleFilterFields objreleasesamplefilterfields = new ReleaseSampleFilterFields();
		objreleasesamplefilterfields.setNrelsampfiltercode(getShort(arg0, "nrelsampfiltercode", arg1));
		objreleasesamplefilterfields.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objreleasesamplefilterfields.setNstatus(getShort(arg0, "nstatus", arg1));
		objreleasesamplefilterfields.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objreleasesamplefilterfields.setNdefaultstatus(getShort(arg0, "ndefaultstatus", arg1));
		objreleasesamplefilterfields.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		return objreleasesamplefilterfields;
	}

}
