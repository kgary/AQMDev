Database:
TABLE1: aqmdata_common
COLUMN_NAME         |TYPE_NAME|DEC&|NUM&|COLUM&|COLUMN_DEF|CHAR_OCTE&|IS_NULL&
------------------------------------------------------------------------------
DEVICEID            |VARCHAR  |NULL|NULL|64    |NULL      |128       |NO
DATETIME            |TIMESTAMP|9   |10  |29    |NULL      |NULL      |NO
LATITUDE            |REAL     |NULL|2   |23    |NULL      |NULL      |YES
LONGITUDE           |REAL     |NULL|2   |23    |NULL      |NULL      |YES
METHOD              |VARCHAR  |NULL|NULL|16    |NULL      |32        |YES

TABLE2: aqmdata_dylos
COLUMN_NAME         |TYPE_NAME|DEC&|NUM&|COLUM&|COLUMN_DEF|CHAR_OCTE&|IS_NULL&
------------------------------------------------------------------------------
DEVICEID            |VARCHAR  |NULL|NULL|64    |NULL      |128       |NO
DATETIME            |TIMESTAMP|9   |10  |29    |NULL      |NULL      |NO
SMALLPARTICLE       |INTEGER  |0   |10  |10    |NULL      |NULL      |NO
LARGEPARTICLE       |INTEGER  |0   |10  |10    |NULL      |NULL      |NO
USERID              |VARCHAR  |NULL|NULL|32    |NULL      |64        |YES

TABLE3: aqmdata_sensordrone
COLUMN_NAME         |TYPE_NAME|DEC&|NUM&|COLUM&|COLUMN_DEF|CHAR_OCTE&|IS_NULL&
------------------------------------------------------------------------------
DEVICEID            |VARCHAR  |NULL|NULL|64    |NULL      |128       |NO
DATETIME            |TIMESTAMP|9   |10  |29    |NULL      |NULL      |NO
PRESSUREDATA        |INTEGER  |0   |10  |10    |NULL      |NULL      |YES
TEMPDATA            |INTEGER  |0   |10  |10    |NULL      |NULL      |YES
CODATA              |INTEGER  |0   |10  |10    |NULL      |NULL      |YES
HUMIDITYDATA        |INTEGER  |0   |10  |10    |NULL      |NULL      |YES
CO2SENSORID         |VARCHAR  |NULL|NULL|64    |NULL      |128       |YES
CO2DATA             |INTEGER  |0   |10  |10    |NULL      |NULL      |YES