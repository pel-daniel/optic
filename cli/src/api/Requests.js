import {authenticated, backendRequest} from "./_backend";

export const LoginUserRequest = ({username, password}, registerAPIToken = true) =>
	backendRequest.post('/users/login', {json: {username, password, registerAPIToken}})

export const PostSnapshot = (projectName, {snapshot, opticVersion, branch}) =>
	authenticated({json: {snapshot, opticVersion, branch}}).then(options =>
		backendRequest.post(`/projects/${encodeURIComponent(projectName)}/snapshot`, options))
