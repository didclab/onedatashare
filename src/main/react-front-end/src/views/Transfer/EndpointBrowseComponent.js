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


import { multiSelectTo as multiSelect } from './utils';
import FileNode from "./FileNode.js";
import CompactFileNodeWrapper from './CompactFileNode/CompactFileNodeWrapper.js';
import { updateViewPreference } from "../../APICalls/APICalls";

import { Droppable } from 'react-beautiful-dnd';

import NewFolderIcon from "@material-ui/icons/CreateNewFolder";
import DeleteIcon from "@material-ui/icons/DeleteForever";
import DownloadButton from "@material-ui/icons/CloudDownload";
import LinkButton from "@material-ui/icons/Link";
import LogoutButton from "@material-ui/icons/ExitToApp";
import RefreshButton from "@material-ui/icons/Refresh";
import Code from '@material-ui/icons/Code';
import {Button, ButtonGroup, Popover, FormControl, FormGroup , Grid, Container, Box} from '@material-ui/core';

import {InputGroup} from "react-bootstrap";
import TextField from '@material-ui/core/TextField';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import Typography from "@material-ui/core/Typography";
// import Tooltip from '@material-ui/core/Tooltip';

import UploaderWrapper from "./UploaderWrapper.js";

import React, { Component } from 'react';

import {
	listFiles,
	mkdir,
	deleteCall,
	download,
	getDownload,
	getSharableLink,
	openBoxOAuth,
	openDropboxOAuth,
	openGoogleDriveOAuth
} from "../../APICalls/EndpointAPICalls";


import { Breadcrumb, OverlayTrigger, Tooltip} from 'react-bootstrap';
import { getFilesFromMemory, getIdsFromEndpoint, getPathFromMemory, 
		emptyFileNodesData, getEntities, setSelectedTasksForSide,  getSelectedTasksFromSide, 
		unselectAll, makeFileNameFromPath, draggingTask, setFilesWithPathListAndId, } from "./initialize_dnd";

import {eventEmitter, store} from "../../App";

import { cookies } from "../../model/reducers";
import { getName, getType } from '../../constants.js';
import { DROPBOX_TYPE, GOOGLEDRIVE_TYPE, BOX_TYPE, SFTP_TYPE, HTTP_TYPE, FTP_TYPE } from "../../constants";
import { CopyToClipboard } from 'react-copy-to-clipboard';
import ProgressUpdateComponent from "./progressUpdateComponent"
import {compactViewPreference} from "../../model/actions";
import Switch from "@material-ui/core/Switch";
import FormControlLabel from "@material-ui/core/FormControlLabel";

export default class EndpointBrowseComponent extends Component {

	constructor(props){
		super(props);
		this.state={
			route: "",
			directoryPath : getPathFromMemory(props.endpoint),
			ids: getIdsFromEndpoint(props.endpoint),
			openShare: false,
			shareUrl: "",
			openAFolder: false,
			addFolderName: "",
			searchText: "",
			ignoreCase : false,
			regex : false
		};

		this.getFilesFromBackend = this.getFilesFromBackend.bind(this);
		this.fileNodeDoubleClicked = this.fileNodeDoubleClicked.bind(this);
		this.getFilesFromBackendWithPath = this.getFilesFromBackendWithPath.bind(this);
		this.breadcrumbClicked = this.breadcrumbClicked.bind(this);
		this.toggleSelection = this.toggleSelection.bind(this);
		this.toggleSelectionInGroup = this.toggleSelectionInGroup.bind(this);
		this.multiSelectTo = this.multiSelectTo.bind(this);
		this.onWindowTouchEnd = this.onWindowTouchEnd.bind(this);
		this.onWindowKeyDown = this.onWindowKeyDown.bind(this);
		this.onWindowClick = this.onWindowClick.bind(this);
		this.fileChangeHandler = this.fileChangeHandler.bind(this);
		this._handleAddFolderTextFieldChange = this._handleAddFolderTextFieldChange.bind(this);
		
		this.filenameAscendingOrderSort = this.filenameAscendingOrderSort.bind(this);
		this.sizeAscendingOrderSort = this.sizeAscendingOrderSort.bind(this);
		this.dateAscendingOrderSort = this.dateAscendingOrderSort.bind(this);
		this.permissionAscendingOrderSort = this.permissionAscendingOrderSort.bind(this);
		this.filenameDescendingOrderSort = this.filenameDescendingOrderSort.bind(this);
		this.sizeDescendingOrderSort = this.sizeDescendingOrderSort.bind(this);
		this.dateDescendingOrderSort = this.dateDescendingOrderSort.bind(this);
		this.permissionDescendingOrderSort = this.permissionDescendingOrderSort.bind(this);
		
		this.sortBy = this.sortBy.bind(this);

		if(this.state.directoryPath.length === 0)
			this.getFilesFromBackend(props.endpoint);
	}

