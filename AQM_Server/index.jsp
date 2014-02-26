<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=gb2312" />
<script type="text/javascript" src="http://localhost:8080/AQMServer/jquery-1.6.2.min.js"></script>
<title>AQM</title>
<style type="text/css">
<!--
body,table{
font-size:12px;
}
table{
table-layout:fixed;
empty-cells:show;
border-collapse: collapse;
margin:0 auto;
}
td{
height:20px;
text-align:center;
}
h1,h2,h3{
font-size:12px;
margin:0;
padding:0;
}

.title { background: #FFF; border: 1px solid #9DB3C5; padding: 1px; width:90%;margin:20px auto; }
.title h1 { line-height: 31px; text-align:center;  background: #2F589C url(th_bg2.gif); background-repeat: repeat-x; background-position: 0 0; color: #FFF; }
  .title th, .title td { border: 1px solid #CAD9EA; padding: 5px; }

//css1
table.tab_css_1{
border:1px solid #cad9ea;
color:#666;
}
table.tab_css_1 th {
background-image: url(th_bg1.gif);
background-repeat::repeat-x;
height:30px;
background-color:#f5fafe;
}
table.tab_css_1 td,table.tab_css_1 th{
border:1px solid #cad9ea;
padding:0 1em 0;
}
table.tab_css_1 tr.tr_css{

}
 
.hover{
   background-color: #53AB38;
   color: #fff;
}

-->
</style>
</head>

<body>
<div style="width:90%;margin:auto">
<p align="right">
<select id="count" onchange="ajax()">
 <option value ="5">5</option>
  <option value ="10">10</option>
  <option value="20" selected = "selected">20</option>
  <option value="50">50</option>
</select>
items per page
</p>

<table id="table" width="100%" id="mytab"  border="1" class="tab_css_1">
  <thead>
    <th width="10%">DEVICEID</th>
    <th width="20%">PATIENTID</th>
    <th width="40%">READINGTIME</th>
    <th width="10%">SMALLPARITICLE</th>
    <th width="10%">LARGEPARITICLE</th>
  </thead>
  <tbody>
  <tr class="tr_css">
    <td>aqm0</td>
    <td>patient0</td>
    <td>2014-2-8 10:14:12</td>
    <td>101</td>
    <td>33</td>
  </tr>
  </tbody>
</table>

<script type="text/javascript">


function ajax(){
	var count = $("#count").val();
	var a = Math.random();      
    $.ajax({
               type:"GET",
               url:"<%=basePath%>" +"AspiraImportServlet?AirQualityReadings=" + count + "&"+"rand=" + a,
               dataType:"json",
                        
//Maybe useful
/*               beforeSend:function(){
               $("#loading").show();
               },
               complete:function(){
               $("#loading").hide();
               $("#download").show();
               },*/
               success:function(data,textStatus){
              
                    var ul="";
                    //Write json reading here
                    //alert("data = "+ data);
                    for(var i=0;i<data.info.length;i++){
                         ul += "<tr>";
                         ul += "<td>";
                         ul += data.info[i].deviceid;
                                        ul += "</td><td>";
                         ul += data.info[i].patientid;
                                        ul += "</td><td>";
                         ul += data.info[i].readingtime;
                                        ul += "</td><td>";
                         ul += data.info[i].smallparticle;
                                        ul += "</td><td>";
                         ul += data.info[i].largeparticle;
                                        ul += "</td>";
                         ul += "</tr>";
                    }
                    //////////////////////////////
                       $("#table tbody").html(ul);    
               }
          });
}

$(function(){   
 ajax();
});
</script> 

</body>
</html>