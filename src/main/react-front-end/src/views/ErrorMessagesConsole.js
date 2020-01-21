import React, { Component } from 'react';
import { eventEmitter} from "../App";
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
			<div style={{textAlign: "left", borderWidth: 1, height: "150px", 
					overflow: "scroll", wordWrap: "break-word", borderStyle: "solid",
					borderColor: "black", padding: "5px", borderRadius: "10px"
				}}
				
			>
				{errorMessages.map(msg => <p style={{color: msg.color}}>{msg.msg}</p>)}
				<p ref={(msgsList) => { this.messagesEnd = msgsList; }}> ></p>
			</div>
			);
		;
	}
}
