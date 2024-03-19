package com.morpheus.proxmox.ve.sync

import com.morpheus.proxmox.ve.ProxmoxVePlugin
import com.morpheus.proxmox.ve.util.ProxmoxComputeUtil
import com.morpheusdata.core.MorpheusContext
import com.morpheusdata.core.util.HttpApiClient
import com.morpheusdata.core.util.SyncTask
import com.morpheusdata.model.Account
import com.morpheusdata.model.Cloud
import com.morpheusdata.model.ComputeServer
import com.morpheusdata.model.Datastore
import com.morpheusdata.model.StorageVolume
import com.morpheusdata.model.projection.ComputeServerIdentityProjection
import com.morpheusdata.model.projection.DatastoreIdentity
import groovy.util.logging.Slf4j



@Slf4j
class DatastoreSync {

    private Cloud cloud
    private MorpheusContext morpheusContext
    private ProxmoxVePlugin plugin
    private HttpApiClient apiClient
    private Map authConfig


    public DatastoreSync(ProxmoxVePlugin proxmoxVePlugin, Cloud cloud, HttpApiClient apiClient) {
        this.@plugin = proxmoxVePlugin
        this.@cloud = cloud
        this.@morpheusContext = proxmoxVePlugin.morpheus
        this.@apiClient = apiClient
        this.@authConfig = plugin.getAuthConfig(cloud)
    }



    def execute() {
        log.debug "Datastore Sync STARTED: ${cloud.id}"

        def datastoreResults = ProxmoxComputeUtil.listProxmoxDatastores(apiClient, authConfig)
        log.debug("Datastore list results: $datastoreResults")
        if (datastoreResults.success) {
            def cloudItems = datastoreResults?.data
            def domainRecords = morpheusContext.async.cloud.datastore.listSyncProjections(cloud.id)

            SyncTask<DatastoreIdentity, Map, StorageVolume> syncTask = new SyncTask<>(domainRecords, cloudItems as Collection)
            syncTask.addMatchFunction { DatastoreIdentity domainObject, Map cloudItem ->
                domainObject.externalId == cloudItem.storage
            }.withLoadObjectDetails { List<SyncTask.UpdateItemDto<DatastoreIdentity, Map>> updateItems ->
                Map<Long, SyncTask.UpdateItemDto<DatastoreIdentity, Map>> updateItemMap = updateItems.collectEntries { [(it.existingItem.id): it]}
                return morpheusContext.async.cloud.datastore.listById(updateItems?.collect { it.existingItem.id }).map { Datastore datastore ->
                    return new SyncTask.UpdateItem<Datastore, Map>(existingItem: datastore, masterItem: updateItemMap[datastore.id].masterItem)
                }            
            }.onAdd { itemsToAdd ->
                addMissingDatastores(cloud, itemsToAdd)
            }.onUpdate { List<SyncTask.UpdateItem<Datastore, Map>> updateItems ->
                updateMatchedDatastores(cloud, updateItems)
            }.onDelete { removeItems ->
                removeMissingDatastores(cloud, removeItems)
            }.start()

        }

        log.debug "Datastore Sync COMPLETED: ${cloud.id}"
    }


    private addMissingDatastores(Cloud cloud, Collection itemsToAdd) {

        try {
            def adds = []
            itemsToAdd?.each { cloudItem ->
                log.debug("Adding datastore: $cloudItem")
                def datastoreConfig = [
                    owner       : new Account(id: cloud.owner.id),
                    name        : cloudItem.storage,
                    externalId  : cloudItem.storage,
                    cloud       : cloud,
                    storageSize : cloudItem.total.toLong(),
                    freeSpace   : cloudItem.avail.toLong(),
                    category    : "proxmox-ve-datastore.${cloud.id}",
                    drsEnabled  : false,
                    online      : true,
                    refType     : 'ComputeZone',
                    refId       : cloud.id,
                ]
                log.debug("Adding datastore: $datastoreConfig")
                Datastore add = new Datastore(datastoreConfig)
                adds << add
            }
            morpheusContext.async.cloud.datastore.create(adds).blockingGet()
        } catch (e) {
            log.error "Error in addMissingDatastores: ${e}", e
        }
    }


    private updateMatchedDatastores(Cloud cloud, List<SyncTask.UpdateItem<Datastore, Map>> updateItems) {
        for (def updateItem in updateItems) {
            def existingItem = updateItem.existingItem
            def cloudItem = updateItem.masterItem

            //Add update logic here...
            //updateMachineMetrics()
        }

        //Example:
        // Openstack - https://github.com/gomorpheus/morpheus-nutanix-prism-plugin/blob/master/src/main/groovy/com/morpheusdata/nutanix/prism/plugin/sync/DatastoresSync.groovy
    }


    private removeMissingDatastores(List<Datastore> removeItems) {
        log.info("Remove Datastores...")
        morpheusContext.async.cloud.datastore.bulkRemove(removeItems).blockingGet()
    }
}
