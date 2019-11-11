#!/bin/bash
# This script will be called from .travis.yml file at the back-end deploy stage
# Note: For this script to work, the $servers environment varialbe should be set in the repository settings of onedatashare at https://travis-ci.com/  


########## Script ########

cd target
echo "The servers are $servers"
IFS=', ' read -r -a array <<< "$servers"
for server in "${array[@]}"
do
	sudo scp -i ../ods-prod.pem -o StrictHostKeyChecking=no -r *.jar $server:/home/Travis-ODS
	ssh -n -f -o StrictHostKeyChecking=no $server 'cd /home/Travis-ODS && ./runProdServer.sh'
    echo "Successfully deployed on $server"
done
echo "Deployed on all servers!"
