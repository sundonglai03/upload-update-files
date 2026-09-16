# files-upload-download

Spring Boot 文件上传、下载和删除服务，支持本地目录、数据库和 MinIO 三种存储后端。

服务启动只初始化存储，不会清空已有文件或数据库记录。

## Docker 启动

```bash
cd complete
docker compose up -d --build
docker compose logs -f files-upload-download
```

- 页面：`http://127.0.0.1:8080`
- 健康检查：`http://127.0.0.1:8080/health`
- 镜像：`files-upload-download:0.0.1`
- 容器：`files-upload-download`
- 本地文件：`complete/upload-dir/`

默认只绑定宿主机回环地址。需要局域网访问时，把 Compose 端口改成 `8080:8080`，并设置访问令牌：

```bash
export FILES_AUTH_TOKEN='替换为随机长字符串'
docker compose up -d
```

设置后，除 `/health` 外的请求必须携带：

```http
Authorization: Bearer 替换为随机长字符串
```

## 本地开发

```bash
cd complete
mvn clean test
mvn spring-boot:run
```

需要 Java 17 和 Maven。

## 接口

| 方法 | 路径 | 作用 |
| --- | --- | --- |
| GET | `/` | 文件列表和上传页面 |
| POST | `/upload` | 上传文件，表单字段名为 `file` |
| GET | `/files/{filename}` | 下载文件 |
| POST | `/files/{filename}/delete` | 删除文件 |
| GET | `/health` | 健康检查，不需要 Token |

文件名不能包含路径分隔符或控制字符，避免把文件写到存储根目录之外。

## 存储方式

通过 `STORAGE_TYPE` 选择：`local`、`database` 或 `object`。

### 本地目录

这是默认方式：

```dotenv
STORAGE_TYPE=local
STORAGE_LOCATION=/data
```

Compose 已把 `./upload-dir` 挂载到容器的 `/data`。

### 数据库

项目已包含 H2 和 MySQL 驱动。使用 MySQL：

```dotenv
STORAGE_TYPE=database
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/filedb?useSSL=false&serverTimezone=UTC&characterEncoding=utf8mb4
SPRING_DATASOURCE_USERNAME=fileapp
SPRING_DATASOURCE_PASSWORD=替换为真实密码
```

生产环境应使用专用低权限账号，不要把密码写入仓库。

### MinIO

```dotenv
STORAGE_TYPE=object
STORAGE_ENDPOINT=http://minio:9000
STORAGE_ACCESS_KEY=替换为AccessKey
STORAGE_SECRET_KEY=替换为SecretKey
STORAGE_BUCKET=file-upload-demo
STORAGE_REGION=us-east-1
```

AccessKey 和 SecretKey 没有默认值；选择对象存储时必须显式提供。HTTP/HTTPS 由 `STORAGE_ENDPOINT` 的协议决定。

## 常用环境变量

| 变量 | 默认值 | 说明 |
| --- | --- | --- |
| `FILES_AUTH_TOKEN` | 空 | Bearer Token；为空时关闭认证 |
| `STORAGE_TYPE` | `local` | 存储后端 |
| `STORAGE_LOCATION` | `upload-dir` | 本地存储目录 |
| `MAX_FILE_SIZE` | `10MB`（Compose） | 单文件大小上限 |
| `MAX_REQUEST_SIZE` | `10MB`（Compose） | 单请求大小上限 |

## 项目结构

```text
complete/
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── src/
    ├── main/java/com/example/filestorage/
    ├── main/resources/
    └── test/java/com/example/filestorage/
```

## 验证

```bash
cd complete
mvn clean test
docker compose config
docker compose build
```
