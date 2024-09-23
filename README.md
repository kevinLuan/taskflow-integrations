# TaskFlow Open API
[![Build Status](https://travis-ci.org/taskflow/taskflow-open-api.svg?branch=master)](https://travis-ci.org/taskflow/taskflow-open-api)
[![Coverage Status](https://coveralls.io/repos/github/taskflow/taskflow-open-api/badge.svg?branch=master)](https://coveralls.io/github/taskflow/taskflow-open-api?branch=master)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

#### 1. 简介
   本 SDK 为 [任务云](http://www.taskflow.cn/) 提供了便捷的 Java 客户端接口。通过它，您可以轻松地访问我们的云服务API，实现自动化管理资源和服务等功能。
#### 2. 安装
   2.1 Maven
   在您的pom.xml文件中添加以下依赖：
```xml
    <dependency>
        <groupId>cn.taskflow</groupId>
        <artifactId>taskflow-sdk</artifactId>
        <version>0.0.1-beta</version>
    </dependency>
```   
#### 2.2 Gradle
    在您的build.gradle文件中添加以下依赖：
```groovy
    dependencies {
        implementation 'cn.taskflow:taskflow-sdk:1.0.0'
    }
```

#### 3. 快速开始
    
##### 3.1 初始化客户端

```java

String basePath = "http://www.taskflow.cn/api";
String keyId = $YourKeyId;
String keySecret = $YourKeySecret;
// 创建客户端
ApiClient apiClient = new ApiClient(basePath, keyId, keySecret);
// 使用GRPC
apiClient.setUseGRPC("www.taskflow.cn", 9000);

```

##### 3.2 使用客户端

```java
import cn.feiliu.taskflow.client.ApiClient;
import cn.feiliu.taskflow.sdk.workflow.def.tasks.SimpleTask;
import cn.feiliu.taskflow.sdk.workflow.task.InputParam;
import cn.feiliu.taskflow.sdk.workflow.task.WorkerTask;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class Examples {

   public static void main(String[] args) {
      ApiClient client = BaseClientApi.getApiClient();
      //添加自定义任务到任务云平台并启动任务拉取
      client.getTaskEngine()
              .addWorkers(new MyWorkers())
              .startPolling();

      //创建工作流
      WorkflowEngine<Map<String, Object>> workflow = client.newWorkflowBuilder("test-workflow", 1)
              //添加任务到工作流
              .add(new SimpleTask("test_task", "test_task_ref").input("msg", "${workflow.input.msg}"))
              .build();
      //注册工作流定义
      workflow.registerWorkflow();
      //运行工作流
      CompletableFuture<ExecutingWorkflow> future = workflow.execute(Map.of("msg", "测试"));
      future.thenAccept((w) -> {
         System.out.println("workflowName: " + w.getWorkflowName() + ", workflowId: " + w.getWorkflowId());
      }).join();
      System.out.println("done");
   }

   public static class MyWorkers {
      @WorkerTask(value = "test_task")
      public String testTask(@InputParam("msg") Object msg) {
         return "echo:" + msg;
      }
   }
}


```

#### 4. API 文档
   [API 文档连接](http://www.taskflow.cn/api-docs)

#### 5. 常见问题
    Q: 如何获取 keyId 和 keySecret ？
    A: 您可以在控制台的安全设置页面找到或创建您的 keyId 和 keySecret。
    Q: SDK支持哪些版本的Java？
    A: 当前SDK支持 Java 8及以上版本。
    Q: 遇到未知错误怎么办？
    A: 请检查您的网络连接，并确保使用的 API 参数正确无误。如果问题仍然存在，请联系技术支持。
    
#### 6. 联系我们
    如果您在使用过程中遇到任何问题或有任何建议，请随时联系我们:
    邮箱: kevin_luan@126.com
    官网: http://www.taskflow.cn
---

感谢您对 TaskFlow Open API 的关注和支持！ 