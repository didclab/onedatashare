import React, { Component } from 'react';
import FineUploaderTraditional from 'fine-uploader-wrappers';
import {makeFileNameFromPath, getMapFromEndpoint} from "./initialize_dnd";

import FileInput from 'react-fine-uploader/file-input';
import UploadButton from "@material-ui/icons/CloudUpload";
import {eventEmitter} from "../../App";
export default class UploaderWrapper extends Component {

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
						directoryPath: encodeURI(makeFileNameFromPath(endpoint.uri,directoryPath,'')),
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
					},
					onStatusChange: function(id, old_status, new_status){
						if(new_status === "submitted"){
							eventEmitter.emit("messageOccured", "Upload initiated!");
						}
						if(new_status === "upload successful"){
							eventEmitter.emit("messageOccured", "Upload complete!");
						
						}
					}

				}
				// "qqchunksize": 1000000
			}
		})

	return <FileInput uploader={uploader} style={buttonStyle}>
                    <UploadButton style={iconStyle}/>
		</FileInput>
	}
}

  			