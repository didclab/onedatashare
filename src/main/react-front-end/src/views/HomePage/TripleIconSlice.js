import React, {Component} from "react";
import Grid from "@material-ui/core/Grid";

export default class TripleIconSlice extends Component {

    constructor(props){
        super(props);
    }

    render(){
        return(
            <Grid className="tripleContent" container xs={12} sm={4} direction='column'>
                <img className='iconTrio' src={this.props.img} alt={this.props.imgAltTxt} />
                <h2>{this.props.title}</h2>
            </Grid>
        );
    }
}