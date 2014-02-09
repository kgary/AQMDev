-- Script to delete all tuples from DB, to be run from ij tool
-- connect 'jdbc:derby:D:/projects/AQM_dev/derby_home/aspiradb';
connect 'jdbc:derby:derby_home/aspiradb';
delete from "APP"."PARTICLEREADING";
delete from "APP"."SERVERPUSHEVENTLOG";
disconnect;
exit;
