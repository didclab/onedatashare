// @flow
import type { DraggableLocation } from 'react-beautiful-dnd';
import PropTypes from 'prop-types';
import {getColumn, getEntities,getTaskFromId} from "./initialize_dnd.js"
import {store} from "../../App.js"

export default reorder;
export function screenIsSmall(){
    return window.innerWidth <= 760;
}

// a little function to help us with reordering the result
const reorder = (list: any[], startIndex: number, endIndex: number): any[] => {
  const result = Array.from(list);
  const [removed] = result.splice(startIndex, 1);
  result.splice(endIndex, 0, removed);
  return result;
};


const withNewTasks = (column, tasks) => ({
  ...column,
  tasks,
});

const reorderSingleDrag = ({
  entities,
  selectedTasks,
  source,
  destination,
}) => {
  // dont allow moving in the same list
  if (source.droppableId === destination.droppableId) {

    const column: Column = getColumn(source.droppableId);
    const reordered = reorder(
      column.tasks,
      source.index,
      destination.index,
    );

    const task = column.tasks[source.index];
    const updated: Entities = {
      ...entities,
      [column.id]: withNewTasks(column, reordered),
    };

    const selected = [...selectedTasks["left"], ...selectedTasks["right"]];
    if(selected.length == 0){
      selected.push(task);
    }

    return {
      ...updated,
      selectedTasks:selected,
      fromTo: [column, column]
    };
  }

  // moving to a new list
  const home: Column = getColumn(source.droppableId);
  const foreign: Column = getColumn(destination.droppableId);

  // the id of the task to be moved
  const task = home.tasks[source.index];

  // remove from home column
  const newHomeTasks = [...home.tasks];
  //newHomeTaskIds.splice(source.index, 1);

  // add to foreign column
  const newForeignTasks = [...foreign.tasks];
  /*const foundDup = newForeignTasks.find((value) => {return value.name == task.name;});
  if(foundDup){
    console.log(foundDup);
  };*/
  if(!newForeignTasks.find((value) => {return value.name == task.name;})){
   // console.log(task);
   // console.log(newForeignTasks);
    newForeignTasks.splice(destination.index, 0, task);
  }else{
  }

  const updated: Entities = {
    ...entities,
    [home.id]: withNewTasks(home, newHomeTasks),
    [foreign.id]: withNewTasks(foreign, newForeignTasks),
  };
  const selected = [...selectedTasks["left"], ...selectedTasks["right"]];
  if(selected.length == 0){
    selected.push(task);
  }
  //console.log(selected);
  return {
    ...updated,
    selectedTasks:selected,
    fromTo: [home, foreign]
  };
};

export const getHomeColumn = (entities, task) => {
  const column = entities[Object.keys(entities).find((columnId) => {
    return entities[columnId].tasks.includes(task);
  })];
  return column;
};


export const multiSelectTo = (
  columnOfNew,
  selectedTasks,
  newTask,
) => {
  // Nothing already selected
  if (!selectedTasks.length) {
    return [newTask];
  }

  //console.log(columnOfNew);

  if(columnOfNew == null){
    return selectedTasks;
  }

  const indexOfNew: number = columnOfNew.tasks.indexOf(newTask);
  //console.log(indexOfNew);
  const lastSelected: Id = selectedTasks[selectedTasks.length - 1];

  var indexOfLast: number = columnOfNew.tasks.indexOf(lastSelected);
  //console.log(indexOfNew);

  // multi selecting in the same column
  // need to select everything between the last index and the current index inclusive
  // nothing to do here
  if (indexOfNew === indexOfLast) {
    //(indexOfNew, indexOfLast);
    return [];
  }

  //console.log(selectedTaskIds); 

  const isSelectingForwards = indexOfNew > indexOfLast;
  const start = isSelectingForwards ? indexOfLast : indexOfNew;
  const end = isSelectingForwards ? indexOfNew : indexOfLast;

  const inBetween = columnOfNew.tasks.slice(start, end + 1);

  // everything inbetween needs to have it's selection toggled.
  // with the exception of the start and end values which will always be selected
  //console.log(inBetween)
  const toAdd = inBetween.filter(
    (task) => {
      // if already selected: then no need to select it again
      return !selectedTasks.includes(task);
    },
  );
  //console.log(toAdd)

  const sorted = isSelectingForwards ? toAdd : [...toAdd].reverse();
  const combined = [...selectedTasks, ...sorted];

  return combined;
};

export const mutliDragAwareReorder = (args) => {
  if (args.selectedTasks["left"].length + args.selectedTasks["right"].length > 1) {
    return reorderMultiDrag(args);
  }
  
  return reorderSingleDrag(args);
};

const reorderMultiDrag = ({
  entities,
  selectedTasks,
  source,
  destination,
}): Result => {
  
  
  const start = getColumn(source.droppableId);
  const dragged = start.tasks[source.index];

  const orderedSelectedTasks = selectedTasks[source.droppableId];
  orderedSelectedTasks.sort((a, b) => {
      // moving the dragged item to the top of the list
      if (a === dragged) {
        return -1;
      }
      if (b === dragged) {
        return 1;
      }

      // sorting by their natural indexes
      const columnForA = getHomeColumn(entities, a);
      const indexOfA = columnForA.tasks.indexOf(a);
      const columnForB = getHomeColumn(entities, b);
      //console.log(columnForB)
      const indexOfB = columnForB.tasks.indexOf(b);

      if (indexOfA !== indexOfB) {
        return indexOfA - indexOfB;
      }
      // sorting by their order in the selectedTaskIds list
      return -1;
    },
  );

  selectedTasks[destination.droppableId] = [];
  const insertAtIndex = destination.index;
  //console.log(insertAtIndex);
  
  const startSave = JSON.parse(JSON.stringify(start));
  // we need to remove all of the selected tasks from their columns
  const withRemovedTasks = Object.keys(entities).reduce(
    (previous, columnId) => {
      // remove the id's of the items that are selected
      const column = entities[columnId];
      const remainingTasks = column.tasks.filter((task) => !Boolean(orderedSelectedTasks.find(tasksel => tasksel.name === task.name)));
      previous[columnId].tasks = remainingTasks;
      return previous;
    },
    entities,
  );
  

  const finalCol = withRemovedTasks[destination.droppableId];
  const sourceCol = entities[source.droppableId];
  const withInserted = (() => {
    const base: {} = {...finalCol};
    base.tasks.splice(insertAtIndex, 0, ...orderedSelectedTasks);
    return base;
  })();

  // insert all selected tasks into final column
  const withAddedTasks = {
    ...withRemovedTasks,
    [startSave.id] : startSave,
    [finalCol.id]: withNewTasks(finalCol, withInserted.tasks),
  };

  const updated = {
    ...entities,
    ...withAddedTasks,
  };
  const fromto = (source.droppableId === destination.droppableId)? [withInserted, withInserted] : [startSave, withInserted];
  return {
    ...updated,
    selectedTasks:  orderedSelectedTasks,
    fromTo: fromto
  };
};
