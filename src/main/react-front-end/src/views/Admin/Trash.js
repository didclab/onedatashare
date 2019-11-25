import React, { Component } from 'react';
import { Label, Glyphicon } from 'react-bootstrap';
import './Trash.css';

class Trash extends Component {
    render() {
        return (
            <h4 style={{ marginTop: 20 }}>
                <Label bsStyle={'success'} bsClass='header'>
                    <Glyphicon glyph="trash" />Trash
                    </Label>
            </h4>
        );
    }
}

export default Trash;
