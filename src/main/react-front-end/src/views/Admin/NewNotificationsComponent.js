import React, { Component, StyleSheet } from 'react';
import { Grid, Row, Col, Nav, NavItem, Glyphicon } from 'react-bootstrap';
import ComposeMail from './ComposeMail';
import SentMail from './SentMail';
import Trash from './Trash';

class NewNotificationsComponent extends Component {
    constructor(props) {
        super(props);
        this.state = {
            activekey: 1
        }
    };

    handleSelect = (event) => {
        this.setState({ activekey: event });
    }

    render() {
        return (
            <Grid style={styles.gridStyle}>
                <Row className="show-grid" style={{ height: '100%' }}>
                    <Col xs={12} sm={3} md={3} lg={2} style={styles.sideNav}>
                        <Nav bsStyle="pills" stacked activeKey={this.state.activekey} style={{ height: '100%' }} onSelect={this.handleSelect}>
                            <div>
                                <h4 style={styles.sideNavHead}>
                                    Notifications
                                </h4>
                            </div>
                            <br></br>
                            <NavItem eventKey={1}>
                                <Glyphicon glyph="pencil" /> Compose Mail
                            </NavItem>
                            <NavItem eventKey={2}>
                                <Glyphicon glyph="check" />  Sent Mail
                            </NavItem>
                            <NavItem eventKey={3}>
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

const styles = {
    gridStyle: { width: '100%', height: '100%', paddingTop: '0', marginTop: '-13px', fontFamily: 'Monaco' },
    sideNav: { backgroundColor: '#073642', height: '100%' },
    sideNavHead: { color: 'white' }
}
