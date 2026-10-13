# Deploy do backend SyncMeet (hospedagem + HTTPS)

Este guia coloca a API PHP+MySQL no ar com SSL gratuito (Let's Encrypt)
e finaliza o app para gerar o AAB de produção.

## O que você precisa

1. Um **domínio** (ex.: `syncmeet.com.br` — ~R$ 30–40/ano; algumas
   hospedagens incluem o domínio grátis no plano anual)
2. Uma **hospedagem com PHP 8+ e MySQL**

## Escolha da hospedagem

| Opção | Indicado para | Custo aprox. |
|---|---|---|
| **Compartilhada cPanel** (Hostinger, HostGator BR, Umbler…) | **Recomendada** para este projeto — upload direto da pasta `Api's`, MySQL e Let's Encrypt no painel | R$ 10–25/mês |
| **Grátis** (InfinityFree e similares) | Testar o app com backend real sem gastar; tem limites de uso e pode instabilizar | R$ 0 |
| **VPS** (Hetzner, Contabo…) | Só se você quiser administrar servidor (Apache, MySQL, Certbot) | US$ 4–6/mês |

> Valores e recursos: CONFIRMAR na oferta atual de cada provedor.

---

## Passo a passo (hospedagem cPanel — caminho recomendado)

### 1. Preparar o banco
1. No painel, abra **MySQL Databases** (ou "Bancos de dados MySQL").
2. Crie o banco **`syncmeet`**.
3. Crie um **usuário MySQL dedicado** com senha forte (nunca use `root`
   em produção) e associe ao banco com **ALL PRIVILEGES**.
4. Abra **phpMyAdmin** → selecione o banco `syncmeet` → aba **SQL** →
   cole o conteúdo de `Api's/database.sql` → Executar.
   (Alternativa: importe o arquivo `database.sql` pela aba "Importar".)

### 2. Subir os arquivos
1. No **File Manager**, entre em `public_html`.
2. Crie a pasta **`syncmeet`**.
3. Envie para dentro dela TODOS os `.php` e o `.htaccess` da pasta
   `Api's` do projeto.
   - **NÃO suba** `database.sql` nem este `DEPLOY.md`.
   - O resultado deve ficar: `public_html/syncmeet/login.php`, etc.
   (a `BASE_URL` do app já espera o sufixo `/syncmeet/`).

### 3. Ajustar a conexão
Edite o `conexao.php` **no servidor** (botão direito → Edit) e troque as
4 credenciais locais pelas de produção:

```php
$host = "localhost";              // na maioria das cPanel é isso mesmo
$user = "syncmeet_user";          // usuário criado no passo 1.3
$pass = "SENHA_FORTE_AQUI";
$db   = "syncmeet";
```

### 4. Ativar o SSL (Let's Encrypt — grátis)
1. No painel, procure **SSL/TLS Status** (ou "Let's Encrypt").
2. Selecione o domínio → **Run AutoSSL / Issue Certificate**.
   Leva minutos; o certificado renova sozinho.
3. Ative **Force HTTPS** se o painel oferecer a opção.
4. No `.htaccess` do servidor, **descomente** as 3 linhas do
   redirecionamento HTTPS (remova o `#` inicial) — isso garante que
   qualquer acesso `http://` vire `https://`.

### 5. Testar a API (antes de mexer no app)
No terminal (ou navegador, para os GET):

```bash
# Deve responder {"success":false,...} com "Parâmetros ausentes" — provou que PHP+MySQL+rota funcionam
curl -X POST https://syncmeet.freedev.app/syncmeet/login.php -d "email=teste@teste.com&senha=123"

# Criar conta de teste
curl -X POST https://syncmeet.freedev.app/syncmeet/register.php -d "email=teste@teste.com&usuario=Teste&senha=123456"

# Login com a conta criada — deve retornar success:true e o id do usuário
curl -X POST https://syncmeet.freedev.app/syncmeet/login.php -d "email=teste@teste.com&senha=123456"

# Listar eventos do usuário 1 (vazio no início)
curl "https://syncmeet.freedev.app/syncmeet/list_events.php?usuario_id=1"
```

Confira também:
- `https://syncmeet.freedev.app/syncmeet/conexao.php` → deve dar **403 Forbidden**
  (o `.htaccess` bloqueando acesso direto — comportamento correto);
- `http://syncmeet.freedev.app/...` → deve redirecionar para `https://`.

### 6. Finalizar o app
1. Em `app/build.gradle.kts`, troque o placeholder pela URL real:
   ```kotlin
   buildConfigField("String", "BASE_URL", "\"https://syncmeet.freedev.app/syncmeet/\"")
   ```
2. **Remova** a permissão `REQUEST_INSTALL_PACKAGES` do
   `app/src/main/AndroidManifest.xml` (exigência para a Play Store).
3. Gere o AAB assinado:
   ```bash
   ./gradlew clean bundleRelease
   ```
   → `app/build/outputs/bundle/release/app-release.aab`

---

## Caminho VPS (resumo)

1. Instale Apache + PHP + MySQL: `apt install apache2 mysql-server php php-mysqli`
2. Copie a pasta para `/var/www/html/syncmeet/` e importe o `database.sql`
   (`mysql -u root -p syncmeet < database.sql`), criando antes o banco e
   o usuário dedicado.
3. Aponte o DNS do domínio para o IP do servidor.
4. Certificado: `apt install certbot python3-certbot-apache` e
   `certbot --apache -d syncmeet.freedev.app` (instala e renova sozinho).
5. Habilite o `.htaccess` (`AllowOverride All` no vhost) e siga os
   passos 5 e 6 acima.

## Caminho hospedagem grátis — InfinityFree (é o caso de syncmeet.freedev.app)

O domínio `syncmeet.freedev.app` aponta para a **InfinityFree**
(iFastNet). **A pasta `deploy/` do projeto já contém o pacote pronto**
(credenciais do banco, `.htaccess`, `index.php` e a versão do SQL sem
`CREATE DATABASE`) — siga o `deploy/LEIA-ME.txt` com o destino de cada
arquivo. Particularidades dessa hospedagem:

1. **Painel:** é o VistaPanel (não cPanel). No Client Area → sua conta →
   **Control Panel**.
2. **Banco de dados:** o banco e o usuário ganham **prefixo** (ex.:
   `if0_43106594_syncmeet` / `if0_43106594` — já configurados no
   `deploy/syncmeet/conexao.php`). No phpMyAdmin deles, importe o
   **`deploy/database_infinityfree.sql`** (versão sem `CREATE DATABASE`,
   que a conta não tem permissão para executar).
3. **Arquivos:** File Manager → dentro de **`htdocs`** suba o
   `index.php` e a pasta `syncmeet/` do pacote `deploy/` (dica: compacte
   em .zip, suba o zip e use "Extract"). O `index.php` da raiz mantém a
   conta com site ativo (evita suspensão por inatividade).
4. **SSL grátis:** Client Area → **Free SSL Certificates** → selecione
   `syncmeet.freedev.app` → instalar. Pode levar algumas horas. Só depois
   suba o `.htaccess` (ele força HTTPS em tudo).
5. **Limites:** há limite diário de acessos e de e-mails — suficiente
   para testes e uso leve. Para o app em produção na Play Store,
   avaliar migração para hospedagem paga (confiabilidade).

Funciona para colocar a API no ar e testar o app com HTTPS. Os passos de
teste (curl) e de finalização do app são os mesmos das seções anteriores
— a URL final é `https://syncmeet.freedev.app/syncmeet/`.

---

## Checklist final do backend em produção

- [ ] Banco `syncmeet` criado e schema importado
- [ ] Usuário MySQL dedicado (não root) com senha forte
- [ ] `conexao.php` do servidor com as credenciais de produção
- [ ] Certificado Let's Encrypt ativo (cadeado verde em `https://syncmeet.freedev.app`)
- [ ] HTTP → HTTPS redirecionando (Force HTTPS + `.htaccess`)
- [ ] `conexao.php` responde 403 no navegador
- [ ] `curl` do login/cadastro/lista retornando JSON esperado
- [ ] `database.sql` e `DEPLOY.md` NÃO estão no servidor
- [ ] `BASE_URL` do app apontando para `https://syncmeet.freedev.app/syncmeet/`
