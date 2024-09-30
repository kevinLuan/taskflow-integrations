## 项目介绍

TaskFlow GRPC Proto 是一个基于 Protocol Buffers 的 gRPC 服务定义项目。它旨在通过定义清晰的服务接口和消息结构来支持任务流控制系统的开发与维护。

## 目录结构

- `proto/model/`: 包含定义 gRPC 服务消息结构。
- `proto/service/`: 包含定义 gRPC 服务接口。

## 快速开始

1. **Generate code and publish to a private server**

```shell
  mvn clean compile install deploy
```   
 
2. maven dependency
```xml
<dependency>
    <groupId>cn.taskflow</groupId>
    <artifactId>taskflow-grpc-proto</artifactId>
    <version>0.0.1-beta</version>
</dependency>
```
