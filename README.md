# C/Java 结构体数据传输测试项目

本项目演示了C客户端通过TCP/IP协议向Java Spring Boot后端发送二进制结构体数据，以及后端如何接收和解析这些数据。

![C-java-struct-trans-test/image/result.png at main · Bainianzzz/C-java-struct-trans-test](https://github.com/Bainianzzz/C-java-struct-trans-test/blob/main/image/result.png)

## 项目概述

- **客户端**：C语言编写的TCP客户端，模拟嵌入式设备发送16字节的结构体数据
- **服务端**：Spring Boot Java应用，接收并解析客户端发送的二进制数据

## 关键技术点

### 1. 字节序处理

- C客户端发送小端序数据
- Java使用`ByteOrder.LITTLE_ENDIAN`正确解析

### 2. 数据完整性

- 循环读取确保16字节数据完整接收
- 处理TCP流可能的分段传输

### 3. 并发处理

- 每个客户端连接使用独立线程处理
- 支持多客户端同时连接

### 4. 资源管理

- 使用try-with-resources自动关闭资源
- `@PreDestroy`确保服务器正确关闭

## 数据结构

客户端和服务端使用相同的数据结构（1字节对齐，总共16字节）：

```c
#pragma pack(1)
typedef struct {
    uint8_t device_id;        // 设备ID (1字节)
    uint16_t sequence_num;    // 序列号 (2字节)
    uint32_t timestamp;       // 时间戳 (4字节)
    float temperature;        // 温度 (4字节)
    float humidity;           // 湿度 (4字节)
    uint8_t status;           // 状态字节 (1字节)
} DeviceData;
#pragma pack()
```

## 后端数据接收与处理流程

### 1. TCP服务器启动

Spring Boot应用启动时，`TcpServerService`服务类通过`@PostConstruct`注解自动初始化：

- 创建`ServerSocket`监听指定端口（默认10000）
- 启动独立的非守护线程持续监听客户端连接
- 支持多客户端并发连接，每个连接在独立线程中处理

### 2. 客户端连接处理

当客户端连接时，服务器执行以下流程：

```
客户端连接 → accept() → 创建处理线程 → handleClient()
```

### 3. 数据接收

在`handleClient()`方法中：

1. **创建输入流**：使用`DataInputStream`包装socket的输入流
2. **读取16字节数据**：
   - 创建16字节的缓冲区
   - 循环读取确保读取完整数据（TCP流可能分段传输）
   - 检查读取是否完整，如果连接提前关闭则记录警告

```java
byte[] buffer = new byte[16];
int bytesRead = 0;
while (bytesRead < 16) {
    int n = inputStream.read(buffer, bytesRead, 16 - bytesRead);
    if (n == -1) {
        // 连接关闭
        return;
    }
    bytesRead += n;
}
```

### 4. 数据解析

使用`ByteBuffer`解析二进制数据，**关键点：字节序处理**

- C客户端使用**小端序（Little Endian）**发送数据
- Java默认使用**大端序（Big Endian）**
- 必须设置`ByteOrder.LITTLE_ENDIAN`才能正确解析

```java
ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
byte deviceId = buffer.get();           // 读取1字节
short sequenceNum = buffer.getShort();  // 读取2字节
int timestamp = buffer.getInt();        // 读取4字节
float temperature = buffer.getFloat();  // 读取4字节
float humidity = buffer.getFloat();     // 读取4字节
byte status = buffer.get();             // 读取1字节
```

**数据类型映射**：
- C `uint8_t` → Java `byte`（使用`& 0xFF`转换为无符号显示）
- C `uint16_t` → Java `short`（使用`& 0xFFFF`转换为无符号显示）
- C `uint32_t` → Java `int`
- C `float` → Java `float`

### 5. 数据输出

解析后的数据通过SLF4J日志框架输出，包含：
- 各字段的详细值（设备ID、序列号、时间戳等）
- 原始二进制数据的十六进制表示
- 完整的对象toString()输出

### 6. 连接关闭

处理完成后：
- 关闭客户端socket连接
- 记录连接关闭日志
- 线程结束，释放资源

## 环境要求

- **JDK 17** 或更高版本
- **Maven 3.6** 或更高版本
- **GCC编译器**（用于编译C客户端）

## 启动流程

### 1. 编译服务端

```bash
cd server
mvn clean package
```

这会生成可执行的jar文件：`target/tcp-server-1.0.0.jar`

### 2. 启动服务端

```bash
java -jar target/tcp-server-1.0.0.jar
```

### 3. 编译客户端

```bash
cd client
gcc -Wall -std=c99 -o client client.c
```

### 4. 运行客户端

```bash
./client
```

### 5. 查看服务端日志

客户端连接并发送数据后，服务端日志会显示：

```
========================================
收到设备数据:
  设备ID: 0x01 (1)
  序列号: 1234
  时间戳: 1234567890
  温度: 25.50°C
  湿度: 60.00%
  状态: 0xAA (170)
  原始数据 (十六进制): 01 D2 04 00 00 49 96 D4 00 00 CC 41 00 00 70 42 AA
完整对象: DeviceData{deviceId=0x01, sequenceNum=1234, timestamp=1234567890, temperature=25.50°C, humidity=60.00%, status=0xAA}
========================================
```

## 常见问题

1. **服务端立即退出**
   - 确保服务器线程不是守护线程（daemon = false）
   - 检查`TcpServerService.java`中的线程设置

2. **数据解析错误**
   - 确认使用`ByteOrder.LITTLE_ENDIAN`
   - 检查字节数组长度是否为16字节

3. **端口被占用**
   - 检查端口：`netstat -tuln | grep 10000`
   - 修改`application.properties`中的端口配置

4. **连接失败**
   - 确保服务端已启动
   - 检查防火墙设置
   - 验证IP地址和端口号

## 扩展建议

- 添加数据持久化（数据库存储）
- 实现数据验证和异常处理
- 添加HTTP API查询接收的数据
- 实现数据统计和分析功能
- 添加数据加密和认证机制
