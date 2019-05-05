import React, { Component } from 'react';

import Card from '@material-ui/core/Card';
import CardHeader from '@material-ui/core/CardHeader';
import TextField from '@material-ui/core/TextField';
import Button from '@material-ui/core/Button';
import LinearProgress from '@material-ui/core/LinearProgress';

import { ValidatorForm, TextValidator } from 'react-material-ui-form-validator';

export default class SupportComponent extends Component{

  constructor(){
    super();
    this.submitIssue = this.submitIssue.bind(this);
  }

  submitIssue(){
    var progressBarDiv = document.getElementById('progress-bar');
    progressBarDiv.style.visibility = 'visible';
  }


  render(){
    
    const cardStyle = { marginLeft: '7.2%', marginRight: '7.2%', marginTop: '5%', marginBottom: '10%', border: 'solid 2px #d9edf7' }
    const divStyle = { marginLeft : '5%', marginRight : '5%', marginTop : '2%', marginBottom : '2%' }

    return(
        <Card style={cardStyle}>
        
          <CardHeader title="Report an Issue" />

          <ValidatorForm ref="support-form" onSubmit={this.submitIssue}>
            <div style={divStyle}>
              <TextField
                required
                label = 'First Name'
                name = 'first_name' 
                style = {{ marginRight : '5%', width :'30%' }}
              />

              <TextField
                required
                label = 'Last Name'
                name = 'last_name'   
                style = {{ marginLeft : '5%', width :'30%' }}
              />
            </div>

            <div style={divStyle}>
              <TextField
                required
                label = 'Email Address'
                name = 'email' 
                style = {{ marginRight : '5%', width :'30%' }}
              />

              <TextField
                label = 'Phone'
                name = 'phone'   
                style = {{ marginLeft : '5%', width :'30%' }}
              />
            </div>

            <div style={divStyle}>
              <TextField
                required
                label = 'Subject'
                name = 'subject'   
                style = {{ width :'70%' }}
              />
            </div>

            <div style={ divStyle } >
              <TextField
                label="Issue Description"
                name="description"
                style={{ width : '70%' }}
                multiline
                rows="6"
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

          </ValidatorForm>
        </Card>
    );
  }
}