#!/bin/sh
# 等待 MySQL 完全就绪的脚本

set -e

host="$1"
shift
cmd="$@"

until nc -z -v -w30 mysql 3306
do
  echo "等待 MySQL 启动..."
  sleep 1
done

echo "MySQL 已就绪，启动应用..."
exec $cmd
