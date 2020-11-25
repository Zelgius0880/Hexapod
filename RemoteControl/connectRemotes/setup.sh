#!/bin/bash

chmod +x ./connectRemotes.sh
chmod +x ./disconnectRemotes.sh

cp ./connectRemotes.sh /root/connectRemotes.sh
cp ./devices /root/devices
cp ./connectRemotes.service /etc/systemd/system/connectRemotes.service

systemctl enable connectRemotes.service
systemctl start connectRemotes.service
