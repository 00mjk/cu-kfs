package edu.cornell.kfs.sys.businessobject.options;

import edu.cornell.kfs.sys.batch.CuBatchFileUtils;
import edu.cornell.kfs.sys.util.SubDirectoryWalker;
import org.kuali.rice.core.api.util.KeyValue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CreateDoneBatchFileDirectoryPathValuesFinder extends CuBatchFileDirectoryPathValuesFinder {
	
	@Override
	public List<KeyValue> getKeyValues() {
		List<File> rootDirectories = CuBatchFileUtils.retrieveBatchFileStagingRootDirectories();
        List<KeyValue> keyValues = new ArrayList<KeyValue>();
        
        for (File rootDirectory: rootDirectories) {
            SubDirectoryWalker walker = new SubDirectoryWalker(keyValues);
            try {
                walker.addKeyValues(rootDirectory);
            }
            catch (IOException e) {
                throw new RuntimeException("IOException caught.", e);
            }
        }
        
        return keyValues;
	}

}
