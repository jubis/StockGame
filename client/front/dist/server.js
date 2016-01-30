'use strict';

var express = require('express');
var browserify = require('browserify');

var app = express();
app.use(express.static('static'));

app.get('/scripts.js', function (req, res) {
	browserify('static/app.js', { debug: true }).transform("babelify", { presets: ["es2015", "react"], plugins: ["transform-object-rest-spread"] }).bundle().pipe(res);
});

app.listen(8081, function () {
	console.log('Server listening on port ' + 8081);
});