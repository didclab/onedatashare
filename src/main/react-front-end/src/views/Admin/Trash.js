import React, { Component } from 'react';
import { Label, Glyphicon } from 'react-bootstrap';

class Trash extends Component {
    render() {
        return (
            <h3 style={{ color: 'green' }}>
                <Label bsStyle={'success'}>
                    <Glyphicon glyph="trash" />Trash
                </Label>
            </h3>
        );
    }
}

export default Trash;
