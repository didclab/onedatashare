import React, { Component } from 'react';
import { Panel, Col, Row, Glyphicon } from 'react-bootstrap';

import { store } from '../../App';
import BrowseModuleComponent from './BrowseModuleComponent';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';

import FormControl from '@material-ui/core/FormControl';
import Radio from '@material-ui/core/Radio';
import RadioGroup from '@material-ui/core/RadioGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import FormLabel from '@material-ui/core/FormLabel';

import { submit, updateViewPreference } from "../../APICalls/APICalls";
import { endpointUpdate, compactViewPreference } from "../../model/actions";

import { DragDropContext } from 'react-beautiful-dnd';
import { mutliDragAwareReorder, screenIsSmall } from "./utils.js";
import { getSelectedTasks, unselectAll, setDraggingTask, getEntities, setBeforeTransferReorder, makeFileNameFromPath, getEndpointFromColumn, getSelectedTasksFromSide, getCurrentFolderId } from "./initialize_dnd.js";

import { eventEmitter } from "../../App.js";
import Slider from '@material-ui/lab/Slider';

import Switch from '@material-ui/core/Switch';

import ErrorMessagesConsole from '../ErrorMessagesConsole';
import queryString from 'query-string';
import { updateGAPageView } from '../../analytics/ga';

export default class TransferComponent extends Component {

  constructor(props) {
    super(props);
    this.state = {
      endpoint1: store.getState().endpoint1,
      endpoint2: store.getState().endpoint2,
      mode1: 0,
      mode2: 0,
      history: [],
      width: window.innerWidth,
      height: window.innerHeight,
      settings: {
        optimizer: "None",
        overwrite: "true",
        verify: "true",
        encrypt: "true",
        compress: "true",
        retry: 5
      },
      compact: store.getState().compactViewEnabled
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

    this.printError();

    updateGAPageView();

  }

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
    this.setState({ width: window.innerWidth, height: window.innerHeight });
    this.setState({ compact: store.getState().compactViewEnabled });

  }

  sendFile = (processed) => {
    if (processed.selectedTasks.length === 0) {
      eventEmitter.emit("errorOccured", "You did not select any files!");
      return 0;
    }
    const endpointSrc = getEndpointFromColumn(processed.fromTo[0])
    const endpointDest = getEndpointFromColumn(processed.fromTo[1])
    const options = this.state.settings;
    const srcUrls = []
    const fileIds = []
    const destUrls = []
    processed.selectedTasks.map((task) => {
      srcUrls.push(makeFileNameFromPath(endpointSrc.uri, processed.fromTo[0].path, task.name))
      fileIds.push(task.id);
      destUrls.push(makeFileNameFromPath(endpointDest.uri, processed.fromTo[1].path, task.name))
    });

    var optionParsed = {}
    Object.keys(options).map((v) => {
      var value = options[v];
      if (value === "true" || value === "false") {
        value = JSON.parse(value)
      }
      optionParsed[v] = value
    })


    const src = {
      credential: endpointSrc.credential,
    }

    const dest = {
      credential: endpointDest.credential,
      id: getCurrentFolderId(endpointDest),
    }

    for (let i = 0; i < srcUrls.length; i++) {
      src["id"] = fileIds[i];
      src["uri"] = encodeURI(srcUrls[i]);
      dest["uri"] = encodeURI(destUrls[i]);

      submit(src, endpointSrc, dest, endpointDest, optionParsed, (response) => {
        eventEmitter.emit("messageOccured", "Transfer Initiated!")
        setBeforeTransferReorder(processed);
      }, (error) => {
        eventEmitter.emit("errorOccured", error);
      })
    }

  };

  updateDimensions() {
    const width = this.state.width;

    // if screen size exceed certain treshhold
    if ((width > 760 && screenIsSmall()) || (width <= 760 && !screenIsSmall())) {
      this.setState({ width: window.innerWidth, height: window.innerHeight });
    }
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
    this.setState({ endpoint1: object.endpoint || this.state.endpoint1, mode1: object.mode });
    if (object.endpoint)
      store.dispatch(endpointUpdate(object.endpoint.side, { ...this.state.endpoint1, ...object.endpoint }));
  }

