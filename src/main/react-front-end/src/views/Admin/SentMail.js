import React, { Component, StyleSheet } from 'react';
import { Label, Glyphicon, Table, Button, ButtonToolbar } from 'react-bootstrap';
import './SentMail.css';

class SentMail extends Component {

    render() {
        return (
            <div style={{ display: 'flex', flexDirection: 'column' }}>
                <h4 style={{ marginTop: 20, flex: 1 }}>
                    <Label bsStyle={'success'} bsClass='header'>
                        <Glyphicon glyph="check" />Sent Mail
                    </Label>
                </h4>
                <div style={{ flex: 3, overflowY: 'scroll' }}>
                    <Table responsive >
                        <thead>
                            <tr>
                                <th>#</th>
                                <th>Recipients</th>
                                <th>Subject</th>
                                <th>Message</th>
                                <th>isHtml</th>
                                <th>Options</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>1</td>
                                <td>32 Users</td>
                                <td>Hello Everybody</td>
                                <td>this is a mail so that it can be used to send sened snfdksjk</td>
                                <td>True</td>
                                <td>
                                    <ButtonToolbar>
                                        <Button bsStyle={'primary'} bsSize="xs">
                                            <Glyphicon glyph='exclamation-sign' />
                                        </Button>
                                        <Button bsStyle={'danger'} bsSize="xs">
                                            <Glyphicon glyph='trash' />
                                        </Button>
                                    </ButtonToolbar>
                                </td>

                            </tr>
                            <tr>
                                <td>2</td>
                                <td>Table cell</td>
                                <td>Table cell</td>
                                <td>Table cell</td>
                                <td>Table cell</td>
                                <td>
                                    <ButtonToolbar>
                                        <Button bsStyle={'primary'} bsSize="xs">
                                            <Glyphicon glyph='exclamation-sign' />
                                        </Button>
                                        <Button bsStyle={'danger'} bsSize="xs">
                                            <Glyphicon glyph='trash' />
                                        </Button>
                                    </ButtonToolbar>
                                </td>

                            </tr>
                            <tr>
                                <td>3</td>
                                <td>Table cell</td>
                                <td>Table cell</td>
                                <td>Table cell</td>
                                <td>Table cell</td>
                                <td>
                                    <ButtonToolbar>
                                        <Button bsStyle={'primary'} bsSize="xs">
                                            <Glyphicon glyph='exclamation-sign' />
                                        </Button>
                                        <Button bsStyle={'danger'} bsSize="xs">
                                            <Glyphicon glyph='trash' />
                                        </Button>
                                    </ButtonToolbar>
                                </td>
                            </tr>
                            <tr>
                                <td>1</td>
                                <td>32 Users</td>
                                <td>Hello Everybody</td>
                                <td>this is a mail so that it can be used to send sened snfdksjk</td>
                                <td>True</td>
                                <td>
                                    <ButtonToolbar>
                                        <Button bsStyle={'primary'} bsSize="xs">
                                            <Glyphicon glyph='exclamation-sign' />
                                        </Button>
                                        <Button bsStyle={'danger'} bsSize="xs">
                                            <Glyphicon glyph='trash' />
                                        </Button>
                                    </ButtonToolbar>
                                </td>

                            </tr>
                            <tr>
                                <td>1</td>
                                <td>32 Users</td>
                                <td>Hello Everybody</td>
                                <td>this is a mail so that it can be used to send sened snfdksjk</td>
                                <td>True</td>
                                <td>
                                    <ButtonToolbar>
                                        <Button bsStyle={'primary'} bsSize="xs">
                                            <Glyphicon glyph='exclamation-sign' />
                                        </Button>
                                        <Button bsStyle={'danger'} bsSize="xs">
                                            <Glyphicon glyph='trash' />
                                        </Button>
                                    </ButtonToolbar>
                                </td>

                            </tr>
                            <tr>
                                <td>1</td>
                                <td>32 Users</td>
                                <td>Hello Everybody</td>
                                <td>this is a mail so that it can be used to send sened snfdksjk</td>
                                <td>True</td>
                                <td>
                                    <ButtonToolbar>
                                        <Button bsStyle={'primary'} bsSize="xs">
                                            <Glyphicon glyph='exclamation-sign' />
                                        </Button>
                                        <Button bsStyle={'danger'} bsSize="xs">
                                            <Glyphicon glyph='trash' />
                                        </Button>
                                    </ButtonToolbar>
                                </td>

                            </tr>
                            <tr>
                                <td>1</td>
                                <td>32 Users</td>
                                <td>Hello Everybody</td>
                                <td>this is a mail so that it can be used to send sened snfdksjk</td>
                                <td>True</td>
                                <td>
                                    <ButtonToolbar>
                                        <Button bsStyle={'primary'} bsSize="xs">
                                            <Glyphicon glyph='exclamation-sign' />
                                        </Button>
                                        <Button bsStyle={'danger'} bsSize="xs">
                                            <Glyphicon glyph='trash' />
                                        </Button>
                                    </ButtonToolbar>
                                </td>

                            </tr>
                            <tr>
                                <td>1</td>
                                <td>32 Users</td>
                                <td>Hello Everybody</td>
                                <td>this is a mail so that it can be used to send sened snfdksjk</td>
                                <td>True</td>
                                <td>
                                    <ButtonToolbar>
                                        <Button bsStyle={'primary'} bsSize="xs">
                                            <Glyphicon glyph='exclamation-sign' />
                                        </Button>
                                        <Button bsStyle={'danger'} bsSize="xs">
                                            <Glyphicon glyph='trash' />
                                        </Button>
                                    </ButtonToolbar>
                                </td>

                            </tr>
                            <tr>
                                <td>1</td>
                                <td>32 Users</td>
                                <td>Hello Everybody</td>
                                <td>this is a mail so that it can be used to send sened snfdksjk</td>
                                <td>True</td>
                                <td>
                                    <ButtonToolbar>
                                        <Button bsStyle={'primary'} bsSize="xs">
                                            <Glyphicon glyph='exclamation-sign' />
                                        </Button>
                                        <Button bsStyle={'danger'} bsSize="xs">
                                            <Glyphicon glyph='trash' />
                                        </Button>
                                    </ButtonToolbar>
                                </td>

                            </tr>
                            <tr>
                                <td>1</td>
                                <td>32 Users</td>
                                <td>Hello Everybody</td>
                                <td>this is a mail so that it can be used to send sened snfdksjk</td>
                                <td>True</td>
                                <td>
                                    <ButtonToolbar>
                                        <Button bsStyle={'primary'} bsSize="xs">
                                            <Glyphicon glyph='exclamation-sign' />
                                        </Button>
                                        <Button bsStyle={'danger'} bsSize="xs">
                                            <Glyphicon glyph='trash' />
                                        </Button>
                                    </ButtonToolbar>
                                </td>

                            </tr>
                            <tr>
                                <td>1</td>
                                <td>32 Users</td>
                                <td>Hello Everybody</td>
                                <td>this is a mail so that it can be used to send sened snfdksjk</td>
                                <td>True</td>
                                <td>
                                    <ButtonToolbar>
                                        <Button bsStyle={'primary'} bsSize="xs">
                                            <Glyphicon glyph='exclamation-sign' />
                                        </Button>
                                        <Button bsStyle={'danger'} bsSize="xs">
                                            <Glyphicon glyph='trash' />
                                        </Button>
                                    </ButtonToolbar>
                                </td>

                            </tr>
                            <tr>
                                <td>1</td>
                                <td>32 Users</td>
                                <td>Hello Everybody</td>
                                <td>this is a mail so that it can be used to send sened snfdksjk</td>
                                <td>True</td>
                                <td>
                                    <ButtonToolbar>
                                        <Button bsStyle={'primary'} bsSize="xs">
                                            <Glyphicon glyph='exclamation-sign' />
                                        </Button>
                                        <Button bsStyle={'danger'} bsSize="xs">
                                            <Glyphicon glyph='trash' />
                                        </Button>
                                    </ButtonToolbar>
                                </td>

                            </tr>
                            <tr>
                                <td>1</td>
                                <td>32 Users</td>
                                <td>Hello Everybody</td>
                                <td>this is a mail so that it can be used to send sened snfdksjk</td>
                                <td>True</td>
                                <td>
                                    <ButtonToolbar>
                                        <Button bsStyle={'primary'} bsSize="xs">
                                            <Glyphicon glyph='exclamation-sign' />
                                        </Button>
                                        <Button bsStyle={'danger'} bsSize="xs">
                                            <Glyphicon glyph='trash' />
                                        </Button>
                                    </ButtonToolbar>
                                </td>

                            </tr>
                            <tr>
                                <td>1</td>
                                <td>32 Users</td>
                                <td>Hello Everybody</td>
                                <td>this is a mail so that it can be used to send sened snfdksjk</td>
                                <td>True</td>
                                <td>
                                    <ButtonToolbar>
                                        <Button bsStyle={'primary'} bsSize="xs">
                                            <Glyphicon glyph='exclamation-sign' />
                                        </Button>
                                        <Button bsStyle={'danger'} bsSize="xs">
                                            <Glyphicon glyph='trash' />
                                        </Button>
                                    </ButtonToolbar>
                                </td>
                            </tr>
                        </tbody>

                    </Table>
                </div>
            </div >
        )
    }
};

export default SentMail;
