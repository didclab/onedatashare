import React, { Component } from 'react';
import FileNodeCompact from "./FileNodeCompact.js";
import Menu from '@material-ui/core/Menu';
import MenuItem from '@material-ui/core/MenuItem';
import CheckIcon from "@material-ui/icons/Check";

export default class CompactFileNodeWrapper extends Component {
	constructor(props){
		super(props);
		this.state = {
			headerCompactStylePos : [200, 200, 50, 50],
			compactStylePos : [200, 200, 50, 50],
			columns: [true, true, true, true],
			leftPosition: 0,
			showBar: null
		}
		this.generateResizer = this.generateResizer.bind(this);
		this.contextMenu = this.contextMenu.bind(this);
		this.handleClose = this.handleClose.bind(this);
	}

	generateResizer = (targetNum) => {
		return (<div style={{top: 0, right: 0, width: "3px", position: "absolute", cursor: "col-resize", backgroundColor: "#00000000", userSelect :'none',height: "100%"}}
   			onMouseDown={(e) => {
   				this.pageX = e.pageX;
   				this.target = targetNum;
   				let rect = document.getElementById("browser"+this.props.endpoint.side).getBoundingClientRect();
		
   				this.setState({showBar: targetNum+1, leftPosition: e.clientX - rect.left});
   			}}
   		></div>);
	}

	componentDidMount() {
	    window.addEventListener("mousemove", this.onMouseMove);
	    window.addEventListener("mouseup", this.onMouseUp);
	}

	componentWillUnmount() {
 		window.removeEventListener("mousemove", this.onMouseMove);
	    window.removeEventListener("mouseup", this.onMouseUp);

	}

	onMouseMove = (e) => {
		let rect = document.getElementById("browser"+this.props.endpoint.side).getBoundingClientRect();
		if(this.target != undefined){
			let temp = this.state.headerCompactStylePos;
			temp[this.target] += (e.pageX - this.pageX);
			this.pageX = e.pageX;
			let accumlate = e.clientX - rect.left;
			this.setState({headerCompactStylePos: temp, leftPosition: accumlate});
		}
	}

	handleClose = (e) => {
		e.preventDefault();
    	this.setState({ anchorEl: null });
  	};
	contextMenu(e){
	    e.preventDefault();
	    this.setState({ anchorEl: e.currentTarget });
	    this.offset = e.pageX -this.refs.CompactHeader.getBoundingClientRect().left;
	    return false;
	}

	onMouseUp = (e) => {
		this.setState({compactStylePos: this.state.headerCompactStylePos.slice(), showBar: null});
		this.pageX = undefined;
		this.target = undefined;
	}

	render(){
		let { displayList, list, selectedTasks, endpoint, draggingTask, toggleSelection, toggleSelectionInGroup, multiSelectTo, onClick, onDoubleClick}  = this.props;
		let { compactStylePos, headerCompactStylePos, anchorEl,columns, leftPosition, showBar } = this.state;

		const pstyle = { height: "15px", textOverflow:"ellipsis", whiteSpace: "nowrap", overflow: "hidden", marginLeft: "10px",textAlign: "left", display: "inline-block"};

		let columnToggle = (num) => () => {
			let newColumns = columns.slice();
			newColumns[num] = !newColumns[num];
			this.setState({columns:newColumns});
		}
		return (
		<div>
			<table id={"tableHeader"+this.props.endpoint.side}>

				<thead  style={{boxShadow: "0px 0px 5px #aaaaaa", overflow: "visible"}} onContextMenu={this.contextMenu} ref="CompactHeader">
					<tr>
					{showBar &&
						<div style={{borderLeft:"1px solid black", height: "270px", position: "absolute", left: leftPosition+17+"px"}}></div>
					}
					{columns[0] && 
					   <th style={{borderBottom: "1px solid gray", height: "10px", textOverflow:"ellipsis", whiteSpace: "nowrap", overflow: "visible", position: "relative"}}>
					   		<p style={{...pstyle, width: headerCompactStylePos[0]}}>File Name</p>
					   		{this.generateResizer(0)}
					   </th>}
					{columns[1] && 
					   <th style={{borderLeft: "1px solid darkgray",height: "10px",  borderBottom: "1px solid gray",textOverflow:"ellipsis", whiteSpace: "nowrap", overflow: "visible", position: "relative"}}>
					   		<p style={{...pstyle, width: headerCompactStylePos[1]}}>Date</p>
					   		{this.generateResizer(1)}
					   </th>}
					{columns[2] && 
					   <th style={{borderLeft: "1px solid darkgray",height: "10px",  borderBottom: "1px solid gray", textOverflow:"ellipsis", whiteSpace: "nowrap", overflow: "visible", position: "relative"}}>
					   		<p style={{...pstyle, width: headerCompactStylePos[2]}}>Permission</p>
					   		{this.generateResizer(2)}
					   </th>}
					{columns[3] && 
					   <th style={{borderLeft: "1px solid darkgray",height: "10px",  borderBottom: "1px solid gray", textOverflow:"ellipsis", whiteSpace: "nowrap", overflow: "visible", position: "relative"}}>
					   		<p style={{...pstyle, width: headerCompactStylePos[3]}}>Size</p>
					   		{this.generateResizer(3)}
					   </th>}
					 </tr>
				</thead>
			</table>

			<Menu
	            id="simple-menu"
	            anchorEl={anchorEl}
	            open={Boolean(anchorEl)}
	            onClose={this.handleClose}
	            style={{width: "200px", left: this.offset+"px", top: "50px"}}
	            onContextMenu={this.handleClose}
	        >
	            <MenuItem onClick={columnToggle(1)}>
	            	{columns[1] && <CheckIcon/>}
	            	Date
	            </MenuItem>
	            <MenuItem onClick={columnToggle(2)}>
	            	{columns[2] && <CheckIcon/>}
	            	Permission
	            </MenuItem>
	            <MenuItem onClick={columnToggle(3)}>
	            	{columns[3] && <CheckIcon/>}
	            	File Size
	            </MenuItem>
	        </Menu>

			<table>
				<tbody>
					{
						displayList.map((fileId, index) => {
						const file = list[fileId];
						const isSelected: boolean = Boolean(
		                  selectedTasks.indexOf(file)!=-1,
		                );
		                const isGhosting: boolean = isSelected &&
		                    Boolean(draggingTask) &&
		                    draggingTask.name !== file.name;

						    return(<FileNodeCompact 
								posit0={compactStylePos[0]}
								posit1={compactStylePos[1]}
								posit2={compactStylePos[2]}
								posit3={compactStylePos[3]}
								key={fileId}
								index={index}
								file={file}
								columns={columns.slice()}
								selectionCount={selectedTasks.length}
								onClick={onClick}
								onDoubleClick={onDoubleClick}
								side={endpoint.side}
								isSelected={isSelected}
								endpoint={endpoint}
			                    isGhosting={isGhosting}
			                    toggleSelection={toggleSelection}
			                    toggleSelectionInGroup={toggleSelectionInGroup}
			                    multiSelectTo={multiSelectTo} />
			                );
						})
					}
				</tbody>
			</table>
		</div>);
	}
}