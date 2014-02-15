-- Script to select * all tuples from DB, to be run from ij tool
-- connect 'jdbc:derby:D:/projects/AQM_dev/derby_home/aspiradb';
connect 'jdbc:derby:derby_home/aspiradb';
select * from "APP"."PARTICLEREADING";
select * from "APP"."SERVERPUSHEVENTLOG";
disconnect;
exit;
