-- Script to initial some reading data to DB, to be run from ij tool
-- connect 'jdbc:derby:D:/ASUProject/AQM_Server/derby_server/AQMdb';
connect 'jdbc:derby:derby_server/AQMdb';
insert into PARTICLE_READING values ('aqm0', 'user0', CURRENT_TIMESTAMP, 0, 0, 0.0, 0.0, 'unknown');
insert into SENSORDRONE_READING values ('SensorDrone00:00:00:00:00:00', CURRENT_TIMESTAMP, 'unknown', 0, 0, 0, 0, 0, 0.0, 0.0, 'unknown');
insert into SERVER_PUSH_EVENT values (CURRENT_TIMESTAMP, 1, 1, 'test Pushed 1 DylosReading to the server');
disconnect;
exit;

