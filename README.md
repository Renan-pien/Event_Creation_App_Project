Event Creation App – SyncMeet

Aplicativo Android desenvolvido para criação, edição, listagem e exclusão de eventos.
O projeto utiliza PHP + MySQL como backend e faz requisições HTTP pelo app para gerenciamento dos dados.

📌 Funcionalidades

Criar eventos com:

Nome do evento

Local

Data

Horário de início

Horário de término

Listar todos os eventos cadastrados

Editar eventos existentes

Excluir eventos

Interface adaptada com telas dedicadas para:

Login

Tela principal

Detalhes do evento

Edição do evento

🛠️ Tecnologias Utilizadas
Frontend (Android)

Java

Android Studio

Volley (HTTP Requests)

ConstraintLayout

RecyclerView

Backend (PHP)

APIs em PHP para:

Login

Listar eventos

Criar eventos

Editar eventos

Excluir eventos

Banco de dados MySQL

  Estrutura básica do projeto:

/app – código fonte Android

/php_api – scripts PHP (login, listar, criar, editar e excluir)

/res/layout – telas XML do aplicativo

MyApplication.java – classe que centraliza a URL base da API

Como rodar o projeto??

1. Configurar API (PHP)

Copie as pastas da API para htdocs/ no XAMPP

Crie o banco de dados no MySQL

Ajuste o arquivo conexao.php com seu usuário, senha e host

2. Configurar Android

Edite MyApplication.java

Configure o IP local do seu servidor PHP, exemplo:

public static final String BASE_URL = "http://10.0.0.2/syncmeet/";

3. Executar

Inicie o Apache e MySQL no XAMPP

Conecte o celular na mesma rede do PC

Compile o app no Android Studio

 Objetivo:

Este projeto foi criado para demonstrar uma solução simples e funcional para gerenciamento de eventos utilizando comunicação cliente-servidor via APIs.

Contato:

Caso queira contribuir ou tirar dúvidas, fique à vontade para abrir uma issue.
