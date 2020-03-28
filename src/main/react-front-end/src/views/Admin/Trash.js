/**
 ##**************************************************************
 ##
 ## Copyright (C) 2018-2020, OneDataShare Team, 
 ## Department of Computer Science and Engineering,
 ## University at Buffalo, Buffalo, NY, 14260.
 ## 
 ## Licensed under the Apache License, Version 2.0 (the "License"); you
 ## may not use this file except in compliance with the License.  You may
 ## obtain a copy of the License at
 ## 
 ##    http://www.apache.org/licenses/LICENSE-2.0
 ## 
 ## Unless required by applicable law or agreed to in writing, software
 ## distributed under the License is distributed on an "AS IS" BASIS,
 ## WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ## See the License for the specific language governing permissions and
 ## limitations under the License.
 ##
 ##**************************************************************
 */


import React, { Component } from 'react';
import { Label, Glyphicon, Table } from 'react-bootstrap';
import './Trash.css';
import { getAllTrashMails } from '../../APICalls/APICalls';
import { cookies } from "../../model/reducers";

class Trash extends Component {
    constructor(props) {
        super(props);
        this.state = {
            mails: []
        }
    }

    getSentMails = async () => {
        const mails = await getAllTrashMails(cookies.get('email'));
        this.setState({ mails });
    }

    async componentDidMount() {
        await this.getSentMails();
    }

    render() {
        const mails = this.state.mails.sort((a, b) => {
            return new Date(a.sentDateTime).getTime() -
                new Date(b.sentDateTime).getTime()
        }).reverse();
        return (
            <div>
                <h2 style={{ marginTop: 20, flex: 4 }}>
                    <Label style={styles.pageHeading}>
                        <Glyphicon glyph="trash" /> Trash
                        </Label>
                </h2>
                {mails && mails.length > 0 ?
                    <div style={{ flex: 3, overflowY: 'scroll' }}>
                        <Table responsive hover>
                            <thead>
                                <tr>
                                    <th>#</th>
                                    <th>Date</th>
                                    <th>Recipients</th>
                                    <th>Subject</th>
                                    <th>Message</th>
                                    <th>isHtml</th>
                                </tr>
                            </thead>
                            <tbody>
                                {
                                    mails.map((mail, index) => {
                                        return (
                                            <tr>
                                                <td>{index + 1}</td>
                                                <td>{new Date(mail.sentDateTime).toLocaleDateString()}</td>
                                                <td>{mail.recipients.length === 1 ? '1 user' : `${mail.recipients.length} users`}</td>
                                                <td>{mail.subject}</td>
                                                <td>{mail.message.substring(0, 100)}</td>
                                                <td>{mail.html === true ? <Glyphicon glyph='ok' style={{ color: 'green' }} /> : <Glyphicon glyph='remove' style={{ color: 'red' }} />}</td>
                                            </tr>
                                        )
                                    })
                                }
                            </tbody>
                        </Table>
                    </div> : <div></div>
                }
            </div>

        );
    }
}

const styles = {
    pageHeading: { backgroundColor: 'transparent', color: '#073642', padding: 7 }
}
export default Trash;
