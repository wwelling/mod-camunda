#!/bin/bash

cd /sync

curl -X POST -H "X-Okapi-Tenant: diku" -H "Content-Type: application/json" http://localhost:9130/authn/login -d '{"username": "diku_admin", "password": "admin"}' -D login-headers.tmp
token_header=$(cat login-headers.tmp | grep x-okapi-token)

# create example trigger, on create user
echo '{
  "name": "User Create",
  "description": "Trigger for when a user is created",
  "method": "POST",
  "pathPattern": "/users"
}' > user_create_trigger.json

curl -v -X POST -H "X-Okapi-Tenant: diku" -H "$token_header" -H "Content-Type: application/json" http://localhost:9130/triggers -d '@user_create_trigger.json'

sleep 5

# cleanup
rm -rf login-headers.tmp