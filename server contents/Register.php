<?php

 $con = mysqli_connect('rulerdb.cgnyyebh5a4i.ap-northeast-2.rds.amazonaws.com', 'admin', 'rulerrdsdb', 'userDB');

 if(!$con){	
	 echo "MYSQL 접속에러 :";
	 echo mysqli_connect_error();
	 exit();
 }

 $userID = $_POST["userid"];
 $passwd = $_POST["passwd"];
 $name = $_POST["name"];
 $email = $_POST["email"];

 $statement = mysqli_prepare($con, "INSERT INTO userInfo VALUES(?,?,?,?)");
 mysqli_stmt_bind_param($statement, "ssss",$userID, $passwd, $name, $email);
 mysqli_stmt_execute($statement);

 $response = array();
 $response["success"] = true;

 echo json_encode($response);

?>
