<?php

 $con = mysqli_connect('rulerdb.cgnyyebh5a4i.ap-northeast-2.rds.amazonaws.com', 'admin', 'rulerrdsdb', 'userDB');

 if(!$con){
         echo "MYSQL 접속에러 :";
         echo mysqli_connect_error();
         exit();
 }
 $t1 = "test";
 $t2 = "test";
 $t3 = "test";
 $t4 = "test";

 $statement = mysqli_prepare($con, "INSERT INTO userInfo VALUES(?,?,?,?)");
 mysqli_stmt_bind_param($statement, "ssss",$t1, $t2, $t3,$t4);
 mysqli_stmt_execute($statement);

 $response = array();
 $response["success"] = true;

 echo json_encode($response);

?>

