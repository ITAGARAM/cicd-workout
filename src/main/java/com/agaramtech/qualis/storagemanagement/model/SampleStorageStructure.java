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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "samplestoragelocation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SampleStorageStructure extends CustomizedResultsetRowMapper<SampleStorageStructure>
		implements Serializable, RowMapper<SampleStorageStructure> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nsamplestoragelocationcode")
	private short nsamplestoragelocationcode;

	@Column(name = "nprojecttypecode")
	private int nprojecttypecode;

	@Column(name = "nproductcode")
	private int nproductcode;

	@Column(name = "ssamplestoragelocationname")
	private String ssamplestoragelocationname = "";

	@Column(name = "nstoragecategorycode")
	private int nstoragecategorycode;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();

	@Column(name = "ncontainertypecode")
	private int ncontainertypecode;

	@Column(name = "ncontainerstructurecode")
	private int ncontainerstructurecode;

	@Column(name = "nneedposition")
	private int nneedposition;

	@Column(name = "nneedautomapping")
	private int nneedautomapping;

	@Column(name = "nrow")
	private int nrow;

	@Column(name = "ncolumn")
	private int ncolumn;

	@Column(name = "nquantity")
	private Double nquantity;

	@Column(name = "nnoofcontainer")
	private int nnoofcontainer;

	@Column(name = "nunitcode")
	private int nunitcode;

	@Column(name = "ndirectionmastercode")
	private short ndirectionmastercode;

	@Column(name = "nmappingtranscode")
	private short nmappingtranscode;
	
	@Column(name = "nmastersitecode")
	private short nmastersitecode;
	
	/*@Column(name = "ninstrumentcode")
	private int ninstrumentcode;*/

	@Transient
	private transient int ntransactionstatus;

	@Transient
	private transient String sstoragecategoryname;

	@Transient
	private transient String stransdisplaystatus;

	@Override
	public SampleStorageStructure mapRow(ResultSet arg0, int arg1) throws SQLException {
		final SampleStorageStructure sampleStorageLocation = new SampleStorageStructure();
		sampleStorageLocation.setNsamplestoragelocationcode(getShort(arg0, "nsamplestoragelocationcode", arg1));
		sampleStorageLocation.setSsamplestoragelocationname(StringEscapeUtils.unescapeJava(getString(arg0, "ssamplestoragelocationname", arg1)));
		sampleStorageLocation.setNstoragecategorycode(getInteger(arg0, "nstoragecategorycode", arg1));
		sampleStorageLocation.setNproductcode(getInteger(arg0, "nproductcode", arg1));
		sampleStorageLocation.setNprojecttypecode(getInteger(arg0, "nprojecttypecode", arg1));
		sampleStorageLocation.setNneedautomapping(getInteger(arg0, "nneedautomapping", arg1));
		sampleStorageLocation.setNrow(getInteger(arg0, "nrow", arg1));
		sampleStorageLocation.setNcolumn(getInteger(arg0, "ncolumn", arg1));
		sampleStorageLocation.setNcontainertypecode(getInteger(arg0, "ncontainertypecode", arg1));
		sampleStorageLocation.setNcontainerstructurecode(getInteger(arg0, "ncontainerstructurecode", arg1));
		sampleStorageLocation.setNneedposition(getInteger(arg0, "nneedposition", arg1));
		sampleStorageLocation.setNquantity(getDouble(arg0, "nquantity", arg1));
		sampleStorageLocation.setNdirectionmastercode(getShort(arg0, "ndirectionmastercode", arg1));
		sampleStorageLocation.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		sampleStorageLocation.setNsitecode(getShort(arg0, "nsitecode", arg1));
		sampleStorageLocation.setNstatus(getShort(arg0, "nstatus", arg1));
		sampleStorageLocation.setNnoofcontainer(getInteger(arg0, "nnoofcontainer", arg1));
		sampleStorageLocation.setNunitcode(getInteger(arg0, "nunitcode", arg1));
		sampleStorageLocation.setNmappingtranscode(getShort(arg0, "nmappingtranscode", arg1));
		sampleStorageLocation.setNmastersitecode(getShort(arg0, "nmastersitecode", arg1));
		/*sampleStorageLocation.setNinstrumentcode(getInteger(arg0, "ninstrumentcode", arg1));*/


		return sampleStorageLocation;
	}

}