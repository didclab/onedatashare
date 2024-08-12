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

import React, { Component } from "react";
import { Hidden } from "@mui/material";
import { createTheme, ThemeProvider } from "@mui/material/styles";
import Button from "@mui/material/Button";
import { siteURLS } from "../../constants";
import Grid from "@mui/material/Grid";
import Logo from "./images/logo.png";

const theme = createTheme({
  palette: {
    primary: {
      main: "#fff",
      contrastText: "#000",
    },
  },
});

export default class TitleClass extends Component {
  render() {
    return (
      <Grid container className="TitleSlice">
        <Hidden mdUp>
          <Grid item container xs={12}>
            <img className="homeImg" src={Logo} alt="OneDataShare Logo" />
          </Grid>
        </Hidden>
        <Grid item container direction="column" md={7} xs={12}>
          <Hidden smDown>
            <h1>OneDataShare</h1>
          </Hidden>
          <Hidden mdUp>
            <h1>
              OneData <br /> Share{" "}
            </h1>
          </Hidden>
          <p> Fast and secure file transfers made easy! </p>
          <br />
          <a href={siteURLS.registerPageUrl}>
            <ThemeProvider theme={theme}>
              <Button
                className="defaultButton"
                variant="contained"
                size="large"
                // color="white"
              >
                {" "}
                Get Started{" "}
              </Button>
            </ThemeProvider>
          </a>
        </Grid>
        <Hidden smDown>
          <Grid item container md={5}>
            <img className="homeImg" src={Logo} alt="OneDataShare Logo" />
          </Grid>
        </Hidden>
      </Grid>
    );
  }
}
