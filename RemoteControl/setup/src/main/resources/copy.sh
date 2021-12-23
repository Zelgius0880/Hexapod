#!/bin/bash
cd "$(dirname "$0")" || exit
chmod +x ./connectRemotes.sh
chmod +x ./disconnectRemotes.sh

cp ./connectRemotes.sh /root/connectRemotes.sh
cp ./devices /root/devices
cp ./connectRemotes.service /etc/systemd/system/connectRemotes.service

cp ./remote.py /root/remote.py
cp ./inputReceiver.service /etc/systemd/system/inputReceiver.service

cp ./controller-1.0-SNAPSHOT-all.jar /root/controller-1.0-SNAPSHOT-all.jar
cp ./controller.service /etc/systemd/system/controller.service

cp ./camera.service /etc/systemd/system/camera.service
# Fixing a scp bug: EOF seems to not be passed with the file
echo "" >> /root/devices