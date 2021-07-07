/**
 ##**************************************************************
 ##
 ## Copyright (C) 2018-2020, OneDataShare Team, 
 ## Department of Computer Science and Engineering,
 ## University at Buffalo, Buffalo, NY, 14260.
 ## 
 ## Licensed under the Apache License, Version 2.0 (the "License"); you
 ## may not use this file except in compliance with the License.  You may
 ## obtain a copy of the License at
 ## 
 ##    http://www.apache.org/licenses/LICENSE-2.0
 ## 
 ## Unless required by applicable law or agreed to in writing, software
 ## distributed under the License is distributed on an "AS IS" BASIS,
 ## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ## See the License for the specific language governing permissions and
 ## limitations under the License.
 ##
 ##**************************************************************
 */


 import React, {Component} from 'react';
 import {DROPBOX_TYPE, GOOGLEDRIVE_TYPE, BOX_TYPE, FTP_TYPE, SFTP_TYPE, GRIDFTP_TYPE, HTTP_TYPE, GRIDFTP_NAME, DROPBOX_NAME, GOOGLEDRIVE_NAME, BOX_NAME, getType,getDefaultPortFromUri} from "../../constants";
 import './ClientsInfoComponent.css';
import PropTypes from "prop-types";
import { deleteHistory, deleteCredentialFromServer, history, savedCredList } from "../../APICalls/APICalls";
import { stringify } from 'query-string';


export default class EndPointCredentialsPage extends Component{
    static propTypes = {
		endpoint : PropTypes.object,
		history : PropTypes.array,
	}
     constructor(props){
         super(props);
         this.state = {
            historyList: props.history
         };
		    this.historyListUpdateFromBackend();
     }
     historyListUpdateFromBackend = () => {
		// this.props.setLoading(true);
		history("",-1, (data) =>{
			this.setState({historyList: data.filter((v) => { return v.indexOf(/*this.props.endpoint.uri*/FTP_TYPE) === 0 })});
			//this.props.setLoading(false);
		}, (error) => {
			this._handleError("Unable to retrieve data from backend. Try log out or wait for few minutes.");
			// this.props.setLoading(false);
		});
	}
	createListOfCreds = (historyList)=>{
		const values = Object.values(historyList);
		const a = [];
		values.forEach(element=>{
			//console.log(typeof(element))
			a.push(<div>{element}</div>)
		})

        // Return entire list of credentials
		return a;
	}
     render(){
        const { historyList } = this.state;

        // Store list of credentials in const a (for now)
        const a = (historyList) && this.createListOfCreds(historyList);
		//console.log("finally",historyList)

         // Create new consts to use different font sizes
         const smallFont = {
             fontSize: 16,
         }

         const mediumFont = {
             fontSize: 24,
         }

         return(


             // Currently using indexes in a to show multiple credentials. Need to dynamically code.
             <div>
                 <mediumFont style={mediumFont}> Dropbox Credentials</mediumFont><br></br>

                 <mediumFont style={mediumFont}> FTP Credentials </mediumFont><br></br>


				 <smallFont style={smallFont}>
                     {a[0]}
                     {a[1]}
                 </smallFont>

                 <mediumFont style={mediumFont}> Google Drive Credentials</mediumFont><br></br>

                 <mediumFont style={mediumFont}> Box Credentials</mediumFont><br></br>

                 <mediumFont style={mediumFont}> GridFTP Credentials</mediumFont><br></br>

                 <mediumFont style={mediumFont}> HTTP/HTTPS Credentials</mediumFont><br></br>

                 <mediumFont style={mediumFont}> SFTP Credentials</mediumFont><br></br>


            </div>
         );
     }
 }
 