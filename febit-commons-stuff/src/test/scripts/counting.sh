#!/usr/bin/env sh

count=$1

if [ -z "$count" ]; then
  echo "Usage: $0 <count>"
  exit 1
fi

i=0
while [ "$i" -lt "$count" ]; do
  echo "$i"
  i=$((i + 1))
  sleep 1
done
