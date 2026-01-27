#!/bin/sh
# 等待 MySQL 完全就绪的脚本

set -e

host="127.0.0.1"
port="3307"
shift
cmd="$@"

until nc -z -v -w30 $host $port
do
  echo "等待本地 MySQL (端口 3307) 启动..."
  sleep 1
done

echo "MySQL 已就绪，启动应用..."
exec $cmd
