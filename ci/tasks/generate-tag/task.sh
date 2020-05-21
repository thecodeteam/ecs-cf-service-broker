#!/bin/sh

if [ "$DEBUG" = true ]; then
  set -ex
else
  set -e
fi

REV=$(cat project-repo/.git/refs/heads/"$BRANCH" | cut -c1-7)
VERSION="$VERSION-$REV"
echo "$VERSION" > genetated_tag/image_tag
cat genetated_tag/image_tag
ls -la ./*