package com.websystique.springsecurity.configuration;

import java.util.Properties;

import javax.sql.DataSource;

import com.google.common.base.Strings;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@ComponentScan({ "com.websystique.springsecurity.configuration" })
@PropertySource(value = { "classpath:application.properties" })
public class HibernateConfiguration {
    static final String MYSQL_URL = "MYSQL_URL";
    static final String MYSQL_USERNAME = "MYSQL_USERNAME";
    static final String MYSQL_PASSWORD = "MYSQL_PASSWORD";

    @Autowired
    private Environment environment;

    @Bean
    public LocalSessionFactoryBean sessionFactory() {
        LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
        sessionFactory.setDataSource(dataSource());
        sessionFactory.setPackagesToScan(new String[] { "com.websystique.springsecurity.model" });
        sessionFactory.setHibernateProperties(hibernateProperties());
        return sessionFactory;
     }
	
    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(environment.getRequiredProperty("jdbc.driverClassName"));
        dataSource.setUrl(environment.getRequiredProperty("jdbc.url"));
        dataSource.setUsername(environment.getRequiredProperty("jdbc.username"));
        dataSource.setPassword(environment.getRequiredProperty("jdbc.password"));

        return loadConfigrationFromEnv(dataSource);
    }
    
    private Properties hibernateProperties() {
        Properties properties = new Properties();
        properties.put("hibernate.dialect", environment.getRequiredProperty("hibernate.dialect"));
        properties.put("hibernate.show_sql", environment.getRequiredProperty("hibernate.show_sql"));
        properties.put("hibernate.format_sql", environment.getRequiredProperty("hibernate.format_sql"));
        properties.put("hibernate.hbm2ddl.auto", environment.getRequiredProperty("hibernate.hbm2ddl.auto"));
        return properties;
    }
    
	@Bean
    @Autowired
    public HibernateTransactionManager transactionManager(SessionFactory s) {
       HibernateTransactionManager txManager = new HibernateTransactionManager();
       txManager.setSessionFactory(s);
       return txManager;
    }

    private DataSource loadConfigrationFromEnv(DriverManagerDataSource dataSource) {
        String url = System.getenv(MYSQL_URL);
        if (!Strings.isNullOrEmpty(url)) {
            dataSource.setUrl(url);
        }

        String userName = System.getenv(MYSQL_USERNAME);
        if (!Strings.isNullOrEmpty(userName)) {
            dataSource.setUsername(userName);
        }

        String password = System.getenv(MYSQL_PASSWORD);
        if (!Strings.isNullOrEmpty(password)) {
            dataSource.setPassword(password);
        }
        return dataSource;
    }
}

