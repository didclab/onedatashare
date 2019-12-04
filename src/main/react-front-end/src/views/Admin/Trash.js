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
        const { mails } = this.state;
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
