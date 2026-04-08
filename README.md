# IMPORTANTE: TODOS OS SEGREDOS AQUI FORAM GERADOS ALEATORIAMENTE PARA TESTE E NÃO SÃO REAIS

# Gitleaks Demo com Maven e Spring Boot

Este repositório existe para demonstrar um erro comum em projetos backend: um desenvolvedor cria uma aplicação funcional, coloca credenciais diretamente no código ou no arquivo de configuração, faz commit disso no repositório e só depois percebe que expôs segredos.

O objetivo aqui não é ensinar uma arquitetura perfeita. O objetivo é simular um cenário plausível de time real:

- uma API Java com Maven e Spring Boot;
- um arquivo `application.properties` com credenciais sensíveis;
- classes que realmente consomem essas propriedades;
- um fluxo simples para mostrar como uma ferramenta como o Gitleaks encontra esse problema;
- e uma forma correta de corrigir o padrão usando variáveis de ambiente.

## 1. O que este projeto simula

Esta aplicação representa um backend simples feito em Java com Spring Boot.

Ela expõe endpoints HTTP, sobe em container Docker e possui classes que simulam integrações comuns de sistemas corporativos:

- acesso a um bucket S3 usando AWS SDK;
- acesso a uma API HTTP protegida com usuário, senha, API key e token;
- leitura de propriedades de configuração definidas em `src/main/resources/application.properties`.

O ponto principal da simulação é que esse arquivo `application.properties` contém valores que não deveriam estar versionados:

- `amazon.accessKey`
- `amazon.secretKey`
- `backend.user`
- `backend.password`
- `backend.token`
- `backend.jtw.token`

Esse é exatamente o tipo de erro que acontece quando alguém quer "fazer funcionar rápido", testa localmente, esquece de externalizar os segredos e faz commit.

## 2. Estrutura geral do projeto

Os arquivos mais importantes deste repositório são:

- `pom.xml`
- `Dockerfile`
- `src/main/resources/application.properties`
- `src/main/java/com/example/gitleaks/GitleaksApplication.java`
- `src/main/java/com/example/gitleaks/HealthController.java`
- `src/main/java/com/example/gitleaks/S3DocumentStorageService.java`
- `src/main/java/com/example/gitleaks/BackendApiAccessService.java`

## 3. Explicando para quem não conhece Maven

Maven é uma ferramenta de build para projetos Java.

Na prática, ele faz algumas tarefas importantes:

- baixa bibliotecas externas do projeto;
- organiza o padrão de pastas;
- compila o código Java;
- empacota a aplicação em um `.jar`;
- pode rodar testes;
- integra com frameworks como Spring Boot.

O arquivo central do Maven é o `pom.xml`.

Nesse arquivo ficam definidos:

- o nome do projeto;
- a versão do projeto;
- a versão do Java usada;
- as dependências externas;
- os plugins de build.

Neste projeto, o `pom.xml` declara dependências como:

- `spring-boot-starter-web`, para criar a API HTTP;
- `spring-boot-starter-test`, para testes;
- `software.amazon.awssdk:s3`, para simular um cliente real de S3.

Quando o build roda, o Maven:

1. lê o `pom.xml`;
2. baixa as dependências necessárias;
3. compila o código em `src/main/java`;
4. empacota tudo em um arquivo `.jar`;
5. esse `.jar` depois é executado dentro do container Docker.

## 4. Como o Spring Boot usa o `application.properties`

Em projetos Spring Boot, o arquivo `application.properties` é uma forma comum de declarar configurações.

Por exemplo:

```properties
server.port=8080
amazon.accessKey=...
backend.user=...
```

Essas propriedades podem ser injetadas nas classes Java usando `@Value`.

Exemplo conceitual:

```java
@Value("${backend.user}")
private String user;
```

Isso significa:

- o Spring sobe a aplicação;
- lê o `application.properties`;
- encontra a chave `backend.user`;
- injeta esse valor na variável Java.

Neste repositório, isso foi usado em classes como:

- `AmazonCredentialsSimulator`
- `BackendCredentialsSimulator`
- `S3DocumentStorageService`
- `BackendApiAccessService`

Ou seja, não é apenas um arquivo solto com segredos. As propriedades realmente são consumidas por classes que simulam comportamento de produção.

## 5. O que as classes estão simulando

### `S3DocumentStorageService`

Essa classe simula um padrão muito comum:

- ler `amazon.accessKey` e `amazon.secretKey`;
- montar um `S3Client` do AWS SDK;
- preparar um `PutObjectRequest`;
- enviar um arquivo JSON para um bucket S3.

Mesmo que aqui o código esteja apenas simulando o fluxo, a estrutura é realista:

- cria credenciais AWS;
- define região;
- monta o cliente S3;
- define bucket, chave do objeto e conteúdo.

