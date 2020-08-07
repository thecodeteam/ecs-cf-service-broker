# ECS Open Service Broker 'broker-e2e-SVCAT-testing' pipeline

The service broker continuous integration is executed within the Dell EMC network on private Concourse systems.

## Description

This pipeline is intended for testing ecs service broker image in kubernetes cluster. At the moment tests include
deploying service catalog, deploying ecs service broker, deploying S3 test app, performing put, get, delete operations on ecs bucket with
verification. Pipeline is triggered if digest of 'latest' image tag is changed. Tested image is pushed to destination
registry with the tag 'green'.

## Setup

Prerequisites:
* deployed kubernetes cluster
* deployed ECS instance

Some configuration files are needed for running the pipeline. They are described below. Files' names may be changed if
you don't like these ones.

* `params.yml` contains some of parameters used by this pipeline. However, there are some sensitive parameters such an
URLs and credentials which are not included in this file for security reasons. Parameters in `params.yml` may be changed
or override in `fly -v` or `-y` command options if needed.

* `kubeconfig.yml` contains var `kubeconfig` with configs of k8s cluster where ecs service broker will be deployed.
This may be copied from `.kube/config` file on your k8s cluster. Example for such file with Kind (k8s in docker) cluster
config look here: `ci/resources/templates/kubeconfig.yml`

* `broker_values.yaml` is a file which contains configs for ecs service broker itself. As a template for this file you
may use file `charts/values.yaml`. There you need to set parameters of ECS instance which the broker will connect
to - replication group, namespace, endpoints etc.

NOTE: dont forget to set `serviceCatalog: true`.
NOTE: dont forget to set in `image` settings `repository` and `tag` in accordance with values of variables
`((registry_inter))` and `((intermediate_image_tag))`. Intermediate repo is used for tracking tested image during the
pipeline.
You may use the same repository for source, intermediate and destination - it's up to you.

## Running the pipeline

To run the pipeline, download `fly` utility from the Concourse home page in the
bottom right-hand corner of the page.  Login to the target:

```shell
$ fly --target <target_alias> login -c <concourse_url>
```

Then set pipeline configuration based with your parameters.

`broker-e2e-SVCAT-testing` example:

```shell
$ fly -t <target_alias> sp -p broker-e2e-ci-svcat \
  -c pipeline.yml \
  -l params.yml \
  -l kubeconfig.yml \
  -l broker_values.yaml \
  -v "registry_src=<docker_source_repo>" \
  -v "registry_inter=<docker_intermediate_repo>" \
  -v "registry_dest=<docker_destination_repo>" \
  -v "registry_inter_username=<docker_intermediate_repo_username>" \
  -v "registry_inter_password=<docker_intermediate_repo_password>" \
  -v git_token=<git_token> \
  -v slack_url=<slack_webhook_url>
```