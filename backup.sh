#!/bin/bash
# Backup Script for ODS DB
# Requires:
# install aws cli
# set the aws key and secret in environment varialbe - The cli will read it from there to connect to s3 bucket
# Create a bucket in s3 with name 'ods-db-backups'
# to add cronjob follow the bleow command
# To add new job use  crontab -e and choose option 1
# it opens cronjob file where add a new job by following the below format
# m h dom mon dow /home/backup.sh
# verify it by crobtab -l
backup_date=`date +%d-%m-%y`
backup_folder="/home/backup/"
backup_path="$backup_folder$backup_date"
echo "The backup path is.. $backup_path"
sudo mongodump --out "$backup_path" --db onedatashare
echo "cd to the backup folder to create zip.. "
cd "$backup_folder"
sudo tar -zcvf "$backup_date.tar.gz" $backup_date
echo "zipped successfully"
# Upload to S3
sudo aws s3 cp "$backup_date.tar.gz" "s3://ods-db-backups"
echo "S3 upload complete"
# Remove the backup file created 5 days ago
older_date=`date -d "-5 days" +%d-%m-%y`
file_to_delete="$older_date.tar.gz"
echo "Deleting the older backup..$file_to_delete"
sudo aws s3 rm "s3://ods-db-backups/$file_to_delete"
echo "Deleted the older backup in s3"
# remove the local backup folder
cd "/home"
sudo rm -r backup
echo "removed the backup folder in VM"
