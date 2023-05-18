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

import { Route, Routes, Navigate } from 'react-router-dom';
import { store } from './App.js';

import AccountControlComponentWrapper from "./views/Login/AccountControlComponentWrapper";
import {siteURLS} from "./constants";

// import TransferComponentOld from './views/Transfer/TransferComponentOld';
import TransferComponentWrapper from './views/Transfer/TransferComponentWrapper';
import HistoryComponent from './views/Admin/HistoryComponent'
import QueueComponent from './views/Queue/QueueComponent';
import UserAccountComponent from './views/Login/UserAccountComponent';
import ClientsInfoComponent from './views/Admin/ClientsInfoComponent';
import NotificationsComponent from './views/Admin/NotificationsComponent';
import NewNotificationsComponent from './views/Admin/NewNotificationsComponent';
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
          <Routes>

            {/* '/account' */}
            <Route path={`${siteURLS.accountPageUrl}/*`} element={<AccountControlComponentWrapper />} />

            {/* '/' */}
            <Route path={siteURLS.rootUrl} element={<HomePageComponent store={store} />} />

            {/* '/terms' */}
            <Route path={siteURLS.termsUrl} element={<TermsComponent />} />

            {/* '/policy' */}
            <Route path={siteURLS.policyUrl} element={<PolicyComponent />} />

            {/* '/support' */}
            <Route path={siteURLS.supportPageUrl} element={<SupportComponent />} />

            {/* '/endpoint_db' */}
            <Route path={siteURLS.endpoint_dbUrl} element={<EndpointDB />} />

            {/* '/get-started' */}
            <Route path={siteURLS.getStartedPageUrl} element={<GetStartedComponent />} />

            {/* '/transfer' */}
            { isLoggedIn &&
              <Route path={siteURLS.transferPageUrl} element={<TransferComponentWrapper store={store} />} />
            }

            {/* '/queue' */}
            { isLoggedIn &&
              <Route path={siteURLS.queuePageUrl} element={<QueueComponent />} />
            }

            {/* '/history' */}
            { isLoggedIn && admin &&
              <Route path={siteURLS.historyPageUrl} element={<HistoryComponent store={store} />} />
            }

            {/* '/clientsInfo' */}
            { isLoggedIn && admin &&
              <Route path={siteURLS.userListPageUrl} element={<ClientsInfoComponent store={store} />} />
            }

            {/* '/sendNotifications' */}
            { isLoggedIn && admin &&
              <Route path={siteURLS.notificationPageUrl} element={<NotificationsComponent store={store} />} />
            }

            {/* '/newNotifications' */}
            { isLoggedIn && admin &&
              <Route path={siteURLS.newNotificationsUrl} element={<NewNotificationsComponent store={store} />} />
            }

            {/* '/user' */}
            { isLoggedIn &&
              <Route path={siteURLS.userPageUrl} element={<UserAccountComponent />} />
            }

            {/* '/' */}
            { !isLoggedIn &&
              <Route element={<Navigate to={ siteURLS.rootUrl} />} />
            }

          </Routes>
        </div>

      </div>
    );
  }
}
