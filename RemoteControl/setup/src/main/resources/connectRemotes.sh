#!/bin/bash
input="devices"
status=1
while IFS= read -r line
do
  while [ $status -ne 0 ]
  do
    sleep 5
    bluetoothctl -- connect $line &
    wait $!
    status=$?
  done
done < "$input"
exit 0