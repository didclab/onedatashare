import React, { Component } from 'react';
import { Panel, Col, Row, Glyphicon} from 'react-bootstrap';

import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import PropTypes from 'prop-types';
import {store} from '../../App';
import BrowseModuleComponent from './BrowseModuleComponent';
import Button from '@material-ui/core/Button';
import Paper from '@material-ui/core/Paper';
import Typography from '@material-ui/core/Typography';
import Select from '@material-ui/core/Select';
import MenuItem from '@material-ui/core/MenuItem';
import InputLabel from '@material-ui/core/InputLabel';

import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';

import FormControl from '@material-ui/core/FormControl';
import Radio from '@material-ui/core/Radio';
import RadioGroup from '@material-ui/core/RadioGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormLabel from '@material-ui/core/FormLabel';

import {submit} from "../../APICalls/APICalls";
import {endpointUpdate} from "../../model/actions";

import { DragDropContext} from 'react-beautiful-dnd';
import {mutliDragAwareReorder, screenIsSmall } from "./utils.js";
import {getSelectedTasks, unselectAll, setDraggingTask, getEntities, setBeforeTransferReorder, makeFileNameFromPath, getEndpointFromColumn, getSelectedTasksFromSide, getCurrentFolderId} from "./initialize_dnd.js";

import {eventEmitter} from "../../App.js";
import Slider from '@material-ui/lab/Slider';

import Switch from '@material-ui/core/Switch';

import ErrorMessagesConsole from '../ErrorMessagesConsole';

export default class TransferComponent extends Component {

  constructor(){
    super();

    this.state = {
      endpoint1: store.getState().endpoint1,
      endpoint2: store.getState().endpoint2,
      mode1: 0,
      mode2: 0,
      history: [],
      width: window.innerWidth, 
      height: window.innerHeight,
      settings:{
        optimizer: "None",
        overwrite: "true",
        verify: "true",
        encrypt: "true",
        compress: "true",
        retry: 5
      },
      compact: true
    }

    this.unsubcribe = store.subscribe(() => {
        this.setState({
          endpoint1: store.getState().endpoint1,
          endpoint2: store.getState().endpoint2,
        });
    });
    this.updateDimensions = this.updateDimensions.bind(this);
    this._returnBrowseComponent1 = this._returnBrowseComponent1.bind(this);
    this._returnBrowseComponent2 = this._returnBrowseComponent2.bind(this);
    this.updateBrowseOne = this.updateBrowseOne.bind(this);
    this.updateBrowseTwo = this.updateBrowseTwo.bind(this);
    this.sendFile = this.sendFile.bind(this);
    this.onSendToRight = this.onSendToRight.bind(this);
    this.onSendToLeft = this.onSendToLeft.bind(this);
  }

  sendFile = (processed) => {
    if(processed.selectedTasks.length == 0){
      eventEmitter.emit("errorOccured", "You did not select any files!");
      return 0;
    }
    const endpointSrc = getEndpointFromColumn(processed.fromTo[0])
    const endpointDest = getEndpointFromColumn(processed.fromTo[1])
    const options = this.state.settings;
    const srcUrls = [] 
    const fileIds = [] 
    const destUrls = []
    console.log(processed.selectedTasks);
    processed.selectedTasks.map((task) => {
      srcUrls.push(makeFileNameFromPath(endpointSrc.uri, processed.fromTo[0].path, task.name))
      fileIds.push(task.id);
      destUrls.push(makeFileNameFromPath(endpointDest.uri, processed.fromTo[1].path, task.name))
    });

    const destUrl = destUrls.reduce((a, v) => a+","+v)
    const srcUrl = srcUrls.reduce((a, v) => a+","+v)
    const fileId = fileIds.reduce((a, v) => a+","+v)

    const src = {
      credential:endpointSrc.credential,
      id: fileId,
      uri: encodeURI(srcUrl)
    }
    const dest = {
      credential:endpointDest.credential,
      id: getCurrentFolderId(endpointDest),
      uri: encodeURI(destUrl)
    }

    var optionParsed = {}
    Object.keys(options).map((v)=>{
      var value = options[v];
      if(value === "true" || value === "false"){
        value = JSON.parse(value)
      }
      optionParsed[v] = value
    })

    submit(src, endpointSrc, dest,endpointDest, optionParsed, (response)=>{
      setBeforeTransferReorder(processed);
      eventEmitter.emit("messageOccured", "Transfer Scheduled!")
    }, (error)=>{
      eventEmitter.emit("errorOccured", error);
    })

  };

