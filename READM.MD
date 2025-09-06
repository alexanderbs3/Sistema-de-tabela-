# 🏆 Sistema de Tabela de Classificação em Tempo Real

Um sistema completo de leaderboard em tempo real desenvolvido com Java, Spring Boot, PostgreSQL e Redis.

## 📋 Funcionalidades

### ✅ Implementado
- **Autenticação JWT** - Registro e login de usuários
- **Submissão de Pontuações** - Usuários podem enviar scores para diferentes jogos
- **Leaderboards em Tempo Real** - Rankings globais e por jogo usando Redis Sorted Sets
- **Consultas de Ranking** - Posição do usuário, top N, jogadores próximos
- **Relatórios de Performance** - Top performers em períodos específicos
- **Fallback para Banco** - Sistema funciona mesmo se Redis estiver indisponível
- **Sincronização Automática** - Leaderboards são sincronizados periodicamente
- **API RESTful** - Endpoints completos e bem documentados
- **Tratamento de Exceções** - Error handling robusto
- **Testes Unitários** - Cobertura de testes abrangente

## 🚀 Tecnologias Utilizadas

- **Backend**: Java 17, Spring Boot 3.x
- **Banco de Dados**: PostgreSQL
- **Cache/Leaderboard**: Redis
- **Autenticação**: JWT (JSON Web Tokens)
- **Documentação**: OpenAPI/Swagger
- **Testes**: JUnit 5, Mockito, TestContainers

## 🏗️ Arquitetura

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Spring Boot   │    │   PostgreSQL    │
│   (Client)      │◄──►│   Application   │◄──►│   Database      │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                              │
                              ▼
                       ┌─────────────────┐
                       │     Redis       │
                       │  (Leaderboards) │
                       └─────────────────┘
```

## 📦 Instalação e Configuração

### Pré-requisitos
- Java 17 ou superior
- Maven 3.8+
- PostgreSQL 13+
- Redis 6+
- Docker (opcional)

### 1. Clone o repositório
```bash
git clone https://github.com/seu-usuario/leaderboard-system.git
cd leaderboard-system
```

### 2. Configure o banco PostgreSQL
```sql
CREATE DATABASE leaderboard;
CREATE USER leaderboard_user WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE leaderboard TO leaderboard_user;
```

### 3. Configure Redis
```bash
# Ubuntu/Debian
sudo apt install redis-server
sudo systemctl start redis-server

# macOS
brew install redis
brew services start redis

# Docker
docker run -d -p 6379:6379 --name redis redis:latest
```

### 4. Configure variáveis de ambiente
```bash
export DB_USERNAME=leaderboard_user
export DB_PASSWORD=password
export JWT_SECRET=mySecretKey123456789012345678901234567890
export REDIS_HOST=localhost
export REDIS_PORT=6379
```

### 5. Execute a aplicação
```bash
# Compile e execute
mvn clean install
mvn spring-boot:run

# Ou usando Docker Compose
docker-compose up -d
```

A aplicação estará disponível em: `http://localhost:8080`

## 📚 API Endpoints

### Autenticação
```http
POST/api/auth/register
POST /api/auth/login
```

### Jogos
```http
GET  /api/games/public           # Listar todos os jogos
GET  /api/games/public/{id}      # Detalhes de um jogo
```

### Pontuações
```http
POST /api/scores/submit          # Submeter pontuação
GET  /api/scores/my              # Minhas pontuações
GET  /api/scores/my/game/{id}    # Minhas pontuações em um jogo
GET  /api/scores/top/global      # Top pontuações globais
GET  /api/scores/top/game/{id}   # Top pontuações de um jogo
```

### Leaderboards
```http
GET  /api/leaderboard/public/global              # Ranking global
GET  /api/leaderboard/public/game/{id}           # Ranking de um jogo
GET  /api/leaderboard/my/ranking                 # Meu ranking
GET  /api/leaderboard/my/neighbors               # Jogadores próximos
GET  /api/leaderboard/range/global?start=1&end=10 # Faixa específica
```

### Exemplo de Uso

