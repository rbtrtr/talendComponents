package de.rrichter.talendcomp.connectionpool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Random;

import oracle.ucp.jdbc.PoolDataSource;

import org.apache.commons.dbcp2.BasicDataSource;

import routines.system.TalendDataSource;

public class PooledTalendDataSource extends TalendDataSource{

	private BasicDataSource ds = null;
	private PoolDataSource dsOra = null;
	
	public PooledTalendDataSource(BasicDataSource ds) {
		super(ds);
		this.ds = ds;
	}
	
	public PooledTalendDataSource(PoolDataSource ds) {
		super(ds);
		this.dsOra = ds;
	}
	
	/**
	 * getConnection
	 * 
	 * @return
	 * @throws SQLException
	 */
	@Override
	public Connection getConnection() throws SQLException {
		
		if (this.ds != null) {
		
			return this.ds.getConnection();
			
		} else if (this.dsOra != null) {

			return this.dsOra.getConnection();
			
		} else {
		
			return null;
		}
	}
	
	@Override
	public void close() throws SQLException {
		
		this.ds.close();
	}
	
}