  updateBrowseTwo(object) {
    if (object.mode === undefined) {
      object.mode = 0
    }
    this.setState({ endpoint2: object.endpoint || this.state.endpoint2, mode2: object.mode });
    if (object.endpoint)
      store.dispatch(endpointUpdate(object.endpoint.side, { ...this.state.endpoint2, ...object.endpoint }));
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
    console.log(getSelectedTasks(), result.source, result.destination)
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

    console.log(processed);
    this.sendFile(processed);
  }

  getSettingComponent(isSmall) {
    const handleChange = (name) => event => {
      var value = event.target.value;
      this.setState({ settings: { ...this.state.settings, [name]: value } });
    };
    const handleChangeRetry = (event, value) => {
      this.setState({ settings: { ...this.state.settings, retry: value } });
    }
    const formlabelstyle = { fontSize: "15px" }
    const formStyle = { marginLeft: "5%", marginRight: "5%" }
    return <Panel bsStyle="primary">
      <div style={{ textAlign: "center" }}>
        <Panel.Heading>Transfer Setting</Panel.Heading>
      </div>
      <Panel.Body key={isSmall} style={{ overflow: "hidden" }}>
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
            <FormControlLabel value="false" control={<Radio />} label="False" />
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
            <FormControlLabel value="false" control={<Radio />} label="False" />
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
            <FormControlLabel value="false" control={<Radio />} label="False" />
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
            <FormControlLabel value="false" control={<Radio />} label="False" />
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
          <FormLabel style={{ marginTop: "20px", fontSize: "20px" }}>{this.state.settings.retry} Times</FormLabel>
        </FormControl>
      </Panel.Body>

    </Panel>
  }

