package com.agaramtech.qualis.configuration.model;

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

/**
 * This class is used to map the fields of 'filterdetail' table of the Database.
 */
@Entity
@Table(name = "filterdetail")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class FilterDetail extends CustomizedResultsetRowMapper<FilterDetail> implements Serializable,RowMapper<FilterDetail>{

		private static final long serialVersionUID = 1L;
		
		@Id
		@Column(name = "nfilterdetailcode")
		private int nfilterdetailcode;

		@Column(name = "nformcode", nullable = false)
		private short nformcode;

		@Column(name = "nusercode", nullable = false)
		private int nusercode;

		@Column(name = "nuserrolecode", nullable = false)
		private int nuserrolecode;
		
//		ALPD-6044	Added nregtypecode and nregsubtypecode column by Vishakh (03-07-2025)
		@Column(name = "nregtypecode", nullable = false)
		@ColumnDefault("-1")
		private int nregtypecode = (int) Enumeration.TransactionStatus.NA.gettransactionstatus();
		
		@Column(name = "nregsubtypecode", nullable = false)
		@ColumnDefault("-1")
		private int nregsubtypecode = (int) Enumeration.TransactionStatus.NA.gettransactionstatus();
	 
		@Column(name = "dmodifieddate")
		private Instant dmodifieddate;
		
		@Lob
		@Column(name = "jsondata", columnDefinition = "jsonb")
		private Map<String, Object> jsondata;
		
		@Lob
		@Column(name = "jsontempdata", columnDefinition = "jsonb")
		private Map<String, Object> jsontempdata;

		
		@Column(name = "nsitecode", nullable = false)
		@ColumnDefault("-1")
		private short nsitecode = (short)Enumeration.TransactionStatus.NA.gettransactionstatus();

		@ColumnDefault("1")
		@Column(name = "nstatus")
		private short nstatus = (short)Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
			

		@Override
		public FilterDetail mapRow(ResultSet arg0, int arg1) throws SQLException {
			// TODO Auto-generated method stub
			final FilterDetail objFilterDetail = new FilterDetail();
			
			objFilterDetail.setNfilterdetailcode(getInteger(arg0,"nfilterdetailcode",arg1));
			objFilterDetail.setNformcode(getShort(arg0,"nformcode",arg1));
			objFilterDetail.setNusercode(getInteger(arg0,"nusercode",arg1));
			objFilterDetail.setNuserrolecode(getInteger(arg0,"nuserrolecode",arg1));
			objFilterDetail.setNregtypecode(getInteger(arg0,"nregtypecode",arg1));
			objFilterDetail.setNregsubtypecode(getInteger(arg0,"nregsubtypecode",arg1));
			objFilterDetail.setDmodifieddate(getInstant(arg0,"dmodifieddate",arg1));
			objFilterDetail.setJsondata(unescapeString(getJsonObject(arg0,"jsondata",arg1)));
			objFilterDetail.setJsontempdata(unescapeString(getJsonObject(arg0,"jsontempdata",arg1)));
		    objFilterDetail.setNsitecode(getShort(arg0,"nsitecode",arg1));
		    objFilterDetail.setNstatus(getShort(arg0,"nstatus",arg1));
			
			return objFilterDetail;
		}
}
