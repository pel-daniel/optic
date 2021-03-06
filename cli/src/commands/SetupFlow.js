import inquirer from 'inquirer'
import p from '../../package'
import colors from 'colors'
import {install} from '../jre/jre-install'
import {track} from "../Analytics";

export async function setupFlow() {

	install((err) => {
		if (!err) {
			// console.log(colors.green('Optic Server Installed'))
		} else {
			track('Could not install Optic server ')
			console.log(colors.red('Optic server could not be installed.' + err))
			process.exit(1)
		}
	})

}
