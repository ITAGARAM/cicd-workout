package com.agaramtech.qualis.global;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import org.apache.commons.text.StringEscapeUtils;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "apiendpoint")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ApiEndPoint extends CustomizedResultsetRowMapper<ApiEndPoint> implements Serializable,RowMapper<ApiEndPoint>  {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "napiendpointcode")
	private int napiendpointcode;

	@Column(name = "sapiendpoint", length = 100, nullable = false)
	private String sapiendpoint;

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

	@Override
	public ApiEndPoint mapRow(ResultSet arg0, int arg1) throws SQLException {
		final ApiEndPoint apiendpoint = new ApiEndPoint();
		apiendpoint.setNapiendpointcode(getInteger(arg0,"napiendpointcode",arg1));
		apiendpoint.setSapiendpoint(StringEscapeUtils.unescapeJava(getString(arg0,"sapiendpoint",arg1)));
		apiendpoint.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0,"sdescription",arg1)));
		apiendpoint.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		apiendpoint.setNsitecode(getShort(arg0,"nsitecode",arg1));
		apiendpoint.setNstatus(getShort(arg0,"nstatus",arg1));
		return apiendpoint;
	}

}
