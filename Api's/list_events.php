<?php
header("Content-Type: application/json; charset=UTF-8");
require_once "conexao.php";

// Cada usuário vê apenas os próprios eventos.
$usuario_id = $_GET["usuario_id"] ?? null;

if ($usuario_id === null || !is_numeric($usuario_id)) {
    echo json_encode([
        "success" => false,
        "message" => "usuario_id ausente ou inválido"
    ]);
    exit;
}

$stmt = $con->prepare("
    SELECT
        id,
        nome_evento,
        local_evento,
        data_evento,
        hora_evento
    FROM eventos
    WHERE usuario_id = ?
    ORDER BY data_evento ASC, hora_evento ASC
");
$stmt->bind_param("i", $usuario_id);
$stmt->execute();
$result = $stmt->get_result();

if (!$result) {
    echo json_encode([
        "success" => false,
        "message" => "Erro ao buscar eventos"
    ]);
    exit;
}

$eventos = [];

while ($row = $result->fetch_assoc()) {
    $eventos[] = $row;
}

echo json_encode([
    "success" => true,
    "quantidade" => count($eventos),
    "eventos" => $eventos
]);

$stmt->close();
$con->close();
