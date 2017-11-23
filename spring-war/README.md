参考源码： http://websystique.com/springmvc/spring-mvc-4-and-spring-security-4-integration-example/

原有demo使用了 spring mvc4 + hibernate4 + spring security, 现在原来的基础上添加了 spring session，以支持分布式session。 session 信息记录在redis中，redis可以使用单点，也可以使用集群。

使用的技术栈：
        
    spring 4.3.1
    spring mvc 4.3.1
    spring security 4.1.1
    spring session 1.3.1
    hibernate 4.3.11
    
依赖Mysql

    docker run -idt --name mysql -p 3306:3306 -v ~/work/docker/config/mysql/my.cnf:/etc/mysql/conf.d/my.cnf -v ~/work/docker/data/mysql:/var/lib/mysql -e MYSQL_ROOT_PASSWORD="root" -e MYSQL_DATABASE="hedwig" mysql:5.7
    
    mysql配置文件my.cnf：
        [mysqld]
        lower_case_table_names=1
        default-storage-engine=INNODB
        character-set-server=utf8
        collation-server=utf8_general_ci
        [client]
        default-character-set=utf8
    
依赖redis:

    docker run --name redis -p 6379:6379 -d redis:4.0.2-alpine redis-server --appendonly yes
    
登陆页面：

  ![image](login.png)
  
列表页面

  ![image](list.png)

新增

  ![image](add.png)
  
更新

  ![image](update.png)