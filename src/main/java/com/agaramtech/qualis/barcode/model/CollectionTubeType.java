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
/**
 * This class is used to map the fields of 'CollectionTubeType' table of the Database.
 */
@Entity
@Table(name = "collectiontubetype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CollectionTubeType extends CustomizedResultsetRowMapper<CollectionTubeType>
		implements Serializable, RowMapper<CollectionTubeType> {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "ncollectiontubetypecode ")
	private int ncollectiontubetypecode;

	@Column(name = "nprojecttypecode ")
	private int nprojecttypecode=-1;

	@Column(name = "stubename ", length = 100, nullable = false)
	private String stubename;

	@Column(name = "ncode", nullable = false)
	private short ncode;
	
	@ColumnDefault("1")
	@Column(name = "ncodelength", nullable = false)
	private int ncodelength = 1;

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;

	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Transient
	private transient String sprojecttypename="";
	
	@Transient
	private transient boolean istubename;
	
	@Transient
	private transient boolean iscode;

	@Override
	public CollectionTubeType mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final CollectionTubeType objCollectionTubeType = new CollectionTubeType();
		
		objCollectionTubeType.setNcollectiontubetypecode(getInteger(arg0, "ncollectiontubetypecode", arg1));
		objCollectionTubeType.setNprojecttypecode(getInteger(arg0, "nprojecttypecode", arg1));
		objCollectionTubeType.setStubename(StringEscapeUtils.unescapeJava(getString(arg0, "stubename", arg1)));
		objCollectionTubeType.setNcode(getShort(arg0, "ncode", arg1));
		objCollectionTubeType.setNcodelength(getInteger(arg0, "ncodelength", arg1));
		objCollectionTubeType.setIstubename(getBoolean(arg0, "istubename", arg1));
		objCollectionTubeType.setIscode(getBoolean(arg0, "iscode", arg1));
		objCollectionTubeType.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objCollectionTubeType.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objCollectionTubeType.setNstatus(getShort(arg0, "nstatus", arg1));
		objCollectionTubeType.setSprojecttypename(getString(arg0, "sprojecttypename", arg1));
		
		return objCollectionTubeType;
	}

}