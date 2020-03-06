import React, {Component} from "react";
import {Hidden} from "@material-ui/core";
import Button from "@material-ui/core/Button";
import {registerPageUrl} from "../../constants.js";
import Grid from "@material-ui/core/Grid";
import Logo from "./images/logo.png";

export default class TitleClass extends Component {

    render() {
        return(
            <Grid container className="TitleSlice">
                <Hidden mdUp>
                    <Grid container xs={12}>
                        <img className='homeImg' src={Logo} alt="OneDataShare Logo" />
                    </Grid>
                </Hidden>
                <Grid container direction="column" md={7} xs={12}>
                    <Hidden smDown>
                        <h1>OneDataShare</h1>
                    </Hidden>
                    <Hidden mdUp>
                        <h1>OneData <br/> Share </h1>
                    </Hidden>
                    <p> Fast and secure file transfers made easy! </p>
                    <br/>
                    <a href={registerPageUrl}>
                        <Button className='defaultButton' variant="contained" size='large'> Get Started </Button>
                    </a>
                </Grid>
                <Hidden smDown>
                    <Grid container md={5}>
                        <img className='homeImg' src={Logo} alt="OneDataShare Logo" />
                    </Grid>
                </Hidden>
            </Grid>
        )
    }
}