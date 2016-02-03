
const {createAction, getPortfolio, doSellAll, doBuy} = require('./data.js')

const Icon = React.createClass({
	render: function() {
		return (
			<i className={classNames('fa', this.props.name)}></i>
		)
	}
})

function addAndIncludePrevious(propName) {
	return (prev, value) => {
		const newPrev = {...prev}
		newPrev[propName] = value
		return newPrev
	}
}

const model = function() {

	const selectPortfolio = createAction()
	const portfolioSelector$ = Bacon.combineTemplate({
		portfolioSelected: selectPortfolio.action,
		currentPortfolio: selectPortfolio.$
	}).toEventStream().log('portfolio selector')

	const buy = createAction()
	const bought$ = doBuy(Bacon.when(
		[buy.$, selectPortfolio.$.toProperty()], ({symbol, amount}, portfolioName) => ({symbol, amount, portfolioName})
	))

	const sellAll = createAction()
	const sold$ = doSellAll(Bacon.when(
		[sellAll.$, selectPortfolio.$.toProperty()], (symbol, portfolioName) => ({symbol, portfolioName})
	))
	const loadPortfolio$ = selectPortfolio.$
	loadPortfolio$.plug(selectPortfolio.$.sampledBy(sold$))
	loadPortfolio$.plug(selectPortfolio.$.sampledBy(bought$))
	const loadedPortfolio$ = getPortfolio(loadPortfolio$).log('portfolio')
	loadPortfolio$.plug(selectPortfolio.$.sampledBy(loadedPortfolio$.delay(5000)).log('reload'))
	const portfolio$ = Bacon.combineTemplate({
		portfolio: loadedPortfolio$,
		sellAll: sellAll.action
	}).toEventStream()

	const state$ = Bacon.update(
		{buyer: {buy: buy.action}},
		[portfolioSelector$], addAndIncludePrevious('portfolioSelector'),
		[portfolio$], addAndIncludePrevious('portfolio')
	).log('state')

	selectPortfolio.action(null)

	return state$
}

$('document').ready(() => {
	model()
		.filter(state => state != null)
		.map(state =>
			<div>
				{(state.portfolio) ?
					(<div className="portfolio-container">{Portfolio(state.portfolio)}{Buyer(state.buyer)}</div>) :
					null
				}
				{PortfolioSelector(state.portfolioSelector)}
			</div>
		)
		.onValue(elem => ReactDOM.render(elem, document.getElementById('content')))
})

function PortfolioSelector(model) {

	function selectPortfolio(event) {
		event.preventDefault()
		model.portfolioSelected(
			$('.portfolio-selector input.portfolio-name').val()
		)
	}

	return (<div className="portfolio-selector ui card">
		<div className="content">
			<p className="header">Portfolio Selector</p>
			<div className="description">
				<h4>Selected portfolio: <i>{model.currentPortfolio || '-'}</i></h4>
				<form className="ui form">
					<div className="field">
						<input type="text" className="portfolio-name"></input>
					</div>
					<button className="ui button" onClick={selectPortfolio}>Select portfolio</button>
				</form>
			</div>
		</div>
	</div>)
}

function Portfolio({portfolio: model, sellAll}) {
	if(model.loading) {
		return (<div className="portfolio ui segment">Loading...</div>)
	}

	function profitClass(profit) {
		return (profit >= 0) ? 'profit-blue' : 'profit-red'
	}

	return (<div className="portfolio ui segments">
		<div className="ui segment">
			<h2>{model.name}</h2>
			<p>Market value: ${model.marketValue}</p>
			<p>Cash: ${model.cash}</p>
			<p><strong>Total Value: ${model.totalValue}</strong></p>
			<p><strong>Profit: ${model.profit} = {model.profitPercentage}%</strong></p>
		</div>
		<div className="ui segment">
			<table className="ui definition table">
				<thead>
					<tr>
						<th></th>
						<th>Count</th>
						<th>Buy Price</th>
						<th>Bid</th>
						<th>Total Value</th>
						<th>Profit</th>
						<th></th>
					</tr>
				</thead>
				<tbody>
				{
					model.stocks.map(({symbol, count, buyPrice, symbolValue, marketValue, profit, profitPercentage}) => {


						return (<tr key={symbol}>
							<td>{symbol}</td>
							<td>{count}</td>
							<td>${buyPrice}</td>
							<td>${symbolValue}</td>
							<td>${marketValue}</td>
							<td className={profitClass(profit)}>
								<span>${profit}</span> = <span>{profitPercentage}%</span>
							</td>
							<td>
								<button onClick={() => sellAll(symbol)} className="sell-all ui red small button">
									Sell all
								</button>
							</td>
						</tr>)
					})
				}
				</tbody>
				<tfoot className="full-width">
					<tr>
						<th></th>
						<th></th>
						<th></th>
						<th></th>
						<th>${model.marketValue}</th>
						<th className={profitClass(model.profit)}>
							<span>${model.profit}</span> = <span>{model.profitPercentage}%</span>
						</th>
						<th></th>
					</tr>
				</tfoot>
			</table>
		</div>
	</div>)
}

function Buyer({buy}) {
	function sendBuy(event) {
		event.preventDefault()
		buy({
			symbol: $('.buyer input.symbol').val(),
			amount: $('.buyer input.amount').val()
		})
	}

	return (<div className="buyer ui segment">
		<div className="content">
			<p className="header">Buyer</p>
			<div className="description">
				<form className="ui form">
					<div className="field">
						<label>Symbol</label>
						<input type="text" className="symbol"></input>
					</div>
					<div className="field">
						<label>Amount</label>
						<input type="text" className="amount"></input>
					</div>
					<button className="ui button green" onClick={sendBuy}>Buy</button>
				</form>
			</div>
		</div>
	</div>)
}