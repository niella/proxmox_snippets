	@Override
	Collection<StorageVolumeType> getRootVolumeStorageTypes() {
		return this.getStorageVolumeTypes()
	}

	/**
	 * Provides a Collection of StorageVolumeTypes that are available for data StorageVolumes
	 * @return Collection of StorageVolumeTypes
	 */
	@Override
	Collection<StorageVolumeType> getDataVolumeStorageTypes() {
		return this.getStorageVolumeTypes()
	}

	Collection<StorageVolumeType> getStorageVolumeTypes() {
		Collection<StorageVolumeType> volumeTypes = []

		volumeTypes << new StorageVolumeType(
				name: "Proxmox VM Generic Volume Type",
				code: "proxmox.vm.generic.volume.type",
				externalId: "proxmox.vm.generic.volume.type",
				displayOrder: 0
		)

		return volumeTypes
	}      
