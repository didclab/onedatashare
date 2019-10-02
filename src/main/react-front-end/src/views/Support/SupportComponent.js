import React, { Component } from 'react';

import { submitIssue } from '../../APICalls/APICalls';
import {store} from '../../App';

import Card from '@material-ui/core/Card';
import CardHeader from '@material-ui/core/CardHeader';
import TextField from '@material-ui/core/TextField';
import Button from '@material-ui/core/Button';
import LinearProgress from '@material-ui/core/LinearProgress';
import { eventEmitter } from "../../App";
import ReCAPTCHA from 'react-google-recaptcha';


import { ValidatorForm } from 'react-material-ui-form-validator';
import { updateGAPageView } from "../../analytics/ga";
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
    
    const cardStyle = { margin: '5% 7.2% 10%', border: 'solid 2px #d9edf7' };
    const divStyle = { margin : '2% 5%' };
    const captchaStyle = { ...divStyle, textAlign : 'center', display: 'inline-block' };

    return(
      <Card style={cardStyle}>
        <CardHeader title="Report an Issue" />
        <ValidatorForm ref="support-form" onSubmit={this.handleSubmit}>
          <div style={divStyle}>
            <TextField
              required
              classes={{label:'support'}}
              label = 'Name'
              name = 'name' 
              onChange = {this.handleChange}
              style = {{ marginRight : '5%', width :'30%' }}
            />

            <TextField
              required
              label = 'Email Address'
              name = 'email'
              value = { this.state.email }
              onChange = {this.handleChange}
              style = {{ marginRight : '5%', width :'30%' }}
            />
          </div>

          <div style={divStyle}>
            <TextField
              required
              label = 'Subject'
              name = 'subject'
              onChange = {this.handleChange}   
              style = {{ width :'70%' }}
            />
          </div>

          <div style={ divStyle } >
            <TextField
              required
              multiline
              rows="6"
              label="Issue Description"
              name="description"
              onChange = {this.handleChange}
              style={{ width : '70%' }}
            />
          </div>
          
          <div style={ captchaStyle }>
              <ReCAPTCHA 
                sitekey= { process.env.REACT_APP_GC_CLIENT_KEY }
                onChange={this.handleCaptchaEvent}
                ref = { r => this.captchaRef = r}
              />
          </div> 
          

          <div id="progress-bar" style={{ marginLeft : '19%', marginRight : '19%', visibility : 'hidden' }}>
            <LinearProgress />
          </div>

          <div style={{marginLeft : '5%', marginRight : '5%', marginTop : '1%', marginBottom : '2%'}}>
            <Button type="submit" size="medium" variant="contained" color="primary" style={{ width : '70%' }}>
              Submit
            </Button>
          </div>

          <div id="msg" style={{marginLeft : '19%', marginRight : '19%', marginTop : '2%', marginBottom : '2%', 
                      textAlign : 'center', paddingTop : '1%', paddingBottom : '1%', visibility : 'hidden'}}>
          </div>

        </ValidatorForm>
      </Card>
    );
  }
}
