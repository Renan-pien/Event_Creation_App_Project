<?php
header("Content-Type: application/json; charset=UTF-8");
require_once "conexao.php";

// Só aceita POST
if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    echo json_encode([
        "success" => false,
        "message" => "Método inválido. Use POST."
    ]);
    exit;
}

// ID do evento e do dono (só exclui se pertencer ao usuário)
$id         = $_POST["id"] ?? null;
$usuario_id = $_POST["usuario_id"] ?? null;

if (!$id || !is_numeric($id) || !$usuario_id || !is_numeric($usuario_id)) {
    echo json_encode([
        "success" => false,
        "message" => "ID inválido ou ausente"
    ]);
    exit;
}

// Prepara comando seguro
$sql = "DELETE FROM eventos WHERE id = ? AND usuario_id = ?";
$stmt = $con->prepare($sql);

if (!$stmt) {
    echo json_encode([
        "success" => false,
        "message" => "Erro no prepare"
    ]);
    exit;
}

$stmt->bind_param("ii", $id, $usuario_id);

// Execução
if ($stmt->execute()) {

    // Se não excluiu nenhuma linha -> ID não existe (ou não é deste usuário)
    if ($stmt->affected_rows === 0) {
        echo json_encode([
            "success" => false,
            "message" => "Nenhum evento encontrado com esse ID"
        ]);
    } else {
        echo json_encode([
            "success" => true,
            "message" => "Evento excluído com sucesso",
            "id" => $id
        ]);
    }

} else {
    echo json_encode([
        "success" => false,
        "message" => "Erro ao excluir evento"
    ]);
}

$stmt->close();
$con->close();
