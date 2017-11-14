# ECS Cloud Foundry Service Broker
[![Build Status](https://persi.ci.cf-app.com/api/v1/pipelines/persi/jobs/ecs-broker-unit/badge)](https://persi.ci.cf-app.com/teams/main/pipelines/persi/jobs/ecs-broker-unit) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/1a414678d5bd473685c29b217ae1c7e4)](https://www.codacy.com/app/spiegela/ecs-cf-service-broker?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=emccode/ecs-cf-service-broker&amp;utm_campaign=Badge_Grade)
## Description

This service broker enables Cloud Foundry applications to create, delete and
modify EMC [ECS](http://emc.com/ecs) (Elastic Cloud Storage) object storage buckets & namespaces; and bind multiple applications to the same resources.

## Features

This service broker supports a number of Cloud Foundry and ECS features
including:
 * Create and Delete Object Storage Buckets
 * Create and Delete Object Storage Namespaces
 * Bind one or more Cloud Foundry applications to a bucket or namespace, with unique credentials and permissions for each application
 * Support quota enforced plans for buckets to limit the amount of capacity
 * Support for encryption and retention of namespaces
 * Change plans of an existing bucket
 * Browse Cloud Foundry instance and binding metadata through an internal bucket
 * Specify an ECS namespace and replication group for provisioning
 * Provide a string prefix for bucket and user names
 * Support a self-signed SSL certificate for the ECS management API
 * Configure offered services & plans through a YAML based configuration
 * Support file system mounts of file access enabled buckets via NFS

## Build

To build, make sure that you have a Java 8 runtime environment, and use Gradle.

```
# The ecs-simulator starts automatically when the test-suite is run
./gradlew test

# Then build the project
./gradlew assemble
```

## Configuration

### ECS Configuration

The service broker supports a number of configuration parameters that are available as environment variables or through
Spring configuration.  All parameters are prefixed with the `broker-config.` string.  Default parameters point to the
bundled ECS simulator.  For more info, check the
[default config](https://github.com/codedellemc/ecs-cf-service-broker/blob/master/src/main/resources/application.yml).

| Parameter          | Default Value  | Required | Description                                        |
| ------------------ |:--------------:| -------- | -------------------------------------------------- |
| managementEndpoint | -              | true     | ECS management API URI (https://<ip>:<port>)       |
| replicationGroup   | -              | true     | Name (not ID) of replication group                 |
| namespace          | -              | true     | Default ECS Namespace name                         |
| baseUrl            | -              | false    | ECS Base URL name (otherwise, a default is picked) | 
| objectEndpoint     | -              | false    | Override endpoint for the object endpoint          |
| repositoryEndpoint | objectEndpoint | false    | Override endpoint for broker metadata storage      |
| repositoryUser     | user           | false    | Username to authenticate to intenal bucket         |
| username           | root           | false    | Username to authenticate to ECS management API     |
| password           | ChangeMe       | false    | Password to authenticate to ECS management API     |
| repositoryBucket   | repository     | false    | Internal bucket for metadata storage               |
| prefix             | ecs-cf-broker- | false    | Prefix to prepend to ECS buckets and users         |
| brokerApiVersion   | 2.10           | false    | Version of the CF broker API to advertise          |
| certificate        | -              | false    | ECS SSL public key cert file                       |

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

The simulator is also useful when running certain JUnit tests outside of the TestSuite provided.  To run an individual test that references the ECS API, just start the simulator, and then execute a test. 

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

The following feature flags are supported by the bucket & namespace.  All parameters are optional, and can be set at the service or plan level in the `service-settings` block.  Parameters are observed with the following precedence:  service-definition (in the catalog), plan and then in command-line parameters.  While buckets don't currently support service-settings or command-line parameters for retention, this will be added soon.

| Resource          | Parameter           | Default | Type     |  Description                                   |
| :---------------- | :-------------------| :-----: | :------- | :--------------------------------------------- |
| bucket            | encrypted           | false   | Boolean  | Enable encryption of namespace                 |
| bucket            | access-during-outage| false   | Boolean  | Enable potentially stale data during outage    |
| bucket            | file-accessible     | false   | Boolean  | Enable file-access (NFS, HDFS) for bucket      |
| bucket            | head-type           | s3      | String   | Specify object type (s3, swift) for bucket     |
| bucket            | default-retention   | -       | Int      | Number of seconds to prevent object deletion/modification |
| bucket            | quota*              | -       | JSON Map | Quota applied to bucket                        |            
| bucket binding    | base-url            | -       | String   | Base URL name for object URI                   |
| bucket binding    | use-ssl             | false   | Boolean  | Use SSL for object endpoint                    |
| bucket binding    | permissions         | -       | JSON List| List of permissions for user in bucket ACL     |
| bucket binding    | path-style-access   | true    | Boolean  | Use path style access for S3 URL, the alternative is to use host style access |
| namespace         | domain-group-admins | -       | JSON List| List of domain admins to be added to namespace |
| namespace         | encrypted           | false   | Boolean  | Enable encryption of namespace                 |
| namespace         | compliance-enabled  | false   | Boolean  | Enable compliance adhearance of retention      |
| namespace         | access-during-outage| false   | Boolean  | Enable potentially stale data during outage    |
| namespace         | default-bucket-quota| -1      | Integer  | Default quota applied to bucket (-1 for none)  |            
| namespace         | quota*              | -       | JSON Map | Quota applied to namespace                     |            
| namespace         | retention**         | -       | JSON Map | Retention policies applied to namespace        |
| namespace         | default-retention   | -       | Int      | Number of seconds to prevent object deletion/modification |
| namespace binding | base-url            | -       | String   | Base URL name for object URI                   |
| namespace binding | use-ssl             | false   | Boolean  | Use SSL for object endpoint                    |

\* Quotas are defined with the following format: `{quota: {limit: <int>, warn: <int>}}`

\*\* Retention policies are defined with the following format: `{retention: {<policy name>: <seconds retained>}}` 

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
### Volume Services

Volume services allow bucket contents to be mounted into Cloud Foundry application containers as a file system.  This
enables applications to interact with bucket contents as though they are ordinary files.

#### Prerequisites
There are a few prerequisites you must set up in your Cloud Foundry deployment in order to take advantage of volume
 services.

1) In your Cloud Foundry deployment, the property `cc.volume_services_enabled` must be set to `true`.

2) The `nfsv3driver` job from [nfs-volume-release]() must be running and colocated on your diego cells.  See the README for details.

3) The `ecs-bucket` service in your catalog must be configured to require volume mounts.  The example application.yml
provided in this repo is already set up with this property:

```yaml
...
catalog:
  services:
    - name: ecs-file-bucket
      type: bucket
      requires:
      - volume_mount
...
```

#### File system enabled service instances
In order to use volume services once the prerequisites above are satisfied, your service instances must be created with
`file-accessible` set to true.  This can be set either in the service or service plan exposed from your service catalog,
or manually by the user during service instance creation:

```bash
cf create-service ecs-bucket 5gb mybucket -c '{"file-accessible":true}'
```

Buckets created in this manner will have file system access enabled, and file shares exposed.  When application bindings
are created, new bucket users will be created to correspond to those bindings, and uid mappings will ensure that traffic
coming from the application operates as the correct user.

The application mount point defaults to `/var/vcap/data/{binding-id-guid}` so that is where the file system will appear
within the application container.  You can find this path from within your application programmatically by parsing it from
the VCAP_SERVICES environment variable.  If you prefer to have the volume mounted to a specific path in your application 
container, you can use the `mount` key from within your bind configuration:

```bash
cf bind-service myapp mybucket -c '{"mount":"/var/something"}'
```
> NOTE: As of this writing **aufs** used by Garden is not capable of creating new root level folders.  As a result, you must choose a path with a root level folder that already exists in the container.  (`/home`, `/usr` or `/var` are good choices.)  If you require a path that does not already exist in the container it is currently only possible if you upgrade your Diego deployment to use [GrootFS](https://github.com/cloudfoundry/grootfs-release) with Garden. Soon, GrootFS will become the standard file system for CF containers, and this limitation will go away.

## Testing

Local test suite can be run with either a live ECS platform, or using the included simulator.  Configuration variables
can be found and/or changed via the
[EcsActionTest class](https://github.com/spiegela/ecs-cf-service-broker/blob/master/src/test/java/com/emc/ecs/common/EcsActionTest.java).

You can then run the test-suite with gradle:

```
./gradlew test
```

## TODOs

Up to date tasks are on our [Github issues](https://github.com/codedellemc/ecs-cf-service-broker/issues) page.
