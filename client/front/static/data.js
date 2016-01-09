'use strict'

function toApiCall($url) {
	return $url
		.map(path => {
			return {type: 'GET', url: 'http://' + window.location.hostname + ':8080' + path}
		})
		.ajax()
}

module.exports = {
	createAction: function(initial) {
		const bus = new Bacon.Bus()

		if(typeof initial !== 'undefined') {
			console.log('initial', initial)
			bus.push(initial)
		}

		return {
			$: bus,
			action: function(arg1) {
				bus.push(arg1)
			}
		}
	},
	getPortfolio: function(portfolioName$) {
		const portfolioSearch$ = portfolioName$.filter(name => name != null)

		return toApiCall(
				portfolioSearch$.map(name => `/portfolio/${name}`)
			)
			.merge(
				portfolioSearch$.map(() => ({loading: true}))
			)
	}
}