<?php
// ====================================================================
// Página da raiz do site (https://syncmeet.freedev.app/)
// Confirma que a API está no ar e que o banco responde — útil para
// validar o deploy antes de testar o app.
// ====================================================================

header("Content-Type: text/plain; charset=UTF-8");

include __DIR__ . "/syncmeet/conexao.php";

$bancoOk = $con->ping();

echo "SyncMeet API online\n";
if ($bancoOk) {
    echo "Banco de dados: conectado (OK)\n";
} else {
    echo "Banco de dados: FALHANDO\n";
    echo "-> Confira host/usuario/senha/banco em /syncmeet/conexao.php\n";
    echo "-> A senha e a que voce definiu no painel em MySQL Databases\n";
}
echo "Endpoints em: https://syncmeet.freedev.app/syncmeet/\n";
