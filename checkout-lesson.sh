#!/bin/sh

# Use this to check out the first track in a particular mitigation sequence
# ./checkout-lesson.sh sql-injection

git checkout `git rev-list master..$1 | tail -1`
git log -n1
