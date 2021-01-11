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
import {styled} from "@material-ui/core/styles";

import EndpointBrowseComponent from "./EndpointBrowseComponent";
import EndpointAuthenticateComponent from "./EndpointAuthenticateComponent";
import {DROPBOX_TYPE, GOOGLEDRIVE_TYPE, BOX_TYPE, FTP_TYPE, SFTP_TYPE, GRIDFTP_TYPE, HTTP_TYPE, GRIDFTP_NAME, DROPBOX_NAME, GOOGLEDRIVE_NAME, BOX_NAME, GRIDFTP,  getType} from "../../constants";
import {showText, showType, showDisplay} from "../../constants";
import {OAuthFunctions} from "../../APICalls/EndpointAPICalls";

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
			return (getType(currentState.endpoint1) === showType.gsiftp || getType(currentState.endpoint2) === showType.gsiftp) && (currentState.endpoint1.login || currentState.endpoint1.login);
		}
		const checkIfGridftpIsOpen = (currentState) => {
			return (getType(currentState.endpoint1) === showType.gsiftp
				|| getType(currentState.endpoint2) === showType.gsiftp)
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
		this.backHome = this.backHome.bind(this);
		this.loginPrep = this.loginPrep.bind(this);
		this.login = this.login.bind(this);
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

	backHome = () => {
		this.setState(prevState => ({mode: pickModule, endpoint: {...prevState.endpoint, uri: "", login: false, credential: {}}}));
		this.props.update(prevState => ({mode: pickModule, endpoint: {...prevState.endpoint, uri: "", login: false, credential: {}}}));
	}

	loginPrep = (uri) => (data) => {
		const {endpoint} = this.state;
		this.setState({mode: inModule, history: this.props.history.filter(
				(v) => { return v.indexOf(uri) === 0 }),
			endpoint: {...endpoint, uri: uri},
			creds: data? data.list : {}
		});
		this.props.update({mode: inModule, endpoint: {...endpoint, uri: uri}});
	}

	login = (service) => {
		if(service[1].credTypeExists){
			this.credentialTypeExistsThenDo(showText[service[0]], this.loginPrep(showType[service[0]]), OAuthFunctions[showType[service[0]]]);
		}else{
			this.loginPrep(showType[service[0]])();
		}
	}

	render() {
		const {endpoint, mode, history, type, loading, creds, oneSideIsLoggedInAsGridftp, gridftpIsOpen} = this.state;
		const {update} = this.props;
		const login = (service) => {
			if(service[1].credTypeExists){
				this.credentialTypeExistsThenDo(showText[service[0]], this.loginPrep(showType[service[0]]), OAuthFunctions[showType[service[0]]]);
			}else{
				this.loginPrep(showType[service[0]])();
			}
		}


		const EndpointButton = this.endpointButton();
		const displays = Object.entries(showDisplay);

	  return (
	    // saved credential
	    // login manually
	    <div id={"browser"+endpoint.side} className={"transferGroup"} /*style={{borderWidth: '1px', borderColor: '#005bbb',borderStyle: 'solid',borderRadius: '10px', width: 'auto', height: 'auto', overflow: "hidden"}}*/>
	      	{(!endpoint.login && mode === pickModule) &&
	      	<div className={"browseContainer"} /*style={{height: "100%", display: "flex", flexDirection: "column", justifyContent: "flex-start"}}*/>
				{displays.map( (service) => {
					const disable = service[0] === GRIDFTP ? !gridftpIsOpen : oneSideIsLoggedInAsGridftp;
					return(
						<EndpointButton id={service.side + service[1].id} disabled={disable} onClick={() => {this.login(service)}}>
							<Icon className={service[1].icon + ' browseIcon'}/>
							{service[1].label}
						</EndpointButton>
					);
				})}
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
			      	back={this.backHome}
		      	/>
		    </div>}
		    {endpoint.login &&
	      	<div>
	      		{loading && <LinearProgress/>}
		      	<EndpointBrowseComponent 
		      		endpoint={endpoint} 
		      		setLoading = {this.setLoading}
		      		getLoading = {this.getLoading} 
		      		back={this.backHome}
		      		displayStyle={this.props.displayStyle}
		      	/>
		    </div>}
	      </div>
	    );
  	}
}


