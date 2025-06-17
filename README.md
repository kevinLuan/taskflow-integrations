taskflow-integrations
============
<div align="left">
  <a href="javascript:void(0);"><img src="https://img.shields.io/badge/build-passing-brightgreen" /></a>
  <a href="javascript:void(0);" target="_blank"><img src="https://img.shields.io/badge/docs-latest-brightgreen" /></a>
  <a href="https://javadoc.io/doc/cn.taskflow/taskflow-sdk/latest/index.html" target="_blank"><img src="https://javadoc.io/badge/cn.taskflow/taskflow-sdk/0.1.7-beta.svg" /></a>
  <a href="https://www.apache.org/licenses/LICENSE-2.0"><img src="https://img.shields.io/badge/License-Apache%202.0-blue.svg" alt="License"></a>
  <a href="https://central.sonatype.com/artifact/cn.taskflow/taskflow-sdk?smo=true"><img src="https://img.shields.io/maven-metadata/v.svg?label=Maven%20Central&metadataUrl=https%3A%2F%2Frepo1.maven.org%2Fmaven2%2Fcn%2Ftaskflow%2Ftaskflow-sdk%2Fmaven-metadata.xml" alt="License"></a>
</div>

[简体中文](./README-zh_CN) | English 

# overview

Taskflow is a powerful and flexible task orchestration-based workflow/task flow management platform designed to streamline your workflow and boost productivity. As a flowchart-based task scheduling and execution system, Taskflow helps businesses and teams achieve automated, visualized business process management. Users can easily design and execute various complex workflows, thereby improving productivity and work efficiency. Taskflow integrates various tasks, processes, data, and systems into a unified workflow platform, enabling users to easily create, manage, and monitor a wide range of complex workflows.

## Documentation

For detailed documentation, please refer to our official docs [official docs](http://www.taskflow.cn).

This SDK provides a convenient Java client interface for [Task Cloud](http://www.taskflow.cn/). Through it, you can easily access our cloud service API, automate the management of resources and services and other features.

## Installation

To integrate TaskFlow into your Java project.

The Maven project adds the following dependency to your 'pom.xml' file：

```xml
<dependency>
    <groupId>cn.taskflow</groupId>
    <artifactId>taskflow-sdk</artifactId>
    <version>latest</version>
</dependency>
```

Add the following dependencies to the gradle project:
```groovy
    dependencies {
        implementation 'cn.taskflow:taskflow-sdk:latest'
    }
```

## Questions
For questions and support, visit [Task Cloud Platform](http://www.taskflow.cn/).

## License

[License Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0)

Copyright (c) 2024 Taskflow