#### 1. Registrar usuário
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "jogador1",
    "email": "jogador1@example.com",
    "password": "minhasenha"
  }'
```

#### 2. Fazer login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "jogador1",
    "password": "minhasenha"
  }'
```

#### 3. Submeter pontuação
```bash
curl -X POST http://localhost:8080/api/scores/submit \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer SEU_JWT_TOKEN" \
  -d '{
    "gameId": 1,
    "score": 1500
  }'
```

#### 4. Ver leaderboard global
```bash
curl http://localhost:8080/api/leaderboard/public/global?limit=10
```

## 🔧 Configuração Avançada

### application.yml personalizado
```yaml
spring:
  profiles:
    active: dev
    
  datasource:
    url: jdbc:postgresql://localhost:5432/leaderboard
    username: ${DB_USERNAME:leaderboard_user}
    password: ${DB_PASSWORD:password}
  
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 10
        max-idle: 10

jwt:
  secret: ${JWT_SECRET:mySecretKey}
  expiration: 86400000 # 24 horas

logging:
  level:
    com.leaderboard: DEBUG
    org.springframework.security: INFO
```

### Docker Compose
```yaml
version: '3.8'
services:
  postgres:
    image: postgres:15
    environment:
      POSTGRES_DB: leaderboard
      POSTGRES_USER: leaderboard_user
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"

  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - DB_USERNAME=leaderboard_user
      - DB_PASSWORD=password
      - REDIS_HOST=redis
    depends_on:
      - postgres
      - redis

volumes:
  postgres_data:
```

## 🧪 Executando Testes

```bash
# Todos os testes
mvn test

# Testes específicos
mvn test -Dtest=LeaderboardServiceTest

# Testes de integração
mvn test -Dtest=LeaderboardIntegrationTest

# Com coverage
mvn jacoco:prepare-agent test jacoco:report
```

## 📈 Monitoramento

### Health Check
```bash
curl http://localhost:8080/api/leaderboard/admin/status
```

### Sincronização Manual
```bash
curl -X POST http://localhost:8080/api/leaderboard/admin/sync \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

## 🔍 Troubleshooting

### Redis não está funcionando
- Verifique se o Redis está rodando: `redis-cli ping`
- Sistema funciona em modo fallback usando apenas PostgreSQL
- Use endpoint de sincronização para restaurar dados no Redis

### Performance
- Configure connection pools adequadamente
- Monitore uso de memória do Redis
- Considere particionamento para high-scale

### Logs
- Logs em `logs/application.log`
- Level DEBUG para desenvolvimento
- Level INFO/WARN para produção

## 🚀 Deploy em Produção

### Variáveis de Ambiente Obrigatórias
```bash
export SPRING_PROFILES_ACTIVE=prod
export DB_USERNAME=prod_user
export DB_PASSWORD=secure_password
export JWT_SECRET=very_secure_secret_key_here
export REDIS_HOST=redis.production.com
export REDIS_PASSWORD=redis_secure_password
```

### Otimizações Recomendadas
- Use pool de conexões otimizado
- Configure Redis clustering para alta disponibilidade
- Implemente rate limiting
- Configure HTTPS
- Use CDN para assets estáticos
- Configure backup automático do PostgreSQL

## 🤝 Contribuição

1. Fork o projeto
2. Crie uma branch para sua feature (`git checkout -b feature/nova-feature`)
3. Commit suas mudanças (`git commit -am 'Adiciona nova feature'`)
4. Push para a branch (`git push origin feature/nova-feature`)
5. Crie um Pull Request

## 📄 Licença

Este projeto está licenciado sob a MIT License - veja o arquivo [LICENSE](LICENSE) para detalhes.

## 👨‍💻 Autor

Desenvolvido por [Seu Nome]

## 🎯 Próximos Passos

- [ ] Interface web (React/Vue.js)
- [ ] WebSocket para atualizações em tempo real
- [ ] Sistema de achievements/conquistas
- [ ] Analytics e métricas avançadas
- [ ] API rate limiting
- [ ] Notificações push
- [ ] Sistema de temporadas/competições

---

**✨ Sistema funcionando! Agora você tem um leaderboard completo e profissional!**