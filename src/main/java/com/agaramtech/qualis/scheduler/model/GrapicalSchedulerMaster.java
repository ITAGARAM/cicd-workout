package com.agaramtech.qualis.scheduler.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;
import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/*@Entity
@Table(name = "grapicalschedulermaster")*/
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class GrapicalSchedulerMaster extends CustomizedResultsetRowMapper<GrapicalSchedulerMaster>
implements Serializable, RowMapper<GrapicalSchedulerMaster> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nschedulecode")
	private int nschedulecode;

	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = 1;

	@Column(name = "stitle", length = 100)
	private String stitle;

	@Column(name = "sscheduletype", length = 255)
	private String sscheduletype;

	@Override
	public GrapicalSchedulerMaster mapRow(ResultSet arg0, int arg1) throws SQLException {

		final GrapicalSchedulerMaster objSch = new GrapicalSchedulerMaster();

		objSch.setNschedulecode(getShort(arg0, "nschedulecode", arg1));
		objSch.setJsondata(getJsonObject(arg0, "jsondata", arg1));
		objSch.setNstatus(getShort(arg0, "nstatus", arg1));
		objSch.setSscheduletype(getString(arg0, "sscheduletype", arg1));
		objSch.setStitle(getString(arg0, "stitle", arg1));

		return objSch;

	}

}
