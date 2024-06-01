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
import Typography from "@material-ui/core/Typography";
import CardActions from "@material-ui/core/CardActions";
import Button from "@material-ui/core/Button";
import { ValidatorForm, TextValidator } from "react-material-ui-form-validator";
import { Link } from "react-router-dom";

import PropTypes from "prop-types";

import { checkLogin } from "../../APICalls/APICalls.js";

import { spaceBetweenStyle } from "../../constants";
import { updateGAPageView } from "../../analytics/ga";
import VisibilityOutlinedIcon from "@material-ui/icons/VisibilityOutlined";
import VisibilityOffOutlinedIcon from "@material-ui/icons/VisibilityOffOutlined";
import InputAdornment from "@material-ui/core/InputAdornment";
import IconButton from "@material-ui/core/IconButton";

const ODSLogoBlack = require("./images/logoBlack.png");

export default class NewLoginComponent extends Component {
  static propTypes = {
    email: PropTypes.string,
    createAccountPressed: PropTypes.func,
    lostValidationCodePressed: PropTypes.func,
    forgotPasswordPressed: PropTypes.func,
    isLoading: PropTypes.func,
    userLoggedIn: PropTypes.func,
  };

  constructor(props) {
    super(props);
    this.state = {
      email: "",
      password: "",
      emailChecked: false,
      error: false,
      errorMessage: null,
      remember: false,
      isAuthenticated: false,
      isPasswordVisible: false,
    };

    this.emailValidated = false;
    this.onEmailNextClicked = this.onEmailNextClicked.bind(this);
    this.onSignInClicked = this.onSignInClicked.bind(this);
    this.handleShowPassword = this.handleShowPassword.bind(this);
    this.handleHidePassword = this.handleHidePassword.bind(this);
    updateGAPageView();
  }
  componentDidMount() {
    if (this.props.email) {
      this.setState({ email: this.props.email });
    }
  }

  onEmailNextClicked() {
    const { isLoading } = this.props;
    isLoading(true);

    checkLogin(
      this.state.email,
      (success) => {
        isLoading(false);
        this.setState({ emailChecked: true });
      },
      (error) => {
        isLoading(false);
        this.setState({
          emailChecked: false,
          error: true,
          errorMessage: "Email not found or server error.",
        });
      }
    );
  }

  onSignInClicked() {
    const { isLoading, userLoggedIn } = this.props;
    var { email, password, remember } = this.state;
    isLoading(true);

    userLoggedIn(email, password, remember, (error) => {
      isLoading(false);
      console.log("error message", error);
      this.setState({
        error: true,
        errorMessage: "Wrong password or server error.",
      });
    });
  }
  /**
   * @deprecated
   */

  handleShowPassword() {
    this.setState({ isPasswordVisible: true });
  }
  /**
   * @deprecated
   */

  handleHidePassword() {
    this.setState({ isPasswordVisible: false });
  }

  toggleShowPassword = () => {
    this.setState({ isPasswordVisible: !this.state.isPasswordVisible });
  };

  render() {
    const { forgotPasswordPressed } = this.props;
    const {
      emailChecked,
      email,
      password,
      error,
      errorMessage,
      isPasswordVisible,
    } = this.state;
    const handleChange = (name) => (event) => {
      this.setState({
        error: false,
        errorMessage: null,
        [name]: event.target.value,
      });
    };

    return (
      <div>
        {!emailChecked && (
          <div className="enter-from-right slide-in">
            <Typography
              style={{ fontSize: "1em", textAlign: "center" }}
              color="textSecondary"
            >
              <img src={ODSLogoBlack} width="100px" alt="Onedatashare logo" />
            </Typography>
            <Typography
              style={{
                fontSize: "1.6em",
                marginBottom: "0.4em",
                textAlign: "center",
                marginTop: "0.5em",
              }}
            >
              Sign In
            </Typography>

            <ValidatorForm ref="email" onSubmit={this.onEmailNextClicked}>
              <TextValidator
                error={error}
                helperText={errorMessage}
                label="Email"
                onChange={handleChange("email")}
                id="email"
                name="email"
                value={email}
                validators={["required", "isEmail"]}
                errorMessages={[
                  "Email field cannot be empty",
                  "Cannot understand email format",
                ]}
                style={{ width: "90%", margin: "5%" }}
                InputProps={{
                  style: { fontSize: 14 },
                }}
              />
              <CardActions style={spaceBetweenStyle}>
                <Link to="/account/register">
                  <Button size="medium" variant="outlined" color="primary">
                    Create Account
                  </Button>
                </Link>

                <Button
                  size="medium"
                  variant="contained"
                  color="primary"
                  type="submit"
                >
                  Next
                </Button>
              </CardActions>
            </ValidatorForm>
          </div>
        )}
        {emailChecked && (
          <div className="enter-from-right slide-in">
            <Typography style={{ fontSize: "1.6em", marginBottom: "0.4em" }}>
              Hi {email.substring(0, email.indexOf("@"))}!
            </Typography>
            <Button
              size="medium"
              style={{ borderRadius: "20px" }}
              variant="outlined"
              color="primary"
              onClick={() => this.setState({ emailChecked: false })}
            >
              {email}
            </Button>

            <ValidatorForm ref="password" onSubmit={this.onSignInClicked}>
              <TextValidator
                error={error}
                helperText={errorMessage}
                id="Password"
                label="Enter Your Password"
                onChange={handleChange("password")}
                type={isPasswordVisible ? "text" : "password"}
                name="password"
                value={password}
                validators={["required"]}
                errorMessages={["Password Field Cannot Be Empty"]}
                style={{ width: "100%", marginTop: "5%", marginBottom: "5%" }}
                InputProps={{
                  style: {
                    fontSize: 14,
                  },
                  endAdornment: (
                    <React.Fragment>
                      <InputAdornment position="end">
                        <IconButton
                          aria-label="toggle password visibility"
                          onClick={this.toggleShowPassword}
                        >
                          {isPasswordVisible ? (
                            <VisibilityOutlinedIcon />
                          ) : (
                            <VisibilityOffOutlinedIcon />
                          )}
                        </IconButton>
                      </InputAdornment>
                    </React.Fragment>
                  ),
                }}
              />
              {/* <FormControlLabel
			control={
	            <Checkbox checked={remember} value="remember"
	            onChange={(event)=>{
	            	this.setState({remember: event.target.checked});
	            }}
	            color="primary"
	            />
	        } label="Remember"/> */}

              <CardActions
                style={{ ...spaceBetweenStyle, marginBottom: "20px" }}
              >
                <Button
                  size="medium"
                  variant="outlined"
                  color="primary"
                  onClick={() => forgotPasswordPressed(email)}
                >
                  Forgot Password?
                </Button>
                <Button
                  size="medium"
                  variant="contained"
                  color="primary"
                  type="submit"
                >
                  Next
                </Button>
              </CardActions>
            </ValidatorForm>
          </div>
        )}
      </div>
    );
  }
}
