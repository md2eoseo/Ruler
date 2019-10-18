<?php

 $con = mysqli_connect('rulerdb.cgnyyebh5a4i.ap-northeast-2.rds.amazonaws.com', 'admin', 'rulerrdsdb', 'userDB');

 $userID = $_POST["userid"];
 $passwd = $_POST["passwd"];

 $statement = mysqli_prepare($con, "SELECT * FROM userInfo WHERE userid = ? AND passwd = ?");
 mysqli_stmt_bind_param($statement,"ss", $userID, $passwd);
 mysqli_stmt_execute($statement);
 mysqli_store_result($statement);
 mysqli_stmt_bind_result($statement, $userID, $passwd, $name, $email);

 $response = array();
 $response["success"] = false;

 while(mysqli_stmt_fetch($statement)){
	 $response["success"] = true;
	 $response["userid"] = $userID;
	 $response["passwd"] = $passwd;
	 $response["name"] = $name;
	 $response["email"] = $email;
 }

 echo json_encode($response);

?>
