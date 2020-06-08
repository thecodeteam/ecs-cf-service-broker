#!/bin/bash

export KUBECONFIG=kubeconfig/config

if [[ $DEBUG == true ]]; then
  set -ex
else
  set -e
fi

FLAGS=()

if [[ "${NAMESPACE}" != "" ]]; then
  FLAGS=("-n" "$NAMESPACE")
fi

for i in $(seq 1 $TIMEOUT); do
  echo "$i of $TIMEOUT"

  ACTUAL_STATUS=$(kubectl get "${KIND}" "${NAME}" "${FLAGS[@]}" -o jsonpath=${CHECK_FIELD})

  echo $ACTUAL_STATUS

  if [[ $ACTUAL_STATUS == "$DESIRED_STATUS" ]]; then
    echo "READY"
    exit 0
  fi

  sleep 1
done

echo "Timed out waitng for $KIND $NAME resources to become ready"
exit 1