# EMC Elastic Cloud Storage (ECS) Cloud Foundry Service Broker [![Build Status](https://travis-ci.org/emccode/ecs-cf-service-broker.svg?branch=master)](https://travis-ci.org/spiegela/ecs-cf-service-broker)

## Description

This service broker enables Cloud Foundry applications to create, delete and
modify [ECS](http://emc.com/ecs) object storage buckets; and bind multiple applications to the same
bucket.

## Features

This service broker supports a number of Cloud Foundry and ECS features
including:
 * Create and Delete Object Storage Buckets
 * Bind one or more Cloud Foundry applications to a bucket, with unique credentials and permissions for each application
 * Support quota enforced plans for buckets to limit the amount of capacity
 * Change plans of an existing bucket
 * Browse Cloud Foundry instance and binding metadata through an internal bucket
 * Specify an ECS namespace and replication group for provisioning
 * Provide a string prefix for bucket and user names
 * Support a self-signed SSL certificate for the ECS management API
 * Configure offered services & plans through a YAML based configuration

## Build

To build, make sure that you have a Java 8 runtime environment, and use Gradle.

```
# start up the ecs-simulator to satisfy the test-suite
./gradlew simulate &

# Then build the project
./gradlew build
```

## Configuration

### ECS Configuration

The service broker supports a number of configuration parameters that are available as environment variables or through
Spring configuration.  All parameters are prefixed with the `broker-config.` string.  Default parameters point to the
bundled ECS simulator.  For more info, check the
[default config](https://github.com/spiegela/ecs-cf-service-broker/blob/master/src/main/resources/application.yml).

| Parameter          | Default Value  | Required | Description                                      |
| ------------------ |:--------------:| -------- | ------------------------------------------------ |
| managementEndpoint | -              | true     | Base URL for the ECS API endpoint                |
| objectEndpoint     | -              | true     | Base URL for the ECS Object endpoint             |
| baseUrl            | -              | false    | Base URL name configured in ECS for object calls | 
| repositoryEndpoint | objectEndpoint | false    | Endpoint used for broker metadata storage        |
| replicationGroup   | -              | true     | ID (not name) of replication group               |
| namespace          | -              | true     | ECS Namespace name                               |
| repositoryUser     | user           | false    | Username to authenticate to intenal bucket       |
| username           | root           | false    | Username to authenticate to ECS management API   |
| password           | ChangeMe       | false    | Password to authenticate to ECS management API   |
| repositoryBucket   | repository     | false    | Internal bucket for metadata storage             |
| prefix             | ecs-cf-broker- | false    | Prefix to prepend to ECS buckets and users       |
| brokerApiVersion   | 2.8            | false    | Version of the CF broker API to advertise        |
| certificate        | localhost.pem  | false    | ECS SSL public key cert file                     | 

If running within Eclipse, you can also set the environment variables using "Run Configuration" and "Environment" tabs.

### The ECS Simulator

The ECS Simulator is helpful for ensuring that the application starts up, without actually having an ECS cluster
accessible.  You'll find the
[simulator in the test-suite](https://github.com/spiegela/ecs-cf-service-broker/blob/master/src/test/java/com/emc/ecs/apiSimulator/Server.java).
Just run this file as a Java program, and the broker will be able to initialize against the
"mocked" API calls.

You can also start the simulator from the command-line:

```yaml
./gradlew simulate
```

Soon, we will add simulated support for other Cloud Foundry to broker interactions.

### Self-signed certificates

To load a self-signed certificate for an ECS system, just provide a PEM formatted certificate file named `localhost.pem`
into the `src/main/resources` directory.

## Deploying your broker

Follow the [documentation](http://docs.cloudfoundry.org/services/managing-service-brokers.html) to register the broker
to Cloud Foundry.

### End-user Broker Usage

CLoud Foundry end-users can create and bind services to their applications using the `cf` CLI application.

```
cf create-service ecs-bucket unlimited my_bucket
```

This creates a bucket of the `ecs-bucket` service with the `unlimited` plan and the name: `my-bucket`.  To bind
an application to this bucket:

```
cf bind-service my-app my-bucket
```

The default will give `my-app` "full control" of the bucket.  To give a reduced set of permissions, you can provide
additional configuration parameters with the `-c` flag:

```
cf bind-service my-app my-bucket -c '{"permissions": ["read", "write"]}'
```

Valid permissions include:
 * read
 * read_acl
 * write
 * write_acl
 * execute
 * full_control
 * privileged_write
 * delete
 * none

### Broker Catalog and Plan Configuration

The service broker catalog can be configured through YAML based configuration.  You can create the file manually,
via PCF or another build tool.  Just add a `catalog` section to the `src/main/resources/application.yml` file:

```yaml
catalog:
  services:
    - id: f3cbab6a-5172-4ff1-a5c7-72990f0ce2aa
      name: ecs-bucket
      description: Elastic Cloud S3 Object Storage Bucket
      bindable: true
      planUpdatable: true
      head-type: s3
      file-system-enabled: false
      stale-allowed: true
      tags:
        - s3
        - storage
        - object
      metadata:
        displayName: ecs-bucket
        imageUrl: http://www.emc.com/images/products/header-image-icon-ecs.png
        longDescription: EMC Elastic Cloud Storage (ECS) Object bucket for storing data using Amazon S3, Swift or Atmos protocols.
        providerDisplayName: EMC Corporation
        documentationUrl: https://community.emc.com/docs/DOC-45012
        supportUrl: http://www.emc.com/products-solutions/trial-software-download/ecs.htm
      plans:
        - id: 8e777d49-0a78-4cf4-810a-b5f5173b019d
          name: 5gb
          description: Free Trial
          quota-limit: 5
          quota-warning: 4
          metadata:
            costs:
              - amount:
                  usd: 0.0
                unit: MONTHLY
            bullets:
              - Shared object storage
              - 5 GB Storage
              - S3 protocol and HDFS access
        - id: 89d20694-9ab0-4a98-bc6a-868d6d4ecf31
          name: unlimited
          description: Pay per GB for Month
          metadata:
            costs:
              - amount:
                  usd: 0.03
                unit: PER GB PER MONTH
            bullets:
              - Shared object storage
              - Unlimited Storage
              - S3 protocol and HDFS access
```

For more info, check the
[default config](https://github.com/spiegela/ecs-cf-service-broker/blob/master/src/main/resources/application.yml).

### Broker security

By default the broker is secured with a dynamically generated password ala Spring Security. In order to register with
Cloud Foundry, a user would need to view the output logs, and grab the password with each restart.

To statically set a broker password, simple add the following to the `src/main/resources/application.yml` file:

```yaml
...
security:
  user:
    password: <password>
...
```

## Testing

Local test suite can be run with either a live ECS platform, or using the included simulator.  Configuration variables
can be found and/or changed via the
[EcsActionTest class](https://github.com/spiegela/ecs-cf-service-broker/blob/master/src/test/java/com/emc/ecs/common/EcsActionTest.java).

First start the simulator either within Eclipse, or via the command-line:

```
./gradlew simulate
```

You can then run the test-suite with gradle:

```
./gradlew test
```

## TODOs

Up to date tasks are on our [Github issues](https://github.com/spiegela/ecs-cf-service-broker/issues) page.
