package com.agaramtech.qualis.barcode.model;

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
@Table(name = "samplecollectiontype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SampleCollectionType extends CustomizedResultsetRowMapper<SampleCollectionType>
		implements Serializable, RowMapper<SampleCollectionType> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nsamplecollectiontypecode ")
	private int nsamplecollectiontypecode=Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nprojecttypecode ")
	private int nprojecttypecode=Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "nproductcode ")
	private int nproductcode=Enumeration.TransactionStatus.NA.gettransactionstatus();

	@Column(name = "scode", length = 2, nullable = false)
	private String scode;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
    @Transient
	private transient boolean isSampletypeExists;
    
    @Transient
	private transient boolean isCodeExists;
    
    @Transient
	private transient String sprojecttypename="";
    
    @Transient
	private transient String sproductname="";

	@Override
	public SampleCollectionType mapRow(ResultSet arg0, int arg1) throws SQLException {
		SampleCollectionType sampleCollectionType = new SampleCollectionType();
		sampleCollectionType.setNsamplecollectiontypecode(getInteger(arg0, "nsamplecollectiontypecode", arg1));
		sampleCollectionType.setNprojecttypecode(getInteger(arg0, "nprojecttypecode", arg1));
		sampleCollectionType.setNproductcode(getInteger(arg0, "nproductcode", arg1));
		sampleCollectionType.setScode(StringEscapeUtils.unescapeJava(getString(arg0, "scode", arg1)));
		sampleCollectionType.setSproductname(getString(arg0, "sproductname", arg1));
		sampleCollectionType.setSprojecttypename(getString(arg0, "sprojecttypename", arg1));
		sampleCollectionType.setSampletypeExists(getBoolean(arg0, "isSampletypeExists", arg1));
		sampleCollectionType.setCodeExists(getBoolean(arg0, "isCodeExists", arg1));
		sampleCollectionType.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		sampleCollectionType.setNsitecode(getShort(arg0, "nsitecode", arg1));
		sampleCollectionType.setNstatus(getShort(arg0, "nstatus", arg1));
		return sampleCollectionType;
	}

}