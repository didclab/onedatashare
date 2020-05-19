# ODS Rest API Documentation #
This guide serves the purpose of displaying basic ways to interact and use the ODS API.

## Pre-reqs ## 
ODS exposes a REST API that uses JSON, so having working knowledge and understanding of HTTP requests that use JSON is required.
[JSON documentation][4] please read the link to have basic knowledge of working with JSON.<br/>
Please reference: Our Open API documentation, this is the most accurate form of all JSON objects. [ODS JSON Docs][2]. <br/>
All Objects found in our Open Api Definition supersedes all object references in this document. 

## Table of Contents ##
1. [Ticket](#ticket-submission)
1. [Overview](#Need to Know)
1. [Authentication](#example)
2. [Transfer](#example2)

#### Ticket Submission
If you encounter an issue using any ODS service please submit a ticket to our [GitHome][3]
When submitting a ticket, it must contain a few pieces of information. 
1. Date and Time
2. The ODS Service you are using.
3. What you are trying to achieve
4. The output and the Error you encountered 
5. A detailed instruction on your procedure in getting the error you encountered.

### Overview ###
The ODS Rest API relies on JWT tokens to authenticate and allow you access to our backend services. 
The first thing to do is please head over to [ODS Home][1] and make an account.<br/>
Please explore the UI so that you have a general understanding of what kind of services we offer before exploring our Rest API.</br>


### Authentication ###
To confirm if your credentials are a registered user you may use **/is-email-registered** route to receive a boolean response on whether the credentials specified are registered. 
The body this request takes in is a **LoginRequest**.
```javascript
{
    "email":"apples@gmail.com",
    "password":"verybadpassword"
}
```
At a minimum the body must contain an email and a password. The response being 
```javascript
{
    "false"
}
```
as the user is not registered.<br/>

To receive your token you must use go to [ODS Home][1] and retrieve it. There currently no permanent tokens nor are there any plans to add them currently. There is no programmatic way to refresh your token(feature in development).
Once you are authenticated and you have your token, you will need to add this token to the Authorization Header of your request. Not all routes are secure so please double check your routes to see if a token is required or not.
<br/>**Please keep this token secure**

### Transfer ###
The transfer service is a core feature being offered by ODS. The request route you The JSON object we are looking for is **TransferRequest**, 
Currently to construct the body for this request you need three keys.

```javascript
TransferRequest=
{
    "src":"UserActionResource",
    "dest":"UserActionResource",
    "options":"TransferOptions"
}
```
To find more information on **UserActionResource** and **TransferOptions** please check [ODS JSON Docs][2].
For more in depth examples on how our api gets exercised please reference our python cli [ODS CLI][5] as it covers most of the routes we expose.
 
[1]: https://www.onedatashare.org
[2]: https://www.onedatashare.org/webjars/swagger-ui/index.html?configUrl=/v3/api-docs/swagger-config#/
[3]: https://github.com/didclab/onedatashare/issues
[4]: https://www.json.org/json-en.html
[5]: https://github.com/didclab/ods-cli/tree/Bhakti
