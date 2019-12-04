import React, { Component } from 'react';
import { Label, Glyphicon } from 'react-bootstrap';
import './Trash.css';

class Trash extends Component {
    render() {
        return (
            <h2 style={{ marginTop: 20, flex: 4 }}>
                <Label style={styles.pageHeading}>
                    <Glyphicon glyph="trash" /> Trash
                        </Label>
            </h2>
        );
    }
}

const styles = {
    pageHeading: { backgroundColor: 'transparent', color: '#073642', padding: 7 }
}
export default Trash;
