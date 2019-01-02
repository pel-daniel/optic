import colors from 'colors'
import config from '../../config'
import gitBranch from 'git-branch'
import niceTry from 'nice-try'
import {agentConnection} from "../../optic/AgentSocket";
import {Spinner} from 'cli-spinner'
import {runTest} from "./readtests";
import clear from 'clear'
import {LoginUserRequest, PostSnapshot} from "../../api/Requests";
import keytar from "keytar";
import pJson from '../../../package'

export const stageCmd = {
	name: 'stage',
	description: 'upload spec to useoptic.com',
	options: [
		["--headless", 'disables interactive prompts']
	],
	action: getStagedSpec
}

export async function getStagedSpec(cmd) {
	let branch = await niceTry(() => gitBranch(config.projectDirectory))
	if (!branch) {
		branch = 'master'
		console.log(colors.yellow(`No git repository found for project. Defaulting to branch = 'master' `))
	}

	let spinner = startSpinner('Reading project...')

	setTimeout(() => {
		agentConnection().actions.startRuntimeAnalysis()
	}, 500)

	agentConnection().onRuntimeAnalysisStarted((data) => {
		if (data.isSuccess) {
			spinner = startSpinner('Running tests...', spinner)
			runTest(data.testcmd, true)
		} else {
			spinner.stop()
			console.log(colors.yellow(`Warning: Could not run tests. Try 'optic runtests' to debug`))
			startSnapshot()
		}
	})

	let runtimeIssues = []
	agentConnection().onRuntimeAnalysisFinished(({isSuccess, results, error}) => {
		if (isSuccess) {
			console.log(colors.green(`Runtime analysis completed`))
			runtimeIssues = results.issues
		} else {
			console.log(colors.yellow(`Error executing Runtime analysis. Run 'optic readtests' to debug further. `))
		}
		startSnapshot()
	})


	function startSnapshot() {
		spinner = startSpinner('Generating Project Spec...', spinner)
		agentConnection().actions.prepareSnapshot()

	}

	agentConnection().onSnapshotDelivered((data) => {
		spinner.stop()
		console.log('\n')
		console.log(colors.green('Snapshot Generated'))
		console.log(colors.green(`${data.snapshot.apiSpec.endpoints.length} endpoints documented`))

		//add runtime issues to project issues
		data.snapshot.projectIssues = [...data.snapshot.projectIssues, runtimeIssues]

		PostSnapshot(data.snapshot.name, {snapshot: data.snapshot, opticVersion: pJson.version, branch})
			.then((response) => {
				console.log(response)
			})
			.catch((error) => {
				console.log("Could not save project snapshot" + colors.red(error.response.body))
			}).finally(() => processResult())
	})

	async function processResult() {
		console.log('\nPress (return) to view or (r) to refresh')
		const cmd = await keypress()
		console.log(cmd)
		switch (cmd) {
			case 'exit': process.exit()
			case 'open': process.exit()
			case 'refresh': {
				clear()
				agentConnection().actions.startRuntimeAnalysis()
				break;
			}
		}
	}

}

function startSpinner(message, lastSpinner) {
	if (lastSpinner) {
		lastSpinner.stop()
	}
	const s = new Spinner(`${message} %s`);
	s.setSpinnerString(18)
	s.start();
	return s
}

const keypress = async () => {
	process.stdin.setRawMode(true)
	return new Promise((resolve, reject) => process.stdin.once('data', (data) => {
		const byteArray = [...data]

		let command = null
		if (byteArray.length > 0 && byteArray[0] == 13) {
			command = 'open'
		} else if (byteArray.length > 0 && byteArray[0] == 114) {
			command = 'refresh'
		} else {
			command = 'exit'
		}

		if (!command) {
			process.stdin.setRawMode(false)
		} else {
			resolve(command)
		}
	}))
}
