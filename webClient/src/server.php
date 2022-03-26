<?php
if($_POST['typeOperation'] == "DELETEAVATAR"){
    $url = 'http://imagerc.ddns.net:80/avatar/deleteImage.php';
    $postFields = [
        'user_id' => $_POST['user_id']
    ];
    $ch = curl_init($url);
    curl_setopt($ch, CURLOPT_POST, 1);
    curl_setopt($ch, CURLOPT_POSTFIELDS, $postFields);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
    curl_setopt($ch, CURLOPT_HEADER, false);
    $html = curl_exec($ch);
    curl_close($ch);
}
if($_FILES['image']['size'] != 0){
    if($_POST['typeOperation'] == "SETAVATAR"){
        $url = 'http://imagerc.ddns.net:80/avatar/uploadImage.php';
        $postFields = [
            'user_id' => $_POST['user_id'],
            'image' => new \CURLFile($_FILES['image']['tmp_name'])
        ];
        $ch = curl_init($url);
        curl_setopt($ch, CURLOPT_POST, 1);
        curl_setopt($ch, CURLOPT_POSTFIELDS, $postFields);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
        curl_setopt($ch, CURLOPT_HEADER, false);
        $html = curl_exec($ch);
        curl_close($ch);
    }
    if($_POST['typeOperation'] == "IMGMSG"){
        $url = 'http://imagerc.ddns.net:80/userImgMsg/uploadNewImgMsg.php';
        $postFields = [
            'photoName' => $_POST['photoName'],
            'nameChat' => $_POST['nameChat'],
            'image' => new \CURLFile($_FILES['image']['tmp_name'])
        ];
        $ch = curl_init($url);
        curl_setopt($ch, CURLOPT_POST, 1);
        curl_setopt($ch, CURLOPT_POSTFIELDS, $postFields);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
        curl_setopt($ch, CURLOPT_HEADER, false);
        $html = curl_exec($ch);
        curl_close($ch);
    }
}
?>