Esse tipo de classe justifica perfeitamente por que um dev poderia ter criado credenciais da AWS no `application.properties`.

### `BackendApiAccessService`

Essa classe simula outro padrão muito comum em sistemas internos:

- usar `backend.user` e `backend.password` para `Basic Auth`;
- usar `backend.token` como API key;
- usar `backend.jtw.token` como token adicional de acesso;
- montar um cliente HTTP com headers de autenticação.

Esse cenário também é bastante plausível no dia a dia:

- uma API interna exige usuário e senha;
- além disso exige um header com API key;
- além disso exige um token específico para acesso.

## 6. Por que isso é um problema grave

Quando um segredo vai para o repositório, o problema não é só o arquivo atual.

O verdadeiro problema é o histórico Git.

Mesmo que alguém depois:

- apague a senha;
- altere o arquivo;
- troque o valor;

o segredo pode continuar existindo em commits antigos.

Isso significa que:

- qualquer pessoa com acesso ao histórico pode recuperar o valor;
- ferramentas de varredura conseguem identificar o vazamento;
- o segredo pode já ter sido copiado por pipelines, forks, caches ou espelhos do repositório.

## 7. Onde está o erro neste repositório

O erro intencional deste demo está em:

- `src/main/resources/application.properties`

Ali existem valores com formato de credenciais reais ou plausíveis.

Esse padrão é exatamente o tipo de ocorrência que ferramentas de secret scanning procuram:

- AWS Access Key;
- tokens longos;
- senhas hardcoded;
- chaves com nomes suspeitos como `secret`, `password`, `token`.

## 8. Como o Gitleaks entra nessa história

Gitleaks é uma ferramenta de detecção de segredos em código e histórico Git.

Ela faz varredura em:

- arquivos do diretório atual;
- conteúdo staged;
- histórico de commits;
- intervalos de commits;
- repositórios inteiros.

Ela usa regras para identificar padrões típicos de segredos, como:

- chaves AWS;
- tokens de API;
- JWTs;
- private keys;
- senhas em contextos suspeitos.

No caso deste projeto, o Gitleaks deveria sinalizar o `application.properties` e também qualquer commit em que esses valores tenham sido introduzidos.

## 9. Como usar o Gitleaks para escanear o projeto

Se o Gitleaks estiver instalado na máquina, um uso simples seria:

```bash
gitleaks detect --source .
```

Esse comando analisa o repositório atual.

Se você quiser saída em JSON para inspecionar melhor:

```bash
gitleaks detect --source . --report-format json --report-path gitleaks-report.json
```

Se a intenção for focar no histórico Git, o fluxo mais importante continua sendo o `detect`, porque ele analisa os commits do repositório.

Dependendo da versão/configuração do Gitleaks, você também pode usar opções como:

```bash
gitleaks detect --source . --verbose
```

O resultado normalmente informa:

- tipo do segredo detectado;
- arquivo afetado;
- linha aproximada;
- commit em que o segredo apareceu;
- regra que acionou o alerta.

## 10. O que procurar no resultado do Gitleaks

Neste repositório, o esperado é que o Gitleaks identifique indícios relacionados a:

- `amazon.accessKey`
- `amazon.secretKey`
- `backend.password`
- `backend.token`
- `backend.jtw.token`

Na prática, algumas descobertas podem vir por:

- regex específica de AWS Access Key;
- regex de token;
- contexto de nomes como `password`, `secret`, `token`.

O mais importante aqui não é decorar a regra exata, mas entender o princípio:

- se um segredo entrou no código ou no histórico, o Gitleaks foi feito para encontrar isso.

## 11. Como um desenvolvedor normalmente causaria esse problema

Um fluxo comum seria:

1. o dev cria a API;
2. precisa testar integração com AWS ou outro serviço;
3. coloca as credenciais direto no `application.properties`;
4. sobe localmente e confirma que funciona;
5. faz commit sem revisar o que entrou;
6. envia para o repositório remoto.

Esse erro é especialmente comum quando:

- a pessoa está aprendendo Spring Boot;
- o time não usa secret manager;
- não existe revisão de segurança;
- ninguém roda scanner de segredos no CI;
- o projeto nasceu pequeno e depois cresceu.

## 12. Forma correta: usar variáveis de ambiente

O jeito correto é não versionar os segredos.

Em vez disso:

- o código espera valores vindos do ambiente;
- cada ambiente injeta seus próprios segredos;
- o repositório guarda no máximo placeholders ou exemplos.

No Spring Boot, isso é simples porque o framework já resolve propriedades a partir de variáveis de ambiente.

Por exemplo, você pode trocar algo assim:

```properties
amazon.accessKey=AKIA...
amazon.secretKey=abc123...
backend.password=minha-senha
```

