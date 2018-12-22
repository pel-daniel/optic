import colors from 'colors'
import request from "request";
import {track} from "../../Analytics";
import {agentConnection, catchProjectErrors} from "../../optic/AgentSocket";
import clear from 'clear'
import prettyjson from 'prettyjson'
import {exec} from 'child_process'
import config from '../../config'

export const readtests = {
	name: 'readtests',
	description: 'learn runtime information from tests',
	options: [],
	action: (cmd) => {

		console.log(colors.blue('Starting Runtime Analysis...'))
		setTimeout(() => {
			agentConnection().actions.startRuntimeAnalysis()
		}, 500)

		agentConnection().onRuntimeAnalysisStarted((data) => {
			if (data.isSuccess) {
				console.log(colors.green('Runtime listeners ready. Starting tests. '))
				runTest(data.testcmd)
			} else {
				console.log(colors.red(`Internal error starting runtime analysis. Please run 'optic refresh' and try again. Error: ${data.error}`))
			}
		})

		agentConnection().onRuntimeAnalysisFinished((data) => {
			console.log('Done')
			process.exit(0)
		})

	}
}

function runTest(testcmd) {
	exec(testcmd, {cwd: config.projectDirectory, stdio: "inherit"}, (err, stdout, stderr) => {
		if (err) {
			console.log(colors.red(err))
		} else {
			console.log(stdout)
		}
		agentConnection().actions.finishRuntimeAnalysis()
	})
}
