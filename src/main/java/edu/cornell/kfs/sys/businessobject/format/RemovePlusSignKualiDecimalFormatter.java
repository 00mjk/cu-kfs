package edu.cornell.kfs.sys.businessobject.format;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.kfs.sys.businessobject.format.KualiDecimalFormatter;

public class RemovePlusSignKualiDecimalFormatter extends KualiDecimalFormatter {
    private static final long serialVersionUID = 5281313124678744980L;
    private static Logger LOG = Logger.getLogger(RemovePlusSignKualiDecimalFormatter.class);

    @Override
    protected Object convertToObject(String target) {
        if (StringUtils.startsWith(target, "+")) {
            LOG.debug("convertToObject, the orginal target: " + target);
            target = StringUtils.remove(target, "+");
            LOG.debug("convertToObject, the converted target: " + target);
        }
        return super.convertToObject(target);
    }
}