This is where the code for the server should go

02/14/14 Fri
AQM_Server:

Need to modify push.url, aqmSerialPort, aspira.home, jdbc.url in *.properties files (AQM_Dylos\properties)

Then copy *.properties files to (AQM_Dylos\WEB-INF\classes\properties) before run it

*************************************************************
1. Now the client side will convert the serialized Java object to JSON object and send it to server, while servlet can accept JSON object via HTTP POST and save data to DB.
the JSON object looks like: (For example, contain 2 airqualityreadings)

{"largeparticle":[18,12],"patientid":["patient3","patient3"],"readingtime":[Fri Feb 14 15:43:22 MST 2014,Fri Feb 14 15:44:21 MST 2014],"smallparticle":[78,88],"deviceid":["aqm3","aqm3"]}

2. And the servlet can fetch data from DB in JSON object, and show it on web.
Now I simply print it on the page (For example: http://localhost:8080/AQM?AirQualityReadings=10 )

the result on web looks like: 
Last server push for type AirQualityReadings (json) : 
{"message":"Pushed 2 jairqualityreadings to the server","eventtime":Fri Feb 14 15:45:24 MST 2014,"objecttype":1,"responsecode":2}

{"info":[{"largeparticle":12,"patientid":"patient3","readingtime":Fri Feb 14 15:44:21 MST 2014,"smallparticle":88,"deviceid":"aqm3"},{"largeparticle":18,"patientid":"patient3","readingtime":Fri Feb 14 15:43:22 MST 2014,"smallparticle":78,"deviceid":"aqm3"},{"largeparticle":15,"patientid":"patient3","readingtime":Fri Feb 14 15:42:22 MST 2014,"smallparticle":85,"deviceid":"aqm3"},{"largeparticle":17,"patientid":"patient3","readingtime":Fri Feb 14 15:41:23 MST 2014,"smallparticle":80,"deviceid":"aqm3"},{"largeparticle":19,"patientid":"patient3","readingtime":Fri Feb 14 15:40:24 MST 2014,"smallparticle":87,"deviceid":"aqm3"},{"largeparticle":19,"patientid":"patient3","readingtime":Fri Feb 14 15:39:25 MST 2014,"smallparticle":89,"deviceid":"aqm3"},{"largeparticle":24,"patientid":"patient3","readingtime":Fri Feb 14 15:38:26 MST 2014,"smallparticle":95,"deviceid":"aqm3"},{"largeparticle":24,"patientid":"patient3","readingtime":Fri Feb 14 15:37:26 MST 2014,"smallparticle":104,"deviceid":"aqm3"},{"largeparticle":28,"patientid":"patient3","readingtime":Fri Feb 14 15:35:28 MST 2014,"smallparticle":110,"deviceid":"aqm3"},{"largeparticle":71,"patientid":"patient3","readingtime":Fri Feb 14 15:13:45 MST 2014,"smallparticle":224,"deviceid":"aqm3"}]}