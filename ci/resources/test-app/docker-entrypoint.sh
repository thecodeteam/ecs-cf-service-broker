#!/bin/sh
set -e

export VCAP_SERVICES="
{
  \"ecs-bucket\": [
    {
      \"credentials\": {
        \"endpoint\": \"${ENDPOINT}\",
        \"accessKey\": \"${ACCESS_KEY}\",
        \"secretKey\": \"${SECRET_KEY}\",
        \"bucket\": \"${BUCKET}\"
      }
    }
  ]
}"

/app/ecs-broker-test-app