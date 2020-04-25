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


import React, { Component } from 'react';
import FineUploaderTraditional from 'fine-uploader-wrappers';
import {makeFileNameFromPath, getMapFromEndpoint} from "./initialize_dnd";

import FileInput from 'react-fine-uploader/file-input';
import UploadButton from "@material-ui/icons/CloudUpload";
import {eventEmitter} from "../../App";
export default class UploaderWrapper extends Component {

	constructor(props){
		super(props);
	}

	shouldComponentUpdate(nextProps, nextState) { 

    	if (nextProps.directoryPath === this.props.directoryPath && 
			nextProps.endpoint === this.props.endpoint
    	) return false;

    	return true;
  	}
render(){
	let {endpoint, directoryPath, lastestId} = this.props;

	const buttonStyle = {flexGrow: 1, padding: "5px"};
	const iconStyle = {fontSize: "15px", width: "100%"};
	const uploader = new FineUploaderTraditional({
			debug: true,
			options: {
				chunking: {
					enabled: true,
					partSize: 1048576,
					concurrent: {
						enabled: false
					},
				},
				request: {
					endpoint: '/api/stork/upload',
					params: {
						directoryPath: encodeURI(makeFileNameFromPath(endpoint.uri, directoryPath,'')),
						credential: JSON.stringify(endpoint.credential),
						id: lastestId,
						map: JSON.stringify(getMapFromEndpoint(endpoint))
					}
				},
				retry: {
					enableAuto: true
				},
				callbacks :{
					onError: function(id, name, errorReason, xhr){
						console.log('error occurred - ' + errorReason);
						eventEmitter.emit("progressUpdateError", name);
					},
					onStatusChange: function(id, old_status, new_status){
						if(new_status === "submitted"){
							eventEmitter.emit("messageOccured", "Upload initiated!");
						}
						if(new_status === "upload successful"){
							eventEmitter.emit("messageOccured", "Upload complete!");
						}
					},
					onProgress: function(id, name, upload, total) {
						//console.log(id, name, upload, total, Math.floor(upload/total*100));
						eventEmitter.emit("progressChange", name, Math.floor(upload/total*100));
					}.bind(this)

				}
				// "qqchunksize": 1000000
			}
		})

	return (
		<FileInput multiple={true} uploader={uploader} style={buttonStyle}>
			<UploadButton style={iconStyle}/>
		</FileInput>
		)
	}
}

  			