# TCP/IP 结构体发送客户端程序

这是一个模拟嵌入式设备的C程序，使用TCP/IP协议向 localhost:10000 发送结构体数据。

**本程序为 Linux 版本**

## 编译方法

### 使用 GCC 编译

在 Linux 终端中执行：

```bash
gcc -Wall -std=c99 -o client client.c
```

**参数说明：**
- `-Wall`：显示所有警告
- `-std=c99`：使用 C99 标准
- `-o client`：指定输出文件名
- `client.c`：源文件

### 编译示例

```bash
cd /path/to/project
gcc -Wall -std=c99 -o client client.c
```

编译成功后，会生成可执行文件 `client`。

## 运行方法

### 前提条件

在运行客户端之前，需要有一个服务器程序监听 `localhost:10000` 端口。如果没有服务器，客户端会连接失败。

可以使用以下方法测试服务器：

**方法1：使用 netcat (nc)**

在一个终端中启动服务器：

```bash
nc -l -p 10000
```

在另一个终端运行客户端。

**方法2：使用 Python 简单服务器**

```bash
python3 -c "import socket; s=socket.socket(); s.bind(('0.0.0.0', 10000)); s.listen(1); conn, addr=s.accept(); data=conn.recv(1024); print('收到数据:', data.hex()); conn.close()"
```

### 运行

编译成功后，直接运行：

```bash
./client
```

或者：

```bash
chmod +x client
./client
```

## 程序功能

程序会：
1. 创建 TCP socket
2. 连接到 localhost:10000
3. 发送一个16字节的结构体数据（使用1字节对齐）
4. 关闭连接

## 结构体定义

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

结构体总大小：16字节

## 常见问题

1. **编译错误：找不到头文件**
   - 确保系统已安装标准C库和开发工具
   - Ubuntu/Debian：`sudo apt-get install build-essential`
   - CentOS/RHEL：`sudo yum groupinstall "Development Tools"`

2. **连接失败：Connection refused**
   - 确保有服务器程序在监听 10000 端口
   - 检查端口是否被占用：`netstat -tuln | grep 10000` 或 `ss -tuln | grep 10000`
   - 可以使用 netcat 测试：`nc -l -p 10000`

3. **权限被拒绝 (Permission denied)**
   - 确保可执行文件有执行权限：`chmod +x client`
   - 或者使用：`bash client`（不推荐）

4. **找不到 gcc 命令**
   - 安装 gcc：`sudo apt-get install gcc`（Ubuntu/Debian）
   - 或：`sudo yum install gcc`（CentOS/RHEL）

## 测试服务器示例

可以使用以下 Python 脚本作为测试服务器：

```python
#!/usr/bin/env python3
import socket

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.bind(('0.0.0.0', 10000))
s.listen(1)
print("服务器监听 10000 端口...")
conn, addr = s.accept()
print(f"客户端连接: {addr}")
data = conn.recv(1024)
print(f"收到 {len(data)} 字节数据")
print(f"十六进制: {data.hex()}")
conn.close()
s.close()
```
