package com.morpheus.proxmox.ve.sync

import com.morpheus.proxmox.ve.ProxmoxVePlugin
import com.morpheus.proxmox.ve.util.ProxmoxComputeUtil
import com.morpheusdata.core.MorpheusContext
import com.morpheusdata.core.data.DataQuery
import com.morpheusdata.core.util.HttpApiClient
import com.morpheusdata.core.util.SyncTask
import com.morpheusdata.model.Cloud
import com.morpheusdata.model.Datastore
import com.morpheusdata.model.Network
import com.morpheusdata.model.projection.NetworkIdentityProjection
import groovy.util.logging.Slf4j


@Slf4j
class NetworkSync {

    private Cloud cloud
    private MorpheusContext morpheusContext
    private ProxmoxVePlugin plugin
    private HttpApiClient apiClient
    private Map authConfig

    public NetworkSync(ProxmoxVePlugin proxmoxVePlugin, Cloud cloud, HttpApiClient apiClient) {
        this.@plugin = proxmoxVePlugin
        this.@cloud = cloud
        this.@morpheusContext = proxmoxVePlugin.morpheus
        this.@apiClient = apiClient
        this.@authConfig = plugin.getAuthConfig(cloud)
    }



    def execute() {
        try {

            log.debug "BEGIN: execute NetworkSync: ${cloud.id}"

            def cloudItems = ProxmoxComputeUtil.listProxmoxNetworks(apiClient, authConfig)
            def domainRecords = morpheusContext.async.network.listIdentityProjections(
                    new DataQuery().withFilter('typeCode', "proxmox.ve.bridge.$cloud.id")
            )

            SyncTask<NetworkIdentityProjection, Map, Network> syncTask = new SyncTask<>(domainRecords, cloudItems.data)

            syncTask.addMatchFunction { NetworkIdentityProjection domainObject, Map network ->
                domainObject?.externalId == network?.iface
            }.onAdd { itemsToAdd ->
                addMissingNetworks(cloud, itemsToAdd)
            }.onDelete { itemsToDelete ->
                removeMissingNetworks(itemsToDelete)
            }.withLoadObjectDetails { updateItems ->
                Map<Long, SyncTask.UpdateItemDto<NetworkIdentityProjection, Map>> updateItemMap = updateItems.collectEntries { [(it.existingItem.id): it]}
                return morpheusContext.async.network.listById(updateItems?.collect { it.existingItem.id }).map { Network network ->
                    return new SyncTask.UpdateItem<Network, Map>(existingItem: network, masterItem: updateItemMap[network.id].masterItem)
                }             
            }.onUpdate { itemsToUpdate ->
                updateMatchedNetworks(itemsToUpdate)
            }.start()
        } catch(e) {
            log.error "Error in NetworkSync execute : ${e}", e
        }
        log.debug "Execute NetworkSync COMPLETED: ${cloud.id}"
    }


    private addMissingNetworks(Cloud cloud, Collection addList) {
        log.debug "addMissingNetworks: ${cloud} ${addList.size()}"
        def networkType = morpheusContext.async.network.type.list(new DataQuery().withFilter('code', 'proxmox-ve-bridge-network')).blockingFirst()
        def networks = []
        try {
            for(cloudItem in addList) {
                log.debug("Adding Network: $cloudItem")
                networks << new Network(
                        externalId   : cloudItem.iface,
                        name         : cloudItem.iface,
                        cloud        : cloud,
                        displayName  : cloudItem.name,
                        description  : cloudItem.cidr,
                        cidr         : cloudItem.cidr,
                        status       : cloudItem.active,
                        code         : "stackit.network.${cloudItem.iface}",
                        typeCode     : networkType.code,
                        type         : networkType,
                        owner        : cloud.account,
                        tenantName   : cloud.account.name,
                        refType      : "ComputeZone",
                        refId        : cloud.id,
                        networkServer: cloud.networkServer,
                        providerId   : "",
                )
            }
            log.debug("Saving ${networks.size()} Networks")
            if (!morpheusContext.async.network.bulkCreate(networks).blockingGet()){
                log.error "Error saving new networks!"
            }

        } catch(e) {
            log.error "Error in creating networks: ${e}", e
        }
    }


    private updateMatchedNetworks(List<SyncTask.UpdateItem<Network, Map>> updateItems) {
        for (def updateItem in updateItems) {
            def existingItem = updateItem.existingItem
            def cloudItem = updateItem.masterItem

            //Add update logic here...
            //updateMachineMetrics()
        }

        //Example:
        // Nutanix - https://github.com/gomorpheus/morpheus-nutanix-prism-plugin/blob/master/src/main/groovy/com/morpheusdata/nutanix/prism/plugin/sync/NetworksSync.groovy
        // Openstack - https://github.com/gomorpheus/morpheus-openstack-plugin/blob/main/src/main/groovy/com/morpheusdata/openstack/plugin/sync/NetworksSync.groovy
    }


    private removeMissingNetworks(List<NetworkIdentityProjection> removeItems) {
        log.info("Remove Networks...")
        morpheusContext.async.network.bulkRemove(removeItems).blockingGet()
    }
}