	componentDidMount() {
	    window.addEventListener('click', this.onWindowClick);
	    window.addEventListener('keydown', this.onWindowKeyDown);
		window.addEventListener('touchend', this.onWindowTouchEnd);
		eventEmitter.on("fileChange", this.fileChangeHandler);
		this.timestamp = Date.now();
	}

	fileChangeHandler(){
	    this.forceUpdate();
	}

	componentWillUnmount() {
	    window.removeEventListener('click', this.onWindowClick);
	    window.removeEventListener('keydown', this.onWindowKeyDown);
		window.removeEventListener('touchend', this.onWindowTouchEnd);
		unselectAll();
	}
	

  	toggleSelection = (task) => {
  		const {endpoint} = this.props;
	    const selectedTaskIds = getSelectedTasksFromSide(endpoint);
	    const wasSelected = selectedTaskIds.includes(task);
	    const newTasks = (() => {
	      // Task was not previously selected
	      // now will be the only selected item
	      if (!wasSelected) {
	        return [task];
	      }
	      // Task was part of a selected group
	      // will now become the only selected item
	      if (selectedTaskIds.length > 1) {
	        return [task];
	      }
	      // task was previously selected but not in a group
	      // we will now clear the selection
	      return [];
	    })();
	    setSelectedTasksForSide(newTasks, endpoint);
  	};

  	toggleSelectionInGroup = (task) => {
  		const {endpoint} = this.props;
	    const selectedTasks = getSelectedTasksFromSide(endpoint);
	    const index = selectedTasks.indexOf(task);
	    // if not selected - add it to the selected items
	    if (index === -1) {
	      setSelectedTasksForSide([...selectedTasks, task], endpoint);
	      return;
	    }
	    // it was previously selected and now needs to be removed from the group
	    const shallow = [...selectedTasks];
	    shallow.splice(index, 1);

	    setSelectedTasksForSide(shallow, endpoint);
	};
	// This behaviour matches the MacOSX finder selection
	multiSelectTo = (newTask) => {
		const {endpoint} = this.props;
	    const updated = multiSelect(
	      getEntities()[endpoint.side],
	      getSelectedTasksFromSide(endpoint),
	      newTask
	 	);
	    if (updated == null) {
	      return;
	    }
	    setSelectedTasksForSide(updated, endpoint);
	};

	onWindowKeyDown = (event) => {
	    if (event.defaultPrevented) {
	      return;
	    }

	    if (event.key === 'Escape') {
	      unselectAll();
	    }
	};

	onWindowClick = (event) => {
	    if (event.defaultPrevented) {
	      return;
	    }
	};

	onWindowTouchEnd = (event) => {
	    if (event.defaultPrevented) {
	      	return;
	    }
	    if(Date.now() - this.timestamp < 200)
			unselectAll();
	    this.timestamp = Date.now();
	};
	
	fileNodeDoubleClicked(filename, id){
		this.props.setLoading(true);
		this.getFilesFromBackendWithPath(this.props.endpoint, [...this.state.directoryPath, filename], [...this.state.ids, id]);
		unselectAll();
	}

