package edu.cornell.kfs.module.ezra.batch;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.kuali.kfs.sys.batch.AbstractStep;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.kns.bo.Parameter;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.service.DateTimeService;
import org.kuali.rice.kns.service.ParameterService;

import edu.cornell.kfs.module.ezra.service.EzraService;

public class AwardStep extends AbstractStep {

	private EzraService ezraService;
	private ParameterService parameterService;
	private static String LAST_SUCCESSFUL_RUN = "LAST_SUCCESSFUL_RUN";
	
	private static final String PARAMETER_NAMESPACE_CODE = "KFS-EZRA";
    private static final String PARAMETER_NAMESPACE_STEP = "AwardStep";
    private static final String PARAMETER_APPLICATION_NAMESPACE_CODE = "KFS";
	
	public boolean execute(String arg0, java.util.Date arg1) throws InterruptedException {
		String dateString = parameterService.getParameterValue(AwardStep.class, LAST_SUCCESSFUL_RUN);
		DateTimeService dtService = SpringContext.getBean(DateTimeService.class);
		java.sql.Date lastRun = null;
		if (dateString !=null) {
			try {
				lastRun = dtService.convertToSqlDate(dateString);
			} catch (ParseException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		boolean result = ezraService.updateAwardsSince(lastRun);
	        if (result) {
	        	BusinessObjectService bos = SpringContext.getBean(BusinessObjectService.class);
	        	Map<String,Object> fieldValues = new HashMap<String,Object>();
	            fieldValues.put("parameterNamespaceCode",PARAMETER_NAMESPACE_CODE);
	            fieldValues.put("parameterDetailTypeCode",PARAMETER_NAMESPACE_STEP);
	            fieldValues.put("parameterName",LAST_SUCCESSFUL_RUN);
	            fieldValues.put("parameterApplicationNamespaceCode", PARAMETER_APPLICATION_NAMESPACE_CODE);
	        	
	        	Parameter parm = (Parameter) bos.findByPrimaryKey(Parameter.class, fieldValues);
	        	parm.setParameterValue(dtService.toDateTimeString(dtService.getCurrentSqlDate()));
	        	bos.save(parm);
	        //	//parameterService.
	        }
	        return result;
	}
	

	/**
	 * @param ezraService the ezraService to set
	 */
	public void setEzraService(EzraService ezraService) {
		this.ezraService = ezraService;
	}

	/**
	 * @param parameterService the parameterService to set
	 */
	public void setParameterService(ParameterService parameterService) {
		this.parameterService = parameterService;
	}
}
