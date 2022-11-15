# Link to starter repository #

[OneDataShare Starter](https://github.com/didclab/ODS-Starter)

Recommended IDEs (used by ODS team for development):
1. IntelliJ Idea Community Edition (for backend code)
2. Visual Studio Code (for front-end code)

## Installation Procedure ##

1. Clone OneDataShare GitHub repository.
	Open command prompt (Windows OS) or terminal (Unix-based OS)  
	Run following command –  
	git clone https://github.com/didclab/onedatashare.git

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

5. To execute ODS as a standalone application, follow steps mentioned in the standalone-execution.txt file.  
   To execute ODS using docker containers, follow steps mentioned in the docker-execution.txt file.

## Contact ODS Team ##

Please direct all issues/concerns/queries with respect to OneDataShare using the support page (https://www.onedatashare.org/support) or by sending us an email at admin@onedatashare.org.

## OneDataShare Team ##

Dr. Tevfik Kosar (tkosar@buffalo.edu)  
Product Owner  
https://cse.buffalo.edu/faculty/tkosar/  
Associate Professor, University at Buffalo   

Jacob Goldverg (jacobgol@buffalo.edu)
Lead Developer
https://engineering.buffalo.edu/computer-science-engineering/people/phd-candidates.html
PhD candidate, University at Buffalo

