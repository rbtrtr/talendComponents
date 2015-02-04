# tConnectionPool – Documentation

## General
- Component allows you to create a connection pool inside a Talend DI Job
- Supported databases: 
	- MySQL
	- Oracle 
- Two operation modes: 
	- Create Connection Pool
	- Close Connection Pool
	- Uses the “specify a data source alias” feature in other database components
- Can also be used over different jobs, if they are combined via tRunJob

## Basic Settings
| Item | Description |
|------|---------------|
|Database Version|- MySQL,Oracle <br> - Supported Oracle Version: 11-6,11,10 <br>- Oracle Connection Types: SID, ServiceName|
|Host|“127.0.0.1”|
|Port|“3306”|
|Database|“myDatabase”|
|User|“databaseUser”|
|Password|“password”|
|Alias|Enter a job wide unique name. You have to use this alias in every other database component <br> e.g. “MyConnectionPool”|


	 
## Advanced Settings
- Please note: All time declaration for 
	- MySQL in milliseconds
	- Oracle in seconds

| Item | Description |
|------|---------------|
|Additional JDBC Parameter|Specify additional properties <br> property_1=value1;property_2=value_2; …|
|Test On Borrow|If checked, every connection is validated before it is used|
|Time Between Checks|Idled connections are checked during a validate run. Here you set the time between that runs.|
|Max Idle Time|Maximum time an connection can stay in idle mode, before it is removed from the pool.|
|Number Checked Connections|Only available for MySQL. Defines the number of connections that are checked during a validation run.|
|Set Initial Size|Initial amount of available connections.|
|Set Max Connection Size|Maximum amount of connections those are available within the pool.|
|Set Max Time To Wait For Connection|Maximum time a connection request can wait if all connections are in use.|
|Set Initial SQL	Only available for MySQL.|Sets a list of SQL statements to be executed when a connection is first created. Separated via semicolon|
	

## Usage

[Usage Documentation][]

[Usage Documentation]: https://github.com/robertrichter/talendComponents/blob/master/tConnectionPool/Usage_tConnectionPool.pdf
 
