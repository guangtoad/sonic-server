# Sonic Server 模块分析文档

## 文档信息

| 属性 | 值 |
|------|------|
| 项目名称 | sonic-server |
| 版本 | 2.0.0 |
| 分析日期 | 2026-07-22 |
| 技术栈 | Spring Boot 3.0.3 + Spring Cloud + MySQL |

---

## 一、模块架构总览

### 1.1 模块关系图

```
┌─────────────────────────────────────────────────────────────────┐
│                        sonic-server (父项目)                     │
│                    <packaging>pom</packaging>                    │
└──────────────────────┬──────────────────────────────────────────┘
                       │
        ┌──────────────┼──────────────┐
        ▼              ▼              ▼
┌───────────────┐ ┌───────────────┐ ┌───────────────┐
│   eureka      │ │   gateway     │ │   controller  │
│ (服务注册中心)  │ │ (API网关)      │ │ (核心业务控制) │
└───────────────┘ └───────────────┘ └───────┬───────┘
                       │                    │
                       │                    │
                       ▼                    ▼
              ┌───────────────┐     ┌───────────────┐
              │    folder     │     │   common      │
              │ (文件存储服务)  │     │ (公共组件库)   │
              └───────────────┘     └───────────────┘
```

### 1.2 模块依赖关系表

| 模块 | 依赖模块 | 被依赖模块 |
|------|----------|------------|
| **common** | 无 | controller, eureka, folder, gateway |
| **controller** | common | 无 |
| **eureka** | common | controller, folder, gateway |
| **folder** | common | 无 |
| **gateway** | common | 无 |

---

## 二、依赖库版本与作用

### 2.1 父项目依赖管理（pom.xml）

| GroupId | ArtifactId | Version | 作用 |
|---------|------------|---------|------|
| org.springframework.boot | spring-boot-starter-parent | 3.0.3 | Spring Boot 父级依赖管理 |
| org.springframework.cloud | spring-cloud-dependencies | 2022.0.1 | Spring Cloud 依赖管理 |
| org.cloud.sonic | sonic-server-common | 1.1.2 | 公共组件模块 |

### 2.2 sonic-server-common 依赖

| GroupId | ArtifactId | Version | 作用 |
|---------|------------|---------|------|
| com.github.xiaoymin | knife4j-openapi3-jakarta-spring-boot-starter | 4.1.0 | API 文档生成工具 |
| com.alibaba | fastjson | 2.0.53 | JSON 序列化/反序列化 |
| org.springframework.boot | spring-boot-starter-aop | - | AOP 切面支持 |
| org.springframework.boot | spring-boot-starter-validation | - | 参数校验支持 |
| org.springframework.boot | spring-boot-starter-web | - | Web 应用支持 |
| com.auth0 | java-jwt | 4.4.0 | JWT Token 处理 |
| org.projectlombok | lombok | 1.18.36 | 简化 Java Bean 代码 |

### 2.3 sonic-server-controller 依赖

| GroupId | ArtifactId | Version | 作用 |
|---------|------------|---------|------|
| org.springframework.boot | spring-boot-starter-web | - | Web 应用支持 |
| org.springframework.boot | spring-boot-starter-websocket | - | WebSocket 实时通信 |
| org.springframework.boot | spring-boot-starter-undertow | - | 高性能 Web 服务器 |
| org.springframework.boot | spring-boot-starter-quartz | - | 定时任务调度 |
| org.springframework.boot | spring-boot-starter-security | - | 安全框架 |
| org.springframework.cloud | spring-cloud-starter-netflix-eureka-client | - | Eureka 客户端 |
| org.springframework.cloud | spring-cloud-starter-openfeign | - | 声明式远程调用 |
| mysql | mysql-connector-java | 8.0.32 | MySQL 数据库驱动 |
| com.baomidou | mybatis-plus-boot-starter | 3.5.5 | MyBatis 增强工具 |
| com.gitee.sunchenbin.mybatis.actable | mybatis-enhance-actable | 1.5.0.RELEASE | 数据库自动建表 |
| org.springframework.ldap | spring-ldap-core | 3.0.1 | LDAP 认证支持 |
| org.cloud.sonic | sonic-server-common | 1.1.2 | 公共组件模块 |

### 2.4 sonic-server-eureka 依赖

| GroupId | ArtifactId | Version | 作用 |
|---------|------------|---------|------|
| org.springframework.boot | spring-boot-starter-security | - | Eureka 安全认证 |
| org.springframework.cloud | spring-cloud-starter-netflix-eureka-server | - | Eureka 服务注册中心 |
| org.springframework.boot | spring-boot-starter-undertow | - | 高性能 Web 服务器 |
| org.cloud.sonic | sonic-server-common | 1.1.2 | 公共组件模块 |

