<?php
require "conn.php";

$username = $_POST["username"];
$password = $_POST["password"];
$mysql_query = "select * from employee_data where username = '$username' and password = '$password';";
$result = mysqli_query($conn, $mysql_query);

if(mysqli_num_rows($result) > 0) {
	echo "Login successful!";
}
else {
	echo "Login unsuccessful";
}
?>