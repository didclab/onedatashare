import React, {Component} from "react";
import {fastImage} from "../../constants";


export default class HomeInfoSlice extends Component {

    sliceTit;
    sliceDesc;
    img;

    constructor(props) {
        super(props);
        this.sliceTit = props.title;
        this.sliceDesc = props.description;
        this.img = props.image;
    }

    render() {
        return (
            <div>
                <img width={'300px'} className="image-center" src={this.img} style={{float: 'left'}} alt={this.sliceTit} />
                <h1>{this.sliceTit}</h1>
                <p>{this.sliceDesc}</p>
            </div>
        )
    }
};