    static ServiceResponse listProxmoxDatastores(HttpApiClient client, Map authConfig) {
        log.debug("listProxmoxDatastores...")

        ServiceResponse datastoreResults = callListApiV2(client, "storage", authConfig)
        List<Map> datastores = datastoreResults.data
        String queryNode = "pve"
        String randomNode = null
        for (ds in datastores) {
            if (ds.containsKey("nodes")) { //some pools don't belong to any node, but api path needs node for status details
                queryNode = ((String) ds.nodes).split(",")[0]
            } else {
                if (!randomNode) {
                    randomNode = listProxmoxHypervisorHosts(client, authConfig).data.get(0).node
                }
                queryNode = randomNode
            }

            Map dsInfo = callListApiV2(client, "nodes/${queryNode}/storage/${ds.storage}/status", authConfig).data
            ds.total = dsInfo.total
            ds.avail = dsInfo.avail
            ds.used = dsInfo.used
            ds.enabled = dsInfo.enabled
        }
        datastoreResults.data = datastores
        return datastoreResults
    }


    static ServiceResponse listProxmoxNetworks(HttpApiClient client, Map authConfig) {
        log.debug("listProxmoxNetworks...")

        Collection<Map> networks = []
        ServiceResponse hosts = listProxmoxHypervisorHosts(client, authConfig)

        hosts.data.each {
            ServiceResponse hostNetworks = callListApiV2(client, "nodes/${it.node}/network", authConfig)
            hostNetworks.data.each { Map network ->
                if (network?.type == 'bridge') {
                    networks << (network)
                }
            }
        }

        return new ServiceResponse(success: true, data: networks)
    }


    static ServiceResponse listTemplates(HttpApiClient client, Map authConfig) {
        log.debug("API Util listTemplates")
        def vms = []
        def qemuVMs = callListApiV2(client, "cluster/resources", authConfig)
        qemuVMs.data.each { Map vm ->
            if (vm?.template == 1 && vm?.type == "qemu") {
                vm.ip = "0.0.0.0"
                def vmCPUInfo = callListApiV2(client, "nodes/$vm.node/qemu/$vm.vmid/config", authConfig)
                vm.maxCores = (vmCPUInfo?.data?.data?.sockets?.toInteger() ?: 0) * (vmCPUInfo?.data?.data?.cores?.toInteger() ?: 0)
                vm.coresPerSocket = vmCPUInfo?.data?.data?.cores?.toInteger() ?: 0
                vms << vm
            }
        }
        return new ServiceResponse(success: true, data: vms)
    }


    static ServiceResponse listVMs(HttpApiClient client, Map authConfig) {
        log.debug("API Util listVMs")
        def vms = []
        def qemuVMs = callListApiV2(client, "cluster/resources", authConfig)
        qemuVMs.data.each { Map vm ->
            if (vm?.template == 0 && vm?.type == "qemu") {
                def vmAgentInfo = callListApiV2(client, "nodes/$vm.node/qemu/$vm.vmid/agent/network-get-interfaces", authConfig)
                vm.ip = "0.0.0.0"
                if (vmAgentInfo.success) {
                    def results = vmAgentInfo.data?.result
                    results.each {
                        if (it."ip-address-type" == "ipv4" && it."ip-address" != "127.0.0.1" && vm.ip == "0.0.0.0") {
                            vm.ip = it."ip-address"
                        }
                    }
                }
                def vmCPUInfo = callListApiV2(client, "nodes/$vm.node/qemu/$vm.vmid/config", authConfig)
                vm.maxCores = (vmCPUInfo?.data?.data?.sockets?.toInteger() ?: 0) * (vmCPUInfo?.data?.data?.cores?.toInteger() ?: 0)
                vm.coresPerSocket = vmCPUInfo?.data?.data?.cores?.toInteger() ?: 0
                vms << vm
            }
        }
        return new ServiceResponse(success: true, data: vms)
    }
