# OneDataShare Core

## Technology Stack

OneDataShare application is built using following technologies/frameworks:
1. Node.js
2. Apache Maven 
3. MongoDB
4. Java 8
5. ReactJS
6. Spring Reactor

## What is this repository?

The ODS Core is the service that faces users and thus is publicly reachable. It provides the core api that the ODS Cli implements, the frontend is also bundled in this repo as the ODS Core hosts the frontend. We do not use Node.js to run the frontend, spring handles this part.

## Local Installation Instructions
1. Clone this repository
2. Have a local MongoDB running in any manner you would like just ensure you have the correct port running.
3. The env variables required to run the jar can be found in the ODS s3 bucket. Please contact the team if you would like a set to try for local development. We cannot expose development environment variables as they specific to the ODS user exclusively.
4. cd src/main/react-front-end
5. npm install
6. npm run build
7. To compile ``mvn clean package -DskipTests``
8. To run ``java -jar target/onedatashare-1.0-SNAPSHOT.jar``

In the case of a frontend developer what you may do is use npm to run the frontend while the backend server is running. This will allow you to have the re-fresh of the front through ``npm start``.


