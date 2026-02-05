# API Seletivo - Catálogo de Artistas e Álbuns (Spring Boot + Postgres + MinIO)
### Nome: Karla Bomfim Castilho
### Inscrição: 16335
### Vaga: Back-End (Engenheiro da Computação)


Este projeto implementa uma API REST versionada (`/api/v1`) para cadastro e consulta de **artistas** (cantor/banda) e **álbuns**, com:
- Persistência em **PostgreSQL**
- Upload de imagens no **MinIO** (compatível com API S3)
- Recuperação de imagens via **presigned URL** (expira em 30 minutos)
- Autenticação **JWT** (access token 5 minutos) com **refresh token**
- Paginação e ordenação
- Documentação via **OpenAPI/Swagger**
- Banco criado e populado via **Flyway Migrations**
- Ambiente containerizado via **Docker Compose**

---

## Arquitetura

- **API**: Spring Boot (REST)
- **Banco**: PostgreSQL
- **Storage**: MinIO (S3)
- **Migrações**: Flyway
- **Docs**: Swagger UI (OpenAPI)
- **Segurança**: JWT + refresh token (persistido no banco)
- **Infra**: Docker Compose orquestrando serviços

---
### Endereços importantes

- **Swagger UI:** http://localhost:8080/swagger-ui/index.html

- **OpenAPI JSON:** http://localhost:8080/v3/api-docs

- **MinIO Console:** http://localhost:9001

  - usuário: minio
  - senha: minio123

- **MinIO API:** http://localhost:9000
- **API Base:** http://localhost:8080/api/v1

# Como rodar o projeto

### Pré-requisitos
- Docker + Docker Compose instalados

### Subir tudo
Na raiz do projeto:

```bash
docker compose up --build
```


## Estrutura de dados (modelagem)
artists:
- id (PK)
- name
- type (CANTOR | BANDA)

albums:
- id (PK)
- title
- cover_object_key (capa principal no MinIO)

artist_album:
Tabela de relacionamento N:N entre artistas e álbuns.

album_images:
Permite uma ou mais imagens por álbum:

- id (PK)
- album_id (FK)
- object_key (arquivo no MinIO)
- created_at
- app_users

Usuários da aplicação:
- id
- username
- password_hash
- role
- refresh_tokens
- id
- user_id
- token
- expires_at
- revoked

## Segurança
### CORS: 
A API bloqueia requisições vindas de domínios não permitidos através de configuração de CORS (allowlist).

### JWT:
Access token com expiração de 5 minutos.

Refresh token persistido no banco para renovação.

GET é público.

POST/PUT/DELETE exigem autenticação.

## Funcionalidades implementadas

- CRUD de artistas
- CRUD de álbuns
- Relacionamento N:N artista ↔ álbum
- Paginação de álbuns
- Ordenação (asc/desc)
- Filtro por nome de artista
- Upload de uma ou mais imagens por álbum
- Armazenamento no MinIO
- Recuperação via presigned URL (30 minutos)
- Versionamento de API (/api/v1)
- Migrations e seed via Flyway
- Documentação com Swagger

## Como testar
1) **Criar usuário:** POST /api/v1/auth/register
2) **Login:** POST /api/v1/auth/login

Copiar o accessToken.

3) **Autorizar** no Swagger: Clicar em Authorize e informar
  ```bash
Bearer
```
4) **Testar endpoints**

Exemplos:

Listar artistas: GET /api/v1/artists

Listar álbuns com paginação: GET /api/v1/albums?page=0&size=10

Ordenar: GET /api/v1/albums?sort=title,asc

Upload de imagem: PUT /api/v1/albums/{id}/images

Gerar URL temporária: GET /api/v1/albums/{id}/images/{imageId}/url

## Decisões técnicas

- JWT com refresh token persistido para maior controle e segurança.
- MinIO com presigned URL para evitar exposição direta de arquivos.
- Flyway para versionamento e reprodutibilidade do banco.
- Docker Compose para isolamento completo do ambiente.
- GET público e escrita protegida para melhor experiência e segurança.

## Decisões de escopo e priorização

Durante o desenvolvimento do projeto, optei por priorizar a implementação completa e consistente dos requisitos fundamentais da aplicação, garantindo:

- Arquitetura organizada em camadas;
- Segurança com JWT Stateless;
- Rate limiting;
- CORS configurado;
- Armazenamento externo via MinIO com presigned URLs;
- Paginação e ordenação;
- Versionamento de Banco com Flyway;
- Containerização total com Docker Compose;
- Documentação via OpenAPI/Swagger;
- Health check com Actuator.

Alguns requisitos avançados não foram implementados nesta versão por decisão estratégica de priorização do escopo. Abaixo explico cada um deles:

1) Testes Unitários e de Integração:
Embora a estrutura da aplicação esteja preparada para testes (com separação de camadas e DTOs desacoplados), optei por concentrar esforçoes na consolidação da arquitetura, segurança e infraestrutura.
A estrutura atual permite facilmente inclusão desses testes.
2) WebSocket: 
O requisito de WebSocket foi analisado, porém o domínio principal da aplicação (gestão de artistas e álbuns) não exige comunicação em tempo real para seu funcionamento básico.
A implementação de WebSocket seria aplicável em cenários como:
   - Notificação em tempo real dos novos álbuns;
   - Atualizações em dashboards administrativos.
     Como o foco desta entrega foi consolidar a arquitetura REST robusta, a funcionalidade de WebSocket foi considerada uma extensão futura.
3) Endpoint de Regionais e Sincronização:
A integração com endpoint externo de regionais envolveria consumo de API externa, sincronização incremental e controle de versionamento lógico (ativo/inativo).
Por envolver dependência externa e lógica adicional de sincronização, foi priorizada a estabilidade dos módulos centrais da aplicação.
A arquitetura atual permite adicionar facilmente esse módulo em uma próxima etapa, inclusive com abordagem orientada a eventos ou jobs agendados.




