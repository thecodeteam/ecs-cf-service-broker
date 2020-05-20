#!/bin/sh

if [ "$DEBUG" = true ]; then
  set -ex
else
  set -e
fi

REV=$(cat git_rev_dir/"$BRANCH")
VERSION="$VERSION-$REV"
echo "$VERSION" > image_dir/image_tag
