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
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "containerstructure")
@Data
public class ContainerStructure extends CustomizedResultsetRowMapper<ContainerStructure>
		implements Serializable, RowMapper<ContainerStructure> {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "ncontainerstructurecode ")
	private int ncontainerstructurecode;

	@Column(name = "ncontainertypecode ")
	private int ncontainertypecode;

	@Column(name = "scontainerstructurename ", length = 100, nullable = false)
	private String scontainerstructurename;

	@Column(name = "nrow", nullable = false)
	private int nrow;

	@Column(name = "ncolumn", nullable = false)
	private int ncolumn;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private transient String scontainertype;
	
	@Transient
	private transient boolean isduplicatename;

	@Override
	public ContainerStructure mapRow(ResultSet arg0, int arg1) throws SQLException {
		ContainerStructure containerStructure = new ContainerStructure();
		containerStructure.setNcontainerstructurecode(getInteger(arg0, "ncontainerstructurecode", arg1));
		containerStructure.setNcontainertypecode(getInteger(arg0, "ncontainertypecode", arg1));
		containerStructure.setScontainerstructurename(getString(arg0, "scontainerstructurename", arg1));
		containerStructure.setNrow(getInteger(arg0, "nrow", arg1));
		containerStructure.setNcolumn(getInteger(arg0, "ncolumn", arg1));
		containerStructure.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		containerStructure.setNsitecode(getShort(arg0, "nsitecode", arg1));
		containerStructure.setScontainertype(getString(arg0, "scontainertype", arg1));
		containerStructure.setIsduplicatename(getBoolean(arg0, "isduplicatename", arg1));
		return containerStructure;
	}
}