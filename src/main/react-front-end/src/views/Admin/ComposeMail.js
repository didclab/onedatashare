import React, { Component } from 'react';
import { Label, Glyphicon, Button, Table, Modal, ButtonToolbar, FormGroup, ControlLabel, FormControl, Form } from 'react-bootstrap';
import './ComposeMail.css';

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
                <h4 style={{ marginTop: 20 }}>
                    <Label bsStyle={'success'} bsClass='header'>
                        <Glyphicon glyph="pencil" /> Compose Mail
                    </Label>
                </h4>
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
                        <FormControl
                            id="formControlsText"
                            type="text"
                            label="Text"
                            placeholder="Enter text" />
                    </FormGroup>
                    <FormGroup controlId="message" >
                        <ControlLabel>Message</ControlLabel>
                        <textarea class="form-control" placeholder={'Enter Message'} rows="10"></textarea>
                    </FormGroup>
                    <ButtonToolbar style={{ display: 'flex', justifyContent: 'flex-end' }}>
                        <Button bsStyle="default">Clear</Button>
                        <Button bsStyle="primary">Send</Button>
                    </ButtonToolbar>
                </Form>
                <Modal
                    show={this.state.show}
                    onHide={this.smClose}
                    bsSize="large"
                    aria-labelledby="contained-modal-title-lg"
                    style={{ fontFamily: 'Monaco' }}
                >
                    <Modal.Header closeButton>
                        <Modal.Title id="contained-modal-title-lg">Select Users</Modal.Title>
                    </Modal.Header>
                    <Modal.Body>
                        <Table striped bordered condensed hover>
                            <thead>
                                <tr>
                                    <th>#</th>
                                    <th>Username</th>
                                    <th>Role</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr>
                                    <td>1</td>
                                    <td>Mark</td>
                                    <td>@mdo</td>
                                </tr>
                                <tr>
                                    <td>2</td>
                                    <td>Jacob</td>
                                    <td>@fat</td>
                                </tr>
                                <tr>
                                    <td>2</td>
                                    <td>Jacob</td>
                                    <td>@fat</td>
                                </tr>
                                <tr>
                                    <td>2</td>
                                    <td>Jacob</td>
                                    <td>@fat</td>
                                </tr>
                                <tr>
                                    <td>2</td>
                                    <td>Jacob</td>
                                    <td>@fat</td>
                                </tr>
                                <tr>
                                    <td>2</td>
                                    <td>Jacob</td>
                                    <td>@fat</td>
                                </tr>
                                <tr>
                                    <td>2</td>
                                    <td>Jacob</td>
                                    <td>@fat</td>
                                </tr>
                                <tr>
                                    <td>2</td>
                                    <td>Jacob</td>
                                    <td>@fat</td>
                                </tr>
                                <tr>
                                    <td>2</td>
                                    <td>Jacob</td>
                                    <td>@fat</td>
                                </tr>
                                <tr>
                                    <td>2</td>
                                    <td>Jacob</td>
                                    <td>@fat</td>
                                </tr>
                            </tbody>
                        </Table>
                    </Modal.Body>

                    <Modal.Footer>

                        <Button bsStyle={'primary'} onClick={this.props.onHide}>Select</Button>
                        <Button onClick={this.props.onHide}>Close</Button>
                    </Modal.Footer>
                </Modal>
            </div>
        )
    }
}
export default ComposeMail;