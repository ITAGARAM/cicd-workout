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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Entity
@Table(name="reactinputfields")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ReactInputFields extends CustomizedResultsetRowMapper<ReactInputFields> implements Serializable,RowMapper<ReactInputFields>{

	
	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="nreactinputfieldcode")
	private int nreactinputfieldcode;
	
	@Lob@Column(name="jsondata",columnDefinition = "jsonb")
	private Map<String,Object> jsondata;
	
	@Column(name="nstatus")
	private short nstatus=(short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable=false)
	private short nsitecode=(short)Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@Column(name = "dmodifieddate", nullable = false)
	private Instant dmodifieddate;
	

	@Override
	public ReactInputFields mapRow(ResultSet arg0, int arg1) throws SQLException {
		
		final ReactInputFields objReactInputFields = new ReactInputFields();
		
		objReactInputFields.setNreactinputfieldcode(getInteger(arg0,"nreactinputfieldcode",arg1));
		objReactInputFields.setJsondata(unescapeString(getJsonObject(arg0,"jsondata",arg1)));
		objReactInputFields.setNstatus(getShort(arg0,"nstatus",arg1));
		objReactInputFields.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		objReactInputFields.setNsitecode(getShort(arg0,"nsitecode",arg1));
		
		return objReactInputFields;
	}

}
