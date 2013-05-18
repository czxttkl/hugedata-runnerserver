/*
 * Copyright 2012 AT&T
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.att.aro.main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.att.aro.model.CacheEntry;
import com.att.aro.model.DomainTCPSessions;
import com.att.aro.model.TCPSession;
import com.att.aro.model.TraceData;

/**
 * Represents the Overview tab screen.
 */
public class AROSimpleTabb extends JPanel {
	private static final long serialVersionUID = 1L;

	private static final ResourceBundle rb = ResourceBundle.getBundle("messages");

	private ApplicationResourceOptimizer parent;

	/**
	 * Refreshes the content of the Overview tab with the specified trace
	 * data.
	 * 
	 * @param analysisData
	 *            - The Analysis object containing the trace data.
	 */
	public void refresh(TraceData.Analysis analysisData) {
		List<TCPSession> tcpSession;
		List<CacheEntry> dupContent;
		if (analysisData != null) {
			tcpSession = analysisData.getTcpSessions();
			dupContent = analysisData.getCacheAnalysis().getDuplicateContentWithOriginals();
		} else {
			tcpSession = Collections.emptyList();
			dupContent = Collections.emptyList();
		}

    	Collection<DomainTCPSessions> cc = DomainTCPSessions.extractDomainTCPSessions(tcpSession);
		for(DomainTCPSessions c : cc) {
			System.out.println("domainName:" + c.getDomainName());
			System.out.println("numfiles:" + c.getNumFiles());
			System.out.println("avesessionlength:" + c.getAvgSessionLength());
			for (TCPSession s: c.getSessions()){
				System.out.println(s.getAppNames());
			}
		}
//		jSimpleDomainTableModel.setData(DomainTCPSessions.extractDomainTCPSessions(tcpSession));
//		getJDuplicatesPanel().setData(dupContent);
//		deviceNetworkProfilePanel.refresh(analysisData);
//		getFileTypesChartPanel().setAnalysisData(analysisData);
//		getTraceOverviewPanel().setAnalysisData(analysisData);
//		getProperSessionTermChartPanel().setAnalysisData(analysisData);
	}



}