  render() {
    const isSmall = screenIsSmall();
    const panelStyle = { height: "auto", margin: isSmall ? "10px" : "0px" };
    const headerStyle = { textAlign: "center" }
    let handleChange = name => event => {
      this.setState({ [name]: event.target.checked });
    };

    let updateCompactViewPreference = name => event => {
      this.setState({ [name]: event.target.checked });
      let compactViewEnabled = event.target.checked;
      let email = store.getState().email;
      updateViewPreference(email, compactViewEnabled,
        (success) => {
          console.log("Compact View Preference Switched Succesfully", success);
          store.dispatch(compactViewPreference(compactViewEnabled));
        },
        (error) => { console.log("ERROR in updation" + error) }
      );
    };

    return (
      <div style={{ display: "flex", flexDirection: 'row', justifyContent: 'center' }}>
        <Col xs={11} style={{ display: "flex", justifyContent: 'center', flexDirection: 'column' }}>

          {!isSmall &&
            <Panel bsStyle="primary">
              <FormControlLabel
                style={{ width: "200px", right: "10px", color: "white", position: "absolute" }}
                control={
                  <Switch
                    color="default"
                    style={{ colorPrimary: "white", colorSecondary: "white" }}
                    checked={this.state.compact}
                    onChange={updateCompactViewPreference('compact')}
                    value="compact"
                  />
                }
                label={<Typography style={{ color: "white", fontSize: "12px" }}>Compact</Typography>}
              />
              <Panel.Heading>
                <div style={headerStyle}>
                  <p>
                    Browse and Transfer Files
              </p>
                </div>
              </Panel.Heading>

              <Panel.Body key={isSmall} style={{ overflow: "hidden" }}>
                <Row style={{ flexDirection: 'column' }}>
                  <DragDropContext
                    onDragStart={this.onDragStart}
                    onDragEnd={this.onDragEnd}>
                    <Col xs={6} style={panelStyle}  >
                      {this._returnBrowseComponent1()}
                    </Col>
                    <Col xs={6} style={panelStyle} >
                      {this._returnBrowseComponent2()}
                    </Col>
                  </DragDropContext>
                </Row>
                <Row style={{ display: 'block', ...headerStyle }}>
                  <Button id="sendFromRightToLeft" style={{ padding: '15px', marginRight: '10px' }} onClick={this.onSendToLeft}> <Glyphicon glyph="arrow-left" />    Send</Button>
                  <Button id="sendFromLeftToRight" style={{ padding: '15px', marginLeft: '10px' }} onClick={this.onSendToRight}> Send<Glyphicon glyph="arrow-right" /></Button>
                </Row>

                <ErrorMessagesConsole />
              </Panel.Body>
            </Panel>


          }
          {/* !isSmall && this.getSettingComponent(isSmall) */}
          {isSmall &&
            <Panel bsStyle="primary">
              <Panel.Heading style={{ textAlign: "center" }}>
                <p>
                  Browse and Transfer Files
          </p>

                <FormControlLabel
                  style={{ color: "white" }}
                  control={
                    <Switch
                      color="default"
                      style={{ colorPrimary: "white", colorSecondary: "white" }}
                      checked={this.state.compact}
                      onChange={updateCompactViewPreference('compact')}
                      value="compact"
                    />
                  }
                  label={<Typography style={{ fontSize: "12px" }}>Compact</Typography>}
                />
              </Panel.Heading>


              <Panel.Body key={isSmall} style={{ overflow: "hidden" }}>
                <Row style={{ flexDirection: 'column' }}>
                  <DragDropContext
                    onDragStart={this.onDragStart}
                    onDragEnd={this.onDragEnd}
                  >
                    <Col style={panelStyle}>
                      {this._returnBrowseComponent1()}
                    </Col>
                    <Row style={{ display: 'flex', flexDirection: 'row', justifyContent: 'center' }}>
                      <Button id="sendFromRightToLeft" style={{ padding: '15px', marginRight: '10px' }} onClick={this.onSendToLeft}> <Glyphicon glyph="arrow-up" /> Send</Button>
                      <Button id="sendFromLeftToRight" style={{ padding: '15px', marginLeft: '10px' }} onClick={this.onSendToRight}> Send<Glyphicon glyph="arrow-down" /></Button>
                    </Row>
                    <Row style={panelStyle}>
                      {this._returnBrowseComponent2()}
                    </Row>

                    {/*  <Row> {this.getSettingComponent(isSmall)} </Row> */}
                  </DragDropContext>

                </Row>
                <div> </div>
                <ErrorMessagesConsole />
              </Panel.Body>
            </Panel>


          }
          {/* !isSmall && this.getSettingComponent(isSmall) */}
          {isSmall &&
            <Panel bsStyle="primary">
              <FormControlLabel
                style={{ width: "200px", float: "right", color: "white" }}
                control={
                  <Switch
                    color="default"
                    style={{ colorPrimary: "white", colorSecondary: "white" }}
                    checked={this.state.compact}
                    onChange={handleChange('compact')}
                    value="compact"
                  />
                }
                label={<Typography style={{ fontSize: "12px" }}>Compact</Typography>}
              />
              <Panel.Heading>
                <p>
                  Browse and Transfer Files</p>
              </Panel.Heading>
              <Panel.Body key={isSmall} style={{ overflow: "hidden" }}>
                <Row style={{ flexDirection: 'column' }}>
                  <DragDropContext
                    onDragStart={this.onDragStart}
                    onDragEnd={this.onDragEnd}
                  >
                    <Col style={panelStyle}>
                      {this._returnBrowseComponent1()}
                    </Col>
                    <Row style={{ display: 'flex', flexDirection: 'row', justifyContent: 'center' }}>
                      <Button id="sendFromRightToLeft" style={{ padding: '15px', marginRight: '10px' }} onClick={this.onSendToLeft}> <Glyphicon glyph="arrow-up" /> Send</Button>
                      <Button id="sendFromLeftToRight" style={{ padding: '15px', marginLeft: '10px' }} onClick={this.onSendToRight}> Send<Glyphicon glyph="arrow-down" /></Button>
                    </Row>
                    <Row style={panelStyle}>
                      {this._returnBrowseComponent2()}
                    </Row>
                  </DragDropContext>
                </Row>
                <div> </div>
                <ErrorMessagesConsole />
              </Panel.Body>
            </Panel>}
        </Col>
      </div>
    );
  }
}
