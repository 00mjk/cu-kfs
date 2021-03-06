/*
 * Copyright 2007 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function updateObjectNames( objField ) {
  function arrayContains(ary, ele) {
    var result = false;
    var count = 0;
    while (count < ary.length) {
      if (ary[count] == ele) {
        result = true;
      }
      count += 1;
    }
    return result;
  }

	// we want the base label - and the object field is in the detail section
  var detailElPrefix = findElPrefix( objField.name );
  var elPrefix = findElPrefix( detailElPrefix );
	var fiscalYear = getElementValue( elPrefix + ".universityFiscalYear" );
  // now, get all of the distinct chart of account codes from the organizations tab
  var chartCodes = [];
  var chartCode = getElementValue(elPrefix + ".add.accountReversionGlobalAccounts.chartOfAccountsCode");
  if (chartCode) {
    chartCodes.push(chartCode);
  }
  // get chart codes from labels with nested inquiry anchors, due to chart being read-only on non-addLines.
  var acctCount = 0;
  var chartElementName = elPrefix + ".accountReversionGlobalAccounts["+acctCount+"].chartOfAccountsCode.div";
  var searchResults = null;
  var chartLabel = document.getElementById(chartElementName);
  while (chartLabel) {
    // Find chart code, which should be the first word between the anchor tags and referenced by the "(\w+)" capture group below.
    searchResults = chartLabel.innerHTML.match(/<[Aa]\s[^>]*>\W*(\w+)[^<]*<\/[Aa]>/);
    chartCode = (searchResults && searchResults.length > 1) ? searchResults[1] : null;
    if (chartCode && !arrayContains(chartCodes, chartCode)) {
      chartCodes.push(chartCode);
    }
    acctCount += 1;
    chartElementName = elPrefix + ".accountReversionGlobalAccounts["+acctCount+"].chartOfAccountsCode.div";
    chartLabel = document.getElementById(chartElementName);
  }
  
	var objectCode = getElementValue( objField.name );
	var nameFieldName = detailElPrefix + ".accountReversionObject.financialObjectCodeName";
	if ( fiscalYear != "" && chartCodes.length > 0 && objectCode != "" ) {
		var dwrReply = {
			callback:function(data) {
			if ( data != null && typeof data == 'string' ) {
				setRecipientValue( nameFieldName, data );
			} else {
				setRecipientValue( nameFieldName, wrapError( "object not found" ), true );			
			} },
			errorHandler:function( errorMessage ) { 
				window.status = errorMessage;
				setRecipientValue( nameFieldName, wrapError( "object not found" ), true );
			}
		};
		ObjectCodeService.getObjectCodeNamesByCharts( fiscalYear, chartCodes, objectCode, dwrReply );
	} else if ( objectCode == "" ) {
		clearRecipients( nameFieldName );
	} else if ( chartCodes.length == 0 ) {
		setRecipientValue(nameFieldName, 'there are no chart codes currently', true );
	} else if ( fiscalYear == "" ) {
		setRecipientValue(nameFieldName, 'fiscal year is missing', true );	
	}
}
