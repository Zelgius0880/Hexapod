#!/bin/bash

chmod +x ./connectRemotes.sh
chmod +x ./disconnectRemotes.sh

cp ./connectRemotes.sh /root/connectRemotes.sh
cp ./devices /root/devices
cp ./connectRemotes.service /etc/systemd/system/connectRemotes.service

cp ./remote.py /root/remote.py
cp ./inputReceiver.service /etc/systemd/system/inputReceiver.service

systemctl enable connectRemotes.service
systemctl start connectRemotes.service

systemctl enable inputReceiver.service
systemctl start inputReceiver.service
