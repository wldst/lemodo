# lemodo
Java easy crud SpringBoot Project,online change crud effect just in time.

这是一个基于图数据Neo4j的增删改查实时生效的管理平台。是一个SpringBoot工程。可以集成到任何微服务工程中去。单独一个SpringBoot 启动jar包。即可实现：
极速增删改查，
实时配置增删改查的相关界面。

基于图数据库，可以添节点之间任意关系。本工程的特点是：增删改查尽量是一套代码。一套增删改查即可覆盖任意的增删改查。

技术栈：JAVA23，SpringBoot3，Neo4j，规则引擎，脚本引擎BeanShell。

此工程开发与2019年，至今已有6年时间了。最近决定分享出来。

AI前端+本工程，可以极速实现增删改查。智能增删改查。

拿到此工程，后需要结合api工程，即可实现UI-业务微服务+lemodo。

有了Lemodo工程后，可以为你省下：Dao，Mapper,省下基本的增删改的Controller、Service、Dao等代码，可以省下VO，DTO等。

从前到后的数据格式：
前端：GET/POST JSON
后端：
Controller:JSON,Map,List<Map<String,Object>>
Service:参数Map

本工程是领域数据驱动，降低绝大部分的业务实体类的定义。

支持用Cypher查询。
有丰富的应用，支持分页查询，模糊查询，前端界面：增删改查列表。

本工程定位：后端数据管理，提供数据服务。以及相关接口。是持久化的微服务。
2025年6月18日01:39:14：今天先分享到此，后续在继续分享。


