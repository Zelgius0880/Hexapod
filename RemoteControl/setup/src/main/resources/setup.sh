#!/bin/bash
cd "$(dirname "$0")" || exit
systemctl enable connectRemotes.service
systemctl start connectRemotes.service

systemctl enable inputReceiver.service
systemctl start inputReceiver.service
