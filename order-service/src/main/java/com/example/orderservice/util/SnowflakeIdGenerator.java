package com.example.orderservice.util;

import org.springframework.stereotype.Component;

/**
 * 雪花算法生成器 - 生成全局唯一订单 ID
 */
@Component
public class SnowflakeIdGenerator {
    
    // 起始时间戳（2024-01-01 00:00:00 UTC）
    private static final long START_TIMESTAMP = 1704067200000L;
    
    // 机器 ID 位数
    private static final long MACHINE_BITS = 5L;
    // 数据中心 ID 位数
    private static final long DATA_CENTER_BITS = 5L;
    // 序列号位数
    private static final long SEQUENCE_BITS = 12L;
    
    // 机器 ID 最大值
    private static final long MAX_MACHINE_ID = ~(-1L << MACHINE_BITS);
    // 数据中心 ID 最大值
    private static final long MAX_DATA_CENTER_ID = ~(-1L << DATA_CENTER_BITS);
    // 序列号最大值
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);
    
    // 机器 ID 位移位数
    private static final long MACHINE_SHIFT = SEQUENCE_BITS;
    // 数据中心 ID 位移位数
    private static final long DATA_CENTER_SHIFT = SEQUENCE_BITS + MACHINE_BITS;
    // 时间戳位移位数
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + MACHINE_BITS + DATA_CENTER_BITS;
    
    // 数据中心 ID（实际应用中可从配置文件读取）
    private final long dataCenterId;
    // 机器 ID（实际应用中可从配置文件读取）
    private final long machineId;
    // 序列号
    private long sequence = 0L;
    // 上次生成 ID 的时间戳
    private long lastTimestamp = -1L;
    
    public SnowflakeIdGenerator() {
        this.dataCenterId = 1L;
        this.machineId = 1L;
    }
    
    public SnowflakeIdGenerator(long dataCenterId, long machineId) {
        if (dataCenterId > MAX_DATA_CENTER_ID || dataCenterId < 0) {
            throw new IllegalArgumentException("数据中心 ID 必须在 0 到 " + MAX_DATA_CENTER_ID + " 之间");
        }
        if (machineId > MAX_MACHINE_ID || machineId < 0) {
            throw new IllegalArgumentException("机器 ID 必须在 0 到 " + MAX_MACHINE_ID + " 之间");
        }
        this.dataCenterId = dataCenterId;
        this.machineId = machineId;
    }
    
    /**
     * 生成下一个订单 ID
     */
    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();
        
        // 时钟回拨处理
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("时钟回拨！拒绝生成 ID");
        }
        
        // 同一毫秒内序列号递增
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // 同一毫秒内序列号溢出，等待下一毫秒
            if (sequence == 0) {
                timestamp = waitNextMillis(lastTimestamp);
            }
        } else {
            // 不同毫秒，序列号重置
            sequence = 0L;
        }
        
        lastTimestamp = timestamp;
        
        // 组合生成 ID：时间戳 + 数据中心 ID + 机器 ID + 序列号
        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_SHIFT)
                | (dataCenterId << DATA_CENTER_SHIFT)
                | (machineId << MACHINE_SHIFT)
                | sequence;
    }
    
    /**
     * 等待下一毫秒
     */
    private long waitNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }
}
