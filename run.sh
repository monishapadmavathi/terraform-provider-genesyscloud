#!/bin/bash

go test ./genesyscloud/... -run TestAccResourceGroupAddresses -v -timeout 80m -cover -coverprofile=coverageAcceptance.out
