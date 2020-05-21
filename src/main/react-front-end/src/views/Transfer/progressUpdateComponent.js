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
import Card from '@material-ui/core/Card';
import Button from '@material-ui/core/Button';
import { eventEmitter, store } from "../../App";
import DialogTitle from '@material-ui/core/DialogTitle';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import CircularProgress from '@material-ui/core/CircularProgress';
import ButtonGroup from '@material-ui/core/ButtonGroup';
import {
	progressFilesUpdate,
} from "../../model/actions";
import IconButton from '@material-ui/core/IconButton';
import CloseIcon from '@material-ui/icons/Close';
import MinimizeIcon from '@material-ui/icons/Minimize';
import CheckBoxOutlineBlankIcon from '@material-ui/icons/CheckBoxOutlineBlank';

export default class ProgressUpdateComponent extends Component {
    constructor(props){
		super(props);
		this.state={
			uploadPercent: 0,
			uplaodFileName: '',
            progressMinimize: false,
            errorUploadFiles: store.getState().progressUpdate.errorFiles,
            progress: true//Object.keys(store.getState().progressUpdate.files).length !== 0,
        }
        this.files = store.getState().progressUpdate.files;
    }

    componentDidMount() {
        eventEmitter.on("progressChange", this.progressChangeHandler); 
        eventEmitter.on("progressUpdateError", this.progressErrorHandler); 
    }
    
    progressChangeHandler = (name, progressPercent) => {
        try {
            var errorFiles = this.state.errorUploadFiles;
            if (errorFiles.length !== 0 && progressPercent !== this.files[name]) {
                const index = errorFiles.indexOf(name);
                if (index > -1) {
                    errorFiles.splice(index, 1);
                }
            }
            this.files[name] = progressPercent;
            store.dispatch(progressFilesUpdate({files: this.files,errorFiles: errorFiles}));
            this.setState({
                uploadPercent: progressPercent,
                uplaodFileName: name,
                progress: true,
                errorUploadFiles: errorFiles
            });
        } catch {
            console.error("error in progress update::progressChangeHandler");
        }
    }

    progressErrorHandler = (name) => {
        try {
            var errorFiles = this.state.errorUploadFiles;
            store.dispatch(progressFilesUpdate({files: this.files,errorFiles: errorFiles}));
            if (errorFiles.indexOf(name) < 0) {
                errorFiles.push(name);
                this.setState({
                    errorUploadFiles: errorFiles,
                });
            }
        } catch {
            console.error("error in progress update::progressChangeHandler");
        }
    }

    handleClose = () => {
        try {
            this.files = {}
            store.dispatch(progressFilesUpdate({files: this.files,errorFiles: []}));
            this.setState({
                progress: false,
                errorUploadFiles: []
            });
        } catch {
            console.error("error in progress update::handleClose");
        }
	};

	handleMinimise = () => {
        try {
            this.setState({ 
                progressMinimize: !this.state.progressMinimize
            });
        } catch {
            console.error("error in progress update::handleMinimise");
        }	
    }

    render(){
        let heightSize = this.state.progressMinimize ? '' : Object.keys(this.files).length === 0 ? '': '15em';
        return (
            <React.Fragment>
                {this.state.progress ? 
                ( 
                <Card raised={true} style={{overflow: '', position: 'absolute', bottom: '5%', right: '2%', float: 'right', width: '17em', height: heightSize, zIndex: '2'}}>
                    <DialogTitle id="form-dialog-title" style={{heigth: '25%', backgroundColor: '#337ab7', color: '#fff', paddingLeft: '1em', paddingRight: '1em', textAlign: 'center'}}>
                        PROGRESS UPDATE
                        <IconButton  onClick={this.handleMinimise} style={{color:'#fff', marginLeft: '0.40em' }}aria-label="upload picture" component="span">
                        {this.state.progressMinimize ?  <CheckBoxOutlineBlankIcon /> :  <MinimizeIcon />}
                           
                        </IconButton>
                        <IconButton  onClick={this.handleClose} style={{color:'#fff' }}aria-label="upload picture" component="span">
                            <CloseIcon />
                        </IconButton>
                    </DialogTitle>
                        {!this.state.progressMinimize &&  Object.keys(this.files).length !== 0? 
                        (   
                            // <React.Fragment>
                            <DialogContent style={{width:"100%", padding: "3%", height: '8em'}}>
                    
                                {Object.keys(this.files).map((file) => 
                                    <React.Fragment key={file}>
                                        <div style={{height: '40px', display:'inline-block', marginRight:'10px', verticalAlign: 'middle', width:"75%", 
                                        textOverflow: "ellipsis", overflow: "hidden", whiteSpace: "nowrap"}}>{file}</div> 
                                        {this.state.errorUploadFiles.includes(file) ? 
                                            <CircularProgress variant="static" value={this.files[file]} color="secondary"/> 
                                            :
                                            <CircularProgress variant="static" value={this.files[file]} style={{color:"#337ab7"}}/>
                                        }
                                    </React.Fragment>
                                )}

                            </DialogContent>
                            /* </React.Fragment> */
                        )
                        :
                        <React.Fragment></React.Fragment>
                        }
                </Card>
                )
                : ('') }
            </React.Fragment>
        );
    }
}