#!/bin/sh
curl http://connect:28082/connectors -H "Content-type:application/json" -X POST -d @mongo_source_connector.txt
curl http://connect:28082/connectors -H "Content-type:application/json" -X POST -d @elasticsearch_sink_connector.txt