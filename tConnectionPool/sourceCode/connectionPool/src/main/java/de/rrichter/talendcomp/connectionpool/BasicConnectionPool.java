package de.rrichter.talendcomp.connectionpool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import oracle.ucp.UniversalConnectionPoolAdapter;
import oracle.ucp.UniversalConnectionPoolException;
import oracle.ucp.admin.UniversalConnectionPoolManager;
import oracle.ucp.admin.UniversalConnectionPoolManagerImpl;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import oracle.ucp.jdbc.ValidConnection;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnection;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDataSource;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

import routines.system.TalendDataSource;



public class BasicConnectionPool {

	private String host;
	private String port;
	private String database;
	private String user;
	private String pass;
	private String databaseType = null;
	private String connectionString = null;
	private boolean testOnBorrow = true;
	//private boolean testWhileIdle = true;
	private Integer timeIdleConnectionIsChecked = 30000;
	private Integer timeBetweenChecks = 60000;
	private Integer initialSize = 0;
	private Integer maxTotal = 5;
	//private Integer maxIdle = 5;
	private Integer maxWaitForConnection = 0;
	private Integer numConnectionsPerCheck = 5;
	private String driver = null;
	private Collection<String> initSQL;
	
	private BasicDataSource dataSource = new BasicDataSource();
	private PoolDataSource dataSourceOra = PoolDataSourceFactory.getPoolDataSource();
	private UniversalConnectionPoolManager ucpManager = null;
	private PooledTalendDataSource pooledTalendDateSource = null;
	private String poolName = null;
	
	private String validationQuery = null;
		
	/**
	 * Constructor with necessary params
	 * @param host
	 * @param port
	 * @param database
	 * @param user
	 * @param password
	 * @param databaseType
	 */
	public BasicConnectionPool(String host, String port, String database,
			String user, String password, String databaseType) {
		
		if (host == null || host.isEmpty()) {
			throw new IllegalArgumentException("Host can not be null");
		} else if (port == null || port.isEmpty()) {
			throw new IllegalArgumentException("Port can not be null");
		} else if (database == null || database.isEmpty()) {
			throw new IllegalArgumentException("Database can not be null");
		} else if (user == null || user.isEmpty()) {
			throw new IllegalArgumentException("User can not be null");
		} else if (password == null) {
			throw new IllegalArgumentException(
					"Password can not be null. At least empty String \"\" ");
		} else if (databaseType == null || databaseType.isEmpty()) {
			throw new IllegalArgumentException(
					"databaseType can not be null. Use 'MySQL', 'Oracle', 'DB2', 'Postgres' or 'SQLServer' \"\" ");
		} else {
			
			this.host = host;
			this.port = port;
			this.database = database;
			this.user = user;
			this.pass = password;
			this.databaseType = databaseType.toUpperCase();
			
			switch (this.databaseType ) {
			case "MYSQL": this.validationQuery = "Select 1";
				break;
			case "ORACLE": this.validationQuery = "Select 1 from dual";
				break;
			case "DB2": this.validationQuery = "Select 1 from sysibm.sysdummy1";
				break;
			case "POSTGRES": this.validationQuery = "Select 1";
				break;
			case "SQLSERVER": this.validationQuery = "Select 1";
				break;
			}
		}
	}

