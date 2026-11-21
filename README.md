# order-client

**API Gateway** da plataforma de pedidos: REST na borda, gRPC por dentro. Visão geral e como rodar tudo: **[order-platform](https://github.com/MatheusSaleh/order-platform)**.

![Plataforma em funcionamento](https://raw.githubusercontent.com/MatheusSaleh/order-platform/main/docs/demo.gif)

<sub>O order-client é a porta de entrada: converte REST em gRPC e transmite os eventos (SSE) que animam a interface.</sub>

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/api/orders` | Cria um pedido (JSON → Protobuf → gRPC `PlaceOrder`) |
| `GET` | `/api/orders/{id}` | Status do pedido via gRPC `GetOrderStatus` |
| `GET` | `/api/events/stream` | **SSE** com os eventos de todos os pedidos + métricas a cada 1s |
| `GET` | `/api/catalog` | Catálogo de produtos de exemplo |
| `POST` | `/api/simulation/burst` | Dispara N pedidos aleatórios (virtual threads) |
| `GET/PUT` | `/api/simulation/worker` | Lê/altera o worker (ligado, taxa de falha, tempo de processamento) |
| `POST` | `/api/simulation/dlq/redrive` | Devolve os pedidos da DLQ para a fila principal |

Erros gRPC são convertidos em HTTP (`NOT_FOUND` → 404, `INVALID_ARGUMENT` → 400, `ALREADY_EXISTS` → 409, `UNAVAILABLE` → 503) no formato RFC 7807.

## Proteções

- Validação de todos os campos (tamanho, intervalo e caracteres permitidos)
- Máximo de conexões SSE (`SSE_MAX_CONNECTIONS`, padrão 200)
- Simulador: rajada de no máximo `SIMULATION_MAX_BURST` (50) pedidos, teto global de `SIMULATION_MAX_IN_FLIGHT` (150) pedidos simulados em andamento e auto-reset do worker após `SIMULATION_AUTO_RESET_SECONDS` (180 s)
- `SIMULATION_ENABLED=false` remove todos os endpoints `/api/simulation/**`
- Escrita vinda de outra origem é recusada (CORS)

## Como os eventos chegam ao navegador

Os serviços publicam no exchange `order.events` (topic). O order-client assina `order.#` com uma fila exclusiva e temporária e repassa cada evento por Server-Sent Events. Ele também consulta a profundidade da fila e da DLQ no broker e o estado do worker, e envia essas métricas a cada segundo.

## Rodando localmente

```bash
./mvnw spring-boot:run
```

Variáveis: `ORDER_SERVER_GRPC_ADDRESS` (padrão `localhost:9090`), `ORDER_WORKER_URL`, `RABBITMQ_*`, `CORS_ALLOWED_ORIGINS`, `SIMULATION_*`, `SSE_MAX_CONNECTIONS`.

Stack: Java 21, Spring Boot 4.2 (`spring-boot-starter-grpc-client`), Spring AMQP 4, Jackson 3.
