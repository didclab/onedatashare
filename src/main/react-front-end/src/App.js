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

import React, { useState, useEffect } from "react";
import "./App.css";
import MainComponent from "./MainComponent";
import TransferComponent from "./views/Transfer/TransferComponent";
import OauthProcessComponent from "./views/OauthProcessComponent";
import { createStore } from "redux";
import { onedatashareModel } from "./model/reducers";
import { Route, Routes } from "react-router-dom";

import Snackbar from "@mui/material/Snackbar";
import Button from "@mui/material/Button";
import { siteURLS } from "./constants";
import EventEmitter from "eventemitter3";

import { initializeReactGA } from "./analytics/ga";
import SupportComponent from "./views/Support/SupportComponent";
import PolicyComponent from "./views/PolicyComponent";
import TermsComponent from "./views/TermsComponent";

export const eventEmitter = new EventEmitter();

export const store = createStore(onedatashareModel);

const App = () => {
  const [loaded, setLoaded] = useState(false);
  const [open, setOpen] = useState(false);
  const [vertical, setVertical] = useState("top");
  const [horizontal, setHorizontal] = useState("center");
  const [error, setError] = useState(null);

  const handleOpen = (errormsg) => {
    console.log(errormsg);
    setOpen(true);
    setVertical("top");
    setHorizontal("center");
    setError(JSON.stringify(errormsg));
    setTimeout(handleClose, 4000);
  };

  const handleClose = () => {
    setOpen(false);
  };

  useEffect(() => {
    setLoaded(true);
    eventEmitter.on("errorOccured", handleOpen);

    return () => {
      eventEmitter.off("errorOccured", handleOpen);
    };
  }, []);

  return (
    <div>
      <Snackbar
        anchorOrigin={{ vertical, horizontal }}
        style={{ marginTop: "20px", zIndex: 1500 }}
        open={open}
        onClose={handleClose}
        ContentProps={{
          "aria-describedby": "message-id",
        }}
        action={
          <Button onClick={handleClose} color="secondary" size="small">
            Close
          </Button>
        }
        message={<span id="message-id">{error}</span>}
      />

      <Routes>
        {/*<Route path='/account' component={AccountControlComponent}/>*/}
        <Route path="/oauth/:tag" element={<OauthProcessComponent />} />
        <Route exact path="/*" element={<MainComponent />} />
        {/* <Route
          exact
          path={siteURLS.supportPageUrl}
          element={<SupportComponent />}
        />
        <Route exact path={siteURLS.termsUrl} element={<TermsComponent />} /> */}
      </Routes>
    </div>
  );
};

export default App;