	/**
	 * load given driver
	 * @param driver
	 * @throws SQLException
	 */
	public void loadDriver(String driver) throws SQLException{
		
		if(driver == null || driver.isEmpty()){
			throw new IllegalArgumentException("driver can not be null or empty");
		}
		
		try {
			
			Class.forName(driver);
			this.driver = driver;
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
	}
	
	
	/**
	 * setup data source with options set via setters
	 * @throws SQLException
	 * @throws UniversalConnectionPoolException
	 */
	public void setupDataSource() throws SQLException, UniversalConnectionPoolException {

		if(this.driver == null){
			throw new IllegalStateException("Please use method loadDriver befor setup datasource");
		}
		
		if(this.connectionString == null){
			throw new IllegalStateException("Please use method setConnectionString befor setup datasource");
		}
		
		// use org.apache.commons.dbcp2.BasicDataSource
		if (!this.databaseType.equals("ORACLE")) {
			
			this.dataSource.setUsername(this.user);
			this.dataSource.setPassword(this.pass);
			this.dataSource.setUrl(this.connectionString);
			this.dataSource.setTestOnBorrow(this.testOnBorrow);
			this.dataSource.setTestWhileIdle(true);
			
			this.dataSource.setMinEvictableIdleTimeMillis(this.timeIdleConnectionIsChecked);
			this.dataSource.setTimeBetweenEvictionRunsMillis(this.timeBetweenChecks);
			
			this.dataSource.setInitialSize(this.initialSize);
			this.dataSource.setMaxTotal(this.maxTotal);
			//this.dataSource.setMaxIdle(this.maxIdle);
			if (this.maxWaitForConnection == 0){ this.maxWaitForConnection = -1;} 
			this.dataSource.setMaxWaitMillis(this.maxWaitForConnection);
			this.dataSource.setNumTestsPerEvictionRun(this.numConnectionsPerCheck);
			this.dataSource.setValidationQuery(this.validationQuery);
			this.dataSource.setConnectionInitSqls(this.initSQL);
			
		// use oracle.ucp.*
		// due to casting in talend oracle components
		} else if (this.databaseType.equals("ORACLE")) {
			
			this.dataSourceOra.setConnectionFactoryClassName(this.driver);
			this.dataSourceOra.setConnectionPoolName(this.poolName);
			
			this.dataSourceOra.setUser(this.user);
			this.dataSourceOra.setPassword(this.pass);
			this.dataSourceOra.setURL(this.connectionString);
			this.dataSourceOra.setValidateConnectionOnBorrow(this.testOnBorrow);
			
			
			this.dataSourceOra.setInactiveConnectionTimeout(this.timeIdleConnectionIsChecked);
			this.dataSourceOra.setTimeoutCheckInterval(this.timeBetweenChecks);
			
			this.dataSourceOra.setInitialPoolSize(this.initialSize);
			this.dataSourceOra.setMaxPoolSize(this.maxTotal);

			this.dataSourceOra.setConnectionWaitTimeout(this.maxWaitForConnection);
			this.dataSourceOra.setSQLForValidateConnection(this.validationQuery);
			
			this.ucpManager = UniversalConnectionPoolManagerImpl.getUniversalConnectionPoolManager();
			this.ucpManager.createConnectionPool((UniversalConnectionPoolAdapter)this.dataSourceOra);
			this.ucpManager.startConnectionPool(this.poolName);
		}
				
	}

	/**
	 * get data source
	 * instantiate appropriate datasource for mysql or oracle pool
	 * @return Interface TalendDataSource
	 */
	public TalendDataSource getDataSource() {
		
		if (pooledTalendDateSource == null) {
			
			if (!this.databaseType.equals("ORACLE")){
				
				pooledTalendDateSource = new PooledTalendDataSource(dataSource);
				
			}else if (this.databaseType.equals("ORACLE")) {
				
				pooledTalendDateSource = new PooledTalendDataSource(dataSourceOra);
			}
		}
		return pooledTalendDateSource;
	}

	/**
	 * set additional parameter separated by semicolon
	 * @param properties
	 * @throws SQLException
	 */
	public void setAdditionalProperties(String properties) throws SQLException {
		
		if (!this.databaseType.equals("ORACLE")){
			
			this.dataSource.setConnectionProperties(properties.trim());
			
		}else if (this.databaseType.equals("ORACLE")) {
			
			String[] entries = properties.split(";");
	        Properties conProperties = new Properties();
	        
	        for (String entry : entries) {
	        	
	            if (entry.length() > 0) {
	               
	            	int index = entry.indexOf('=');
	               
	            	if (index > 0) {
	                    
	            		String name = entry.substring(0, index);
	                    String value = entry.substring(index + 1);
	                    conProperties.setProperty(name, value);
	              
	            	} else {

	                    conProperties.setProperty(entry, "");
	                }
	            }
	        }
			
			this.dataSourceOra.setConnectionProperties(conProperties);
		}
		
		
	}
	

	/**
	 * close connection pool
	 * @throws SQLException
	 * @throws UniversalConnectionPoolException
	 */
	public void close() throws SQLException, UniversalConnectionPoolException {
		
		if (!this.databaseType.equals("ORACLE")){
					
			this.dataSource.close();
			
		}else if (this.databaseType.equals("ORACLE")) {
			
			this.ucpManager.destroyConnectionPool(this.poolName);
			
		}
		
	}

	
	public String getPoolName() {
		return poolName;
	}

	public void setPoolName(String poolName) {
		this.poolName = poolName;
	}

	public boolean getTestOnBorrow() {
		return testOnBorrow;
	}

	public void setTestOnBorrow(boolean testOnBorrow) {
		this.testOnBorrow = testOnBorrow;
	}

	public Integer getTimeIdleConnectionIsChecked() {
		return timeIdleConnectionIsChecked;
	}

	/**
	 * time an connection can be in idle state before it is checked <br>
	 * required testWhileIdle = true<br>
	 * default = 60000 Milli Sec (MySql), Oracle 60000 (Sec)
	 * 
	 * @param timeIdleConnectionIsChecked
	 */
	public void setTimeIdleConnectionIsChecked(Integer timeIdleConnectionIsChecked) {
		if (timeIdleConnectionIsChecked == null) {
			throw new IllegalArgumentException("timeIdleConnectionIsChecked can not be null");
		} else {
			this.timeIdleConnectionIsChecked = timeIdleConnectionIsChecked;
		}

	}

	public Integer getTimeBetweenChecks() {
		return timeBetweenChecks;
	}

	/**
	 * time between checks for connections in idle state<br>
	 * required testWhileIdle = true<br>
	 * default = 60000 Milli Sec (MySql), Oracle 60000 (Sec)
	 * 
	 * @param timeBetweenChecks
	 */
	public void setTimeBetweenChecks(Integer timeBetweenChecks) {
		if (timeBetweenChecks == null) {
			throw new IllegalArgumentException("timeBetweenChecks can not be null");
		} else {
			this.timeBetweenChecks = timeBetweenChecks;
		}

	}

	public Integer getInitialSize() {
		return initialSize;
	}

	/**
	 * default = 0
	 * 
	 * @param initialSize
	 */
	public void setInitialSize(Integer initialSize) {
		if (initialSize == null) {
			throw new IllegalArgumentException("initialSize can not be null");
		} else {
			this.initialSize = initialSize;
		}

	}

	public Integer getMaxTotal() {
		return maxTotal;
	}

	/**
	 * max number of connections in pool<br>
	 * <br>
	 * default = 5
	 * 
	 * @param maxTotal
	 */
	public void setMaxTotal(Integer maxTotal) {
		if (maxTotal == null) {
			throw new IllegalArgumentException("maxTotal can not be null");
		} else {
			this.maxTotal = maxTotal;
		}

	}

	public Integer getMaxWaitForConnection() {
		return maxWaitForConnection;
	}

	/**
	 * Time to wait for connections if maxTotal size is reached<br>
	 * default = 0 
	 * 
	 * @param maxWaitForConnection
	 */
	public void setMaxWaitForConnection(Integer maxWaitForConnection) {
		if (maxWaitForConnection == null) {
			throw new IllegalArgumentException("maxWaitForConnection can not be null");
		} else {
			this.maxWaitForConnection = maxWaitForConnection;
		}

	}

	public Integer getNumConnectionsPerCheck() {
		return numConnectionsPerCheck;
	}

	/**
	 * number of connections in idle state that are checked <br>
	 * default = 5
	 * 
	 * @param numConnectionsPerCheck
	 */
	public void setNumConnectionsPerCheck(Integer numConnectionsPerCheck) {
		if (numConnectionsPerCheck == null) {
			throw new IllegalArgumentException("numConnectionsPerCheck can not be null");
		} else {
			this.numConnectionsPerCheck = numConnectionsPerCheck;
		}

	}
	
	public Collection<String> getInitSQL() {
		return initSQL;
	}


	/**
	 * set SQL that is fired before a connection become available in pool
	 * separeted by semicolon
	 * @param initSQL
	 */
	public void setInitSQL(String initSQL) {
		if (initSQL == null || initSQL.equals("")) {
			
			throw new IllegalArgumentException("initSQL can not be null");
			
		} else {
			
			String[] splitted = initSQL.split(";");
			
			for (String sql : splitted) {
				this.initSQL.add(sql);
			}
			
		}

	}
	

	public void setConnectionString(String connectionString) {
		if (connectionString == null || connectionString.isEmpty()) {
			throw new IllegalArgumentException("numConnectionsPerCheck can not be null");
		} else {
			this.connectionString = connectionString;
		}

	}
	

}
