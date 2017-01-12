package edu.cornell.kfs.sys.service.impl;

/*
 * Copyright 2012 The Kuali Foundation.
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.kuali.rice.kim.api.role.RoleService;

import edu.cornell.kfs.sys.service.ReportWriterService;

/**
 * UConn implementation of the Kuali ReportWriterTextService. This
 * implementation exposes some reportWriterService properties like reportFile
 * and lineNumber. It also adds mail properties for the purpose of mailing the
 * report.
 *
 * @author Dave Raines
 * @version $Revision$
 */
public class ReportWriterTextServiceImpl extends org.kuali.kfs.sys.service.impl.ReportWriterTextServiceImpl
		implements ReportWriterService {
	private static Logger LOG = Logger.getLogger(ReportWriterTextServiceImpl.class);

	protected String fullFilePath;
	protected String fromAddress;
	protected String messageBody;
	protected RoleService roleService;
	protected Set<String> ccAddresses;

	public ReportWriterTextServiceImpl() {
		super();
		ccAddresses = new HashSet<String>();
	}

	/**
	 * @see org.kuali.kfs.sys.batch.service.WrappingBatchService#initialize()
	 */
	@Override
	public void initialize() {
		try {
			this.fullFilePath = generateFullFilePath();
			printStream = new PrintStream(fullFilePath);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}

		page = initialPageNumber;
		initializeBusinessObjectReportHelpers();
		this.writeHeader(title);
	}

	@Override
	public File getReportFile() {
		File report = new File(this.fullFilePath);
		return report;
	}

	@Override
	public int getLineNumber() {
		return line;
	}

	@Override
	public int getPageLength() {
		return pageLength;
	}

	@Override
	public boolean isNewPage() {
		return newPage;
	}

	@Override
	public void setNewPage(boolean newPage) {
		this.newPage = newPage;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getFullFilePath() {
		return fullFilePath;
	}

	/**
	 *
	 */
	@Override
	public String getFromAddress() {
		return fromAddress;
	}

	/**
	 *
	 */
	@Override
	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	/**
	 *
	 */
	@Override
	public Set<String> getCcAddresses() {
		return ccAddresses;
	}

	/**
	 *
	 */
	@Override
	public void setCcAddresses(Set<String> ccAddressList) {
		this.ccAddresses = ccAddressList;
	}

	/**
	 *
	 */
	@Override
	public String getMessageBody() {
		return messageBody;
	}

	/**
	 *
	 */
	@Override
	public void setMessageBody(String messageBody) {
		this.messageBody = messageBody;
	}

	/**
	 *
	 */
	@Override
	public RoleService getRoleService() {
		return roleService;
	}

	/**
	 *
	 */
	@Override
	public void setRoleService(RoleService roleService) {
		this.roleService = roleService;
	}
}