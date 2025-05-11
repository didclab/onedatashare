import React from 'react';

const Platforms = () => {
    return (
        <div className='platform_section'>
            <h1>Platforms We Support</h1>
            <div className='platforms'>
                <div className='platforms_card'>
                    <img src="https://img.icons8.com/?size=100&id=ya4CrqO7PgnY&format=png&color=000000" alt="Google Drive Icon"/>
                    <p>Google Drive</p>
                </div>
                <div className='platforms_card'>
                    <img src="https://img.icons8.com/?size=100&id=13657&format=png&color=000000" alt="Dropbox Icon"/>
                    <p>Dropbox</p>
                </div>
                <div className='platforms_card'>
                    <img src="https://img.icons8.com/?size=100&id=13648&format=png&color=000000" alt="Box Icon"/>
                    <p>Box</p>
                </div>
                <div className='platforms_card'>
                    <img src="https://img.icons8.com/?size=100&id=17989&format=png&color=000000" alt="S3 Icon"/>
                    <p>FTP</p>
                </div>
                <div className='platforms_card'>
                    <img src="https://img.icons8.com/?size=100&id=fBhhRXV4c8Xm&format=png&color=000000" alt="SFTP Icon"/>
                    <p>SFTP</p>
                </div>
                <div className='platforms_card'>
                    <img src="https://img.icons8.com/?size=100&id=bWzzNy3uEGDP&format=png&color=000000" alt="HTTP/HTTPS Icon"/>
                    <p>HTTP/HTTPS</p>
                </div>
                <div className='platforms_card'>
                    <img src="https://img.icons8.com/?size=100&id=69427&format=png&color=000000" alt="S3 Icon"/>
                    <p>Amazon S3</p>
                </div>
            </div>
        </div>
    );
}

export default Platforms;