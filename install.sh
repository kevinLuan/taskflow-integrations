#!/bin/sh

rm -rf .m2/repository/cn/taskflow/taskflow-sdk/
rm -rf .m2/repository/cn/taskflow/taskflow-sdk-spring/
mvn clean compile install -DskipTests
echo "Installing done"