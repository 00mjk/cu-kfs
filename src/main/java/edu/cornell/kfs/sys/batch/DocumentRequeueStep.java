/**
 * 
 */
package edu.cornell.kfs.sys.batch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kew.actionrequest.service.DocumentRequeuerService;
import org.kuali.rice.kew.routeheader.DocumentRouteHeaderValue;
import org.kuali.rice.kew.routeheader.service.RouteHeaderService;

import edu.cornell.kfs.sys.dataaccess.DocumentRequeueFileBuilderDao;

/**
 * @author kwk43
 *
 */
public class DocumentRequeueStep extends AbstractStep {

	private String stagingDirectory;
	private String fileName = "documentRequeue.txt";
	
	
	
	/* (non-Javadoc)
	 * @see org.kuali.kfs.sys.batch.Step#execute(java.lang.String, java.util.Date)
	 */
	public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
		
		DocumentRequeuerService requeuer = SpringContext.getBean(DocumentRequeuerService.class);

		File f = new File(stagingDirectory+File.separator+fileName);
	    List<String> docIds = new ArrayList<String>();

	    docIds = SpringContext.getBean(DocumentRequeueFileBuilderDao.class).getDocumentRequeueFileValues();
	    
		for (Iterator<String> it = docIds.iterator(); it.hasNext(); ) {
			Long id = new Long(it.next());
			requeuer.requeueDocument(id);
		}
		
		addTimeStampToFileName(f, fileName, stagingDirectory);
		
		return true;
	}

	public void setStagingDirectory(String stagingDirectory) {
		this.stagingDirectory = stagingDirectory;
	}
	
	

}