por algo assim:

```properties
amazon.accessKey=${AMAZON_ACCESS_KEY}
amazon.secretKey=${AMAZON_SECRET_KEY}
backend.user=${BACKEND_USER}
backend.password=${BACKEND_PASSWORD}
backend.token=${BACKEND_API_KEY}
backend.jtw.token=${BACKEND_JWT_TOKEN}
```

Agora o arquivo continua existindo, mas os valores reais não ficam hardcoded.

O que o Spring faz nesse caso:

- lê `${AMAZON_ACCESS_KEY}`;
- procura a variável de ambiente `AMAZON_ACCESS_KEY`;
- injeta o valor real somente em tempo de execução.

## 13. Exemplo local com variáveis de ambiente

Num ambiente local Linux ou macOS, poderia ser algo como:

```bash
export AMAZON_ACCESS_KEY="valor-real"
export AMAZON_SECRET_KEY="valor-real"
export BACKEND_USER="svc_backend"
export BACKEND_PASSWORD="valor-real"
export BACKEND_API_KEY="valor-real"
export BACKEND_JWT_TOKEN="valor-real"
```

Depois disso, a aplicação pode subir sem que o segredo esteja no repositório.

## 14. Exemplo com GitHub Actions

Em GitHub Actions, o padrão recomendado é guardar os valores em `Secrets` do repositório ou da organização.

Exemplo conceitual:

```yaml
name: build

on:
  push:
    branches: [main]

jobs:
  build:
    runs-on: ubuntu-latest

    env:
      AMAZON_ACCESS_KEY: ${{ secrets.AMAZON_ACCESS_KEY }}
      AMAZON_SECRET_KEY: ${{ secrets.AMAZON_SECRET_KEY }}
      BACKEND_USER: ${{ secrets.BACKEND_USER }}
      BACKEND_PASSWORD: ${{ secrets.BACKEND_PASSWORD }}
      BACKEND_API_KEY: ${{ secrets.BACKEND_API_KEY }}
      BACKEND_JWT_TOKEN: ${{ secrets.BACKEND_JWT_TOKEN }}

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Set up Java
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: 17

      - name: Build
        run: mvn clean package
```

Nesse modelo:

- o valor real não fica no `application.properties`;
- o valor real não fica no `pom.xml`;
- o valor real não vai para o Git;
- o GitHub injeta o segredo apenas no runtime do job.

## 15. Boas práticas adicionais

Além de usar variáveis de ambiente, um time maduro normalmente também faz o seguinte:

- mantém um `application.properties.example` sem segredos reais;
- adiciona scanners como Gitleaks no pipeline de CI;
- bloqueia merge quando segredo é detectado;
- rotaciona imediatamente qualquer segredo exposto;
- evita nomes e padrões perigosos em commits de teste;
- usa services próprios de secret management quando o ambiente cresce.

## 16. O que corrigir depois que o Gitleaks encontrar o problema

Encontrar o segredo é só o começo.

Depois disso, o time precisa:

1. invalidar ou rotacionar as credenciais expostas;
2. remover os segredos do código atual;
3. revisar o histórico Git, se necessário;
4. adicionar proteção para impedir recorrência;
5. mover os valores para ambiente seguro.

Se o segredo era real, nunca se deve assumir que "ninguém viu".

## 17. Resumo

Este projeto demonstra três coisas ao mesmo tempo:

1. como um backend Maven/Spring Boot pode consumir propriedades do `application.properties`;
2. como um dev pode cometer o erro de versionar credenciais reais ou plausíveis;
3. como o Gitleaks ajuda a detectar esse problema e por que a solução correta é externalizar segredos por variáveis de ambiente e `secrets` no CI.

Se a intenção for usar este repositório como laboratório, o fluxo didático é:

1. analisar o `application.properties`;
2. observar as classes que consomem essas propriedades;
3. rodar o Gitleaks contra o repositório;
4. refatorar a configuração para `${NOME_DA_VARIAVEL}`;
5. injetar os valores reais por ambiente, nunca por commit.

## 18. Resultado do uso do Gitleaks

Rodando o comando abaixo:

```bash
gitleaks detect --source . --report-format json --report-path gitleaks-report.json
```

Gerou o seguinte resultado:

```bash

    ○
    │╲
    │ ○
    ○ ░
    ░    gitleaks

8:08PM INF 1 commits scanned.
8:08PM INF scanned ~25594 bytes (25.59 KB) in 222ms
8:08PM WRN leaks found: 4
```
Isso gera na raiz do projeto um arquivo chamado gitleaks-report.json com todos os segredos encontrados:

