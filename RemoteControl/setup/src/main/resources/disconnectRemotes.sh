#!/bin/bash
input="/root/devices"
status=1
while IFS= read -r line
do
    bluetoothctl disconnect $line &
    wait $!
done < "$input"
exit 0