package com.agaramtech.qualis.basemaster.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
import org.apache.commons.text.StringEscapeUtils;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;
import com.agaramtech.qualis.global.CustomizedResultsetRowMapper;
import com.agaramtech.qualis.global.Enumeration;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor 
@AllArgsConstructor 
@EqualsAndHashCode(callSuper = false)  
@Entity
@Table(name = "bulkbarcodeconfigdetails")
@Data
public class BulkBarcodeConfigDetails extends CustomizedResultsetRowMapper<BulkBarcodeConfigDetails>
		implements Serializable, RowMapper<BulkBarcodeConfigDetails> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "nbulkbarcodeconfigdetailscode ")
	private int nbulkbarcodeconfigdetailscode;
	@Column(name = "nbulkbarcodeconfigcode ")
	private int nbulkbarcodeconfigcode;
	@Column(name = "nprojecttypecode ")
	private int nprojecttypecode;
	@Column(name = "nneedmaster ")
	private short nneedmaster;
	@Column(name = "nqueryneed ")
	private int nqueryneed;
	@Column(name = "nquerybuildertablecode ")
	private short nquerybuildertablecode;
	@Column(name = "stablename", length = 50, nullable = false)
	private String stablename;
	@Column(name = "stablecolumnname", length = 50, nullable = false)
	private String stablecolumnname;
	@Column(name = "nfieldstartposition ")
	private short nfieldstartposition;
	@Column(name = "nfieldlength ")
	private short nfieldlength;
	@Column(name = "nsorter ")
	private short nsorter;
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")
	private Map<String, Object> jsondata;
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode= (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus= (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	@Column(name = "squery", length = 1000, nullable = false)
	private String squery;
	@Column(name = "sdescription", length = 255)
	private String sdescription= "";

	@Transient
	private transient String sdisplayname;
	@Transient
	private transient String sformname;
	@Transient
	private transient String sparentformname;
	@Transient
	private transient int nparentfieldlength;
	@Transient
	private transient String sfieldname;
	@Transient
	private transient int isneedparent;
	@Transient
	private transient int nbarcodemastercode;
	@Transient
	private transient int nparentnbarcodemastercode;
	@Transient
	private transient int isvalidationrequired;

	@Override
	public BulkBarcodeConfigDetails mapRow(ResultSet arg0, int arg1) throws SQLException {

		final BulkBarcodeConfigDetails objMapper = new BulkBarcodeConfigDetails();
		objMapper.setNbulkbarcodeconfigdetailscode(getInteger(arg0, "nbulkbarcodeconfigdetailscode", arg1));
		objMapper.setNprojecttypecode(getShort(arg0, "nprojecttypecode", arg1));
		objMapper.setNneedmaster(getShort(arg0, "nneedmaster", arg1));
		objMapper.setNquerybuildertablecode(getShort(arg0, "nquerybuildertablecode", arg1));
		objMapper.setStablename(StringEscapeUtils.unescapeJava(getString(arg0, "stablename", arg1)));
		objMapper.setStablecolumnname(StringEscapeUtils.unescapeJava(getString(arg0, "stablecolumnname", arg1)));
		objMapper.setNfieldstartposition(getShort(arg0, "nfieldstartposition", arg1));
		objMapper.setNfieldlength(getShort(arg0, "nfieldlength", arg1));
		objMapper.setNsorter(getShort(arg0, "nsorter", arg1));
		objMapper.setNsitecode(getShort(arg0, "nsitecode", arg1));
		objMapper.setNstatus(getShort(arg0, "nstatus", arg1));
		objMapper.setDmodifieddate(getInstant(arg0, "dmodifieddate", arg1));
		objMapper.setJsondata(unescapeString(getJsonObject(arg0, "jsondata", arg1)));
		objMapper.setNqueryneed(getInteger(arg0, "nqueryneed", arg1));
		objMapper.setSquery(StringEscapeUtils.unescapeJava(getString(arg0, "squery", arg1)));
		objMapper.setNbulkbarcodeconfigcode(getInteger(arg0, "nbulkbarcodeconfigcode", arg1));
		objMapper.setSdescription(StringEscapeUtils.unescapeJava(getString(arg0, "sdescription", arg1)));
		objMapper.setSdisplayname(getString(arg0, "sdisplayname", arg1));
		objMapper.setSparentformname(getString(arg0, "sparentformname", arg1));
		objMapper.setSformname(getString(arg0, "sformname", arg1));
		objMapper.setSfieldname(getString(arg0, "sfieldname", arg1));
		objMapper.setNparentfieldlength(getInteger(arg0, "nparentfieldlength", arg1));
		objMapper.setIsneedparent(getInteger(arg0, "isneedparent", arg1));
		objMapper.setNbarcodemastercode(getInteger(arg0, "nbarcodemastercode", arg1));
		objMapper.setNparentnbarcodemastercode(getInteger(arg0, "nparentnbarcodemastercode", arg1));
		objMapper.setIsvalidationrequired(getInteger(arg0, "isvalidationrequired", arg1));	//ALPD-5082 Added transient field for isvalidationrequired by VISHAKH
		return objMapper;
	}
}
