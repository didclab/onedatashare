import React, { Component } from 'react';
import Paper from '@material-ui/core/Paper';

export default class OngoingQueuePanel extends Component {

	constructor(props){
		super(props);
	}
	
	render(){
		const {ongoingJobs} = this.props;
		const jobs = ongoingJobs.sort((job1, job2) => job1.job_id-job2.job_id).map(job => {
			//{job.src.type.split(":")[0]} -> {job.dest.type.split(":")[0]}
			let divide = job.bytes.done / job.bytes.total * 100;
			divide = divide ? divide : 0;
			console.log(divide)
			return <div key={job.job_id} style={{position: "relative", float: "left",  height: "20px", width: "50px", borderRadius: "5px" , margin: "5px", borderWidth: "1px", borderColor: "black", borderStyle:"solid"}}>
					<div style={{backgroundColor:"lightgreen", width: divide+"%", height: "100%"}}/>
						<p style={{position: "absolute", top:"0",zIndex:"10", left: "0", color: "black", fontSize: "10px"}}>Job Id: {job.job_id}</p>
					</div>
		})
		return(
			<div style={{textAlign: "left", display: "inline-block", overflow: "hidden", padding: "5px", borderRadius: "10px"
			}}>
				{jobs}
			</div>
			);
		;
	}
}
