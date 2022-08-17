import React, {Component} from "react";
import Paper from "@material-ui/core/Paper";
import Table from "@material-ui/core/Table";
import QueueHeader from "./QueueHeader";
import QueueBody from "./QueueBody";
import {getJobsForUser} from "../../APICalls/APICalls";


class QueueGridComponent extends Component {

    constructor(props) {
        super(props);

        this.state = {
            dataToDisplay: [],
        }
        this.queueFunc = this.queueFunc.bind(this)
        this.queueFuncSuccess = this.queueFuncSuccess.bind(this)
        this.queueFuncFail = this.queueFuncFail.bind(this)
    }

    componentDidMount() {
        document.title = "OneDataShare - Queue"
        this.queueFunc()
    }

    queueFuncSuccess = (resp) => {
        console.log('resp',JSON.stringify(resp))
        let list=[];
        list.push(resp);
        this.setState({
            dataToDisplay: list,
        });
        console.log(this.state)
    }

    queueFuncFail(resp) {
        //failed
        console.log(resp)
        console.log('Error in queue request to API layer');
    }

    queueFunc() {
        console.log('this.state in queueFunc');
        getJobsForUser(
            this.queueFuncSuccess,
            this.queueFuncFail
        );
    }

    render() {
        return (
            <div className="QueueView">
                <Paper>
                    <Table stickyHeader aria-label="sticky table">
                        <QueueHeader/>
                        <QueueBody
                        rowData={this.state.dataToDisplay}
                        />
                    </Table>
                </Paper>
            </div>
        );
    }

}
export default QueueGridComponent