	breadcrumbClicked(index){
		this.props.setLoading(true);
		let tempDirectoryPath = this.state.directoryPath;
		let tempIds = this.state.ids;
		tempDirectoryPath.length = index;
		tempIds.length = index + 1;
		this.setState({
			directoryPath : tempDirectoryPath,
			ids : tempIds
		});
		this.getFilesFromBackendWithPath(this.props.endpoint, this.state.directoryPath, this.state.ids);
	}

	getFilesFromBackend(endpoint){
		this.getFilesFromBackendWithPath(endpoint, [], [null]);
	}

	filenameAscendingOrderSort = (files) => {
		return files.sort((a, b) => { 
			if(a.dir && !b.dir){
				return -1;
			}else if(!a.dir && b.dir){
				return 1;
			}else{
				return a.name.localeCompare(b.name);
			}
		});
	}

	filenameDescendingOrderSort = (files) => {
		return files.sort((a, b) => { 
			if(a.dir && !b.dir){
				return -1;
			}else if(!a.dir && b.dir){
				return 1;
			}else{
				return b.name.localeCompare(a.name);
			}
		});
	}

	dateAscendingOrderSort(files){
		return files.sort((a, b) => { 
			return a.time - b.time;
		});
	}

	dateDescendingOrderSort(files){
		return files.sort((a, b) => { 
			return b.time - a.time;
		});
	}

	sizeAscendingOrderSort (files){
		return files.sort((a, b) => { 
			return a.size - b.size;
		});
	}

	sizeDescendingOrderSort(files){
		return files.sort((a, b) => { 
			return b.size - a.size;
		});
	}

	permissionAscendingOrderSort = (files) => {
		return files.sort((a, b) => { 
			if(a.perm && b.perm)
				return a.perm.localeCompare(b.perm);
			return 0;
		});
	}

	permissionDescendingOrderSort = (files) => {
		return files.sort((a, b) => { 
			if(a.perm && b.perm)
				return b.perm.localeCompare(a.perm);
			return 0;
		});
	}

	sortBy = (sortingFunc) => {
		const {endpoint} = this.props;
		const {directoryPath, ids} = this.state;
		let files = getFilesFromMemory(endpoint);
		let sortedfiles = sortingFunc(files);
		setFilesWithPathListAndId(sortedfiles, directoryPath, ids, endpoint);
		this.setState({directoryPath: directoryPath, ids: ids});
	}

	getFilesFromBackendWithPath(endpoint, path, id){
		var uri = endpoint.uri;
		const {setLoading} = this.props;
		setLoading(true);
		uri = makeFileNameFromPath(uri, path, "");

		listFiles(uri, endpoint, id[id.length-1], (data) =>{
			let sortedfiles = this.filenameAscendingOrderSort(data.files);
			setFilesWithPathListAndId(sortedfiles, path, id, endpoint);
			this.setState({directoryPath: path, ids: id});
			setLoading(false);
		}, (error) =>{
			if(error === "500"){
				this._handleError("Login Failed. Re-directing to OAuth page");
				setLoading(false);
				emptyFileNodesData(endpoint);
				
				let type = getName(endpoint);
				let cred = endpoint.credential;
				let savedCreds = cookies.get(type);

				// Delete the creds from the cookie if they exist
				if(savedCreds !== undefined){
					let parsedCredsArr = JSON.parse();
					let filteredCredsArr = parsedCredsArr.filter((curObj)=>{
																	return curObj.name !== cred.name;
															});
					if(filteredCredsArr.length === 0){
						cookies.remove(type);
					}
					else{
						cookies.set(type, JSON.stringify(filteredCredsArr));
					}	
				}

				unselectAll();
				this.props.back();
				
				setTimeout(()=> {
					if(getType(endpoint) === DROPBOX_TYPE)
						openDropboxOAuth();
					else if(getType(endpoint) === GOOGLEDRIVE_TYPE)
						openGoogleDriveOAuth();
					else if(getType(endpoint) === BOX_TYPE)
						openBoxOAuth();				
					}, 3000);
			}
		});
	};

	handleClickOpen = (url) => {
		this.setState({ openShare: true, shareUrl: url });
	};

	handleClickOpenAddFolder = () => {
		this.setState({ openAFolder: true });
	};

