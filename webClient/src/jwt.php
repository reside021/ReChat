<?php
    require_once "../../../vendor/autoload.php";
    use Emarref\Jwt\Claim;
    $jwt = new Emarref\Jwt\Jwt();
    $algorithm = new Emarref\Jwt\Algorithm\Hs256('ReChatWebVersion');
    $encryption = Emarref\Jwt\Encryption\Factory::create($algorithm);
    $token = new Emarref\Jwt\Token();
    $token->addClaim(new Claim\Issuer($_POST['tagUser']));
    $token->addClaim(new Claim\JwtId($_POST['time']));
    $token->addClaim(new Claim\Subject($_POST['platform']));
    $serializedToken = $jwt->serialize($token, $encryption);
    echo $serializedToken;
?>