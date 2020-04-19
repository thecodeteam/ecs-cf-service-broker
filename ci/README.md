# ECS Open Service Broker Pipelines

The service broker continuous integration and build is executed within the
Dell EMC network on private Concourse systems.  This allows for full testing
of the broker, and a test application with both Kubernetes and Cloud Foundry
systems to ensure compatibility with new releases of Pivotal Cloud Foundry, and
Kubernetes Service Catalog.

## Setup

To manually run a pipeline, first configure the `credentials.yml` file with the
necessary credentials for the Pivotal Operations Manager, Kubernetes cluster,
Artifactory, etc:

```yaml
# The token used to download the repos from GitHub
git_username: <github username>
git_token:    <github personal access token>

# Token used to download the product file from Pivotal Network. Find this
# on your Pivotal Network profile page:
# https://network.pivotal.io/users/dashboard/edit-profile
pivnet_token: <pivotal network token>

artifactory_user:     <artifactory username>
artifactory_password: <artifactory password>

opsman_config: |
  target: https://<operations manager IP>
  skip-ssl-validation: true
  username: <operations manager username>
  password: <operations manager password>

cf_foundation_name: <cloud foundry foundation name>
cf_apps_domain:     <cloud foundry apps domain name>
cf_api:             <cloud foundry api hostname>
cf_user:            <cloud foundry username>
cf_password:        <cloud foundry password>
cf_org:             <cloud foundry organization name>
cf_space:           <cloud foundry space name>
cf_skip_cert_check: true
```

You can then configure the `params.yml` with any parameters specific to this run
of the pipeline including the target branch, and catalog entries.

## Available pipelines

| Name                            | Description                                  |
|---------------------------------|----------------------------------------------|
| `broker-development-pcf`        | Builds the broker as a Java archive, uploads |
|                                 | as a PCF app, registers as a service broker, |
|                                 | and tests provisioned bucket with a test app |
|---------------------------------|----------------------------------------------|
| `broker-development-kubernetes` | Builds the broker Docker container, uploads  |
|                                 | as a K8s deployment, registers with service  |
|                                 | catalog broker, and tests provisioned bucket |
|                                 | with a test app                              |

## Running the pipeline

To run the pipeline, download `fly` utility from the Concourse home page in the
bottom right-hand corner of the page.  Login to the target:

```shell
$ fly --target gotham login --concourse-url http://concourse.gotham.local:8080
```

Then set pipeline configuration based with your parameters:

```shell
$ fly --target gotham set-pipeline -l globals.yml \
    -l credentials.yml \
    -l <pipeline dir>/params.yml \
    -c <pipeline dir>/pipeline.yml \
    -p <pipeline name>
```

For example:

```shell
$ fly --target gotham set-pipeline -l globals.yml \
    -l credentials.yml \
    -l broker-development-pcf/params.yml \
    -c broker-development-pcf/pipeline.yml \
    -p broker-development-pcf
```
