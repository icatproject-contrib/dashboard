import sys
import datetime
import argparse

from ConfigParser import ConfigParser

hasLogTable = False

#Parse the configuration file
def parseOracleConfig():
	print("Parsing the configuration file")
	config= ConfigParser()
	
	configuration = {}	

	try:
		config.read("icat_transfer.ini")
		
	except Exception as e:
		print(e)
	
	configuration['icatUsername'] = config.get('ICAT','oracle.username')
	configuration['icatPassword'] = config.get('ICAT','oracle.password')
	configuration['icatDb'] = config.get('ICAT','oracle.url')

	configuration['dashboardUsername'] = config.get('Dashboard','oracle.username')
	configuration['dashboardPassword'] = config.get('Dashboard','oracle.password')
	configuration['dashboardDb'] = config.get('Dashboard','oracle.url')
	
	configuration['rootUserName'] = config.get('ICAT','rootUserName')
		
	print("Successfully parsed the configuration file")
	
	return configuration


#Parse the MySql configuration file
def parseMySqlConfig():
	print("Parsing the configuration file")
	config= ConfigParser()
	
	configuration = {}	

	try:
		config.read("icat_transfer.ini")
		
	except Exception as e:
		print(e)
	
	configuration['icatUsername'] = config.get('ICAT','mySql.username')
	configuration['icatPassword'] = config.get('ICAT','mySql.password')
	configuration['icatDb'] = config.get('ICAT','mySql.db')
	configuration['icatHost'] = config.get('ICAT','mySql.host')
	configuration['icatPort'] = config.get('ICAT','mySql.port')

	configuration['dashboardUsername'] = config.get('Dashboard','mySql.username')
	configuration['dashboardPassword'] = config.get('Dashboard','mySql.password')
	configuration['dashboardDb'] = config.get('Dashboard','mySql.db')
	configuration['dashboardHost'] = config.get('Dashboard','mySql.host')
	configuration['dashboardPort'] = config.get('Dashboard','mySql.port')
	
	configuration['rootUserName'] = config.get('ICAT','rootUserName')
	print("Successfully parsed the configuration file")
	
	return configuration


#Connect to a oracle database
def connectToOracle(configuration,database):
	print("Connecting to the "+database+ " database")

	try:
		connectionString = configuration[database+'Username']+"/"+configuration[database+'Password']+'@'+configuration[database+'Db']
		con =  cx_Oracle.connect(connectionString)

	except Exception as e:
		print("Issue connecting with the oracle database: ", e)
		sys.exit(1)

	print("Successfully connected to the Oracle database")
	return con 

#Connect to a MySql database
def connectToMySql(configuration,database):
	print("Connecting to the "+database+ " database")

	try:		
		connection = pymysql.connect(host=configuration[database+'Host'],port=int(configuration[database+'Port']),user=configuration[database+'Username'],password=configuration[database+'Password'],db=configuration[database+'Db'])	
	except Exception as e:
		print("Issue connecting with the MySql database: ", e)
		sys.exit(1)

	print("Successfully connected to the mySql database")
	return connection 

#Retrieve the Ids of the users in the Dashboard
def getDashboardUserIds(dashboardCon):
	userIdQuery="SELECT name,id FROM ICATUSER"

	ids = {}

	cursId = dashboardCon.cursor()

	cursId.execute(userIdQuery)

	for result in cursId:
                ids[result[0]] = result[1]
	
	
	cursId.close()

	return ids
	
