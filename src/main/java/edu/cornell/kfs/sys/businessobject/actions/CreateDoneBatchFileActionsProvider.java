package edu.cornell.kfs.sys.businessobject.actions;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.datadictionary.Action;
import org.kuali.kfs.krad.bo.BusinessObjectBase;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.krad.util.UrlFactory;
import org.kuali.kfs.sys.batch.BatchFile;
import org.kuali.kfs.sys.batch.BatchFileUtils;
import org.kuali.kfs.sys.businessobject.actions.BatchFileActionsProvider;
import org.kuali.rice.kim.api.identity.Person;

import org.kuali.kfs.krad.util.KRADConstants;

import edu.cornell.kfs.sys.CUKFSPropertyConstants;
import edu.cornell.kfs.sys.batch.service.CreateDoneBatchFileAuthorizationService;
import edu.cornell.kfs.sys.businessobject.lookup.CreateDoneBatchFileLookupableHelperServiceImpl;

public class CreateDoneBatchFileActionsProvider extends BatchFileActionsProvider {
    private static final Logger LOG = LogManager.getLogger(CreateDoneBatchFileActionsProvider.class);
    protected static String KRAD_URL_PREFIX = "kr/";
    
    protected CreateDoneBatchFileAuthorizationService createDoneAuthorizationService;
    
    @Override
    public List<Action> getActionLinks(BusinessObjectBase businessObject, Person user) {
        BatchFile batchFile = (BatchFile) businessObject;
        List<Action> actionLinks = new LinkedList<>();

        if (canCreateDoneFile(batchFile, user)) {
            actionLinks.add(new Action("Create Done", "GET", KRAD_URL_PREFIX + getCreateDoneUrl(batchFile)));
        }

        return actionLinks;
    }
    
    protected String getCreateDoneUrl(BatchFile batchFile) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("filePath",
                BatchFileUtils.pathRelativeToRootDirectory(batchFile.retrieveFile().getAbsolutePath()));
        parameters.put(KRADConstants.DISPATCH_REQUEST_PARAMETER, "createDone");
        return UrlFactory.parameterizeUrl("../createDoneBatchFileAdmin.do", parameters);
    }
    
    protected boolean canCreateDoneFile(BatchFile batchFile, Person user) {
        boolean isDoneFile = StringUtils.endsWith(batchFile.getFileName(), ".done");
        return (!isDoneFile && createDoneAuthorizationService.canCreateDoneFile(batchFile, user));
    }
    
    public CreateDoneBatchFileAuthorizationService getCreateDoneAuthorizationService() {
        return createDoneAuthorizationService;
    }

    public void setCreateDoneAuthorizationService(
            CreateDoneBatchFileAuthorizationService createDoneAuthorizationService) {
        this.createDoneAuthorizationService = createDoneAuthorizationService;
    }
}
