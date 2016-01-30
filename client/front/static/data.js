'use strict'

function fullUrl(path) {
	return 'http://' + window.location.hostname + ':8080' + path
}

function toApiCall($url) {
	return $url
		.map(path => {
			return {type: 'GET', url: fullUrl(path)}
		})
		.ajax()
}
function toApiPost(post$) {
	return post$
		.flatMap(({url, data}) => Bacon.$.ajaxPost(fullUrl(url), data))
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
			.map(({name, cash, marketValue, totalValue, profit, profitPercentage, assets}) => {
				const stocks = assets.map(({asset, marketValue, profit, profitPercentage}) => {
					return {...asset, marketValue, profit, profitPercentage}
				})
				return {name, cash, marketValue, totalValue, profit, profitPercentage, stocks}
			}).log('portfolio')
			.merge(
				portfolioSearch$.map(() => ({loading: true}))
			)

	},
	doSellAll: function(sellAllMsg$) {
		return toApiPost(
				sellAllMsg$.map(({portfolioName, symbol}) => ({
						url: `/portfolio/${portfolioName}/sell`,
						data: {symbol}
				}))
			)
			.flatMap(result => (result == "successful") ? Bacon.once(true) : new Bacon.Error("Sell failed"))
	},
	doBuy: function(buyMsg$) {
		return toApiPost(
			buyMsg$.map(({portfolioName, symbol, amount}) => ({
				url: `/portfolio/${portfolioName}/buy`,
				data: {symbol, amount}
			}))
		)
	}
}