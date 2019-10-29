import React, { Component } from 'react';
import './App.css';
import MainComponent from "./MainComponent";
import OauthProcessComponent from "./views/OauthProcessComponent";
import { createStore } from 'redux';
import { onedatashareModel } from './model/reducers';
import  { Route, Switch} from 'react-router-dom';

import Snackbar from '@material-ui/core/Snackbar';
import Button from '@material-ui/core/Button';

import EventEmitter from 'eventemitter3';

import { initializeReactGA } from './analytics/ga';

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

    initializeReactGA();
  }


  handleOpen = (errormsg) => {
    console.log(errormsg);
    this.setState({ open: true, vertical: 'top', horizontal: 'center', error: JSON.stringify(errormsg) });
    setTimeout(this.handleClose, 4000);
  };

  handleClose = () => {
    this.setState({ open: false });
  };

  componentDidMount(){
    this.setState({loaded: true});
    eventEmitter.on("errorOccured", this.handleOpen); 
  }

  
  render() {
    const { vertical,horizontal, error, open } = this.state;

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
          {/*<Route path='/account' component={AccountControlComponent}/>*/}
          <Route path='/oauth/:tag' component={OauthProcessComponent}/>
          <Route exact path='/*/' component={MainComponent}/>
        </Switch>

      </div>
    );
  }
}

export default App;