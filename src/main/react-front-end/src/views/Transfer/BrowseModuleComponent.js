/*	Window in the Transfer Component */

import React, { Component } from 'react';
import Button from '@material-ui/core/Button';
import {openDropboxOAuth, openGoogleDriveOAuth, openGridFtpOAuth, openBoxOAuth, savedCredList} from "../../APICalls/APICalls";
import {store} from "../../App";
import PropTypes from "prop-types";
import {cookies} from "../../model/reducers.js";
import './transfer.css';

import LinearProgress from '@material-ui/core/LinearProgress';

import { loadCSS } from 'fg-loadcss';
import Icon from '@material-ui/core/Icon';

import EndpointBrowseComponent from "./EndpointBrowseComponent";
import EndpointAuthenticateComponent from "./EndpointAuthenticateComponent";
import {DROPBOX_TYPE, GOOGLEDRIVE_TYPE, BOX_TYPE, FTP_TYPE, SFTP_TYPE, GRIDFTP_TYPE, HTTP_TYPE, SCP_TYPE, GRIDFTP_NAME, DROPBOX_NAME, GOOGLEDRIVE_NAME, BOX_NAME, getType} from "../../constants";

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


	  return (
	    // saved credential
	    // login manually
	    <div id={"browser"+endpoint.side} className="browseModuleComponent_endpoint_style">
	      	{(!endpoint.login && mode === pickModule) &&
	      	<div className="browseModuleComponent_innerdiv">
	      		<Button id={endpoint.side + "DropBox"} id="browseModule_buttonStyle" disabled={oneSideIsLoggedInAsGridftp} onClick={() => {
		      		this.credentialTypeExistsThenDo(DROPBOX_NAME, loginPrep(DROPBOX_TYPE), openDropboxOAuth);
		      	}}>
		      		<Icon className={'fab fa-dropbox'} id="browseModule_iconStyle" />
		      		DropBox
		      	</Button>
	      		<Button id={endpoint.side + "FTP"} id="browseModule_buttonStyle" disabled={oneSideIsLoggedInAsGridftp} onClick={() => {
		      		loginPrep(FTP_TYPE)()
		      	}}>
		      		<Icon className={'far fa-folder-open'} id="browseModule_iconStyle"/>
		      		FTP
	      		</Button>
		      	<Button id={endpoint.side + "GoogleDrive"} id="browseModule_buttonStyle" disabled={oneSideIsLoggedInAsGridftp} onClick={() => {

		      		this.credentialTypeExistsThenDo(GOOGLEDRIVE_NAME, loginPrep(GOOGLEDRIVE_TYPE), openGoogleDriveOAuth);
		      	}}>
			      	<Icon className={'fab fa-google-drive'} id="browseModule_iconStyle"/>
			      	Google Drive
		      	</Button>
                <Button id={endpoint.side + "Box"} id="browseModule_buttonStyle" disabled={oneSideIsLoggedInAsGridftp} onClick={() => {

                    this.credentialTypeExistsThenDo(BOX_NAME, loginPrep(BOX_TYPE), openBoxOAuth);
                }}>
					<Icon className={'fas fa-bold'} id="browseModule_iconStyle"/>
                    Box
                </Button>
	      		<Button id={endpoint.side + "GridFTP"} id="browseModule_buttonStyle" disabled={!gridftpIsOpen} onClick={() =>{
	      			this.credentialTypeExistsThenDo(GRIDFTP_NAME, loginPrep(GRIDFTP_TYPE), openGridFtpOAuth);
	      		}}>
	      			<Icon className={'fas fa-server'} id="browseModule_iconStyle"/>
	      		GridFTP
		      	</Button>

				<Button id={endpoint.side + "HTTP"} id="browseModule_buttonStyle" disabled={oneSideIsLoggedInAsGridftp} onClick={() =>{
	      			loginPrep(HTTP_TYPE)()
	      		}}>
		      		<Icon className={'fas fa-globe'} id="browseModule_iconStyle"/>
		      		HTTP/HTTPS
	      		</Button>

		      	<Button id={endpoint.side + "SFTP"} id="browseModule_buttonStyle" disabled={oneSideIsLoggedInAsGridftp} onClick={() =>{
		      		loginPrep(SFTP_TYPE)()
		      	}}>
		      		<Icon className={'fas fa-folder-open'} id="browseModule_iconStyle"/>
		      		SFTP
		      	</Button>

	      		<Button id={endpoint.side + "SSH"} id="browseModule_buttonStyle" disabled={oneSideIsLoggedInAsGridftp} onClick={() =>{
	      			loginPrep(SCP_TYPE)()
	      		}}>
	      			<Icon className={'fas fa-terminal'} id="browseModule_iconStyle"/>
	      			SCP
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


