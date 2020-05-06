import React from "react";
import {Col, Grid, Row} from "react-bootstrap";
import {humanReadableSpeed} from "../../utils";
import moment from "moment";


export default class TabContent extends React.Component {

    render() {
        const resp = this.props.resp;
        const selectedTab = this.props.selectedTab;

        if (selectedTab) {
            return <Grid className={"detailedInfo"}>
                <Row>
                    <pre>{JSON.stringify(resp, null, "\t")}</pre>
                </Row>
            </Grid>
        } else {
            return <Grid className={"detailedInfo"}>
                <Row>
                    <Col md={6}><b>User</b></Col>
                    <Col md={6}>{resp.owner}</Col>
                </Row>
                <Row>
                    <Col md={6}><b>Job ID</b></Col>
                    <Col md={6}>{resp.job_id}</Col>
                </Row>
                <Row>
                    <Col md={6}><b>Source</b></Col>
                    <Col md={6}>{decodeURIComponent(resp.src.uri)}</Col>
                </Row>
                <Row>
                    <Col md={6}><b>Destination</b></Col>
                    <Col md={6}>{decodeURIComponent(resp.dest.uri)}</Col>
                </Row>
                <Row>
                    <Col md={6}><b>Instant Speed</b></Col>
                    <Col md={6}>{humanReadableSpeed(resp.bytes.inst)}</Col>
                </Row>
                <Row>
                    <Col md={6}><b>Average Speed</b></Col>
                    <Col md={6}>{humanReadableSpeed(resp.bytes.avg)}</Col>
                </Row>
                <Row>
                    <Col md={6}><b>Scheduled Time</b></Col>
                    <Col md={6}>{moment(resp.times.scheduled).format("DD-MM-YYYY HH:mm:ss")}</Col>
                </Row>
                <Row>
                    <Col md={6}><b>Started Time</b></Col>
                    <Col md={6}>{moment(resp.times.started).format("DD-MM-YYYY HH:mm:ss")}</Col>
                </Row>
                <Row>
                    <Col md={6}><b>Completed Time</b></Col>
                    <Col md={6}>{moment(resp.times.completed).format("DD-MM-YYYY HH:mm:ss")}</Col>
                </Row>
                <Row>
                    <Col md={6}><b>Time Duration</b></Col>
                    <Col md={6}>{((resp.times.completed - resp.times.started) / 1000).toFixed(2)} sec</Col>
                </Row>
                <Row>
                    <Col md={6}><b>Attempts</b></Col>
                    <Col md={6}>{resp.attempts}</Col>
                </Row>
                <Row>
                    <Col md={6}><b>Status</b></Col>
                    <Col md={6}>{resp.status}</Col>
                </Row>
            </Grid>
        }
    }
}