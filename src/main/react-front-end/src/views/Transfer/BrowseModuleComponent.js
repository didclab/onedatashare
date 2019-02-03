/*	Window in the Transfer Component */

import React, { Component } from 'react';
import Button from '@material-ui/core/Button';
import {openDropboxOAuth, openGoogleDriveOAuth, openGridFtpOAuth, history, dropboxCredList} from "../../APICalls/APICalls";
import {store} from "../../App";
import {endpointProgress} from "../../model/actions"
import PropTypes from "prop-types";

import LinearProgress from '@material-ui/core/LinearProgress';

import EndpointBrowseComponent from "./EndpointBrowseComponent";
import EndpointAuthenticateComponent from "./EndpointAuthenticateComponent";
import {DROPBOX_TYPE, GOOGLEDRIVE_TYPE, FTP_TYPE, SFTP_TYPE, GRIDFTP_TYPE, HTTP_TYPE, SCP_TYPE, GRIDFTP_NAME, DROPBOX_NAME, GOOGLEDRIVE_NAME} from "../../constants";

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
		gridFTPCredentialExists: false
	}

	constructor(props){
		super(props);
		this.state={
			history: props.history.filter((v) => { return v.indexOf(props.endpoint.uri) == 0 }),
			endpoint: props.endpoint, 
			mode: props.mode,
			loading: false,
		};
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

	credentialTypeExistsThenDo = (containsType, succeed, failed) => {
		this.setLoading(true);
		dropboxCredList((data) => {
			console.log(data);
			if(Object.keys(data).some(id => {
				return data[id].name.toLowerCase().
				indexOf(containsType.toLowerCase()) != -1 
			})){
				succeed();
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
		const {endpoint, mode, history, type, loading, gridFTPCredentialExists} = this.state;
		const {update} = this.props;
		const loginPrep = (uri) => () => {

			this.setState({mode: inModule, history: this.props.history.filter(
				(v) => { return v.indexOf(uri) == 0 }), endpoint: {...endpoint, uri: uri} 
			});
			this.props.update({mode: inModule, endpoint: {...endpoint, uri: uri}});
		}

		const backHome = (uri, type) => {
			this.setState({mode: pickModule, endpoint: {...endpoint, uri: "", login: false, credential: {}}});
			this.props.update({mode: pickModule, endpoint: {...endpoint, uri: "", login: false, credential: {}}});
		}

		const buttonStyle = {flexGrow: 1, width: "100%", fontSize: "12px"};
	    return (
	    // saved credential
	    // login manually
	    <div style={{borderWidth: '1px', borderColor: '#005bbb',borderStyle: 'solid',borderRadius: '10px', width: '100%', height: '100%', overflow: "hidden"}}>
	      	{(!endpoint.login && mode == pickModule) &&
	      	<div style={{height: "100%",display: "flex", flexDirection: "column"}}>
		      	<Button style={buttonStyle} onClick={() => {
		      		this.credentialTypeExistsThenDo(DROPBOX_NAME, loginPrep(DROPBOX_TYPE), openDropboxOAuth);
		      	}}>DropBox</Button>
		      	<Button style={buttonStyle} onClick={() => {
		      		this.credentialTypeExistsThenDo(GOOGLEDRIVE_NAME, loginPrep(GOOGLEDRIVE_TYPE), openGoogleDriveOAuth);
		      	}}>Google Drive</Button>
		      	<Button style={buttonStyle} onClick={() =>{
		      		this.credentialTypeExistsThenDo(GRIDFTP_NAME, loginPrep(GRIDFTP_TYPE), openGridFtpOAuth);
		      	}}>Grid FTP</Button>
		      	<Button style={buttonStyle} onClick={() => {
		      		loginPrep(FTP_TYPE)()
		      	}}>FTP</Button>
		      	<Button style={buttonStyle} onClick={() =>{
		      		loginPrep(SFTP_TYPE)()
		      	}}>SFTP</Button>
		      	<Button style={buttonStyle} onClick={() =>{
		      		loginPrep(HTTP_TYPE)()
		      	}}>HTTP</Button>
		      	<Button style={buttonStyle} onClick={() =>{
		      		loginPrep(SCP_TYPE)()
		      	}}>SSH</Button>

		    </div>}

		    {(!endpoint.login && mode == inModule) &&
	      	<div>
	      		{loading && <LinearProgress/>}
		      	<EndpointAuthenticateComponent endpoint={endpoint} 
		      		history={history} type={type}
			      	loginSuccess={(object) =>{
			      		this.setState({endpoint: object});
			      		update({endpoint: object})
			      	}}
			      	setLoading = {this.setLoading}
			      	back={() =>{
		      			backHome();
		      		}}
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
		      	/>
		    </div>}

	      </div>
	    );
  	}
}


