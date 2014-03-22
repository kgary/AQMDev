-- [{"deviceId":"aqm0","userId":"user0","dateTime":"Thu Mar 13 07:29:55 MST 2014","smallParticle":78,-- "largeParticle":18,"geoLatitude":0.0,"geoLongitude":0.0,"geoMethod":"manual"},{"deviceId":"aqm0","-- userId":"user0","dateTime":"Thu Mar 13 07:30:55 MST 2014","smallParticle":88,"largeParticle":12,"g-- eoLatitude":33.309986,"geoLongitude":-111.672504,"geoMethod":"manual"}]

-- {"id":"SensorDroneB8:FF:FE:B9:D9:A0","presureData":"97200","tempData":"29","coData":"2","dateTime":
-- "20140302_231028","humidityData":"48","co2Data":"-1"} 

CREATE TABLE aqmdata_common (
       deviceId VARCHAR(64) NOT NULL,
       dateTime	Timestamp   NOT NULL,
       latitude Real,
       longitude Real,
       method	 VARCHAR (16)
);

ALTER TABLE aqmdata_common ADD CONSTRAINT aqmdata_common_pk PRIMARY KEY (deviceId, dateTime);

CREATE TABLE aqmdata_dylos (
       deviceId 	   VARCHAR(64) NOT NULL,
       dateTime		   Timestamp   NOT NULL,
       smallParticle	   INTEGER     NOT NULL,
       largeParticle	   INTEGER     NOT NULL,
       userId		   VARCHAR(32)
);

ALTER TABLE aqmdata_dylos ADD CONSTRAINT dylos_common_fk FOREIGN KEY (deviceId, dateTime) REFERENCES aqmdata_common(deviceId, dateTime);

CREATE TABLE aqmdata_sensordrone (
       deviceId 	   VARCHAR(64) NOT NULL,
       dateTime		   Timestamp   NOT NULL,
       pressureData	   INTEGER,
       tempData		   INTEGER,
       coData		   INTEGER,
       humidityData	   INTEGER,
       co2SensorId	   VARCHAR(64),
       co2Data		   INTEGER
);

ALTER TABLE aqmdata_sensordrone ADD CONSTRAINT sensordrone_common_fk FOREIGN KEY (deviceId, dateTime) REFERENCES aqmdata_common(deviceId, dateTime);
