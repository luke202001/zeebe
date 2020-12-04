#!/bin/sh -eux

apt-get install --no-install-recommends -qq -y jq

chmod +x clients/go/cmd/zbctl/dist/zbctl

alias zbctl="clients/go/cmd/zbctl/dist/zbctl"

zbctl create instance external-tool-integration --variables "${QA_RUN_VARIABLES}"

businessKey="${BUSINESS_KEY}"

waiting=1

echo "Waiting for result of $businessKey"

while [ $waiting -eq 1 ]; do

    zbctl activate jobs "$businessKey" > activationresponse.txt 2>error.txt

    key=$(jq -r '.key' < activationresponse.txt)

    if [ -z "$key" ]; then
        echo "Still waiting"
        sleep 5m
    else
        echo "QA run completed"
        waiting=0
    fi
done

key=$(jq -r '.key' < activationresponse.txt)

echo "Job key is: $key"

variables=$(jq -r '.variables' < activationresponse.txt)

echo "Job variables are: $variables"

testResult=$(echo "$variables" | jq -r '.aggregatedTestResult')

echo "Test result is: $testResult"

zbctl complete job "$key"

if [ "$testResult" == "FAILED" ]; then
  echo "Test failed"
  exit 1
else
  echo "Test passed or skipped"
  exit 0
fi
