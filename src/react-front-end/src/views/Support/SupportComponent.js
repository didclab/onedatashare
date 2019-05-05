import React, { Component } from 'react';

import { submitIssue } from '../../APICalls/APICalls'

import Card from '@material-ui/core/Card';
import CardHeader from '@material-ui/core/CardHeader';
import TextField from '@material-ui/core/TextField';
import Button from '@material-ui/core/Button';
import LinearProgress from '@material-ui/core/LinearProgress';

import { ValidatorForm, TextValidator } from 'react-material-ui-form-validator';

export default class SupportComponent extends Component{

  constructor(){
    super();
    this.handleSubmit = this.handleSubmit.bind(this);
    this.handleChange = this.handleChange.bind(this);
  }

  handleChange = (event) =>{
    this.setState({
      [event.target.name] : event.target.value
    });
  }

  handleSubmit(){
    var progressBarDiv = document.getElementById('progress-bar');
    progressBarDiv.style.visibility = 'visible';

    var msgDiv = document.getElementById('msg');

    var reqBody = {
      name : this.state.first_name + ' ' + this.state.last_name,
      email : this.state.email,
      phone : this.state.phone,
      subject : this.state.subject,
      issueDescription : this.state.description
    };
    
    submitIssue(reqBody, 
      (resp)=>{
        progressBarDiv.style.visibility = 'hidden';
        msgDiv.style.border = '1px solid green'
        msgDiv.style.color = "green";
        msgDiv.innerHTML = "Support ticket created successfully. Ticket number - " + resp;
        msgDiv.style.visibility = 'visible';
      },
      (err)=>{
        progressBarDiv.style.visibility = 'hidden';
        msgDiv.style.border = '1px solid red';
        msgDiv.style.color = "red";
        msgDiv.innerHTML = "There was an error while creating the support ticket. Please try again.";
        msgDiv.style.visibility = 'visible';
      });
  }


  render(){
    
    const cardStyle = { marginLeft: '7.2%', marginRight: '7.2%', marginTop: '5%', marginBottom: '10%', border: 'solid 2px #d9edf7' }
    const divStyle = { marginLeft : '5%', marginRight : '5%', marginTop : '2%', marginBottom : '2%' }

    return(
        <Card style={cardStyle}>
        
          <CardHeader title="Report an Issue" />

          <ValidatorForm ref="support-form" onSubmit={this.handleSubmit}>
            <div style={divStyle}>
              <TextField
                required
                label = 'First Name'
                name = 'first_name' 
                onChange = {this.handleChange}
                style = {{ marginRight : '5%', width :'30%' }}
              />

              <TextField
                required
                label = 'Last Name'
                name = 'last_name'   
                onChange = {this.handleChange}
                style = {{ marginLeft : '5%', width :'30%' }}
              />
            </div>

            <div style={divStyle}>
              <TextField
                required
                label = 'Email Address'
                name = 'email' 
                onChange = {this.handleChange}
                style = {{ marginRight : '5%', width :'30%' }}
              />

              <TextField
                label = 'Phone'
                name = 'phone'   
                onChange = {this.handleChange}
                style = {{ marginLeft : '5%', width :'30%' }}
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
                multiline
                rows="6"
                label="Issue Description"
                name="description"
                onChange = {this.handleChange}
                style={{ width : '70%' }}
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