package com.agaramtech.qualis.instrumentmanagement.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Map;
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

/**
 * This class is used to map the fields of 'instrumenttype' table of the Database.
 */
@Entity
@Table(name = "instrumenttype")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class InstrumentType extends CustomizedResultsetRowMapper<InstrumentType> implements Serializable,RowMapper<InstrumentType> {

	private static final long serialVersionUID = 1L;
	
	@Id
	@Column(name = "ninstrumenttypecode")
	private int ninstrumenttypecode;

	@Column(name = "sinstrumenttypename", length=50, nullable = false)
	private String sinstrumenttypename;
	
	@Lob
	@Column(name="jsondata",columnDefinition = "jsonb")
	private Map<String,Object> jsondata;
	
	@Column(name = "ndefaultstatus", nullable = false)
	private short ndefaultstatus=(short)Enumeration.TransactionStatus.NO.gettransactionstatus();
	

	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();	
	
	@Transient
	private transient String sinstrumenttype;
	
	
	@Override
	public InstrumentType mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final InstrumentType objInstrumentType = new InstrumentType();
		
		objInstrumentType.setNinstrumenttypecode(getInteger(arg0,"ninstrumenttypecode",arg1));
		objInstrumentType.setSinstrumenttypename(getString(arg0,"sinstrumenttypename",arg1));
		objInstrumentType.setNdefaultstatus(getShort(arg0,"ndefaultstatus",arg1));
		objInstrumentType.setSinstrumenttype(getString(arg0,"sinstrumenttype",arg1));
		objInstrumentType.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objInstrumentType.setNstatus(getShort(arg0,"nstatus",arg1));
		objInstrumentType.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objInstrumentType.setJsondata(unescapeString(getJsonObject(arg0,"jsondata",arg1)));
		
		return objInstrumentType;
	}

}