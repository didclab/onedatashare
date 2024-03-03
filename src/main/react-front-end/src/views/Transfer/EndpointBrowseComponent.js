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
import BrowseButton from "./EndpointBrowseButton";

import { Droppable } from 'react-beautiful-dnd';

import NewFolderIcon from "@material-ui/icons/CreateNewFolder";
import DeleteIcon from "@material-ui/icons/DeleteForever";
import LinkButton from "@material-ui/icons/Link";
import LogoutButton from "@material-ui/icons/ExitToApp";
import RefreshButton from "@material-ui/icons/Refresh";
import {Button, Grid, Box, Breadcrumbs, Link} from '@material-ui/core';

import TextField from '@material-ui/core/TextField';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import Typography from "@material-ui/core/Typography";


import React, { Component } from 'react';

import {
	listFiles,
	mkdir,
	deleteCall,
	getSharableLink,
	OAuthFunctions
} from "../../APICalls/EndpointAPICalls";


import { getFilesFromMemory, getIdsFromEndpoint, getPathFromMemory, 
		emptyFileNodesData, getEntities, setSelectedTasksForSide,  getSelectedTasksFromSide, 
		unselectAll, makeFileNameFromPath, draggingTask, setFilesWithPathListAndId, } from "./initialize_dnd";

import {eventEmitter, store} from "../../App";

import { cookies } from "../../model/reducers";
import { getName, getType, showType, isOAuth} from "../../constants";
import { CopyToClipboard } from 'react-copy-to-clipboard';
import {compactViewPreference} from "../../model/actions";
import Switch from "@material-ui/core/Switch";
import FormControlLabel from "@material-ui/core/FormControlLabel";
import VFSBrowseComponent from './VFSBrowseComponent'


//PROGRESS: S3 browse functions are all finished (listing, deleting, going into other directories, refresh, logout)
// FTP can only list root directory (check listfiles() comments on EndpointAPICalls.js for more details on FTP problems)
// SFTP (check listfiles() comments on EndpointAPICalls.js for more details on FTP problems) and HTTP currently not functional

