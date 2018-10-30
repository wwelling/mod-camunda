# -*- mode: ruby -*-
# vi: set ft=ruby :
# Build a VM to serve as an Okapi/Docker server
# Deploy development environment

Vagrant.configure(2) do |config|

  # Note that provisioning a Stripes webpack requires more RAM
  config.vm.provider "virtualbox" do |vb|
    vb.memory = 16384
    vb.cpus = 4
  end

  config.vm.define "testing", autostart: true do |testing|
    testing.vm.box = "folio/testing"
    testing.vm.network "forwarded_port", guest: 9130, host: 9130
    testing.vm.network "forwarded_port", guest: 3000, host: 3000
    testing.vm.network "forwarded_port", guest: 9000, host: 9000
    testing.vm.network "forwarded_port", guest: 61616, host: 61616
  end

  config.vm.synced_folder ".vagrant/sync", "/sync", id: 'folio', create: true, mount_options: [ "dmode=777", "fmode=777", "uid=312", "gid=312" ]

  $okapi = <<-SCRIPT
  git clone https://github.com/folio-org/okapi.git
  cd okapi
  git checkout ea7fe3dd8f7563a58902352d7d37602caaf3dafc
  mvn clean install -DskipTests
  cp /usr/share/folio/okapi/lib/okapi-core-fat.jar /usr/share/folio/okapi/lib/okapi-core-fat.bckup
  cp okapi-core/target/okapi-core-fat.jar /usr/share/folio/okapi/lib/okapi-core-fat.jar
  systemctl restart okapi
  sleep 30
  SCRIPT

  $workflow = <<-SCRIPT
  git clone https://github.com/folio-org/mod-workflow.git
  cd mod-workflow
  git checkout master
  mvn clean install -DskipTests
  nohup java -jar target/mod-workflow-1.0.0-SNAPSHOT.jar &
  sleep 15
  curl -X POST -H "Content-Type: application/json" -d "@target/descriptors/ModuleDescriptor.json" http://localhost:9130/_/proxy/modules
  curl -X POST -H "Content-Type: application/json" -d '{"srvcId": "mod-workflow-1.0.0-SNAPSHOT", "instId": "mod-workflow-1.0.0-SNAPSHOT", "url": "http://localhost:9001"}' http://localhost:9130/_/discovery/modules
  curl -X POST -H "Content-Type: application/json" -d '{"id": "mod-workflow-1.0.0-SNAPSHOT"}' http://localhost:9130/_/proxy/tenants/diku/modules
  SCRIPT

  $camunda = <<-SCRIPT
  git clone https://github.com/folio-org/mod-camunda.git
  cd mod-camunda
  git checkout master
  mvn clean install -DskipTests
  nohup java -jar target/mod-camunda-1.0.0-SNAPSHOT.jar &
  sleep 15
  curl -X POST -H "Content-Type: application/json" -d "@target/descriptors/ModuleDescriptor.json" http://localhost:9130/_/proxy/modules
  curl -X POST -H "Content-Type: application/json" -d '{"srvcId": "mod-camunda-1.0.0-SNAPSHOT", "instId": "mod-camunda-1.0.0-SNAPSHOT", "url": "http://localhost:9000"}' http://localhost:9130/_/discovery/modules
  curl -X POST -H "Content-Type: application/json" -d '{"id": "mod-camunda-1.0.0-SNAPSHOT"}' http://localhost:9130/_/proxy/tenants/diku/modules
  SCRIPT

  $permissions = <<-SCRIPT
  curl -X POST -H "X-Okapi-Tenant: diku" -H "Content-Type: application/json" http://localhost:9130/authn/login -d '{"username": "diku_admin", "password": "admin"}' -D login-headers.tmp
  token_header=$(cat login-headers.tmp | grep x-okapi-token)
  # update diku_admin permissions, add all permissions for mod-workflow and mod-camunda
  echo '{
    "id": "2cdefed8-300a-47c3-9d70-00536c487e0c",
    "userId": "deb4e698-acf3-5093-ab63-962b40d748a0",
    "permissions": [
      "process.all",
      "process-definition.all",
      "decision-definition.all",
      "task.all",
      "message.all",
      "action.all",
      "trigger.all",
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
  sleep 5
  curl -X PUT -H "X-Okapi-Tenant: diku" -H "$token_header" -H "Content-Type: application/json" http://localhost:9130/perms/users/2cdefed8-300a-47c3-9d70-00536c487e0c -d '@diku_admin_perms.json'
  SCRIPT

  $triggers = <<-SCRIPT
  curl -X POST -H "X-Okapi-Tenant: diku" -H "Content-Type: application/json" http://localhost:9130/authn/login -d '{"username": "diku_admin", "password": "admin"}' -D login-headers.tmp
  token_header=$(cat login-headers.tmp | grep x-okapi-token)
  # create example trigger, on create user
  echo '{
    "name": "User Create",
    "description": "Trigger for when a user is created",
    "method": "POST",
    "pathPattern": "/users"
  }' > user_create_trigger.json
  sleep 5
  curl -X POST -H "X-Okapi-Tenant: diku" -H "$token_header" -H "Content-Type: application/json" http://localhost:9130/triggers -d '@user_create_trigger.json'
  SCRIPT

  config.vm.provision "shell", inline: $okapi
  config.vm.provision "shell", inline: $workflow
  config.vm.provision "shell", inline: $camunda
  config.vm.provision "shell", inline: $permissions
  config.vm.provision "shell", inline: $triggers

end

