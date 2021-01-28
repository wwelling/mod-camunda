#!/bin/bash

cd /sync

curl -X POST -H "X-Okapi-Tenant: diku" -H "Content-Type: application/json" http://localhost:9130/authn/login -d '{"username": "diku_admin", "password": "admin"}' -D login-headers.tmp
token_header=$(cat login-headers.tmp | grep x-okapi-token)

curl -v -H "X-Okapi-Tenant: diku" -H "$token_header" -H "Content-Type: application/json" "http://localhost:9130/extractors/ed75fb11-abb2-41d9-98f7-aeb79d7700f4/run"

# cleanup
rm -rf login-headers.tmp
