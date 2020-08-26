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


/*	Window in the Transfer Component */

import React, { Component } from 'react';
import Button from '@material-ui/core/Button';
import Grid from "@material-ui/core/Grid";
import { openDropboxOAuth, openGoogleDriveOAuth, openGridFtpOAuth, openBoxOAuth } from "../../APICalls/EndpointAPICalls";
import { savedCredList } from "../../APICalls/APICalls";
import {store} from "../../App";
import PropTypes from "prop-types";
import {cookies} from "../../model/reducers.js";


import LinearProgress from '@material-ui/core/LinearProgress';

import { loadCSS } from 'fg-loadcss';
import Icon from '@material-ui/core/Icon';
import {makeStyles, styled} from "@material-ui/core/styles";

import EndpointBrowseComponent from "./EndpointBrowseComponent";
import EndpointAuthenticateComponent from "./EndpointAuthenticateComponent";
import {DROPBOX_TYPE, GOOGLEDRIVE_TYPE, BOX_TYPE, FTP_TYPE, SFTP_TYPE, GRIDFTP_TYPE, HTTP_TYPE, GRIDFTP_NAME, DROPBOX_NAME, GOOGLEDRIVE_NAME, BOX_NAME, getType} from "../../constants";

import {eventEmitter} from "../../App";

const pickModule = 0;
const inModule = 1;

export default class BrowseModuleComponent extends Component {

	static propTypes = {
		endpoint : PropTypes.object,
		history : PropTypes.array,
		mode : PropTypes.number,
		update : PropTypes.func,
		type: PropTypes.string,
		display: PropTypes.string 
	}

	constructor(props){
		super(props);

		const checkIfOneSideIsLoggedInAsGrid = (currentState) => {
			return (getType(currentState.endpoint1) === GRIDFTP_TYPE || getType(currentState.endpoint2) === GRIDFTP_TYPE) && (currentState.endpoint1.login || currentState.endpoint1.login);
		}
		const checkIfGridftpIsOpen = (currentState) => {
			return (getType(currentState.endpoint1) === GRIDFTP_TYPE 
				|| getType(currentState.endpoint2) === GRIDFTP_TYPE) 
				|| !(currentState.endpoint1.login || currentState.endpoint1.login);
		}


		let constructState = store.getState();

		this.state={
			history: props.history.filter((v) => { return v.indexOf(props.endpoint.uri) === 0 }),
			creds: {},
			endpoint: props.endpoint, 
			mode: props.mode,
			loading: false,
			oneSideIsLoggedInAsGridftp: checkIfOneSideIsLoggedInAsGrid(constructState),
			gridftpIsOpen: checkIfGridftpIsOpen(constructState)
		};

		this.unsubcribe = store.subscribe(() => {
			let currentState = store.getState();
			// Check if either side is logged in as GRID_FTP
			let oneSideIsLoggedInAsGrid = checkIfOneSideIsLoggedInAsGrid(currentState);
			let gridftpIsOpen = checkIfGridftpIsOpen(currentState);
			if(oneSideIsLoggedInAsGrid !== this.state.oneSideIsLoggedInAsGridftp || gridftpIsOpen !== this.state.gridftpIsOpen){
				this.setState({oneSideIsLoggedInAsGridftp: oneSideIsLoggedInAsGrid, gridftpIsOpen: gridftpIsOpen});
			}
    	});

		this.setLoading = this.setLoading.bind(this);
		this.getLoading = this.getLoading.bind(this);

		this.credentialTypeExistsThenDo = this.credentialTypeExistsThenDo.bind(this);
		this._handleError = this._handleError.bind(this);
	}

	endpointButton = () => styled(Button)({
		flexGrow: 1,
		justifyContent: "flex-start",
		width: "100%",
		fontSize: "16px",
		paddingLeft: "35%"
	});

	setLoading(bool){
		this.setState({loading: bool});
	};

	getLoading(){
		return this.state.loading;
	};

	_handleError = (msg) =>{
    	eventEmitter.emit("errorOccured", msg);
	}

	componentDidMount(){
		loadCSS(
	      'https://use.fontawesome.com/releases/v5.1.0/css/all.css',
	      document.querySelector('#font-awesome-css'),
	    );
	}

	credentialTypeExistsThenDo = (containsType, succeed, failed) => {
		this.setLoading(true);
		
		if(store.getState().saveOAuthTokens){
			// If the user has opted to store tokens on ODS server,
			// query backed for saved credentials
			console.log("Checking backend for " + containsType + " credentials");

			savedCredList((data) => {
				if(Object.keys(data).some(id => {
					return data[id].name.toLowerCase().indexOf(containsType.toLowerCase()) !== -1
				})){
					succeed(data);
				}else{
					failed();
				}
				this.setLoading(false);
			}, (error) =>{
				this._handleError("Could not get credential from our server. Maybe check your internet connection.");
				failed();
				this.setLoading(false);
			});
		}
		else{
			// If the user has opted not to store tokens on ODS server,
			// query cookies for saved credentials
			console.log("Checking cookies for " + containsType + " credentials");

			let creds = cookies.get(containsType) || 0;
			if(creds !== 0){
				creds= JSON.parse(creds);
				succeed(creds);
				this.setLoading(false);
			}
			else{
				failed();
				this.setLoading(false);
			}
		}
	}

