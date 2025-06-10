# TaskFlow Spring Boot Starter

TaskFlow Spring Boot Starter 提供了与 Spring Boot 应用程序的无缝集成，采用标准的 Spring Boot 配置属性模式。

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>cn.taskflow</groupId>
    <artifactId>taskflow-sdk-spring</artifactId>
    <version>latest-version</version>
</dependency>
```

### 2. 配置应用属性

在 `application.yml` 或 `application.properties` 中配置 TaskFlow：

#### YAML 配置示例

```yaml
taskflow:
  # 是否启用TaskFlow功能 (默认: true)
  enabled: true
  
  # 开发者应用key (必需)
  key-id: your-app-key-id
  
  # 开发者应用秘钥 (必需) 
  key-secret: your-app-secret
  
  # 飞流云平台API地址 (默认: https://developer.taskflow.cn/api)
  base-url: https://developer.taskflow.cn/api
  
  # 是否自动注册不存在的任务 (默认: true)
  auto-register: true
  
  # 是否更新已存在的任务 (默认: true)
  update-existing: true
  
  # WebSocket连接地址 (默认: wss://developer.taskflow.cn)
  web-socket-url: wss://developer.taskflow.cn
```

#### Properties 配置示例

```properties
# 是否启用TaskFlow功能
taskflow.enabled=true

# 开发者应用key (必需)
taskflow.key-id=your-app-key-id

# 开发者应用秘钥 (必需)
taskflow.key-secret=your-app-secret

# 飞流云平台API地址
taskflow.base-url=https://developer.taskflow.cn/api

# 是否自动注册不存在的任务
taskflow.auto-register=true

# 是否更新已存在的任务
taskflow.update-existing=true

# WebSocket连接地址
taskflow.web-socket-url=wss://developer.taskflow.cn
```

### 3. 定义工作任务

```java
@Component
public class MyTaskWorker {
    
    @WorkerTask(value = "hello_task", tag = "示例")
    public String helloTask(String name) {
        return "Hello, " + name + "!";
    }
}
```

### 4. 启动应用

```java
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

## 配置属性详情

| 属性名 | 类型 | 默认值 | 描述 | 必需 |
|--------|------|--------|------|------|
| `taskflow.enabled` | boolean | true | 是否启用TaskFlow功能 | 否 |
| `taskflow.key-id` | String | - | 开发者应用key | 是 |
| `taskflow.key-secret` | String | - | 开发者应用秘钥 | 是 |
| `taskflow.base-url` | String | https://developer.taskflow.cn/api | 飞流云平台API地址 | 否 |
| `taskflow.auto-register` | Boolean | true | 是否自动注册不存在的任务 | 否 |
| `taskflow.update-existing` | Boolean | true | 是否更新已存在的任务 | 否 |
| `taskflow.web-socket-url` | String | wss://developer.taskflow.cn | WebSocket连接地址 | 否 |

## 启用/禁用功能

通过设置 `taskflow.enabled=false` 可以完全禁用 TaskFlow 功能，在禁用状态下：

- 不会创建 `ApiClient` Bean
- 不会注册任何工作任务
- 不会连接到 TaskFlow 服务器

这在不同环境（如测试环境）中非常有用。

## 环境配置示例

### 开发环境 (application-dev.yml)

```yaml
taskflow:
  enabled: true
  key-id: dev-key-id
  key-secret: dev-secret
  base-url: http://localhost:8082/api
  web-socket-url: ws://localhost:8082
```

### 生产环境 (application-prod.yml)

```yaml
taskflow:
  enabled: true
  key-id: ${TASKFLOW_KEY_ID}
  key-secret: ${TASKFLOW_KEY_SECRET}
  base-url: https://developer.taskflow.cn/api
  web-socket-url: wss://developer.taskflow.cn
```

## 故障排除

### 常见错误

1. **TaskFlow keyId不能为空**
   - 确保在配置文件中设置了 `taskflow.key-id`

2. **TaskFlow keySecret不能为空**
   - 确保在配置文件中设置了 `taskflow.key-secret`

3. **Bean创建失败**
   - 检查 `taskflow.enabled` 是否为 `true`
   - 确保所有必需的配置项都已设置 