import React from 'react';
import Modal from 'react-modal';
// import Modal from 'react-bootstrap/Modal';
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import TableCell from "@material-ui/core/TableCell";
import { TableBody, TableContainer } from '@material-ui/core';
import { Table } from 'react-bootstrap';
import Paper from "@material-ui/core/Paper";
import TablePagination from '@material-ui/core/TablePagination';
import { useEffect, useState } from "react";

const customStyles = {
  content: {
    top: '50%',
    left: '50%',
    right: 'auto',
    width: '20%',
    bottom: 'auto',
    marginRight: '-50%',
    transform: 'translate(-50%, -50%)'
  }
};

function MyModalComponent(props) {
  console.log("props.IsModalOpened",props.IsModalOpened);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(5);
  const handleChangePage = (event, newPage) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (event) => {
    setRowsPerPage(+event.target.value);
    setPage(0);
  };
  if (props.IsModalOpened){


  var data = props.dynData;
  var batchSteps = data.batchSteps;
  var difference;
  console.log("batch steps",batchSteps);
  for(let i=0;i<batchSteps.length;i++)
  {
      difference = Date.parse(batchSteps[i].endTime)/1000 - Date.parse(batchSteps[i].startTime)/1000;
      batchSteps[i]["total_time"] = difference;
  }
  if (data !== 'undefined'){
  // console.log("data batchsteps",batchSteps[0]);
  function afterOpenModal(e) {
    props.onAfterOpen(e);
  }

  function onModalClose(event) {
    // let data = { name: 'example', type: 'closed from child' };
    props.onCloseModal(event);
  }



  return (
    // <div>
      <Modal
        isOpen={props.IsModalOpened}
        onAfterOpen={e => afterOpenModal(e)}
        style={customStyles}
        ariaHideApp={false}
      >
      {/* <TableContainer component={Paper}> */}
      <Paper sx={{ width: '100%' }}>
      <TableContainer>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell className='queueHeaderCell'><h5>Total Time</h5></TableCell>
              {/* <TableCell className='queueHeaderCell' colSpan={3}>Throughput</TableCell> */}
              {/* <TableCell className='queueHeaderCell'><h4>End time</h4></TableCell> */}
              <TableCell className='queueHeaderCell'><h5>File name</h5></TableCell>
            </TableRow>
          </TableHead>
        <TableBody>
        {(rowsPerPage > 0
            ? batchSteps.slice(page * rowsPerPage, page * rowsPerPage + rowsPerPage)
            : batchSteps
          ).map((row) => (
            <TableRow key = {row.id} >
             {/* <TableCell component="th" scope="row" align="center" className='queueBodyCell'>{row.id}</TableCell> */}
             <TableCell component="th" scope="row" align="center" className='queueBodyCell'><h5>{row.total_time}</h5></TableCell>
             {/* <TableCell component="th" scope="row" align="center" className='queueBodyCell'>{row.endTime}</TableCell> */}
             <TableCell component="th" scope="row" align="center" className='queueBodyCell'><h5>{row.step_name}</h5></TableCell>
            </TableRow>
         ))}
        </TableBody>
        </Table>
      </TableContainer>
      {/* <TableRow> */}
      <TablePagination
        rowsPerPageOptions={[5,7]}
        component="div"
        count={batchSteps.length}
        rowsPerPage={rowsPerPage}
        page={page}
        onPageChange={handleChangePage}
        onRowsPerPageChange={handleChangeRowsPerPage}
      />
      {/* </TableRow> */}
        </Paper>
        {/* </TableContainer> */}
        <button onClick={e => onModalClose(e)} >close</button>
      </Modal>
    // </div>
  );
}
}
else{
  return null;
}
}

export default MyModalComponent;