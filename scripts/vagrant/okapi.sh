#!/bin/bash

cd /sync

git clone https://github.com/folio-org/okapi.git

cd okapi

git checkout headers-hack
git pull

mvn clean install -DskipTests

cp /usr/share/folio/okapi/lib/okapi-core-fat.jar /usr/share/folio/okapi/lib/okapi-core-fat.bckup
cp okapi-core/target/okapi-core-fat.jar /usr/share/folio/okapi/lib/okapi-core-fat.jar

systemctl restart okapi

# wait for Okapi to start
sleep 120
