package com.example.server.service;

import com.example.server.model.DeviceData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

@Service
public class TcpServerService {
    private static final Logger logger = LoggerFactory.getLogger(TcpServerService.class);
    
    @Value("${tcp.server.port:10000}")
    private int port;
    
    private ServerSocket serverSocket;
    private boolean running = false;
    private Thread serverThread;

    @PostConstruct
    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            running = true;
            logger.info("TCP服务器启动，监听端口: {}", port);
            
            serverThread = new Thread(this::acceptConnections);
            serverThread.setDaemon(false);
            serverThread.start();
        } catch (IOException e) {
            logger.error("无法启动TCP服务器", e);
            throw new RuntimeException("TCP服务器启动失败", e);
        }
    }

    private void acceptConnections() {
        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                logger.info("客户端连接: {}", clientSocket.getRemoteSocketAddress());
                
                // 为每个连接创建处理线程
                new Thread(() -> handleClient(clientSocket)).start();
            } catch (IOException e) {
                if (running) {
                    logger.error("接受连接时出错", e);
                }
            }
        }
    }

    private void handleClient(Socket socket) {
        try (DataInputStream inputStream = new DataInputStream(socket.getInputStream())) {
            // 读取16字节的结构体数据
            byte[] buffer = new byte[16];
            int bytesRead = 0;
            
            // 确保读取完整的16字节
            while (bytesRead < 16) {
                int n = inputStream.read(buffer, bytesRead, 16 - bytesRead);
                if (n == -1) {
                    logger.warn("连接关闭，仅读取 {} 字节", bytesRead);
                    return;
                }
                bytesRead += n;
            }
            
            // 解析结构体数据
            DeviceData deviceData = parseDeviceData(buffer);
            
            // 打印到日志
            logger.info("========================================");
            logger.info("收到设备数据:");
            logger.info("  设备ID: 0x{} ({})", 
                String.format("%02X", deviceData.getDeviceId() & 0xFF), 
                deviceData.getDeviceId() & 0xFF);
            logger.info("  序列号: {}", deviceData.getSequenceNum() & 0xFFFF);
            logger.info("  时间戳: {}", deviceData.getTimestamp());
            logger.info("  温度: {}°C", String.format("%.2f", deviceData.getTemperature()));
            logger.info("  湿度: {}%", String.format("%.2f", deviceData.getHumidity()));
            logger.info("  状态: 0x{} ({})", 
                String.format("%02X", deviceData.getStatus() & 0xFF), 
                deviceData.getStatus() & 0xFF);
            logger.info("  原始数据 (十六进制): {}", bytesToHex(buffer));
            logger.info("完整对象: {}", deviceData);
            logger.info("========================================");
            
        } catch (IOException e) {
            logger.error("处理客户端数据时出错", e);
        } finally {
            try {
                socket.close();
                logger.debug("客户端连接已关闭: {}", socket.getRemoteSocketAddress());
            } catch (IOException e) {
                logger.warn("关闭客户端连接时出错", e);
            }
        }
    }

    /**
     * 解析16字节的二进制数据为DeviceData对象
     * 注意：C客户端直接发送原始字节（小端序），Java使用大端序，需要转换
     */
    private DeviceData parseDeviceData(byte[] data) {
        if (data.length < 16) {
            throw new IllegalArgumentException("数据长度不足16字节");
        }
        
        // 使用ByteBuffer解析，设置为小端序以匹配C客户端
        ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
        
        byte deviceId = buffer.get();
        short sequenceNum = buffer.getShort();
        int timestamp = buffer.getInt();
        float temperature = buffer.getFloat();
        float humidity = buffer.getFloat();
        byte status = buffer.get();
        
        return new DeviceData(deviceId, sequenceNum, timestamp, temperature, humidity, status);
    }

    /**
     * 将字节数组转换为十六进制字符串
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b & 0xFF));
        }
        return sb.toString().trim();
    }

    @PreDestroy
    public void stop() {
        running = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                logger.info("TCP服务器已关闭");
            }
        } catch (IOException e) {
            logger.error("关闭服务器时出错", e);
        }
    }
}
