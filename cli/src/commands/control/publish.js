import colors from 'colors'
import {getStagedSpec} from "./stage";

export const publishCmd = {
	name: 'publish',
	description: 'publish new version of spec to useoptic.com',
	options: [],
	action: (cmd) => getStagedSpec(cmd, true)
}