	handleClose = () => {
		this.setState({ openShare: false, openAFolder: false });
	};

	handleCloseWithFolderAdded = () =>{
		const {endpoint, setLoading} = this.props;
		const {directoryPath, addFolderName, ids} = this.state;
		this.setState({ openShare: false, openAFolder: false });
		let dirName = makeFileNameFromPath(endpoint.uri,directoryPath, addFolderName);
		const dirType = getType(endpoint);
		//make api call
		mkdir(dirName,dirType, endpoint, (response) => {
			setLoading(true);
			this.getFilesFromBackendWithPath(endpoint, directoryPath, ids);
		}, (error) => {
			this._handleError(error);
		})
	}

	_handleError = (error) =>{
		eventEmitter.emit("errorOccured", error);
	}

	_handleConfirmation = (query) => {
		return window.confirm(query);
	}

	_handleAddFolderTextFieldChange = (e) => {
		this.setState({
				addFolderName: e.target.value
		});
	}

	handleCloseWithFileDeleted = (files) => {
		const {endpoint, setLoading} = this.props;
		const {directoryPath, ids} = this.state;
		const len = files.length;
		var i = 0;
		if(this._handleConfirmation("Are you sure you want to delete" + files.reduce((a, v) => a+"\n"+v.name, ""))){
			setLoading(true);
			files.map((file) => {
				const fileName = makeFileNameFromPath(endpoint.uri, directoryPath, file.name);
				deleteCall( fileName, endpoint,  file.id, (response) => {
					i++;
					if(i === len){
						this.getFilesFromBackendWithPath(endpoint, directoryPath, ids);
					}
				}, (error) => {
					this._handleError(error);
				});
				return null;
			});

			unselectAll();
		}
	}

