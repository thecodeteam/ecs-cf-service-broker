#!/bin/bash

export TERM=${TERM:-dumb}
cd ecs-cf-service-broker
./gradlew --no-daemon build