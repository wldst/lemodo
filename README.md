# lemodo
A real-time, graph-based management platform built on SpringBoot and Neo4j. Deployable as a single JAR, it provides lightning-fast CRUD operations with dynamically configurable interfaces. Its core innovation is a single, reusable CRUD codebase capable of handling any data model and arbitrary node relationships within the graph database.

这是一个基于图数据Neo4j的增删改查实时生效的管理平台。是一个SpringBoot工程。可以集成到任何微服务工程中去。单独一个SpringBoot 启动jar包。即可实现：
极速增删改查，实时配置增删改查的相关界面。基于图数据库，可以添节点之间任意关系。本工程的特点是：增删改查尽量是一套代码。一套增删改查即可覆盖任意的增删改查。


Effortless, ultra-fast CRUD generation. Real-time configuration and instant activation of CRUD features. Includes an embedded Neo4j graph database. No need to write Controller, Service, Dao, Mapper, or other layered code to enjoy real-time CRUD services. Provides efficient data services for any microservice. Even users without programming experience can easily implement basic CRUD functions.

Built on graph databases and metadata, it can become a self-evolving data platform. Combined with LLMs, it enables intelligent CRUD data services.
超级容易、极速生成增删改查的功能。可实时配置并生效增删改查相关功能。自带嵌入式图数据库Neo4j，不用编写Controller，Service，Dao，Mapper等各层代码即可享受实时的增删改查服务。为任意微服务提供高效的数据服务。没有编程经验的人也可以轻易实现简单的增删改查功能。基于图数据库和元数据，可成为一个自动进化和生长的数据平台。结合LLM，实现智能化的增删改查数据服务。

# 技术栈：
  JAVA(JDK19)，SpringBoot3，Neo4j，规则引擎，脚本引擎BeanShell。

# 背景
此工程开发与2019年，至今已有6年时间了。最近决定分享出来。与LLM结合：AI前端+本工程，可以极速实现增删改查。智能增删改查。

# 应用场景
开发业务微服务：要结合sdk:api.jar,即我另外的[api](https://github.com/wldst/api)工程，即可实现业务UI微服务+业务后端微服务+lemodo（CRUD）。
# 主要效果 和作用
  无需自己在安装数据相关的，启动本工程。
  即可实现极速增删改查
  可以为任意的微服务提供增删改查数据服务。
有了[Lemodo](https://github.com/wldst/lemodo)工程后，可以为你省下基础的增删改查代码例如：Dao，Mapper,省下基本的增删改的Controller、Service、Dao等代码，可以省下相关VO，DTO等。
本工程是领域数据驱动，降低绝大部分的业务实体类的定义。不被OOP约束，所有的业务对象都可以基于元数据（MetaData）和Map来实现。

# 设计
本项目的数据架构，从前到后的数据格式：
前端：GET/POST JSON
后端：
Controller:JSON,Map,List<Map<String,Object>>
Service:参数Map,List<Map<String,Object>>,String cypher,url等
支持用Cypher查询。
有丰富的应用，支持分页查询，模糊查询，前端界面：增删改查列表。

# 使用
在本地建一个目录：例如：D:/Test，cd D:/Test
git clone https://github.com/wldst/lemodo.git
使用Eclipse，idea都可以打开此项目。相关端口配置，和SpringBoot相关项目一样。

这是一个SpringBoot项目，启动类：LemodoApplication
jvm添加参数：--add-opens
						java.desktop/java.awt.font=ALL-UNNAMED --add-opens
						java.base/java.text=ALL-UNNAMED --add-opens
						java.base/java.lang.reflect=ALL-UNNAMED --add-opens
						java.base/java.util=ALL-UNNAMED --add-opens
						java.base/sun.nio.ch=ALL-UNNAMED --add-opens
						java.base/java.lang=ALL-UNNAMED
启动后会根据neo4j.db.path的属性值，在指定目录创建嵌入式数据库。
首次启动会有6分钟左右的数据初始化，具体情况参考file/data/中的初始化数据的数量大小来定。
初始化完成后会在默认浏览器中打开：https://127.0.0.1:9443/root/login
默认账号和密码：admin/admin2023

本工程定位：后端数据管理，提供数据服务。以及相关接口。是持久化的微服务。
# 捐赠：
您的捐赠是我们开源最大的动力
ETH/USDT(以太坊/USDT)：0x26a96339A0b0f3304070C95dEDb1E0967C9875f6
觉得好用了，赏口饭吃。我的微信收款码。
![3ed2121a9a2468b5eea771e00fe6b0a](https://github.com/user-attachments/assets/d34315b4-58bf-43ee-add4-845dc9bc7095)