	render() {
		const {endpoint, mode, history, type, loading, creds, oneSideIsLoggedInAsGridftp, gridftpIsOpen} = this.state;
		const {update} = this.props;
		const loginPrep = (uri) => (data) => {
			this.setState({mode: inModule, history: this.props.history.filter(
				(v) => { return v.indexOf(uri) === 0 }),
				endpoint: {...endpoint, uri: uri},
				creds: data
			});
			this.props.update({mode: inModule, endpoint: {...endpoint, uri: uri}});
		}

		const backHome = () => {
			this.setState({mode: pickModule, endpoint: {...endpoint, uri: "", login: false, credential: {}}});
			this.props.update({mode: pickModule, endpoint: {...endpoint, uri: "", login: false, credential: {}}});
		}

		// const iconStyle = {marginRight: "10px", fontSize: "16px", width: "20px"};
		const buttonStyle1 = {flexGrow: 1, justifyContent: "flex-start", width: "100%", fontSize: "12px", paddingLeft: "30%"};
		const buttonStyle = {label: "browseButton"};
		const EndpointButton = this.endpointButton();

	  return (
	    // saved credential
	    // login manually
	    <div id={"browser"+endpoint.side} className={"transferGroup"} /*style={{borderWidth: '1px', borderColor: '#005bbb',borderStyle: 'solid',borderRadius: '10px', width: 'auto', height: 'auto', overflow: "hidden"}}*/>
	      	{(!endpoint.login && mode === pickModule) &&
	      	<div className={"browseContainer"} /*style={{height: "100%", display: "flex", flexDirection: "column", justifyContent: "flex-start"}}*/>
	      		<EndpointButton id={endpoint.side + "DropBox"}  disabled={oneSideIsLoggedInAsGridftp} onClick={() => {
		      		this.credentialTypeExistsThenDo(DROPBOX_NAME, loginPrep(DROPBOX_TYPE), openDropboxOAuth);
		      	}}>
		      		<Icon className={'fab fa-dropbox browseIcon'}/>
		      		DropBox
		      	</EndpointButton>
	      		<EndpointButton id={endpoint.side + "FTP"} disabled={oneSideIsLoggedInAsGridftp} onClick={() => {
		      		loginPrep(FTP_TYPE)()
		      	}}>
		      		<Icon className={'far fa-folder-open browseIcon'}/>
		      		FTP
	      		</EndpointButton>
		      	<EndpointButton id={endpoint.side + "GoogleDrive"} disabled={oneSideIsLoggedInAsGridftp} onClick={() => {

		      		this.credentialTypeExistsThenDo(GOOGLEDRIVE_NAME, loginPrep(GOOGLEDRIVE_TYPE), openGoogleDriveOAuth);
		      	}}>
			      	<Icon className={'fab fa-google-drive browseIcon'}/>
			      	Google Drive
		      	</EndpointButton>
                <EndpointButton id={endpoint.side + "Box"} disabled={oneSideIsLoggedInAsGridftp} onClick={() => {

                    this.credentialTypeExistsThenDo(BOX_NAME, loginPrep(BOX_TYPE), openBoxOAuth);
                }}>
					<Icon className={'fas fa-bold browseIcon'}/>
                    Box
                </EndpointButton>
				<EndpointButton id={endpoint.side + "GridFTP"} hidden="true	" disabled={!gridftpIsOpen} onClick={() =>{
					this.credentialTypeExistsThenDo(GRIDFTP_NAME, loginPrep(GRIDFTP_TYPE), openGridFtpOAuth);
				}}>
					<Icon className={'fas fa-server browseIcon'}/>
				GridFTP
				</EndpointButton>
				<EndpointButton id={endpoint.side + "HTTP"}  disabled={oneSideIsLoggedInAsGridftp} onClick={() =>{
	      			loginPrep(HTTP_TYPE)()
	      		}}>
		      		<Icon className={'fas fa-globe browseIcon'}/>
		      		HTTP/HTTPS
	      		</EndpointButton>

		      	<EndpointButton id={endpoint.side + "SFTP"}  disabled={oneSideIsLoggedInAsGridftp} onClick={() =>{
		      		loginPrep(SFTP_TYPE)()
		      	}}>
		      		<Icon className={'fas fa-terminal browseIcon'}/>
		      		SFTP
		      	</EndpointButton>
		    </div>}

		    {(!endpoint.login && mode === inModule) &&
	      	<div>
	      		{loading && <LinearProgress/>}
		      	<EndpointAuthenticateComponent endpoint={endpoint} 
		      		history={history} type={type}
		      		credentials={creds}
			      	loginSuccess={(object) =>{
			      		this.setState({endpoint: object});
			      		update({endpoint: object})
			      	}}
			      	setLoading = {this.setLoading}
			      	back={backHome}
		      	/>
		    </div>}
		    {endpoint.login &&
	      	<div>
	      		{loading && <LinearProgress/>}
		      	<EndpointBrowseComponent 
		      		endpoint={endpoint} 
		      		setLoading = {this.setLoading}
		      		getLoading = {this.getLoading} 
		      		back={backHome}
		      		displayStyle={this.props.displayStyle}
		      	/>
		    </div>}
	      </div>
	    );
  	}
}


