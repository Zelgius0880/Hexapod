#!/bin/bash
input="devices"
status=1
while IFS= read -r line
do
  while [ $status -ne 0 ]
  do
    bluetoothctl connect $line &
    wait $!
    status=$?
  done
done < "$input"
exit 0