# 贝尔实验室 Spring 官方推荐镜像 JDK下载地址 https://bell-sw.com/pages/downloads/
FROM bellsoft/liberica-openjdk-rocky:17.0.16-cds
#FROM bellsoft/liberica-openjdk-rocky:21.0.8-cds
#FROM findepi/graalvm:java17-native

LABEL maintainer="YangYi"

RUN mkdir -p /lingtai/nodes/logs \
    /lingtai/nodes/temp \
    /lingtai/skywalking/agent

WORKDIR /lingtai/nodes

ENV SERVER_PORT=9810 LANG=C.UTF-8 LC_ALL=C.UTF-8 JAVA_OPTS=""

EXPOSE ${SERVER_PORT}

ADD ./target/lingtai-nodes.jar ./app.jar

SHELL ["/bin/bash", "-c"]

ENTRYPOINT java -Djava.security.egd=file:/dev/./urandom -Dserver.port=${SERVER_PORT} \
           #-Dskywalking.agent.service_name=lingtai-nodes \
           #-javaagent:/lingtai/skywalking/agent/skywalking-agent.jar \
           -XX:+HeapDumpOnOutOfMemoryError -XX:+UseZGC ${JAVA_OPTS} \
           -jar app.jar

