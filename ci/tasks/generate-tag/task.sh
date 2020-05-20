#!/bin/sh

if [ "$DEBUG" = true ]; then
  set -ex
else
  set -e
fi

ls -la ./project-repo
ls -la ./project-repo/.git/refs/heads/
REV=$(cat project-repo/.git/refs/heads/"$BRANCH")
VERSION="$VERSION-$REV"
echo "$VERSION" > image_dir/image_tag