### 2.5 sonic-server-folder 依赖

| GroupId | ArtifactId | Version | 作用 |
|---------|------------|---------|------|
| org.springframework.boot | spring-boot-starter-web | - | Web 应用支持 |
| org.springframework.cloud | spring-cloud-starter-netflix-eureka-client | - | Eureka 客户端 |
| org.cloud.sonic | sonic-server-common | 1.1.2 | 公共组件模块 |

### 2.6 sonic-server-gateway 依赖

| GroupId | ArtifactId | Version | 作用 |
|---------|------------|---------|------|
| com.github.xiaoymin | knife4j-gateway-spring-boot-starter | 4.1.0 | Gateway API 文档 |
| org.springframework.cloud | spring-cloud-starter-gateway | - | Spring Cloud Gateway |
| org.springframework.cloud | spring-cloud-starter-netflix-eureka-client | - | Eureka 客户端 |
| org.springframework.cloud | spring-cloud-loadbalancer | - | 负载均衡 |
| org.cloud.sonic | sonic-server-common | 1.1.2 | 公共组件模块 |

---

## 三、程序流程分析

### 3.1 启动流程

#### 3.1.1 Eureka Server 启动流程

```
EurekaApplication.main()
    └── SpringApplication.run()
        ├── 扫描 @SpringBootApplication 包
        ├── 加载 @EnableEurekaServer 注解
        ├── 启动 Eureka 服务注册中心
        └── 监听端口（默认 8761）
```

#### 3.1.2 Controller 启动流程

```
ControllerApplication.main()
    └── SpringApplication.run()
        ├── 扫描组件：controller、common、mybatis-actable
        ├── 加载 MyBatis Mapper
        ├── 启动 Eureka Client（注册到 Eureka）
        ├── 启动 WebSocket 服务
        ├── 启动 Quartz 定时任务
        └── 初始化数据库连接池
```

#### 3.1.3 Gateway 启动流程

```
GatewayApplication.main()
    └── SpringApplication.run()
        ├── 扫描组件：gateway、common.tools
        ├── 启动 Eureka Client（发现服务）
        ├── 加载路由配置
        └── 初始化全局过滤器（AuthFilter）
```

#### 3.1.4 Folder 启动流程

```
FolderApplication.main()
    └── SpringApplication.run()
        ├── 扫描组件：folder、common
        └── 启动 Eureka Client（注册到 Eureka）
```

### 3.2 请求处理流程

#### 3.2.1 用户登录流程

```
前端请求 → Gateway（AuthFilter 放行白名单）→ Controller
                                                    │
                                                    ▼
                                        UsersController.login()
                                                    │
                                                    ▼
                                        UsersServiceImpl.login()
                                                    │
                                            ┌──────┴──────┐
                                            ▼             ▼
                                    本地用户认证     LDAP 用户认证
                                            │             │
                                            └──────┬──────┘
                                                    ▼
                                        JWTTokenTool.getToken()
                                                    │
                                                    ▼
                                        返回 RespModel<token>
```

#### 3.2.2 认证过滤流程（Gateway）

```
前端请求 → Gateway
            │
            ▼
    AuthFilter.filter()
            │
    ┌───────┴───────┐
    ▼               ▼
 白名单检查      Token 检查
    │               │
    └───────┬───────┘
            ▼
    JWTTokenTool.verify()
            │
    ┌───────┴───────┐
    ▼               ▼
 验证通过      验证失败
    │               │
    ▼               ▼
 转发请求      返回 401 错误
```

#### 3.2.3 权限过滤流程（Controller）

```
请求 → PermissionFilter.doFilterInternal()
            │
    ┌───────┴───────┐
    ▼               ▼
 超管检查       权限检查
    │               │
    └───────┬───────┘
            ▼
    ResourcesService.search()
            │
    ┌───────┴───────┐
    ▼               ▼
 资源存在       资源不存在
    │               │
    ▼               ▼
 角色权限检查    返回 404 错误
            │
    ┌───────┴───────┐
    ▼               ▼
 权限通过       权限拒绝
    │               │
    ▼               ▼
 继续处理       返回 403 错误
```

### 3.3 文件上传流程

```
前端上传 → Gateway → Folder
                        │
                        ▼
              UploadController.upload()
                        │
                        ▼
              FileTool.upload()
                        │
                        ▼
              保存文件到服务器
                        │
                        ▼
              返回文件 URL
```

