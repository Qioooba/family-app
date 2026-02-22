package com.family.common.cache.redisson;

import org.redisson.config.Config;
import org.redisson.config.ReadMode;
import org.redisson.config.SubscriptionMode;
import org.redisson.connection.balancer.RoundRobinLoadBalancer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;

import java.util.List;

/**
 * Redisson高级配置
 * 支持集群、哨兵、主从等模式
 *
 * @author family
 */
@Configuration
@ConfigurationProperties(prefix = "redisson")
public class RedissonAdvancedConfig {

    private String mode = "single";  // single, cluster, sentinel, master-slave
    private String password;
    private int database = 0;
    private int connectionMinimumIdleSize = 5;
    private int connectionPoolSize = 20;
    private int timeout = 3000;
    private int connectTimeout = 10000;

    // 单节点配置
    private String address = "redis://localhost:6379";

    // 集群配置
    private List<String> nodeAddresses;

    // 哨兵配置
    private String sentinelMasterName;
    private List<String> sentinelAddresses;

    @Bean
    @Primary
    public RedissonClient redissonClient() {
        Config config = new Config();

        switch (mode.toLowerCase()) {
            case "cluster":
                configureCluster(config);
                break;
            case "sentinel":
                configureSentinel(config);
                break;
            case "master-slave":
                configureMasterSlave(config);
                break;
            default:
                configureSingle(config);
        }

        // 通用配置
        config.setThreads(8);
        config.setNettyThreads(8);
        config.setCodec(new org.redisson.codec.JsonJacksonCodec());

        return Redisson.create(config);
    }

    /**
     * 单节点模式
     */
    private void configureSingle(Config config) {
        config.useSingleServer()
                .setAddress(address)
                .setPassword(password)
                .setDatabase(database)
                .setConnectionMinimumIdleSize(connectionMinimumIdleSize)
                .setConnectionPoolSize(connectionPoolSize)
                .setTimeout(timeout)
                .setConnectTimeout(connectTimeout)
                .setRetryAttempts(3)
                .setRetryInterval(1500);
    }

    /**
     * 集群模式
     */
    private void configureCluster(Config config) {
        if (nodeAddresses == null || nodeAddresses.isEmpty()) {
            throw new IllegalArgumentException("Cluster mode requires nodeAddresses");
        }

        config.useClusterServers()
                .addNodeAddress(nodeAddresses.toArray(new String[0]))
                .setPassword(password)
                .setScanInterval(2000)
                .setReadMode(ReadMode.MASTER_SLAVE)
                .setSubscriptionMode(SubscriptionMode.MASTER)
                .setLoadBalancer(new RoundRobinLoadBalancer())
                .setMasterConnectionMinimumIdleSize(connectionMinimumIdleSize)
                .setMasterConnectionPoolSize(connectionPoolSize)
                .setSlaveConnectionMinimumIdleSize(connectionMinimumIdleSize)
                .setSlaveConnectionPoolSize(connectionPoolSize);
    }

    /**
     * 哨兵模式
     */
    private void configureSentinel(Config config) {
        if (sentinelMasterName == null || sentinelAddresses == null || sentinelAddresses.isEmpty()) {
            throw new IllegalArgumentException("Sentinel mode requires sentinelMasterName and sentinelAddresses");
        }

        config.useSentinelServers()
                .setMasterName(sentinelMasterName)
                .addSentinelAddress(sentinelAddresses.toArray(new String[0]))
                .setPassword(password)
                .setDatabase(database)
                .setReadMode(ReadMode.MASTER_SLAVE)
                .setMasterConnectionMinimumIdleSize(connectionMinimumIdleSize)
                .setMasterConnectionPoolSize(connectionPoolSize)
                .setSlaveConnectionMinimumIdleSize(connectionMinimumIdleSize)
                .setSlaveConnectionPoolSize(connectionPoolSize);
    }

    /**
     * 主从模式
     */
    private void configureMasterSlave(Config config) {
        if (nodeAddresses == null || nodeAddresses.isEmpty()) {
            throw new IllegalArgumentException("Master-slave mode requires nodeAddresses");
        }

        String masterAddress = nodeAddresses.get(0);
        String[] slaveAddresses = nodeAddresses.subList(1, nodeAddresses.size()).toArray(new String[0]);

        config.useMasterSlaveServers()
                .setMasterAddress(masterAddress)
                .addSlaveAddress(slaveAddresses)
                .setPassword(password)
                .setDatabase(database)
                .setReadMode(ReadMode.MASTER_SLAVE)
                .setMasterConnectionMinimumIdleSize(connectionMinimumIdleSize)
                .setMasterConnectionPoolSize(connectionPoolSize)
                .setSlaveConnectionMinimumIdleSize(connectionMinimumIdleSize)
                .setSlaveConnectionPoolSize(connectionPoolSize);
    }

    // Getters and Setters
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public int getDatabase() { return database; }
    public void setDatabase(int database) { this.database = database; }
    public int getConnectionMinimumIdleSize() { return connectionMinimumIdleSize; }
    public void setConnectionMinimumIdleSize(int connectionMinimumIdleSize) { this.connectionMinimumIdleSize = connectionMinimumIdleSize; }
    public int getConnectionPoolSize() { return connectionPoolSize; }
    public void setConnectionPoolSize(int connectionPoolSize) { this.connectionPoolSize = connectionPoolSize; }
    public int getTimeout() { return timeout; }
    public void setTimeout(int timeout) { this.timeout = timeout; }
    public int getConnectTimeout() { return connectTimeout; }
    public void setConnectTimeout(int connectTimeout) { this.connectTimeout = connectTimeout; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public List<String> getNodeAddresses() { return nodeAddresses; }
    public void setNodeAddresses(List<String> nodeAddresses) { this.nodeAddresses = nodeAddresses; }
    public String getSentinelMasterName() { return sentinelMasterName; }
    public void setSentinelMasterName(String sentinelMasterName) { this.sentinelMasterName = sentinelMasterName; }
    public List<String> getSentinelAddresses() { return sentinelAddresses; }
    public void setSentinelAddresses(List<String> sentinelAddresses) { this.sentinelAddresses = sentinelAddresses; }
}
