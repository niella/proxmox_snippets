import com.morpheus.proxmox.ve.util.ProxmoxComputeUtil
import groovy.util.logging.Slf4j

@Slf4j


	@Override
	ServiceResponse validate(Cloud cloudInfo, ValidateCloudRequest validateCloudRequest) {
		log.info("validate: {}", cloudInfo)
		try {
			if(!cloudInfo) {
				return new ServiceResponse(success: false, msg: 'No cloud found')
			}

			def username, password
			def baseUrl = cloudInfo.serviceUrl
			log.debug("Service URL: $baseUrl")

			// Provided creds vs. Infra > Trust creds
			if (validateCloudRequest.credentialType == 'username-password') {
				username = validateCloudRequest.credentialUsername ?: cloudInfo.serviceUsername
				password = validateCloudRequest.credentialPassword ?: cloudInfo.servicePassword
			} else if (validateCloudRequest.credentialType == 'local') {
				username = cloudInfo.getConfigMap().get("username")
				password = cloudInfo.getConfigMap().get("password")
			} else {
				return new ServiceResponse(success: false, msg: "Unknown credential source type $validateCloudRequest.credentialType")
			}

			// Integration needs creds and a base URL
			if (username?.length() < 1 ) {
				return new ServiceResponse(success: false, msg: 'Enter a username.')
			} else if (password?.length() < 1) {
				return new ServiceResponse(success: false, msg: 'Enter a password.')
			} else if (cloudInfo.serviceUrl.length() < 1) {
				return new ServiceResponse(success: false, msg: 'Enter a base url.')
			}

			// Setup token get using util class
			log.info("Attempting authentication to populate access token and csrf token.")
			def tokenTest = ProxmoxComputeUtil.getApiV2Token(username, password, baseUrl)
			if (tokenTest.success) {
				return new ServiceResponse(success: true, msg: 'Cloud connection validated using provided credentials and URL...')
			} else {
				return new ServiceResponse(success: false, msg: 'Unable to validate cloud connection using provided credentials and URL')
			}
		} catch(e) {
			log.error('Error validating cloud', e)
			return new ServiceResponse(success: false, msg: "Error validating cloud ${e}")
		}
	}
