'use strict'

function toApiCall($url) {
	return $url
		.map(path => {
			return {type: 'GET', url: 'http://' + window.location.hostname + ':8080' + path}
		})
		.ajax()
}
function toApiPost(url$) {
	//return url$
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
			.map(portfolio => {
				const marketValue = portfolio.totalValue - portfolio.cash
				portfolio.assets = portfolio.assets.map(({asset, symbolValue}) => {
					const totalValue = symbolValue * asset.count
					const symbolProfit = (symbolValue - asset.buyPrice) * asset.count
					return {asset, symbolValue, totalValue, symbolProfit}
				})
				return {marketValue, ...portfolio}
			}).log('portfolio')
			.merge(
				portfolioSearch$.map(() => ({loading: true}))
			)

	},
	doSellAll: function(sellAllMsg$) {
		toApiPost(
			sellAllMsg$.map(({portfolioName, symbol}) => `/portfolio/${portfolioName}/sell`)
		)
		return Bacon.never()
	}
}