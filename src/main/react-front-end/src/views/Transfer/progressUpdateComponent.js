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
import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogTitle from '@material-ui/core/DialogTitle';
import CircularProgress from '@material-ui/core/CircularProgress';
import { eventEmitter } from "../../App";

export default class ProgressUpdateComponent extends Component {
    constructor(props){
		super(props);
		this.state={
            progress: true,
			uploadPercent: 0,
			uplaodFileName: '',
            progressMinimize: false,
            errorUploadFiles: []
        }
        this.files = {}
    }

    componentDidMount() {
        eventEmitter.on("progressChange", this.progressChangeHandler); 
        eventEmitter.on("progressUpdateError", this.progressErrorHandler); 
	}

	progressChangeHandler = (name, progressPercent) => {
        try {
            this.files[name] = progressPercent;
            var errorFiles = this.state.errorUploadFiles;
            if (errorFiles.length !== 0 && progressPercent === 100) {
                const index = errorFiles.indexOf(name);
                if (index > -1) {
                    errorFiles.splice(index, 1);
                }
            }
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
            this.setState({ 
                progress: false
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
        return(
            <Dialog
				disableBackdropClick={true}
                open={this.state.progress}
                onClose={this.handleClose}
				aria-labelledby="form-dialog-title"
				style={{position: 'absolute', top: '', left: '', width: '33%', height:'36%'}}
				hideBackdrop={true}
	        >
				<DialogTitle id="form-dialog-title" style={{textAlign: 'center', backgroundColor: '#337ab7', color: '#fff'}}>PROGRESS UPDATE:</DialogTitle>
                    {!this.state.progressMinimize ? 
                    (   
                        <React.Fragment>
					    <DialogContent style={{width:"100%", padding: "3%"}}>
				  
                        {/* <DialogContentText> */}
                            {Object.keys(this.files).map((file) => 
                                <React.Fragment key={file}>
                                    <div style={{height: '40px', display:'inline-block', marginRight:'10px', verticalAlign: 'middle', width:"75%", 
                                    textOverflow: "ellipsis", overflow: "hidden", whiteSpace: "nowrap"}}>{file}</div> 
                                    {this.state.errorUploadFiles.includes(file) ? 
                                        <CircularProgress variant="static" value={this.files[file]} color="secondary"/> 
                                        :
                                        <CircularProgress variant="static" value={this.files[file]} />
                                    }
                                </React.Fragment>
                            )}
                         </DialogContent>
                        </React.Fragment>
                    )
					:
					<React.Fragment></React.Fragment>
				    }
                <DialogActions>
                    <Button onClick={this.handleMinimise} color="primary">
                    {this.state.progressMinimize ? 'Maximize' : 'Minimize'}
                    </Button>
                    <Button onClick={this.handleClose} color="primary">
                    Close
                    </Button>
                </DialogActions>
	        </Dialog>
        )
    }
}