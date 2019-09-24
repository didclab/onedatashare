#!/bin/bash
# Backup Script for ODS DB
# sed -i -e 's/\r$//' backup.sh - Execute this command if '/bin/bash^M: bad interpreter: No such file or directory' error occurs
# This occurs if the script is edited in Windows text editor
# Requires:
######### AWS CLI Configuration ########
# install aws cli
# set the aws key and secret in environment varialbe - The cli will read it from there to connect to s3 bucket
# Create a bucket in s3 with name 'ods-db-backups'
######### Cron Job Confuguration ####### 
# To add new job use  crontab -e and choose option 1
# it opens cronjob file where add a new job by following the below format
# m h dom mon dow /home/backup.sh
# verify it by crobtab -l
########## Email Configurations #########
# sudo apt-get install bsd-mailx
# A prompt to configure postfix will appear.
# choose "internet site" and leave rest as default
# To change the configurations later use reconfigure cmd: dpkg-reconfigure postfix
# Finally execute systemctl restart postfix

########## Script ########

# Method parameters
# Sends error email and exits if the previously executed cmd is not successful
# $? gets the status of previous cmd. 0 is success and 1 is failure
#$1 - Email body with failure message
#$2 - Email subject
#$3 - Admin email Id
check_error()
{
	if [ $? -ne 0 ]; then
		# echo "Caught Exception $1"
		echo "$1 Exception occured after the above operation." | mail -s "Error: $2" "$3"
		# clean up
		cd "/home"		
		if [ -d "backup" ]; then
			sudo rm -r "backup"
			# sudo rm "$4"
		fi
		exit
	fi
}

# Configurations
admin_email=$ODS_ADMIN_EMAIL
email_subject="ODS DB Backup Report"
no_of_backups=5 # Number of backups to maintain in s3 bucket. Backups older than 'no_of_backups' will be removed on every job execution
s3_bucket_name='ods-db-backups'
backup_date=`date +%d-%m-%y`
backup_folder="/home/backup/"
database_name="onedatashare"

# Variables
backup_path="$backup_folder$backup_date"
backup_msg=" Started creating backup on `date`."$'\n'
dump_output=`sudo mongodump --out "$backup_path" --db $database_name 2>&1`

# cd to backup folder to create tar
cd "$backup_folder"
check_error "$backup_msg $dump_output Mongo dump operation failed." "$email_subject" "$admin_email"
backup_msg="$backup_msg $dump_output"$'\n'
backup_msg="$backup_msg Mongo dump successful."$'\n'
sudo tar -zcvf "$backup_date.tar.gz" $backup_date
check_error "$backup_msg Failed while creating zip" "$email_subject" "$admin_email"
backup_msg="$backup_msg Created zip file: $backup_date.tar.gz."$'\n'

# Upload to S3
cli_output=`sudo aws s3 cp "$backup_date.tar.gz" "s3://$s3_bucket_name" 2>&1`
check_error "$backup_msg $cli_output Upload to s3 failed" "$email_subject" "$admin_email"
backup_msg="$backup_msg $cli_output."$'\n'
backup_msg="$backup_msg Successfully uploaded the backup to s3 bucket."$'\n'
backup_msg="$backup_msg The backup  url is: s3://$s3_bucket_name/$backup_date.tar.gz."$'\n'

# Remove the backup file created $no_of_days days ago
older_date=`date -d "-$no_of_backups days" +%d-%m-%y`
file_to_delete="$older_date.tar.gz"
cli_output=`sudo aws s3 rm "s3://$s3_bucket_name/$file_to_delete" 2>&1`
check_error "$backup_msg $cli_output" "$email_subject" "$admin_email"
backup_msg="$backup_msg Deleted the backup file $file_to_delete created $no_of_backups days ago."$'\n'

# remove the local backup folder
cd "/home"
sudo rm -r backup
backup_msg="$backup_msg Removed the local backup folder."$'\n'
backup_msg="$backup_msg The backup operation is completed successfully!"$'\n'

# send success email
echo "$backup_msg" | mail -s "Success: $email_subject" "$admin_email"

