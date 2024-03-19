
	@Override
	Collection<NetworkType> getNetworkTypes() {

		NetworkType bridgeNetwork = new NetworkType([
				code              : 'proxmox-ve-bridge-network',
				externalType      : 'LinuxBridge',
				cidrEditable      : false,
				dhcpServerEditable: false,
				dnsEditable       : true,
				gatewayEditable   : false,
				vlanIdEditable    : false,
				canAssignPool     : true,
				name              : 'Proxmox VE Bridge Network'
		])

		return [bridgeNetwork]
	}

	@Override
	Collection<StorageVolumeType> getStorageVolumeTypes() {
		Collection<StorageVolumeType> volumeTypes = []

		volumeTypes << new StorageVolumeType(
				name: "Proxmox VM Generic Volume Type",
				code: "proxmox.vm.generic.volume.type",
				displayOrder: 0
		)

		return volumeTypes
	}
