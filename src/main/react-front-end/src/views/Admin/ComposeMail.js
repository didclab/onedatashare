import React, { Component } from 'react';
import { Label, Glyphicon, Button, Table, Checkbox, InputGroup, Modal, ButtonToolbar, FormGroup, ControlLabel, FormControl, Form } from 'react-bootstrap';
import './ComposeMail.css';
import { getAllUsers, sendEmailNotification } from '../../APICalls/APICalls';
import { cookies } from "../../model/reducers";

class ComposeMail extends Component {

    constructor(props) {
        super(props);
        this.state = {
            show: false,
            selectAll: false
        }
    }

    handleChange = name => event => {
        this.setState({
            [name]: event.target.value,
        });
    };

    smClose = () => {
        this.setState({ show: false });
    }

    smOpen = () => {
        this.setState({ show: true });
    }

    async componentDidMount() {
        const users = await getAllUsers(cookies.get('email'))
        if (users && users.length > 0) {
            this.setState({ users });
        } else {
            console.log("empty user list");
        }
        console.log("Get user list");
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
                    <FormGroup controlId={'to'}>
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
                    <Modal.Header style={{ display: 'flex', flexDirection: 'row' }}>
                        <Modal.Title id="contained-modal-title-lg" style={{ flex: 1 }}>Select Users</Modal.Title>
                        <FormGroup controlId={'search'} style={{ flex: 3, justifyContent: 'flex-start', width: '50%' }}>
                            <InputGroup>
                                <FormControl type="text" />
                                <InputGroup.Addon>
                                    <Glyphicon glyph="search" />
                                </InputGroup.Addon>
                            </InputGroup>
                        </FormGroup>
                    </Modal.Header>
                    <Modal.Body>
                        <Table responsive>
                            <thead>
                                <tr>
                                    <th>
                                        <FormGroup controlId={'selectAll'}>
                                            <Checkbox checked={false} style={{ margin: 0 }} onChange={this.handleChange('selectAll')} value={this.state.selectAll} ></Checkbox>
                                        </FormGroup>
                                    </th>
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
                        <Button onClick={this.smClose}>Close</Button>
                    </Modal.Footer>
                </Modal>
            </div>
        )
    }
}
export default ComposeMail;