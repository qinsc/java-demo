package com.websystique.springsecurity.configuration;

import com.google.common.base.Strings;
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

import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableRedisHttpSession
@PropertySource(value = {"classpath:application.properties"})
public class SpringSessionConfig {
    @Autowired
    private Environment environment;

    @Bean
    public RedisConnectionFactory connectionFactory() {
        String cluster = environment.getRequiredProperty("redis.cluster");
        String clusterNodes = environment.getRequiredProperty("redis.nodes");
        List<IpPort> ipPorts = parseNodesProperty(clusterNodes);

        LettuceConnectionFactory factory = null;
        if ("true".equals(cluster)) {
            RedisSentinelConfiguration sentinelConfig = new RedisSentinelConfiguration().master("mymaster");
            for (IpPort ipPort : ipPorts) {
                sentinelConfig.addSentinel(new RedisNode(ipPort.getIp(), ipPort.getPort()));
            }
            factory = new LettuceConnectionFactory(sentinelConfig);
        } else {
            factory = new LettuceConnectionFactory();
            if (ipPorts.size() ==  1) {
                IpPort ipPort = ipPorts.get(0);
                factory.setHostName(ipPort.getIp());
                factory.setPort(ipPort.getPort());
            }
        }
        factory.setPassword(environment.getProperty("redis.password"));
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

    private List<IpPort> parseNodesProperty(String proertyValue){
        List<IpPort> ipPorts = new ArrayList<>();
        if (!Strings.isNullOrEmpty(proertyValue)) {
            String[] nodeIpPorts = proertyValue.split(",");
            if (nodeIpPorts != null && nodeIpPorts.length > 0){
                for (int i = 0; i < nodeIpPorts.length ; i++) {
                    String[] ipPort = nodeIpPorts[i].split(":");
                    if (ipPort != null && ipPort.length == 2){
                        ipPorts.add(new IpPort(ipPort[0], Integer.parseInt(ipPort[1])));
                    }
                }
            }
        }
        return ipPorts;
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
