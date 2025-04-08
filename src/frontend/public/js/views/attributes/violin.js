// adapted from https://observablehq.com/@ssiegmund/violin-plot-playground

function violin(svg, {
  orient, resp, name, data,
} = {}) {
  const scale = resp.axes[name];

  function kde(kernel, thds) {
    return (V) => thds.map((t) => [t, d3.mean(V, (d) => kernel(t - d))]);
  }

  function epanechnikov(bandwidth) {
    return (x) => Math.abs((x /= bandwidth)) <= 1 ? (0.75 * (1 - x * x)) / bandwidth : 0;
  }

  const bandwidth = 0.3;
  const thds = scale.ticks(40);
  const density = kde(epanechnikov(bandwidth), thds);

  const max = d3.max(data);
  const xNum = d3
    .scaleLinear()
    .domain([-max, max])
    .range([-5, 5]);

  const area = orient
    ? d3
      .area()
      .x0((d) => xNum(-d[1]))
      .x1((d) => xNum(d[1]))
      .y((d) => scale(d[0]))
      .curve(d3.curveNatural)
    : d3
      .area()
      .y0((d) => xNum(-d[1]))
      .y1((d) => xNum(d[1]))
      .x((d) => scale(d[0]))
      .curve(d3.curveNatural);

  svg.append('path')
    .datum(density(data))
    .style('stroke', 'none')
    .style('fill', '#000')
    .style('opacity', 0.1)
    .attr('d', area);
}

export { violin };