  updateDimensions() {
    const width = this.state.width;
    
    // if screen size exceed certain treshhold
    if((width > 760 && screenIsSmall()) || (width <= 760 && !screenIsSmall())){
      this.setState({width: window.innerWidth, height: window.innerHeight});
    }
  }

  componentDidMount(){
    document.title = "OneDataShare - Transfer";
    window.addEventListener("resize", this.updateDimensions);
    this.setState({width: window.innerWidth, height: window.innerHeight});
  }

  componentWillUnmount(){

    document.title = "OneDataShare - Home";
//    this.unsubcribe();
  }

  _returnBrowseComponent1(){
     const {mode1, endpoint1,history, compact} = this.state;
     console.log("asdsaDAS\n\n\n")
    return <BrowseModuleComponent 
      id="browserleft"
      mode={mode1} 
      endpoint={endpoint1} 
      history={history} 
      displayStyle={compact ? "compact" : "comfort"}
      update={this.updateBrowseOne}/>
  }

  _returnBrowseComponent2(){
     const {mode2, endpoint2, history, compact} = this.state;
   
    return <BrowseModuleComponent 
      id="browserright"
      mode={mode2}
      endpoint={endpoint2} 
      history={history} 
      displayStyle={compact ? "compact" : "comfort"}
      update={this.updateBrowseTwo}
    />
  }

  updateBrowseOne(object){
      if(object.mode == undefined){
        object.mode = 0
      }
      this.setState({endpoint1: object.endpoint || this.state.endpoint1, mode1: object.mode});
      if(object.endpoint)
        store.dispatch(endpointUpdate(object.endpoint.side, {...this.state.endpoint1, ...object.endpoint}));
  }

  updateBrowseTwo(object){
      if(object.mode == undefined){
        object.mode = 0
      }
      this.setState({endpoint2: object.endpoint || this.state.endpoint2, mode2: object.mode});
      if(object.endpoint)
        store.dispatch(endpointUpdate(object.endpoint.side, {...this.state.endpoint2, ...object.endpoint}));
  }

  onDragStart = (start: DragStart) => {
    var task = JSON.parse(start.draggableId.slice(start.draggableId.indexOf(" ")));
    var selectedSide = start.source.droppableId;
    const selected = getSelectedTasks()[selectedSide].find(
      (listTask): boolean => listTask.name === task.name,
    );

    // if dragging an item that is not selected - unselect all items
    if (!selected) {
      unselectAll();
    }
    setDraggingTask(task);
  };

