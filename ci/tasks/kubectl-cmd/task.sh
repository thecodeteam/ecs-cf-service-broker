#!/bin/bash

if [[ $DEBUG == true ]]; then
  set -x
fi

if [[ $IGNORE_ERROR == true ]]; then
  set +e
else
  set -e
fi

ARG_ARRAY=("${ARGS}")

echo "${ARG_ARRAY[@]}"

if [[ $OUTPUT_FILE_NAME != "" ]]; then
  ARG_ARRAY+=(">" "output/$OUTPUT_FILE_NAME")
fi

kubectl --kubeconfig config/${K8S_CONFIG_FILE_NAME} ${COMMAND} "${ARG_ARRAY[@]}"

exit 0
