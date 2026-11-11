# foj-ai-service
独立 AI 微服务,不依赖 FOJ 主仓库,通过 RabbitMQ 接收判题完成事件,HTTP 调用 FOJ 老服务接口。
技术栈:JDK 17 + Spring Boot 3.3.13 + Spring AI 1.1.8 + Spring Cloud Alibaba 2023.0.3.3 + RabbitMQ。
红线:
1. 所有敏感配置(DEEPSEEK_API_KEY)走环境变量,禁止硬编码
2. AI 链路必须可降级,超时/异常返回占位文案,不影响 MQ 消费确认
3. 调用 FOJ 老服务的 HTTP 接口路径先问我,不要猜
4. 代码规范:阿里规约,类/方法必须有中文注释
本机环境:JDK 17(ms-17.0.16)、Windows 11、Maven 已装;Nacos 与 RabbitMQ 地址后续我提供实际值再填。
