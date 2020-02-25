import React from "react";
import TableRow from "@material-ui/core/TableRow";
import TableCell from "@material-ui/core/TableCell";
import AppBar from "@material-ui/core/AppBar";
import Tabs from "@material-ui/core/Tabs";
import Tab from "@material-ui/core/Tab";
import {ProgressBar} from "react-bootstrap";
import Tooltip from "@material-ui/core/Tooltip";
import Zoom from "@material-ui/core/Zoom";
import Button from "@material-ui/core/Button";
import Info from "@material-ui/icons/Info";
import Cancel from "@material-ui/icons/Cancel";
import Refresh from "@material-ui/icons/Refresh";
import DeleteOutline from "@material-ui/icons/DeleteOutline";
import {humanReadableSpeed} from "../../utils";
import {TabContent} from "./TabContent";

const tbcellStyle = {textAlign: 'center'}

export class RowElement extends React.Component {

    constructor(props) {
        super(props)
        this.state = { selectedTab: 0 }
        this.toggleTabs = this.toggleTabs.bind(this)
    }

    toggleTabs() {
        const { selectedTab } = this.state
        this.setState({selectedTab: !selectedTab})
    }

    infoRow() {
        const { resp } = this.props
        const { selectedTab } = this.state
        return <TableRow>
            <TableCell colSpan={7} style={{...tbcellStyle, fontSize: '1rem', backgroundColor: '#e8e8e8', margin: '2%' }}>
                <div id="infoBox" style={{ marginBottom : '0.5%' }}>
                    <AppBar position="static" style={{ boxShadow: 'unset' }}>
                        <Tabs value={selectedTab ? 1: 0} onChange={this.toggleTabs} style={{ backgroundColor: '#e8e8e8' }}>
                            <Tab style={{ backgroundColor: '#428bca', margin: '0.5%', borderRadius: '4px' }} label="Formatted" />
                            <Tab style={{ backgroundColor: '#428bca', margin: '0.5%', borderRadius: '4px' }} label="JSON" />
                        </Tabs>
                    </AppBar>
                    <div style={{ backgroundColor: 'white', borderRadius: '4px', textAlign: 'left', marginTop: '0.3%'}}>
                        <TabContent resp={resp} selectedTab={selectedTab}/>
                    </div>
                </div>
            </TableCell>
        </TableRow>
    }

    getStatus(status, total, done) {
        //TODO: move to CSS file
        let now, bsStyle, label
        if (status === 'complete') {
            now = 100
            bsStyle = ''
            label = 'Complete'
        } else if (status === 'failed') {
            now = 100
            bsStyle = 'danger'
            label = 'Failed'
        } else if (status === 'removed' || status === 'cancelled') {
            now = 100
            bsStyle = 'danger'
            label = 'Cancelled'
        } else {
            now = ((done / total) * 100).toFixed()
            bsStyle = 'warning'
            label = `Transferring ${now}%`
        }

        if(bsStyle === '') {
            return <ProgressBar label={label} now={now} />
        } else {
            return <ProgressBar bsStyle={bsStyle} label={label} now={now} />
        };
    }
    renderActions(owner, jobID, status, deleted) {
        const { infoButtonOnClick, cancelButtonOnClick, restartButtonOnClick, deleteButtonOnClick } = this.props
        // Convert all these to CSS files
        return <div>
            <Tooltip TransitionComponent={Zoom} placement="top" title="Detailed Information">
                <Button onClick={infoButtonOnClick.bind(null, owner, jobID)} variant="contained" size="small" color="primary"
                        style={{
                            backgroundColor: 'rgb(224, 224, 224)', color: '#333333', fontFamily: 'FontAwesome', fontSize: '1.5rem', height: '30%',
                            fontWeight: 'bold', width: '20%', textTransform: 'none',
                            minWidth: '0px', minHeigth: '0px'
                        }}>
                    <Info />
                </Button>
            </Tooltip>
            {(status === 'transferring' || status === 'scheduled' || status === 'transferring') &&
            <Tooltip TransitionComponent={Zoom} title="Cancel">
                <Button onClick={cancelButtonOnClick.bind(null, jobID)} variant="contained" size="small" color="primary"
                        style={{
                            backgroundColor: 'rgb(224, 224, 224)', color: '#333333', fontSize: '1.5rem', fontWeight: 'bold', width: '20%', height: '20%',
                            textTransform: 'none', minWidth: '0px', minHeigth: '0px'
                        }}>
                    <Cancel />
                </Button>
            </Tooltip>
            }
            {status !== 'transferring' && status !== 'scheduled' &&
            <Tooltip TransitionComponent={Zoom} title="Restart">
                <Button onClick={restartButtonOnClick.bind(null, jobID)} variant="contained" size="small" color="primary"
                        style={{
                            backgroundColor: 'rgb(224, 224, 224)', color: '#333333', fontSize: '1.5rem', fontWeight: 'bold', width: '20%', height: '20%',
                            textTransform: 'none', minWidth: '0px', minHeigth: '0px'
                        }}>
                    <Refresh />
                </Button>
            </Tooltip>
            }
            {status !== 'transferring' && status !== 'scheduled' && !deleted &&
            <Tooltip TransitionComponent={Zoom} title="Delete">
                <Button onClick={deleteButtonOnClick.bind(null, jobID)} variant="contained" size="small" color="primary"
                        style={{
                            backgroundColor: 'rgb(224, 224, 224)', color: '#333333', fontSize: '1.5rem', fontWeight: 'bold', width: '20%', height: '20%',
                            textTransform: 'none', minWidth: '0px', minHeigth: '0px'
                        }}>
                    <DeleteOutline />
                </Button>
            </Tooltip>
            }
        </div>
    }
    render() {
        const { resp, infoVisible } = this.props
        return <React.Fragment>
            <TableRow style={{alignSelf: "stretch"}}>
                {/* <TableCell scope="row" style={{...tbcellStyle, width: '15%',  fontSize: '1.2rem'}}>
					{resp.owner}
				</TableCell> */}
                <TableCell style={{...tbcellStyle, width: '5%',  fontSize: '1.2rem'}} numeric="true">
                    {resp.job_id}
                </TableCell>
                <TableCell style={{...tbcellStyle, width: '30%',  fontSize: '1.2rem'}}>
                    {this.getStatus(resp.status, resp.bytes.total, resp.bytes.done)}
                </TableCell>
                <TableCell style={{...tbcellStyle, width: '5%', maxWidth: '20vw', overflow:"hidden", fontSize: '1.2rem', margin: "0px", maxHeight: "10px"}}>
                    {humanReadableSpeed(resp.bytes.avg)}
                </TableCell>
                <TableCell style={{...tbcellStyle, width: '25%',  fontSize: '1.2rem'}}>
                    {decodeURIComponent(resp.src.uri)} <b>-></b> {decodeURIComponent(resp.dest.uri)}
                </TableCell>
                <TableCell style={{...tbcellStyle, width: '15%',  fontSize: '1.2rem'}}>
                    {this.renderActions(resp.owner, resp.job_id, resp.status, resp.deleted)}
                </TableCell>
            </TableRow>
            { infoVisible && this.infoRow() }
        </React.Fragment>
    }
}