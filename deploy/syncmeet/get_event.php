<?php
header("Content-Type: application/json; charset=UTF-8");
require_once "conexao.php";

$id = $_GET["id"] ?? null;
$usuario_id = $_GET["usuario_id"] ?? null;

if (!$id || !is_numeric($id) || !$usuario_id || !is_numeric($usuario_id)) {
    echo json_encode([
        "success" => false,
        "message" => "ID inválido ou ausente"
    ]);
    exit;
}

// Só retorna o evento se ele pertencer ao usuário informado
$stmt = $con->prepare("
    SELECT id, nome_evento, local_evento, data_evento, hora_evento
    FROM eventos
    WHERE id = ? AND usuario_id = ?
");
$stmt->bind_param("ii", $id, $usuario_id);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    echo json_encode([
        "success" => false,
        "message" => "Evento não encontrado"
    ]);
    exit;
}

$evento = $result->fetch_assoc();

// Formata data para dd/MM/yyyy
if (!empty($evento["data_evento"])) {
    $evento["data_evento"] = date("d/m/Y", strtotime($evento["data_evento"]));
}

echo json_encode([
    "success" => true,
    "evento" => $evento
]);

$stmt->close();
$con->close();
