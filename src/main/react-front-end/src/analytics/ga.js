import ReactGA from "react-ga";

/** 
 * Initializes the google analytics library 
 */ 
export function initializeReactGA() {
  if(process.env.REACT_APP_GA_KEY !== undefined){
    ReactGA.initialize(process.env.REACT_APP_GA_KEY);
  }
}

/** 
 * Updates the user's current page view in Google analytics
 */ 
export function updateGAPageView() {
  if(process.env.REACT_APP_GA_KEY !== undefined){
    ReactGA.pageview(window.location.pathname);
  }
}
