import React, { Component } from 'react';

import { store } from '../../App';
import BrowseModuleComponent from './BrowseModuleComponent';
import Button from '@material-ui/core/Button';
import Typography from '@material-ui/core/Typography';


export default class BrowserSlice extends Component{
    constructor(props) {
        super(props);
    }

    _returnBrowseComponent1() {
        // const { mode1, endpoint1, history, compact } = this.state;
        return <BrowseModuleComponent
            id="browserleft"
            mode={props.mode1}
            endpoint={props.endpoint1}
            history={props.history}
            displayStyle={props.compact ? "compact" : "comfort"}
            update={props.updateBrowseOne} />
    }

    _returnBrowseComponent2() {
        // const { mode2, endpoint2, history, compact } = this.state;

        return <BrowseModuleComponent
            id="browserright"
            mode={props.mode2}
            endpoint={props.endpoint2}
            history={props.history}
            displayStyle={props.compact ? "compact" : "comfort"}
            update={props.updateBrowseTwo}
        />
    }
}