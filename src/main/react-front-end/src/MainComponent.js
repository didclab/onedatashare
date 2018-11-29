import React, { Component } from 'react';

import NavbarComponent from "./views/NavbarComponent";
import HomePageComponent  from './views/HomePageComponent';
import PropTypes from 'prop-types';

import  { Route, Switch, Redirect } from 'react-router-dom';
import {store} from './App.js';
import {cookies} from './model/reducers';
import TransferComponent from './views/Transfer/TransferComponent';
import HistoryComponent from './views/Admin/HistoryComponent'
import QueueComponent from './views/Queue/QueueComponent.js';
import UserAccountComponent from './views/Login/UserAccountComponent.js';
import ClientsInfoComponent from './views/Admin/ClientsInfoComponent.js';

import Snackbar from '@material-ui/core/Snackbar';
import Button from '@material-ui/core/Button';

import EventEmitter from 'eventemitter3';
export const eventEmitter = new EventEmitter();

export default class MainComponent extends Component {

  constructor(props){
    super(props);
    console.log(cookies);
    this.state={
      isLoggedIn: store.getState().login,
      admin: store.getState().admin,
      open: false, 
      vertical: 'top', 
      horizontal: 'center',
      error: "null"
    }
    this.unsubscribe = store.subscribe(() => {
      this.setState({
        isLoggedIn: store.getState().login,
        admin: store.getState().admin
      });
    });

  }
  componentWillUnmount(){
    this.unsubscribe();
  }
  componentDidMount(){
    eventEmitter.on("errorOccured", this.handleOpen); 
  }

  handleOpen = (errormsg) => {
    console.log("asdasd");
    this.setState({ open: true, vertical: 'top', horizontal: 'center', error: errormsg });
    setTimeout(this.handleClose, 4000);
  };

  handleClose = () => {
    this.setState({ open: false });
  };

  render() {
    const { isLoggedIn, admin, vertical, horizontal, open, error } = this.state;
    return (
      <div className="App">
        
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css"/>
        <link rel="stylesheet" type="text/css" charSet="UTF-8" href="https://cdnjs.cloudflare.com/ajax/libs/slick-carousel/1.6.0/slick.min.css" />
        <link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/slick-carousel/1.6.0/slick-theme.min.css" />

        <NavbarComponent key={isLoggedIn} login={isLoggedIn} email={store.getState().email}></NavbarComponent>

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

        <div style={{marginTop: '50px', display: 'block'}}>
          <Switch>
            <Route exact path='/' render = {(props) =>
                <HomePageComponent  {...props} store={store} />
            }/>
            {isLoggedIn && 
              <Route exact path='/transfer' render=
                { (props) => 
                  <TransferComponent  {...props} store={store}/>
                }
              />
            }
            {isLoggedIn && 
              <Route exact path='/queue' render=
                { (props) => 
                  <QueueComponent />
                }
              />
            }
            {isLoggedIn && admin &&
                <Route exact path='/history' render=
                  { (props) =>
                    <HistoryComponent  {...props} store={store}/>
                    }
                />
            }
            {isLoggedIn && admin &&
                <Route exact path='/clientsInfo' render=
                  { (props) =>
                    <ClientsInfoComponent {...props} store={store}/>
                    }
                />
            }
            {isLoggedIn && 
              <Route exact path='/user' render=
                { (props) => 
                  <UserAccountComponent />
                }
              />
            }
            {(isLoggedIn ) && 
              <h1 to='/transfer'>Page Not Found</h1>
            }
            
          </Switch>
        </div>

      </div>
    );
  }
}