```json
fribeiro@Note-latitude-3400:gitleaks$ cat gitleaks-report.json 
[
 {
  "RuleID": "generic-api-key",
  "Description": "Detected a Generic API Key, potentially exposing access to various services and sensitive operations.",
  "StartLine": 4,
  "EndLine": 4,
  "StartColumn": 2,
  "EndColumn": 38,
  "Match": "amazon.accessKey=AKIA7Q9M2X4L8N5R1T6V",
  "Secret": "AKIA7Q9M2X4L8N5R1T6V",
  "File": "src/main/resources/application.properties",
  "SymlinkFile": "",
  "Commit": "ce03ef3bf27c7d15f4469a765360361530aa13f4",
  "Entropy": 4.221928,
  "Author": "Franklin Ribeiro",
  "Email": "franklin.ribe@gmail.com",
  "Date": "2026-04-08T23:08:45Z",
  "Message": "feat(gitleaks): Commit de teste do gitleks",
  "Tags": [],
  "Fingerprint": "ce03ef3bf27c7d15f4469a765360361530aa13f4:src/main/resources/application.properties:generic-api-key:4"
 },
 {
  "RuleID": "generic-api-key",
  "Description": "Detected a Generic API Key, potentially exposing access to various services and sensitive operations.",
  "StartLine": 5,
  "EndLine": 5,
  "StartColumn": 2,
  "EndColumn": 58,
  "Match": "amazon.secretKey=2mJ8xQ7pLs9Vn4RtYw3Kc6HdZa1UfNe5Bx0GqPrS",
  "Secret": "2mJ8xQ7pLs9Vn4RtYw3Kc6HdZa1UfNe5Bx0GqPrS",
  "File": "src/main/resources/application.properties",
  "SymlinkFile": "",
  "Commit": "ce03ef3bf27c7d15f4469a765360361530aa13f4",
  "Entropy": 5.2719283,
  "Author": "Franklin Ribeiro",
  "Email": "franklin.ribe@gmail.com",
  "Date": "2026-04-08T23:08:45Z",
  "Message": "feat(gitleaks): Commit de teste do gitleks",
  "Tags": [],
  "Fingerprint": "ce03ef3bf27c7d15f4469a765360361530aa13f4:src/main/resources/application.properties:generic-api-key:5"
 },
 {
  "RuleID": "generic-api-key",
  "Description": "Detected a Generic API Key, potentially exposing access to various services and sensitive operations.",
  "StartLine": 9,
  "EndLine": 9,
  "StartColumn": 2,
  "EndColumn": 51,
  "Match": "backend.token=glk_api_7fA92kLmQx4Pz8Nw3Ts1Yv6Hd0Rb",
  "Secret": "glk_api_7fA92kLmQx4Pz8Nw3Ts1Yv6Hd0Rb",
  "File": "src/main/resources/application.properties",
  "SymlinkFile": "",
  "Commit": "ce03ef3bf27c7d15f4469a765360361530aa13f4",
  "Entropy": 5.058814,
  "Author": "Franklin Ribeiro",
  "Email": "franklin.ribe@gmail.com",
  "Date": "2026-04-08T23:08:45Z",
  "Message": "feat(gitleaks): Commit de teste do gitleks",
  "Tags": [],
  "Fingerprint": "ce03ef3bf27c7d15f4469a765360361530aa13f4:src/main/resources/application.properties:generic-api-key:9"
 },
 {
  "RuleID": "jwt",
  "Description": "Uncovered a JSON Web Token, which may lead to unauthorized access to web applications and sensitive user data.",
  "StartLine": 10,
  "EndLine": 10,
  "StartColumn": 20,
  "EndColumn": 180,
  "Match": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJnZXJlbmF0ZWQtdXNlciIsInJvbGUiOiJiYWNrZW5kLWFwaSIsImlhdCI6MTcxMjU3MjgwMH0.c2lnbmF0dXJlLWZha2UtZ2VuZXJhdGVkLXRva2Vu",
  "Secret": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJnZXJlbmF0ZWQtdXNlciIsInJvbGUiOiJiYWNrZW5kLWFwaSIsImlhdCI6MTcxMjU3MjgwMH0.c2lnbmF0dXJlLWZha2UtZ2VuZXJhdGVkLXRva2Vu",
  "File": "src/main/resources/application.properties",
  "SymlinkFile": "",
  "Commit": "ce03ef3bf27c7d15f4469a765360361530aa13f4",
  "Entropy": 5.35309,
  "Author": "Franklin Ribeiro",
  "Email": "franklin.ribe@gmail.com",
  "Date": "2026-04-08T23:08:45Z",
  "Message": "feat(gitleaks): Commit de teste do gitleks",
  "Tags": [],
  "Fingerprint": "ce03ef3bf27c7d15f4469a765360361530aa13f4:src/main/resources/application.properties:jwt:10"
 }
]
```