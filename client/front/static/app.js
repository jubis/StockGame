
const {createAction, getPortfolio} = require('./data.js')

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

	const portfolio$ = getPortfolio(selectPortfolio.$).log('portfolio')

	const state$ = Bacon.update(
		null,
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
				{(state.portfolio) ? Portfolio(state.portfolio) : null}
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

function Portfolio(model) {
	if(model.loading) {
		return (<div className="portfolio ui segment">Loading...</div>)
	}

	return (<div className="portfolio ui segment">
		<h2>{model.name}</h2>
		<p>Market value: ${model.totalValue}</p>
		<p>Cash: ${model.cash}</p>
	</div>)
}