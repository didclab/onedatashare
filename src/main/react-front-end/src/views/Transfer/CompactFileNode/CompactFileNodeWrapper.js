import React, { Component } from 'react';
import FileNodeCompact from "./FileNodeCompact.js";
import Menu from '@material-ui/core/Menu';
import MenuItem from '@material-ui/core/MenuItem';
import CheckIcon from "@material-ui/icons/Check";
import Button from '@material-ui/core/Button';
import TableSortLabel from '@material-ui/core/TableSortLabel';

export default class CompactFileNodeWrapper extends Component {
	constructor(props){
		super(props);
		this.state = {
			headerCompactStylePos : [100, 150, 70, 70],
			compactStylePos : [100, 150, 70, 70],
			columns: [true, true, true, true],
			orderBy: -1,
			columnsOrder: ["Desc", "Desc", "Desc", "Desc"],
			leftPosition: 0,
			showBar: null
		}
		this.generateResizer = this.generateResizer.bind(this);
		this.contextMenu = this.contextMenu.bind(this);
		this.handleClose = this.handleClose.bind(this);
		this.CompactTable = React.createRef();
	}

	handleRequestSort = (property) => {
	    const orderBy = property;
	    let order = 'desc';

	    if (this.state.orderBy === property && this.state.order === 'desc') {
	      order = 'asc';
	    }

		this.setState({ order:order, orderBy:orderBy });
		this.state.order=order
		this.state.orderBy = orderBy
		this.queueFunc()
	};	

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

	componentDidMount(){
		let headerStyleDelta = this.state.headerCompactStylePos;
		let headersTotal = headerStyleDelta.reduce((a, x) => a+x, 0)
		let multiplier = this.CompactTable.current.offsetWidth/headersTotal
		let headerMultiplied = headerStyleDelta.map((v) => v*multiplier);
		
		headerStyleDelta = headerMultiplied.map((v)=> v < 70? 70 : v);
		
		this.setState({headerCompactStylePos: headerStyleDelta, compactStylePos: headerStyleDelta});
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
			if(temp[this.target] < 70){
				temp[this.target] = 70;
			}
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
	    this.offset = e.pageX - this.refs.CompactHeader.getBoundingClientRect().left;
	    return false;
	}

	onMouseUp = (e) => {
		this.setState({compactStylePos: this.state.headerCompactStylePos.slice(), showBar: null});
		this.pageX = undefined;
		this.target = undefined;
	}

	flippingChange = (change) => {
		if(change == "Asc"){
			return "Desc";
		}else{
			return "Asc";
		}
	}

	handleRequestSort = (id) => () => {
		let { orderBy, columnsOrder } = this.state;

		if(orderBy != id){
			orderBy = id;
		}else{
			columnsOrder[orderBy] = this.flippingChange(columnsOrder[orderBy]);
		}
		this.props.sortBy(this.props.sortFunctions[orderBy][columnsOrder[orderBy]]);

		this.setState({
			orderBy: orderBy, 
			columnsOrder: columnsOrder
		});
	}

	render(){
		let { displayList, list, selectedTasks, endpoint, draggingTask, toggleSelection, toggleSelectionInGroup, multiSelectTo, onClick, onDoubleClick}  = this.props;
		let { compactStylePos, headerCompactStylePos, anchorEl,columns, leftPosition, showBar, orderBy, columnsOrder } = this.state;

		const columnNames = ["File Name", "Date", "Permission", "Size"];

		let showingColumn = [0,1,2,3].filter((i) => columns[i]);

		const pstyle = {textOverflow:"ellipsis", whiteSpace: "nowrap", overflow: "hidden", marginLeft: "10px",textAlign: "left", display: "inline-block"};

		let columnToggle = (num) => () => {
			let newColumns = columns.slice();
			newColumns[num] = !newColumns[num];
			this.setState({columns:newColumns});
		}

		let headers = showingColumn.map( (colId) => 
		   <th style={{borderLeft: "1px solid darkgray", borderBottom: "1px solid gray", height: "10px", textOverflow:"ellipsis", whiteSpace:"nowrap", overflow: "visible", position: "relative"}}>
		   		
		   		<TableSortLabel
		   			style={{...pstyle, width: headerCompactStylePos[colId]}}
					active={orderBy === colId}
					direction={columnsOrder[colId]}
					onClick={this.handleRequestSort(colId)}>
					{columnNames[colId]}
				</TableSortLabel>
		   		
		   		
		   		{this.generateResizer(colId)}
		   </th>
		);
		return (
		<div ref={this.CompactTable}>
			<table id={"tableHeader"+this.props.endpoint.side}>
				<thead style={{boxShadow: "0px 0px 5px #aaaaaa", overflow: "visible"}} onContextMenu={this.contextMenu} ref="CompactHeader">
					<tr>
						{showBar &&
							<div style={{borderLeft:"1px solid black", height: "270px", position: "absolute", left: leftPosition+17+"px"}}></div>
						}
						{headers}
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