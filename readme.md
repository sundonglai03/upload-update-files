# Spring Boot 文件上传示例

这是一个基于 Spring Boot 4 的文件上传和下载示例项目，使用 Thymeleaf 提供上传页面，支持上传文件并在页面中展示已上传文件列表。

## 功能概览

- 支持通过浏览器上传单个文件
- 上传成功后返回首页，并显示提示信息
- 自动初始化本地存储目录
- 列出已上传文件列表
- 可通过文件链接下载已上传的文件
- 具备基础的文件路径校验，避免越权写入到应用目录外

## 项目结构

```text
complete/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/uploadingfiles/
│   │   │       ├── FileUploadController.java
│   │   │       ├── UploadingFilesApplication.java
│   │   │       └── storage/
│   │   │           ├── FileSystemStorageService.java
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

## 核心代码说明

### 1. 启动类

`UploadingFilesApplication` 是应用入口，使用 `@SpringBootApplication` 启动 Spring Boot 应用，并通过 `CommandLineRunner` 在启动时调用存储服务初始化：

- 清空存储目录中的旧文件
- 调用 `storageService.init()` 创建默认上传目录

### 2. 控制器

`FileUploadController` 负责：

- `GET /`：加载已上传文件列表并显示上传表单
- `POST /`：处理文件上传
- `GET /files/{filename}`：返回文件内容，供浏览器下载
- `@ExceptionHandler`：处理未找到文件的异常

### 3. 存储服务

`FileSystemStorageService` 实现了文件系统存储逻辑：

- 创建目标目录
- 校验上传文件是否为空
- 校验文件路径是否在允许目录中
- 复制文件到本地存储目录
- 读取、列出和下载文件

### 4. 配置属性

`StorageProperties` 通过 `@ConfigurationProperties("storage")` 读取配置，默认存储目录为：

```text
upload-dir
```

`application.properties` 中设置了上传文件大小限制：

```properties
spring.servlet.multipart.max-file-size=128KB
spring.servlet.multipart.max-request-size=128KB
```

## 运行方式

进入项目目录后执行：

```bash
cd complete
mvn spring-boot:run
```

启动成功后，访问：

```text
http://localhost:8080
```

在网页中可以选择文件上传，并查看已上传文件列表。

## 关键依赖

- Spring Boot 4
- Spring Web MVC
- Thymeleaf
- Spring Boot Test

## 说明

这个示例主要用于演示“Spring Boot + 文件上传 + 本地存储”的基础实现，适合学习：

- MultipartFile 的处理方式
- 文件上传页面的实现
- Spring MVC 的控制器设计
- 本地文件系统存储的基本模式

如果需要更高规模的文件管理，后续可以扩展为：

- 文件名校验
- 文件大小限制策略
- 文件删除功能
- 集成数据库或对象存储（如 MinIO / S3）
