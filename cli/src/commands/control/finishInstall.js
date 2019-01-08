import {setupFlow} from "../SetupFlow";

export const finishInstallCmd = {
	name: 'finishinstall',
	action: (cmd) => {
		setupFlow()
		.then((didIt) => {

		})
		.catch((error) => {
			console.log(error)
		})
	}
}
