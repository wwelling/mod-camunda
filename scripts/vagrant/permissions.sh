#!/bin/bash

cd /sync

curl -X POST -H "X-Okapi-Tenant: diku" -H "Content-Type: application/json" http://localhost:9130/authn/login -d '{"username": "diku_admin", "password": "admin"}' -D login-headers.tmp
token_header=$(cat login-headers.tmp | grep x-okapi-token)

curl -v -H "X-Okapi-Tenant: diku" -H "$token_header" -H "Content-Type: application/json" "http://localhost:9130/users?query=username=diku_admin"

user_json=$(curl -H "X-Okapi-Tenant: diku" -H "$token_header" -H "Content-Type: application/json" "http://localhost:9130/users?query=username=diku_admin")

id_regex='"id":"([^"]+)'

if [[ $user_json =~ $id_regex ]]
then
  user_id="${BASH_REMATCH[1]}"
else
  echo "Could not get diku_admin id!"
fi

curl -v -H "X-Okapi-Tenant: diku" -H "$token_header" -H "Content-Type: application/json" "http://localhost:9130/perms/users?query=userId=$user_id"

user_perms_json=$(curl -H "X-Okapi-Tenant: diku" -H "$token_header" -H "Content-Type: application/json" "http://localhost:9130/perms/users?query=userId=$user_id")

if [[ $user_perms_json =~ $id_regex ]]
then
  perm_id="${BASH_REMATCH[1]}"
else
  echo "Could not get diku_admin permission id!"
fi

# update diku_admin permissions, add all permissions for mod-workflow and mod-camunda
echo '{
  "id": "4c9056ae-2b59-45cb-b1cf-9f9e35ba9d89",
  "userId": "c4de12e4-03e1-58b4-b6d3-8206061fd527",
  "permissions": [
    "process.all",
    "process-definition.all",
    "decision-definition.all",
    "task.all",
    "message.all",
    "action.all",
    "trigger.all",
    "workflow.all",
    "perms.all",
    "okapi.proxy.pull.modules.post",
    "login.all",
    "okapi.all",
    "users.all",
    "configuration.all",
    "tags.all",
    "users-bl.all",
    "notify.all",
    "inventory-storage.all",
    "validation.all",
    "inventory.all",
    "login-saml.all",
    "user-import.all",
    "codex.all",
    "circulation-storage.all",
    "vendor.module.all",
    "circulation.all",
    "calendar.collection.all",
    "notes.all",
    "finance.module.all",
    "fund.all",
    "feesfines.all",
    "orders.all",
    "templates.all",
    "rtac.all",
    "orders-storage.module.all",
    "audit.all",
    "module.myprofile.enabled",
    "ui-myprofile.view",
    "settings.tags.enabled",
    "settings.transfers.all",
    "module.orders.enabled",
    "module.organization.enabled",
    "ui-organization.settings.location",
    "settings.data-import.enabled",
    "module.eholdings.enabled",
    "ui-inventory.settings.instance-formats",
    "ui-checkin.all",
    "ui-circulation.settings.fixed-due-date-schedules",
    "ui-inventory.settings.instance-types",
    "ui-checkout.all",
    "ui-circulation.settings.cancellation-reasons",
    "settings.checkout.enabled",
    "settings.loan-rules.all",
    "module.developer.enabled",
    "module.finance.enabled",
    "ui-inventory.all-permissions.TEMPORARY",
    "ui-organization.settings.key-bindings",
    "ui-organization.settings.locale",
    "settings.loan-policies.all",
    "module.data-import.enabled",
    "settings.developer.enabled",
    "settings.orders.enabled",
    "orders.module.all",
    "settings.eholdings.enabled",
    "ui-inventory.settings.materialtypes",
    "ui-organization.settings.plugins",
    "ui-inventory.settings.contributor-types",
    "ui-inventory.settings.loantypes",
    "ui-organization.settings.sso",
    "ui-organization.settings.servicepoints",
    "ui-users.editperms",
    "settings.accounts.all",
    "settings.feefineactions.all",
    "ui-users.loans.renew",
    "module.search.enabled",
    "ui-users.create",
    "ui-users.edituserservicepoints",
    "ui-users.editproxies",
    "module.notes.enabled",
    "module.tags.enabled",
    "settings.refunds.all",
    "settings.waives.all",
    "ui-requests.all",
    "stripes-util-notes.all",
    "stripes-util-notes.edit",
    "stripes-util-notes.create",
    "stripes-util-notes.delete",
    "settings.comments.all",
    "settings.feefines.all",
    "settings.payments.all",
    "settings.transfertypes.all",
    "settings.usergroups.all",
    "module.vendors.enabled",
    "settings.owners.all",
    "ui-users.editpermsets",
    "settings.addresstypes.all"
  ]
}' > diku_admin_perms.json

curl -X PUT -H "X-Okapi-Tenant: diku" -H "$token_header" -H "Content-Type: application/json" "http://localhost:9130/perms/users/4c9056ae-2b59-45cb-b1cf-9f9e35ba9d89" -d "@diku_admin_perms.json"

# cleanup
rm -rf login-headers.tmp

# wait permissions to propegate
sleep 30