#Import logs to the Dashboard from the ICAT.
def exportLogs(icatCon, dashboardCon,database):
	print("Exporting the logs from the ICAT")
	        
	if(database=="mySql"):
		importQuery = "INSERT INTO ICATLOG (QUERY,OPERATION,ENTITYID,ENTITYTYPE,LOGTIME,DURATION, CREATE_TIME,MOD_TIME,USER_ID) VALUES(%s,%s,%s,%s,%s,%s,%s,%s,%s)"
	elif(database=="oracle"):
		importQuery = "INSERT INTO ICATLOG (QUERY,OPERATION,ENTITYID,ENTITYTYPE,LOGTIME,DURATION, CREATE_TIME,MOD_TIME,USER_ID,ID) VALUES(:0,:1,:2,:3,:4,:5,:6,:7,:8,ID_SEQ.NEXTVAL)"

	searchQuery= "SELECT LOG.CREATE_ID as user_name, query, operation, entityId, entityName, MOD_TIME AS LOGTIME,duration FROM LOG"
	
	logCount= getEntityAmount("LOG",icatCon)
	
	#Create import and export curs
        cursExport = icatCon.cursor()
        cursImport = dashboardCon.cursor()
	
	cursExport.execute(searchQuery)	

	print str(logCount)+ " Log entries to Import"
			
	result =[]	
	counter = 0
	loopCounter = 0
	
	userIds = getDashboardUserIds(dashboardCon)	

	
	now = datetime.datetime.now()

	for row_data in cursExport:
		
		print str(counter)+ " Inserted" 

		userId = userIds.get(row_data[0])
		
		row_data = row_data[1:]+ (now,now,userId)
			
		sys.stdout.write("\033[F") #back to previous line
		sys.stdout.write("\033[K") #clear line

		if(userId!=None):		
			result.append(row_data)

		if(loopCounter == 100000):
			importMany(result,cursImport,importQuery)
			loopCounter = 0
			result = []						
		counter +=1
		loopCounter+=1	

	#Import any remaining files 	
	importMany(result,cursImport,importQuery)
	
	
	#Closes connections
	cursImport.close()
	cursExport.close()		

	
#Imports multiple rows of logs into the Dashboard.	
def importMany(data,cursImport,query):
	
	try:
		cursImport.executemany(query,data)
			
	except Exception as e:
		print(e)
		sys.exit(1)
	
#Gathers users from the ICAT and then inserts them into the dashboard.	
def exportUsers(icatCon,dashboardCon, database,rootName):
	print("Exporting Users from the ICAT")

	if(database=="mySql"):
		importQuery = "INSERT INTO ICATUSER (FULLNAME,USER_ICAT_ID,NAME,CREATE_TIME,MOD_TIME) values (%s,%s,%s,%s,%s)"
	elif(database=="oracle"):
		importQuery = "INSERT INTO ICATUser (FULLNAME,USER_ICAT_ID,NAME,CREATE_TIME,MOD_TIME,ID) values (:0,:1,:2,:3,:4,ID_SEQ.NEXTVAL)"

	
	cursExport = icatCon.cursor()
	cursImport = dashboardCon.cursor()

	userCount = getEntityAmount("USER_",icatCon)

	print "Importing: "+str(userCount)+" users"
	print "Current Amount Inserted: "

	userQuery= "SELECT FULLNAME, ID as userICATID, name  FROM USER_"

	cursExport.execute(userQuery)

	now = datetime.datetime.now()

	counter = 0
	#First insert the root user into the Dashboard
	importSingle(cursImport,(rootName,99999999,rootName,now,now),importQuery)

    	for row_data in cursExport:
		counter+=1
		importSingle(cursImport,row_data+(now,now),importQuery)
		print(counter)
		sys.stdout.write("\033[F") #back to previous line
		sys.stdout.write("\033[K") #clear line

	#Commit and close connections commit required so the program can get the user ids to associated with a log	
	dashboardCon.commit()
	cursImport.close()
	cursExport.close()


#Imports multiple rows into the user table.
def importSingle(cursImport,data,query):
	try:
		cursImport.execute(query,data)
	except Exception as e:
		print("Entity has already been inserted by the dashboard. Carrying on...")
		
	

#Retrieves the amount of entities from the entity and the provided connection
def getEntityAmount(entity, connection):
	cursCount = connection.cursor()
	cursCount.execute("SELECT COUNT(*) FROM "+entity)

	for number in cursCount:
		return number[0]
	
