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

  $workflow = <<-SCRIPT
  git clone https://github.com/folio-org/mod-workflow.git
  cd mod-workflow
  mvn clean package
  nohup java -jar target/mod-workflow-1.0.0-SNAPSHOT.jar &
  sleep 15
  curl -H "Content-Type: application/json" -d "@target/descriptors/ModuleDescriptor.json" http://localhost:9130/_/proxy/modules
  curl -H "Content-Type: application/json" -d '{"srvcId": "mod-workflow-1.0.0-SNAPSHOT", "instId": "mod-workflow-1.0.0-SNAPSHOT", "url": "http://localhost:9001"}' http://localhost:9130/_/discovery/modules
  curl -H "Content-Type: application/json" -d '{"id": "mod-workflow-1.0.0-SNAPSHOT"}' http://localhost:9130/_/proxy/tenants/diku/modules
  SCRIPT

  $camunda = <<-SCRIPT
  git clone https://github.com/folio-org/mod-camunda.git
  cd mod-camunda
  mvn clean package
  nohup java -jar target/mod-camunda-1.0.0-SNAPSHOT.jar &
  sleep 15
  curl -H "Content-Type: application/json" -d "@target/descriptors/ModuleDescriptor.json" http://localhost:9130/_/proxy/modules
  curl -H "Content-Type: application/json" -d '{"srvcId": "mod-camunda-1.0.0-SNAPSHOT", "instId": "mod-camunda-1.0.0-SNAPSHOT", "url": "http://localhost:9000"}' http://localhost:9130/_/discovery/modules
  curl -H "Content-Type: application/json" -d '{"id": "mod-camunda-1.0.0-SNAPSHOT"}' http://localhost:9130/_/proxy/tenants/diku/modules
  SCRIPT

  config.vm.provision "shell", inline: $workflow
  config.vm.provision "shell", inline: $camunda

end

