# Sonic Server 构建配置修复记录

## 文档信息
| 项目 | 内容 |
|------|------|
| 项目名称 | sonic-server |
| 修复日期 | 2026-07-22 |
| 作者 | 自动生成 |

---

## 问题列表

### 问题1: Spring Boot 应用启动失败 - ServletWebServerFactory bean 未定义

**问题描述**：
```
Error starting ApplicationContext.
Web application could not be started as there was no org.springframework.boot.web.servlet.server.ServletWebServerFactory bean defined in the context.
```

**根因分析**：
1. 部分模块（sonic-server-folder）缺少 `spring-boot-starter-web` 依赖
2. `sonic-server-controller` 使用 `spring-boot-starter-websocket` 并排除了 tomcat，但未直接引入 `spring-boot-starter-web`
3. `sonic-server-common` 版本硬编码为 `1.1.2`，与项目版本不一致，导致依赖传递冲突

**解决方案**：
1. 在 `sonic-server-controller` 中显式引入 `spring-boot-starter-web`（排除 tomcat）
2. 在 `sonic-server-folder` 中引入 `spring-boot-starter-web`，并排除 `sonic-server-common` 中的 web 依赖避免冲突

**修改内容**：

| 文件 | 修改内容 |
|------|----------|
| [sonic-server-controller/pom.xml](file:///Users/toad/Developer/AT/toad/sonic-server/sonic-server-controller/pom.xml#L23-L32) | 添加 `spring-boot-starter-web` 依赖，排除 `spring-boot-starter-tomcat` |
| [sonic-server-folder/pom.xml](file:///Users/toad/Developer/AT/toad/sonic-server/sonic-server-folder/pom.xml#L21-L24) | 添加 `spring-boot-starter-web` 依赖 |
| [sonic-server-folder/pom.xml](file:///Users/toad/Developer/AT/toad/sonic-server/sonic-server-folder/pom.xml#L25-L35) | 在 `sonic-server-common` 依赖中添加 `spring-boot-starter-web` 排除 |

**状态**：✅ 已修复

---

### 问题2: Maven 构建失败 - spring-boot-maven-plugin AOT 处理错误

**问题描述**：
```
[ERROR] Failed to execute goal org.springframework.boot:spring-boot-maven-plugin:3.0.3:process-aot (process-aot) on project sonic-server: Process terminated with exit code: 1
```

**根因分析**：
1. 父项目（`packaging=pom`）错误地配置了 `spring-boot-maven-plugin`
2. 父项目是 Maven 聚合项目，没有 Java 源代码，不应该执行 spring-boot-maven-plugin 的 AOT 处理
3. 原配置中的 `mainClass` 值为 `com.example.Application`，是模板默认值，不是本项目的类

**解决方案**：
1. 在父项目中禁用 `spring-boot-maven-plugin`（添加 `<skip>true</skip>`）
2. 在每个子模块中独立配置 `spring-boot-maven-plugin`，指定各自的 `mainClass`

**修改内容**：

| 文件 | 修改内容 |
|------|----------|
| [pom.xml](file:///Users/toad/Developer/AT/toad/sonic-server/pom.xml#L181-L183) | 将 `mainClass` 改为 `<skip>true</skip>`，禁用父项目的插件执行 |
| [sonic-server-controller/pom.xml](file:///Users/toad/Developer/AT/toad/sonic-server/sonic-server-controller/pom.xml#L151-L161) | 添加 `<build>` 配置，指定 `mainClass` 为 `org.cloud.sonic.controller.ControllerApplication` |
| [sonic-server-folder/pom.xml](file:///Users/toad/Developer/AT/toad/sonic-server/sonic-server-folder/pom.xml#L38-L48) | 添加 `<build>` 配置，指定 `mainClass` 为 `org.cloud.sonic.folder.FolderApplication` |
| [sonic-server-gateway/pom.xml](file:///Users/toad/Developer/AT/toad/sonic-server/sonic-server-gateway/pom.xml#L51-L61) | 添加 `<build>` 配置，指定 `mainClass` 为 `org.cloud.sonic.gateway.GatewayApplication` |
| [sonic-server-eureka/pom.xml](file:///Users/toad/Developer/AT/toad/sonic-server/sonic-server-eureka/pom.xml#L49-L59) | 添加 `<build>` 配置，指定 `mainClass` 为 `org.cloud.sonic.eureka.EurekaApplication` |

**状态**：✅ 已修复

---

## 修改文件总览

| 文件路径 | 修改类型 | 问题关联 |
|----------|----------|----------|
| `pom.xml` | 修改 | 问题2 |
| `sonic-server-controller/pom.xml` | 修改 | 问题1、问题2 |
| `sonic-server-folder/pom.xml` | 修改 | 问题1、问题2 |
| `sonic-server-gateway/pom.xml` | 修改 | 问题2 |
| `sonic-server-eureka/pom.xml` | 修改 | 问题2 |

---

## 待处理事项

| 序号 | 事项 | 说明 | 状态 |
|------|------|------|------|
| 1 | 版本统一 | `sonic-server-common` 版本仍为硬编码 `1.1.2`，建议改为 `${current.version}` | ⏳ 待处理 |
| 2 | 依赖排除策略统一 | 各模块对 `spring-boot-starter-web` 的排除策略需保持一致 | ⏳ 待处理 |

---

## 构建验证

**推荐验证命令**：
```bash
cd /Users/toad/Developer/AT/toad/sonic-server
mvn clean install
```

**Native 构建命令**（需在子模块级别执行）：
```bash
mvn -pl sonic-server-controller install -P native
mvn -pl sonic-server-folder install -P native
mvn -pl sonic-server-gateway install -P native
mvn -pl sonic-server-eureka install -P native
```