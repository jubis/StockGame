'use strict';

let express = require('express')
let browserify = require('browserify')

let app = express()
app.use(express.static('static'))

app.get('/scripts.js', (req, res) => {
	browserify('static/app.js', {debug: true})
		.transform("babelify", {presets: ["es2015", "react"], plugins: ["transform-object-rest-spread"]})
		.bundle()
		.pipe(res);
})

app.listen(8081, () => {
	console.log('Server listening on port ' + 8081)
})
