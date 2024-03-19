    static cloneTemplate(HttpApiClient client, Map authConfig, String templateId, String name, String nodeId) {

        def rtn = new ServiceResponse(success: true)
        def nextId = callListApiV2(client, "cluster/nextid", authConfig).data
        log.info("Next VM Id is: $nextId")

        try {
            def tokenCfg = getApiV2Token(authConfig.username, authConfig.password, authConfig.apiUrl).data
            rtn.data = []
            def opts = [
                    headers: [
                            'Content-Type': 'application/json',
                            'Cookie': "PVEAuthCookie=$tokenCfg.token",
                            'CSRFPreventionToken': tokenCfg.csrfToken
                    ],
                    body: [
                            'newid': nextId,
                            'node': nodeId,
                            'vmid': templateId,
                            'name': name
                    ],
                    contentType: ContentType.APPLICATION_JSON,
                    ignoreSSL: true
            ]

            log.info("Cloning template $templateId to VM $nextId")
            def results = client.callJsonApi(
                    (String) authConfig.apiUrl,
                    "${authConfig.v2basePath}/nodes/$nodeId/qemu/$templateId/clone",
                    null, null,
                    new HttpApiClient.RequestOptions(opts),
                    'POST'
            )

            def resultData = results.content
            if(results?.success && !results?.hasErrors()) {
                rtn.success = true
                rtn.data = resultData
                opts.body = [vmid: nextId, node: nodeId]
                def startResults = client.callJsonApi(
                        (String) authConfig.apiUrl,
                        "${authConfig.v2basePath}/nodes/$nodeId/qemu/$nextId/status/start",
                        null, null,
                        new HttpApiClient.RequestOptions(opts),
                        'POST'
                )
            } else {
                rtn.msg = "Provisioning failed: $results.data"
                rtn.success = false
            }
        } catch(e) {
            log.error "Error Provisioning VM: ${e}", e
            return ServiceResponse.error("Error Provisioning VM: ${e}")
        }
        return rtn
    }
