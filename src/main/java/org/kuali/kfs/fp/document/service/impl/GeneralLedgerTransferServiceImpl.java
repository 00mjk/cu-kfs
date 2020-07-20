/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2020 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.fp.document.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.A21SubAccount;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.coa.businessobject.SubAccount;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.fp.FPParameterConstants;
import org.kuali.kfs.fp.businessobject.GeneralLedgerTransferEntry;
import org.kuali.kfs.fp.businessobject.GeneralLedgerTransferSourceAccountingLine;
import org.kuali.kfs.fp.document.GeneralLedgerTransferDocument;
import org.kuali.kfs.fp.document.dataaccess.GeneralLedgerTransferEntryDao;
import org.kuali.kfs.fp.document.service.GeneralLedgerTransferService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.AccountingLineBase;
import org.kuali.kfs.sys.businessobject.TargetAccountingLine;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Transactional
public class GeneralLedgerTransferServiceImpl implements GeneralLedgerTransferService {

    private static final Logger LOG = LogManager.getLogger();

    private ParameterService parameterService;
    private BusinessObjectService businessObjectService;
    private GeneralLedgerTransferEntryDao generalLedgerTransferEntryDao;

    @Override
    public boolean doesExceedDefaultNumberOfDays(List<GeneralLedgerTransferSourceAccountingLine> sourceAccountingLines,
            List<TargetAccountingLine> targetAccountingLines) {
        final Optional<Integer> smallestSubFundNumberOfDaysOverride =
                Stream.of(sourceAccountingLines, targetAccountingLines)
                .flatMap(Collection::stream)
                .map(AccountingLineBase::getAccount)
                .map(Account::getSubFundGroupCode)
                .map(subFundGroupCode -> parameterService.getSubParameterValueAsString(GeneralLedgerTransferDocument.class,
                            FPParameterConstants.LATE_ADJUSTMENT_DEFAULT_OVERRIDE_BY_SUB_FUND,
                            subFundGroupCode))
                .filter(StringUtils::isNotEmpty)
                .map(Integer::parseInt)
                .sorted()
                .findFirst();

        int numberOfDays;
        if (smallestSubFundNumberOfDaysOverride.isPresent()) {
            numberOfDays = smallestSubFundNumberOfDaysOverride.get();
        } else {
            numberOfDays = Integer.parseInt(parameterService
                .getParameterValueAsString(GeneralLedgerTransferDocument.class,
                        FPParameterConstants.DEFAULT_NUMBER_OF_DAYS_LATE_ADJUSTMENT_TAB_REQUIRED));
        }

        for (GeneralLedgerTransferSourceAccountingLine accountingLine : sourceAccountingLines) {
            final Date transferTransactionPostingDate = accountingLine.getTransferTransactionPostingDate();
            if (ObjectUtils.isNull(transferTransactionPostingDate)) {
                LOG.error("All sourceAccountingLines on GTL should have a transferTransactionDateTimeStamp but" +
                        " this one did not. Failing validation. accountingLine=" + accountingLine.toString());
                return true;
            }

            long dateDifference = ChronoUnit.DAYS.between(
                    LocalDate.parse(transferTransactionPostingDate.toString()),
                    LocalDate.now());
            if (dateDifference > numberOfDays) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean isAnyLateAdjustmentFieldPopulated(GeneralLedgerTransferDocument generalLedgerTransferDocument) {
        return StringUtils.isNotBlank(generalLedgerTransferDocument.getExpenditureDescription())
                || StringUtils.isNotBlank(generalLedgerTransferDocument.getExpenditureProjectBenefit())
                || StringUtils.isNotBlank(generalLedgerTransferDocument.getLateAdjustmentDescription())
                || StringUtils.isNotBlank(generalLedgerTransferDocument.getLateAdjustmentReason())
                || StringUtils.isNotBlank(generalLedgerTransferDocument.getLateAdjustmentActionDescription());
    }

    @Override
    public boolean areAllLateAdjustmentFieldsPopulated(GeneralLedgerTransferDocument generalLedgerTransferDocument) {
        return StringUtils.isNotBlank(generalLedgerTransferDocument.getExpenditureDescription())
                && StringUtils.isNotBlank(generalLedgerTransferDocument.getExpenditureProjectBenefit())
                && StringUtils.isNotBlank(generalLedgerTransferDocument.getLateAdjustmentDescription())
                && StringUtils.isNotBlank(generalLedgerTransferDocument.getLateAdjustmentReason())
                && StringUtils.isNotBlank(generalLedgerTransferDocument.getLateAdjustmentActionDescription());
    }

    @Override
    public void updateGeneralLedgerTransferEntry(
            List<GeneralLedgerTransferSourceAccountingLine> gltSourceAccountingLines, String newDocumentNumber) {
        List<GeneralLedgerTransferEntry> generalLedgerTransferEntries = new ArrayList<>();

        final List<GeneralLedgerTransferEntry> queriedGeneralLedgerTransferEntries = generalLedgerTransferEntryDao
            .findSourceEntriesByGeneralLedgerTransferSourceAccountingLines(gltSourceAccountingLines, newDocumentNumber);

        for (GeneralLedgerTransferEntry gltEntry : queriedGeneralLedgerTransferEntries) {
            if (isGeneralLedgerTransferEntryDocNumberSetFromRecall(newDocumentNumber, gltEntry)) {
                continue;
            } else if (StringUtils.isNotEmpty(gltEntry.getGeneralLedgerTransferDocumentNumber())
                    && StringUtils.isNotEmpty(newDocumentNumber)) {
                final String errorMessage = "GeneralLedgerTransferDocument attempted to set "
                        + "generalLedgerTransferDocumentNumber to " + newDocumentNumber +
                        " but it was already set to "
                        + gltEntry.getGeneralLedgerTransferDocumentNumber() + ". That should not happen. gltEntry="
                        + gltEntry.toString();
                LOG.fatal(errorMessage);
                throw new RuntimeException(errorMessage);
            }

            gltEntry.setGeneralLedgerTransferDocumentNumber(newDocumentNumber);
            generalLedgerTransferEntries.add(gltEntry);
        }

        businessObjectService.save(generalLedgerTransferEntries);
    }

    @Override
    public Map<Integer, String> areSourceAccountingLinesTransferred(
            List<GeneralLedgerTransferSourceAccountingLine> gltSourceAccountingLines) {
        Map<Integer, String> result = new LinkedHashMap<>();

        for (GeneralLedgerTransferSourceAccountingLine gltSourceAccountingLine : gltSourceAccountingLines) {
            final GeneralLedgerTransferEntry gltEntry =
                generalLedgerTransferEntryDao.findSourceEntryByGeneralLedgerTransferSourceAccountingLine(
                        gltSourceAccountingLine);

            final String gltEntryTransferDocumentNumber = gltEntry.getGeneralLedgerTransferDocumentNumber();
            if (StringUtils.isNotEmpty(gltEntryTransferDocumentNumber)
                    && !gltEntryTransferDocumentNumber.equals(gltSourceAccountingLine.getDocumentNumber())) {
                result.put(gltSourceAccountingLine.getSequenceNumber(), gltEntry.getGeneralLedgerTransferDocumentNumber());
            }
        }

        return result;
    }

    @Override
    public Set<String> determineAccountingLineReferenceDocumentsThatDoNotBalance(
            List<GeneralLedgerTransferSourceAccountingLine> gltSourceAccountingLines,
        List<TargetAccountingLine> gltTargetAccountingLines) {
        Map<String, KualiDecimal> sourceBalances = constructBalanceMap(gltSourceAccountingLines);
        Map<String, KualiDecimal> targetBalances = constructBalanceMap(gltTargetAccountingLines);

        return sourceBalances.keySet().stream()
            .filter(referenceDocument -> !sourceBalances.get(referenceDocument)
                    .equals(targetBalances.get(referenceDocument)))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Map<String, KualiDecimal> constructBalanceMap(List<? extends AccountingLine> accountingLines) {
        Map<String, KualiDecimal> result = new LinkedHashMap<>();
        accountingLines.forEach(accountingLine -> {
            String key = accountingLine.getReferenceOriginCode() + "-" + accountingLine.getReferenceNumber();
            if (result.get(key) != null) {
                result.put(key, result.get(key).add(accountingLine.getAmount()));
            } else {
                result.put(key, accountingLine.getAmount());
            }
        });

        return result;
    }

    @Override
    public TargetAccountingLine copyAccountingLine(AccountingLineBase source, Class<TargetAccountingLine> targetClass,
            boolean modeSplit) throws InstantiationException, IllegalAccessException {
        TargetAccountingLine target = targetClass.newInstance();
        fieldsForCloningAccountingLine(source, target, modeSplit);

        return target;
    }

    private void fieldsForCloningAccountingLine(AccountingLineBase source, TargetAccountingLine target,
            boolean modeSplit) {
        target.setChartOfAccountsCode(source.getChartOfAccountsCode());
        target.setAccountNumber(source.getAccountNumber());
        target.setSubAccountNumber(source.getSubAccountNumber());
        target.setFinancialObjectCode(source.getFinancialObjectCode());
        target.setFinancialSubObjectCode(source.getFinancialSubObjectCode());
        target.setProjectCode(source.getProjectCode());
        target.setOrganizationReferenceId(source.getOrganizationReferenceId());
        target.setAmount(source.getAmount());
        target.setReferenceOriginCode(source.getReferenceOriginCode());
        target.setReferenceNumber(source.getReferenceNumber());
        target.setReferenceTypeCode(source.getReferenceTypeCode());
        target.setFinancialDocumentLineDescription(source.getFinancialDocumentLineDescription());

        if (modeSplit) {
            target.setDebitCreditCode(source.getDebitCreditCode());
        } else {
            final String flippedDebitCreditCode = KFSConstants.GL_DEBIT_CODE.equals(source.getDebitCreditCode())
                ? KFSConstants.GL_CREDIT_CODE : KFSConstants.GL_DEBIT_CODE;
            target.setDebitCreditCode(flippedDebitCreditCode);
        }
    }

    @Override
    public boolean canErrorCorrect(String documentNumber) {
        return generalLedgerTransferEntryDao.hasTransferredEntries(documentNumber);
    }

    @Override
    public boolean newAccountOnTarget(List<GeneralLedgerTransferSourceAccountingLine> sourceAccountingLines,
            List<TargetAccountingLine> targetAccountingLines) {
/*
 * 2020-02-13 Base code in this comment
 * 
        final Set<Account> sourceAccounts = Stream.of(sourceAccountingLines)
            .flatMap(Collection::stream)
            .map(AccountingLineBase::getAccount)
            .collect(Collectors.toSet());

        // Can't use .contains because targetAccounts are OJB Account while sourceAccounts may be not be OJB proxied
        return !Stream.of(targetAccountingLines)
            .flatMap(Collection::stream)
            .map(AccountingLineBase::getAccount)
            .allMatch(target ->
                sourceAccounts.stream().anyMatch(source ->
                    Objects.equals(target.getChartOfAccounts(), source.getChartOfAccounts())
                        && Objects.equals(target.getAccountNumber(), source.getAccountNumber()))
            );
*/
        final Set<Account> sourceAccounts = Stream.of(sourceAccountingLines)
                .flatMap(Collection::stream)
                .map(AccountingLineBase::getAccount)
                .collect(Collectors.toSet());
        
        boolean usesContainsFlag = !Stream.of(targetAccountingLines)
                .flatMap(Collection::stream)
                .map(AccountingLineBase::getAccount)
                .allMatch(sourceAccounts::contains);
        
        boolean usesAllMatchFlag = !Stream.of(targetAccountingLines)
                .flatMap(Collection::stream)
                .map(AccountingLineBase::getAccount)
                .allMatch(target ->
                    sourceAccounts.stream().anyMatch(source ->
                        Objects.equals(target.getChartOfAccounts(), source.getChartOfAccounts())
                            && Objects.equals(target.getAccountNumber(), source.getAccountNumber()))
                );
        LOG.info("newAccountOnTarget##### Stream.of USING CONTAINS instead of individual comparisons would return =" + usesContainsFlag + "=  #####");
        LOG.info("newAccountOnTarget***** Stream.of USING INDIVIDUAL ATTRIBUTE COMPARISONS FROM BASE CODE will return =" + usesAllMatchFlag + "=  *****");
        
            // Can't use .contains because targetAccounts are OJB Account while sourceAccounts may be not be OJB proxied
            return !Stream.of(targetAccountingLines)
                .flatMap(Collection::stream)
                .map(AccountingLineBase::getAccount)
                .allMatch(target ->
                    sourceAccounts.stream().anyMatch(source ->
                        Objects.equals(target.getChartOfAccounts(), source.getChartOfAccounts())
                            && Objects.equals(target.getAccountNumber(), source.getAccountNumber()))
                );
    }

    @Override
    public boolean newObjectCodeOnTarget(List<GeneralLedgerTransferSourceAccountingLine> sourceAccountingLines,
            List<TargetAccountingLine> targetAccountingLines) {
/* 
 * 2020-02-13 Base code in this comment
 * 
           final Set<ObjectCode> sourceObjectCodes = Stream.of(sourceAccountingLines)
            .flatMap(Collection::stream)
            .map(AccountingLineBase::getObjectCode)
            .collect(Collectors.toSet());

        return !Stream.of(targetAccountingLines)
            .flatMap(Collection::stream)
            .map(AccountingLineBase::getObjectCode)
            .allMatch(sourceObjectCodes::contains);
*/
        
/*
 * Base code from above still being implemented below.
 * All additional code is for log file debugging.
 */
        final Set<ObjectCode> sourceObjectCodes = Stream.of(sourceAccountingLines)
                .flatMap(Collection::stream)
                .map(AccountingLineBase::getObjectCode)
                .collect(Collectors.toSet());
        
        boolean usesContainsFlag = !Stream.of(targetAccountingLines)
                .flatMap(Collection::stream)
                .map(AccountingLineBase::getObjectCode)
                .allMatch(sourceObjectCodes::contains);
        
        boolean usesAllMatchFlag = !Stream.of(targetAccountingLines)
                .flatMap(Collection::stream)
                .map(AccountingLineBase::getObjectCode)
                .allMatch(target ->
                    sourceObjectCodes.stream().anyMatch(source ->
                        Objects.equals(target.getUniversityFiscalYear(), source.getUniversityFiscalYear())
                            && Objects.equals(target.getChartOfAccountsCode(), source.getChartOfAccountsCode())
                                && Objects.equals(target.getFinancialObjectCode(), source.getFinancialObjectCode()))
                );
        LOG.info("newObjectCodeOnTarget##### Stream.of USING BASE CODE CONTAINS IS RETURNING =" + usesContainsFlag + "=  #####");
        LOG.info("newObjectCodeOnTarget***** Stream.of USING INDIVIDUAL ATTRIBUTE COMPARISONS WOULD return =" + usesAllMatchFlag + "=  *****");

        Iterator<ObjectCode> objCdIterator = sourceObjectCodes.iterator();
        while (objCdIterator.hasNext()) {
            ObjectCode objCd = objCdIterator.next();
            LOG.info("newObjectCodeOnTarget.SOURCE LINE:: OBJECT CODE VALUE=" + (ObjectUtils.isNull(objCd) ? "null": objCd.getCode()) + "=");
        }
            
        final Set<ObjectCode> targetObjectCodes = Stream.of(targetAccountingLines)
                .flatMap(Collection::stream)
                .map(AccountingLineBase::getObjectCode)
                .collect(Collectors.toSet());
        
        Iterator<ObjectCode> targetObjCdIterator = targetObjectCodes.iterator();
        while (targetObjCdIterator.hasNext()) {
            ObjectCode objCd = targetObjCdIterator.next();
            LOG.info("newObjectCodeOnTarget.TARGET LINE:: OBJECT CODE VALUE=" + (ObjectUtils.isNull(objCd) ? "null=": objCd.getCode()) + "=");
        }
            
        return !Stream.of(targetAccountingLines)
                .flatMap(Collection::stream)
                .map(AccountingLineBase::getObjectCode)
                .allMatch(sourceObjectCodes::contains);
    }

    @Override
    public boolean costSharePresent(List<GeneralLedgerTransferSourceAccountingLine> sourceAccountingLines,
            List<TargetAccountingLine> targetAccountingLines) {
/*
 *  2020-02-13 Base code in this comment
 * 
        return Stream.of(sourceAccountingLines, targetAccountingLines)
            .flatMap(Collection::stream)
            .map(AccountingLineBase::getSubAccount)
            .filter(ObjectUtils::isNotNull)
            .map(SubAccount::getA21SubAccount)
            .filter(ObjectUtils::isNotNull)
            .map(A21SubAccount::getSubAccountTypeCode)
            .filter(StringUtils::isNotEmpty)
            .anyMatch(KFSConstants.SubAccountType.COST_SHARE::contains);
*/
        boolean value = Stream.of(sourceAccountingLines, targetAccountingLines)
                .flatMap(Collection::stream)
                .map(AccountingLineBase::getSubAccount)
                .filter(ObjectUtils::isNotNull)
                .map(SubAccount::getA21SubAccount)
                .filter(ObjectUtils::isNotNull)
                .map(A21SubAccount::getSubAccountTypeCode)
                .filter(StringUtils::isNotEmpty)
                .anyMatch(KFSConstants.SubAccountType.COST_SHARE::contains);
        
        LOG.info("costSharePresent boolean using baseCode ANY MATCH:: valueToBeReturned=" +value + "=");
        
        ListIterator<GeneralLedgerTransferSourceAccountingLine> srcIterator = sourceAccountingLines.listIterator();
        while (srcIterator.hasNext()) {
            GeneralLedgerTransferSourceAccountingLine gltSourceLine = srcIterator.next();
            //AccountingLineBase subAcct = gltSourceLine.getSubAccount();
            SubAccount subAcct = gltSourceLine.getSubAccount();
            A21SubAccount a21 = ObjectUtils.isNotNull(subAcct) ? subAcct.getA21SubAccount() : null;
            String subTypeCd = ObjectUtils.isNotNull(a21) ? a21.getSubAccountTypeCode() : null;
            LOG.info("costSharePresent.SOURCE LINE:: subAcct=" + (ObjectUtils.isNull(subAcct) ? "null": "NOT null") 
                    + "=      a21=" + (ObjectUtils.isNull(a21) ? "null": "NOT null") 
                    + "=    subAccountTypeCode=" + (ObjectUtils.isNull(subTypeCd) ? "null=": "NOT null=") + "= ************************************");
        }
        
        ListIterator<TargetAccountingLine> targetIterator = targetAccountingLines.listIterator();
        while (srcIterator.hasNext()) {
            TargetAccountingLine gltTargetLine = targetIterator.next();
            //AccountingLineBase subAcct = gltTargetLine.getSubAccount();
            SubAccount subAcct = gltTargetLine.getSubAccount();
            A21SubAccount a21 = ObjectUtils.isNotNull(subAcct) ? subAcct.getA21SubAccount() : null;
            String subTypeCd = ObjectUtils.isNotNull(a21) ? a21.getSubAccountTypeCode() : null;
            LOG.info("costSharePresent.TARGET LINE::  subAcct=" + (ObjectUtils.isNull(subAcct) ? "null": "NOT null") 
                    + "=      a21=" + (ObjectUtils.isNull(a21) ? "null": "NOT null")
                    + "=    subAccountTypeCode=" + (ObjectUtils.isNull(subTypeCd) ? "null=": "NOT null=") + "= ************************************");
        }
        return Stream.of(sourceAccountingLines, targetAccountingLines)
                .flatMap(Collection::stream)
                .map(AccountingLineBase::getSubAccount)
                .filter(ObjectUtils::isNotNull)
                .map(SubAccount::getA21SubAccount)
                .filter(ObjectUtils::isNotNull)
                .map(A21SubAccount::getSubAccountTypeCode)
                .filter(StringUtils::isNotEmpty)
                .anyMatch(KFSConstants.SubAccountType.COST_SHARE::contains);
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setGeneralLedgerTransferEntryDao(GeneralLedgerTransferEntryDao generalLedgerTransferEntryDao) {
        this.generalLedgerTransferEntryDao = generalLedgerTransferEntryDao;
    }

    private boolean isGeneralLedgerTransferEntryDocNumberSetFromRecall(String documentNumber,
            GeneralLedgerTransferEntry gltEntry) {
        return StringUtils.isNotEmpty(documentNumber)
                && documentNumber.equals(gltEntry.getGeneralLedgerTransferDocumentNumber());
    }
}
