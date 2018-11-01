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

  config.vm.synced_folder ".vagrant/sync", "/sync", id: 'folio', create: true, mount_options: [ "dmode=777", "fmode=777" ]

  config.vm.provision "file", source: "scripts/vagrant/okapi.sh", destination: "/sync/okapi.sh"
  config.vm.provision "file", source: "scripts/vagrant/mod-workflow.sh", destination: "/sync/mod-workflow.sh"
  config.vm.provision "file", source: "scripts/vagrant/mod-camunda.sh", destination: "/sync/mod-camunda.sh"
  config.vm.provision "file", source: "scripts/vagrant/permissions.sh", destination: "/sync/permissions.sh"
  config.vm.provision "file", source: "scripts/vagrant/triggers.sh", destination: "/sync/triggers.sh"

  $init = <<-SCRIPT
  cd /sync 

  apt-get install -y dos2unix

  dos2unix okapi.sh
  ./okapi.sh

  dos2unix mod-workflow.sh
  ./mod-workflow.sh

  dos2unix mod-camunda.sh
  ./mod-camunda.sh

  dos2unix permissions.sh
  ./permissions.sh

  dos2unix triggers.sh
  ./triggers.sh
  SCRIPT

  config.vm.provision "shell", inline: $init

end
