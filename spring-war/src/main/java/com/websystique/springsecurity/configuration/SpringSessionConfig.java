package com.websystique.springsecurity.configuration;

import com.google.common.base.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisNode;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.session.data.redis.config.ConfigureRedisAction;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

import java.util.*;

@Configuration
@EnableRedisHttpSession
@PropertySource(value = {"classpath:application.properties"})
public class SpringSessionConfig {
    private static final Logger logger = LoggerFactory.getLogger(SpringSessionConfig.class);

    static final String ENV_REDIS_CLUSTER = "REDIS_CLUSTER";
    static final String ENV_REDIS_HOST_NAME = "REDIS_HOST_NAME";
    static final String ENV_REDIS_HOST_PORT = "REDIS_HOST_PORT";
    static final String ENV_REDIS_SENTINEL_NODES = "REDIS_SENTINEL_NODES";
    static final String ENV_REDIS_PASSWORD = "REDIS_PASSWORD";

    static final String KEY_REDIS_CLUSTER = "redis.cluster";
    static final String KEY_REDIS_HOST_NAME = "redis.host.name";
    static final String KEY_REDIS_HOST_PORT = "redis.host.port";
    static final String KEY_REDIS_SENTINEL_NODES = "redis.sentinel.nodes";
    static final String KEY_REDIS_PASSWORD = "redis.password";

    @Autowired
    private Environment environment;

    @Bean
    public RedisConnectionFactory connectionFactory() {
        RedisConfig config = loadRedisConfig();
        logger.info(config.toString());

        LettuceConnectionFactory factory = null;
        if (config.isCluster()) {
            RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration().master("mymaster");
            for (IpPort ipPort : config.getSentinelNodes()) {
                sentinelConfig.addSentinel(new RedisNode(ipPort.getIp(), ipPort.getPort()));
            }
            factory = new LettuceConnectionFactory(sentinelConfig);
        } else {
            factory = new LettuceConnectionFactory();
            factory.setHostName(config.getHostName());
            factory.setPort(config.getHostPort());
        }
        if (!Strings.isNullOrEmpty(config.getPassword())) {
            factory.setPassword(config.getPassword());
        }
        return factory;
    }

    /**
     * 让Spring Session不再执行config命令
     *
     * @return
     */
    @Bean
    public static ConfigureRedisAction configureRedisAction() {
        return ConfigureRedisAction.NO_OP;
    }

    private List<IpPort> parseNodesProperty(String proertyValue) {
        List<IpPort> ipPorts = new ArrayList<>();
        if (!Strings.isNullOrEmpty(proertyValue)) {
            String[] nodeIpPorts = proertyValue.split(",");
            if (nodeIpPorts != null && nodeIpPorts.length > 0) {
                for (int i = 0; i < nodeIpPorts.length; i++) {
                    String[] ipPort = nodeIpPorts[i].split(":");
                    if (ipPort != null && ipPort.length == 2) {
                        IpPort t = new IpPort(ipPort[0], Integer.parseInt(ipPort[1]));
                        logger.info(t.toString());
                        ipPorts.add(t);
                    }
                }
            }
        }
        return ipPorts;
    }

    private RedisConfig loadRedisConfig() {
        showSystemEnvAndProperties();
        RedisConfig config = new RedisConfig();

        String cluster = System.getenv(ENV_REDIS_CLUSTER);
        String hostName = System.getenv(ENV_REDIS_HOST_NAME);
        String hostPort = System.getenv(ENV_REDIS_HOST_PORT);
        String redisSentinelNodes = System.getenv(ENV_REDIS_SENTINEL_NODES);
        String password = System.getenv(ENV_REDIS_PASSWORD);

        if (Strings.isNullOrEmpty(cluster)) {
            cluster = environment.getRequiredProperty(KEY_REDIS_CLUSTER);
        }
        if (Strings.isNullOrEmpty(hostName)) {
            hostName = environment.getRequiredProperty(KEY_REDIS_HOST_NAME);
        }
        if (Strings.isNullOrEmpty(hostPort)) {
            hostPort = environment.getRequiredProperty(KEY_REDIS_HOST_PORT);
        }
        if (Strings.isNullOrEmpty(redisSentinelNodes)) {
            redisSentinelNodes = environment.getRequiredProperty(KEY_REDIS_SENTINEL_NODES);
        }
        if (Strings.isNullOrEmpty(password)) {
            password = environment.getProperty(KEY_REDIS_PASSWORD);
        }

        config.setCluster("true".equals(cluster));
        config.setHostName(hostName);
        if (hostPort != null) {
            config.setHostPort(Integer.parseInt(hostPort));
        }
        if (redisSentinelNodes != null) {
            config.getSentinelNodes().addAll(parseNodesProperty(redisSentinelNodes));
        }
        config.setPassword(password);

        return config;
    }

    private void showSystemEnvAndProperties() {
        Map<String, String> envs = System.getenv();
        Set<String> keys = envs.keySet();
        logger.info("System envs:");
        for (String key : keys) {
            logger.info(key + " --> " + envs.get(key));
        }

        Properties properties = System.getProperties();
        Set<Object> proKeys = properties.keySet();
        logger.info("System properties:");
        for (Object key : proKeys) {
            logger.info(key + " --> " + properties.get(key));
        }
    }

    class RedisConfig {
        /**
         * 是否为集群
         */
        private boolean cluster;
        /**
         * 非集群时，redis服务器的主机名或ip地址
         */
        private String hostName;
        /**
         * 非集群时，redis服务器的端口号，默认为6379
         */
        private Integer hostPort = 6379;
        /**
         * 集群时，哨兵的地址，多个地址之间用逗号分隔，如： 127.0.0.1:6470,127.0.0.1:6471,127.0.0.:6472
         */
        private List<IpPort> sentinelNodes = new ArrayList<>();
        /**
         * redis的访问密码
         */
        private String password;

        public boolean isCluster() {
            return cluster;
        }

        public void setCluster(boolean cluster) {
            this.cluster = cluster;
        }

        public String getHostName() {
            return hostName;
        }

        public void setHostName(String hostName) {
            this.hostName = hostName;
        }

        public Integer getHostPort() {
            return hostPort;
        }

        public void setHostPort(Integer hostPort) {
            this.hostPort = hostPort;
        }

        public List<IpPort> getSentinelNodes() {
            return sentinelNodes;
        }

        public void setSentinelNodes(List<IpPort> sentinelNodes) {
            this.sentinelNodes = sentinelNodes;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        @Override
        public String toString() {
            return "RedisConfig{" +
                    "cluster=" + cluster +
                    ", hostName='" + hostName + '\'' +
                    ", hostPort=" + hostPort +
                    ", sentinelNodes=" + sentinelNodes +
                    ", password='" + password + '\'' +
                    '}';
        }
    }

    /**
     * IP 与 端口
     */
    class IpPort {
        String ip;
        Integer port;

        public IpPort(String ip, Integer port) {
            this.ip = ip;
            this.port = port;
        }

        @Override
        public String toString() {
            return "IpPort{" +
                    "ip='" + ip + '\'' +
                    ", port=" + port +
                    '}';
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }
    }
}
