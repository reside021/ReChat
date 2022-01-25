<?php
if(!empty($_FILES['image']['tmp_name'])){
    $tmp = $_FILES['image']['tmp_name'];
    $photoName = $_POST['photoName'];
    $photoName = $photoName.'.jpg';
    $path = $_POST['nameChat'];
    $path = str_replace("::", "--", $path);
    $path = $path;
    if(!is_dir($path)){
        mkdir($path);
    }
    $path = $path."/".$photoName;
    if($_FILES['image']['size'] != 0){
        move_uploaded_file($tmp, $path);
    }
}
?>