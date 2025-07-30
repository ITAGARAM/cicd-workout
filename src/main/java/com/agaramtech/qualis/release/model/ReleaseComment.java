

package com.agaramtech.qualis.release.model;

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
@Table(name = "releasecomment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class ReleaseComment extends CustomizedResultsetRowMapper<ReleaseComment> implements Serializable, RowMapper<ReleaseComment> {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name="nreleasecommentcode") 
	private int nreleasecommentcode;
	
	@Column(name="ncoaparentcode", nullable = false) 
	private int ncoaparentcode;
	
	@Column(name="ndeputyusercode", nullable = false) 
	private int ndeputyusercode;
	
	@Column(name="ndeputyuserrolecode", nullable = false) 
	private int ndeputyuserrolecode;
	
	@Column(name="nuserrolecode", nullable = false) 
	private int nuserrolecode;
	
	@Column(name="nusercode", nullable = false) 
	private int nusercode;
	
	@Column(name = "sreleasecomments",  length = 3000, nullable = false)
	private String sreleasecomments;
	
	@Column(name = "dtransactiondate", nullable = false)
	private Instant dtransactiondate;
	
	@ColumnDefault("0")
	@Column(name = "noffsetdtransactiondate", nullable = false) 
	private int noffsetdtransactiondate = Enumeration.TransactionStatus.NON_EMPTY.gettransactionstatus();
	
	@ColumnDefault("-1")
	@Column(name = "ntransdatetimezonecode", nullable = false ) 
	private short ntransdatetimezonecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
		
	@Column(name = "nsitecode", nullable = false)
	@ColumnDefault("-1")
	private short nsitecode = (short) Enumeration.TransactionStatus.NA.gettransactionstatus();
	
	@ColumnDefault("1")
	@Column(name ="nstatus", nullable=false)
	private short nstatus = (short) Enumeration.TransactionStatus.ACTIVE.gettransactionstatus();
	
	
	@Transient
	private transient String susername;
	@Transient
	private transient String suserrolename;
	@Transient
	private transient String sreportno;

			
	@Override
	public ReleaseComment mapRow(ResultSet arg0, int arg1) throws SQLException {

		ReleaseComment objReleaseComment = new ReleaseComment();

		objReleaseComment.setNreleasecommentcode(getInteger(arg0,"nreleasetestcommentcode",arg1));
		objReleaseComment.setNcoaparentcode(getInteger(arg0,"ncoaparentcode",arg1));
		objReleaseComment.setNdeputyusercode(getInteger(arg0,"ndeputyusercode",arg1));
		objReleaseComment.setNdeputyuserrolecode(getInteger(arg0,"ndeputyuserrolecode",arg1));
		objReleaseComment.setNusercode(getInteger(arg0,"nusercode",arg1));
		objReleaseComment.setNuserrolecode(getInteger(arg0,"nuserrolecode",arg1));
		objReleaseComment.setDtransactiondate(getInstant(arg0,"dtransactiondate",arg1));
		objReleaseComment.setNtransdatetimezonecode(getShort(arg0,"ntransdatetimezonecode",arg1));
		objReleaseComment.setNoffsetdtransactiondate(getInteger(arg0,"noffsetdtransactiondate",arg1));
		objReleaseComment.setNsitecode(getShort(arg0,"nsitecode",arg1));
		objReleaseComment.setNstatus(getShort(arg0,"nstatus",arg1));
		objReleaseComment.setSreleasecomments(StringEscapeUtils.unescapeJava(getString(arg0,"sreleasecomments",arg1)));
		objReleaseComment.setSusername(getString(arg0,"susername",arg1));
		objReleaseComment.setSuserrolename(getString(arg0,"suserrolename",arg1));
		objReleaseComment.setSreportno(getString(arg0,"sreportno",arg1));
		return objReleaseComment;
	}

}
