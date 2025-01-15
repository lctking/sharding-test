# sharding-test
分库分表学习记录
位置：E:\codebase\sharding-test

### 1，pom依赖：
shardingsphere,mybatis-plus,mybatis-spring,mysql
```
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core</artifactId>
    <version>5.3.2</version>
</dependency>
<dependency>
    <groupId>com.baomidou</groupId>
    <artifactId>mybatis-plus-boot-starter</artifactId>
    <version>3.5.5</version>
    <exclusions>
        <exclusion>
            <artifactId>mybatis-spring</artifactId>
            <groupId>org.mybatis</groupId>
        </exclusion>
    </exclusions>
</dependency>
<dependency>
    <groupId>org.mybatis</groupId>
    <artifactId>mybatis-spring</artifactId>
    <version>3.0.3</version>
</dependency>
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <scope>runtime</scope>
    <version>5.1.47</version>
</dependency>
```



### 2，配置文件书写：
#### 2.1，在application.yaml中写：shardingsphere的配置文件路径和使用的shardingsphere驱动类  
![image](https://github.com/user-attachments/assets/ea1b268e-e77f-45e5-b090-4a86cbcde77f)

#### 2.2，shardingsphere-config.yaml中写具体的分库分表配置（分片策略、库表的路径）  
![image](https://github.com/user-attachments/assets/1841c9fc-7245-47dd-ab98-20f226dfaf49)

##### 2.2.1 dataSources（数据源，个人理解是数据库）配置：
ds_0,ds_1,ds_2...ds_n，表示任意个数据库的配置

##### 2.2.2 rules（分片的规则）：  
###### 2.2.2.1 tables表规则：
![image](https://github.com/user-attachments/assets/5e6414f6-0bcc-4952-801a-5f4ad67d7a1e)

###### 在tables下写t_user表示接下来声明t_user表的分片规则；
- 1，actualDataNodes：ds_${0..2}.t_user_${0..5}表示数据节点范围是数据库ds_0到ds_2，包含的数据表范围是t_user_0到t_user_5；
	注意，这里的t_user_0-5不是每个ds下都有六个表，而是ds_0-2三个数据库一共六个t_user表。
- 2，databaseStrategy数据库（分片）策略，这里选取了complex复杂分片（这样可以进行复合分片，即根据两个或以上的字段进行分片。与complex相反的是standard普通分片）。
- 3，shardingColumns：声明分片使用的字段有哪些。注意shardingColumns的单复数（>1数量的字段要使用复数）
- 4，shardingAlgorithmName：声明要使用什么分片算法。（这里的分片算法名是自定义的，下文会提及）
###### 2.2.2.2 shardingAlgorithms分片算法：
![image](https://github.com/user-attachments/assets/2d2eb1c0-9a31-4a1a-8f25-52350d448b61)

###### 这部分用来配置自定义分片算法。
- user_database_complex_mod：是上文使用到的分片(分库)算法。接下来做相应的配置。
- type：CLASS_BASED，表示该算法是基于类的（即算法在类中书写、实现）
- props：属性配置，这部分内容可以在算法类中访问到，灵活性比较大。例如asia-sharding-table-nodes到table-sharding-count四个字段都是我自定义的。
	- asia-sharding-table-nodes：表示亚洲地区数据表范围是t_user_0到3，这里没有写ds数据库的范围，是因为默认了每个数据库下面的t_user表数量是均匀的，即都是table-sharding_count（自定义的，表示一个库下t_user表的数量）数量个表，因此“{0..3}”反映出ds_0-ds_1都是asia分片下的数据库。
	- sharding-count：表示总分片数量（也即是t_user表的总数量）
	- strategy：complex复杂分片策略
	- algorithmClassName：分片算法实现类的全类名




### 3，自定义复合分片算法：
详见项目中的代码和相关注释

这里的参数是什么意思：public Collection<String> doSharding(Collection availableTargetNames, ComplexKeysShardingValue shardingValues)   
ChatGPT 说：    
方法 public Collection<String> doSharding(Collection availableTargetNames, ComplexKeysShardingValue shardingValues) 是 ShardingSphere 框架中一个典型的分片算法接口的实现方法。以下是该方法的两个参数的含义和作用：

参数 1: Collection availableTargetNames
• 类型: Collection<String>
• 含义:
o 表示可用的目标名称集合，例如数据库实例名或表名。
o 这些目标名称是 ShardingSphere 在运行时解析出的候选目标，分片算法需要从中筛选出具体的目标。
• 典型场景:
o 数据库分片：目标名称可能是多个数据源名（如 ds_0, ds_1, ds_2）。
o 表分片：目标名称可能是多个逻辑表名（如 t_user_0, t_user_1, t_user_2）。
• 示例: 假设一个逻辑表 t_user 分成了 6 个分片表（t_user_0 到 t_user_5），availableTargetNames 的值可能是：
• ["t_user_0", "t_user_1", "t_user_2", "t_user_3", "t_user_4", "t_user_5"]



参数 2: ComplexKeysShardingValue shardingValues
• 类型: ComplexKeysShardingValue
• 含义:
o 包含分片所需的关键信息，例如：
1. 逻辑表名：当前分片操作的逻辑表名。
2. 分片字段和对应的值：表示分片键的名称和它的值，可能包括多个分片字段。
3. 操作类型：当前分片操作是查询、插入还是其他操作。
o ComplexKeysShardingValue 支持多个分片键（字段）的分片场景。
• 常用方法:
o shardingValues.getLogicTableName():
1. 返回当前分片操作的逻辑表名。
o shardingValues.getColumnNameAndShardingValuesMap():
1. 返回一个 Map，其中键是分片字段名，值是该字段的具体值集合。
2. 例如：
3.
  ```
{
 
    "user_id": [1001, 1002],
 
    "order_id": [20001, 20002]
}
```  
o shardingValues.getColumnNameAndRangeValuesMap():  
1. 如果分片键的值是一个范围（如 user_id BETWEEN 100 AND 200），可以通过此方法获取范围信息。




