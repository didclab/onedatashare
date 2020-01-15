import React, {Component} from "react";

const {sliceTit, sliceDesc} = props;

export default class HomeInfoSlice extends Component {

    constructor(props) {
        super(props);
    }

    render() {
        return (
            <div>
                <h1>{sliceTit}</h1>
                <p>{sliceDesc}</p>
            </div>
        )
    }
};