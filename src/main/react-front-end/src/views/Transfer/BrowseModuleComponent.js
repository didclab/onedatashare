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
import { openDropboxOAuth, openGoogleDriveOAuth, openGridFtpOAuth, openBoxOAuth } from "../../APICalls/EndpointAPICalls";
import { savedCredList } from "../../APICalls/APICalls";
import {store} from "../../App";
import PropTypes from "prop-types";
import {cookies} from "../../model/reducers.js";


import LinearProgress from '@material-ui/core/LinearProgress';

import { loadCSS } from 'fg-loadcss';
import Icon from '@material-ui/core/Icon';

import EndpointBrowseComponent from "./EndpointBrowseComponent";
import EndpointAuthenticateComponent from "./EndpointAuthenticateComponent";
import {DROPBOX_TYPE, GOOGLEDRIVE_TYPE, BOX_TYPE, FTP_TYPE, SFTP_TYPE, GRIDFTP_TYPE, HTTP_TYPE, GRIDFTP, DROPBOX, GOOGLEDRIVE, BOX, getType} from "../../constants";

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
		
			// If the user has opted to store tokens on ODS server,
			// query backed for saved credentials
			console.log("Checking backend for " + containsType + " credentials");

			savedCredList(containsType, (data) => {
				if(data !== undefined && data.list.length > 0){
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

	render() {
		const {endpoint, mode, history, type, loading, creds, oneSideIsLoggedInAsGridftp, gridftpIsOpen} = this.state;
		const {update} = this.props;
		const loginPrep = (uri) => (data) => {
			this.setState({mode: inModule, history: this.props.history.filter(
				(v) => { return v.indexOf(uri) === 0 }),
				endpoint: {...endpoint, uri: uri},
				creds: data ? data.list : {}
			});
			this.props.update({mode: inModule, endpoint: {...endpoint, uri: uri}});
		}

		const backHome = () => {
			this.setState({mode: pickModule, endpoint: {...endpoint, uri: "", login: false, credential: {}}});
			this.props.update({mode: pickModule, endpoint: {...endpoint, uri: "", login: false, credential: {}}});
		}

		const iconStyle = {marginRight: "10px", fontSize: "16px", width: "20px"};
		const buttonStyle = {flexGrow: 1, justifyContent: "flex-start", width: "100%", fontSize: "12px", paddingLeft: "30%"};

	  return (
	    // saved credential
	    // login manually
	    <div id={"browser"+endpoint.side} style={{borderWidth: '1px', borderColor: '#005bbb',borderStyle: 'solid',borderRadius: '10px', width: 'auto', height: 'auto', overflow: "hidden"}}>
	      	{(!endpoint.login && mode === pickModule) &&
	      	<div style={{height: "100%", display: "flex", flexDirection: "column", }}>
	      		<Button id={endpoint.side + "DropBox"} style={buttonStyle} disabled={oneSideIsLoggedInAsGridftp} onClick={() => {
		      		this.credentialTypeExistsThenDo(DROPBOX, loginPrep(DROPBOX_TYPE), openDropboxOAuth);
		      	}}>
		      		<Icon className={'fab fa-dropbox'} style={iconStyle}/>
		      		DropBox
		      	</Button>
	      		<Button id={endpoint.side + "FTP"} style={buttonStyle} disabled={oneSideIsLoggedInAsGridftp} onClick={() => {
		      		loginPrep(FTP_TYPE)()
		      	}}>
		      		<Icon className={'far fa-folder-open'} style={iconStyle}/>
		      		FTP
	      		</Button>
		      	<Button id={endpoint.side + "GoogleDrive"} style={buttonStyle} disabled={oneSideIsLoggedInAsGridftp} onClick={() => {

		      		this.credentialTypeExistsThenDo(GOOGLEDRIVE, loginPrep(GOOGLEDRIVE_TYPE), openGoogleDriveOAuth);
		      	}}>
			      	<Icon className={'fab fa-google-drive'} style={iconStyle}/>
			      	Google Drive
		      	</Button>
                <Button id={endpoint.side + "Box"} style={buttonStyle} disabled={oneSideIsLoggedInAsGridftp} onClick={() => {

                    this.credentialTypeExistsThenDo(BOX, loginPrep(BOX_TYPE), openBoxOAuth);
                }}>
					<Icon className={'fas fa-bold'} style={iconStyle}/>
                    Box
                </Button>
				<Button id={endpoint.side + "GridFTP"} style={buttonStyle} hidden="true	" disabled={!gridftpIsOpen} onClick={() =>{
					this.credentialTypeExistsThenDo(GRIDFTP, loginPrep(GRIDFTP_TYPE), openGridFtpOAuth);
				}}>
					<Icon className={'fas fa-server'} style={iconStyle}/>
				GridFTP
				</Button> 
				<Button id={endpoint.side + "HTTP"} style={buttonStyle} disabled={oneSideIsLoggedInAsGridftp} onClick={() =>{
	      			loginPrep(HTTP_TYPE)()
	      		}}>
		      		<Icon className={'fas fa-globe'} style={iconStyle}/>
		      		HTTP/HTTPS
	      		</Button>

		      	<Button id={endpoint.side + "SFTP"} style={buttonStyle} disabled={oneSideIsLoggedInAsGridftp} onClick={() =>{
		      		loginPrep(SFTP_TYPE)()
		      	}}>
		      		<Icon className={'fas fa-terminal'} style={iconStyle}/>
		      		SFTP
		      	</Button>
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
					updateCredentials = {(data => {
						this.setState({creds: data? data.list: {}})
					})}
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


