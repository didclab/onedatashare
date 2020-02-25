import Grid from "@material-ui/core/Grid";
import {nsfImage, ubImage} from "../../constants";
import React, {Component} from "react";

export default class RecognitionSlice extends Component {

    render() {
        return (
            <Grid container
                  direction='row'
                  justify='center'
                  alignItems='center'
                  className='RecognitionSlice'>

                <img width={'100px'} src={nsfImage} style={{float: 'left'}} alt="NSF Logo"/>
                <img width={'100px'} src={ubImage} style={{float: 'left'}} alt="UB Logo"/>

            </Grid>
        );
    }
}