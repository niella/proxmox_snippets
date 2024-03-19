import com.morpheusdata.core.util.HttpApiClient
import com.morpheusdata.model.Cloud


	@Override
	Boolean supportsAgent() {
		return false
	}

	@Override
	Boolean canCustomizeRootVolume() {
		return true
	}

	@Override
	Boolean canCustomizeDataVolumes() {
		return true
	}

	Boolean createDefaultInstanceType() {
		return false;
	}

	@Override
	Collection<OptionType> getOptionTypes() {
		def options = []
		options << new OptionType(
				name: 'skip agent install',
				code: 'provisionType.nutanixPrism.noAgent',
				category: 'provisionType.nutanixPrism',
				inputType: OptionType.InputType.CHECKBOX,
				fieldName: 'noAgent',
				fieldContext: 'config',
				fieldCode: 'gomorpheus.optiontype.SkipAgentInstall',
				fieldLabel: 'Skip Agent Install',
				fieldGroup:'Advanced Options',
				displayOrder: 4,
				required: false,
				enabled: true,
				editable:false,
				global:false,
				placeHolder:null,
				helpBlock:'Skipping Agent installation will result in a lack of logging and guest operating system statistics. Automation scripts may also be adversely affected.',
				defaultValue: true,
				custom:false,
				fieldClass:null
		)
		return options
	}
