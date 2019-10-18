<?php
$conn = mysqli_connect(
  'rulerdb.cgnyyebh5a4i.ap-northeast-2.rds.amazonaws.com',
  'admin',
  'rulerrdsdb',
  'userDB');
echo "<h1>multi row</h1>";
$sql = "SELECT * FROM userInfo";
$result = mysqli_query($conn, $sql);
while($row = mysqli_fetch_array($result)) {
  echo '<h2>'.$row['userid'].'</h2>';
  echo $row['description'];
}
?>
