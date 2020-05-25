# ECS Open Service Broker Pipelines

The service broker continuous integration is executed within the Dell EMC network on private Concourse systems.

## Setup

`params.yml` contains some of parameters used by these pipelines. However, there are some sensitive parameters such an
URLs and credentials which are not included in this file for security reasons. Parameters in `params.yml` may be changed
or override in `fly -v` or `-y` command options if needed.


## Available pipelines

| Name                            | Description                                  |
|---------------------------------|----------------------------------------------|
| `broker-k8s-image-ci`           | Builds the broker k8s image on each commit to|
|                                 | to specified branch, tags it with unique tag,|
|                                 | and pushes image to specified registry       |
|---------------------------------|----------------------------------------------|
| `broker-k8s-image-release`      | Pulls the docker image with specified tag,   |
|                                 | tags it with needed release tag and pushes to|
|                                 | specified registry                          |

## Running the pipeline

To run the pipeline, download `fly` utility from the Concourse home page in the
bottom right-hand corner of the page.  Login to the target:

```shell
$ fly --target <target_alias> login -c <concourse_url>
```

Then set pipeline configuration based with your parameters.
`broker-k8s-image-ci` example:

```shell
$ fly -t <target_alias> sp -p broker-k8s-image-ci \
  -c pipeline.yml \
  -l params.yml \
  -v "custom_registry=<docker_ci_repo>" \
  -v broker_branch=master \
  -v git_token=<git_token> \
  -v slack_url=<slack_webhook_url>
```

`broker-k8s-image-release` example:

```shell
$ fly -t <target_alias> sp -p broker-k8s-image-release-<release_image_tag> \
  -c pipeline.yml \
  -l params.yml \
  -v "registry_src=<docker_ci_repo>" \
  -v "registry_dest=s<docker_release_repo>" \
  -v "registry_dest_username=<docker_release_repo_username>" \
  -v "registry_dest_password=<docker_release_repo_password>" \
  -v src_image_tag=<source_image_tag> \
  -v release_tag=<release_image_tag> \
  -v git_token=<git_token> \
  -v slack_url=<slack_webhook_url>
```