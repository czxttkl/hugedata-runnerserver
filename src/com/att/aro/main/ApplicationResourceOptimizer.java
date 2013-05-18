package com.att.aro.main;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.att.aro.model.AnalysisFilter;
import com.att.aro.model.Profile;
import com.att.aro.model.ProfileException;
import com.att.aro.model.TraceData;
import com.att.aro.model.UserPreferences;
import com.att.aro.pcap.PCapAdapter;
import com.czxttkl.hugedata.server.RunnerServer;

public class ApplicationResourceOptimizer {

	private static final ResourceBundle rb = ResourceBundle.getBundle("messages");
	//public ResourceBundle resultBundle = ResourceBundle.getBundle("ResultBundle", Locale.CHINA);
	
	private static final Logger logger = Logger.getLogger(RunnerServer.class
			.getName());
	private File traceDirectory;
	private TraceData traceData;
	private TraceData.Analysis analysisData;
	private Profile profile;

	public ApplicationResourceOptimizer() {
		super();
		initialize();
	}

	/**
	 * Implements opening of selected trace directory.
	 * 
	 * @param pcap
	 * @throws IOException
	 */
	public synchronized void openPcap(File pcap) throws IOException,
			UnsatisfiedLinkError {

		clearTrace();

		this.traceDirectory = pcap.getParentFile();
		this.traceData = new TraceData(pcap);

		if (traceData != null) {
			analysisData = traceData.runAnalysis(profile, null);
			try {

				displayAnalysis(analysisData, profile, null, null);

			} catch (IOException e) {
				logger.log(Level.SEVERE,
						"Unexpected IOException analyzing trace", e);
			}
		}
		// refresh(this.profile, null, null);

		// Save selected directory for traces
		// userPreferences.setLastTraceDirectory(this.traceDirectory);

		// Change window name to reflect trace directory
		// this.setTitle(MessageFormat.format(rb.getString("aro.title"),
		// pcap.toString()));
	}

	/**
	 * Clears the previously loaded trace before loading a new trace.
	 * 
	 * @throws IOException
	 */
	public synchronized void clearTrace() throws IOException {

		if (this.traceData != null) {
			this.traceData = null;
			clearAnalysis();
		}
	}

	/**
	 * Clears the analysis data trace before loading a new trace.
	 * 
	 * @throws IOException
	 */
	private synchronized void clearAnalysis() throws IOException {

		if (this.analysisData != null) {

			this.analysisData.clear();
			//displayAnalysis(null, this.profile, null, null);

			// Free memory from previous trace
			System.gc();

		}
	}

	/**
	 * Refreshes the view based on the Profile and applications selected when a
	 * trace is loaded for analysis.
	 * 
	 * @param analysis
	 *            The trace analysis data.
	 * @param profile
	 *            The selected profile.
	 * @param selections
	 *            The collection of selected application/IPs.
	 * 
	 */
	private synchronized void displayAnalysis(TraceData.Analysis analysis,
			Profile profile, AnalysisFilter filter, String msg)
			throws IOException {

		logger.info("Enter Display");
		
		AROSimpleTabb aroSimpleTab = new AROSimpleTabb();
		aroSimpleTab.refresh(analysisData);
		/*
		 * // Force regeneration of TRA dialog this.timeRangeAnalysisDialog =
		 * null; this.excludeTimeRangeDialog = null;
		 * 
		 * getAroVideoPlayer().refresh(analysisData);
		 * getAroAdvancedTab().setAnalysisData(analysisData);
		 * getAroSimpleTab().refresh(analysisData);
		 */
		
		//getBestPracticesPanel().refresh(analysisData);
		
		/*
		 * getAnalysisResultsPanel().refresh(analysisData);
		 * getWaterfallPanel().refresh(analysis);
		 */

		this.profile = profile;
		//UserPreferences.getInstance().setLastProfile(profile);
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
		// this.addWindowStateListener(aroWindowStateListener);
		// Load user preference
		// this.traceDirectory = userPreferences.getLastTraceDirectory();

		// Default profile loaded.
/*		try {
			this.profile = ProfileManager.getInstance()
					.getLastUserProfile(null);
		} catch (ProfileException e) {
			this.profile = ProfileManager.getInstance().getDefaultProfile();
		} catch (IOException e) {
			logger.info(rb.getString("configuration.loaderror"));
			this.profile = ProfileManager.getInstance().getDefaultProfile();
		}*/

		// chartPlotOptionsDialog = new ChartPlotOptionsDialog(
		// ApplicationResourceOptimizer.this, aroAdvancedTab);

	}
}
