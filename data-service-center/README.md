数据访问基础能力
通过引入jar包使用;

````
步骤1：
拉去代码。在starter 代码中
引入以下jar包，同时设定data-all-version 的版本为 1.0-SNAPSHOT
<properties>
		<java.version>1.8</java.version>
		<data-all-version>1.0-SNAPSHOT</data-all-version>
	</properties>
	
<!--common -start-->
		<dependency>
			<groupId>com.huaan</groupId>
			<artifactId>data-service-common</artifactId>
			<version>${data-all-version}</version>
			<type>pom</type>
		</dependency>
		<dependency>
			<groupId>com.huaan</groupId>
			<artifactId>data-service-center-runtime</artifactId>
			<version>${data-all-version}</version>
		</dependency>
		<dependency>
			<groupId>com.huaan</groupId>
			<artifactId>data-service-center-sdk</artifactId>
			<version>${data-all-version}</version>
		</dependency>
		<dependency>
			<groupId>com.huaan</groupId>
			<artifactId>data-service-center-share</artifactId>
			<version>${data-all-version}</version>
		</dependency>
		<dependency>
			<groupId>com.huaan</groupId>
			<artifactId>plugin-rdbms-util</artifactId>
			<version>${data-all-version}</version>
		</dependency>
		<!--common -end -->
		
步骤2、创建controller 。定义接口输出。
引入 如下service 
能力使用controller 代码如下：


@RestController
public class CommonController {

    @Autowired
    private CommonDBService commonDBService;

    /**
     * @param dBinfo 
     * @return
     */
    @PostMapping("/queryRecordList")
    public ResponseEntity getRecordList(@RequestBody DBinfo dBinfo) {
        List<JSONObject> results=commonDBService.getRecordList(dBinfo);
        return ResponseEntity.ok(results);
    }
   }
````

优先默认以querySql 为查询语句。
入参示例1：
````
{
    "username":"root",
    "password":"chenks",
    "databaseType":"mysql",
    "querySql":"select id from jks_dict_item acode limit 10",
    "where":null,
    "columns":null,
    "table":null,
                "jdbcUrl": "jdbc:mysql://127.0.0.1:3306/chip-data-center?useUnicode=true&characterEncoding=utf-8&serverTimezone=GMT%2B8&useSSL=false&allowPublicKeyRetrieval=true&rewriteBatchedStatements=true"
}
````

入参示例2：
````
{
    "username":"root",
    "password":"chenks",
    "databaseType":"mysql",
    "querySql":"select *  from jks_dict_item where id = ?" ,
    "prepareValue":["feaff"],
                "jdbcUrl": "jdbc:mysql://127.0.0.1:3306/chip-data-center?useUnicode=true&characterEncoding=utf-8&serverTimezone=GMT%2B8&useSSL=false&allowPublicKeyRetrieval=true&rewriteBatchedStatements=true"
}
````