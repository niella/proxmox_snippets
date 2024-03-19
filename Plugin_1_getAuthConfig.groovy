import com.morpheusdata.model.Cloud
import com.morpheusdata.model.AccountCredential
import groovy.util.logging.Slf4j


@Slf4j


    def getAuthConfig(Cloud cloud) {
        log.debug "getAuthConfig: ${cloud}"
        def rtn = [
                apiUrl    : cloud.serviceUrl,
                v2basePath: '/api2/json',
                username  : null,
                password  : null
        ]

        if(!cloud.accountCredentialLoaded) {
            AccountCredential accountCredential
            try {
                accountCredential = this.morpheus.async.cloud.loadCredentials(cloud.id).blockingGet()
            } catch(e) {
               log.error("Error loading cloud credentials: ${e}")
            }
            cloud.accountCredentialLoaded = true
            cloud.accountCredentialData = accountCredential?.data
        }

        if(cloud.accountCredentialData && cloud.accountCredentialData.containsKey('username')) {
            rtn.username = cloud.accountCredentialData['username']
        } else {
            rtn.username = cloud.serviceUsername
        }

        if(cloud.accountCredentialData && cloud.accountCredentialData.containsKey('password')) {
            rtn.password = cloud.accountCredentialData['password']
        } else {
            rtn.password = cloud.servicePassword
        }
        return rtn
    }
