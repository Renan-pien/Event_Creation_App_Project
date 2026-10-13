<?php

header("Content-Type: application/json");

// Aceita somente POST
if ($_SERVER["REQUEST_METHOD"] !== "POST") {
    echo json_encode(["success" => false, "message" => "Método inválido"]);
    exit;
}

require_once "conexao.php";

// Recebe dados
$nome       = $_POST["nome"] ?? null;
$local      = $_POST["local"] ?? null;
$data       = $_POST["data"] ?? null;   // dd/MM/yyyy
$hora       = $_POST["hora"] ?? null;   // HH:mm
$usuario_id = $_POST["usuario_id"] ?? null;

// Verifica parâmetros
if (!$nome || !$local || !$data || !$hora || !$usuario_id) {
    echo json_encode(["success" => false, "message" => "Parâmetros ausentes"]);
    exit;
}


//  VALIDAÇÃO
$dataBR = str_replace('/', '-', $data); 
$dataSQL = date("Y-m-d", strtotime($dataBR));

$dtEvento = strtotime("$dataSQL $hora");
$agora = time();

if ($dtEvento === false) {
    echo json_encode(["success" => false, "message" => "Data ou hora inválida"]);
    exit;
}

if ($dtEvento <= $agora) {
    echo json_encode(["success" => false, "message" => "O evento deve estar no futuro"]);
    exit;
}

// Prepara query
$stmt = $con->prepare("
    INSERT INTO eventos (usuario_id, nome_evento, local_evento, data_evento, hora_evento)
    VALUES (?, ?, ?, ?, ?)
");

$stmt->bind_param("issss", $usuario_id, $nome, $local, $dataSQL, $hora);

// Executa
if ($stmt->execute()) {

    echo json_encode([
        "success" => true,
        "message" => "Evento criado com sucesso",
        "id"      => $con->insert_id  
    ]);

} else {

    echo json_encode([
        "success" => false,
        "message" => "Erro ao criar evento",
        "erro"     => $stmt->error
    ]);
}

$stmt->close();
$con->close();
