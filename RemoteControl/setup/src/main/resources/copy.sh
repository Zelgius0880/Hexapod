#!/bin/bash
cd "$(dirname "$0")" || exit
chmod +x ./connectRemotes.sh
chmod +x ./disconnectRemotes.sh

cp ./connectRemotes.sh /root/connectRemotes.sh
cp ./devices /root/devices
cp ./connectRemotes.service /etc/systemd/system/connectRemotes.service

cp ./remote.py /root/remote.py
cp ./inputReceiver.service /etc/systemd/system/inputReceiver.service
# Fixing a scp bug: EOF seems to not be passed with the file
 echo "" >> /root/devices