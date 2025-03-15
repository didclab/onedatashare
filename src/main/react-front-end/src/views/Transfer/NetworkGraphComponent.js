// NetworkGraphComponent.jsx
import React, { useEffect, useState } from 'react';
import cytoscape from 'cytoscape';

const NetworkGraphComponent = (props) => {
  const [elements, setElements] = useState([        
    { data: { id: 'Source', label: props.sourceNodeName["uri"], type: 'source' }, position: {x: 0, y: 0}, locked: true },
    { data: {id: 'Transfer Node', label: "ODS Transfer Service", type: 'source'}, position: {x: 175, y: 0}, locked: true},
    { data: { id: 'Destination', label: props.destinationNodeName["uri"], type: 'destination' }, position: {x: 350, y: 0}, locked: true },
  ])

  // This function keeps track of the source and destination node names
  useEffect(() => {
    setElements([
      { data: { id: 'Source', label: props.sourceNodeName["uri"], type: 'source' }, position: {x: 0, y: 0}, locked: true },
      { data: {id: 'Transfer Node', label: "ODS Transfer Service", type: 'source'}, position: {x: 175, y: 0}, locked: true},
      { data: { id: 'Destination', label: props.destinationNodeName["uri"], type: 'destination' }, position: {x: 350, y: 0}, locked: true },
    ]);
  }, [props.sourceNodeName, props.destinationNodeName]); 


  // This useEffect function creates the Network graph in Transfer settings
  useEffect(() => {
    const cy = cytoscape({
      container: document.getElementById('cy'), 
      elements: elements,
      style: [
        {
          selector: 'node[type="source"]',
          style: {
            'background-color': 'black',
            'label': 'data(label)',
            'color': 'black',
            'text-outline-color': 'black',
            'font-size': 5,
            'width': 5,
            'height': 5, 
            'shape': 'square'
          }
        },
        {
          selector: 'node[type="destination"]',
          style: {
            'background-color': 'black',
            'label': 'data(label)',
            'color': 'black',
            'text-outline-color': 'black',
            'font-size': 5,
            'width': 5,
            'height': 5, 
            'shape': 'square'
          }
        },
        {
          selector: 'node[type="TCP"]',
          style: {
            'background-color': 'black',
            'label': 'data(id)',
            'color': 'black',
            'text-outline-color': 'black',
            'font-size': 5,
            'width': 5,
            'height': 5, 
          }
        },
        {
          selector: 'node[type="parallel"]',
          style: {
            'background-color': '#FF4136',
            'label': '',
            'width': 3,
            'height': 3,
          }
        },
        {
          selector: 'edge',
          style: {
            'width': 1,
            'line-color': '#ccc',
            'target-arrow-color': '#ccc',
            'curve-style': 'round-taxi'
          }
        },
        {
          selector: 'edge[type="concurrent"]',
          style: {
            'width': 0.1,
            'line-color': 'red',
            'target-arrow-color': '#ccc',
            'curve-style': 'bezier'
          }
        },
        
      ],

      layout: {
        name: 'grid',
        rows: 1
      },

      
      userZoomingEnabled: false,
      userPanningEnabled: false,
      boxSelectionEnabled: false,
      autounselectify: true
    });
    let parallelThreadCount = props.parallelThreadCount;
    let concurrencyThreadCount = props.concurrencyThreadCount;

    if (parallelThreadCount > 10) {
      parallelThreadCount = 10
    }

    if (props.concurrencyThreadCount > 10) {
      concurrencyThreadCount = 10
    }
    const maxThreadDistance = parallelThreadCount * 5;
    const spacing = maxThreadDistance / (parallelThreadCount - 1);
    let counter = 0;
  
    const newNodes = [];
    if (props.parallelThreadCount == 1) {
      newNodes.push({
        group: 'nodes',
        position: { x: 87.5, y: 0 },
        data: { id: 'n' + counter, type: 'parallel' },
      });
      newNodes.push({
        group: 'nodes',
        position: { x: 262.5, y: 0 },
        data: { id: 'd' + counter, type: 'parallel' },
      });
      newNodes.push({
        group: 'edges',
        data: {source: 'Source', target: 'n' + counter }
      });
      newNodes.push({
        group: 'edges',
        data: {source: 'd' + counter, target: 'Destination' }
      });
    }
    for (let i = 0; i < parallelThreadCount; i++) {
      const yPosition = -(maxThreadDistance / 2) + i * spacing;
      newNodes.push({
        group: 'nodes',
        position: { x: 87.5, y: yPosition },
        data: { id: 'n' + counter, type: 'parallel' },
      });
      newNodes.push({
        group: 'nodes',
        position: { x: 262.5, y: yPosition },
        data: { id: 'd' + counter, type: 'parallel' },
      });
      newNodes.push({
        group: 'edges',
        data: {source: 'Source', target: 'n' + counter }
      });
      newNodes.push({
        group: 'edges',
        data: {source: 'd' + counter, target: 'Destination'}
      });

      for (let j = 0; j < concurrencyThreadCount; j++) {
        newNodes.push({
          group: 'edges',
          data: { source: 'n' + counter, target: 'Transfer Node', type:"concurrent"},
        });
        newNodes.push({
          group: 'edges',
          data: { source: 'd' + counter, target: 'Transfer Node', type:"concurrent"},
        });
      }
      counter += 1;
    }
  
    cy.add(newNodes);

    return () => {
      cy.destroy();
    };
  }, [props, elements]);

  return (
    <div
      id="cy"
      style={{ width: '100%', height: "640px", border: '1px solid #ddd', pointerEvents:'none'}}
    ></div>
  );
};

export default NetworkGraphComponent;
