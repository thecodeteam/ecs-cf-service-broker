# EMC Elastic Cloud Storage (ECS) Cloud Foundry Service Broker

## Description

This service broker enables Cloud Foundry applications to create, delete and
modify [ECS](http://emc.com/ecs) object storage buckets; and bind multiple applications to the same
bucket.

## Features

This service broker supports a number of Cloud Foundry and ECS features
including:
 * Create and Delete Object Storage Buckets
 * Bind one or more Cloud Foundry applications to a bucket, with unique credentials for each application
 * Support quota enforced plans for buckets to limit the amount of capacity
 * Change plans of an existing bucket
 * Browse Cloud Foundry instance and binding metadata through an internal bucket
 * Specify an ECS namespace and replication group for provisioning
 * Provide a string prefix for bucket and user names
 * Support a self-signed SSL certificate for the ECS management API

## Build

To build, make sure that you have a Java 8 runtime environment, and use Gradle.

```
./gradlew build
```

## Configuration

### ECS Configuration

The service broker supports a number of configuration parameters that are available as environment variables or through
Spring configuration.  An example YAML file is provided pointing to the local ECS simulator:

| Parameter          | Default Value  | Required | Description                                    |
| ------------------ |:--------------:| -------- | ---------------------------------------------- |
| managementEndpoint | -              | true     | Base URL for the API endpoint                  |
| replicationGroup   | -              | true     | ID (not name) of replication group             |
| namespace          | -              | true     | ECS Namespace name                             |
| repositoryUser     | user           | false    | Username to authenticate to intenal bucket     |
| username           | root           | false    | Username to authenticate to ECS management API |
| password           | ChangeMe       | false    | Password to authenticate to ECS management API |
| repositoryBucket   | repository     | false    | Internal bucket for metadata storage           |
| prefix             | ecs-cf-broker- | false    | Prefix to prepend to ECS buckets and users     |
| brokerApiVersion   | 2.8            | false    | Version of the CF broker API to advertise      |

If running within Eclipse, you can also set the environment variables using "Run Configuration" and "Environment" tabs.

### The ECS Simulator

The ECS Simulator is helpful for ensuring that the application starts up, without actually having an ECS cluster
accessible.  You'll find the
[simulator in the test-suite](https://github.com/spiegela/ecs-cf-service-broker/blob/master/src/test/java/com/emc/ecs/apiSimulator/Server.java).
Just run this file as a Java program, and the broker will be able to initialize against the
"mocked" API calls.

Soon, we will add simulated support for other Cloud Foundry to broker interactions.

### Self-signed certificates

To load a self-signed certificate for an ECS system, just provide a PEM formatted certificate file named `localhost.pem`
into the `src/main/resources` directory.

### Broker Catalog and Plan Configuration

The service broker catalog can be configured through the
[CatalogConfig](https://github.com/spiegela/ecs-cf-service-broker/blob/master/src/main/java/com/emc/ecs/serviceBroker/config/CatalogConfig.java)
class.  This will be replaced with a YAML based configuration in the future, so that it can be generated dynamically as
part of PCF or another build tool.

### Broker security

By default the broker is secured with a dynamically generated password ala Spring Security. In order to register with
Cloud Foundry, a user would need to view the output logs, and grab the password with each restart.

To statically set a broker password, simple add the following to the `src/main/resources/application.yml` file:

```yaml
security:
  user:
    password: <password>
```

## Deploying your broker

Follow the [documentation](http://docs.cloudfoundry.org/services/managing-service-brokers.html) to register the broker
to Cloud Foundry.

## Testing

Local test suite can be run with either a live ECS platform, or using the included simulator.  Configuration variables
can be found and/or changed via the
[EcsActionTest class](https://github.com/spiegela/ecs-cf-service-broker/blob/master/src/test/java/com/emc/ecs/common/EcsActionTest.java).

You can then run the test-suite with gradle:

```
./gradlew test
```

Or within Eclipse.  It might work fine in other IDEs, but I've only run them in Eclipse.

## Todos
 * Add FS-enabled and CAS bucket types to plans
 * Add parameters to bindings for user ACLs (read-only, read-write, etc.)
 * Add support for dynamically generated plans as done the
 [MySQL service broker](https://github.com/cloudfoundry/cf-mysql-broker) [wip]
 * Delete ACLs from bucket (in addition to the user) when removing bindings
 * Automatic support for multiple Cloud Foundry instances as
 [shown here](http://docs.cloudfoundry.org/services/supporting-multiple-cf-instances.html)
 * Support syslog drain URL for logging
 * Support TLS for internal S3 bucket with self-signed cert (requires ECS 2.1.1) [wip]
 * Add BOSH release project and integration for PCF Tile
 * Add simulator support for CF created services & bindings