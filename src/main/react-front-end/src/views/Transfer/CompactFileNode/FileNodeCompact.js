import React, { Component } from 'react';
import Button from '@material-ui/core/Button';

import FolderIcon from "@material-ui/icons/Folder";
import FileIcon from "@material-ui/icons/Note";
import InFolderIcon from "@material-ui/icons/ArrowForwardIos";
import { Draggable } from 'react-beautiful-dnd';
import styled from "styled-components";
import {getSelectionCount} from "../initialize_dnd";
import { screenIsSmall } from "../utils.js";

/**
	Component for file and directory
	Example 
	[File.   x]
	[Folder xxx x]
	and you can click to enter
*/

import { DraggableProvided, DraggableStateSnapshot } from 'react-beautiful-dnd';

const FileDiv = styled.tr`
  outline: none;
  user-select: none;
  background-color: ${props => getBackgroundColor(props.isSelected)};
  color: ${ props => getTextColor(props.isSelected)},
  display: inline;
  white-space: nowrap;
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

export default class FileNodeCompact extends Component {


	arraysEqual(a, b) {
	  if (a === b) return true;
	  if (a == null || b == null) return false;
	  if (a.length != b.length) return false;

	  // If you don't care about the order of the elements inside
	  // the array, you should sort both arrays here.
	  // Please note that calling sort on an array will modify that array.
	  // you might want to clone your array first.

	  for (var i = 0; i < a.length; ++i) {
	    if (a[i] !== b[i]) return false;
	  }
	  return true;
	}

	shouldComponentUpdate(nextProps, nextState) { 

    	if (this.arraysEqual(nextProps.columns, this.props.columns) &&
    		nextProps.posit0 === this.props.posit0 &&
    		nextProps.posit1 === this.props.posit1 &&
    		nextProps.posit2 === this.props.posit2 &&
    		nextProps.posit3 === this.props.posit3 &&
    		nextProps.isSelected === this.props.isSelected && 
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

	    const wasMetaKeyUsed: boolean = event.metaKey;
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
		this.arraysEqual = this.arraysEqual.bind();

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
		const {index, side, onClick, onDoubleClick, isSelected, isGhosting, endpoint, posit0,posit1,posit2,posit3, columns} = this.props;
		const {name, dir, perm, time, size, children} = this.props.file;
		const {isDragging} = this.state;
		const hasAttr = (time != 0 || perm || size != 0);
		var options = { year: 'numeric', month: 'numeric', day: 'numeric', hour: 'numeric', minute: 'numeric', second: 'numeric'};
		const date = new Date(time * 1000);
		const pstyle =  {textOverflow:"ellipsis", whiteSpace: "nowrap", overflow: "hidden", marginLeft: "10px",textAlign: "left", display: "inline-block"};
		return (
			<Draggable draggableId={ endpoint.side + " " +JSON.stringify(this.props.file)} index = {index}>
			{(provided : DraggableProvided, snapshot : DraggableStateSnapshot) => {
				const selectionCount = getSelectionCount(endpoint);
				const shouldShowSelection: boolean = snapshot.isDragging && selectionCount > 1;
				return (
					<FileDiv
						onDoubleClick={() => {
							if(dir){onDoubleClick(this.props.file.name, this.props.file.id)}
						}}
						{...provided.draggableProps}
						{...provided.dragHandleProps}
						ref={provided.innerRef}
						width={"100px"}
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
							{shouldShowSelection &&
								<SelectionCount>{selectionCount}</SelectionCount>
							}

							{columns[0] && <td style={{ float: "left", textOverflow:"ellipsis", whiteSpace: "nowrap", overflow: "hidden"}}>
								{dir && <FolderIcon style={{width: 40}}/>}
								{!dir && <FileIcon style={{width: 40}}/>}
								<p style={{...pstyle, width: posit0-40, minWidth: "50"}} > {name} </p>
								
							</td>}
							{columns[1] && <td style={{borderLeft: "1px solid lightgray", whiteSpace: "nowrap", overflow: "hidden"}}>

								<p style={{...pstyle, width: posit1}}>
									{time === 0? "Not Available" : new Intl.DateTimeFormat('en-US', options).format(date)}
								</p>
								
							</td>}

							{columns[2] && <td style={{borderLeft: "1px solid lightgray", textOverflow:"ellipsis", whiteSpace: "nowrap", overflow: "hidden"}}>
								<p style={{...pstyle, width: posit2}}> {perm? perm: "N\/A"} </p>
							</td>}
							{columns[3] && <td style={{borderLeft: "1px solid lightgray", textOverflow:"ellipsis", whiteSpace: "nowrap", overflow: "hidden"}}>
								<p style={{...pstyle, width: posit3}}> {size===0 ? "N\/A" : this.humanFileSize(size)} </p>
							</td>}
					</FileDiv>
			)}}
		</Draggable>);
	}
}
