<?php
header("Content-Type: application/json; charset=UTF-8");

// Aceita somente POST
if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    echo json_encode(["success" => false, "message" => "Método inválido"]);
    exit;
}

require_once "conexao.php";

// Recebe dados
$id          = $_POST["id"] ?? null;
$usuario_id  = $_POST["usuario_id"] ?? null;
$nome        = $_POST["nome"] ?? null;
$local       = $_POST["local"] ?? null;
$data        = $_POST["data"] ?? null;  // dd/MM/yyyy
$hora        = $_POST["hora"] ?? null;  // HH:mm

// Validação básica
if (!$id || !$usuario_id || !$nome || !$local || !$data || !$hora) {
    echo json_encode(["success" => false, "message" => "Parâmetros ausentes"]);
    exit;
}

$dataBR   = str_replace('/', '-', $data);
$dataSQL  = date("Y-m-d", strtotime($dataBR));

$dtEvento = strtotime("$dataSQL $hora");
$agora    = time();

if ($dtEvento === false) {
    echo json_encode(["success" => false, "message" => "Data ou hora inválida"]);
    exit;
}

// Evento atualizado para um horário no passado?
if ($dtEvento <= $agora) {
    echo json_encode(["success" => false, "message" => "O evento deve ser marcado para um horário futuro"]);
    exit;
}

// Atualiza somente se o evento pertencer ao usuário (WHERE inclui usuario_id)
$sql = "
    UPDATE eventos
    SET nome_evento = ?,
        local_evento = ?,
        data_evento = ?,
        hora_evento = ?
    WHERE id = ? AND usuario_id = ?
";

$stmt = $con->prepare($sql);

if (!$stmt) {
    echo json_encode(["success" => false, "message" => "Erro no prepare"]);
    exit;
}

$stmt->bind_param("ssssii", $nome, $local, $dataSQL, $hora, $id, $usuario_id);

if ($stmt->execute()) {

    if ($stmt->affected_rows === 0) {
        echo json_encode([
            "success" => false,
            "message" => "Evento não encontrado para este usuário"
        ]);
    } else {
        echo json_encode([
            "success" => true,
            "message" => "Evento atualizado com sucesso",
            "id" => $id  // devolve ID para re-agendar alarme
        ]);
    }
} else {
    echo json_encode([
        "success" => false,
        "message" => "Erro ao atualizar evento"
    ]);
}

$stmt->close();
$con->close();