//for notes on listing: line 336

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
			regex : false,
			compact: store.getState().compactViewEnabled,
			oauth: false,
			name: ""
		};

		this.getFilesFromBackend = this.getFilesFromBackend.bind(this);
		this.fileNodeDoubleClicked = this.fileNodeDoubleClicked.bind(this);
		this.getFilesFromBackendWithPath = this.getFilesFromBackendWithPath.bind(this);
		this.httpPathHandler = this.httpPathHandler.bind(this);
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

		this.handleClickOpen = this.handleClickOpen.bind(this);
		this.handleClickOpenAddFolder = this.handleClickOpenAddFolder.bind(this);
		this.handleCloseWithFileDeleted = this.handleCloseWithFileDeleted.bind(this);

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

	// Fetch HTTP directory
	httpPathHandler(endpoint, path, id) {
		const {setLoading} = this.props;
		setLoading(true);
		var uri = endpoint.uri;
		uri = makeFileNameFromPath(uri, path, "");
		// console.log(path)
		let dirPath = "/" + (path[0]? path[0] : "")
		listFiles(uri, endpoint, dirPath , (data) =>{
			setLoading(false);
			let sortedfiles = this.filenameAscendingOrderSort(data.files);
			setFilesWithPathListAndId(sortedfiles, path, id, endpoint);
			this.setState({directoryPath: path, ids: id});
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
					const type = getType(endpoint)
					if(isOAuth[type] && type !== showType.gsiftp){
						OAuthFunctions[type]();
					}
					// if(getType(endpoint) === DROPBOX_TYPE)
					// 	openDropboxOAuth();
					// else if(getType(endpoint) === GOOGLEDRIVE_TYPE)
					// 	openGoogleDriveOAuth();
					// else if(getType(endpoint) === BOX_TYPE)
					// 	openBoxOAuth();
					},
					3000);
			}
		});
	}


	// Fully functional with S3
	// FTP: Can browse files from the root directory, but unable to go into other directories
	// Listingi does not yet work for SFTP and HTTP
	getFilesFromBackendWithPath(endpoint, path, id){
		if (endpoint?.uri === showType.vfs) {
			this.props.setLoading(false);
			return
		}
		var uri = endpoint.uri;
		var uriType = uri.split(":")
		if (uriType[0] === "http") {
			this.httpPathHandler(endpoint, path, id)
		}
		else {
			const {setLoading} = this.props;
			setLoading(true);
			uri = makeFileNameFromPath(uri, path, "");
			console.log(endpoint)
			listFiles(uri, endpoint, id[id.length-1], (data) =>{
				setLoading(false);
				let sortedfiles = this.filenameAscendingOrderSort(data.files);
				setFilesWithPathListAndId(sortedfiles, path, id, endpoint);
				this.setState({directoryPath: path, ids: id});
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
						const type = getType(endpoint)
						if(isOAuth[type] && type !== showType.gsiftp){
							OAuthFunctions[type]();
						}
						// if(getType(endpoint) === DROPBOX_TYPE)
						// 	openDropboxOAuth();
						// else if(getType(endpoint) === GOOGLEDRIVE_TYPE)
						// 	openGoogleDriveOAuth();
						// else if(getType(endpoint) === BOX_TYPE)
						// 	openBoxOAuth();
						},
						3000);
				}
			});
		}
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
		//make api call
		mkdir(dirName, endpoint, (response) => {
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
				deleteCall( fileName, endpoint, file.id, (response) => {
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

	updateCompactViewPreference = name => event => {
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

	render(){
		const {endpoint, back, setLoading, getLoading, /*displayStyle*/} = this.props;
		const {directoryPath, searchText, compact} = this.state;
		const displayStyle = compact ? "compact" : "comfort";
		const type = getType(endpoint);

		// let updateCompactViewPreference = name => event => {
		// 	this.setState({ [name]: event.target.checked });
		// 	let compactViewEnabled = event.target.checked;
		// 	let email = store.getState().email;
		// 	updateViewPreference(email, compactViewEnabled,
		// 		(success) => {
		// 			console.log("Compact View Preference Switched Successfully", success);
		// 			store.dispatch(compactViewPreference(compactViewEnabled));
		// 		},
		// 		(error) => { console.log("ERROR in updation" + error) }
		// 	);
		// };

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
		
		const iconStyle = {fontSize: "20px"};
		const buttonStyle = {flexGrow: 1, padding: "5px"};

		const selectedTasks = getSelectedTasksFromSide(endpoint);
		const loading = getLoading();


		return (
		<div>
		{
		<Box>
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
			<div style={{display: "flex",alignSelf: "stretch", height: "60px", backgroundColor: "#d9edf7", width: "100%", overflowX: "scroll", overflowY: "hidden"}}>
				<Breadcrumbs style={{whiteSpace:"nowrap", padding: "3%", flexGrow: "1"}}>
					<Link key={endpoint.uri} style={{display: "inline-block", fontWeight: "bold", color: "black", fontSize: "16px"}} onClick={() => this.breadcrumbClicked(0)}>
						{/*{endpoint.oauth ? endpoint.uri + endpoint.credential.name.split(" ")[1]: endpoint.uri}*/}
						{endpoint.uri}
					</Link>
					{directoryPath.map((item, index) => <Link key={item+index} style={{display: "inline-block", fontWeight: "bold", color: "black", fontSize: "11px"}} onClick={() => this.breadcrumbClicked(index+1)}>{item}</Link>)}
				</Breadcrumbs>
				<div>
					<FormControlLabel
						className={"compactSwitch"}
						control={
							<Switch
								color="default"
								style={{colorPrimary: "white", colorSecondary: "white"}}
								checked={this.state.compact}
								onChange={this.updateCompactViewPreference('compact')}
								value="compact"
							/>
						}
						label={<Typography>Compact</Typography>}
					/>
				</div>
			</div>
			{/*alignSelf: "stretch", display: "flex", flexDirection: "row", alignItems: "center", height: "40px",*/}
			<div style={{  backgroundColor: "#d9edf7"}}>

				<Grid container direction={"row"} spacing={2} justifyContent={"space-between"} alignItems={"center"} style={{width: "99%", padding: "0"}}>

					<BrowseButton
						id={endpoint.size + "ShareButton"} disabled = {getSelectedTasksFromSide(endpoint).length !== 1 || getSelectedTasksFromSide(endpoint)[0].dir
					 || /*!(getType(endpoint) === GOOGLEDRIVE_TYPE || getType(endpoint) === DROPBOX_TYPE || getType(endpoint) === BOX_TYPE)*/!(isOAuth[type] && type !== showType.gsiftp)} style={buttonStyle} click={() => {
						const downloadUrl = makeFileNameFromPath(endpoint.uri,directoryPath, getSelectedTasksFromSide(endpoint)[0].name);
						const taskList = getSelectedTasksFromSide(endpoint);
						getSharableLink(downloadUrl, endpoint.credential, taskList[0].id)
							.then(response => {
								if(response !== ""){
									this.handleClickOpen(response.url);
								}
								else{
									eventEmitter.emit("errorOccured", "Error encountered while generating link");
								}
							})
					}}
						label={"Share"}
						buttonIcon={<LinkButton style={iconStyle}/>}
					/>


					{/*s3 does not have functionality for mkdir, so button is removed when browsing s3 files*/}
					{endpoint.credential.type !== showType.s3 && showType.vfs !== type && <BrowseButton id={endpoint.side + "MkdirButton"} style={buttonStyle} click={() => {
						this.handleClickOpenAddFolder()
					}}
								   label={"New Folder"}
								   buttonIcon={<NewFolderIcon style={iconStyle}/>}
					/>}

					<BrowseButton id={endpoint.side + "DeleteButton"} disabled={getSelectedTasksFromSide(endpoint).length < 1 || type === showType.vfs} click={() => {
						this.handleCloseWithFileDeleted(getSelectedTasksFromSide(endpoint));
					}}
								  style={buttonStyle}
								  label={"Delete"}
								  buttonIcon={<DeleteIcon style={iconStyle}/>}
					/>

					<BrowseButton d={endpoint.side + "RefreshButton"} disabled={type === showType.vfs} style={buttonStyle}
					click={() => {
						setLoading(true);
						this.getFilesFromBackendWithPath(endpoint, directoryPath, this.state.ids);
					}}
								  label={"Refresh"}
								  buttonIcon={<RefreshButton style={iconStyle}/>}
					/>


					<BrowseButton id={endpoint.side + "LogoutButton"} style={buttonStyle} click={() =>
					{
						emptyFileNodesData(endpoint);
						unselectAll();
						back();
					}}
								  label={"Log out"}
								  buttonIcon={<LogoutButton style={iconStyle}/>}
								  />


				</Grid>
			</div>

			{/*<div style={{ alignSelf: "stretch", display: "flex", flexDirection: "row", alignItems: "center", height: "40px", padding: "10px", backgroundColor: "#d9edf7"}}>*/}
			<div style={{backgroundColor: "#d9edf7", paddingBottom:"5px"}}>
				<Grid container direction={"row"} alignItems={"center"} style={{margin: "0 2%", padding: "0", display: "flex", alignItems: "center", width: "95%"}} >
					<Grid item md={10} xs={12}>
						<TextField
							fullWidth
							disabled={type === showType.vfs}
							variant={"outlined"}
							id={endpoint.side + "Search"}
							margin={"dense"}
							placeholder={"Search"}
							onChange={(event) => {
								this.setState({searchText: event.target.value})
							}}
							InputProps={{
								className: "searchTextfield"
							}}
						/>
					</Grid>

					{/*Remember to put popover hover after*/}
					<Grid item md={2} xs={12} >

						<BrowseButton
							buttongroup={true}
							disabled={type === showType.vfs}
							id={[endpoint.side + "IgnoreCase", endpoint.side + "Regex"]}
							style={[
								{color: this.state.ignoreCase ? "white" : "black", backgroundColor: this.state.ignoreCase ? "#337AB6" : "white" ,
									border: "1px solid #ccc", textTransform: "capitalize", fontFamily : "monospace", fontSize : "10px",marginLeft: "8px",borderRadius: "0px",
									marginTop: "6px",
									height: "32px",},
								{color: this.state.regex ? "white" : "black", backgroundColor: this.state.regex ? "#337AB6" : "white" ,
									border: "1px solid #ccc", fontSize : "10px",borderRadius: "0px",
									marginTop: "6px",
									height: "32px",
									}
							]}
							click={[
								() => {
									this.setState({ignoreCase : !this.state.ignoreCase})
								},
								() => {
									this.setState({regex : !this.state.regex})
								}
							]}
							label={[
								"Ignore Case", "Regular Expression"
							]}
							buttonIcon={["Aa", "*."]}
						/>


					</Grid>





				</Grid>
			</div>

			{
				type === showType.vfs && 
				<VFSBrowseComponent endpoint={endpoint}/>
			}

			{type !== showType.vfs &&
			<Droppable droppableId={endpoint.side} >

				{(provided, snapshot) => (
					<div
						ref={provided.innerRef}
						{...provided.droppableProps}
						style={{  overflowY: 'scroll', width: "100%", marginTop: "0px", height: "440px",paddingLeft:"10px",paddingRight:"10px"}}
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
			}
		</Box>}
		</div>
		);
	}
}


