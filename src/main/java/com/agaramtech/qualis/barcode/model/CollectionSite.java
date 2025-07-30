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
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * This class is used to map the fields of 'CollectionSite' table of the
 * Database.
 */
@Entity
@Table(name = "collectionsite")
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class CollectionSite extends CustomizedResultsetRowMapper<CollectionSite> implements Serializable, RowMapper<CollectionSite> {
	
	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "ncollectionsitecode")
	private int ncollectionsitecode;
	
	@Column(name = "ssitename", length = 100, nullable = false)
	private String ssitename;
	
	@Column(name = "scode", length = 2, nullable = false)
	private String scode;
	
	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode=(short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "nstatus", nullable = false)
	@ColumnDefault("1")
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@Column(name = "nprojecttypecode")
	private int nprojecttypecode;

	@Transient
	private transient String sprojecttypename;
	
	@Transient
	private transient String smodifieddate;
	
	@Transient
	private transient Boolean iscollectionsite;
	
	@Transient
	private transient Boolean iscode;

	@Override
	public CollectionSite mapRow(ResultSet arg0, int arg1) throws SQLException {
		CollectionSite collectionSite = new CollectionSite();
		collectionSite.setNcollectionsitecode(getInteger(arg0, "ncollectionsitecode", arg1));
		collectionSite.setSsitename(StringEscapeUtils.unescapeJava(getString(arg0, "ssitename", arg1)));
		collectionSite.setScode(StringEscapeUtils.unescapeJava(getString(arg0, "scode", arg1)));
		collectionSite.setNsitecode(getShort(arg0, "nsitecode", arg1));
		collectionSite.setNstatus(getShort(arg0, "nstatus", arg1));
		collectionSite.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		collectionSite.setNprojecttypecode(getInteger(arg0, "nprojecttypecode", arg1));
		collectionSite.setSprojecttypename(getString(arg0, "sprojecttypename", arg1));
		collectionSite.setSmodifieddate(getString(arg0, "smodifieddate", arg1));
		collectionSite.setIscollectionsite(getBoolean(arg0, "iscollectionsite", arg1));
		collectionSite.setIscode(getBoolean(arg0, "iscode", arg1));
		return collectionSite;
	}
}
