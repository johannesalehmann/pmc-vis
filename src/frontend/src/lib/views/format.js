import { info } from '../main/main.js';

function pid(propType, propName) {
  // creates safe Polyline ID
  return `${propType.replace(/\s/g, '')}_____${propName}`;
}
function ndl_to_pcp(data, prop) {
  const returnable = { pl: [], pld: {} }; // polylines, polyline data

  returnable.pl = data.nodes.map((d) => {
    const polyline = {
      id: d.id,
      _color: d.type === 's' ? '--pcp-primary' : '--pcp-secondary',
      _selected: d._selected,
    };

    Object.keys(prop).forEach((p) => {
      if (!info.types[p].includes(d.type)) return;

      Object.keys(prop[p].props).forEach((e) => {
        if (prop[p].props[e] && d.details[p]) {
          polyline[pid(p, e)] = d.details[p][e];
          returnable.pld[pid(p, e)] ||= {
            type: prop[p].metadata[e].type,
            min: prop[p].metadata[e].min,
            max: prop[p].metadata[e].max,
            prop: p,
            name: e,
          };
        }
      });
    });

    return polyline;
  });

  return returnable;
}

export { ndl_to_pcp, pid };
