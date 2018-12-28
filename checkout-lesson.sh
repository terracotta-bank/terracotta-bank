#!/bin/sh

# Use this to check out the first track in a particular mitigation sequence
# ./checkout-lesson.sh sql-injection

git config branch.$1.description
git checkout `git rev-list master..$1 | tail -1`~1 > /dev/null 2>&1
