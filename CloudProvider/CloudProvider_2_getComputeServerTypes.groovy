import com.morpheusdata.model.PlatformType

	@Override
	Collection<ComputeServerType> getComputeServerTypes() {
		Collection<ComputeServerType> serverTypes = []

		serverTypes << new ComputeServerType (
				name: 'Proxmox VE Node',
				code: 'proxmox-ve-node',
				description: 'Proxmox VE Node',
				vmHypervisor: true,
				controlPower: false,
				reconfigureSupported: false,
				externalDelete: false,
				hasAutomation: false,
				agentType: ComputeServerType.AgentType.none,
				platform: PlatformType.unknown,
				managed: false,
				provisionTypeCode: 'proxmox-ve-provider',
				nodeType: 'proxmox-ve-node'
		)
		serverTypes << new ComputeServerType (
				name: 'Proxmox VE VM',
				code: 'proxmox-qemu-vm',
				description: 'Proxmox VE Qemu VM',
				vmHypervisor: false,
				controlPower: true,
				reconfigureSupported: false,
				externalDelete: false,
				hasAutomation: true,
				agentType: ComputeServerType.AgentType.none,
				platform: PlatformType.unknown,
				managed: false,
				provisionTypeCode: 'proxmox-ve-provider',
				nodeType: 'proxmox-qemu-vm'
		)
		return serverTypes
	}
