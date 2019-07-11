import React, { Component } from 'react';
import Button from '@material-ui/core/Button';

import FolderIcon from "@material-ui/icons/Folder";
import FileIcon from "@material-ui/icons/Note";
import InFolderIcon from "@material-ui/icons/ArrowForwardIos";
import { Draggable } from 'react-beautiful-dnd';
import styled from "styled-components";
import {getSelectionCount} from "./initialize_dnd";
import { screenIsSmall } from "./utils.js";

/**
	Component for file and directory
	Example 
	[File.   x]
	[Folder xxx x]
	and you can click to enter
*/

import type { DraggableProvided, DraggableStateSnapshot } from 'react-beautiful-dnd';

const FileDiv = styled.div`
  outline: none;
  user-select: none;
  background-color: ${props => getBackgroundColor(props.isSelected)};
  color: ${ props => getTextColor(props.isSelected)},
  display: flex;
  flex-direction: column; 
  padding: 5px;
  ${props =>
    props.isDragging
      ? `box-shadow: 2px 2px 1px ${"#333"};`
      : ''} ${props =>
    props.isGhosting
      ? 'opacity: 0.2;'
      : ''}
`;


const getBackgroundColor = (isSelected, isGhosting): string => {
	if(isGhosting){
		return "#888";
	}
	if (isSelected) {
	    return "#d9edf7";
	}
	return "#ffffff";
};

const getTextColor = (isSelected, isGhosting): string => {
	if(isGhosting){
		return "#888";
	}
	if (isSelected) {
	    return "#31708f";
	}

	return "#333333";
};

const getExtraStyle = (isDragging, isGhosting): string => {
	if(isDragging){
		return {boxShadow: "3px 3px 1px #333"};
	}
	if(isGhosting){
		return {opacity: 0.2};
	}

	return {};
};

const keyCodes = {
	enter: 13,
	escape: 27,
	arrowDown: 40,
	arrowUp: 38,
	tab: 9,
};

const SelectionCount = styled('div')`
  left: -${15}px;
  top: -${15}px;
  color: ${"#fff"};
  background: ${"#337ab7"};
  border-radius: 50%;
  height: ${30}px;
  width: ${30}px;
  line-height: ${30}px;
  position: absolute;
  text-align: center;
  font-size: 1.8rem;
`;

const primaryButton = 0;

export default class FileNode extends Component {

	shouldComponentUpdate(nextProps, nextState) { 
    	if (nextProps.isSelected === this.props.isSelected && 
    		nextProps.isGhosting === this.props.isGhosting && 
    		nextProps.file === this.props.file) return false;

    	return true;
  	}
	onKeyDown = (
	    event: KeyboardEvent,
	    provided: DraggableProvided,
	    snapshot: DraggableStateSnapshot,
	) => {
	    if (provided.dragHandleProps) {
	      provided.dragHandleProps.onKeyDown(event);
	    }

	    if (event.defaultPrevented) {
	      return;
	    }

	    if (snapshot.isDragging) {
	      return;
	    }

	    if (event.keyCode !== keyCodes.enter) {
	      return;
	    }
	    // we are using the event for selection
	    event.preventDefault();

	    const wasMetaKeyUsed: boolean = event.metaKey || event.ctrlKey;
	    const wasShiftKeyUsed: boolean = event.shiftKey;

	    this.performAction(wasMetaKeyUsed, wasShiftKeyUsed);
	};

	onClick = (event: MouseEvent) => {
	    if (event.defaultPrevented) {
	      return;
	    }
	    if (event.button !== primaryButton) {
	      return;
	    }
	    // marking the event as used
	    event.preventDefault();

	    const wasMetaKeyUsed: boolean = event.metaKey;
	    const wasShiftKeyUsed: boolean = event.shiftKey;
	    this.performAction(wasMetaKeyUsed, wasShiftKeyUsed);
	  };
	onTouchStart = (event) => {

		this.setState({dragging: false});
	}

	onTouchMove = (event) => {

		this.setState({dragging: true});
	}

