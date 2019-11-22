import React, { Component } from 'react';
import { Label, Glyphicon, Button, Modal, ButtonToolbar, FormGroup, ControlLabel, FormControl, Form } from 'react-bootstrap';

class ComposeMail extends Component {

    constructor(props) {
        super(props);
        this.state = {
            show: false
        }
    }
    smClose = () => {
        this.setState({ show: false });
    }

    smOpen = () => {
        this.setState({ show: true });
    }


    render() {
        return (
            <div>
                <h3 style={{ color: 'green' }}>
                    <Label bsStyle={'success'}>
                        <Glyphicon glyph="pencil" /> Compose Mail
                    </Label>
                </h3>
                <br></br>
                <Form>
                    <FormGroup>
                        <ControlLabel>To</ControlLabel>
                        <FormControl.Static>
                            <a onClick={this.smOpen}>Select recipients </a>
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
                    <ButtonToolbar pullRight>
                        <Button bsStyle="default">Clear</Button>
                        <Button bsStyle="primary">Primary</Button>
                    </ButtonToolbar>
                </Form>
                <Modal
                    show={this.state.show}
                    onHide={this.smClose}
                    bsSize="large"
                    aria-labelledby="contained-modal-title-lg"
                >
                    <Modal.Header closeButton>
                        <Modal.Title id="contained-modal-title-lg">Select Users</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        <h4>Wrapped Text</h4>
                        <p>
                            Cras mattis consectetur purus sit amet fermentum. Cras justo odio,
                            dapibus ac facilisis in, egestas eget quam. Morbi leo risus, porta
                            ac consectetur ac, vestibulum at eros.
                        </p>
                        <p>
                            Praesent commodo cursus magna, vel scelerisque nisl consectetur et.
                            Vivamus sagittis lacus vel augue laoreet rutrum faucibus dolor
                            auctor.
                        </p>
                    </Modal.Body>
                    <Modal.Footer>
                        <Button onClick={this.props.onHide}>Close</Button>
                    </Modal.Footer>
                </Modal>
            </div>
        )
    }
}
export default ComposeMail;