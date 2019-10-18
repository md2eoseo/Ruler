<?php

 $con = mysqli_connect('rulerdb.cgnyyebh5a4i.ap-northeast-2.rds.amazonaws.com', 'admin', 'rulerrdsdb', 'dataDB');

$statement = mysqli_prepare($con, "SELECT fname FROM dataInfo WHERE fextention = 'jpg' ");
$countstmt = mysqli_query($con, "SELECT count(*) as fnum from dataInfo where fextention = 'jpg'"); 

mysqli_stmt_execute($statement);
mysqli_store_result($statement);
mysqli_stmt_bind_result($statement, $fName);

$fnum = mysqli_fetch_array($countstmt);

 $revs = array();
$allrevs = array();
$allrevs["fNum"] = $fnum['fnum'];
 while(mysqli_stmt_fetch($statement)){
	 $revs = $fName;
	 $allrevs[] = $revs;
 }
 echo json_encode($allrevs);

?>

