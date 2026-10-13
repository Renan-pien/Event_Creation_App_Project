<?php
header("Content-Type: application/json; charset=UTF-8");

// Exclusão de conta — exigência da política da Google Play para apps
// com criação de conta. Apaga o usuário E todos os seus eventos.

if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    echo json_encode(["success" => false, "message" => "Método inválido"]);
    exit;
}

require_once "conexao.php";

$usuario_id = $_POST["usuario_id"] ?? null;

if (!$usuario_id || !is_numeric($usuario_id)) {
    echo json_encode(["success" => false, "message" => "usuario_id inválido ou ausente"]);
    exit;
}

// Exclui primeiro os eventos do usuário
$stmtEventos = $con->prepare("DELETE FROM eventos WHERE usuario_id = ?");
$stmtEventos->bind_param("i", $usuario_id);
$stmtEventos->execute();
$stmtEventos->close();

// Depois a conta
$stmtUser = $con->prepare("DELETE FROM usuarios WHERE id = ?");
$stmtUser->bind_param("i", $usuario_id);

if ($stmtUser->execute()) {
    if ($stmtUser->affected_rows === 0) {
        echo json_encode(["success" => false, "message" => "Conta não encontrada"]);
    } else {
        echo json_encode(["success" => true, "message" => "Conta e eventos excluídos com sucesso"]);
    }
} else {
    echo json_encode(["success" => false, "message" => "Erro ao excluir conta"]);
}

$stmtUser->close();
$con->close();
