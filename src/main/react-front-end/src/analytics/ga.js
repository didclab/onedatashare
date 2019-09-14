import ReactGA from "react-ga";

/** 
 * Initializes the google analytics library 
 */ 
export function initializeReactGA() {
  if(process.env.REACT_APP_ENV === 'production'){
    ReactGA.initialize('UA-147970452-1');
  }
  else if(process.env.REACT_APP_ENV === 'development'){
    ReactGA.initialize('UA-147976263-1');    
  }
}

/** 
 * Updates the user's current page view in Google analytics
 */ 
export function updateGAPageView() {
  if(process.env.REACT_APP_ENV === 'production' || process.env.REACT_APP_ENV === 'development'){
    ReactGA.pageview(window.location.pathname);
  }
}