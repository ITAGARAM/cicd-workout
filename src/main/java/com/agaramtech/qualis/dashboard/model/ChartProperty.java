package com.agaramtech.qualis.dashboard.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;
import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'charttype' table of the Database.
 * 
 * @author ATE113
 * @version 9.0.0.1
 * @since 05- Oct- 2020
 */
@Entity
@Table(name = "chartproperty")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ChartProperty extends CustomizedResultsetRowMapper<ChartProperty> implements Serializable, RowMapper<ChartProperty> {
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nchartpropertycode")
	private short nchartpropertycode;

	@Column(name = "ncharttypecode", nullable = false)
	private short ncharttypecode;

	@Column(name = "schartpropertyname", length = 100, nullable = false)
	private String schartpropertyname;

	@Column(name = "sdescription", length = 255)
	private String sdescription;

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = 1;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode;

	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;

	@Override
	public ChartProperty mapRow(ResultSet arg0, int arg1) throws SQLException {

		final ChartProperty chartProperty = new ChartProperty();

		chartProperty.setNchartpropertycode(getShort(arg0, "nchartpropertycode", arg1));
		chartProperty.setNcharttypecode(getShort(arg0, "ncharttypecode", arg1));
		chartProperty.setSchartpropertyname(getString(arg0, "schartpropertyname", arg1));
		chartProperty.setSdescription(getString(arg0, "sdescription", arg1));
		chartProperty.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		chartProperty.setNsitecode(getShort(arg0, "nsitecode", arg1));

		return chartProperty;
	}

}
