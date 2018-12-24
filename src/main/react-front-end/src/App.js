import React, { Component } from 'react';
import './App.css';
import MainComponent from "./MainComponent";
import AccountControlComponent from "./views/Login/AccountControlComponent.js";
import OauthProcessComponent from "./views/OauthProcessComponent";
import { createStore } from 'redux';
import { onedatashareModel } from './model/reducers';
import  { Route, Switch, Redirect } from 'react-router-dom';
//import './lightTheme.css';

import Snackbar from '@material-ui/core/Snackbar';
import Button from '@material-ui/core/Button';
import EventEmitter from 'eventemitter3';
export const eventEmitter = new EventEmitter();


export const store = createStore(onedatashareModel);
class App extends Component {

  constructor(){
    super();
    this.state={
      loaded: false,
      open: false, 
      vertical: 'top', 
      horizontal: 'center',
      error: "null"
    };
  }


  handleOpen = (errormsg) => {
    console.log("asdasd");
    this.setState({ open: true, vertical: 'top', horizontal: 'center', error: errormsg });
    setTimeout(this.handleClose, 4000);
  };

  handleClose = () => {
    this.setState({ open: false });
  };

  componentDidMount(){
    document.title = "OneDataShare - Home";
    this.setState({loaded: true});
    eventEmitter.on("errorOccured", this.handleOpen); 
  }

  render() {
    const { loggedIn,vertical,horizontal, error, open } = this.state;
    return (
      <div>

        <Snackbar
          anchorOrigin={{ vertical, horizontal }}
          style={{marginTop: "20px"}}
          open={open}
          onClose={this.handleClose}
          ContentProps={{
            'aria-describedby': 'message-id',
          }}
          action={
            <Button onClick={this.handleClose} color="secondary" size="small">
              Close
            </Button>
          }
          message={<span id="message-id">{error}</span>}
        />

        <Switch>
          <Route path='/account' component={AccountControlComponent}/>
          <Route path='/oauth/:id' component={OauthProcessComponent}/>
          <Route exact path='/*/' component={MainComponent}/>
        </Switch>
      </div>
    );
  }
}

export default App;
