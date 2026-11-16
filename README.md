# foj-ai-service

FOJ 在线判题平台的 AI 编程助手微服务,基于 Spring Boot 3 + Spring AI,对接智谱 GLM-4-Flash(OpenAI 兼容协议),提供流式对话、多轮记忆、会话隔离与按 IP 限流能力。

## 项目简介

- **定位**:独立 AI 微服务,不依赖 FOJ 主仓库;通过 RabbitMQ 接收判题完成事件,HTTP 调用 FOJ 老服务接口。
- **技术栈**:JDK 17 + Spring Boot 3.3.13 + Spring AI 1.1.8 + Spring Cloud Alibaba 2023.0.3.3 + MySQL + RabbitMQ + bucket4j。
- **模型**:glm-4-flash(智谱,免费模型,经 OpenAI 兼容协议接入;模型名 / API Key 一律从配置读取,代码中不写死)。

## 核心功能

- **SSE 流式对话**:`POST /ai/chat/stream` 以 `text/event-stream` 逐段推送回复(打字机效果)。
- **多轮记忆(JDBC 持久化)**:会话历史落库 MySQL,应用重启后记忆不丢失。
- **会话隔离**:同一 `conversationId` 的多轮请求共享上下文,不同会话互不干扰。
- **按 IP 限流**:bucket4j 令牌桶,每 IP 每分钟 10 次(可配置),超限返回 429 + 统一 JSON。
- **全链路优雅降级**:AI 调用超时 / 异常返回占位文案,不抛异常、不阻塞调用方,不影响 MQ 消费确认。

## 架构说明

- **stateless / memory 双 ChatClient**:`/ai/test` 走无记忆的 `statelessChatClient`;多轮与流式走挂载 `MessageChatMemoryAdvisor` 的 `memoryChatClient`,职责隔离,避免无状态接口误写记忆。
- **MessageWindowChatMemory 窗口 20 条**:每次请求携带历史消息发给模型,窗口既控制 token 成本,又防止上下文溢出。
- **refillGreedy 匀速回血**:令牌桶按窗口匀速补充令牌,容量 10、窗口 60s(即 10 次 / 分钟,可配置 `ai.rate-limit.*`)。
- **HikariCP 30s 连接超时 → catch 降级**:AI 底层 RestClient 显式配置连接 5s / 读取 30s;MySQL 挂掉时由 HikariCP 超时 + service 层 catch 兜底,返回降级文案而非 500。

## 接口一览

| 方法 | 路径 | 说明 |
|---|---|---|
| GET | /health | 健康检查 |
| POST | /ai/test | 单轮测试(无记忆) |
| POST | /ai/chat | 同步多轮对话(带记忆) |
| POST | /ai/chat/stream | SSE 流式多轮对话(带记忆) |

## 运行手册

### 1. 环境变量(仅占位符,禁止硬编码)

| 变量 | 必填 | 说明 |
|---|---|---|
| `AI_API_KEY` | ✅ | 智谱 API Key |
| `MYSQL_PASSWORD` | ✅ | MySQL 密码 |
| `MYSQL_USERNAME` | 否 | MySQL 用户名,默认 `root` |
| `AI_BASE_URL` | 否 | 默认 `https://open.bigmodel.cn/api/paas` |
| `AI_MODEL` | 否 | 默认 `glm-4-flash` |
| `AI_COMPLETIONS_PATH` | 否 | 默认 `/v4/chat/completions` |
| `NACOS_ADDR` | 否 | 默认 `127.0.0.1:8848` |
| `RABBITMQ_HOST` | 否 | 默认 `localhost` |

### 2. MySQL 建库

```sql
CREATE DATABASE IF NOT EXISTS foj_ai DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. schema 自动建表

启动时应用自动执行 `src/main/resources/db/schema-mysql.sql`(`spring.sql.init.mode=always`),该脚本为 Spring AI 官方 schema(`IF NOT EXISTS`,幂等),创建 `SPRING_AI_CHAT_MEMORY` 表,无需手工建表。

### 4. 启动步骤

1. 启动 MySQL,并执行上面的建库语句;
2. 设置环境变量 `AI_API_KEY`、`MYSQL_PASSWORD`;
3. (按需)启动 Nacos / RabbitMQ;
4. 启动应用:`mvn spring-boot:run`,或在 IDEA 中运行 `AiServiceApplication`。

## Known Issues

1. **glm-4-flash 输出非确定性**:模型为概率生成,同一输入可能得到不同输出,属正常现象。
2. **空会话可能虚构示例名字**:全新会话中询问"我叫什么名字?"时,弱模型可能按 few-shot 模板虚构一个名字(已加入反例约束,但仍可能偶发)。
3. **AI 功能验收判"存在性"而非"每次性"**:由于输出非确定性,验收时以"能力存在、降级生效"为准,而非要求每次结果完全一致。
