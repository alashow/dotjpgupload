<?php
$max_size = 12097152;

function randomName() {
	$alphabet = "abcdefghijklmnopqrstuwxyzABCDEFGHIJKLMNOPQRSTUWXYZ0123456789-_!";
	$pass = array();
	$alphaLength = strlen($alphabet) - 1;
	for ($i = 0; $i < 5; $i++) {
		$n = rand(0, $alphaLength);
		$pass[] = $alphabet[$n];
	}
	return implode($pass);
}

if (isset($_FILES['image']) && $_POST['password'] == "VYnu4YF9MrkNpknWGgR33ZyZ") {
	$file_name = $_FILES['image']['name'];
	$file_size = $_FILES['image']['size'];
	$file_tmp = $_FILES['image']['tmp_name'];
	$file_type = $_FILES['image']['type'];
	$file_ext = strtolower(end(explode('.', $_FILES['image']['name'])));
	
	$extensions = array("jpeg", "jpg", "png");
	if (in_array($file_ext, $extensions) === false) {
		$arr = array('error' => "invalid file type");
		echo json_encode($arr);
	}
	if ($file_size > $max_size) {
		$arr = array('error' => "file more than 12mb");
		echo json_encode($arr);
	}
	
	if (empty($errors) == true) {
		$newFileName = randomName() . "." . $file_ext;
		if (move_uploaded_file($file_tmp, "../images/" . $newFileName)) {
			$con = mysql_connect("localhost", "root", "your database password") or die(mysql_error());
			mysql_select_db("dotjpg") or die("Database yok");
			
			mysql_query("INSERT INTO `images`(`filename`) VALUES ('$newFileName')");
			mysql_close();
			
			$arr = array('image' => $newFileName);
			echo json_encode($arr);
		} else {
			$arr = array('error' => "unknown error");
			echo json_encode($arr);
		}
	} else {
		$arr = array('error' => "unknown error");
		echo json_encode($arr);
	}
} else {
	$arr = array('error' => "image not uploaded or incorrect password");
	echo json_encode($arr);
}
?>
