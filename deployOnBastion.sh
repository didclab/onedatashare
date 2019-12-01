#!/bin/bash
# This script will be called from .travis.yml file at the back-end deploy stage
# Note: For this script to work, the $bastion environment varialbe should be set in the repository settings of onedatashare at https://travis-ci.com/  

########## Script ########

cd target
echo "The bastion server is $bastion"
sudo scp -i ../ods-prod.pem -o StrictHostKeyChecking=no -r *.jar $bastion:/home/Travis-ODS
ssh -n -f -o StrictHostKeyChecking=no $bastion 'cd /home/Travis-ODS && ./deployToAppServers.sh'
