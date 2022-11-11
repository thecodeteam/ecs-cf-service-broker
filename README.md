# ECS Open Service Broker

## Description

This service broker enables Kubernetes & Cloud Foundry applications to create,
delete and modify Dell EMC [ECS](https://www.delltechnologies.com/en-us/storage/ecs/index.htm) (Elastic Cloud Storage) 
and Dell [Objectscale](https://www.dell.com/en-us/dt/storage/objectscale.htm) object storage buckets & namespaces; 
and bind multiple applications to the same resources.

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
 * Support for multiple PCF instances to share data through ECS buckets and namespaces

## Build

To build, make sure you have a Java 11 runtime environment, and use Gradle.

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
[example config](https://github.com/codedellemc/ecs-cf-service-broker/blob/master/src/test/resources/application-test.yml).

| Parameter          | Default Value  | Required | Description                                        |
| ------------------ |:--------------:| -------- | -------------------------------------------------- |
| managementEndpoint | -              | true     | ECS management API URI (https://<ip>:<port>)       |
| replicationGroup   | -              | true     | Name (not ID) of replication group                 |
| namespace          | -              | true     | Default ECS Namespace name                         |
| baseUrl            | -              | false    | ECS Base URL name (DefaultBaseUrl is picked if value is not provided) | 
| useSsl             | false          | false    | Whether to use HTTPS for object endpoint connections (when base url is provided) | 
| objectEndpoint     | -              | false    | Override endpoint for the object endpoint          |
| repositoryEndpoint | objectEndpoint | false    | Override endpoint for broker metadata storage      |
| repositoryUser     | user           | false    | Username to authenticate to intenal bucket         |
| username           | root           | false    | Username to authenticate to ECS management API     |
| password           | ChangeMe       | false    | Password to authenticate to ECS management API     |
| repositoryBucket   | repository     | false    | Internal bucket for metadata storage               |
| prefix             | ecs-cf-broker- | false    | Prefix to prepend to ECS buckets and users         |
| brokerApiVersion   | *              | false    | Version of the CF broker API to advertise          |
| certificate        | -              | false    | ECS SSL public key cert file                       |
| ignoreSslValidation | -             | false    | Allows to ignore SSL validation errors             |

If running within IDE, you can set the environment variables within "Run Configuration".

### Objectscale Configuration

When connecting to Objectscale, Service Broker requires different set of parameters:  

| Parameter          | Default Value  | Required | Description                                        |
| ------------------ |:--------------:| -------- | -------------------------------------------------- |
| apiType            | objectscale    | true     | Storage API type (ECS or Objectscale)              |
| username           | root           | true     | Objectscale management user name                   |
| password           | ChangeMe       | true     | Objectscale management user password               |
| objectscaleId      | -              | true     | Objectscale ID                                     |
| objectscaleGatewayEndpoint  | -     | true     | Objectscale Gateway endpoint url                   |
| objectstoreId      | -              | true     | Objectstore ID                                     |
| objectstoreName    | -              | true     | Objectstore name                                   |
| objectstoreManagementEndpoint | -   | true     | Objectstore management endpoint url                |
| objectstoreS3Endpoint   | -         | true     | Objectstore S3 endpoint url                        |
| accountId          | -              | true     | Account ID                                         |
| accessKey          | -              | true     | Access key of a pre-created IAM user with S3 access |
| secretKey          | -              | true     | Secret key of a pre-created IAM user with S3 access |
| repositoryBucket   | repository     | false    | Internal bucket name for metadata storage          |
| prefix             | ecs-cf-broker- | false    | Prefix to prepend to ECS buckets and users         |
| brokerApiVersion   | *              | false    | Version of the CF broker API to advertise          |
| certificate        | -              | false    | ECS SSL public key cert file                       |
| ignoreSslValidation | -             | false    | Allows to ignore SSL validation errors             |   

### The ECS Simulator

The ECS Simulator is helpful for ensuring that the application starts up, without actually having an ECS cluster
accessible.  You'll find the
[simulator in the test-suite](https://github.com/thecodeteam/ecs-cf-service-broker/tree/master/src/test/java/com/emc/ecs/management/simulator/Server.java).
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

#### Install using the PCF Tile

Download the [ECS Service Broker tile from Tanzu Network](https://network.pivotal.io/products/ecs-service-broker/), and
and follow the [installation instructions](https://docs.pivotal.io/partners/ecs-service-broker/installing.html) found there.

#### End-user Broker Usage

Cloud Foundry end-users can create and bind services to their applications using the `cf` CLI application.

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

Service plan can be changed using `update-service` command. Only a subset of service attributes are applied:
* Buckets: quota and default retention
* Namespaces: bucket quota, retention policies, ADO, compliance and domain group admins 

More detailed instructions for using the broker in Cloud Foundry can be found in the
[Tanzu Network ECS broker documentation](https://docs.pivotal.io/partners/ecs-service-broker/using.html).

### Broker Catalog and Plan Configuration

The service broker catalog can be configured through YAML based configuration.  You can create the file manually,
via PCF or another build tool.  Just add a `catalog` section to the `src/main/resources/application.yml` file:

The following feature flags are supported by the bucket & namespace. All parameters are optional, and can be set at the service or plan level in the `service-settings` block. Parameters are observed with the following precedence:  service-definition (in the catalog), plan and then in command-line parameters.

| Resource          | Parameter            | Default | Type     | Description                                                                   |
|:------------------|:---------------------|:-------:| :------- |:------------------------------------------------------------------------------|
| bucket            | namespace            |  false  | String   | Namespace where bucket will be created                                        |
| bucket            | replication-group    |  false  | String   | Replication group for bucket                                                  |
| bucket            | encrypted            |  false  | Boolean  | Enable encryption of bucket                                                   |
| bucket            | access-during-outage |  false  | Boolean  | Enable potentially stale data during outage                                   |
| bucket            | ado-read-only        |  false  | Boolean  | Only allow read access during outage (requires ADO to be enabled)             |
| bucket            | file-accessible      |  false  | Boolean  | Enable file-access (NFS, HDFS) for bucket                                     |
| bucket            | head-type            |   s3    | String   | Specify object type (s3, swift) for bucket                                    |
| bucket            | default-retention    |    -    | Int      | Number of seconds to prevent object deletion/modification                     |
| bucket            | quota*               |   -     | JSON Map | Quota applied to bucket                                                       |
| bucket            | remote_connection*** |    -    | JSON Map | Remote connection details for previously created bucket                       |
| bucket            | name                 |    -    | String   | String to add to bucket name after the broker prefix (prefix-name-id)         |
| bucket            | tags****             |    -    | JSON List| List of tags that are key-value pairs associated with a bucket                |
| bucket            | search-metadata***** |    -    | JSON List| List of search metadata that are used for bucket indexing                     |
| bucket binding    | base-url             |    -    | String   | Base URL name for object URI                                                  |
| bucket binding    | permissions          |    -    | JSON List| List of permissions for user in bucket ACL                                    |
| bucket binding    | path-style-access    |  true   | Boolean  | Use path style access for S3 URL, the alternative is to use host style access |
| bucket binding    | name                 |    -    | String   | String to add to binding name after the broker prefix (prefix-name-id)        |
| namespace         | replication-group    |  false  | String   | Replication group of namespace                                                |
| namespace         | domain-group-admins  |    -    | JSON List| List of domain admins to be added to namespace                                |
| namespace         | encrypted            |  false  | Boolean  | Enable encryption of namespace                                                |
| namespace         | compliance-enabled   |  false  | Boolean  | Enable compliance adhearance of retention                                     |
| namespace         | access-during-outage |  false  | Boolean  | Enable potentially stale data during outage                                   |
| namespace         | default-bucket-quota |   -1    | Integer  | Default quota applied to bucket (-1 for none)                                 |            
| namespace         | quota*               |    -    | JSON Map | Quota applied to namespace                                                    |            
| namespace         | retention**          |    -    | JSON Map | Retention policies applied to namespace                                       |
| namespace         | default-retention    |    -    | Int      | Number of seconds to prevent object deletion/modification                     |
| namespace         | remote_connection*** |    -    | JSON Map | Remote connection details for previously created namespace                    |
| namespace         | name                 |    -    | String   | String to add to namespace name after the broker prefix (prefix-name-id)      |
| namespace binding | base-url             |    -    | String   | Base URL name for object URI                                                  |
| namespace binding | use-ssl              |  false  | Boolean  | Use SSL for object endpoint                                                   |
| namespace binding | name                 |    -    | String   | String to add to binding name after the broker prefix (prefix-name-id)        |

NOTE: When working with Objectstore, Service Broker does not support Namespace services!

\* Quotas are defined with the following format: `{quota: {limit: <int>, warn: <int>}}`

\*\* Retention policies are defined with the following format: `{retention: {<policy name>: <seconds retained>}}` 

\*\*\* Remote connection details are describe below in _remote connections_

\*\*\*\* Tags are defined with the following format: `{tags:[{"key": <str>,"value": <str>}, ...]}`.
Details about bucket tagging could be found below in _Bucket Tags_ section 

\*\*\*\*\* List of search metadata is defined with following format: `{"search_metadata": [{"type": <str>,"name": <str>,"datatype": <str>>}, ...]}` 
Details about search metadata definition are described below in _Search metadata_ section 

For more info, check the
[example config](https://github.com/thecodeteam/ecs-cf-service-broker/blob/master/src/test/resources/application-test.yml).

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

Information provided in response of binding creation request includes s3 URL, secret credentials and user Unix ID.

The application mount point defaults to `/var/vcap/data/{binding-id-guid}` so that is where the file system will appear
within the application container.  You can find this path and corresponding uid from within your application programmatically 
by parsing it from the VCAP_SERVICES environment variable.  If you prefer to have the volume mounted to a specific path 
in your application container, you can use the `mount` key from within your bind configuration:

```bash
cf bind-service myapp mybucket -c '{"mount":"/var/something"}'
```

### Remote Connections to Service Instances

A common use case for object storage, in general, and ECS specifically, is to share
data across data-centers, platforms, Cloud Foundry instances, or clouds using a shared
object-storage bucket. The ECS Service Broker supports with with a specialized feature,
called _"remote connections"_. This features allows you to share services across Cloud
Foundry, or other service-broker-enabled platforms securely, and without user-provided-services,
which can be error-prone.

To share a bucket or namespaces between Cloud Foundry instances, use the following steps:

1. Create the ECS service at the original site or CF instance:

```bash
pcf-1$ cf create-service ecs-bucket 5gb mybucket
```

After creating the services, you may bind it to local applications as described above.

2. Create a "remote connection" service-key

```bash
pcf-1$ cf create-service-key mybucket pcf2Key -c '{"remote_connection": true}'
```

3. List the remote connection service key details:

```bash
pcf-1$ cf service-key mybucket pcf2Key
Getting key pcf2Key for service instance mybucket as user...
{
  "accessKey": "...",
  "instanceId": "...",
  "secretKey": "..."
}
```

These service keys can be used by administrators to connect the service instance to any
number of Cloud Foundry instances or other service broker enabled platforms, such as
Pivotal Container Service.

4. Connect to a separate Cloud Foundry instance, and create the remote instance of the
service:

```bash
pcf-2$ cf create-service ecs-bucket 5gb mybucket -c '{"remote_connection": {"accessKey": "...", "instanceId": "...", "secretKey": "..."}}'
```

When using multiple service-brokers, ensure that the service and plan definitions
(defined in the broker catalog) are the same between the deployed brokers. If the catalog
definitions differ between the two, the broker will return an error to the user, and
decline to remotely connect the service instance.

At this point, the service is created at the remote instance, and can be bound to
apps.

#### Service Deletions with Remote Connections

When deleting a service instance through a Cloud Foundry command such as:

```bash
$ cf delete-service mybucket
```

The broker will only delete the actual service instance (bucket or namespace) once all
remote connections have been removed. Prior to that, the service instance will be removed
from the Cloud Foundry database, and the remote connection will be removed from the ECS
broker metadata for that instance.

#### Changing Plans with Remote Connections

Changing/upgrading plans with remote connections is currently disabled, as it would leave
one of the Cloud Foundry instances out of date.

### Search Metadata

Search metadata is used for ECS Object Storage bucket indexing and allows to lookup objects without iterating through bucket contents.

Metadata fields number is limited to 30, and must be defined when bucket is created.
_Note:_ plan changes will disable metadata search if plans defines different metadata field sets! 

Each metadata field definition consists of three fields: type, name and datatype.

```yaml
...
search-metadata: 
- type: <type>
  name: <name>
  datatype: <datatype>
...
```
There are two types of search metadata provided in the ECS buckets: _System_ and _User_.

#### System search metadata

ECS Documentation provides following system metadata:

| Name (Alias)    | Data Type | Description                                           |
| :-------------- | :---------| :---------------------------------------------------- |
| ObjectName      | String    |  Name of the object.                                  |
| Owner           | String    |  Identify the owner of the object.                    |
| Size            | Integer   |  Size of the object.                                  |
| CreateTime      | DateTime  |  Time at which the object was created.                |
| LastModified    | DateTime  |  Time and date at which the object was last modified. |

System search metadata provided in `service-settings` block should be tagged with _System_ type, existing name and corresponding datatype.

#### User search metadata

User defined search metadata should be tagged with _User_ type, existing datatype and name starting with prefix `x-amz-meta-`.
Prefix will be added to field name if it is not already there. 

#### Metadata Search Datatypes
When writing metadata to objects, client should provide data in format appropriate for each fields' datatype.
ECS supports 4 types of datatype: _String_, _Integer_, _DateTime_ and _Decimal_.

| Datatype | Description |
|:---------|:------------|
| String    | Search comparisons are done over plaintext. |
| Integer   | String should contain only digits, and is converted to integer in search comparisons. |
| Decimal   | Decimal value with "." character treates as decimap point. |
| Datetime  | Expected string format is _yyyy-MM-ddTHH:mm:ssZ_ |

#### Example

Example of `service-settings` block part describing Search Metadata is presented in the listing:

```yaml
...
search-metadata:
- type: System
  name: Expiration
  datatype: DateTime
- type: System
  name: Size
  datatype: Integer
- type: User
  name: x-amz-meta-my-meta
  datatype: Decimal
...
```

### Bucket Tags

When specified in service definition or in request parameters, tags are defined with the following format: `{tags:[{"key": <str>,"value": <str>}, ...]}`

Tag value could contain a special placeholder, which will be replaced by request context value.
Supported placeholder values are listed below:
  
| Placeholder       | Platform                  | Property          | Comment |
|:------------------|---------------------------|:------------------|---------|
| $CF_ORG_GUID      | Cloud Foundry             | organization_guid | The GUID of the organization that a Service Instance is associated with        |
| $CF_ORG_NAME      | Cloud Foundry             | organization_name | The name of the organization that a Service Instance is associated with.         |
| $CF_SPACE_GUID    | Cloud Foundry             | space_guid        | The GUID of the space that a Service Instance is associated with.        |
| $CF_SPACE_NAME    | Cloud Foundry             | space_name        | The name of the space that a Service Instance is associated with.        |
| $CF_INSTANCE_NAME | Cloud Foundry, Kubernetes | instance_name     | The name of the Service Instance.        |
| $CTX_NAMESPACE    | Kubernetes                | namespace         | The name of the Kubernetes namespace in which the Service Instance will be visible.        |
| $CTX_CLUSTER_ID   | Kubernetes                | clusterid         | The unique identifier for the Kubernetes cluster from which the request was sent.        | 

For additional details, see Open Service Broker API documentation for request context object: 
https://github.com/openservicebrokerapi/servicebroker/blob/master/profile.md#context-object

####  Example
Tag Placeholders usage example:

```json
{ "tags":
    [
        {"key":"org_guid","value":"$CF_ORG_GUID"},
        {"key":"org_name","value":"$CF_ORG_NAME"},
        {"key":"org_space","value":"$CF_SPACE_GUID"},
        {"key":"space_name","value":"$CF_SPACE_NAME"},
        {"key":"instance_name","value":"$CF_INSTANCE_NAME"}
    ]
}
```
## Testing

Local test suite can be run with either a live ECS platform, or using the included simulator.  Configuration variables
can be found and/or changed via the
[EcsActionTest class](https://github.com/thecodeteam/ecs-cf-service-broker/blob/master/src/test/java/com/emc/ecs/common/EcsActionTest.java).

You can then run the test-suite with gradle:

```
./gradlew test
```

## TODOs

Up to date tasks are on our [Github issues](https://github.com/thecodeteam/ecs-cf-service-broker/issues) page.
