# OneDataShare

OneDataShare provides data scheduling and optimization as a cloud-hosted service.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites

Below are the following technologies and versions that we currently use to get OneDataShare up and running.

1.	[NodeJS](https://nodejs.org/en/download/) (v8.9.4) – needed for node package manager (NPM)
2.	[Apache Maven](https://maven.apache.org/download.cgi)
3.	[MongoDB](https://www.mongodb.com/) (version 4.0.4)
4.	[Java](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) (JDK 1.8)
5.	[Git](https://git-scm.com/downloads) (version 2.16 or above)

_Since we use the Lombok plugin for IntelliJ to generate some of our code, this is the recommended IDE for using OneDataShare._

### Installing

The following steps are the same no matter what operating system you use. Although a Unix based operating system is recommended, we have included steps for Windows, Linux, and macOS.

1. Open your terminal and clone the GitHub repository.

    ```
    git clone https://github.com/didclab/onedatashare-spring.git
    ```

2. Once you have the repository cloned to your computer, open it in IntelliJ and then import it as a Maven project.

3. In IntelliJ, go to `File > Settings > Plugins` and then, in the search box, type `Lombok` and click on `search in repositories`, and then Install the Lombok plugin.
 
_Note – The Lombok plugin enables us to avoid writing boilerplate code such as getters, setters and parameterized constructors._




### Steps for Linux/OSX
1. Navigate to the front-end codebase 
    ```
    cd <cloned_directory>/src/main/react-front-end/
    ```

2. Run 
    ```
    npm install
    ```
    then
    ```
    npm run build
    ```
    
3. Setting Necessary environment variables. 

    Open the Linux terminal and run the command 
    ```
    gedit ~/.profile
    ```

    If you're on Mac you can run 
    ```
    vim ~/.profile
    ``` 
    if you're comfortable with vim, or 
    ```
    nano ~/.profile
    ```
    Then paste the following text and save the file
    ```
    export ODS_EMAIL_ADDRESS="ods.notification.do.not.reply@gmail.com" export ODS_EMAIL_PWD="OneDataShareMailer@DIDCLab" 
    export ods_drive_client_id="1093251746493-hga9ltfasf35q9daqrf00rgcu1ocj3os.apps.googleusercontent.com" 
    export ods_drive_client_secret="8Zsk-F6iP3jyIDVvHV33CkKh" 
    export ods_drive_project_id="onedatashare-1531417250475"
    ```



### Steps for Windows
1. Navigate to the front-end codebase 
    and run 
    ```
    npm install
    ```
    then
    ```
    npm run build-win
    ```

2. Set necessary environment variables

    Open the command prompt and run following commands:
    ```
    setx ODS_EMAIL_ADDRESS "ods.notification.do.not.reply@gmail.com"
    ```
    ```
    setx ODS_EMAIL_PWD "OneDataShareMailer@DIDCLab"
    ```
    ```
    setx ods_drive_client_id "1093251746493-hga9ltfasf35q9daqrf00rgcu1ocj3os.apps.googleusercontent.com"
    ```
    ```
    setx ods_drive_client_secret "8Zsk-F6iP3jyIDVvHV33CkKh"
    ```
    ```
    setx ods_drive_project_id "onedatashare-1531417250475"
    ```

_Note-For environment variables to take effect, please restart your machine and proceed._


After you've successfully installed OneDataShare, start the MongoDB server by running `mongod` executable in the `bin` directory of MongoDB installation, and then run the ODS codebase `Run > Run ‘ServerApplication’`

## Versioning

We use [SemVer](http://semver.org/) for versioning. 