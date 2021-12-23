#!/bin/bash
cd "$(dirname "$0")" || exit
systemctl enable connectRemotes.service
systemctl start connectRemotes.service

systemctl enable inputReceiver.service
systemctl start inputReceiver.service

systemctl enable camera.service
systemctl start camera.service

systemctl enable controller.service
systemctl start controller.service
