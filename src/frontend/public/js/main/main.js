import { spawnPane, info, getPanes } from "../views/panes/panes.js";
import { params } from "../views/node-link/layout-options/elk.js";
import { spawnGraph } from "../views/node-link/node-link.js";
import { BACKEND, PROJECT } from "../utils/controls.js";
import events from "../utils/events.js";

window.onresize = () => {
  dispatchEvent(events.RESIZE_ALL);
};

const ww = window.innerWidth;
if (ww) {
  document.getElementById('numberOfPanes').value = Math.floor(ww / 200); 
}

Promise.all([
  fetch(BACKEND + PROJECT + "/status").then(r => r.json()),
  fetch(BACKEND + PROJECT + "/initial").then(r => r.json()),
  //fetch(BACKEND + PROJECT).then((res) => res.json()) // requests entire dataset
]).then((promises) => {
  Object.keys(promises[0].info).forEach(k => {
    info[k] = promises[0].info[k];
  });
  
  const data = promises[1];
  const nodesIds = data.nodes
    .map((node) => node.id)
    .filter((id) => !id.includes("t_"));

  info.metadata = {
    ID: info.ID,
    Scheduler: info.Scheduler,
    initial: `#${nodesIds.join(', #')}`,
  };
  delete info.ID;
  delete info.Scheduler;

  if (document.getElementById("project-id")) {
    document.getElementById("project-id").innerHTML = info.metadata.ID;
  }

  const firstPaneId = "pane-0";
  const pane = spawnPane(
    { id: firstPaneId },
    nodesIds
  );

  spawnGraph(pane, data, params);
});

addEventListener('linked-selection', function (e) {
  const selection = e.detail.selection;
  const panes = getPanes();
  panes[e.detail.pane].cy.nodes().unselect();
  const strSelection = '#' + selection.map(n => n.id).join(', #');
  
  if (strSelection !== '#') {
    panes[e.detail.pane].cy.$(strSelection).select();
  }
}, true);
