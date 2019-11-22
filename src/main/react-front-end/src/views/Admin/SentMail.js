import React, { Component } from 'react';
import { Label, Glyphicon } from 'react-bootstrap';

class SentMail extends Component {

    render() {
        return (
            <h3 style={{ color: 'green' }}>
                <Glyphicon glyph="check" />Sent Mail
            </h3>
        )
    }
}
export default SentMail;