  onDragEnd = (result: DropResult) => {
    const destination: ?DraggableLocation = result.destination;
    const source: DraggableLocation = result.source;
    // nothing to do

    if (!destination || result.reason === 'CANCEL') {
      setDraggingTask(null);
      return;
    }
    console.log(getSelectedTasks(), result.source, result.destination)
    const processed: ReorderResult = mutliDragAwareReorder({
      entities: getEntities(),
      selectedTasks: getSelectedTasks(),
      source,
      destination,
    });

    if(processed.fromTo[0] == processed.fromTo[1]){
      setBeforeTransferReorder(processed);
    }else{  
      this.sendFile(processed);
    }
    
    setDraggingTask(null)
  };

  
  onSendToRight(){

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
      selectedTasks: getSelectedTasksFromSide({side: "left"})
    }
    this.sendFile(processed);
  }
  onSendToLeft(){
    const entity = getEntities();
    const processed = {
      fromTo: [entity.right, entity.left],
      selectedTasks: getSelectedTasksFromSide({side: "right"})
    }

    console.log(processed)
    this.sendFile(processed);
  }

  getSettingComponent(isSmall){
    const handleChange = (name) => event => {
      var value = event.target.value;
      this.setState({settings:{ ...this.state.settings, [name]: value }});
    };
    const handleChangeRetry = (event, value) => {
      this.setState({settings:{ ...this.state.settings, retry: value }});
    }
    const formlabelstyle = {fontSize: "15px"}
    const formStyle = {marginLeft: "5%", marginRight: "5%"}
    return <Panel bsStyle="primary">
              <Panel.Heading>Transfer Setting</Panel.Heading>
              <Panel.Body key={isSmall} style={{overflow: "hidden"}}>
                <FormControl component="fieldset" style={formStyle}>
                  <FormLabel component="legend" style={formlabelstyle}>Optimization</FormLabel>
                  <RadioGroup
                    aria-label="Optimization"
                    value={this.state.settings.optimizer}
                    onChange={handleChange("optimizer")}
                  >
                    <FormControlLabel value="None" control={<Radio />} label="None" />
                    <FormControlLabel value="2nd Order" control={<Radio />} label="2nd Order" />
                    <FormControlLabel value="PCP" control={<Radio />} label="PCP" />
                  </RadioGroup>
                </FormControl>

                <FormControl component="fieldset" style={formStyle}>
                  <FormLabel component="legend" style={formlabelstyle}>Overwrite</FormLabel>
                  <RadioGroup
                    aria-label="Overwrite"
                    value={this.state.settings.overwrite}
                    onChange={handleChange("overwrite")}
                  >
                    <FormControlLabel value="true" control={<Radio />} label="True" />
                    <FormControlLabel value="false" control={<Radio />} label="False"/>
                  </RadioGroup>
                </FormControl>
                <FormControl component="fieldset" style={formStyle}>
                  <FormLabel component="legend" style={formlabelstyle}>Integrity</FormLabel>
                  <RadioGroup
                    aria-label="Integrity"
                    value={this.state.settings.verify}
                    onChange={handleChange("verify")}
                  >
                    <FormControlLabel value="true" control={<Radio />} label="True" />
                    <FormControlLabel value="false" control={<Radio />} label="False"/>
                  </RadioGroup>
                </FormControl>

                <FormControl component="fieldset" style={formStyle}>
                  <FormLabel component="legend" style={formlabelstyle}>Encrypt</FormLabel>
                  <RadioGroup
                    aria-label="Encrypt"
                    value={this.state.settings.encrypt}
                    onChange={handleChange("encrypt")}
                  >
                    <FormControlLabel value="true" control={<Radio />} label="True" />
                    <FormControlLabel value="false" control={<Radio />} label="False"/>
                  </RadioGroup>
                </FormControl>

                <FormControl component="fieldset" style={formStyle}>
                  <FormLabel component="legend" style={formlabelstyle}>Compress</FormLabel>
                  <RadioGroup
                    aria-label="Compress"
                    value={this.state.settings.compress}
                    onChange={handleChange("compress")}
                  >
                    <FormControlLabel value="true" control={<Radio />} label="True" />
                    <FormControlLabel value="false" control={<Radio />} label="False"/>
                  </RadioGroup>
                </FormControl>

                <FormControl component="fieldset">
                  <FormLabel component="legend" style={formlabelstyle}>Retry Counts</FormLabel>
                  <Slider

                    value={this.state.settings.retry}
                    min={0}
                    max={10}
                    step={1}
                    onChange={handleChangeRetry}
                  />
                  <FormLabel style={{marginTop: "20px", fontSize: "20px"}}>{this.state.settings.retry} Times</FormLabel>
                </FormControl>
              </Panel.Body>
              
            </Panel>
  }
  render() {

    const isSmall = screenIsSmall();
    const panelStyle = { height: "auto", margin: isSmall? "10px": "0px"};
    let handleChange = name => event => {
      this.setState({ [name]: event.target.checked });
    };

    const { alignment } = this.state;
    console.log(alignment)
    return (
      <div style={{display: "flex", flexDirection: 'row', justifyContent: 'center', paddingTop: '20px'}}>
        <Col xs={11} style={{ display: "flex",justifyContent: 'center', flexDirection: 'column'}}>
          
          {!isSmall && 
          <Panel bsStyle="primary">
          <FormControlLabel
            style={{width: "200px", right: "10px", color: "white", position: "absolute"}}
            control={
              <Switch
                color="default"
                style={{colorPrimary: "white", colorSecondary:"white"}}
                checked={this.state.compact}
                onChange={handleChange('compact')}
                value="compact"
              />
            }
            label={<Typography style={{color: "white", fontSize: "12px"}}>Compact</Typography>}
          />
          <Panel.Heading>
            <p>
              Browse and Transfer Files
            </p>
            

            </Panel.Heading>
            <Panel.Body key={isSmall} style={{overflow: "hidden"}}>
                <Row style={{flexDirection: 'column'}}>
                  <DragDropContext 
                    onDragStart={this.onDragStart}
                    onDragEnd={this.onDragEnd}
                  >
                  <Col xs={6} style={panelStyle}  >
                    {this._returnBrowseComponent1()}
                  </Col>
                  <Col xs={6} style={panelStyle} >
                    {this._returnBrowseComponent2()}  
                  </Col>
                  </DragDropContext>
                </Row>
                <Row style={{display: 'block', }}>
                    <Button style={{padding: '15px', marginRight: '10px'}} onClick={this.onSendToLeft}> <Glyphicon glyph="arrow-left" />    Send</Button>
                    <Button style={{padding: '15px', marginLeft: '10px'}} onClick={this.onSendToRight}> Send<Glyphicon glyph="arrow-right" /></Button>
                </Row>
            

              <ErrorMessagesConsole/>
            </Panel.Body>
          </Panel>

          
        }
        {/* !isSmall && this.getSettingComponent(isSmall) */}
        {isSmall &&
        <Panel bsStyle="primary">
        <FormControlLabel
                      style={{width: "200px", float: "right", color: "white"}}
                      control={
                        <Switch
                          color="default"
                          style={{colorPrimary: "white", colorSecondary:"white"}}
                          checked={this.state.compact}
                          onChange={handleChange('compact')}
                          value="compact"
                        />
                      }
                      label={<Typography style={{fontSize: "12px"}}>Compact</Typography>}
                    />
        <Panel.Heading>
          <p>
            Browse and Transfer Files


          </p>


        </Panel.Heading>


        <Panel.Body key={isSmall} style={{overflow: "hidden"}}>
            <Row style={{flexDirection: 'column'}}>
              <DragDropContext
                  onDragStart={this.onDragStart}
                  onDragEnd={this.onDragEnd}
              >
                <Col style={panelStyle}>
                  {this._returnBrowseComponent1()}
                </Col>
                <Row style={{display: 'flex', flexDirection: 'row', justifyContent: 'center' }}>
                  <Button style={{padding: '15px', marginRight: '10px'}} onClick={this.onSendToLeft}> <Glyphicon glyph="arrow-up" /> Send</Button>
                  <Button style={{padding: '15px', marginLeft: '10px'}} onClick={this.onSendToRight}> Send<Glyphicon glyph="arrow-down" /></Button>
                </Row>
                <Row style={panelStyle}>
                  {this._returnBrowseComponent2()}  
                </Row>
                
                 {/*  <Row> {this.getSettingComponent(isSmall)} </Row> */}
              </DragDropContext>
              
            </Row>
            <div> </div>
            <ErrorMessagesConsole/>
            </Panel.Body>
            </Panel>
          }

        </Col>
      </div>
        
    );
  }
}

