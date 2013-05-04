var margin = {
    top : 100,
    right : 80,
    bottom : 100,
    left : 100
}, width = 1000 - margin.left - margin.right, height = 1000 - margin.top - margin.bottom;

var parseDate = d3.time.format("%Y%m%d").parse;

var x = d3.scale.linear().range([0, width]);
var y = d3.scale.linear().range([height, 0]);

var color = d3.scale.category10();

var xAxis = d3.svg.axis().scale(x).orient("bottom");

var yAxis = d3.svg.axis().scale(y).orient("left");

var line = d3.svg.line().interpolate("basis").x(function(d) {
    return x(d.date);
}).y(function(d) {
    return y(d.temperature);
});

var svg = d3.select("body").select("#count_graph").append("svg").attr("width", width + margin.left + margin.right).attr("height", height + margin.top + margin.bottom).append("g").attr("transform", "translate(" + margin.left + "," + margin.top + ")");

d3.text("highlight.txt", "text/tsv", f_csv);
function f_csv(tsv) {
    var rows = d3.tsv.parseRows(tsv);

    plotTrend(rows);

}

function plotTrend(data) {

    var wordNames = data.map(function(d) {
        return d[0]
    });

    color.domain(wordNames);

    var cities = color.domain().map(function(name, i) {
        var yearCount = 1509;
        var yearGroupSize = 10;
        // console.log("hard");
        console.log(name + ";" + i);
        return {
            name : name,
            values : _.map(data[i][2].split(","), function(percent) {
                yearCount = yearCount + yearGroupSize;
                var output = {
                    date : yearCount,
                    temperature : parseFloat(percent)
                }
                console.log(output);
                return output;
                // });
                // console.log(output);
                // yearCount = 1510;
            })
            ,
            // values : [{
            // date : 1952,
            // temperature : 0.00003
            // }, {
            // date : 1953,
            // temperature : 0.00005
            // }]
            //
            // values : data.map(function(d) {
            // console.log('d');
            // return {
            // date : d['year'],
            // temperature : +d[name]
            // };
        }

    });
    console.log("cities");
    console.log(cities);
    // x.domain(d3.extent(data, function(d) {
    // console.log(d);
    // return d[0];
    // }));
    x.domain(d3.extent([1510, 2009]));
    y.domain(d3.extent([0.0000, 0.0001]));
    //
    // y.domain([d3.min(cities, function(c) {
    // return d3.min(c.values, function(v) {
    // return v.temperature;
    // });
    // }), d3.max(cities, function(c) {
    // return d3.max(c.values, function(v) {
    // return v.temperature;
    // });
    // })]);

    svg.append("g").attr("class", "x axis").attr("transform", "translate(0," + height + ")").call(xAxis);

    svg.append("g").attr("class", "y axis").call(yAxis).append("text").attr("transform", "rotate(-90)").attr("y", 6).attr("dy", ".71em").style("text-anchor", "end").text("Percentage");

    var city = svg.selectAll(".city").data(cities).enter().append("g").attr("class", "city");

    city.append("path").attr("class", "line").attr("d", function(d) {
        // console.log('d.values');
        // console.log(d.values);
        return line(d.values);
    }).style("stroke", function(d) {
        return color(d.name);
    });

    city.append("text").datum(function(d) {
        return {
            name : d.name,
            value : d.values[d.values.length - 1]
        };
    }).attr("transform", function(d) {
        return "translate(" + x(d.value.date) + "," + y(d.value.temperature) + ")";
    }).attr("x", 3).attr("dy", ".35em").text(function(d) {
        return d.name;
    });
    console.log("done");
}

