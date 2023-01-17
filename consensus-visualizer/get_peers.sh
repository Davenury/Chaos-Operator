#!/bin/bash

echo "REACT_APP_PEERS="$(kubectl get svc --sort-by=.metadata.name | grep "peer" | awk '{ print $5 }' | awk -F"," '{ print $1 }' | awk -F"[:/]" '{ print $2 }' | paste -s -d, -)
