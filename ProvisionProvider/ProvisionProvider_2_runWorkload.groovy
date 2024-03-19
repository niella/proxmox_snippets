import com.morpheusdata.core.util.HttpApiClient
import com.morpheusdata.model.Cloud
import com.morpheus.proxmox.ve.util.ProxmoxComputeUtil
import groovy.util.logging.Slf4j


@Slf4j


	@Override
	ServiceResponse<ProvisionResponse> runWorkload(Workload workload, WorkloadRequest workloadRequest, Map opts) {
		log.info("In runWorkload...")
		Thread.sleep(10000)

		ComputeServer server = workload.server
		Cloud cloud = server.cloud
		Map authConfig = plugin.getAuthConfig(cloud)
		HttpApiClient client = new HttpApiClient()
		String nodeId = workload.server.getConfigProperty('proxmoxNode') ?: null

		log.info("Provisioning/cloning: ${workload.getInstance().name} from Image Id: $server.sourceImage.externalId on node: $nodeId")
		ServiceResponse rtn = ProxmoxComputeUtil.cloneTemplate(client, authConfig, server.sourceImage.externalId, workload.getInstance().name, nodeId)

		return new ServiceResponse<ProvisionResponse>(
				true,
				"Provisioned",
				null,
				new ProvisionResponse(
						success: rtn.success,
						noAgent: true,
						skipNetworkWait: true,
						installAgent: false,
						externalId: "abc123",
						//publicIp: "10.10.10.10",
						privateIp: "1.1.1.1"
				)
		)
	}

