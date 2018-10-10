git checkout `git rev-list master..$1 | tail -1`
git log -n1
