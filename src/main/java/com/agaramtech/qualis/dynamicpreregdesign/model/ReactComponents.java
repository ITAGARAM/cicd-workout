package com.agaramtech.qualis.dynamicpreregdesign.model;

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

@Entity
@Data
@Table(name = "reactcomponents")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ReactComponents extends CustomizedResultsetRowMapper<ReactComponents> implements Serializable,RowMapper<ReactComponents> {
	
	private static final long serialVersionUID = 1L;
	
	@Id @Column(name="nreactcomponentcode") 
	private short nreactcomponentcode;
	
	@Lob@Column(name="jsondata",columnDefinition = "jsonb")
	private Map<String,Object> jsondata;
	
	@Column(name="nstatus")
	private short nstatus;
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable=false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Transient
	private transient String componentname;
	@Transient
	private transient String sdisplayname;
	@Transient
	private transient String sdefaultname;
	

	@Override
	public ReactComponents mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final ReactComponents objReactComponents = new ReactComponents();
		
		objReactComponents.setNreactcomponentcode(getShort(arg0,"nreactcomponentcode",arg1));
		objReactComponents.setJsondata(unescapeString(getJsonObject(arg0,"jsondata",arg1)));
		objReactComponents.setNstatus(getShort(arg0,"nstatus",arg1));
		objReactComponents.setComponentname(getString(arg0,"componentname",arg1));
		objReactComponents.setSdisplayname(getString(arg0,"sdisplayname",arg1));
		objReactComponents.setSdefaultname(getString(arg0,"sdefaultname",arg1));
		objReactComponents.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objReactComponents.setNsitecode(getShort(arg0,"nsitecode",arg1));
		
		return objReactComponents;
	}

}
