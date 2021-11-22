<?php
if(!empty($_FILES['image']['tmp_name'])){
    $tmp = $_FILES['image']['tmp_name'];
    $name = $_POST['user_id'];
    $name = $name.'.jpg';
    $path = "avatarImg/".$name;
    if($_FILES['image']['size'] != 0){
        move_uploaded_file($tmp, $path);
    }
}
?>