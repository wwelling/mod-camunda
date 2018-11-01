#!/bin/bash

cd /sync

git clone https://github.com/folio-org/okapi.git
cd okapi
git checkout ea7fe3dd8f7563a58902352d7d37602caaf3dafc
git pull
mvn clean install -DskipTests
cp /usr/share/folio/okapi/lib/okapi-core-fat.jar /usr/share/folio/okapi/lib/okapi-core-fat.bckup
cp okapi-core/target/okapi-core-fat.jar /usr/share/folio/okapi/lib/okapi-core-fat.jar
systemctl restart okapi