---

## 四、数据流关系

### 4.1 模块间数据流

```
┌─────────────┐     HTTP请求      ┌─────────────┐
│   前端页面   │ ───────────────→ │   Gateway   │
└─────────────┘                   └──────┬──────┘
                                         │ 路由转发
                    ┌─────────────────────┼─────────────────────┐
                    ▼                     ▼                     ▼
            ┌─────────────┐      ┌─────────────┐      ┌─────────────┐
            │  Controller │      │    Folder   │      │    Eureka   │
            └──────┬──────┘      └─────────────┘      └─────────────┘
                   │ 数据库操作
                   ▼
            ┌─────────────┐
            │   MySQL     │
            └─────────────┘
```

### 4.2 Controller 内部数据流

```
Controller层 → Service层 → Mapper层 → MySQL
    │             │             │
    ▼             ▼             ▼
 RequestDTO   Domain实体    SQL语句
    │             │             │
    ▼             ▼             ▼
 ResponseDTO  Domain实体    查询结果
    │             │             │
    └───────┬─────┴─────┬───────┘
            ▼             ▼
      Convert转换     Convert转换
```

### 4.3 数据模型层次

| 层次 | 包路径 | 用途 |
|------|--------|------|
| **Domain** | `models.domain` | 数据库实体模型（与表结构对应） |
| **DTO** | `models.dto` | 数据传输对象（用于接口请求/响应） |
| **Params** | `models.params` | 请求参数封装 |
| **HTTP** | `models.http` | 通用 HTTP 请求对象 |
| **Interfaces** | `models.interfaces` | 枚举接口（状态码、类型定义） |
| **Enums** | `models.enums` | 枚举类 |

---

## 五、核心组件说明

### 5.1 sonic-server-common 核心组件

| 组件 | 路径 | 功能 |
|------|------|------|
| **JWTTokenTool** | `tools/JWTTokenTool.java` | JWT Token 的生成、解析、验证 |
| **RespModel** | `http/RespModel.java` | 统一响应模型 |
| **RespEnum** | `http/RespEnum.java` | 响应状态码枚举 |
| **WebAspect** | `config/WebAspect.java` | AOP 日志注解 |
| **GlobalWebException** | `config/GlobalWebException.java` | 全局异常处理 |
| **BeanTool** | `tools/BeanTool.java` | Bean 对象工具类 |
| **ReflectionTool** | `tools/ReflectionTool.java` | 反射工具类 |

### 5.2 sonic-server-controller 核心组件

| 组件 | 路径 | 功能 |
|------|------|------|
| **ControllerApplication** | `ControllerApplication.java` | 启动类 |
| **UsersController** | `controller/UsersController.java` | 用户管理接口 |
| **UsersServiceImpl** | `services/impl/UsersServiceImpl.java` | 用户服务实现 |
| **PermissionFilter** | `config/PermissionFilter.java` | 权限过滤器 |
| **WebSocketConfig** | `config/WebSocketConfig.java` | WebSocket 配置 |
| **TransportServer** | `transport/TransportServer.java` | 设备通信服务 |
| **QuartzJob** | `quartz/QuartzJob.java` | 定时任务 |

### 5.3 sonic-server-gateway 核心组件

| 组件 | 路径 | 功能 |
|------|------|------|
| **GatewayApplication** | `GatewayApplication.java` | 启动类 |
| **AuthFilter** | `config/AuthFilter.java` | 全局认证过滤器 |
| **CORSConfig** | `config/CORSConfig.java` | 跨域配置 |
| **UriWithBracketsFilter** | `config/UriWithBracketsFilter.java` | URL 特殊字符处理 |

### 5.4 sonic-server-folder 核心组件

| 组件 | 路径 | 功能 |
|------|------|------|
| **FolderApplication** | `FolderApplication.java` | 启动类 |
| **UploadController** | `controller/UploadController.java` | 文件上传接口 |
| **FilesController** | `controller/FilesController.java` | 文件管理接口 |
| **FileTool** | `tools/FileTool.java` | 文件操作工具类 |

### 5.5 sonic-server-eureka 核心组件

| 组件 | 路径 | 功能 |
|------|------|------|
| **EurekaApplication** | `EurekaApplication.java` | 启动类 |
| **WebSecurityConfig** | `config/WebSecurityConfig.java` | Eureka 安全配置 |

---

## 六、数据库表关系

### 6.1 核心实体表

