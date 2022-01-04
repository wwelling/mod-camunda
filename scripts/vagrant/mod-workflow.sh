#!/bin/bash

cd /sync

git clone https://github.com/folio-org/mod-workflow.git

cd mod-workflow

git checkout master
git pull

mvn clean install -DskipTests
nohup java -jar target/mod-workflow-1.1.0.jar &

# wait for mod-workflow to start
sleep 45

curl -X POST -H "Content-Type: application/json" -d "@target/descriptors/ModuleDescriptor.json" http://localhost:9130/_/proxy/modules
sleep 5

curl -X POST -H "Content-Type: application/json" -d '{"srvcId": "mod-workflow-1.1.0", "instId": "mod-workflow-1.1.0", "url": "http://localhost:9001"}' http://localhost:9130/_/discovery/modules
sleep 5

curl -X POST -H "Content-Type: application/json" -d '{"id": "mod-workflow-1.1.0"}' http://localhost:9130/_/proxy/tenants/diku/modules
