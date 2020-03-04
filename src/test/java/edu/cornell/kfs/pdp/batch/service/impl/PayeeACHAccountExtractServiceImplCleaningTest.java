package edu.cornell.kfs.pdp.batch.service.impl;

import static org.junit.Assert.assertEquals;

import org.apache.commons.lang3.StringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.cornell.kfs.pdp.businessobject.PayeeACHAccountExtractDetail;

public class PayeeACHAccountExtractServiceImplCleaningTest {

    private PayeeACHAccountExtractServiceImpl payeeACHAccountExtractService;

    @Before
    public void setUp() throws Exception {
        payeeACHAccountExtractService = new PayeeACHAccountExtractServiceImpl();
    }

    @After
    public void tearDown() throws Exception {
        payeeACHAccountExtractService = null;
    }

    @Test
    public void testCleanPayeeACHAccountExtractDetailNoClean() {
        String bankAccountNumber = "12345";
        validateCleanPayeeACHAccountExtractDetail(bankAccountNumber, bankAccountNumber);
    }
    
    @Test
    public void testCleanPayeeACHAccountExtractDetailCleanDashes() {
        String bankAccountNumber = "1-2---3-4-5----6-";
        String expectedBankAccount = "123456";
        validateCleanPayeeACHAccountExtractDetail(bankAccountNumber, expectedBankAccount);
    }
    
    @Test
    public void testCleanPayeeACHAccountExtractDetailCleanDashesWithLetters() {
        String bankAccountNumber = "A1-2---3-4-5---6-";
        String expectedBankAccount = "A123456";
        validateCleanPayeeACHAccountExtractDetail(bankAccountNumber, expectedBankAccount);
    }
    
    @Test
    public void testCleanPayeeACHAccountExtractDetailWithLetters() {
        String bankAccountNumber = "A12345";
        validateCleanPayeeACHAccountExtractDetail(bankAccountNumber, bankAccountNumber);
    }
    
    @Test
    public void testCleanPayeeACHAccountExtractDetailWithNull() {
        validateCleanPayeeACHAccountExtractDetail(null, null);
    }
    
    @Test
    public void testCleanPayeeACHAccountExtractDetailWithEmptyString() {
        validateCleanPayeeACHAccountExtractDetail(StringUtils.EMPTY, StringUtils.EMPTY);
    }
    
    @Test
    public void testCleanPayeeACHAccountExtractDetailWithSpaces() {
        validateCleanPayeeACHAccountExtractDetail(" 1 23  4 5 6 ", "123456");
    }
    
    @Test
    public void testCleanPayeeACHAccountExtractDetailWithSpacesAndDashes() {
        validateCleanPayeeACHAccountExtractDetail(" 1 2----3  4 - 5 6-7 ", "1234567");
    }
    
    @Test
    public void testCleanPayeeACHAccountExtractDetailWithSpacesAndDashesAndLetters() {
        validateCleanPayeeACHAccountExtractDetail("A 1 2----3  4 - 5 6-7 B ", "A1234567B");
    }
    
    @Test
    public void testCleanPayeeACHAccountExtractDetailWithJustSpaces() {
        validateCleanPayeeACHAccountExtractDetail("   ", StringUtils.EMPTY);
    }
    
    private void validateCleanPayeeACHAccountExtractDetail(String bankAccountNumber, String expectedCleanedBankAccountNumber) {
        PayeeACHAccountExtractDetail detail = new PayeeACHAccountExtractDetail();
        detail.setBankAccountNumber(bankAccountNumber);
        payeeACHAccountExtractService.cleanPayeeACHAccountExtractDetail(detail);
        
        assertEquals(expectedCleanedBankAccountNumber, detail.getBankAccountNumber());
    }

}
