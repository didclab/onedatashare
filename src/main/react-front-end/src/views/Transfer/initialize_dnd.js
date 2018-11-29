
import {store} from "../../App.js";
import {eventEmitter} from "../../MainComponent";

//left
var column1 = {
	id: "left",
	title: "",
	path: [],
	tasks: [],
	selectedTasks : [],
};
//right
var column2 = {
	id: "right",
	title: "",
	path: [],
	tasks: [],
	selectedTasks :[],
};
export var draggingTask = null;

export function setDraggingTask(task){
	draggingTask = task;
}

//@return: initial+"/"+path[1]+"/"+path[2]+...+path[n]+"/"+name
export function makeFileNameFromPath(initial, path, name){
	console.log(initial, path, name)
	var pathstr;
	if(path.length == 0){
		pathstr = initial;
	}else{
		pathstr = initial + (initial[initial.length-1] === '/' ? "" : "/") + path.reduce((a, v) =>  a+"/"+v);
	}
	pathstr = (pathstr + ((name.length === 0 || pathstr[pathstr.length-1] == '/') ? name : "/"+name));
	return pathstr;
}

export function getEntities(){ 
	return {left: column1, right: column2}
};

export function getColumn(id){ 
	return getEntities()[id]
};

export function getCred(){ 
	const state = store.getState();
	const credentials = {...state.endpoint1.credential, ...state.endpoint2.credential}
	return Object.keys(credentials).map((v)=>credentials[v]);
};


export function setFilesWithPathList(files, path, endpoint){
	if(endpoint.side == "left"){
		column1.tasks = files;
		column1.title = endpoint.uri;
		column1.path = path;
	}else{
		column2.tasks = files;
		column2.title = endpoint.uri;
		column2.path = path;
	}
}

export function getFilesFromMemory(endpoint){
	if(endpoint.side == "left"){
		return column1.tasks;
	}else{
		return column2.tasks;
	}
}

export function getPathFromMemory(endpoint){
	if(endpoint.side == "left"){
		return column1.path;
	}else{
		return column2.path;
	}
}

export function emptyFileNodesData(endpoint){
	if(endpoint.side == "left"){
		column1 = {
			id: "left",
			title: "",
			path: [],
			tasks: [],
			previousTasks: [],
			selectedTasks : [],

		};
	}else{
		column2 = {
			id: "right",
			title: "",
			path: [],
			tasks: [],
			previousTasks: [],
			selectedTasks : [],
		};
	}
}

export function setBeforeTransferReorder(reorderResult){
	if(!reorderResult){
		return
	}
	//console.log(reorderResult);
	if(reorderResult.left){
		column1.tasks = reorderResult["left"].tasks;
	}
	if(reorderResult.right){
		column2.tasks = reorderResult["right"].tasks;
	}

	if(reorderResult.fromTo[1].id == "left"){
		column1.selectedTasks = reorderResult.selectedTasks;
	}else{
		column2.selectedTasks = reorderResult.selectedTasks;	
	}
	//column1.previousTasks = reorderResult["0"].taskIds;
	//column2.previousTasks = reorderResult["1"].taskIds;
	eventEmitter.emit("fileChange", null, null);
}

/*export function findTaskFromId(id){
	const find1 = column1.tasks.find((task)=>{
		return task.name == id
	})
	const find2 = column2.tasks.find((task)=>{
		return task.name == id
	})
	return find1 || find2
}*/

export function setSelectedTasks(selTasksLeft, selTasksRight){
	column1.selectedTasks = selTasksLeft;
	column2.selectedTasks = selTasksRight;
	eventEmitter.emit("fileChange", null, null);
}

export function getSelectedTasksFromSide(endpoint){
	if(endpoint.side == "left"){
		return column1.selectedTasks;
	}else{
		return column2.selectedTasks;
	}	
}
/*
export function getTaskFromId(endpoint, id){
	if(endpoint.side == "left"){
		return column1.tasks.find((task)=>task.name+endpoint.side==id);
	}else{
		return column2.tasks.find((task)=>task.name+endpoint.side==id);
	}
}*/
export function setSelectedTasksForSide(tasks, endpoint){
	if(endpoint.side == "left"){
		setSelectedTasks(tasks, column2.selectedTasks);
	}else{
		setSelectedTasks(column1.selectedTasks, tasks);
	}
}
export function getEndpointFromColumn(column){
	if(column.id == "left"){
		return store.getState().endpoint1;
	}else{
		return store.getState().endpoint2;
	}
}

export function getSelectedTasks(){
	return {left: column1.selectedTasks, right: column2.selectedTasks};
}

export function getSelectionCount(endpoint){
	if(endpoint.side == "left"){
		return column1.selectedTasks.length;
	}else{
		return column2.selectedTasks.length;
	}
}

export function unselectAll (){
	setSelectedTasks([], []);
    return [];
};
