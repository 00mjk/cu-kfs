package edu.cornell.kfs.fp.businessobject;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.kuali.rice.krad.bo.PersistableBusinessObjectExtensionBase;

public class ProcurementCardTransactionExtendedAttribute extends PersistableBusinessObjectExtensionBase{

    private static final long serialVersionUID = 1L;
    private Integer transactionSequenceRowNumber;
    private String transactionType;
    private List<PurchasingDataRecord> purchasingDataRecords;
	
	public ProcurementCardTransactionExtendedAttribute() {
	  purchasingDataRecords = new ArrayList<PurchasingDataRecord>();
	}
	
	/**
	 * Gets the transactionSequenceRowNumber.
	 * 
	 * @return transactionSequenceRowNumber
	 */
	public Integer getTransactionSequenceRowNumber() {
		return transactionSequenceRowNumber;
	}

	/**
	 * Sets the transactionSequenceRowNumber.
	 * 
	 * @param transactionSequenceRowNumber
	 */
	public void setTransactionSequenceRowNumber(Integer transactionSequenceRowNumber) {
		this.transactionSequenceRowNumber = transactionSequenceRowNumber;
	}

	/**
	 * Gets the transactionType.
	 * 
	 * @return transactionType
	 */
	public String getTransactionType() {
		return transactionType;
	}

	/**
	 * Sets the transactionType.
	 * 
	 * @param transactionType
	 */
	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

	/**
	 * @return the purchasingDataDetails
	 */
	public List<PurchasingDataRecord> getPurchasingDataRecords() {
      return purchasingDataRecords;
	}

	/**
     * @param purchasingData the purchasingDataRecords to set
     */
    public void setPurchasingDataRecords(List<PurchasingDataRecord> purchasingDataRecords) {
      this.purchasingDataRecords = purchasingDataRecords;
    }
    
    /**
     * Adds any addendum lines following the main ProcurementCardTransaction.
     * 
     * @param bufferedFileReader The reader for the current file
     * @param lineCount The current line count
     * @throws IOException Triggered when unable to parse a given line
     */
    public int addAddendumLines(BufferedReader bufferedFileReader, int lineCount) throws IOException {
      this.setPurchasingDataRecords(parseUSBankType50Lines(bufferedFileReader, lineCount));
      return lineCount + this.getPurchasingDataRecords().size();
    }
    
    /**
     * Parses any Type 50 record addendum lines. 
     * Type 50 lines for a given transaction are expected to appear after a Type 5 record line.
     * There may be zero to many per transaction.
     * 
     * @param bufferedFileReader The reader for the current file
     * @param lineCount The current line count
     * @throws IOException Triggered when unable to parse a given line
     */
    @SuppressWarnings("unused")
    private List<PurchasingDataRecord> parseUSBankType50Lines(BufferedReader bufferedFileReader, int lineCount) throws IOException  {
      List<PurchasingDataRecord> purchasingDataRecords = new ArrayList<PurchasingDataRecord>();
      String fileLine = null, recordId = null;
      int lineBufferLength = 401; // Lines can be 400 chars long, plus a newline

      // Advance the reader
      bufferedFileReader.mark(lineBufferLength); // Mark current location
      fileLine = bufferedFileReader.readLine(); // Read first line
      recordId = USBankRecordFieldUtils.extractNormalizedString(fileLine, 0, 2);
      
      if (recordId == null) {
          throw new IOException("Unable to determine record ID necessary in order to parse line " + lineCount);
      }

      // do nothing if it's not a type 50 line
      while(recordId.equals(PurchasingDataRecord.RECORD_ID)) {
        // parse the line
        PurchasingDataRecord purchasingDataRecord = new PurchasingDataRecord();
        purchasingDataRecord.parse(fileLine, lineCount);
        
        purchasingDataRecords.add(purchasingDataRecord);
        
        // Advance the reader
        bufferedFileReader.mark(lineBufferLength); // Mark current location
        fileLine = bufferedFileReader.readLine(); // Read first line
        if (fileLine == null) {
            throw new IOException("No Type 50 or footer record when trying to parse line " + lineCount);
        }
        
        recordId = USBankRecordFieldUtils.extractNormalizedString(fileLine, 0, 2);
        if (recordId == null) {
            throw new IOException("Unable to determine record ID necessary in order to parse line " + lineCount);
        }
      } // Process all the type 50 lines
      
      bufferedFileReader.reset(); // reset because either way we've overshot the collection of type 50's by one      
      
      return purchasingDataRecords;
    }
  
}
