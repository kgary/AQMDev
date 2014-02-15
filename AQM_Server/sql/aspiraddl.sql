-- ============================

-- This file was created using Derby's dblook utility.
-- Timestamp: 2013-04-11 21:20:21.696
-- Specified schema is: APP
-- appendLogs: false

-- ----------------------------------------------
-- DDL Statements for sequences
-- ----------------------------------------------

CREATE SEQUENCE "APP"."ASPIRA_SEQ"
    AS INTEGER
    START WITH -2147483648
    INCREMENT BY 1
    MAXVALUE 9999999
    MINVALUE -2147483648
    CYCLE
;

-- ----------------------------------------------
-- DDL Statements for tables
-- ----------------------------------------------

CREATE TABLE "APP"."PARTICLEREADING" ("DEVICEID" VARCHAR(16) NOT NULL, "PATIENTID" VARCHAR(16) NOT NULL, "READINGTIME" TIMESTAMP NOT NULL, "SMALLPARTICLE" INTEGER NOT NULL, "LARGEPARTICLE" INTEGER, "GROUPID" INTEGER);

CREATE TABLE "APP"."SERVERPUSHEVENTLOG" ("EVENTTIME" TIMESTAMP NOT NULL, RESPONSECODE INTEGER NOT NULL, OBJECTTYPE INTEGER, MESSAGE VARCHAR(1024));

-- ----------------------------------------------
-- DDL Statements for keys
-- ----------------------------------------------

-- primary/unique

ALTER TABLE "APP"."PARTICLEREADING" ADD CONSTRAINT "SQL130411000134490" PRIMARY KEY ("DEVICEID", "PATIENTID", "READINGTIME");


