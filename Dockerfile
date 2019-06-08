# Create a container from the latest Maven image (Maven images are coupled with JDK)
FROM maven

#Set the necessary environment variables for the container
ENV ODS_EMAIL_ADDRESS <Set the source email address for all outgoing emails>
ENV ods_drive_client_id <Set Google Drive client ID>
ENV ods_drive_client_secret <Set Google Drive client secret>
ENV ods_drive_project_id <Set Google Drive Project ID>
ENV FRESHDESK_API_KEY <Set Freshdesk API key>
ENV GOOGLE_CAPTCHA_SECRET <Set Google CAPTCHA secret key>
ENV AWS_ACCESS_KEY <Set AWS SES access key>
ENV AWS_SECRET_KEY <Set AWS SES secret key>

# Copy the POM file and download all the dependencies into the container
COPY pom.xml .
COPY ./lib /lib
RUN mvn dependency:go-offline

# Command to run whenever the container is started
ENTRYPOINT ["mvn","spring-boot:run"]