# matrix-cloud-ETL

The Java environment is jdk11

The Gradle version uses gradle/gradle-wrapper.properties inside each folder

## Run locally

Install the necessary local environment through docker and docker-compose

docker-compose repository address：https://github.com/WhiteMatrixTech/dev-in-docker

### Install DynamoDB local

```
cd dynamodb
docker-compose up -d
```

Verify that dynamodb-local is installed successfully (optional)

```
npm install -g dynamodb-admin

// For Windows
set DYNAMO_ENDPOINT=http://localhost:8000
dynamodb-admin

// For Mac/Linux
DYNAMO_ENDPOINT=http://localhost:8000 dynamodb-admin
```

After the installation is successful, the browser opens http://localhost:8001

If you start SyncerApplication, you can see the information in the following table

![](dynamodb-admin.png)

Open the table matrix-cloud-blockchain-tip-local and add the following objects:

{
"chainId": "mainnet_ethereum",
"blockNumber": 15988116 }

![](matrix-cloud-blockchain-tip-local.png)

### Install a local Apache Kafka

```
cd kafka

//Modify parameters according to readme

docker-compose up -d
```

Verify that kafka-local is installed successfully (optional)

Download and install kafka tool according to the platform：https://www.kafkatool.com/download.html

Connection configuration

![](kafka-viewer-config1.png)

![](kafka-viewer-config2.png)

After starting SyncerApplication, you can see the following information

![](kafka-viewer.png)

### Install s3-local

```
cd s3
docker-compose up -d
```

Verify that s3-local is installed successfully (optional)

### Install Redis locally

```
cd redis
docker-compose up -d
```

### Apply for Ethereum network endpoint

Visit https://www.alchemy.com/ or https://www.infura.io/ to create an app and get the endpoint and
fill it in blockchain:ethereum-provider-endpoint under application-local in
matrix-cloud-blockchain-syncer.

## Start the Syncer service

If there is no AWS-related configuration in the local environment, set AWS_ACCESS_KEY_ID=123;
AWS_SECRET_KEY=123 in the environment variable to skip the AWS check.

Use IDEA to open the matrix-cloud-blockchain-syncer folder and start the SyncerApplication service
under matrix-cloud-blockchain-syncer.
