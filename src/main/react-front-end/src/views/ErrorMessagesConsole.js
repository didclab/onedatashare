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
import { eventEmitter} from "../App";
import {Grid, Card} from "@material-ui/core";
export default class ErrorMessagesConsole extends Component {

	constructor(props){
		super(props);
		this.state = {
			errorMessages: [{msg: "Welcome to OneDataShare! Please choose the end-points you would like to access. You can simply drag-and-drop files and directories between end-points to perform a transfer. Then, relax and monitor your transfer to complete.", color: "black"},]
		}
		this.onErrorOccured = this.onErrorOccured.bind(this);
		this.onMessageOccurred = this.onMessageOccurred.bind(this);
		this.onWarningOccurred = this.onWarningOccurred.bind(this);
	}
	componentDidMount(){
    	eventEmitter.on("errorOccured", this.onErrorOccured); 
    	eventEmitter.on("messageOccured", this.onMessageOccurred); 

    	eventEmitter.on("warningOccured", this.onWarningOccurred); 
  	}

	onErrorOccured(message){
		let messagesWithin = this.state.errorMessages;
		messagesWithin.push({msg: message, color: "red"});
		this.setState({errorMessages: messagesWithin});
	}

	onMessageOccurred(message){
		let messagesWithin = this.state.errorMessages;
		messagesWithin.push({msg: message, color: "black"});
		this.setState({errorMessages: messagesWithin});
	}

	onWarningOccurred(message){
		let messagesWithin = this.state.errorMessages;
		messagesWithin.push({msg: message, color: "orange"});
		this.setState({errorMessages: messagesWithin});
	}

	scrollToBottom = () => {
	  this.messagesEnd.scrollIntoView({ behavior: "smooth" });
	}
	componentDidUpdate() {
	  this.scrollToBottom();
	}
	render(){
		const {errorMessages} = this.state;
		return(
			// <Grid container direction={"row"} alignItems={"flex-start"} spacing={2}>
			// 	<Grid item md={5} xs={12} >
			// 		<div className={"instructions"}>
			// 			<p>
			//
			// 			</p>
			// 		</div>
			// 	</Grid>
			// 	<Grid item md={7} xs={12}>
					<div className={"errorConsole"} /*style={{textAlign: "left", borderWidth: 1, height: "100px",
					overflow: "scroll", wordWrap: "break-word", borderStyle: "solid",
					borderColor: "black", padding: "5px", borderRadius: "10px", marginTop: "20px"
				}}*/

					>
						{errorMessages.map((msg, i) => <p key={i} style={{color: msg.color}}>{msg.msg}</p>)}
						<p ref={(msgsList) => { this.messagesEnd = msgsList; }}> ></p>

					</div>
			// 	</Grid>
			// </Grid>





			);
		;
	}
}
