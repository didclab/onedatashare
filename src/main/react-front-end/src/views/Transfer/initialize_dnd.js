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



import {store} from "../../App.js";
import {eventEmitter} from "../../App";

//left
var column1 = {
	id: "left",
	title: "",
	path: [],
	tasks: [],
	ids: [null],
	selectedTasks : [],
};
//right
var column2 = {
	id: "right",
	title: "",
	path: [],
	tasks: [],
	ids: [null],
	selectedTasks :[],
};
export var draggingTask = null;

export function getMapFromEndpoint(endpoint){

	if(endpoint.side === "left"){
//		console.log(column1.ids);
//		console.log(column1.path);
		return column1.ids.map(function(e, i) {
		  return {id: column1.ids[i], path: buildPathToIndex(endpoint.uri, column1.path, i)};
		});
	}else{

//		console.log(column2.ids);
//		console.log(column2.path);
		return column2.ids.map(function(e, i) {
		  return {id: column2.ids[i], path: buildPathToIndex(endpoint.uri, column2.path, i)};
		});
	}
}

export function getIdsFromEndpoint(endpoint){
	if(endpoint.side === "left"){
		return column1.ids;
	}else{
		return column2.ids;
	}
}

function buildPathToIndex(edpuri, idsArray, index){
	if(index === 0 || idsArray.length === 0){
		return edpuri;
	}
	
	let tempArray = idsArray.slice(0, index);

	return makeFileNameFromPath(edpuri, tempArray, "");
}

 
export function setDraggingTask(task){
	draggingTask = task;
}

//@return: initial+"/"+path[1]+"/"+path[2]+...+path[n]+"/"+name
export function makeFileNameFromPath(initial, path, name){
//	console.log(initial, path, name)
	var pathstr;
	if(path.length === 0){
		pathstr = initial;
	}else{
		pathstr = initial + (initial[initial.length-1] === '/' ? "" : "/") + path.reduce((a, v) =>  a+"/"+v);
	}
	pathstr = (pathstr + ((name.length === 0 || pathstr[pathstr.length-1] === '/') ? name : "/"+name));
	return pathstr;
}
export function longestCommonPrefix(files){
	if(files.length === 0){
		return ""
	}
	if(files.length === 1){
		return files[0]
	}
	let length = files[0].Length;
    for (var i = 1; i < files.Length; i++)
    {
        length = Math.Min(length, files[i].Length);
        for (var j = 0; j < length; j++)
            if (files[i][j] !== files[0][j])
            {
                files = j;
                break;
            }
    }
    return files[0].substring(0, length);
}
export function getEntities(){ 
	return {left: column1, right: column2}
};

export function getColumn(id){ 
	return getEntities()[id]
};

export function getCred(){ 
	const state = store.getState();
	let loggedInAccounts = []
	if (state?.endpoint1?.login) {
		loggedInAccounts.push(`${state?.endpoint1?.uri}${state?.endpoint1?.credential?.name}`)
	}
	if (state?.endpoint2?.login) {
		loggedInAccounts.push(`${state?.endpoint2?.uri}${state?.endpoint2?.credential?.name}`)
	}
	return loggedInAccounts
};


export function setFilesWithPathListAndTasks(files, path, endpoint){
	if(endpoint.side === "left"){
		column1.selectedTasks = files;
		column1.path = path;
	}else{
		column2.selectedTasks = files;
		column2.path = path;
	}
}

export function setFilesWithPathListAndId(files, path, ids, endpoint){
	if(endpoint.side === "left"){
		column1.tasks = files;
		column1.title = endpoint.uri;
		column1.path = path;
		column1.ids = ids;
	}else{
		column2.tasks = files;
		column2.title = endpoint.uri;
		column2.path = path;
		column2.ids = ids;
	}
}


export function getCurrentFolderId(endpoint){
	if(endpoint.side === "left"){
		return column1.ids[column1.ids.length-1];
	}else{
		return column2.ids[column2.ids.length-1];
	}
}

export function getFilesFromMemory(endpoint){
	if(endpoint.side === "left"){
		return column1.tasks;
	}else{
		return column2.tasks;
	}
}

export function getPathFromMemory(endpoint){
	if(endpoint.side === "left"){
		return column1.path;
	}else{
		return column2.path;
	}
}

export function emptyFileNodesData(endpoint){
	if(endpoint.side === "left"){
		column1 = {
			id: "left",
			title: "",
			path: [],
			tasks: [],
			ids:[null],
			previousTasks: [],
			selectedTasks : [],

		};
	}else if(endpoint.side === "right"){
		column2 = {
			id: "right",
			title: "",
			path: [],
			tasks: [],
			ids:[null],
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

	if(reorderResult.fromTo[1].id === "left"){
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
		return task.name === id
	})
	const find2 = column2.tasks.find((task)=>{
		return task.name === id
	})
	return find1 || find2
}*/

export function setSelectedTasks(selTasksLeft, selTasksRight){
	column1.selectedTasks = selTasksLeft;
	column2.selectedTasks = selTasksRight;
	eventEmitter.emit("fileChange", null, null);
}

export function getSelectedTasksFromSide(endpoint){
	if(endpoint.side === "left"){
		return column1.selectedTasks;
	}else{
		return column2.selectedTasks;
	}	
}
/*
export function getTaskFromId(endpoint, id){
	if(endpoint.side === "left"){
		return column1.tasks.find((task)=>task.name+endpoint.side===id);
	}else{
		return column2.tasks.find((task)=>task.name+endpoint.side===id);
	}
}*/
export function setSelectedTasksForSide(tasks, endpoint){
	if(endpoint.side === "left"){
		setSelectedTasks(tasks, column2.selectedTasks);
	}else{
		setSelectedTasks(column1.selectedTasks, tasks);
	}
}
export function getEndpointFromColumn(column){
	if(column.id === "left"){
		return store.getState().endpoint1;
	}else{
		return store.getState().endpoint2;
	}
}

export function getSelectedTasks(){
	return {left: column1.selectedTasks, right: column2.selectedTasks};
}

export function getSelectionCount(endpoint){
	if(endpoint.side === "left"){
		return column1.selectedTasks.length;
	}else{
		return column2.selectedTasks.length;
	}
}

export function unselectAll (){
	setSelectedTasks([], []);
};