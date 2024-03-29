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


/*
  Note - All commented code is for the custom integration that was developed for the support page.
  This was replaced by a free Freshdesk widget just before the release as a workaround for free Freshdesk API restrictions
*/

import React, { Component } from 'react';
import { submitIssue } from '../../APICalls/APICalls';
import {store} from '../../App';

import TextField from '@material-ui/core/TextField';
import Button from '@material-ui/core/Button';
import LinearProgress from '@material-ui/core/LinearProgress';
import { eventEmitter } from "../../App";
import ReCAPTCHA from 'react-google-recaptcha';
// import Box from '@material-ui/core/Box';
import {Container, Box} from "@material-ui/core";

import { ValidatorForm } from 'react-material-ui-form-validator';
import { updateGAPageView } from "../../analytics/ga";
import Typography from "@material-ui/core/Typography";
import Logo from "./logo-blue.png";
import './support_style.scss';

export default class SupportComponent extends Component{

  constructor(){
    super();
    this.state = {
      captchaVerified : false,
      captchaVerificationValue : null,
      email : (store.getState().email === "noemail" ? "" : store.getState().email)
    };

    this.captchaRef = null;

    this.handleSubmit = this.handleSubmit.bind(this);
    this.handleChange = this.handleChange.bind(this);
    this.handleCaptchaEvent = this.handleCaptchaEvent.bind(this);

    updateGAPageView();
    this.resetCaptcha = this.resetCaptcha.bind(this);
  }

  componentDidMount(){
    document.title = "OneDataShare - Support";
  }

  handleCaptchaEvent(value){
    this.setState({ captchaVerified : true, captchaVerificationValue : value});
  }

  resetCaptcha(){
    this.setState({ captchaVerified : false, captchaVerificationValue : null});
    this.captchaRef.reset();
  }

  handleChange = (event) =>{
    this.setState({
      [event.target.name] : event.target.value
    });
  }

  handleSubmit(){
    if(this.state.captchaVerified){
      let progressBarDiv = document.getElementById('progress-bar');
      progressBarDiv.style.visibility = 'visible';

      let msgDiv = document.getElementById('msg');

      let reqBody = {
        name : this.state.name,
        email : this.state.email,
        phone : this.state.phone,
        subject : this.state.subject,
        description : this.state.description,
        captchaVerificationValue : this.state.captchaVerificationValue
      };

      submitIssue(reqBody,
        (resp)=>{
          progressBarDiv.style.visibility = 'hidden';
          msgDiv.style.border = '1px solid green'
          msgDiv.style.color = "green";
          msgDiv.innerHTML = "Support ticket created successfully. Ticket number - " + resp;
          msgDiv.style.visibility = 'visible';
          this.resetCaptcha();
        },
        (err)=>{
          progressBarDiv.style.visibility = 'hidden';
          msgDiv.style.border = '1px solid red';
          msgDiv.style.color = "red";
          msgDiv.innerHTML = "There was an error while creating the support ticket. Please try again or email us at <a href=\"mailto:admin@onedatashare.org\">admin@onedatashare.org</a>";
          msgDiv.style.visibility = 'visible';
          this.resetCaptcha();
        });
      }
      else
        eventEmitter.emit("errorOccured", "Please verify you are not a robot!");
  }

  render(){

    const divStyle = { margin : '2% 5%', alignItems: "center" };
    const captchaStyle = { ...divStyle, textAlign : 'center', display: 'inline-block' };

    return(
        <div style={{display: "flex", flexDirection: 'row', justifyContent: 'center', textAlign: 'center'}} >
          <Container maxWidth={"md"} style={{ display: "flex"}}>
            <Box boxShadow={3} style={{borderRadius: "8px", backgroundColor: "#fff"}}>
              <div style={divStyle}>
                <img src={Logo} style={{width: "20%"}} alt="Logo"/>
              </div>
              <Typography component="h1" variant="h4" align="center">
              <div style={divStyle}>
                <b style={{color: "#172753"}}>Report an Issue</b>
                </div>
              </Typography>

              <ValidatorForm ref="support-form" className="support-form-wrapper" onSubmit={this.handleSubmit}>
                <div style={divStyle}>
                  <TextField
                    required
                    classes={{label:'support'}}
                    label = 'Name'
                    name = 'name'
                    onChange = {this.handleChange}
                    style = {{ marginRight : '5%', width :'37.5%' }}
                    variant="outlined"
                  />

                  <TextField
                    required
                    label = 'Email'
                    name = 'email'
                    value = { this.state.email }
                    onChange = {this.handleChange}
                    style = {{ width :'37.5%' }}
                    variant="outlined"
                  />
                </div>

                <div style={divStyle}>
                  <TextField
                    required
                    label = 'Subject&nbsp;'
                    name = 'subject'
                    onChange = {this.handleChange}
                    style = {{ width :'80%' }}
                    variant="outlined"
                  />
                </div>

                <div style={ divStyle } >
                  <TextField
                    required
                    multiline
                    rows="6"
                    label="Issue"
                    name="description"
                    onChange = {this.handleChange}
                    style={{ width : '80%' }}
                    helperText="Enter Issue Description"
                    variant="outlined"
                  />
                </div>

                {process.env.REACT_APP_GC_CLIENT_KEY && <div style={ captchaStyle }>
                    <ReCAPTCHA
                      sitekey= { process.env.REACT_APP_GC_CLIENT_KEY }
                      onChange={this.handleCaptchaEvent}
                      ref = { r => this.captchaRef = r}
                    />
                </div>}


                <div id="progress-bar" style={{ marginLeft : '19%', marginRight : '19%', visibility : 'hidden' }}>
                  <LinearProgress />
                </div>

                <div style={{marginLeft : '5%', marginRight : '5%', marginTop : '1.5%', marginBottom : '2%'}}>
                  <Button type="submit" size="large" variant="contained" color="primary">
                    Submit
                  </Button>
                </div>

                <div id="msg" style={{marginLeft : '19%', marginRight : '19%', marginTop : '2%', marginBottom : '2%',
                            textAlign : 'center', paddingTop : '1%', paddingBottom : '1%', visibility : 'hidden'}}>
                </div>

              </ValidatorForm>

            </Box>
          </Container>
        </div>
    );
  }
}