#Retrieves a list of entities and how many were created on certain dates.
def exportEntityCount(cursor,entity,database):
	result = []

        if entity in ("LOG","log"):
                hasLogTable = True
	
	if(database=="mySql"):
		query ="SELECT COUNT("+entity+".id), DATE_FORMAT("+entity+".CREATE_TIME, '%Y,%m,%d') FROM "+entity+" WHERE "+entity+".CREATE_TIME < CURDATE()  GROUP BY DATE_FORMAT("+entity+".CREATE_TIME, '%Y,%m,%d');"
	elif(database=="oracle"):
		query="SELECT COUNT("+entity+".id), to_char("+entity+".CREATE_TIME,'yyyy,mm,dd') FROM "+entity+" WHERE "+entity+".CREATE_TIME < trunc(sysdate) GROUP BY to_char("+entity+".CREATE_TIME,'yyyy,mm,dd')"
	
	cursor.execute(query)

	now = datetime.datetime.now()

	for row_data in cursor:
		dateValues = row_data[1].split(",")
		row = (entity,row_data[0],datetime.datetime(int(dateValues[0]),int(dateValues[1]),int(dateValues[2]),00,00,00),now,now)
		result.append(row)

	return result


#Main method to collect entities and how many where created each day.
def importEntitiesCount(dashboardCon, icatCon, database,configuration):
	
	print("Initiating collection of entity count")
	
	if(database=="mySql"):
		importQuery = "INSERT INTO ENTITYCOUNT(ENTITYTYPE,ENTITYCOUNT,COUNTDATE, CREATE_TIME,MOD_TIME ) VALUES(%s,%s,%s,%s,%s) "
		entityTypeQuery = "select table_name from information_schema.tables where table_schema='"+configuration["icatDb"]+"' AND table_name!='SESSION_' AND table_name!='SEQUENCE'" 
	elif(database=="oracle"):
		importQuery = "INSERT INTO ENTITYCOUNT(ENTITYTYPE,ENTITYCOUNT,COUNTDATE,CREATE_TIME,MOD_TIME,ID) VALUES(:0,:1,:2,:3,:4,ID_SEQ.NEXTVAL)"
		entityTypeQuery = "select table_name from user_tables where table_name!='SESSION_' AND table_name!='SEQUENCE'"
	
	cursEntities = icatCon.cursor()
	
	cursEntityCounter = icatCon.cursor()
	
	cursEntities.execute(entityTypeQuery)

        cursImport = dashboardCon.cursor()
        	
	for entity in cursEntities:
		result = exportEntityCount(cursEntityCounter,entity[0],database)	
		importMany(result,cursImport,importQuery)
	
	print("Completed collection of entity count")

#Collects the instrument meta data from the ICAT.
def exportInstrumentMeta(icatCon,database,dashboardCon):

	if(database=="mySql"):
                searchQuery = "SELECT INSTRUMENT.ID, COUNT(DATAFILE.ID), SUM(DATAFILE.filesize),  DATE_FORMAT(DATAFILE.CREATE_TIME, '%Y,%m,%d') FROM DATAFILE JOIN DATASET ON DATASET.ID = DATAFILE.DATASET_ID JOIN INVESTIGATION ON INVESTIGATION.ID = DATASET.INVESTIGATION_ID  JOIN INVESTIGATIONINSTRUMENT ON INVESTIGATIONINSTRUMENT.INVESTIGATION_ID = INVESTIGATION.ID JOIN INSTRUMENT ON INSTRUMENT.ID = INVESTIGATIONINSTRUMENT.INSTRUMENT_ID WHERE DATAFILE.CREATE_TIME < CURDATE() GROUP BY  DATE_FORMAT(DATAFILE.CREATE_TIME, '%Y,%m,%d'), INSTRUMENT.ID" 
	elif(database=="oracle"):
		searchQuery = "SELECT INSTRUMENT.ID, COUNT (datafile.id), SUM(datafile.filesize), to_char(Datafile.CREATE_TIME,'yyyy,mm,dd') FROM Datafile JOIN Dataset ON Dataset.ID = Datafile.DATASET_ID JOIN Investigation ON Investigation.ID = Dataset.INVESTIGATION_ID JOIN Investigationinstrument ON Investigationinstrument.INVESTIGATION_ID = Investigation.ID JOIN Instrument ON Instrument.ID = Investigationinstrument.INSTRUMENT_ID WHERE DATAFILE.CREATE_TIME < trunc(sysdate) GROUP BY to_char(Datafile.CREATE_TIME,'yyyy,mm,dd'), Instrument.ID"

	try:
		cursInstrument = icatCon.cursor()

		cursInstrument.execute(searchQuery)
	
		result = []
	
		now = datetime.datetime.now()		
		for row_data in cursInstrument:
       			dateValues = row_data[3].split(",")
                	row = (row_data[0],row_data[1],row_data[2],datetime.datetime(int(dateValues[0]),int(dateValues[1]),int(dateValues[2]),00,00,00),now,now)
			result.append(row)
		
	except Exception as e:
		print "An error has occured exporting instrument data " , e
		sys.exit(1)
	
	finally:
		cursInstrument.close()		
	
	return result

	
