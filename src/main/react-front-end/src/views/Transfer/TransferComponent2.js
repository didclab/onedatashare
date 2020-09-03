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


import React, { Component, useState } from 'react';
import { Panel, Col, Row, Glyphicon } from 'react-bootstrap';

import { store } from '../../App';
import BrowseModuleComponent from './BrowseModuleComponent2';
// import BrowserSlice from "./BrowserSlice";
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';

import FormControl from '@material-ui/core/FormControl';
import Radio from '@material-ui/core/Radio';
import RadioGroup from '@material-ui/core/RadioGroup';
import FormControlLabel from '@material-ui/core/FormControlLabel';
import Checkbox from "@material-ui/core/Checkbox";
import FormLabel from '@material-ui/core/FormLabel';
// import Grid from "@material-ui/core/Grid";
import {Hidden, Container, Box, TextField, Grid, useMediaQuery, Snackbar, Fade /*Accordion, AccordionSummary, AccordionDetails*/} from "@material-ui/core";
import Accordion from "@material-ui/core/Accordion";
import AccordionSummary from "@material-ui/core/AccordionSummary";
import AccordionDetails from "@material-ui/core/AccordionDetails";

import Divider from "@material-ui/core/Divider";
import {KeyboardArrowRightRounded, KeyboardArrowLeftRounded, KeyboardArrowDownRounded, KeyboardArrowUpRounded, ExpandMore} from "@material-ui/icons";

import { submit, updateViewPreference } from "../../APICalls/APICalls";
import { endpointUpdate, compactViewPreference } from "../../model/actions";

import { DragDropContext } from 'react-beautiful-dnd';
import { mutliDragAwareReorder, screenIsSmall } from "./utils.js";

import { getSelectedTasks, unselectAll, setDraggingTask, getEntities, setBeforeTransferReorder, makeFileNameFromPath, getEndpointFromColumn, getSelectedTasksFromSide, getCurrentFolderId } from "./initialize_dnd.js";

import { eventEmitter } from "../../App.js";
import { gridFullWidth, gridHalfWidth} from "../../constants";
import Slider from '@material-ui/lab/Slider';

import Switch from '@material-ui/core/Switch';

import ErrorMessagesConsole from '../ErrorMessagesConsole';
import  Terminal  from '../Terminal';

