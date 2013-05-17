package com.att.aro.main;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import com.att.aro.model.Profile;
import com.att.aro.model.ProfileException;
import com.att.aro.pcap.PCapAdapter;
import com.czxttkl.hugedata.server.RunnerServer;

public class ApplicationResourceOptimizer {

	private static final ResourceBundle rb = ResourceBundleManager
			.getDefaultBundle();
	private static final Logger logger = Logger.getLogger(RunnerServer.class
			.getName());
	private Profile profile;
	private File traceDirectory;
	
	public ApplicationResourceOptimizer() {
		super();
		initialize();
	}

	/**
	 * Initializes the ARO application.
	 */
	private void initialize() {

		try {
			// Checks that all necessary pcap libraries are installed on the
			// system.
			PCapAdapter.ping();
		} catch (UnsatisfiedLinkError ule) {
			logger.info(rb.getString("aro.winpcap_error"));
			System.exit(-1);
		} catch (Exception e) {
			logger.info(rb.getString("aro.winpcap_error"));
			System.exit(-1);
		}

		// Register aroWindowStateListener with the frame
		//this.addWindowStateListener(aroWindowStateListener);
		// Load user preference
		//this.traceDirectory = userPreferences.getLastTraceDirectory();

		// Default profile loaded.
//		try {
//			this.profile = ProfileManager.getInstance()
//					.getLastUserProfile(null);
//		} catch (ProfileException e) {
//			this.profile = ProfileManager.getInstance().getDefaultProfile();
//		} catch (IOException e) {
//			logger.info(rb.getString("configuration.loaderror"));
//			this.profile = ProfileManager.getInstance().getDefaultProfile();
//		}

//		chartPlotOptionsDialog = new ChartPlotOptionsDialog(
//				ApplicationResourceOptimizer.this, aroAdvancedTab);

	}
}
