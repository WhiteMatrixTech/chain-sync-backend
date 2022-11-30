# matrix-cloud-ETL-backend

## Prerequisites

- JDK 11
- Docker
- Gradle版本使用每个文件夹内的gradle/gradle-wrapper.properties

## Frontend repository

https://github.com/WhiteMatrixTech/matrix-cloud-ETL-front-public

## Run locally

1. 启动依赖的组件

```
docker-compose up -d
```

- (optional) 验证dynamodb-local是否安装成功

```
npm install -g dynamodb-admin

// For Windows
set DYNAMO_ENDPOINT=http://localhost:8000
dynamodb-admin

// For Mac/Linux
DYNAMO_ENDPOINT=http://localhost:8000 dynamodb-admin
```

安装成功后浏览器打开http://localhost:8001

如果启动SyncerApplication后可以看到如下表的信息

![](dynamodb-admin.png)

打开表matrix-cloud-blockchain-tip-local，添加如下对象

```
{
    "chainId": "mainnet_ethereum",
    "blockNumber": 15988116 
}
```

![](matrix-cloud-blockchain-tip-local.png)

- (optional) 验证kafka-local是否安装成功

根据平台下载并安装kafkatool：https://www.kafkatool.com/download.html

连接配置

![](kafka-viewer-config.png)

启动SyncerApplication后可以看到如下topic的信息

![](kafka-viewer.png)

2. 申请 ethereum network endpoint

可以去https://www.alchemy.com/ 或者 https://www.infura.io/ 创建app并获取endpoint然后填入
matrix-cloud-blockchain-syncer 中application-local下的blockchain:ethereum-provider-endpoint

3. 启动Syncer服务

如果本地环境中没有aws相关配置，在环境变量中设置AWS_ACCESS_KEY_ID=123;AWS_SECRET_KEY=123以跳过aws检查

使用IDEA打开matrix-cloud-blockchain-syncer文件夹，启动matrix-cloud-blockchain-syncer下的SyncerApplication服务
