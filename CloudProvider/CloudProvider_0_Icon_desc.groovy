  @Override
	String getDescription() {
		return 'Proxmox Virtual Environment Integration'
	}

	/**
	 * Returns the Cloud logo for display when a user needs to view or add this cloud. SVGs are preferred.
	 * @since 0.13.0
	 * @return Icon representation of assets stored in the src/assets of the project.
	 */
	@Override
	Icon getIcon() {
		return new Icon(path:'proxmox-full-lockup-color.svg', darkPath:'proxmox-full-lockup-inverted-color.svg')
	}

	/**
	 * Returns the circular Cloud logo for display when a user needs to view or add this cloud. SVGs are preferred.
	 * @since 0.13.6
	 * @return Icon
	 */
	@Override
	Icon getCircularIcon() {
		return new Icon(path:'proxmox-logo-stacked-color.svg', darkPath:'proxmox-logo-stacked-inverted-color.svg')
	}


	@Override
	Collection<OptionType> getOptionTypes() {
		Collection<OptionType> options = []

		options << new OptionType(
				name: 'Proxmox API URL',
				code: 'proxmox-url',
				displayOrder: 0,
				fieldContext: 'domain',
				fieldLabel: 'Proxmox API URL',
				fieldCode: 'gomorpheus.optiontype.serviceUrl',
				fieldName: 'serviceUrl',
				inputType: OptionType.InputType.TEXT,
				required: true,
				defaultValue: ""
		)
		options << new OptionType(
				code: 'proxmox-credential',
				inputType: OptionType.InputType.CREDENTIAL,
				name: 'Credentials',
				fieldName: 'type',
				fieldLabel: 'Credentials',
				fieldContext: 'credential',
				required: true,
				defaultValue: 'local',
				displayOrder: 1,
				optionSource: 'credentials',
				config: '{"credentialTypes":["username-password"]}'
		)
		options << new OptionType(
				name: 'User Name',
				code: 'proxmox-username',
				displayOrder: 2,
				fieldContext: 'config',
				fieldLabel: 'User Name',
				fieldCode: 'gomorpheus.optiontype.UserName',
				fieldName: 'username',
				inputType: OptionType.InputType.TEXT,
				localCredential: true,
				required: true
		)
		options << new OptionType(
				name: 'Password',
				code: 'proxmox-password',
				displayOrder: 3,
				fieldContext: 'config',
				fieldLabel: 'Password',
				fieldCode: 'gomorpheus.optiontype.Password',
				fieldName: 'password',
				inputType: OptionType.InputType.PASSWORD,
				localCredential: true,
				required: true
		)
/*		options << new OptionType(
				name: 'Proxmox Token',
				code: 'proxmox-token',
				displayOrder: 4,
				fieldContext: 'config',
				fieldLabel: 'Proxmox Token',
				fieldCode: 'gomorpheus.optiontype.Token',
				fieldName: 'token',
				inputType: OptionType.InputType.PASSWORD,
				localCredential: false,
				required: true
		)
*/
		return options
	}



