import React, { Component } from 'react';

import { Carousel } from 'react-bootstrap'

import background from "../assets/background-texture.png";
import gs1 from "../assets/gs1.png";
import gs2 from "../assets/gs2.png";
import gs3 from "../assets/gs3.png";
import gs4 from "../assets/gs4.png";
import gs5 from "../assets/gs5.gif";
import gs6 from "../assets/gs6.gif";
import gs7 from "../assets/gs7.png";
import Button from '@material-ui/core/Button';
import { Link } from 'react-router-dom';

import './GetStartedComponent.css';

export default class GetStartedComponent extends Component{

    constructor(){
        super();

        let imgStyle = { maxWidth : '90%', maxHeight: '70vh',  border: '1px black solid', marginTop: '2vw' }

        this.data = [
            {
                title : "Start by creating your OneDataShare account",
                imgPath: gs1,
                imgStyle : { ...imgStyle}
            },
            {
                title : "Sign in using your account credentials",
                imgPath: gs2,
                imgStyle : { ...imgStyle }
            },
            {
                title : "Browse through the list of supported endpoints",
                imgPath: gs3,
                imgStyle : { ...imgStyle, width : '80%'}
            },
            {
                title : "Log in to the required endpoints",
                imgPath: gs4,
                imgStyle : { ...imgStyle, width : '80%'}
            },
            {
                title : "Intiate a transfer by dragging a file/folder from one endpoint to another",
                imgPath: gs5,
                imgStyle : { ...imgStyle}
            },
            {
                title : "Monitor your transfer on the queue page",
                imgPath: gs6,
                imgStyle : { ...imgStyle, marginTop: '8vw'}
            },
            {
                title : "Review details related to your file/folder transfer",
                imgPath: gs7,
                imgStyle : { ...imgStyle, width : '80%'}
            }
        ];
    }

    render(){

        let carouselItemStyle = { width : '100%', textAlign : 'center'}

        var carouselItems = [];
        this.data.map(item =>{
            carouselItems.push(
                <Carousel.Item style={ carouselItemStyle }>
                    <div style={{ fontSize: '2em', color: 'white' }}>
                        {item.title}
                    </div>
                    <div>
                        <img style={ imgStyle } src={ item.imgPath } alt = "Get Started" />
                    </div>
                </Carousel.Item>
            );
        })
        return(
            <div className="adjustTop adjustContent content" 
                style={{ backgroundImage: 'url(' + background + ')'}}>
                <Carousel className="carouselStyle">
                    {carouselItems}

                    <Carousel.Item style={ carouselItemStyle }>
                        <div style={{ fontSize: '2em', color: 'white', margin: '10%' }}>
                            Like it already?
                            <br/>
                            Let's start by creating your free OneDataShare account now!
                            <br/> <br/>
                            <Button variant="contained" color="primary" style={{ backgroundColor : 'rgb(45, 46, 48)', fontSize: '1.5rem'}}>
                                <Link to="/account/register" style={{ color:'white' }}>
                                    Register
                                </Link>
                            </Button>
                        </div>
                    </Carousel.Item>
                </Carousel>
            </div>
        )
    }
}