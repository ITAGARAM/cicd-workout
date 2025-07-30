package com.agaramtech.qualis.global;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

import org.hibernate.annotations.ColumnDefault;
import org.springframework.jdbc.core.RowMapper;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


/**
 * This class is used to map the fields of 'dynamicstatusvalidation' table of the Database.
 * 
 * @author AT-E236
 * @version 9.0.0.1
 * @since 01- March- 2024
 */
@Entity
@Table(name = "dynamicstatusvalidation")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DynamicStatusValidation extends CustomizedResultsetRowMapper implements Serializable,RowMapper<DynamicStatusValidation> {

	private static final long serialVersionUID = 1L;
	@Id
	@Column(name = "nvalidationcode")
	private int nvalidationcode;

	@Column(name = "nformcode")
	private int nformcode;

	@Column(name = "ncontrolcode")
	private int ncontrolcode;
	
	@Column(name = "ntranscode")
	private int ntranscode;
	
	@Column(name = "dmodifieddate")
	private Instant dmodifieddate;
	
	@ColumnDefault("-1")
	@Column(name = "nsitecode", nullable = false)
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();

	@ColumnDefault("1")
	@Column(name = "nstatus", nullable = false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();


	
	@Override
	public DynamicStatusValidation mapRow(ResultSet arg0, int arg1)
			throws SQLException {
		final DynamicStatusValidation objDynamicStatusValidation = new DynamicStatusValidation();
		objDynamicStatusValidation.setNvalidationcode(getInteger(arg0,"nvalidationcode",arg1));
		objDynamicStatusValidation.setNformcode(getInteger(arg0,"nformcode",arg1));
		objDynamicStatusValidation.setNcontrolcode(getInteger(arg0,"ncontrolcode",arg1));
		objDynamicStatusValidation.setNtranscode(getShort(arg0,"ntranscode",arg1));
		objDynamicStatusValidation.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objDynamicStatusValidation.setNstatus(getShort(arg0,"nstatus",arg1));
		objDynamicStatusValidation.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
		

		return objDynamicStatusValidation;
	}
	
}