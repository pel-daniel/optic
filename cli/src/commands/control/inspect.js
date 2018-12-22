import colors from 'colors'
import request from "request";
import {track} from "../../Analytics";
import {agentConnection, catchProjectErrors} from "../../optic/AgentSocket";
import clear from 'clear'
import prettyjson from 'prettyjson'

export const inspectCmd = {
	name: 'inspect',
	description: 'realtime inspector for code interpretation',
	options: [],
	action: (cmd) => {

		const included = [ 'apiatlas:schemas/endpoint' ]
		let lastTimer = null

		agentConnection().onContextFound(({editorSlug, relativeFilePath, line, results}) => {
			if (lastTimer) {
				clearTimeout(lastTimer)
			}
			clear()
			let nothingSinceLast = true

			console.log(`${colors.blue(editorSlug)} ${relativeFilePath}:${line}`)

			const filtered = results.models.filter(i => included.includes(i.schemaRef))
			if (filtered.length) {
				const item = filtered[0]
				if (item.sync.name) {
					console.log(`Name: ${colors.green(item.sync.name).bold}`)
					delete item.value._variables
					console.log(prettyjson.render(item.value))
				} else {
					delete item.value._variables
					console.log(prettyjson.render(item.value))
				}
			} else {
				lastTimer = setTimeout(() => console.log(colors.yellow('Nothing found at cursor position')), 100)
			}

		})

	}
}
