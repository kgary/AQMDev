-- Script to select * all tuples from DB, to be run from ij tool
-- connect 'jdbc:derby:D:/ASUProject/AQM_Dylos/derby_home/AQMdb';
connect 'jdbc:derby:derby_home/AQMdb';
select * from "APP"."PARTICLEREADING";
select * from "APP"."SERVERPUSHEVENTLOG";
disconnect;
exit;
