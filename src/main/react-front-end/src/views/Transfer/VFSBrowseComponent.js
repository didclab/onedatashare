import TextField from '@material-ui/core/TextField';
import React, { useState } from 'react';
import IconButton from '@material-ui/core/IconButton';
import Add from '@material-ui/icons/Add';
import Remove from '@material-ui/icons/Remove';
import { setFilesWithPathListAndTasks } from './initialize_dnd';

function VFSBrowseComponent(props) {
    const [parentPath, setParentPath] = useState('');
    const [filesList, setFilesList] = useState([{idx: 0, value: '', size: null}]);

    const handleMultipleChanges = (event, index) => {
        let newArray = [...filesList].map(val => val.idx === index ? {idx: index, value: event.target.value, size: val.size} : val)
        setFilesList(newArray)
        setFilesWithPathListAndTasks(newArray, parentPath, props.endpoint)
    }
    const handleMultipleChangesSize = (event, index) => {
        let newArray = [...filesList].map(val => val.idx === index ? {idx: index, size: event.target.value, value: val.value} : val)
        setFilesList(newArray)
        setFilesWithPathListAndTasks(newArray, parentPath, props.endpoint)
    }
    const addFileHolder = () => {
        setFilesList([...filesList, {idx: filesList.length > 0 ? filesList[filesList.length - 1].idx + 1 : 0, value: ''}])
    }
    const removeFileHolder = (index) => {
        let newArray = filesList.filter(val => val.idx !== index)
        setFilesList(newArray)
    }

    const parentPathHandler = (event) => {
        setParentPath(event.target.value)
        setFilesWithPathListAndTasks(filesList, event.target.value, props.endpoint)
    }

    return (
    <div style={{  overflowY: 'scroll', width: "100%", marginTop: "0px", height: "440px",paddingLeft:"10px",paddingRight:"10px"}}>
					<div style={{paddingTop: '15px'}}>
						Parent path
						<TextField
								fullWidth
								variant={"outlined"}
								margin={"dense"}
								placeholder={"Parent path"}
                                value={parentPath}
								onChange={(event) => {
									parentPathHandler(event)
								}}
								InputProps={{
									className: "searchTextfield"
								}}
                                key="parent-path"
							/>
						</div>
					<div style={{paddingTop: '15px'}}>
						Files list
						{filesList.map((file) => {
                            return <div style={{display: 'flex', flexDirection: 'row'}}>
                                <TextField
								fullWidth
								variant={"outlined"}
								margin={"dense"}
								placeholder={"File path"}
                                name={file.idx}
                                value={file.value}
								onChange={event => {handleMultipleChanges(event, file.idx)}}
								InputProps={{
									className: "searchTextfield"
								}}
                                key={`child-${file.idx}`}
                                style={{marginRight: '10px'}}
							/>
                                <TextField
								fullWidth
								variant={"outlined"}
								margin={"dense"}
								placeholder={"Size"}
                                name={file.idx}
                                value={file.size}
                                type="number"
								onChange={event => {handleMultipleChangesSize(event, file.idx)}}
								InputProps={{
									className: "searchTextfield"
								}}
                                style={{width: '150px'}}
                                key={`child-size-${file.idx}`}
							/>
                            <IconButton onClick={() => addFileHolder()} aria-label="add">
                                <Add />
                            </IconButton>
                            {filesList.length > 1 && <IconButton onClick={() => removeFileHolder(file.idx)} aria-label="delete">
                                <Remove />
                            </IconButton>}
                            </div>
                        })}
						</div>

				</div>
    )
}

export default VFSBrowseComponent