import queryString from 'query-string';
import { updateGAPageView } from '../../analytics/ga';
import {useTheme, styled} from "@material-ui/core/styles";

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
        optimizer: localStorage.hasOwnProperty("optimizer") ? localStorage.getItem("optimizer") : "None",
        // overwrite: "true",
        // verify: "true",
        // encrypt: "true",
        // compress: "true",
        overwrite: localStorage.hasOwnProperty("overwrite") ? JSON.parse(localStorage.getItem("overwrite")) : true,
        verify: localStorage.hasOwnProperty("verify") ? JSON.parse(localStorage.getItem("verify")) : true,
        encrypt: localStorage.hasOwnProperty("encrypt") ? JSON.parse(localStorage.getItem("encrypt")) : true,
        compress: localStorage.hasOwnProperty("compress") ? JSON.parse(localStorage.getItem("compress")) : true,
        retry: localStorage.hasOwnProperty("retry") ? Number(localStorage.getItem("retry")) : 5
      },
      compact: store.getState().compactViewEnabled,
      notif: false
     // terminalFlag:false
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

  labelStyle = () => styled(Typography)({
    fontSize: "12px",
    color: "black",
    ["@media only screen and (max-width: 600px)"]:{
      fontSize: "16px"
    }
  })


  headerStyle = () => styled(Typography)({
    fontSize: "15px",
    ["@media only screen and (max-width: 600px)"]:{
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
    processed.selectedTasks.forEach((task) => {
      srcUrls.push(makeFileNameFromPath(endpointSrc.uri, processed.fromTo[0].path, task.name))
      fileIds.push(task.id);
      destUrls.push(makeFileNameFromPath(endpointDest.uri, processed.fromTo[1].path, task.name))
    });

    var optionParsed = {}
    Object.keys(options).forEach((v)=>{
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
        eventEmitter.emit("messageOccured", "Transfer initiated! Please visit the queue page to monitor the transfer");
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
    const closeNotif = () => {
      this.setState({notif: false});
    }

    const formlabelstyle = { fontSize: "15px" }
    // const formStyle = { marginLeft: "5%", marginRight: "5%" }
    const formStyle = { marginLeft: "35%", marginRight: "35%"}
    const desktopWidth = 4;

    const tabletWidth = 4;
    const ToggleLabel = this.labelStyle();
    const ToggleHeader = this.headerStyle();
    const FieldLabel = this.fieldLabelStyle();
    const radioStyles = {
      formLabel: {
        color: "black",
        '&.Mui-focused': {
          color: "black",
        }
      }
    };
    // style={formlabelstyle}
    // style={formStyle}
    // md={desktopWidth} sm={tabletWidth} xs={gridFullWidth}
    return (
    // <Box className="transferSettings">
    //   <div style={{ textAlign: "center" }}>
    //     <h5>Transfer Setting</h5>
    //   </div>
        <Container>
      <Grid container className="innerBox" direction="row" align-items="flex-start" justify="center" spacing={2} style={{paddingLeft: "20px"}}>
        <Grid item md={desktopWidth} sm={tabletWidth}>
          <FormControl component="fieldset" >
            <FormLabel component="legend" classes={{root: radioStyles.formLabel}} ><ToggleHeader>Optimization</ToggleHeader></FormLabel>
            <RadioGroup
                aria-label="Optimization"
                value={this.state.settings.optimizer}
                onChange={handleChange("optimizer")}
            >
              <FormControlLabel value="None" control={<Radio />} label={<ToggleLabel>None</ToggleLabel>} />
              <FormControlLabel value="2nd Order" control={<Radio />} label={<ToggleLabel>2nd Order</ToggleLabel>} />
              <FormControlLabel value="PCP" control={<Radio />} label={<ToggleLabel>PCP</ToggleLabel>}  />
            </RadioGroup>
          </FormControl>
        </Grid>


        {/*<Grid item md={desktopWidth} sm={tabletWidth} xs={gridFullWidth}>*/}
        {/*  <FormControl component="fieldset" style={formStyle}>*/}
        {/*    <FormLabel component="legend" style={formlabelstyle}>Overwrite</FormLabel>*/}
        {/*    <RadioGroup*/}
        {/*        aria-label="Overwrite"*/}
        {/*        value={String(this.state.settings.overwrite)}*/}
        {/*        onChange={handleChange("overwrite")}*/}
        {/*    >*/}
        {/*      <FormControlLabel value="true" control={<Radio />} label="True" />*/}
        {/*      <FormControlLabel value="false" control={<Radio />} label="False" />*/}
        {/*    </RadioGroup>*/}
        {/*  </FormControl>*/}
        {/*</Grid>*/}

        {/*<Grid item md={desktopWidth} sm={tabletWidth} xs={gridFullWidth}>*/}
        {/*  <FormControl component="fieldset" style={formStyle}>*/}
        {/*    <FormLabel component="legend" style={formlabelstyle}>Integrity</FormLabel>*/}
        {/*    <RadioGroup*/}
        {/*        aria-label="Integrity"*/}
        {/*        value={String(this.state.settings.verify)}*/}
        {/*        onChange={handleChange("verify")}*/}
        {/*    >*/}
        {/*      <FormControlLabel value="true" control={<Radio />} label="True" />*/}
        {/*      <FormControlLabel value="false" control={<Radio />} label="False" />*/}
        {/*    </RadioGroup>*/}
        {/*  </FormControl>*/}
        {/*</Grid>*/}

        {/*<Grid item md={desktopWidth} sm={tabletWidth} xs={gridFullWidth}>*/}
        {/*  <FormControl component="fieldset" style={formStyle}>*/}
        {/*    <FormLabel component="legend" style={formlabelstyle}>Encrypt</FormLabel>*/}
        {/*    <RadioGroup*/}
        {/*        aria-label="Encrypt"*/}
        {/*        value={String(this.state.settings.encrypt)}*/}
        {/*        onChange={handleChange("encrypt")}*/}
        {/*    >*/}
        {/*      <FormControlLabel value="true" control={<Radio />} label="True" />*/}
        {/*      <FormControlLabel value="false" control={<Radio />} label="False" />*/}
        {/*    </RadioGroup>*/}
        {/*  </FormControl>*/}
        {/*</Grid>*/}


        {/*<Grid item md={desktopWidth} sm={tabletWidth} xs={gridFullWidth}>*/}
        {/*  <FormControl component="fieldset" style={formStyle}>*/}
        {/*    <FormLabel component="legend" style={formlabelstyle}>Compress</FormLabel>*/}
        {/*    <RadioGroup*/}
        {/*        aria-label="Compress"*/}
        {/*        value={String(this.state.settings.compress)}*/}
        {/*        onChange={handleChange("compress")}*/}
        {/*    >*/}
        {/*      <FormControlLabel value="true" control={<Radio />} label="True" />*/}
        {/*      <FormControlLabel value="false" control={<Radio />} label="False" />*/}
        {/*    </RadioGroup>*/}
        {/*  </FormControl>*/}
        {/*</Grid>*/}



        {/* checkbox version */}
        {/*direction={"column"} justify={"center"}*/}
        <Grid item container direction={"row"} justify={"space-evenly"} md={desktopWidth} sm={tabletWidth} spacing={(theme) => theme.breakpoints.up('sm') ? 2 : 8}>
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

        {/*<Grid item md={desktopWidth} sm={tabletWidth} xs={gridFullWidth}>*/}
        {/*  <FormControl component="fieldset">*/}
        {/*    <FormLabel component="legend" style={formlabelstyle}>Retry Counts</FormLabel>*/}
        {/*    <Slider*/}

        {/*        value={this.state.settings.retry}*/}
        {/*        min={0}*/}
        {/*        max={10}*/}
        {/*        step={1}*/}
        {/*        marks={true}*/}
        {/*        valueLabelDisplay={"auto"}*/}
        {/*        onChange={handleChangeRetry}*/}
        {/*    />*/}
        {/*    <FormLabel style={{ marginTop: "20px", fontSize: "20px" }}>{this.state.settings.retry} Times</FormLabel>*/}
        {/*  </FormControl>*/}
        {/*</Grid>*/}
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

      </Grid>
          <Divider/>
    <Grid container justify={'center'}>
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


  render() {
    // const isSmall = screenIsSmall();
    // const isSmall = false;
    // const panelStyle = { height: "auto", margin: isSmall ? "10px" : "0px" };
    // const headerStyle = { textAlign: "center" }
    let handleChange = name => event => {
      this.setState({ [name]: event.target.checked });
    };

    // Tooltip

    const ToggleLabel = this.labelStyle();

    return (
        <div>
        <Grid container direction="column" className={"outeractionContainer"}>
          <Container className={"boxHeader"}>

            <p>
              Browse and Transfer Files
            </p>

          </Container>
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

              <Box className="innerBox">
                <Grid container direction="row" justify="center" spacing={2}>
                  <DragDropContext
                      onDragStart={this.onDragStart}
                      onDragEnd={this.onDragEnd}>
                    <Grid item md={gridHalfWidth} xs={gridFullWidth}>
                      {this._returnBrowseComponent1()}
                      {/*<h6>Source</h6>*/}
                    </Grid>
                    <Hidden mdUp>
                      <Grid container item direction="row" align-items="center" justify="center">
                        <Grid item>
                          <Button id="sendFromRightToLeft" onClick={this.onSendToLeft}>
                            <KeyboardArrowUpRounded />
                            Send
                          </Button>
                        </Grid>
                        <Grid item>
                          <Button id="sendFromLeftToRight" onClick={this.onSendToRight}>
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
                  <Grid container direction="row" align-items="center" justify="center">
                    <Grid item>
                      <Button id="sendFromRightToLeft" onClick={this.onSendToLeft}> <KeyboardArrowLeftRounded/>    Send</Button>
                    </Grid>
                    <Grid item>
                      <Button id="sendFromLeftToRight" onClick={this.onSendToRight}> Send<KeyboardArrowRightRounded /></Button>
                    </Grid>

                  </Grid>
                </Hidden>
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
            <ErrorMessagesConsole/>



            {/*}*/}
            {/*{!isSmall && this.getSettingComponent(isSmall)}*/}
            {/*{isSmall &&*/}
            {/*<Panel bsStyle="primary">*/}
            {/*  <FormControlLabel*/}
            {/*      style={{ width: "200px", float: "right", color: "white" }}*/}
            {/*      control={*/}
            {/*        <Switch*/}
            {/*            color="default"*/}
            {/*            style={{ colorPrimary: "white", colorSecondary: "white" }}*/}
            {/*            checked={this.state.compact}*/}
            {/*            onChange={handleChange('compact')}*/}
            {/*            value="compact"*/}
            {/*        />*/}
            {/*      }*/}
            {/*      label={<Typography style={{ fontSize: "12px" }}>Compact</Typography>}*/}
            {/*  />*/}
            {/*  <Panel.Heading>*/}
            {/*    <p>*/}
            {/*      Browse and Transfer Files</p>*/}
            {/*  </Panel.Heading>*/}
            {/*  <Panel.Body key={isSmall} style={{ overflow: "hidden" }}>*/}
            {/*    <Row style={{ flexDirection: 'column' }}>*/}
            {/*      <DragDropContext*/}
            {/*          onDragStart={this.onDragStart}*/}
            {/*          onDragEnd={this.onDragEnd}*/}
            {/*      >*/}
            {/*        <Col style={panelStyle}>*/}
            {/*          {this._returnBrowseComponent1()}*/}
            {/*        </Col>*/}
            {/*        <Row style={{ display: 'flex', flexDirection: 'row', justifyContent: 'center' }}>*/}
            {/*          <Button id="sendFromRightToLeft" style={{ padding: '15px', marginRight: '10px' }} onClick={this.onSendToLeft}> <Glyphicon glyph="arrow-up" /> Send</Button>*/}
            {/*          <Button id="sendFromLeftToRight" style={{ padding: '15px', marginLeft: '10px' }} onClick={this.onSendToRight}> Send<Glyphicon glyph="arrow-down" /></Button>*/}
            {/*        </Row>*/}
            {/*        <Row style={panelStyle}>*/}
            {/*          {this._returnBrowseComponent2()}*/}
            {/*        </Row>*/}
            {/*      </DragDropContext>*/}
            {/*    </Row>*/}
            {/*    <div> </div>*/}
            {/*    <ErrorMessagesConsole />*/}
            {/*    <div> </div>*/}


            {/*  </Panel.Body>*/}
            {/*</Panel>}*/}

        </Container>
      </Grid>
          {/*{this.getSettingComponent()}*/}
          <Container className={"terminalContainer"}>
            <div>
              <Terminal endpoint={this.state.endpoint1} /> <br/>
              <Terminal endpoint={this.state.endpoint2} />

            </div>
          </Container>
        </div>
    );
  }
}
