# TCP设备数据接收服务器

这是一个Spring Boot应用程序，用于接收C客户端通过TCP/IP协议发送的设备数据结构体数据。

## 功能特性

- 监听TCP端口10000
- 接收16字节的二进制结构体数据
- 解析设备数据（设备ID、序列号、时间戳、温度、湿度、状态）
- 将接收到的数据打印到日志中

## 数据结构

对应C客户端的`DeviceData`结构体（1字节对齐，总共16字节）：

```
- device_id (1字节): uint8_t
- sequence_num (2字节): uint16_t
- timestamp (4字节): uint32_t
- temperature (4字节): float
- humidity (4字节): float
- status (1字节): uint8_t
```

## 环境要求

- JDK 17 或更高版本
- Maven 3.6 或更高版本

## 编译和运行

### 1. 编译项目

```bash
cd server
mvn clean package
```

### 2. 运行应用

```bash
# 方式1：使用Maven运行
mvn spring-boot:run

# 方式2：运行编译后的jar文件
java -jar target/tcp-server-1.0.0.jar
```

### 3. 查看日志

应用启动后，会监听10000端口。当C客户端发送数据时，日志会显示接收到的设备数据。

## 配置

在 `src/main/resources/application.properties` 或 `application.yml` 中可以配置：

- `tcp.server.port`: TCP服务器监听端口（默认：10000）
- 日志级别和格式

## 使用示例

1. 启动Spring Boot服务器：
   ```bash
   mvn spring-boot:run
   ```

2. 在另一个终端运行C客户端：
   ```bash
   cd ../client
   gcc -Wall -std=c99 -o client client.c
   ./client
   ```

3. 查看服务器日志，会看到类似以下输出：
   ```
   2024-01-01 12:00:00.000 [main] INFO  com.example.server.service.TcpServerService - TCP服务器启动，监听端口: 10000
   2024-01-01 12:00:05.123 [Thread-1] INFO  com.example.server.service.TcpServerService - 客户端连接: /127.0.0.1:54321
   2024-01-01 12:00:05.125 [Thread-2] INFO  com.example.server.service.TcpServerService - ========================================
   2024-01-01 12:00:05.125 [Thread-2] INFO  com.example.server.service.TcpServerService - 收到设备数据:
   2024-01-01 12:00:05.125 [Thread-2] INFO  com.example.server.service.TcpServerService -   设备ID: 0x01 (1)
   2024-01-01 12:00:05.125 [Thread-2] INFO  com.example.server.service.TcpServerService -   序列号: 1234
   2024-01-01 12:00:05.125 [Thread-2] INFO  com.example.server.service.TcpServerService -   时间戳: 1234567890
   2024-01-01 12:00:05.125 [Thread-2] INFO  com.example.server.service.TcpServerService -   温度: 25.50°C
   2024-01-01 12:00:05.125 [Thread-2] INFO  com.example.server.service.TcpServerService -   湿度: 60.00%
   2024-01-01 12:00:05.125 [Thread-2] INFO  com.example.server.service.TcpServerService -   状态: 0xAA (170)
   2024-01-01 12:00:05.125 [Thread-2] INFO  com.example.server.service.TcpServerService - ========================================
   ```

## 项目结构

```
server/
├── pom.xml
├── README.md
└── src/
    └── main/
        ├── java/
        │   └── com/
        │       └── example/
        │           └── server/
        │               ├── TcpServerApplication.java
        │               ├── model/
        │               │   └── DeviceData.java
        │               └── service/
        │                   └── TcpServerService.java
        └── resources/
            ├── application.properties
            └── application.yml
```

## 注意事项

1. **字节序处理**：C客户端使用小端序（Little Endian）发送数据，Java代码使用`ByteOrder.LITTLE_ENDIAN`进行解析。

2. **端口占用**：确保10000端口未被其他程序占用。

3. **数据类型**：注意Java中的`signed`类型与C中的`unsigned`类型的转换，使用`& 0xFF`或`& 0xFFFF`来正确显示无符号值。

4. **多客户端支持**：服务器支持多个客户端同时连接，每个连接在独立的线程中处理。
