# File Upload Download Demo

这是一个 Spring Boot 文件上传下载示例，当前项目支持三种存储方式：

- 本地文件存储：`storage.type=local`
- 数据库存储：`storage.type=database`
- 对象存储：`storage.type=object`

项目默认可直接运行，默认使用本地文件存储；也可以切换到 MySQL 或 MinIO。

## 功能概览

- 上传文件
- 列出已上传文件
- 下载文件
- 删除文件
- 支持本地、数据库、对象存储三种后端
- 可在 `application.properties` 中切换配置

## 目录结构

```text
complete/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/uploadingfiles/
│   │   │       ├── FileUploadDownloadApplication.java
│   │   │       ├── UploadController.java
│   │   │       ├── DownloadController.java
│   │   │       └── storage/
│   │   │           ├── FileUploadService.java
│   │   │           ├── StorageException.java
│   │   │           ├── StorageFileNotFoundException.java
│   │   │           ├── StorageProperties.java
│   │   │           └── StorageService.java
│   │   ├── resources/
│   │   │   ├── application.properties
│   │   │   └── templates/
│   │   │       └── uploadForm.html
│   └── test/
│       └── java/com/example/uploadingfiles/
│           ├── FileUploadIntegrationTests.java
│           ├── FileUploadTests.java
│           └── storage/
│               └── FileSystemStorageServiceTests.java
```

## 启动命令

推荐统一命令如下：

```bash
cd complete
mvn clean package
mvn spring-boot:run
```

或者直接运行：

```bash
cd complete
mvn spring-boot:run
```

启动后访问：

```text
http://localhost:8080
```

## 访问方式

### 上传

- 访问首页
- 选择文件后提交到 `/upload`
- 上传完成后返回首页并显示成功提示

### 下载

- 首页会列出已上传文件列表
- 点击对应链接即可下载文件
- 实际访问地址类似：

```text
http://localhost:8080/files/test.txt
```

## 关键实现说明

### 1. 上传实现

`UploadController` 负责处理：

- `GET /`：展示上传页和已上传文件列表
- `POST /upload`：接收 `MultipartFile` 并调用存储服务保存

### 2. 下载实现

`DownloadController` 负责处理：

- `GET /files/{filename}`：查询文件并返回 `Resource`
- 设置 `Content-Disposition: attachment` 让浏览器下载文件
- 未找到文件时返回 `404`

### 3. 存储实现

`FileUploadService`（原 `FileSystemStorageService`）负责存储逻辑：

- 初始化存储目录
- 校验空文件
- 校验文件路径是否合法
- 保存文件到本地目录
- 根据文件名读取资源

### 4. 配置项

`StorageProperties` 通过 `@ConfigurationProperties("storage")` 读取目录配置，默认目录为：

```text
upload-dir
```

`application.properties` 中设置了文件大小限制：

```properties
spring.servlet.multipart.max-file-size=128KB
spring.servlet.multipart.max-request-size=128KB
```

## 本地文件存储

默认使用本地文件方式：

```properties
storage.type=local
storage.location=upload-dir
```

文件会保存到项目下的 `upload-dir` 目录。

## 数据库存储

如果要用数据库存储，配置如下：

```properties
storage.type=database
spring.datasource.url=jdbc:mysql://localhost:3306/filedb?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=utf8mb4
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.hibernate.ddl-auto=update
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect
```

MySQL 驱动：

```xml
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <scope>runtime</scope>
</dependency>
```

Spring Boot 默认使用 HikariCP 连接池，常见配置：

```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
```

## 对象存储（MinIO）

如果要用对象存储，配置如下：

```properties
storage.type=object
storage.endpoint=http://localhost:9000
storage.access-key=minioadmin
storage.secret-key=minioadmin
storage.bucket=file-upload-demo
storage.region=us-east-1
storage.secure=false
```

启动 MinIO 后，程序会自动创建 bucket，并把上传文件保存到对象存储中。

## 启动方式

```bash
cd complete
mvn spring-boot:run
```

访问：

```text
http://localhost:8080
```

## 说明

- 默认模式是本地文件存储；不需要额外依赖即可启动。
- 数据库存储适合小文件和结构化管理。
- 对象存储适合大文件和生产环境。
- 具体配置都在 [complete/src/main/resources/application.properties](complete/src/main/resources/application.properties) 中。
