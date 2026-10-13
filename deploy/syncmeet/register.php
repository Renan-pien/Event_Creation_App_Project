<?php
header("Content-Type: application/json");

if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    echo json_encode(["success" => false, "message" => "Método inválido"]);
    exit;
}

require_once "conexao.php";

$email   = $_POST["email"]   ?? null;
$usuario = $_POST["usuario"] ?? null;
$senha   = $_POST["senha"]   ?? null;


if (empty($email) || empty($usuario) || empty($senha)) {
    echo json_encode([
        "success" => false,
        "message" => "Parâmetros ausentes"
    ]);
    exit;
}

$senhaHash = password_hash($senha, PASSWORD_DEFAULT);

$stmt = $con->prepare("INSERT INTO usuarios (email, usuario, senha) VALUES (?, ?, ?)");
$stmt->bind_param("sss", $email, $usuario, $senhaHash);

if ($stmt->execute()) {
    echo json_encode(["success" => true, "message" => "Usuário cadastrado!"]);
} else {
    echo json_encode(["success" => false, "message" => "Erro ao cadastrar usuário"]);
}

$stmt->close();
$con->close();
?>