	onTouchEnd = (event: TouchEvent) => {
	    if (event.defaultPrevented || this.state.dragging) {
	      return;
	    }

	    // marking the event as used
	    // we would also need to add some extra logic to prevent the click
	    // if this element was an anchor
	    event.preventDefault();

	    this.props.toggleSelectionInGroup(this.props.file);
	    return false;
	};

	performAction = (wasMetaKeyUsed: boolean, wasShiftKeyUsed: boolean) => {
		const {
		  file,
		  toggleSelection,
		  toggleSelectionInGroup,
		  multiSelectTo,
		} = this.props;

		if (wasMetaKeyUsed) {
		  toggleSelectionInGroup(this.props.file);
		  return;
		}

		if (wasShiftKeyUsed) {
		  multiSelectTo(this.props.file);
		  return;
		}
		toggleSelection(this.props.file);
	};

	constructor(props){
		super(props);
		this.humanFileSize = this.humanFileSize.bind(this);
		this.state = {
			isDragging: false,
		}

	}

	humanFileSize(bytes) {
	    var thresh = 1024;
	    if(Math.abs(bytes) < thresh) {
	        return bytes + ' B';
	    }
	    var units = ['kB','MB','GB','TB','PB','EB','ZB','YB']
	    var u = -1;
	    do {
	        bytes /= thresh;
	        ++u;
	    } while(Math.abs(bytes) >= thresh && u < units.length - 1);
	    return bytes.toFixed(1)+' '+units[u];
	}

	render(){
		const {index, side, onClick, onDoubleClick, isSelected, isGhosting, endpoint} = this.props;
		const {name, dir, perm, time, size, children} = this.props.file;
		const {isDragging} = this.state;
		const hasAttr = (time != 0 || perm || size != 0);
		var options = { year: 'numeric', month: 'numeric', day: 'numeric',hour: 'numeric', minute: 'numeric', second: 'numeric'};
		const date = new Date(time * 1000);
		
		return (
			<Draggable draggableId={ endpoint.side + " " +JSON.stringify(this.props.file) } index = {index}>
			{(provided : DraggableProvided, snapshot : DraggableStateSnapshot) => {
				const selectionCount = getSelectionCount(endpoint);
				const shouldShowSelection: boolean =
	            snapshot.isDragging && selectionCount > 1;
				return (
					<FileDiv
						onDoubleClick={() => {
							if(dir){onDoubleClick(this.props.file.name, this.props.file.id)}
						}}
						{...provided.draggableProps}
						{...provided.dragHandleProps}
						ref={provided.innerRef}

						onClick={this.onClick}
		                onTouchEnd={(e)=>{this.onTouchEnd(e)}}
		                onTouchStart={(e)=>{this.onTouchStart(e)}}

		                onTouchMove={(e)=>{this.onTouchMove(e)}}
		                onKeyDown={(event: KeyboardEvent) =>
		                  this.onKeyDown(event, provided, snapshot)
		                }
		                isSelected={isSelected}
		                isGhosting={shouldShowSelection && isGhosting}
					>
						

							<div style={{display: "flex", flexGrow: 1, flexDirection: "row", justifyContent: "flex-start"}}>
								{ dir && <FolderIcon/>}
								{!dir && <FileIcon />  }
								<p style={{marginLeft: "10px", flexGrow: 1, textAlign: "left"}} > {name} </p>
								{(dir && screenIsSmall()) && <Button style={{width: "40px", float: "right"}} onTouchStart={() => {
									onDoubleClick(this.props.file.name);
								}}>
									<InFolderIcon/>
								</Button>} 
							</div>
							{shouldShowSelection &&
								<SelectionCount>{selectionCount}</SelectionCount>
							}
							{hasAttr && 
								<div style={{display: "flex", flexGrow: 1, flexDirection: "row", justifyContent: "space-between"}}>
							
									{time != 0 && <p style={{fontSize: "10px", color: "#444"}}>{new Intl.DateTimeFormat('en-US', options).format(date)} </p>}
									{perm && <p > {perm} </p>}
									{size != 0 && <p style={{fontSize: "10px", color: "#444"}}> {this.humanFileSize(size)} </p>}
								</div>
							}
					</FileDiv>
			)}}
		</Draggable>);
	}
}
