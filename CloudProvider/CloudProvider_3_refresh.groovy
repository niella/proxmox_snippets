import com.morpheusdata.core.util.HttpApiClient
import com.morpheus.proxmox.ve.sync.HostSync
//import com.morpheus.proxmox.ve.sync.DatastoreSync
//import com.morpheus.proxmox.ve.sync.NetworkSync
//import com.morpheus.proxmox.ve.sync.VirtualImageSync
//import com.morpheus.proxmox.ve.sync.VMSync

	@Override
	ServiceResponse refresh(Cloud cloudInfo) {

		log.debug("Refresh triggered, service url is: " + cloudInfo.serviceUrl)
		HttpApiClient client = new HttpApiClient()
		try {

			(new HostSync(plugin, cloudInfo, client)).execute()
			//(new DatastoreSync(plugin, cloudInfo, client)).execute()
			//(new NetworkSync(plugin, cloudInfo, client)).execute()
			//(new VMSync(plugin, cloudInfo, client, this)).execute()
			//(new VirtualImageSync(plugin, cloudInfo, client, this)).execute()

		} catch (e) {
			log.error("refresh cloud error: ${e}", e)
		} finally {
			if(client) {
				client.shutdownClient()
			}
		}
		return ServiceResponse.success()
	}
