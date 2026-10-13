<?php
// ====================================================================
// CONEXÃO COM O BANCO — AMBIENTE LOCAL (XAMPP do seu PC)
//
// Este arquivo é para a pasta htdocs/syncmeet do SEU computador
// (usado pelo app em modo debug no emulador).
//
// O ARQUIVO DO SERVIDOR InfinityFree é outro:
//   deploy/syncmeet/conexao.php  (credenciais de produção)
// Não misture os dois — o MySQL da InfinityFree não aceita conexão
// externa, então as credenciais deles não funcionam no XAMPP local.
// ====================================================================

$host = "localhost";
$user = "root";
$pass = "";
$db   = "syncmeet";

// PHP 8.1+: sem isto, falha de conexão vira exceção não tratada (500).
mysqli_report(MYSQLI_REPORT_OFF);

$con = @mysqli_connect($host, $user, $pass, $db);

if (!$con) {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Erro interno do servidor"]);
    exit;
}

mysqli_set_charset($con, "utf8mb4");
?>
