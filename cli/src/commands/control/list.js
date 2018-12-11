import colors from 'colors'
import request from "request";
import {track} from "../../Analytics";
import Colors from 'colors'
import {agentConnection} from "../../optic/AgentSocket";
import {shouldStart} from "../../interactive/Interactive";

export const listCmd = {
	name: 'list',
	description: 'lists models found in code',
	options: [
		["--type <string>", 'type filter']
	],
	action: (cmd) => {
		const type = cmd.args[0].type
		if (type) {
			shouldStart().then(() => {
				agentConnection().actions.collectAll(type)

				agentConnection().onCollectAllResults((data) => {
					console.log(JSON.stringify(data.results, null, 4))
					process.exit(0)
				})

			})

		} else {
			console.log(Colors.red('Type required for list command. Try "list --type <schema-type>" '))
		}

	}
}
