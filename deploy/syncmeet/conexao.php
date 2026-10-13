<?php
// ====================================================================
// CONEXÃO COM O BANCO — SERVIDOR INFINITYFREE (produção)
//
// ATENÇÃO: o nome do banco TEM o prefixo if0_43106594_ (é assim que
// ele aparece no painel/phpMyAdmin). Não use só "syncmeet".
//
// Não versione/publish este arquivo com a senha real.
// ====================================================================

$host = "sql304.infinityfree.com";
$user = "if0_43106594";
$pass = "Renan4785";
$db   = "if0_43106594_syncmeet";

// PHP 8.1+: sem isto, falha de conexão vira exceção não tratada
// (HTTP 500 em tela branca). Com OFF, retorna false e o if abaixo
// responde JSON de erro controlado.
mysqli_report(MYSQLI_REPORT_OFF);

$con = @mysqli_connect($host, $user, $pass, $db);

if (!$con) {
    http_response_code(500);
    echo json_encode(["success" => false, "message" => "Erro interno do servidor"]);
    exit;
}

mysqli_set_charset($con, "utf8mb4");
?>
