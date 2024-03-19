    static ServiceResponse listProxmoxHypervisorHosts(HttpApiClient client, Map authConfig) {
        log.debug("listProxmoxHosts...")

        def nodes = callListApiV2(client, "nodes", authConfig).data
        nodes.each {
            def nodeNetworkInfo = callListApiV2(client, "nodes/$it.node/network", authConfig)
            def ipAddress = nodeNetworkInfo.data[0].address ?: nodeNetworkInfo.data[1].address
            it.ipAddress = ipAddress
        }

        return new ServiceResponse(success: true, data: nodes)
    }


    private static ServiceResponse callListApiV2(HttpApiClient client, String path, Map authConfig) {
        log.info("callListApiV2: path: ${path}")

        def tokenCfg = getApiV2Token(authConfig.username, authConfig.password, authConfig.apiUrl).data
        def rtn = new ServiceResponse(success: false)
        try {
            rtn.data = []
            def opts = new HttpApiClient.RequestOptions(
                    headers: [
                        'Content-Type': 'application/json',
                        'Cookie': "PVEAuthCookie=$tokenCfg.token",
                        'CSRFPreventionToken': tokenCfg.csrfToken
                    ],
                    contentType: ContentType.APPLICATION_JSON,
                    ignoreSSL: true
            )
            def results = client.callJsonApi(authConfig.apiUrl, "${authConfig.v2basePath}/${path}", null, null, opts, 'GET')
            def resultData = results.toMap().data.data
            log.info("callListApiV2 success: $results.success: ${resultData}")
            rtn.data = resultData
            if (results.success) {
                rtn.success = results.success
            }
            if(results.hasErrors()) {
                rtn.msg = "${results.getErrors()}: $results.data"
            }
        } catch(e) {
            log.error "Error in callListApiV2: ${e}", e
            rtn.msg = "Error in callListApiV2: ${e}"
            rtn.success = false
        }
        return rtn
    }

    /*private static ServiceResponse callListApiV2(HttpApiClient client, String path, Map authConfig) {
        log.debug("callListApiV2: path: ${path}")

        def tokenCfg = getApiV2Token(authConfig.username, authConfig.password, authConfig.apiUrl).data
        def rtn = new ServiceResponse(success: false)
        try {
            rtn.data = []
            def opts = new HttpApiClient.RequestOptions(
                    headers: [
                        'Content-Type': 'application/json',
                        'Cookie': "PVEAuthCookie=$tokenCfg.token",
                        'CSRFPreventionToken': tokenCfg.csrfToken
                    ],
                    contentType: ContentType.APPLICATION_JSON,
                    ignoreSSL: true
            )
            def results = client.callJsonApi(authConfig.apiUrl, "${authConfig.v2basePath}/${path}", null, null, opts, 'GET')
            def resultData = results.toMap().data.data
            log.debug("callListApiV2 success: $results.success: ${resultData}")
            rtn.data = resultData
            if (results.success) {
                rtn.success
            }
            if(results.hasErrors()) {
                rtn.msg = "${results.getErrors()}: $results.data"
            }
        } catch(e) {
            log.error "Error in callListApiV2: ${e}", e
            rtn.msg = "Error in callListApiV2: ${e}"
            rtn.success = false
        }
        return rtn
    }*/
