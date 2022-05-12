#!/bin/sh
until curl http://connect:28082/connectors 2>&1 | grep "\["; do
    printf '.'
    sleep 1
done

curl http://connect:28082/connectors -H "Content-type:application/json" -X POST -d @mongo_source_connector.txt
curl http://connect:28082/connectors -H "Content-type:application/json" -X POST -d @elasticsearch_sink_connector.txt
