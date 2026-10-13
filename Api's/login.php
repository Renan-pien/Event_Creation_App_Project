<?php
header("Content-Type: application/json; charset=UTF-8");
include("conexao.php");

if ($_SERVER["REQUEST_METHOD"] !== "POST" || !isset($_POST["email"]) || !isset($_POST["senha"])) {
    echo json_encode(["success" => false, "message" => "Parâmetros ausentes"]);
    exit;
}

$email = $_POST["email"];
$senha = $_POST["senha"];

// Query preparada: impede SQL Injection no campo de e-mail
$stmt = $con->prepare("SELECT id, email, usuario, senha FROM usuarios WHERE email = ? LIMIT 1");
$stmt->bind_param("s", $email);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows > 0) {
    $user = $result->fetch_assoc();

    if (password_verify($senha, $user['senha'])) {
        echo json_encode([
            "success" => true,
            "message" => "Login ok",
            "user" => [
                "id" => $user['id'],
                "email" => $user['email'],
                "usuario" => $user['usuario']
            ]
        ]);
    } else {
        echo json_encode(["success" => false, "message" => "Senha incorreta"]);
    }
} else {
    echo json_encode(["success" => false, "message" => "Email não encontrado"]);
}

$stmt->close();
mysqli_close($con);
?>
