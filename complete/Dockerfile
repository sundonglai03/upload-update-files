FROM maven:3.9.11-eclipse-temurin-17 AS build

# 先只拷 pom.xml 拉依赖：只要 pom.xml 不变，改源码不会重新下载依赖。
# 构建机装了 buildx 后，可以给这两条 RUN 加上 --mount=type=cache,target=/root/.m2。
WORKDIR /app
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

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
