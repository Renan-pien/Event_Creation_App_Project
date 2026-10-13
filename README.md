# Event Creation App – SyncMeet

Aplicativo Android para criação, edição, listagem e exclusão de eventos, com
login de usuário e lembrete por notificação (40 minutos antes do evento).
O backend é PHP + MySQL.

## 📌 Funcionalidades

- Criar conta / login (e-mail + senha com hash no servidor)
- "Lembrar de mim" (guarda apenas o e-mail — a senha nunca fica no aparelho)
- Criar eventos: nome, local, data e horário
- Lembrete por notificação 40 min antes (criação e edição)
- Listar, editar e excluir eventos (por usuário)
- Histórico e agenda de eventos
- Exclusão de conta (remove usuário e eventos)

## 🛠️ Tecnologias

**Android (Java)**
- Volley (requisições HTTP) com fila única (`VolleyQueue`)
- URL base centralizada em `BuildConfig.BASE_URL`
- minSdk 24 / targetSdk 36

**Backend (PHP + MySQL)** em `Api's/`
- `login.php`, `register.php`, `create_event.php`, `list_events.php`,
  `get_event.php`, `update_event.php`, `delete_event.php`, `delete_account.php`
- Queries preparadas (sem SQL injection)
- Cada usuário só acessa os próprios eventos (`usuario_id`)

## Como rodar em desenvolvimento

1. Copie a pasta `Api's/` para `htdocs/syncmeet/` no XAMPP e crie o banco
   `syncmeet` (tabelas `usuarios` e `eventos`).
2. Rode o app em **debug** — a URL `http://10.0.2.2/syncmeet/` (emulador)
   já está configurada no `buildTypes.debug`. HTTP só é permitido em debug.
3. Em um celular físico, troque `10.0.2.2` pelo IP do seu PC no
   `app/build.gradle.kts` (buildType `debug`).

## Como gerar o AAB de produção

1. Coloque o backend em HTTPS seguindo o **`DEPLOY.md`** (hospedagem +
   SSL Let's Encrypt + passo a passo completo). A `BASE_URL` de release
   já aponta para `https://syncmeet.freedev.app/syncmeet/`.
2. Gere a keystore:
   `keytool -genkeypair -v -keystore syncmeet-release.jks -keyalg RSA -keysize 2048 -validity 10000 -alias syncmeet`
3. Crie `keystore.properties` na raiz (fora do git) com storeFile,
   storePassword, keyAlias e keyPassword.
4. `./gradlew clean bundleRelease`
   → `app/build/outputs/bundle/release/app-release.aab`

## ⚠️ Nota sobre permissões

`REQUEST_INSTALL_PACKAGES` (solicitada em fase anterior do projeto) foi
**removida** do Manifest para atender à política da Google Play — o app
não possui função de instalação de APKs. Se um dia for distribuir ou
atualizar o app por APK fora da loja, basta reinseri-la com uma linha
`<uses-permission>`, mas builds com ela **não** devem ser enviados à
Play Store.
