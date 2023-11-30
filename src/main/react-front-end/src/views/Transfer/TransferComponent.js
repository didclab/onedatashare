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

 import { store } from '../../App';
 import BrowseModuleComponent from './BrowseModuleComponent';
 // import BrowserSlice from "./BrowserSlice";
 import Button from '@material-ui/core/Button';
 import Typography from '@material-ui/core/Typography';
 
 import FormControl from '@material-ui/core/FormControl';
 import Radio from '@material-ui/core/Radio';
 import RadioGroup from '@material-ui/core/RadioGroup';
 import FormControlLabel from '@material-ui/core/FormControlLabel';
 import Checkbox from "@material-ui/core/Checkbox";
 import FormLabel from '@material-ui/core/FormLabel';
 import {Hidden, Container, Box, TextField, Grid, Snackbar, Fade } from "@material-ui/core";
 import Accordion from "@material-ui/core/Accordion";
 import AccordionSummary from "@material-ui/core/AccordionSummary";
 import AccordionDetails from "@material-ui/core/AccordionDetails";
 
 import Divider from "@material-ui/core/Divider";
 import { DateTimePicker } from '@mui/x-date-pickers';
 import { LocalizationProvider } from '@mui/x-date-pickers';
 import { AdapterDayjs } from '@mui/x-date-pickers/AdapterDayjs'
 import dayjs from 'dayjs';
 
 import {KeyboardArrowRightRounded, KeyboardArrowLeftRounded, KeyboardArrowDownRounded, KeyboardArrowUpRounded, ExpandMore} from "@material-ui/icons";
 
 import { submitTransferRequest } from "../../APICalls/APICalls";
 import { endpointUpdate} from "../../model/actions";
 
 import { DragDropContext } from 'react-beautiful-dnd';
 import { mutliDragAwareReorder} from "./utils.js";
 
 import { getSelectedTasks, unselectAll, setDraggingTask, getEntities, setBeforeTransferReorder, getEndpointFromColumn, getSelectedTasksFromSide, longestCommonPrefix } from "./initialize_dnd.js";
 
 import { eventEmitter } from "../../App.js";
 import { formatType,getType, gridFullWidth, gridHalfWidth, isOAuth, showType} from "../../constants";
 
 
 
 import queryString from 'query-string';
 import { updateGAPageView } from '../../analytics/ga';
 import {styled} from "@material-ui/core/styles";
 
 export default class TransferComponent extends Component {
 
   constructor(props) {
     super(props);
     this.state = {
       endpoint1: store.getState().endpoint1,
       endpoint2: store.getState().endpoint2,
       mode1: 0,
       mode2: 0,
       history: [],
       settings: {
         optimizer: localStorage.hasOwnProperty("optimizer") ? localStorage.getItem("optimizer") : "None",
         overwrite: localStorage.hasOwnProperty("overwrite") ? JSON.parse(localStorage.getItem("overwrite")) : true,
         verify: localStorage.hasOwnProperty("verify") ? JSON.parse(localStorage.getItem("verify")) : true,
         encrypt: localStorage.hasOwnProperty("encrypt") ? JSON.parse(localStorage.getItem("encrypt")) : true,
         compress: localStorage.hasOwnProperty("compress") ? JSON.parse(localStorage.getItem("compress")) : true,
         retry: localStorage.hasOwnProperty("retry") ? Number(localStorage.getItem("retry")) : 5,
         concurrencyThreadCount:localStorage.hasOwnProperty("concurrencyThreadCount")?Number(localStorage.getItem("concurrencyThreadCount")):1,
         pipeSize:localStorage.hasOwnProperty("pipeSize")?Number(localStorage.getItem("pipeSize")):1,
         chunkSize:localStorage.hasOwnProperty("chunkSize")?Number(localStorage.getItem("chunkSize")):10400000,
         parallelThreadCount:localStorage.hasOwnProperty("parallelThreadCount")?Number(localStorage.getItem("parallelThreadCount")):1,
         scheduledTime: new Date().toISOString(),
       },
       compact: store.getState().compactViewEnabled,
       notif: false,
       isMessageVisible:false
     }
 
     this.unsubcribe = store.subscribe(() => {
       this.setState({
         endpoint1: store.getState().endpoint1,
         endpoint2: store.getState().endpoint2,
       });
     });
 
     this.printError = this.printError.bind(this);
     this.updateDimensions = this.updateDimensions.bind(this);
     this._returnBrowseComponent1 = this._returnBrowseComponent1.bind(this);
     this._returnBrowseComponent2 = this._returnBrowseComponent2.bind(this);
     this.updateBrowseOne = this.updateBrowseOne.bind(this);
     this.updateBrowseTwo = this.updateBrowseTwo.bind(this);
     this.sendFile = this.sendFile.bind(this);
     this.onSendToRight = this.onSendToRight.bind(this);
     this.onSendToLeft = this.onSendToLeft.bind(this);
     this.setDate = this.setDate.bind(this);
 
     this.printError();
 
     updateGAPageView();
 
   }
 
   labelStyle = () => styled(Typography)({
     fontSize: "12px",
     color: "black",
     "@media only screen and (max-width: 600px)":{
       fontSize: "16px"
     }
   })
 
 
   headerStyle = () => styled(Typography)({
     fontSize: "15px",
     "@media only screen and (max-width: 600px)":{
       fontSize: "20px"
     }
   })
 
   fieldLabelStyle = () => styled(Typography)({
     fontSize: "12px"
   })
 
   printError() {
     const error = queryString.parse(this.props.location.search);
     if (error && error["error"])
       setTimeout(() => {
         eventEmitter.emit("errorOccured", error["error"]);
       }, 500);
   }
 
   componentDidMount() {
     document.title = "OneDataShare - Transfer";
     window.addEventListener("resize", this.updateDimensions);
     // this.setState({ width: window.innerWidth, height: window.innerHeight });
     this.setState({ compact: store.getState().compactViewEnabled });
   }

   setDate = (new_date) => {
    const date = new Date(new_date);
    const iso8601_conversion = date.toISOString();
    this.setState({ settings: { ...this.state.settings, scheduledTime: iso8601_conversion } })
  }
 
   sendFile = (processed) => {
     if (processed.selectedTasks.length === 0) {
       eventEmitter.emit("errorOccured", "You did not select any files!");
       return 0;
     }
     const endpointSrc = getEndpointFromColumn(processed.fromTo[0])
     const endpointDest = getEndpointFromColumn(processed.fromTo[1])
     const options = this.state.settings;
 
     let sType = formatType(endpointSrc.credential.type!=null?endpointSrc.credential.type:getType(endpointSrc))
     let dType = formatType(endpointDest.credential.type!=null?endpointDest.credential.type:getType(endpointDest))
 
     let sourceParent = ""
     let destParent = ""
     let infoList= []
     let sourceCredId =""
     let destCredId = ""
     
     // Populate source object
     if(isOAuth[showType[sType]]){
       sourceParent = sType!== "box" ?"":"0"
       sourceCredId = endpointSrc.credential.uuid
       processed.selectedTasks.forEach(x=>{
         infoList.push({id:x.id,size:x.size, path:x.id})
       }
       )
     } 
     else if (endpointSrc?.uri === showType.vfs) {
       sourceCredId = endpointSrc?.credential?.credId
       sourceParent = Array.isArray(processed.fromTo[0].path) ? "" : processed.fromTo[0].path
       processed.selectedTasks.forEach(x=>{
        infoList.push({id:x.id, size:x.size, path:x.id})
       })
     }
     else if (sType === "http") {
      console.log("HTTP Here!")
      sourceParent = longestCommonPrefix(processed.fromTo[0].selectedTasks.map(x=>x.id))
      console.log(sourceParent)


      // Logic to evaluate whether path is directory or a file

      // Path is a file we set sourceParent to root directory
      if (sourceParent.lastIndexOf("/") == 0) {
        sourceParent = "/"
      }
      // Path is a directory we set sourceParent to the directory path
      else {
        sourceParent = sourceParent.substring(0, sourceParent.lastIndexOf("/"))
      }
      console.log(sourceParent)
      sourceCredId = endpointSrc.credential.credId
      processed.selectedTasks.forEach(x=>infoList.push({id:x.name , size:x.size, path:x.id}))
     }
     else{
       sourceParent = longestCommonPrefix(processed.fromTo[0].selectedTasks.map(x=>x.id))
       console.log(sourceParent)
       sourceParent = sourceParent.includes(".") ? sourceParent.substr(0,sourceParent.lastIndexOf("/"))+(sourceParent!=="")? "":"/" : sourceParent
       sourceCredId = endpointSrc.credential.credId
       processed.selectedTasks.forEach(x=>infoList.push({id:x.name , size:x.size, path:x.id}))
     }
     
     // Populate destination object
     if(isOAuth[showType[dType]]){
       let ids = processed.fromTo[1].ids
       let lastId = ids[ids.length - 1]
       if (processed.fromTo[1].selectedTasks.length !== 0) {
         destParent = processed.fromTo[1].selectedTasks[0].id
       } else {
         destParent = lastId || (dType !== "box" ? "" : "0")
       }
       destCredId = endpointDest.credential.uuid
     } else if (endpointDest?.uri === showType.vfs) {
       destParent = Array.isArray(processed.fromTo[1].path) ? "" : processed.fromTo[1].path
       destCredId = endpointDest?.credential?.credId
     }
     else{
       destParent = longestCommonPrefix(processed.fromTo[1].selectedTasks.map(x=>x.id))
       destParent = destParent.includes(".") ? destParent.substr(0,destParent.lastIndexOf("/"))+"/":destParent
       destCredId = endpointDest.credential.credId
     }
     let source = {
       credId:sourceCredId,
       type:sType,
       fileSourcePath: sourceParent,
       resourceList: infoList
     }
     let destination={
       credId:destCredId,
       type:dType,
       fileDesinationPath: destParent,
     }

     var optionParsed = {}
     Object.keys(options).forEach((v)=>{
       var value = options[v];
       if (value === "true" || value === "false") {
         value = JSON.parse(value)
       }
       optionParsed[v] = value
     })
     console.log({source, destination, optionParsed})
     submitTransferRequest(source,destination, optionParsed, (response) => {
       eventEmitter.emit("messageOccured", "Transfer initiated! Please visit the queue page to monitor the transfer");
       setBeforeTransferReorder(processed);
       this.setState({isMessageVisible:true,message:"Job submitted"});
       unselectAll()
     }, (error) => {
       eventEmitter.emit("errorOccured", error);
     })
 
 
   };
 
   updateDimensions() {
     // const width = this.state.width;
 
     // if screen size exceed certain treshhold
     // if ((width > 760 && screenIsSmall()) || (width <= 760 && !screenIsSmall())) {
     //   this.setState({ width: window.innerWidth, height: window.innerHeight });
     // }
   }
 
 
 
   _returnBrowseComponent1() {
     const { mode1, endpoint1, history, compact } = this.state;
     return <BrowseModuleComponent
         id="browserleft"
         mode={mode1}
         endpoint={endpoint1}
         history={history}
         displayStyle={compact ? "compact" : "comfort"}
         update={this.updateBrowseOne} />
   }
 
   _returnBrowseComponent2() {
     const { mode2, endpoint2, history, compact } = this.state;
 
     return <BrowseModuleComponent
         id="browserright"
         mode={mode2}
         endpoint={endpoint2}
         history={history}
         displayStyle={compact ? "compact" : "comfort"}
         update={this.updateBrowseTwo}
     />
   }
 
   updateBrowseOne(object) {
     if (object.mode === undefined) {
       object.mode = 0
     }
     this.setState({ endpoint1: object.endpoint || this.state.endpoint1, mode1: object.mode })
     if (object.endpoint)
       store.dispatch(endpointUpdate("left", { ...this.state.endpoint1, ...object.endpoint }));
     }
 
   updateBrowseTwo(object) {
     if (object.mode === undefined) {
       object.mode = 0
     }
     this.setState({ endpoint2: object.endpoint || this.state.endpoint2, mode2: object.mode });
     if (object.endpoint)
       store.dispatch(endpointUpdate("right", { ...this.state.endpoint2, ...object.endpoint }));
   }
 
   onDragStart = (start) => {
     var task = JSON.parse(start.draggableId.slice(start.draggableId.indexOf(" ")));
     var selectedSide = start.source.droppableId;
     const selected = getSelectedTasks()[selectedSide].find(
         (listTask) => listTask.name === task.name,
     );
 
     // if dragging an item that is not selected - unselect all items
     if (!selected) {
       unselectAll();
     }
     setDraggingTask(task);
   };
 
   onDragEnd = (result) => {
     const destination = result.destination;
     const source = result.source;
     // nothing to do
 
     if (!destination || result.reason === 'CANCEL') {
       setDraggingTask(null);
       return;
     }
     // console.log(getSelectedTasks(), result.source, result.destination)
     const processed = mutliDragAwareReorder({
       entities: getEntities(),
       selectedTasks: getSelectedTasks(),
       source,
       destination,
     });
 
     if (processed.fromTo[0] === processed.fromTo[1]) {
       setBeforeTransferReorder(processed);
     } else {
       this.sendFile(processed);
     }
 
     setDraggingTask(null)
   };
 
 
   onSendToRight() {
 
     /*
     const processed: ReorderResult = mutliDragAwareReorder({
       entities: getEntities(),
       selectedTasks: getSelectedTasks(),
 
       {droppableId: "left"},
       {droppableId: "right"},
     });
 
     if(processed.fromTo[0] == processed.fromTo[1]){
       setBeforeTransferReorder(processed);
     }else{
       this.sendFile(processed);
     }*/
 
     const entity = getEntities();
     const processed = {
       fromTo: [entity.left, entity.right],
       selectedTasks: getSelectedTasksFromSide({ side: "left" })
     }
     this.sendFile(processed);
   }
   onSendToLeft() {
     const entity = getEntities();
     const processed = {
       fromTo: [entity.right, entity.left],
       selectedTasks: getSelectedTasksFromSide({ side: "right" })
     }
 
     // console.log(processed);
     this.sendFile(processed);
   }
 
   getSettingComponent() {
     const handleChange = (name) => event => {
       var value = event.target.value;
       console.log(value);
       this.setState({ settings: { ...this.state.settings, [name]: value } });
     };
     const handleChangeCheckbox = (name) => event => {
       var value = event.target.checked;
       this.setState({ settings: { ...this.state.settings, [name]: value } });
     };
     const handleChangeRetry = (event/*, value*/) => {
       var value = event.target.value;
       if(value >= 10){
         value = 10;
         event.target.value = 10;
       }else if(value <= 0){
         value = 0;
         event.target.value = 0;
       }
       this.setState({ settings: { ...this.state.settings, retry: value } });    
     }
 
     const setDefault = () => {
       const settings = this.state.settings;
       for(let [key, val] of Object.entries(settings)){
         localStorage.setItem(key, String(val));
       }
       this.setState({notif: true});
       setTimeout(() => {
         this.setState({notif: false})
       },1000);
     }
 
     const desktopWidth = 4;
 
     const tabletWidth = 4;
     const ToggleLabel = this.labelStyle();
     const ToggleHeader = this.headerStyle();
     const FieldLabel = this.fieldLabelStyle();
     return (
         <Container>
       <Grid container className="innerBox" direction="row" align-items="flex-start" justifyContent="center" spacing={2} style={{paddingLeft: "20px"}}>
         <Grid item md={desktopWidth} sm={tabletWidth}>
           <FormControl component="fieldset" >
             <FormLabel component="legend" ><ToggleHeader>Optimization</ToggleHeader></FormLabel>
             <RadioGroup
                 aria-label="Optimization"
                 value={this.state.settings.optimizer}
                 onChange={handleChange("optimizer")}
             >
               <FormControlLabel value="None" control={<Radio />} label={<ToggleLabel>None</ToggleLabel>} />
               <FormControlLabel value="VDA2C" control={<Radio />} label={<ToggleLabel>Value Decomposition Actor Critic (VDA2C)</ToggleLabel>}  />
               <FormControlLabel value="BO" control={<Radio />} label={<ToggleLabel>Bayesian Optimization (BO)</ToggleLabel>}  />
               <FormControlLabel value="SGD" control={<Radio />} label={<ToggleLabel>Stochastic Gradient Descent (SGD)</ToggleLabel>}  />
               <FormControlLabel value="MADDPG" control={<Radio />} label={<ToggleLabel>Multi-Agent DDPG (MADDPG)</ToggleLabel>}  />
               <FormControlLabel value="PPO" control={<Radio />} label={<ToggleLabel>Proximal Policy Optimization (PPO)</ToggleLabel>}  />
               <FormControlLabel value="DDPG" control={<Radio />} label={<ToggleLabel>Deep Deterministic Policy Gradient (DDPG)</ToggleLabel>}  />
 
             </RadioGroup>
           </FormControl>
         </Grid>
 
 
         {/* checkbox version */}
         {/*direction={"column"} justifyContent={"center"}*/}
         <Grid item container direction={"row"} justifyContent={"space-evenly"} md={desktopWidth} sm={tabletWidth} >
             <Grid item sm={gridFullWidth} xs={5}>
               <FormControlLabel
                   control=
                       {<Checkbox
                           aria-label="Overwrite"
                           checked={this.state.settings.overwrite}
                           onChange={handleChangeCheckbox("overwrite")}
                       />}
                   label={<ToggleLabel>Overwrite</ToggleLabel>}
               />
             </Grid>
             <Grid item  sm={gridFullWidth} xs={5}>
               <FormControlLabel
                   control=
                       {<Checkbox
                           aria-label="Integrity"
                           checked={this.state.settings.verify}
                           onChange={handleChangeCheckbox("verify")}
                       />}
                   label={<ToggleLabel>Integrity</ToggleLabel>}
               />
             </Grid>
             <Grid item sm={gridFullWidth} xs={5}>
               <FormControlLabel
                   control=
                       {<Checkbox
                           aria-label="Encrypt"
                           checked={this.state.settings.encrypt}
                           onChange={handleChangeCheckbox("encrypt")}
                       />}
                   label={<ToggleLabel>Encrypt</ToggleLabel>}
               />
             </Grid>
             <Grid item  sm={gridFullWidth} xs={5}>
               <FormControlLabel
                   control=
                       {<Checkbox
                           aria-label="Compress"
                           checked={this.state.settings.compress}
                           onChange={handleChangeCheckbox("compress")}
                       />}
                   label={<ToggleLabel>Compress</ToggleLabel>}
               />
             </Grid>
 
         </Grid>
 
 
         <Grid item md={desktopWidth} sm={tabletWidth}>
           <FormControl component="fieldset">
             <FormLabel component="legend"><ToggleHeader>Retries</ToggleHeader></FormLabel>
             <TextField
                 id="outlined-number"
                 label={<FieldLabel>Retries</FieldLabel>}
                 type="number"
                 InputLabelProps={{
                   shrink: true,
                 }}
                 variant="outlined"
                 onChange={handleChangeRetry}
                 value={this.state.settings.retry}
             />
             <FormLabel style={{ marginTop: "20px", fontSize: "20px" }}>{this.state.settings.retry} Times</FormLabel>
           </FormControl>
         </Grid>

         <Grid item md={desktopWidth} sm={tabletWidth}>
            <FormControl component="fieldset">
              <FormLabel component="legend"><ToggleHeader>Date</ToggleHeader></FormLabel>
                <LocalizationProvider dateAdapter={AdapterDayjs}>
                  <DateTimePicker viewRenderers={{hours: null, minutes: null,seconds: null}} label={<FieldLabel>Date</FieldLabel>} defaultValue={dayjs()} onChange={(e) => this.setDate(e)} minDate={dayjs()}/>
                </LocalizationProvider>
              </FormControl>
          </Grid>
 
         <Grid item md={desktopWidth} sm={tabletWidth}>
           <FormControl component="fieldset">
             <FormLabel component="legend"><ToggleHeader>Concurrency Thread Count</ToggleHeader></FormLabel>
             <TextField
                 id="outlined-number"
                 label={<FieldLabel>Concurrency Thread Count</FieldLabel>}
                 type="number"
                 InputLabelProps={{
                   shrink: true,
                 }}
                 variant="outlined"
                 onChange={handleChange("concurrencyThreadCount")}
                 value={this.state.settings.concurrencyThreadCount}
             />
           </FormControl>
         </Grid>
         <Grid item md={desktopWidth} sm={tabletWidth}>
           <FormControl component="fieldset">
             <FormLabel component="legend"><ToggleHeader>Parallel Thread Count</ToggleHeader></FormLabel>
             <TextField
                 id="outlined-number"
                 label={<FieldLabel>Parallel Thread Count</FieldLabel>}
                 type="number"
                 InputLabelProps={{
                   shrink: true,
                 }}
                 variant="outlined"
                 onChange={handleChange("parallelThreadCount")}
                 value={this.state.settings.parallelThreadCount}
             />
           </FormControl>
         </Grid>
         <Grid item md={desktopWidth} sm={tabletWidth}>
           <FormControl component="fieldset">
             <FormLabel component="legend"><ToggleHeader>Pipe Size</ToggleHeader></FormLabel>
             <TextField
                 id="outlined-number"
                 label={<FieldLabel>Pipe Size</FieldLabel>}
                 type="number"
                 InputLabelProps={{
                   shrink: true,
                 }}
                 variant="outlined"
                 onChange={handleChange("pipeSize")}
                 value={this.state.settings.pipeSize}
             />
           </FormControl>
         </Grid>
         <Grid item md={desktopWidth} sm={tabletWidth}>
           <FormControl component="fieldset">
             <FormLabel component="legend"><ToggleHeader>Chunk Size <sub style={{top: "0",bottom: "0",lineHeigh: "1",fontSize: ".7em",verticalAlign: "middle"}}>(bytes)</sub></ToggleHeader></FormLabel>
             <TextField
                 id="outlined-number"
                 label={<FieldLabel>Chunk Size</FieldLabel>}
                 type="number"
                 InputLabelProps={{
                   shrink: true,
                 }}
                 variant="outlined"
                 onChange={handleChange("chunkSize")}
                 value={this.state.settings.chunkSize}
             />
           </FormControl>
         </Grid>
       </Grid>
           <Divider/>
     <Grid container justifyContent={'center'}>
       <Grid item>
         <FormControlLabel
             control=
                 {
                   <Button
                     aria-label="Set as Default"
                     style={{backgroundColor: "#172753", color: "white", marginTop: "10px"}}
                     onClick={setDefault}
                     // checked={this.state.settings.default}
                     // onChange={handleChangeCheckbox("default")}
                 >
                   Set as Default
                 </Button>
                   }
         />
         {/*<Snackbar open={this.state.notif} autoHideDuration={1000} onClose={closeNotif} message={"Default Set!"}/>*/}
         <Fade in={this.state.notif}><Typography style={{paddingLeft: "5px"}}>Default Set!</Typography></Fade>
       </Grid>
     </Grid>
         </Container>
     // </Box>
     );
   };
 
   handleMessageClose=()=>{
     this.setState({isMessageVisible:false})
   }
   render() {
     // const isSmall = screenIsSmall();
     // const isSmall = false;
     // const panelStyle = { height: "auto", margin: isSmall ? "10px" : "0px" };
     // const headerStyle = { textAlign: "center" }
 
     // Tooltip
 
     const ToggleLabel = this.labelStyle();
 
     return (
         <div className={"outeractionContainer"}>
         <Grid container direction="column" justifyContent={"center"}>
           <Container className={"actionContainer"}>
 
             {/*{!isSmall &&*/}
             <Box className={"wrapperBox"}>
               {/*<FormControlLabel*/}
               {/*    className={"wrapperBoxForm"}*/}
               {/*    control={*/}
               {/*      <Switch*/}
               {/*          color="default"*/}
               {/*          style={{colorPrimary: "white", colorSecondary: "white"}}*/}
               {/*          checked={this.state.compact}*/}
               {/*          onChange={updateCompactViewPreference('compact')}*/}
               {/*          value="compact"*/}
               {/*      />*/}
               {/*    }*/}
               {/*    label={<ToggleLabel>Compact</ToggleLabel>}*/}
               {/*/>*/}
 
               <Box>
                 <Grid container direction="row" justifyContent="center" spacing={2}>
                   <DragDropContext
                       onDragStart={this.onDragStart}
                       onDragEnd={this.onDragEnd}>
                     <Grid item md={gridHalfWidth} xs={gridFullWidth}>
                       {this._returnBrowseComponent1()}
                       {/*<h6>Source</h6>*/}
                     </Grid>
                     <Hidden mdUp>
                       <Grid container item direction="row" align-items="center" justifyContent="center">
                         <Grid item>
                           <Button className={"sendButton"} id="sendFromRightToLeft" onClick={this.onSendToLeft}>
                             <KeyboardArrowUpRounded />
                             Send
                           </Button>
                         </Grid>
                         <Grid item>
                           <Button className={"sendButton"} id="sendFromLeftToRight" onClick={this.onSendToRight}>
                             Send<KeyboardArrowDownRounded/>
                           </Button>
                         </Grid>
                       </Grid>
                     </Hidden>
                     {/*<Grid item md={1} xs={gridFullWidth}>*/}
                     {/*  <Button id="sendFromLeftToRight" onClick={this.onSendToRight}> Send<KeyboardArrowRightRounded /></Button>*/}
                     {/*</Grid>*/}
                     <Grid item md={gridHalfWidth} xs={gridFullWidth}>
                       {this._returnBrowseComponent2()}
                       {/*<h6>Destination</h6>*/}
                     </Grid>
                   </DragDropContext>
                 </Grid>
 
                 <Hidden smDown>
                   <Grid container direction="row" align-items="center" justifyContent="center">
                     <Grid item>
                       <Button className={"sendButton"} id="sendFromRightToLeft" onClick={this.onSendToLeft}> <KeyboardArrowLeftRounded/>    Send</Button>
                     </Grid>
                     <Grid item>
                       <Button className={"sendButton"} id="sendFromLeftToRight" onClick={this.onSendToRight}> Send<KeyboardArrowRightRounded /></Button>
                     </Grid>
 
                   </Grid>
                 </Hidden>
                 <Snackbar open={this.state.isMessageVisible}
                  message={this.state.message}
                  key={"message"}
                  anchorOrigin={{
                   vertical: 'top',
                   horizontal: 'center',
                 }}                 
                 autoHideDuration={2000}
                 onClose={this.handleMessageClose}/>
                 <Accordion style={{marginTop: "10px"}}>
                   <AccordionSummary
                       expandIcon={<ExpandMore />}
                       aria-controls="panel1a-content"
                       id="panel1a-header"
                   >
                     <ToggleLabel>Transfer Settings</ToggleLabel>
                   </AccordionSummary>
                   <AccordionDetails>
                     {this.getSettingComponent()}
                   </AccordionDetails>
                 </Accordion>
 
 
               </Box>
 
             </Box>
             
 
 
 
         </Container>
       </Grid>
         </div>
     );
   }
 }
 