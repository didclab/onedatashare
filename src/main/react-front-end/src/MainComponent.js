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


import React, { Component } from 'react';

import NavbarComponent from "./views/NavbarComponent";
import HomePageComponent from './views/HomePageComponent';

import { Route, Switch, Redirect } from 'react-router-dom';
import { store } from './App.js';

import AccountControlComponent from "./views/Login/AccountControlComponent.js";
import {siteURLS} from "./constants";

// import TransferComponentOld from './views/Transfer/TransferComponentOld';
import TransferComponent from './views/Transfer/TransferComponent';
import JobHistoryComponent from './views/JobHistory/JobHistoryComponent'
import QueueComponent from './views/Queue/QueueComponent';
import UserAccountComponent from './views/Login/UserAccountComponent';
// import SupportComponentOld from './views/Support/SupportComponentOld';
import SupportComponent from './views/Support/SupportComponent';
import EndpointDB from './views/Endpoint_Authorization/Endpoint_DB'
import TermsComponent from './views/TermsComponent';
import PolicyComponent from './views/PolicyComponent';
import GetStartedComponent from './views/GetStartedComponent';

import "./MainComponent.css"

// XNOTE: split the components into logged in, not logged in, and admin using HOCs

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


        <div className="content" style={{ display: 'block'}}>
          <Switch>

            <Route path={ siteURLS.accountPageUrl /*'/account'*/}
              render={(props) => <AccountControlComponent {...props} />}
            />

            <Route exact path={ siteURLS.rootUrl /*'/'*/}
              render={(props) =>
                <HomePageComponent  {...props} store={store} />
              }
            />

            <Route exact path={ siteURLS.termsUrl /*'/terms'*/}
              render={() =>
                <TermsComponent />
              }
            />

            <Route exact path={ siteURLS.policyUrl /*'/policy'*/}
              render={() =>
                <PolicyComponent />
              }
            />

            <Route exact path={ siteURLS.supportPageUrl /*"/support"*/}
              render={() =>
                <SupportComponent />
              }
            />

            <Route exact path={ siteURLS.endpoint_dbUrl /*"/endpoint_db"*/}
              render={() =>
                <EndpointDB />
              }
            />

            <Route exact path={ siteURLS.getStartedPageUrl /*"/get-started"*/}
              render={() =>
                <GetStartedComponent />
              }
            />

            {isLoggedIn &&
              <Route exact path={ siteURLS.transferPageUrl /*'/transfer'*/}
                render={(props) =>
                  <TransferComponent  {...props} store={store} />
                }
              />
            }

            {isLoggedIn &&
              <Route exact path={ siteURLS.queuePageUrl /*'/queue'*/}
                render={(props) =>
                  <QueueComponent {...props} />
                }
              />
            }
            
            {isLoggedIn &&
              <Route exact path={ siteURLS.historyPageUrl /*'/queue'*/}
                render={(props) =>
                  <JobHistoryComponent {...props} />
                }
              />
            }

            {/* {isLoggedIn && admin &&
              <Route exact path={ siteURLS.historyPageUrl '/history' } 
                render={(props) =>
                  <HistoryComponent  {...props} store={store} />
                }
              />
            } */}
            
            {isLoggedIn && admin &&
              <Route exact path={ siteURLS.userListPageUrl /*'/clientsInfo'*/}
                render={(props) =>
                  <ClientsInfoComponent {...props} store={store} />
                }
              />
            }
            {isLoggedIn && admin &&
              <Route exact path={ siteURLS.notificationPageUrl /*'/sendNotifications'*/} render=
                {(props) =>
                  <NotificationsComponent {...props} store={store} />
                }
              />
            }
            {isLoggedIn && admin &&
              <Route exact path={ siteURLS.newNotificationsUrl /*'/newNotifications'*/} render=
                {(props) =>
                  <NewNotificationsComponent {...props} store={store} />
                }
              />
            }
            {isLoggedIn &&
              <Route exact path={ siteURLS.userPageUrl /*'/user'*/}
                render={(props) =>
                  <UserAccountComponent {...props} />
                }
              />
            }

            {!isLoggedIn &&
              <Route render={() => <Redirect to={ siteURLS.rootUrl /*'/'*/} />} />
            }
          </Switch>
        </div>

      </div>
    );
  }
}
