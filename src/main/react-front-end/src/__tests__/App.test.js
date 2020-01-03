import React from 'react';
import { shallow } from 'enzyme';
import App from '../App';


describe('App component', () => {
  const component = shallow(<App />);
  it('renders componentDidMount', () => {
    expect(component.state().loaded).toBe(true);
  });

  it('renders Snackbar with its props', () => {
    expect(component.find('Snackbar')).toBeDefined();
    let snackbar = component.find('div').props().children[0];
    expect(snackbar.props.anchorOrigin).toEqual({ vertical: component.state().vertical,
       horizontal: component.state().horizontal });
    expect(snackbar.props.open).toEqual(component.state().open);
    expect(snackbar.props.onClose).toEqual(component.instance().handleClose);
    expect(snackbar.props.action.props.color).toEqual('secondary');
    expect(snackbar.props.action.props.size).toEqual('small');
    expect(snackbar.props.action.props.children).toEqual('Close');
    expect(snackbar.props.action.props.onClick).toEqual(component.instance().handleClose);
    expect(snackbar.props.message.props.id).toEqual('message-id');
    expect(snackbar.props.message.props.children).toEqual(component.state().error);

    component.instance().setState({'error': 'New Error occured'});

    snackbar = component.find('div').props().children[0];
    expect(snackbar.props.message.props.children).toEqual('New Error occured');
  });

  describe('Should test the component function ', () => {
    it('handleOpen', () => {
      expect(component.state().open).toBe(false);
      component.instance().handleOpen('New Error');
      expect(component.state().open).toBe(true);
      expect(component.state().error).toBe("\"New Error\"");
    });

    it('handleClose', () => {
      component.setState({open: true});
      expect(component.state().open).toBe(true);
      component.instance().handleClose();
      expect(component.state().open).toBe(false);
    });
  });
});
