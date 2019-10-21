import React, { Component } from 'react';

import NavbarComponent from "./views/NavbarComponent";
import HomePageComponent from './views/HomePageComponent';

import { Route, Switch, Redirect } from 'react-router-dom';
import { store } from './App.js';

import AccountControlComponent from "./views/Login/AccountControlComponent.js";

import TransferComponent from './views/Transfer/TransferComponent';
import HistoryComponent from './views/Admin/HistoryComponent'
import QueueComponent from './views/Queue/QueueComponent';
import UserAccountComponent from './views/Login/UserAccountComponent';
import ClientsInfoComponent from './views/Admin/ClientsInfoComponent';
import SupportComponent from './views/Support/SupportComponent';
import TermsComponent from './views/TermsComponent';
import PolicyComponent from './views/PolicyComponent';

export default class MainComponent extends Component {
  constructor(props) {
    super(props);
    this.state = {
      isLoggedIn: store.getState().login,
      admin: store.getState().admin,
    }
    this.unsubscribe = store.subscribe(() => {
      this.setState({
        isLoggedIn: store.getState().login,
        admin: store.getState().admin
      });
    });

  }
  componentWillUnmount() {
    this.unsubscribe();
  }


  render() {
    const { isLoggedIn, admin } = this.state;
    return (
      <div className="App">
        
        <link rel="stylesheet" type="text/css" charSet="UTF-8" href="https://cdnjs.cloudflare.com/ajax/libs/slick-carousel/1.6.0/slick.min.css" />
        <link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/slick-carousel/1.6.0/slick-theme.min.css" />

        <NavbarComponent key={isLoggedIn} login={isLoggedIn} email={store.getState().email}></NavbarComponent>


        <div style={{ marginTop: '50px', display: 'block' }}>
          <Switch>

            <Route path='/account' 
              render={(props) => <AccountControlComponent {...props} /> }
            />

            <Route exact path='/' 
              render={(props) =>
                <HomePageComponent  {...props} store={store} />
              } 
            />

            <Route exact path='/terms' 
              render={() =>
                <TermsComponent />
              }
            />

            <Route exact path='/policy' 
              render={() =>
                <PolicyComponent />
              }
            />

            <Route exact path="/support" 
              render={() =>
               <SupportComponent />
              } 
            />

            {isLoggedIn &&
              <Route exact path='/transfer' 
                render={(props) =>
                  <TransferComponent  {...props} store={store} />
                }
              />
            }

            {isLoggedIn &&
              <Route exact path='/queue' 
                render={ (props) => 
                  <QueueComponent {...props} />
                }
              />
            }

            {isLoggedIn && admin &&
              <Route exact path='/history' render=
                {(props) =>
                  <HistoryComponent  {...props} store={store} />
                }
              />
            }
            {isLoggedIn && admin &&
              <Route exact path='/clientsInfo' 
                render={(props) =>
                  <ClientsInfoComponent {...props} store={store} />
                }
              />
            }

            {isLoggedIn &&
              <Route exact path='/user' 
              render={ (props) => 
                  <UserAccountComponent {...props} />
                }
              />
            }

            {(isLoggedIn) &&
              <h1 to='/transfer'>Page Not Found</h1>
            }

            {!isLoggedIn &&
              <Route render={() => <Redirect to="/" />} />
            }
          </Switch>
        </div>

      </div>
    );
  }
}
