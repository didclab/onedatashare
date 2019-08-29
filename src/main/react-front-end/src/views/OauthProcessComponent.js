import React, { Component, Text } from 'react';

import PropTypes from 'prop-types';

import  { Redirect } from 'react-router-dom';
import {store} from '../App';
import {transferPageUrl, oauthPreUrl,
		DROPBOX_TYPE, GOOGLEDRIVE_TYPE, FTP_TYPE, SFTP_TYPE, GRIDFTP_TYPE, HTTP_TYPE,
		sideLeft, sideRight, DROPBOX_NAME
} from "../constants";
import {eventEmitter} from "../App";
import {endpointLogin} from '../model/actions';
import { cookies } from '../model/reducers';

export default class OauthProcessComponent extends Component{

	constructor(props){
			
		super(props);
		const {tag} = this.props.match.params;

		if(tag === 'uuid'){
			console.log('User has opted to save auth tokens at ODS servers');
			console.log('UUID received');
		}
		else if(tag === 'dropbox' ){
			let qs = this.props.location.search;
			console.log('Dropbox identifier received');
			
			let qsObj = JSON.parse(decodeURIComponent(qs.substring(qs.indexOf('=') + 1)));
			let dropboxCreds = cookies.get(DROPBOX_NAME) || 0;
			if(dropboxCreds !== 0){
				let parsedJSON = JSON.parse(dropboxCreds);
				parsedJSON.push({name : qsObj.name.split(":+")[1], token : qsObj.token });
				cookies.set(DROPBOX_NAME, JSON.stringify(parsedJSON));
			}
			else{
				cookies.set(DROPBOX_NAME, JSON.stringify([{name : qsObj.name.split(":+")[1], token : qsObj.token }]));
			}
		}
		else if(tag === 'googledrive'){

		}
		else if(tag === 'gridftp'){

		}

		// this.state={
		// 	: id,
		// }
		
//		let queryString = this.props.location.search;
//		console.log(this.props.match.params, queryString);

		if(tag === "ExistingCredGoogleDrive"){
            setTimeout( () => {eventEmitter.emit(
               "errorOccured","Credential for the endpoint already Exists. Please logout from Google Drive and try again."
            )}, 500);
		}else if(tag === "ExistingCredDropbox"){
            setTimeout( () => { eventEmitter.emit(
                "errorOccured","Credential for the endpoint already Exists. Please logout from Dropbox and try again."
            )}, 500);
        }else{
            endpointLogin(DROPBOX_TYPE, sideLeft, {uuid: tag});
	    }
	}

	render(){
		// let search = new URLSearchParams(window.location.search);
		// console.log('search is ', search);
		// let oauthId = search.get("state");
		// if(oauthId!=null){
		//   console.log("OAUTH id ", oauthId);
		//   cookies.set("OAUTH", oauthId);
		// }
		
		// const {id} = this.state;
		return <div>
			{/* from={oauthPreUrl+id} */}
			<Redirect  to={transferPageUrl}></Redirect>
			<h1> 
				Wait a second, You will be redirected.
			</h1>
			{/* <h2>
				ID: {id}
			</h2> */}
		</div>
	}
}