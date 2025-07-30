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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "containerstoragecondition")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ContainerStorageCondition extends CustomizedResultsetRowMapper implements Serializable, Cloneable, RowMapper<ContainerStorageCondition> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ncontainerstoragecode")
	private short ncontainerstoragecode;

	@Column(name = "scontainercode")
	private String scontainercode = "";

	@Column(name = "nstorageconditioncode")
	private short nstorageconditioncode;

	@Column(name = "nsamplestoragelocationcode")
	private short nsamplestoragelocationcode;

	@Column(name = "slocationhierarchy")
	private String slocationhierarchy = "";

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Transient
	private String sstorageconditionname = "";

	@Override
	public ContainerStorageCondition mapRow(ResultSet arg0, int arg1) throws SQLException {

		ContainerStorageCondition containerStorageCondition = new ContainerStorageCondition();
		containerStorageCondition.setNcontainerstoragecode(getShort(arg0, "ncontainerstoragecode", arg1));
		containerStorageCondition.setScontainercode(getString(arg0, "scontainercode", arg1));
		containerStorageCondition.setSstorageconditionname(getString(arg0, "sstorageconditionname", arg1));
		containerStorageCondition.setNstorageconditioncode(getShort(arg0, "nstorageconditioncode", arg1));
		containerStorageCondition.setNsamplestoragelocationcode(getShort(arg0, "nsamplestoragelocationcode", arg1));
		containerStorageCondition.setSlocationhierarchy(getString(arg0, "slocationhierarchy", arg1));
//		containerStorageCondition.setNoffsetdmodifieddate(getInteger(arg0,"noffsetdmodifieddate",arg1));
//		containerStorageCondition.setNtzmodifieddate(getShort(arg0,"ntzmodifieddate",arg1));
		containerStorageCondition.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		containerStorageCondition.setNsitecode(getShort(arg0, "nsitecode", arg1));
		containerStorageCondition.setNstatus(getShort(arg0, "nstatus", arg1));
		return containerStorageCondition;
	}
}
