// adapted from https://observablehq.com/@ssiegmund/violin-plot-playground

function getArea(orient, scale, max, apperture) {
  const s = d3
    .scaleLinear()
    .domain([-max, max])
    .range([-apperture, apperture]);

  const area = d3.area();

  if (orient) {
    area.y(d => scale(d[0]))
      .x0(d => s(-d[1]))
      .x1(d => s(d[1]));
  } else {
    area.x(d => scale(d[0]))
      .y0(d => s(-d[1]))
      .y1(d => s(d[1]));
  }

  return area;
}

function violin(svg, {
  orient, resp, name, data,
} = {}) {
  const scale = resp.axes[name];
  const apperture = 10;
  const bandwidth = 0.3;
  const thds = scale.ticks(40);

  function kde(kernel, thds) {
    return (V) => thds.map((t) => [t, d3.mean(V, (d) => kernel(t - d))]);
  }

  function epanechnikov(bandwidth) {
    return (x) => Math.abs((x /= bandwidth)) <= 1 ? (0.75 * (1 - x * x)) / bandwidth : 0;
  }

  const density = kde(epanechnikov(bandwidth), thds);
  const values = density(data);
  const max = d3.max(values.map(d => d[1]));
  const area = getArea(orient, scale, max, apperture);

  area.curve(d3.curveCatmullRom);

  svg.append('path')
    .datum(values)
    .attr('d', area)
    .style('stroke', 'none')
    .style('fill', '#000')
    .style('opacity', 0.1);
}

function histogram(svg, {
  orient, resp, name, data,
} = {}) {
  const scale = resp.axes[name];
  const apperture = 25;
  const ticks = scale.ticks(scale.range()[1]);

  function frequencies(ts) {
    return (V) => ts.map(
      (t, i) => [t, V.filter(d => i > 0 ? ts[i - 1] > d && d >= t : d >= t).length],
    );
  }

  const values = frequencies(ticks)(data);
  const max = d3.max(values.map(d => d[1]));
  const area = getArea(orient, scale, max, apperture);

  area.curve(d3.curveStep);

  svg.append('path')
    .datum(values)
    .attr('d', area)
    .style('stroke', 'none')
    .style('fill', 'red')
    .style('opacity', 0.5);
}

export { violin, histogram };