#Main method that imports the instrument meta data into the dashboard	
def importInstrumentMeta(dashboardCon,icatCon,database):
	print"Starting to gather meta data on instruments"

	if(database=="mySql"):
		importQuery = "INSERT INTO INSTRUMENTMETADATA(INSTRUMENTID,DATAFILECOUNT,DATAFILEVOLUME, COLLECTIONDATE, CREATE_TIME,MOD_TIME ) VALUES(%s,%s,%s,%s,%s,%s)"
	elif(database=="oracle"):
		importQuery = "INSERT INTO INSTRUMENTMETADATA(INSTRUMENTID,DATAFILECOUNT,DATAFILEVOLUME, COLLECTIONDATE, CREATE_TIME,MOD_TIME,ID ) VALUES(:0,:1,:2,:3,:4,:5,ID_SEQ.NEXTVAL)"
	
	try:
		cursInstrument = dashboardCon.cursor()
	
		data = exportInstrumentMeta(icatCon,database,dashboardCon)
		cursInstrument.executemany(importQuery,data)
		
	except Exception as e:
		print "Issue with gather statistics on the Instrument data" , e
		sys.exit(1)
	finally:
		cursInstrument.close()	

	
	print "Importing of instrument meta data complete"

#Collects the investigation meta data from the ICAT.
def exportInvestigationMeta(icatCon,database):

	if(database=="mySql"):
        	searchQuery="SELECT INVESTIGATION.ID, COUNT(DATAFILE.id), SUM(DATAFILE.filesize),  DATE_FORMAT(DATAFILE.CREATE_TIME, '%Y,%m,%d') FROM DATAFILE JOIN DATASET ON DATASET.ID = DATAFILE.DATASET_ID JOIN INVESTIGATION ON INVESTIGATION.ID = DATASET.INVESTIGATION_ID WHERE DATAFILE.CREATE_TIME<CURDATE() GROUP BY DATE_FORMAT(DATAFILE.CREATE_TIME, '%Y,%m,%d'), INVESTIGATION.ID"
	elif(database=="oracle"):
		searchQuery = "SELECT INVESTIGATION.ID, COUNT (datafile.id), SUM(datafile.filesize), to_char(Datafile.CREATE_TIME,'yyyy,mm,dd') FROM Datafile JOIN Dataset ON Dataset.ID = Datafile.DATASET_ID JOIN Investigation ON Investigation.ID = Dataset.INVESTIGATION_ID WHERE DATAFILE.CREATE_TIME<trunc(sysdate) GROUP BY to_char(Datafile.CREATE_TIME,'yyyy,mm,dd'), INVESTIGATION.ID"
	try:
		cursInvestigation = icatCon.cursor()

		cursInvestigation.execute(searchQuery)

		result = []
	
		now = datetime.datetime.now()
	
		for row_data in cursInvestigation:
			dateValues = row_data[3].split(",")
                	row = (row_data[0],row_data[1],row_data[2],datetime.datetime(int(dateValues[0]),int(dateValues[1]),int(dateValues[2]),00,00,00),now,now)
                	result.append(row)
	
	except Exception as e:
		print "Issue with collecting investigation meta data", e
		sys.exit(1)

	finally:
		cursInvestigation.close()

	return result

