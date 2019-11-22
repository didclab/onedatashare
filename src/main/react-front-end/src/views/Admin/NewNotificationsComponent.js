import React, { Component } from 'react';
import { Grid, Row, Col, Nav, NavItem, Glyphicon } from 'react-bootstrap';
import ComposeMail from './ComposeMail';
import SentMail from './SentMail';
import Trash from './Trash';

class NewNotificationsComponent extends Component {
    constructor(props) {
        super(props);
        this.state = {
            activekey: 2
        }
    };

    handleSelect = (event) => {
        this.setState({ activekey: event });
    }

    render() {
        return (
            <Grid style={{ width: '100%', height: '100%', paddingTop: '0', marginTop: '-13px' }}>
                <Row className="show-grid" style={{ height: '100%' }}>
                    <Col xs={12} sm={3} md={3} lg={2} style={{ backgroundColor: '#133926', height: '100%' }}>
                        <Nav bsStyle="pills" stacked activeKey={this.state.activekey} style={{ height: '100%' }} onSelect={this.handleSelect}>
                            <div>
                                <h4 style={{ color: 'white' }}>
                                    Notifications
                                </h4>
                            </div>
                            <br></br>
                            <NavItem eventKey={1} style={{ color: 'white', backgroundColor: 'transparent', alignItems: 'center' }}>
                                <Glyphicon glyph="pencil" /> Compose Mail
                            </NavItem>
                            <NavItem eventKey={2} style={{ color: 'white' }}>
                                <Glyphicon glyph="check" />  Sent Mail
                            </NavItem>
                            <NavItem eventKey={3} style={{ color: 'white' }}>
                                <Glyphicon glyph="trash" />  Trash
                            </NavItem>
                        </Nav>
                    </Col>
                    <Col xs={12} sm={9} md={9} lg={10}>
                        {this.state.activekey === 1 ?
                            (<div><ComposeMail /></div>)
                            : this.state.activekey === 2 ?
                                <div><SentMail /></div>
                                : this.state.activekey === 3 ?
                                    <Trash />
                                    : <div></div>}
                    </Col>
                </Row>
            </Grid>

        );
    }
}

export default NewNotificationsComponent;
