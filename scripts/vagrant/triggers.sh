#!/bin/bash

cd /sync

curl -X POST -H "X-Okapi-Tenant: diku" -H "Content-Type: application/json" http://localhost:9130/authn/login -d '{"username": "diku_admin", "password": "admin"}' -D login-headers.tmp
token_header=$(cat login-headers.tmp | grep x-okapi-token)

# create example trigger, on create user
echo '{
  "name": "User Create",
  "description": "Trigger for when a user is created",
  "type": "PROCESS_START",
  "method": "POST",
  "pathPattern": "/users"
}' > user_create_trigger.json

curl -v -X POST -H "X-Okapi-Tenant: diku" -H "$token_header" -H "Content-Type: application/json" http://localhost:9130/triggers -d "@user_create_trigger.json"

# create trigger on check out
echo '{
  "name": "Check Out",
  "description": "Trigger for book check out",
  "type": "PROCESS_START",
  "method": "POST",
  "pathPattern": "/circulation/check-out-by-barcode"
}' > check_out_trigger.json

curl -v -X POST -H "X-Okapi-Tenant: diku" -H "$token_header" -H "Content-Type: application/json" http://localhost:9130/triggers -d "@check_out_trigger.json"

# create trigger on check in
echo '{
  "name": "Check In",
  "description": "Trigger for book check in",
  "type": "MESSAGE_CORRELATE",
  "method": "PUT",
  "pathPattern": "/circulation/loans"
}' > check_in_trigger.json

curl -v -X POST -H "X-Okapi-Tenant: diku" -H "$token_header" -H "Content-Type: application/json" http://localhost:9130/triggers -d "@check_in_trigger.json"

# cleanup
rm -rf login-headers.tmp
