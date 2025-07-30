package com.agaramtech.qualis.credential.model;
/**
 * This class is used to map fields of 'qualismodule' table of database
*/
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
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

@Entity
@Table(name="qualismodule")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class QualisModule extends CustomizedResultsetRowMapper<QualisModule>  implements Serializable,RowMapper<QualisModule>{
	
	private static final long serialVersionUID = 1L; 
	
	@Id
    @Column(name="nmodulecode") 
	private short nmodulecode;
	
	@Column(name="nmenucode", nullable=false) 
	private short nmenucode;
	
	@Column(name="smodulename",length = 100, nullable=false) 
	private String smodulename;	
	
	@Lob
	@Column(name = "jsondata", columnDefinition = "jsonb")	
	private Map<String,Object> jsondata;
	
	@ColumnDefault("0")
	@Column(name="nsorter", nullable=false) 
	private short nsorter = (short) Enumeration.TransactionStatus.ALL.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name="nstatus", nullable=false) 
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
		
	@Transient
	private transient List<QualisForms> lstforms;
	@Transient
	private transient String sdisplayname;
	@Transient
	private transient String sdefaultname;
	@Transient
	private transient String sformname;
	@Transient
	private transient short nformcode;
	@Transient
	private transient String smenuname;
	@Transient
	private transient String sformdisplayname;
	@Transient
	private transient String smoduledisplayname;



	@Override
	public QualisModule mapRow(ResultSet arg0, int arg1) throws SQLException {
		final QualisModule objQualisModule = new QualisModule();
		objQualisModule.setNmodulecode(getShort(arg0,"nmodulecode",arg1));
		objQualisModule.setNmenucode(getShort(arg0,"nmenucode",arg1));
		objQualisModule.setSmodulename(getString(arg0,"smodulename",arg1));
		objQualisModule.setSdisplayname(getString(arg0,"sdisplayname",arg1));
		objQualisModule.setNsorter(getShort(arg0,"nsorter",arg1));
		objQualisModule.setNstatus(getShort(arg0,"nstatus",arg1));
		objQualisModule.setJsondata(getJsonObject(arg0,"jsondata",arg1));
		objQualisModule.setSdefaultname(getString(arg0,"sdefaultname",arg1));
		objQualisModule.setNformcode(getShort(arg0,"nformcode",arg1));
		objQualisModule.setSformname(getString(arg0,"sformname",arg1));
		objQualisModule.setSmenuname(getString(arg0,"smenuname",arg1));
		objQualisModule.setSformdisplayname(getString(arg0,"sformdisplayname",arg1));
		objQualisModule.setSmoduledisplayname(getString(arg0,"smoduledisplayname",arg1));
		objQualisModule.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));

		return objQualisModule;
	}
}