#Inserts the investigation meta data into the dashboard.
def importInvestigationMeta(dashboardCon,icatCon,database):
	print "Starting to gather meta data on investigations"
	
	if(database=="mySql"):
		importQuery = "INSERT INTO INVESTIGATIONMETADATA(INVESTIGATIONID, DATAFILECOUNT, DATAFILEVOLUME, COLLECTIONDATE, CREATE_TIME,MOD_TIME) VALUES(%s,%s,%s,%s,%s,%s) "
	elif(database=="oracle"):
		importQuery= "INSERT INTO INVESTIGATIONMETADATA(INVESTIGATIONID,DATAFILECOUNT,DATAFILEVOLUME,COLLECTIONDATE,CREATE_TIME,MOD_TIME, ID) VALUES(:0,:1,:2,:3,:4,:5,ID_SEQ.NEXTVAL)"

	data = exportInvestigationMeta(icatCon,database)
	
	try:	
		cursInvestigationMeta = dashboardCon.cursor()

		cursInvestigationMeta.executemany(importQuery,data)
	
	except Exception as e:
		print "Issue with importing investigation meta data into the dashboard", e
		sys.exit(1)		
	finally:
		cursInvestigationMeta.close()
	
	

	print "Import of investigation meta data complete"	

#Final method that sets the integrity flag in the dashboard for the previous day so the dashboard can begin collecting data the next day.
def insertIntegrityFlag(dashboardCon,database,integrityType):
	now = datetime.datetime.now()
	yesterday = now - datetime.timedelta(days = 1)

	cursIntegrity = dashboardCon.cursor()

	if(database=="mySql"):
		importQuery="INSERT INTO IMPORTCHECK(TYPE,CHECKDATE,PASSED,CREATE_TIME,MOD_TIME) VALUES(%s,%s,%s,%s,%s)"
	elif(database=="oracle"):
		importQuery = " INSERT INTO IMPORTCHECK(TYPE,CHECKDATE,PASSED,CREATE_TIME,MOD_TIME,ID) VALUES(:0,:1,:2,:3,:4,ID_SEQ.NEXTVAL)"
	
	
	data = (integrityType,yesterday,1,now,now)

	try:

		cursIntegrity.execute(importQuery,data)

	except Exception as e:
		print "Integrity flag insert has failed ",e
		sys.exit(1)
	finally:
		cursIntegrity.close()

	dashboardCon.commit()


if __name__ == "__main__":    
	print("Initiating ICAT to Dashboard")    	

	parser = argparse.ArgumentParser(description='Decide what databases to transfer from')
  	parser.add_argument('--mysql',action="store_true", help='Mysql Flag')
	parser.add_argument('--oracle',action="store_true", help='Oracle flag')
	args = parser.parse_args()
	
	database="oracle"
	
	if(args.mysql):

		configuration = parseMySqlConfig()
		icatCon = connectToMySql(configuration,"icat")
		dashboardCon = connectToMySql(configuration,"dashboard")		
		database="mySql"
		import pymysql

	elif(args.oracle):
		configuration = parseOracleConfig()
		icatCon = connectToOracle(configuration,"icat")
		dashboardCon = connectToOracle(configuration,"dashboard")
		import cx_Oracle 


	exportUsers(icatCon,dashboardCon,database,configuration['rootUserName'])
	importEntitiesCount(dashboardCon, icatCon, database,configuration)
	if hasLogTable:
                exportLogs(icatCon,dashboardCon,database)	
	importInstrumentMeta(dashboardCon,icatCon,database)
	importInvestigationMeta(dashboardCon,icatCon,database)
	
	insertIntegrityFlag(dashboardCon,database,"entity")
	insertIntegrityFlag(dashboardCon,database,"instrument")	
	insertIntegrityFlag(dashboardCon,database,"investigation")
	

	icatCon.close()
	dashboardCon.close()

	print("Finished the Dashboard Initial Import")
	
