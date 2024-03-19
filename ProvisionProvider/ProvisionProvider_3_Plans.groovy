	@Override
	Collection<ServicePlan> getServicePlans() {
		Collection<ServicePlan> plans = []
		plans << new ServicePlan([code:'proxmox-ve-vm-512', name:'1 vCPU, 512MB Memory', description:'1 vCPU, 512MB Memory', sortOrder:0,
										 maxStorage:10l * 1024l * 1024l * 1024l, maxMemory: 1l * 512l * 1024l * 1024l, maxCores:1,
										 customMaxStorage:true, customMaxDataStorage:true, addVolumes:true])

		plans << new ServicePlan([code:'proxmox-ve-vm-1024', name:'1 vCPU, 1GB Memory', description:'1 vCPU, 1GB Memory', sortOrder:1,
										 maxStorage: 10l * 1024l * 1024l * 1024l, maxMemory: 1l * 1024l * 1024l * 1024l, maxCores:1,
										 customMaxStorage:true, customMaxDataStorage:true, addVolumes:true])

		plans << new ServicePlan([code:'proxmox-ve-vm-2048', name:'1 vCPU, 2GB Memory', description:'1 vCPU, 2GB Memory', sortOrder:2,
										 maxStorage: 20l * 1024l * 1024l * 1024l, maxMemory: 2l * 1024l * 1024l * 1024l, maxCores:1,
										 customMaxStorage:true, customMaxDataStorage:true, addVolumes:true])

		plans << new ServicePlan([code:'proxmox-ve-vm-4096', name:'1 vCPU, 4GB Memory', description:'1 vCPU, 4GB Memory', sortOrder:3,
										 maxStorage: 40l * 1024l * 1024l * 1024l, maxMemory: 4l * 1024l * 1024l * 1024l, maxCores:1,
										 customMaxStorage:true, customMaxDataStorage:true, addVolumes:true])

		plans << new ServicePlan([code:'proxmox-ve-vm-8192', name:'2 vCPU, 8GB Memory', description:'2 vCPU, 8GB Memory', sortOrder:4,
								  		maxStorage: 80l * 1024l * 1024l * 1024l, maxMemory: 8l * 1024l * 1024l * 1024l, maxCores:2,
								  		customMaxStorage:true, customMaxDataStorage:true, addVolumes:true])

		plans << new ServicePlan([code:'proxmox-ve-vm-8192', name:'2 vCPU, 8GB Memory', description:'2 vCPU, 8GB Memory', sortOrder:4,
								  		maxStorage: 80l * 1024l * 1024l * 1024l, maxMemory: 8l * 1024l * 1024l * 1024l, maxCores:2,
								  		customMaxStorage:true, customMaxDataStorage:true, addVolumes:true])

		plans << new ServicePlan([code:'proxmox-ve-vm-16384', name:'2 vCPU, 16GB Memory', description:'2 vCPU, 16GB Memory', sortOrder:5,
										 maxStorage: 160l * 1024l * 1024l * 1024l, maxMemory: 16l * 1024l * 1024l * 1024l, maxCores:2,
										 customMaxStorage:true, customMaxDataStorage:true, addVolumes:true])

		plans << new ServicePlan([code:'proxmox-ve-vm-24576', name:'4 vCPU, 24GB Memory', description:'4 vCPU, 24GB Memory', sortOrder:6,
										 maxStorage: 240l * 1024l * 1024l * 1024l, maxMemory: 24l * 1024l * 1024l * 1024l, maxCores:4,
										 customMaxStorage:true, customMaxDataStorage:true, addVolumes:true])

		plans << new ServicePlan([code:'proxmox-ve-vm-32768', name:'4 vCPU, 32GB Memory', description:'4 vCPU, 32GB Memory', sortOrder:7,
										 maxStorage: 320l * 1024l * 1024l * 1024l, maxMemory: 32l * 1024l * 1024l * 1024l, maxCores:4,
										 customMaxStorage:true, customMaxDataStorage:true, addVolumes:true])

		plans << new ServicePlan([code:'proxmox-ve-internal-custom', editable:false, name:'Proxmox Custom', description:'Proxmox Custom', sortOrder:0,
										 customMaxStorage:true, customMaxDataStorage:true, addVolumes:true, customCpu: true, customCores: true, customMaxMemory: true, deletable: false, provisionable: false,
										 maxStorage:0l, maxMemory: 0l,  maxCpu:0])
		return plans
	}
