FROM maven:3.9.11-eclipse-temurin-17 AS build

# cache mount 把 Maven 本地仓库（约定路径 /root/.m2）挂到宿主机构建缓存上。
# pom 变动 / 换构建机时依赖命中本地仓库，不再重下；build 阶段会被丢弃，不进最终镜像。
WORKDIR /app
COPY pom.xml ./
RUN --mount=type=cache,target=/root/.m2 mvn -B -q dependency:go-offline
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q package -DskipTests

FROM eclipse-temurin:17-jre-jammy

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --create-home --uid 1000 app

WORKDIR /app
COPY --from=build /app/target/files-upload-download-*.jar app.jar

ENV STORAGE_TYPE=local \
    STORAGE_LOCATION=/data

RUN mkdir -p /data && chown -R app:app /app /data
USER app

VOLUME /data
EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=15s --retries=3 \
    CMD curl --fail --silent http://127.0.0.1:8080/health >/dev/null || exit 1

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
