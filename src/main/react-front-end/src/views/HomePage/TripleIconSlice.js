import React, {Component} from "react";
import Grid from "@material-ui/core/Grid";

export default class TripleIconSlice extends Component {

    constructor(props){
        super(props);
    }

    render(){
        return(
            <Grid className="TripleIconSlice" container direction='row'>
                <Grid className="tripleContent" container xs={12} md={4}>
                    <h2>Hi</h2>
                </Grid>
                <Grid className="tripleContent" container xs={12}  md={4}>
                    <p> Hi </p>
                </Grid>
                <Grid className="tripleContent" container xs={12}  md={4}>
                    <p> Hi </p>
                </Grid>
            </Grid>
        );
    }
}