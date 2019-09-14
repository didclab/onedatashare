import ReactGA from "react-ga";

/** 
 * Initializes the google analytics library 
 */ 
export function initializeReactGA() {
  ReactGA.initialize("UA-147970452-1");
}

/** 
 * Updates the user's current page view in Google analytics
 */ 
export function updateGAPageView() {
  ReactGA.pageview(window.location.pathname);
}