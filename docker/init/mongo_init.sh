#!/bin/bash

MONGODB=mongo

echo "**********************************************" ${MONGODB}
echo "Waiting for startup.."

until curl http://${MONGODB}:27017/serverStatus\?text\=1 2>&1 | grep uptime | head -1; do
  printf '.'
  sleep 1
done

echo "Started.."

mongosh --host ${MONGODB}:27017 --eval "rs.initiate();"