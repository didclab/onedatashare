import React, { Component } from 'react';

import { Carousel } from 'react-bootstrap'

import background from "../assets/background-texture.png";
import gs1 from "../assets/gs1.png";
import gs2 from "../assets/gs2.png";
import gs3 from "../assets/gs3.png";
import gs4 from "../assets/gs4.png";
import gs5 from "../assets/gs5.png";
import gs6 from "../assets/gs6.png";
import gs7 from "../assets/gs7.png";

import './GetStartedComponent.css';

export default class GetStartedComponent extends Component{

    constructor(){
        super();

        this.data = [
            {
                title : "Start by creating your OneDataShare account",
                imgPath: gs1
            },
            {
                title : "Sign in using your account credentials",
                imgPath: gs2
            },
            {
                title : "Browse through the list of supported endpoints",
                imgPath: gs3
            },
            {
                title : "Log in to the required endpoints",
                imgPath: gs4
            },
            {
                title : "Intiate a transfer by dragging a file/folder from one endpoint to another",
                imgPath: gs5
            },
            {
                title : "Monitor your transfer on the queue page",
                imgPath: gs6
            },
            {
                title : "Review details related to your file/folder transfer",
                imgPath: gs7
            }
        ];
    }

    render(){

        let carouselItemStyle = { width : '100%', height : '90%', textAlign : 'center'}
        let imgStyle = {width : '70%', border: '1px black solid'}

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
            <div style={{height: '800px', paddingTop : '5%', paddingBottom : '5%', backgroundImage: 'url(' + background + ')'}}>
                <Carousel >
                    {carouselItems}
                </Carousel>
            </div>
        )
    }
}