	render(){
		const {endpoint, back, setLoading, getLoading, displayStyle} = this.props;
		const {directoryPath, searchText} = this.state;

		let updateCompactViewPreference = name => event => {
			this.setState({ [name]: event.target.checked });
			let compactViewEnabled = event.target.checked;
			let email = store.getState().email;
			updateViewPreference(email, compactViewEnabled,
				(success) => {
					console.log("Compact View Preference Switched Successfully", success);
					store.dispatch(compactViewPreference(compactViewEnabled));
				},
				(error) => { console.log("ERROR in updation" + error) }
			);
		};

		const list = getFilesFromMemory(endpoint) || [];
		let displayList = Object.keys(list);


		if(searchText.length > 0){
			if(this.state.regex){
				var flags = this.state.ignoreCase? "i" : "";
				try{
					var regex = new RegExp(searchText, flags);
					displayList = Object.keys(list).filter(key => regex.test(list[key].name));
				} catch {
					console.log("Invalid regex")
				}	
			}
			else{
				if(this.state.ignoreCase){
					var keyword = searchText.toLowerCase()
					displayList = Object.keys(list).filter(key => list[key].name.toLowerCase().includes(keyword));
				}
				else
					displayList = Object.keys(list).filter(key => list[key].name.includes(searchText));
			}
		} 
		
		const iconStyle = {fontSize: "15px", width: "100%"};
		const buttonStyle = {flexGrow: 1, padding: "5px"};
		const buttonGroupStyle = {display: "flex", flexDirection: "row", flexGrow: 2};

		const selectedTasks = getSelectedTasksFromSide(endpoint);
		const loading = getLoading();
		const tooltip = (name) => (
		  <Tooltip id="tooltip">
		  	{name}
		  </Tooltip>
		);

		return (
		<Box style={{/*display: "flex", flexDirection: "column",*/  minHeight: "100%", maxHeight: "400px", }}>

	        <Dialog
	          open={this.state.openShare}
	          onClose={this.handleClose}
						aria-labelledby="form-dialog-title"
	        >
	          <DialogTitle id="form-dialog-title">Share</DialogTitle>
	          <DialogContent style={{width:"100%"}}>
	            <DialogContentText>
	              Share this URL to allow others access to the selected file:
	            </DialogContentText>
	            <div style={{width:"96%", float:"left"}}><TextField
								autoFocus
								id="name"
								disabled
								value={this.state.shareUrl}
								fullWidth
	            ></TextField></div>
							<CopyToClipboard text = {this.state.shareUrl} style={{float:"right", width:"3%"}}>
							<svg width="24" height="24" viewBox="0 0 24 24">
								<path d="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12V1zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2zm0 16H8V7h11v14z"/>
							</svg>								
							</CopyToClipboard>
	          </DialogContent>
	          <DialogActions>
	            <Button onClick={this.handleClose} color="primary">
	              Close
	            </Button>
	          </DialogActions>
	        </Dialog>

	        <Dialog
	          open={this.state.openAFolder}
	          onClose={this.handleClose}
	          aria-labelledby="form-dialog-title"
	        >
	          <DialogTitle id="form-dialog-title">Create directory</DialogTitle>
	          <DialogContent>
	            <TextField
	              autoFocus
	              id={endpoint.side+"MkdirName"}
	              label="Directory Name"
	              onChange={this._handleAddFolderTextFieldChange}
	              fullWidth
	            />
	          </DialogContent>
	          <DialogActions>
	            <Button id={endpoint.side+"MkdirSubmit"} onClick={this.handleCloseWithFolderAdded} color="primary">
	              Create
	            </Button>
	          </DialogActions>
	        </Dialog>
			<div style={{display: "flex",alighSelf: "stretch", height: "60px", backgroundColor: "#d9edf7", width: "100%", overflowX: "scroll", overflowY: "hidden"}}>
				<Breadcrumb style={{float: "left", backgroundColor: "#d9edf7", whiteSpace:"nowrap"}}>
				  <Breadcrumb.Item key={endpoint.uri} style={{display: "inline-block"}}><Button style={{padding: "0px", margin: "0px"}} onClick={() => this.breadcrumbClicked(0)}>{endpoint.uri}</Button></Breadcrumb.Item>
				  {directoryPath.map((item, index) => <Breadcrumb.Item key={item+index} style={{display: "inline-block"}}><Button style={{padding: "0px", margin: "0px"}} onClick={() => this.breadcrumbClicked(index+1)}>{item}</Button></Breadcrumb.Item>)}
				</Breadcrumb>
			</div>
			
			<div style={{alignSelf: "stretch", display: "flex", flexDirection: "row", alignItems: "center", height: "40px", padding: "10px", backgroundColor: "#d9edf7"}}>

				<ButtonGroup style={buttonGroupStyle}>

					{ new Set([SFTP_TYPE, FTP_TYPE]).has(getType(endpoint)) &&
						<OverlayTrigger placement="top" overlay={tooltip("Console")}>
							<Button onClick={() => {}} style={buttonStyle}>
								<Code style={iconStyle} />
							</Button>
						</OverlayTrigger>
					}

					<OverlayTrigger placement="top" overlay={tooltip("Download")}>
						<Button id={endpoint.side + "DownloadButton"} disabled={getSelectedTasksFromSide(endpoint).length !== 1 || getSelectedTasksFromSide(endpoint)[0].dir}
						onClick={() => {
							const downloadUrl = makeFileNameFromPath(endpoint.uri,directoryPath, getSelectedTasksFromSide(endpoint)[0].name);
								const taskList = getSelectedTasksFromSide(endpoint);
								if(getType(endpoint) === SFTP_TYPE){
									getDownload(downloadUrl, endpoint.credential, taskList);
								}
								else if(getType(endpoint) === HTTP_TYPE){
									window.open(downloadUrl);
								}
								else{
									download(downloadUrl, endpoint.credential, taskList[0].id)
							}
						}}
						style={buttonStyle}>
							<DownloadButton style={iconStyle}/>
						</Button>
					</OverlayTrigger>
					
					<OverlayTrigger placement="top" overlay={tooltip("Upload")}>
						<Button id={endpoint.side + "UploadButton"} >
							<UploaderWrapper endpoint={endpoint} directoryPath={directoryPath} lastestId={this.state.ids[this.state.ids.length-1]}/>
						</Button>
					</OverlayTrigger>
					
					<OverlayTrigger placement="top"  overlay={tooltip("Share")}>
						<Button id={endpoint.size + "ShareButton"} disabled = {getSelectedTasksFromSide(endpoint).length !== 1 || getSelectedTasksFromSide(endpoint)[0].dir
						|| !(getType(endpoint) === GOOGLEDRIVE_TYPE || getType(endpoint) === DROPBOX_TYPE)} style={buttonStyle} onClick={() => {
							const downloadUrl = makeFileNameFromPath(endpoint.uri,directoryPath, getSelectedTasksFromSide(endpoint)[0].name);
							const taskList = getSelectedTasksFromSide(endpoint);
							getSharableLink(downloadUrl, endpoint.credential, taskList[0].id)
							.then(response => {
								if(response !== ""){
									this.handleClickOpen(response);
								}
								else{
									eventEmitter.emit("errorOccured", "Error encountered while generating link");
								}	
							})
				  		}}>
				  			<LinkButton style={iconStyle}/>
				  		</Button>
					</OverlayTrigger>

					<OverlayTrigger placement="top" overlay={tooltip("New Folder")}>
						<Button id={endpoint.side + "MkdirButton"} style={buttonStyle} onClick={() => {
							this.handleClickOpenAddFolder()
						}}>
							<NewFolderIcon style={iconStyle}/>
						</Button>
					</OverlayTrigger>
					
					<OverlayTrigger placement="top" overlay={tooltip("Delete")}>
						<Button id={endpoint.side + "DeleteButton"} disabled={getSelectedTasksFromSide(endpoint).length < 1} onClick={() => {
							this.handleCloseWithFileDeleted(getSelectedTasksFromSide(endpoint));
						}}
						style={buttonStyle}><DeleteIcon style={iconStyle}/></Button>
					</OverlayTrigger>


					<OverlayTrigger placement="top" overlay={tooltip("Refresh")}>
				  		<Button id={endpoint.side + "RefreshButton"} style={buttonStyle}  onClick={() => {
				  			setLoading(true);
				  			this.getFilesFromBackendWithPath(endpoint, directoryPath, this.state.ids);
				  		}}>
				  			<RefreshButton style={iconStyle}/>
				  		</Button>
					</OverlayTrigger>

					<OverlayTrigger placement="top" overlay={tooltip("Log out")}>
				  		<Button id={endpoint.side + "LogoutButton"} bsStyle="primary" style={buttonStyle} onClick={() =>
				  		{
				  			emptyFileNodesData(endpoint);
				  			unselectAll();
				  			back();
				  		}}
				  			><LogoutButton style={iconStyle}/></Button>
					</OverlayTrigger>
				</ButtonGroup>
			</div>

			{/*<div style={{ alignSelf: "stretch", display: "flex", flexDirection: "row", alignItems: "center", height: "40px", padding: "10px", backgroundColor: "#d9edf7"}}>*/}
				<FormGroup style={{margin: "0 1%", display: "flex", alignItems: "center"}} >
					<FormControl margin={"dense"} fullWidth={true} >
						<TextField
							variant={"outlined"}
							id={endpoint.side + "Search"}
							margin={"dense"}
							placeholder={"Search"}
							onChange={(event) => {
								this.setState({searchText: event.target.value})
							}}
						/>
					</FormControl>

					{/*Remember to put popover hover after*/}
					<FormControl margin={"dense"} style={{marginLeft: "85%"}}>
						<ButtonGroup fullWidth={true}>
							<Button id={endpoint.side + "IgnoreCase"} style={{color: this.state.ignoreCase ? "white" : "black", backgroundColor: this.state.ignoreCase ? "#337AB6" : "white" ,
								border: "1px solid #ccc", textTransform: "capitalize", fontFamily : "monospace", fontSize : "10px", width: "5%"}}
									onClick={() => {
										this.setState({ignoreCase : !this.state.ignoreCase})
									}
									}>Aa</Button>
							<Button id={endpoint.side + "Regex"} style={{color: this.state.regex ? "white" : "black", backgroundColor: this.state.regex ? "#337AB6" : "white" ,
								border: "1px solid #ccc", fontSize : "10px"}}
									onClick={() => {
										this.setState({regex : !this.state.regex})
									}}><b>*.</b></Button>
						</ButtonGroup>
					</FormControl>

					{/*<OverlayTrigger placement="top" overlay={tooltip("Ignore Case")}>*/}
					{/*	*/}
					{/*</OverlayTrigger>*/}
					{/*<OverlayTrigger placement="top" overlay={tooltip("Regular Expression")}>*/}
					{/*	*/}
					{/*</OverlayTrigger>*/}



				</FormGroup>

			{/*</div>*/}

			{/*<div>*/}
			{/*	<FormControlLabel*/}
			{/*		className={"compactSwitch"}*/}
			{/*		control={*/}
			{/*			<Switch*/}
			{/*				color="default"*/}
			{/*				style={{colorPrimary: "white", colorSecondary: "white"}}*/}
			{/*				checked={this.state.compact}*/}
			{/*				onChange={updateCompactViewPreference('compact')}*/}
			{/*				value="compact"*/}
			{/*			/>*/}
			{/*		}*/}
			{/*		label={<Typography>Compact</Typography>}*/}
			{/*	/>*/}
			{/*</div>*/}
			<Droppable droppableId={endpoint.side} >

				{(provided, snapshot) => (
					<div
						ref={provided.innerRef}
						{...provided.droppableProps}
						style={{  overflowY: 'scroll', width: "100%", marginTop: "0px", height: "320px"}}
					>
						{!loading && Object.keys(list).length === 0 &&
							<h2 style={{ textAlign: 'center' }}>
								This directory is empty.
							</h2>
						}

						{loading && Object.keys(list).length === 0 &&
							<h2 style={{ textAlign: 'center' }}>
								Loading...
							</h2>
						}

						{!loading && displayList.length === 0 && Object.keys(list).length > 0 &&
							<h2>
								No Search Result
							</h2>
						}

						{displayStyle === "compact" && !loading && displayList.length !== 0 &&
							<CompactFileNodeWrapper 
								sortFunctions = {[{"Asc": this.filenameAscendingOrderSort, "Desc" : this.filenameDescendingOrderSort}, 
												  {"Asc": this.dateAscendingOrderSort, "Desc":this.dateDescendingOrderSort},
												  {"Asc": this.permissionAscendingOrderSort, "Desc":this.permissionDescendingOrderSort},
												  {"Asc": this.sizeAscendingOrderSort, "Desc":this.sizeDescendingOrderSort}]}
								sortBy = {this.sortBy}
								list={list} 
								displayList={displayList} 
								selectedTasks={selectedTasks} 
								endpoint={endpoint} 
								draggingTask={draggingTask}
								toggleSelection={this.toggleSelection}
								onClick={this.fileNodeClicked}
								onDoubleClick={this.fileNodeDoubleClicked}
								toggleSelectionInGroup={this.toggleSelectionInGroup}
					            multiSelectTo={this.multiSelectTo}
							/>
						}


						{displayStyle === "comfort" && displayList.map((fileId, index) => {
							const file = list[fileId];
							const isSelected = Boolean(
			                  selectedTasks.indexOf(file)!==-1,
			                );
			                const isGhosting =
			                  isSelected &&
			                  Boolean(draggingTask) &&
			                  draggingTask.name !== file.name;

							  return(
								<FileNode
									key={fileId}
									index={index}
									file={file}
									id={fileId}
									fileId={fileId}
									selectionCount={selectedTasks.length}
									onClick={this.fileNodeClicked}
									onDoubleClick={this.fileNodeDoubleClicked}
									side={endpoint.side}
									isSelected={isSelected}
									endpoint={endpoint}
									isGhosting={isGhosting}
									toggleSelection={this.toggleSelection}
									toggleSelectionInGroup={this.toggleSelectionInGroup}
									multiSelectTo={this.multiSelectTo}
							/>);
						})}
						{provided.placeHolder}
					</div>
				)}
			</Droppable>
			<ProgressUpdateComponent />
		</Box>);
	}
}


