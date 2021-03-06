<%@ include file="/jsp/sys/kfsTldHeader.jsp"%>

<c:set var="dvAttributes" value="${DataDictionary.DisbursementVoucherDocument.attributes}" />
<c:set var="recurringDVDetailAttributes" value="${DataDictionary.RecurringDisbursementVoucherDetail.attributes}" />
<c:set var="recurringDVAttributes" value="${DataDictionary.RecurringDisbursementVoucherDocument.attributes}" />
<kul:tab tabTitle="Pre-Disbursement Processor Status"
	defaultOpen="${KualiForm.preDisbursementProcessorTabDefaultOpen}"
	tabErrorKey="document.paymentCancelReason">
	<div class="tab-container">
		<table cellpadding=0 class="standard side-margins" summary="Recurring DV PDP Statuses">
			<tr class="header">
				<th>DV Document Number</th>
				<th>Due Date</th>
				<th>DV Status</th>
				<th>PDP Status</th>
				<th>Extract Date</th>
				<th>Paid Date</th>
				<th>Cancel Date</th>
			</tr>
			<logic:iterate indexId="ctr" name="KualiForm" property="pdpStatuses" id="currentDetail">
				<tr class="${ctr % 2 == 0 ? "highlight" : ""}">
					<td class="datacell">
						<kul:htmlControlAttribute attributeEntry="${recurringDVDetailAttributes.dvDocumentNumber}" 
							property="pdpStatuses[${ctr}].documentNumber" readOnly="true" />
					</td>
					<td class="datacell">
						<kul:htmlControlAttribute attributeEntry="${dvAttributes.disbursementVoucherDueDate}"
							property="pdpStatuses[${ctr}].dueDate" readOnly="true" /></td>
					<td class="datacell">
						<kul:htmlControlAttribute attributeEntry="${dvAttributes.disbursementVoucherPdpStatus}" 
							property="pdpStatuses[${ctr}].dvStatus" readOnly="true" />
					</td>
					<td class="datacell">
						<kul:htmlControlAttribute attributeEntry="${dvAttributes.disbursementVoucherPdpStatus}"
							property="pdpStatuses[${ctr}].pdpStatus" readOnly="true" /></td>
					<td class="datacell">
						<kul:htmlControlAttribute attributeEntry="${dvAttributes.extractDate}"
							property="pdpStatuses[${ctr}].extractDate" readOnly="true" /></td>
					<td class="datacell">
						<kul:htmlControlAttribute attributeEntry="${dvAttributes.paidDate}"
							property="pdpStatuses[${ctr}].paidDate" readOnly="true" /> 
						<c:if test="${not empty currentDetail.extractDate}">
							<fp:dvDisbursementInfo sourceDocumentNumber="${currentDetail.documentNumber}" 
								sourceDocumentType="${currentDetail.paymentDetailDocumentType}" />
						</c:if>
					</td>
					<td class="datacell">
						<kul:htmlControlAttribute attributeEntry="${dvAttributes.cancelDate}"
							property="pdpStatuses[${ctr}].cancelDate" readOnly="true" />
					</td>
				</tr>
			</logic:iterate>
			<c:if test="${KualiForm.cancelPDPPaymentsActionAvailable}">
				<tr class="header">
					<th colspan="2"></th>
					<th colspan="2">
						<kul:htmlAttributeLabel attributeEntry="${recurringDVAttributes.paymentCancelReason}" />
						<br /> 
						<kul:htmlControlAttribute attributeEntry="${recurringDVAttributes.paymentCancelReason}"
							property="document.paymentCancelReason" readOnly="false" />
					</th>
					<th colspan="2">
						<html:submit styleClass="btn btn-default" property="methodToCall.confirmAndCancel" title="Cancel PDP Payments" 
							alt="Cancel PDP Payments" value="Cancel PDP Payments" />
					</th>
					<th></th>
				</tr>
			</c:if>
		</table>
	</div>
</kul:tab>
