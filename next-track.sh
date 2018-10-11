#!/bin/sh

# Use this script to jump to the track on a lesson, e.g.
# ./next-track.sh sql-injection

git checkout `git rev-list --reverse HEAD^..$1 | head -2 | tail -1`
git log -n1
