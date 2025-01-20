// NetworkGraphComponent.jsx
import React, { useEffect, useState } from 'react';
import cytoscape from 'cytoscape';

const NetworkGraphComponent = (props) => {
  const [elements, setElements] = useState([        
    { data: { id: 'Node A' }, position: {x: 0, y: 0}, locked: true },
    { data: { id: 'Node B' }, position: {x: 350, y: 0}, locked: true },
  ])

  useEffect(() => {
    // Initialize Cytoscape on component mount
    const cy = cytoscape({
      container: document.getElementById('cy'), 
      elements: elements,
      style: [
        {
          selector: 'node',
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
            'width': 1,
            'line-color': '#ccc',
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

    const maxThreadDistance = props.parallelThreadCount * 5; // Total range for threads
    const spacing = maxThreadDistance / (props.parallelThreadCount - 1); // Calculate spacing dynamically
    let counter = 0;
  
    const newNodes = [];
    if (props.parallelThreadCount == 1) {
      newNodes.push({
        group: 'nodes',
        position: { x: 175, y: 0 },
        data: { id: 'n' + counter, type: 'parallel' },
      });
      newNodes.push({
        group: 'edges',
        data: {source: 'Node A', target: 'n' + counter }
      });
    }
    for (let i = 0; i < props.parallelThreadCount; i++) {
      const yPosition = -(maxThreadDistance / 2) + i * spacing; // Calculate y position for each node
      newNodes.push({
        group: 'nodes',
        position: { x: 175, y: yPosition },
        data: { id: 'n' + counter, type: 'parallel' },
      });
      newNodes.push({
        group: 'edges',
        data: {source: 'Node A', target: 'n' + counter }
      });

      for (let j = 0; j < props.concurrencyThreadCount; j++) {
        newNodes.push({
          group: 'edges',
          data: { source: 'n' + counter, target: 'Node B', type:"concurrent"},
        });
      }
      counter += 1;
    }
  
    // Add new elements to Cytoscape
    cy.add(newNodes);

    // Cleanup Cytoscape instance when component unmounts
    return () => {
      cy.destroy();
    };
  }, [props]);

  return (
    <div
      id="cy"
      style={{ width: '100%', height: "640px", border: '1px solid #ddd', pointerEvents:'none'}}
    ></div>
  );
};

export default NetworkGraphComponent;
