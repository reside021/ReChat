<?php
if(!empty($_POST['user_id'])){
    $name = $_POST['user_id'];
    $name = $name.'.jpg';
    $path = "avatarImg/".$name;
     if(file_exists($path)){
         unlink($path);
     }
}
?>