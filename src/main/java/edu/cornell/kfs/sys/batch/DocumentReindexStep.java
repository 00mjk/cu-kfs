/**
 * 
 */
package edu.cornell.kfs.sys.batch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;


import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kew.api.KewApiServiceLocator;
import org.kuali.rice.kew.api.document.attribute.DocumentAttributeIndexingQueue;

import edu.cornell.kfs.sys.service.DocumentMaintenanceService;
import edu.cornell.kfs.sys.batch.CuAbstractStep;

/**
 * @author kwk43
 *
 */
public class DocumentReindexStep extends CuAbstractStep {

	private String stagingDirectory;
	private String fileName = "documentReindex.txt";
	
	
	
	/* (non-Javadoc)
	 * @see org.kuali.kfs.kns.bo.Step#execute(java.lang.String, java.util.Date)
	 */
	public boolean execute(String jobName, Date jobRunDate) throws InterruptedException {
        
        final DocumentAttributeIndexingQueue queue = KewApiServiceLocator.getDocumentAttributeIndexingQueue();
        
		File f = new File(stagingDirectory+File.separator+fileName);
	    ArrayList<String> docIds = new ArrayList<String>();

	    try {
	    	BufferedReader reader = new BufferedReader(new FileReader(f));

	    	String line = null;
	    	while ((line=reader.readLine()) != null) {
	    		docIds.add(line);   	
	    	}
	    } catch (IOException ioe) {
	    	ioe.printStackTrace();
	    	return false;
	    }
		for (Iterator<String> it = docIds.iterator(); it.hasNext(); ) {
			Long id = new Long(it.next());
			try {
			    queue.indexDocument(id.toString());
			} catch (Exception e) {/*move to the next doc*/}
		}
		
		
		addTimeStampToFileName(f, fileName, stagingDirectory);
		
		return true;
		//return SpringContext.getBean(DocumentMaintenanceService.class).requeueDocuments();
	}


	public void setStagingDirectory(String stagingDirectory) {
		this.stagingDirectory = stagingDirectory;
	}
	
	

}