| 表名 | 实体类 | 说明 |
|------|--------|------|
| `users` | Users | 用户表 |
| `roles` | Roles | 角色表 |
| `resources` | Resources | 资源表 |
| `role_resources` | RoleResources | 角色-资源关联表 |
| `projects` | Projects | 项目表 |
| `modules` | Modules | 模块表 |
| `test_cases` | TestCases | 测试用例表 |
| `test_suites` | TestSuites | 测试套件表 |
| `steps` | Steps | 步骤表 |
| `elements` | Elements | 元素表 |
| `devices` | Devices | 设备表 |
| `agents` | Agents | Agent 表 |
| `results` | Results | 测试结果表 |
| `result_detail` | ResultDetail | 结果详情表 |
| `jobs` | Jobs | 任务表 |
| `scripts` | Scripts | 脚本表 |
| `packages` | Packages | 安装包表 |
| `versions` | Versions | 版本表 |
| `alert_robots` | AlertRobots | 告警机器人表 |
| `global_params` | GlobalParams | 全局参数表 |
| `conf_list` | ConfList | 配置列表 |

### 6.2 表关系

```
users ────< roles ────< role_resources >──── resources
    │
    └───< projects >──── modules >──── test_cases >──── steps >──── elements
                                      │
                                      └───< test_suites >──── test_suites_test_cases
                                      │
                                      └───< test_suites_devices >──── devices
                                      │
                                      └───< results >──── result_detail
                                      │
                                      └───< jobs
                                      │
                                      └───< scripts
                                      │
                                      └───< packages
                                      │
                                      └───< versions
                                      │
                                      └───< agents
```

---

## 七、关键技术特性

### 7.1 认证与授权

| 特性 | 实现方式 |
|------|----------|
| **Token 机制** | JWT（java-jwt 库） |
| **认证方式** | 本地认证 + LDAP 认证 |
| **权限控制** | 基于角色的访问控制（RBAC） |
| **认证过滤** | Gateway 全局过滤器 |
| **权限过滤** | Controller 过滤器 |

### 7.2 服务架构

| 特性 | 实现方式 |
|------|----------|
| **服务注册** | Eureka Server |
| **服务发现** | Eureka Client |
| **API 网关** | Spring Cloud Gateway |
| **负载均衡** | Spring Cloud LoadBalancer |
| **远程调用** | OpenFeign |

### 7.3 数据持久化

| 特性 | 实现方式 |
|------|----------|
| **ORM 框架** | MyBatis-Plus |
| **数据库类型** | MySQL 8.x |
| **连接池** | HikariCP |
| **自动建表** | mybatis-enhance-actable |

### 7.4 实时通信

| 特性 | 实现方式 |
|------|----------|
| **WebSocket** | Spring WebSocket |
| **设备通信** | 自定义 TransportServer |

### 7.5 定时任务

| 特性 | 实现方式 |
|------|----------|
| **调度框架** | Quartz |
| **任务管理** | QuartzJob + QuartzJobTools |

---

## 八、配置文件说明

### 8.1 配置文件结构

```
src/main/resources/
├── application.yml           # 主配置文件
├── application-jdbc.yml      # 数据库配置
└── application-dev.yml       # 开发环境配置
```

### 8.2 关键配置项

| 配置项 | 说明 | 示例值 |
|--------|------|--------|
| `server.port` | 服务端口 | 8080 |
| `spring.datasource.url` | 数据库连接 URL | jdbc:mysql://192.168.1.201:3306/sonic |
| `spring.datasource.username` | 数据库用户名 | toad |
| `spring.datasource.password` | 数据库密码 | 12345678 |
| `eureka.client.service-url.defaultZone` | Eureka 地址 | http://localhost:8761/eureka/ |
| `sonic.token.secret` | JWT 密钥 | sonic |
| `sonic.token.expireDay` | Token 过期天数 | 7 |
| `sonic.permission.enable` | 是否开启权限管理 | true |
| `sonic.user.register.enable` | 是否允许注册 | true |
| `sonic.user.ldap.enable` | 是否开启 LDAP | false |

---

## 九、待处理问题

| 问题 | 描述 | 状态 |
|------|------|------|
| **版本不一致** | sonic-server-common 版本为 1.1.2，其他模块为 1.11 | ⚠️ 待修复 |
| **依赖排除** | 各模块对 spring-boot-starter-web 的排除策略不一致 | ⚠️ 待统一 |
| **mainClass** | 父项目 spring-boot-maven-plugin 配置了错误的 mainClass | ✅ 已修复 |
| **AOT 构建** | 聚合项目不适合直接执行 native 构建 | ✅ 已修复 |