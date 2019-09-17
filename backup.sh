#!/bin/bash
# Backup Script for ODS DB
# sed -i -e 's/\r$//' backup.sh - Execute this command if '/bin/bash^M: bad interpreter: No such file or directory' error occurs
# This occurs if the script is edited in Windows text editor
# Requires:
# install aws cli
# set the aws key and secret in environment varialbe - The cli will read it from there to connect to s3 bucket
# Create a bucket in s3 with name 'ods-db-backups'
# to add cronjob follow the bleow command
# To add new job use  crontab -e and choose option 1
# it opens cronjob file where add a new job by following the below format
# m h dom mon dow /home/backup.sh
# verify it by crobtab -l
no_of_backups=5 # Number of backups to maintain in s3 bucket. Backups older than 'no_of_backups' will be removed on every job execution
s3_bucket_name='ods-db-backups'
backup_date=`date +%d-%m-%y`
backup_folder="/home/backup/"
backup_path="$backup_folder$backup_date"
sudo mongodump --out "$backup_path" --db onedatashare
# cd to backup folder to create tar
cd "$backup_folder"
sudo tar -zcvf "$backup_date.tar.gz" $backup_date
# Upload to S3
sudo aws s3 cp "$backup_date.tar.gz" "s3://ods-db-backups"
# Remove the backup file created 5 days ago
older_date=`date -d "-$no_of_backups days" +%d-%m-%y`
file_to_delete="$older_date.tar.gz"
sudo aws s3 rm "s3://$s3_bucket_name/$file_to_delete"
# remove the local backup folder
cd "/home"
sudo rm -r backup