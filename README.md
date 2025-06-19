# lemodo
Java easy crud SpringBoot Project,online change crud effect just in time.CRUD operate base Neo4j。no POJO,no Controller，Service，Dao,no Mapper,no Entity，cut them out. even no java Code。modify online，view online effective result. People who with no program experience. can implements any CRUD operate of Node. you can create relationship between startNode and endNode. you can create view of node,property,relationship. with no code.

这是一个基于图数据Neo4j的增删改查实时生效的管理平台。是一个SpringBoot工程。可以集成到任何微服务工程中去。单独一个SpringBoot 启动jar包。即可实现：
极速增删改查，实时配置增删改查的相关界面。基于图数据库，可以添节点之间任意关系。本工程的特点是：增删改查尽量是一套代码。一套增删改查即可覆盖任意的增删改查。

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

本工程定位：后端数据管理，提供数据服务。以及相关接口。是持久化的微服务。
# 捐赠：
您的捐赠是我们开源最大的动力
ETH/USDT(以太坊/USDT)：0x26a96339A0b0f3304070C95dEDb1E0967C9875f6


