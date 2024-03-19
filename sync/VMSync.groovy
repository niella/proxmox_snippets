package com.morpheus.proxmox.ve.sync

import com.morpheus.proxmox.ve.ProxmoxVePlugin
import com.morpheus.proxmox.ve.util.ProxmoxComputeUtil
import com.morpheusdata.core.MorpheusContext
import com.morpheusdata.core.data.DataQuery
import com.morpheusdata.core.providers.CloudProvider
import com.morpheusdata.core.util.HttpApiClient
import com.morpheusdata.core.util.SyncTask
import com.morpheusdata.model.Cloud
import com.morpheusdata.model.ComputeServer
import com.morpheusdata.model.Network
import com.morpheusdata.model.OsType
import com.morpheusdata.model.projection.ComputeServerIdentityProjection
import com.morpheusdata.model.projection.NetworkIdentityProjection
import com.sun.security.ntlm.Server
import groovy.util.logging.Slf4j

import static com.morpheusdata.core.util.SyncTask.UpdateItem

@Slf4j
class VMSync {

    private Cloud cloud
    private MorpheusContext context
    private ProxmoxVePlugin plugin
    private HttpApiClient apiClient
    private CloudProvider cloudProvider
    private Map authConfig


    VMSync(ProxmoxVePlugin proxmoxVePlugin, Cloud cloud, HttpApiClient apiClient, CloudProvider cloudProvider) {
        this.@plugin = proxmoxVePlugin
        this.@cloud = cloud
        this.@apiClient = apiClient
        this.@context = proxmoxVePlugin.morpheus
        this.@cloudProvider = cloudProvider
        this.@authConfig = plugin.getAuthConfig(cloud)
    }



    def execute() {
        try {
            log.debug "Execute VMSync STARTED: ${cloud.id}"
            def cloudItems = ProxmoxComputeUtil.listVMs(apiClient, authConfig).data
            def domainRecords = context.async.computeServer.listIdentityProjections(cloud.id, null).filter {
                it.computeServerTypeCode == 'proxmox-qemu-vm'
            }

            log.debug("VM cloudItems: ${cloudItems.collect { it.toString() }}")
            log.debug("VM domainObjects: ${domainRecords.map { "${it.externalId} - ${it.name}" }.toList().blockingGet()}")

            SyncTask<ComputeServerIdentityProjection, Map, ComputeServer> syncTask = new SyncTask<>(domainRecords, cloudItems)
            syncTask.addMatchFunction { ComputeServerIdentityProjection domainObject, Map cloudItem ->

                domainObject.externalId == cloudItem.vmid.toString()
            }.onAdd { itemsToAdd ->
                addMissingVirtualMachines(cloud, itemsToAdd)
            }.onDelete { itemsToDelete ->
                removeMissingVMs(itemsToDelete)
            }.withLoadObjectDetails { updateItems ->
                Map<Long, SyncTask.UpdateItemDto<ComputeServerIdentityProjection, Map>> updateItemMap = updateItems.collectEntries { [(it.existingItem.id): it]}
                return context.async.computeServer.listById(updateItems?.collect { it.existingItem.id }).map { ComputeServer server ->
                    return new SyncTask.UpdateItem<ComputeServer, Map>(existingItem: server, masterItem: updateItemMap[server.id].masterItem)
                } 
            }.onUpdate { itemsToUpdate ->
                updateMatchingVMs(itemsToUpdate)
            }.start()
        } catch(e) {
            log.error "Error in VMSync execute : ${e}", e
        }
        log.debug "Execute VMSync COMPLETED: ${cloud.id}"
    }


    private void addMissingVirtualMachines(Cloud cloud, Collection items) {
        log.info("Adding ${items.size()} new VMs for Proxmox cloud ${cloud.name}")

        def newVMs = []

        def hostIdentitiesMap = context.async.computeServer.listIdentityProjections(cloud.id, null).filter {
            it.computeServerTypeCode == 'proxmox-ve-node'
        }.toMap {it.externalId }.blockingGet()

        def computeServerType = cloudProvider.computeServerTypes.find {
            it.code == 'proxmox-qemu-vm'
        }

        items.each { Map cloudItem ->
            def newVM = new ComputeServer(
                account          : cloud.account,
                externalId       : cloudItem.vmid,
                name             : cloudItem.name,
                externalIp       : cloudItem.ip,
                internalIp       : cloudItem.ip,
                sshHost          : cloudItem.ip,
                sshUsername      : 'root',
                provision        : false,
                cloud            : cloud,
                lvmEnabled       : false,
                managed          : false,
                serverType       : 'vm',
                status           : 'provisioned',
                uniqueId         : cloudItem.vmid,
                powerState       : cloudItem.status == 'running' ? ComputeServer.PowerState.on : ComputeServer.PowerState.off,
                maxMemory        : cloudItem.maxmem,
                maxCores         : cloudItem.maxCores,
                coresPerSocket   : cloudItem.coresPerSocket,
                parentServer     : hostIdentitiesMap[cloudItem.node],
                osType           :'unknown',
                serverOs         : new OsType(code: 'unknown'),
                category         : "proxmox.ve.vm.${cloud.id}",
                computeServerType: computeServerType
            )
            newVMs << newVM
        }
        context.async.computeServer.bulkCreate(newVMs).blockingGet()
    }


    private updateMatchingVMs(List<SyncTask.UpdateItem<ComputeServer, Map>> updateItems) {
        for (def updateItem in updateItems) {
            def existingItem = updateItem.existingItem
            def cloudItem = updateItem.masterItem

            //Add update logic here...
            //updateMachineMetrics()
        }

        //Example:
        // Nutanix - https://github.com/gomorpheus/morpheus-nutanix-prism-plugin/blob/master/src/main/groovy/com/morpheusdata/nutanix/prism/plugin/sync/VirtualMachinesSync.groovy
    }


    private removeMissingVMs(List<ComputeServerIdentityProjection> removeItems) {
        log.info("Remove Networks...")
        context.async.computeServer.bulkRemove(removeItems).blockingGet()
    }
}
