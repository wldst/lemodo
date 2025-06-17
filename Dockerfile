# 使用 Docker Hub 官方镜像
FROM eclipse-temurin:23-jre-alpine

# 复制应用 JAR 文件
ARG JAR_FILE=./target/*.jar
COPY ${JAR_FILE} app.jar

ARG NEO4J_FILE=D:\show\product\crud\neo4j-data
COPY ${NEO4J_FILE} .

# 设置时区
ENV TZ=Asia/Shanghai

# 启动应用
ENTRYPOINT ["java", "-jar", "/app.jar"]