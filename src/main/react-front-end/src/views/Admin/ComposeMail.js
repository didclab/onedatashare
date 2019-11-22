import React, { Component } from 'react';
import { Label, Glyphicon, Button, ButtonToolbar, FormGroup, ControlLabel, FormControl, Form } from 'react-bootstrap';

class ComposeMail extends Component {

    render() {
        return (
            <div>
                <h3 style={{ color: 'green' }}>
                    <Label bsStyle={'primary'}>
                        <Glyphicon glyph="pencil" /> Compose Mail
                    </Label>
                </h3>
                <br></br>
                <Form>
                    <FormGroup>
                        <ControlLabel>To</ControlLabel>
                        <FormControl.Static>
                            <a href=''>Select recipients</a>
                        </FormControl.Static>
                    </FormGroup>
                    <FormGroup controlId={'subject'}>
                        <ControlLabel>Subject</ControlLabel>
                        <FormControl id="formControlsText"
                            type="text"
                            label="Text"
                            placeholder="Enter text" />

                    </FormGroup>
                    <FormGroup controlId="message" style={{ height: '200px' }}>
                        <ControlLabel>Message</ControlLabel>
                        <textarea class="form-control" placeholder={'Enter Message'} rows="8"></textarea>
                    </FormGroup>
                    <ButtonToolbar bsStyle={{ display: 'flex', alignItems: 'flex-end' }}>
                        <Button bsStyle="default">Clear</Button>
                        <Button bsStyle="primary">Primary</Button>
                    </ButtonToolbar>
                </Form>
            </div>
        )
    }
}
export default ComposeMail;