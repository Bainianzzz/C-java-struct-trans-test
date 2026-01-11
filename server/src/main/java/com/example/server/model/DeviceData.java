package com.example.server.model;

/**
 * 设备数据结构体，对应C客户端的DeviceData结构
 * 使用1字节对齐，总共16字节
 */
public class DeviceData {
    private byte deviceId;          // 设备ID (1字节)
    private short sequenceNum;      // 序列号 (2字节)
    private int timestamp;          // 时间戳 (4字节)
    private float temperature;      // 温度 (4字节)
    private float humidity;         // 湿度 (4字节)
    private byte status;            // 状态字节 (1字节)

    public DeviceData() {
    }

    public DeviceData(byte deviceId, short sequenceNum, int timestamp, 
                     float temperature, float humidity, byte status) {
        this.deviceId = deviceId;
        this.sequenceNum = sequenceNum;
        this.timestamp = timestamp;
        this.temperature = temperature;
        this.humidity = humidity;
        this.status = status;
    }

    public byte getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(byte deviceId) {
        this.deviceId = deviceId;
    }

    public short getSequenceNum() {
        return sequenceNum;
    }

    public void setSequenceNum(short sequenceNum) {
        this.sequenceNum = sequenceNum;
    }

    public int getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(int timestamp) {
        this.timestamp = timestamp;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = temperature;
    }

    public float getHumidity() {
        return humidity;
    }

    public void setHumidity(float humidity) {
        this.humidity = humidity;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return String.format(
            "DeviceData{deviceId=0x%02X, sequenceNum=%d, timestamp=%d, " +
            "temperature=%.2f°C, humidity=%.2f%%, status=0x%02X}",
            deviceId & 0xFF, sequenceNum & 0xFFFF, timestamp,
            temperature, humidity, status & 0xFF
        );
    }
}
