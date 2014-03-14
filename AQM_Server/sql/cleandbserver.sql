-- Script to delete all tuples from DB, to be run from ij tool
-- connect 'jdbc:derby:D:/ASUProject/AQM_Server/derby_server/AQMdb';
connect 'jdbc:derby:derby_server/AQMdb';
delete from "APP"."PARTICLE_READING";
delete from "APP"."SENSORDRONE_READING";
delete from "APP"."SERVER_PUSH_EVENT";
disconnect;
exit;
