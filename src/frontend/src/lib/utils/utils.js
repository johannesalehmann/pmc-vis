import d3 from '../views/imports/import-d3';

function h(tag, attrs, children) {
  const el = document.createElement(tag);

  Object.keys(attrs).forEach(key => {
    var val = attrs[key];

    el.setAttribute(key, val);
  });

  if (children) {
    children.forEach(child => {
      el.appendChild(child);
    });
  }

  return el;
}

function t(text) {
  return document.createTextNode(text);
}

const DECIMAL_PLACES = 3;

function fixed(num) {
  if (num === undefined) return;
  return num % 1 != 0 ? num.toFixed(DECIMAL_PLACES) : num;
}

const colorList = [
  '#888c94',
  '#f51d05',
  '#05f7eb',
  '#0749f0',
  '#734222',
  '#1c005c',
  '#f007dc',
  '#09db00',
  '#F8F32B',
  '#f79205',
];

function highlightPropType(type, fix = false) {
  d3.selectAll(`.dimension.${
    type.replace(/\s/g, '-')
  } > .axis > path.domain, line`).attr('fill', () => '#c7e9eb');
  document.getElementById(`details-${type}`).style.backgroundColor = '#c7e9eb';
}

function resetHighlightPropType(type) {
  d3.selectAll(`.dimension.${
    type.replace(/\s/g, '-')
  } > .axis > path.domain, line`).attr('fill', () => 'none');
  document.getElementById(`details-${type}`).style.backgroundColor = 'white';
}

export {
  h, t, fixed, colorList, highlightPropType, resetHighlightPropType,
};
