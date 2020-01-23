import React, {Component} from "react";
import {Hidden} from "@material-ui/core";
import Button from "@material-ui/core/Button";
import {registerPageUrl} from "../../constants.js";

export default class TitleClass extends Component {

    render() {
        return(
            <div className='TitleSlice'>
                <Hidden smDown>
                    <h1>OneDataShare</h1>
                </Hidden>
                <Hidden mdUp>
                    <h1>OneData <br/> Share </h1>
                </Hidden>
                <p> Reliable and fast data movement in the cloud </p>
                <br/>
                <a href={registerPageUrl}>
                    <Button className='defaultButton' variant="contained" size='large'> Get Started </Button>
                </a>
            </div>
        )
    }
}