# deploy to all app servers

IFS=', ' read -r -a array <<< "$app_servers"

for server in "${array[@]}"
do
        sudo scp -i ods-prod.pem -o StrictHostKeyChecking=no -r *.jar ubuntu@$server:/home/ODS-BUILD
        ssh -n -f -o StrictHostKeyChecking=no -i ods-prod.pem ubuntu@$server 'cd /home/ODS-BUILD && ./runProdServer.sh'
        sleep 5
        echo "Downloading the log file"
        sudo scp -i ods-prod.pem -o StrictHostKeyChecking=no -r ubuntu@$server:/home/ODS-BUILD/logs/server-log.log $(pwd)
        log=`cat server-log.log | grep "Started HttpServer"`
        echo "Checking for success msg"
        if [ "$log" == "" ]; then
                        echo "Failed to deploy on the server $server" | mail -s "Failed: ODS Deployment Report" "$admin_email"
                        exit
        fi
        sudo rm server-log.log
        echo "Successfully deployed on $server"
done
echo "Successfully deployed on all app servers" | mail -s "Success: ODS Deployment Report" "$admin_email"