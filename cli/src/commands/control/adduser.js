import inquirer from "inquirer";
import {projectFileGenerator} from "../../optic/ProjectFileGenerator";
import fs from "fs";
import clear from 'clear'
import colors from "colors";
import opn from 'opn'
import {LoginUserRequest} from "../../api/Requests";
import keytar from 'keytar'
export const adduserCmd = {
	name: 'adduser',
	options: [
		["--token <string>", 'add api token directly']
	],
	action: (cmd) => {

		const rawToken = cmd.rawArgs[3]
		if (rawToken) {
			keytar.setPassword('optic-cli', 'main', rawToken)
			console.log('API Token Added')
			process.exit()
		}

		inquirer
			.prompt([
				{
					type: 'confirm',
					message: 'Do you have an existing Optic account?',
					name: 'hasAccount',
				}])
			.then((answers) => {
				if (answers.hasAccount) {
					promptCredentials()
				} else {
					opn('https://useoptic.com')
					console.log(`Opening useoptic.com \nSign up and then rerun 'optic adduser'`)
					process.exit()
				}
			})
	}
}


function promptCredentials() {
	inquirer
		.prompt([
			{
				type: 'input',
				message: 'username',
				name: 'username',
			},
			{
				type: 'password',
				message: 'password',
				name: 'password',
			}
		]).then((answers) => {

		LoginUserRequest({...answers})
			.then(({isSuccess, user, apiKey}) => {
				if (isSuccess) {
					keytar.setPassword('optic-cli', 'main', apiKey)
					console.log(colors.green(`Login successful. An API Token has been issued.`))
				} else {
					clear()
					console.log(colors.red('Login invalid. Please try again'))
					promptCredentials()
				}
			})
			.catch((error) => {
				console.log(colors.red('Login invalid. Please try again'))
			})
	})
}
