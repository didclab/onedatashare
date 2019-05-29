# OneDataShare #

OneDataShare (abbreviated as ODS) is a cloud-based managed file transfer system that performs inter-protocol data transfers in a reliable and efficient manner.

ODS provides the end-user with an interface which can be used to intitate file transfers and monitor them simultaenously. ODS guarantees efficiency in data transfer by performing software level optimizations (for example - chunking of data into a byte stream of fixed size).

OneDataShare beta version is a publicly available for use and can be accessed using https://www.onedatashare.org/.

ODS currently supports transfers between 7 endpoint protocol types viz.
1. Dropbox
2. Google Drive
3. Grid FTP (Globus)
4. FTP
5. SFTP
6. SSH
7. HTTP

_Note: Box and Amazon S3 endpoints support will be provided in future releases_

### Technology Stack ###

OneDataShare application is built using following technologies/frameworks:
1. Node.js (v8.9.4) – needed for node package manager (NPM)
2. Apache Maven (version 3.5.2)
3. MongoDB (version 4.0.4)
4. Java (JDK 1.8)
5. Git (version 2.16 or above)
6. ReactJS
7. Spring Boot coupled with Reactive Web (commonly known as WebFlux) dependency.

Recommended IDEs (used by ODS team for development):
1. IntelliJ Idea Community Edition (for backend code)
2. Visual Studio Code (for front-end code)

### Installation Procedure ###

1. Clone OneDataShare GitHub repository.
	Open command prompt (Windows OS) or terminal (Unix-based OS)
	Run following command –
	git clone https://github.com/didclab/onedatashare-spring.git

2. Open the cloned repository in IntelliJ and import it as a Maven project.

3. Install Lombok Plugin in IntelliJ.
	In IntelliJ, goto File &gt; Settings &gt; Plugins.
	In the search box, type ‘Lombok’ and click on ‘search in repositories’.
	Install the Lombok plugin.

	_Note – Lombok plugin enables us to avoid writing boilerplate code such as getters, setters and parameterized constructors._

4. Navigate to the front-end codebase (&lt;cloned_directory&gt;/src/main/react-front-end/) and build.
	For Windows OS, run following commands:
		npm install
		npm run build-win
	For Unix-based OS, run following commands:
		npm install
		npm run build

5. Configure necessary environment variables
	Note-
	1. Contact the ODS development team for environment variables setup information using the contact details mentioned below. 
	2. For environment variables to take effect, please restart your machine and proceed.

6. Start MongoDB server by running ‘mongod’ executable in the ‘bin’ directory of MongoDB installation.

7. Run the ODS codebase (Run &gt; Run 'Server Application')

### Contact Team ###

Please direct all issues/concerns/queries with respect to OneDataShare using the support page (https://www.onedatashare.org/support) or by sending us an email at admin@onedatashare.org.

### OneDataShare Team ###

Dr. Tevfik Kosar (tkosar@buffalo.edu)  
Product Owner  
https://cse.buffalo.edu/faculty/tkosar/  
Associate Professor, University at Buffalo   

Asif Imran  
Stakeholder/Researcher  
Ph.D. Student, University at Buffalo   

Yifu Yin  
Application Developer  
Graduate Student, University at Buffalo  

Linus Castelino  
Application Developer  
Graduate Student, University at Buffalo  

Praveenkumar Rajendran  
Application Developer  
Graduate Student, University at Buffalo  

Aashish Jain  
Application Developer  
Graduate Student, University at Buffalo  

Atul Singh  
Application Developer  
Graduate Student, University at Buffalo  

Kiran Prabhakar  
Application Developer  
Graduate Student, University at Buffalo  

Ramandeep Singh  
Application Developer  
Graduate Student, University at Buffalo  

Rohit Bhalke  
Application Developer  
Graduate Student, University at Buffalo  

Javier Falca  
Application Developer  
Undergraduate Student, University at Buffalo  

Ryan Dils  
Application Developer  
Undergraduate Student, University at Buffalo  
