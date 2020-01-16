import React, {Component} from "react";


export default class HomeInfoSlice extends Component {

    sliceTit;
    sliceDesc;

    constructor(props) {
        super(props);
        this.sliceTit = props.title;
        this.sliceDesc = props.description;
    }

    render() {
        return (
            <div>
                <h1>{this.sliceTit}</h1>
                <p>{this.sliceDesc}</p>
            </div>
        )
    }
};