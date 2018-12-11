import "regenerator-runtime/runtime";
import path from 'path'
const isDev = true

const jreDirectory = require('os').homedir()+'/.optic-jre'

module.exports = (() => {
	if (isDev) {
		return {
			runServerCmd: {
				options: ['-jar', `${process.cwd()}/jars/server-assembly.jar`]
			},
			storageDirectory: process.cwd()+'/.optic-storage',
			jreDirectory,
			projectDirectory: '/Users/aidancunniffe/Desktop/time-tracker'
		}
	} else {

		const jarPath = path.join(__dirname, '../jars/server-assembly.jar')

		return {
			runServerCmd: {
				options: ['-jar', jarPath]
			},
			storageDirectory: require('os').homedir()+'/.optic-storage',
			jreDirectory,
			projectDirectory: process.cwd()
		}
	}

})()

module.exports.isDev = isDev
