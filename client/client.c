#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <errno.h>
#include <stdint.h>

// 使用1字节对齐的结构体，模拟嵌入式设备信息
#pragma pack(1)
typedef struct {
    uint8_t device_id;        // 设备ID (1字节)
    uint16_t sequence_num;    // 序列号 (2字节)
    uint32_t timestamp;       // 时间戳 (4字节)
    float temperature;        // 温度 (4字节)
    float humidity;           // 湿度 (4字节)
    uint8_t status;           // 状态字节 (1字节)
    // 总计: 16字节
} DeviceData;
#pragma pack()

int main() {
    int sock;
    struct sockaddr_in server;
    
    // 创建socket
    if ((sock = socket(AF_INET, SOCK_STREAM, 0)) < 0) {
        perror("无法创建socket");
        return 1;
    }
    printf("Socket创建成功。\n");
    
    // 配置服务器地址
    server.sin_addr.s_addr = inet_addr("127.0.0.1");
    server.sin_family = AF_INET;
    server.sin_port = htons(10000);
    
    // 连接到服务器
    printf("正在连接到 localhost:10000...\n");
    if (connect(sock, (struct sockaddr*)&server, sizeof(server)) < 0) {
        perror("连接失败");
        close(sock);
        return 1;
    }
    printf("连接成功。\n");
    
    // 准备要发送的结构体数据（嵌入式设备通常直接使用原始值）
    DeviceData device_data;
    device_data.device_id = 0x01;
    device_data.sequence_num = 1234;
    device_data.timestamp = 1234567890;
    device_data.temperature = 25.5f;
    device_data.humidity = 60.0f;
    device_data.status = 0xAA;
    
    // 打印结构体信息
    printf("\n准备发送的数据:\n");
    printf("设备ID: 0x%02X\n", device_data.device_id);
    printf("序列号: %u\n", device_data.sequence_num);
    printf("时间戳: %u\n", device_data.timestamp);
    printf("温度: %.2f°C\n", device_data.temperature);
    printf("湿度: %.2f%%\n", device_data.humidity);
    printf("状态: 0x%02X\n", device_data.status);
    printf("结构体大小: %zu 字节\n", sizeof(DeviceData));
    
    // 直接发送结构体数据（嵌入式设备通常直接发送数据，不发送HTTP头）
    printf("\n正在发送结构体数据...\n");
    ssize_t bytes_sent = send(sock, (char*)&device_data, sizeof(DeviceData), 0);
    if (bytes_sent < 0) {
        perror("发送数据失败");
        close(sock);
        return 1;
    }
    printf("成功发送 %zd 字节数据。\n", bytes_sent);
    
    // 关闭socket
    close(sock);
    
    printf("\n程序执行完成。\n");
    return 0;
}
