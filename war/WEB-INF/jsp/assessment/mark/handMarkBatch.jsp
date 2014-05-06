<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="nextStudent" value="-1"/>
<c:forEach var="student" items="${hasSubmission}" varStatus="studentStatus">
		<c:choose>
			<c:when test="${student && savingStudentIndex == studentStatus.index}">
				<a href="../${studentStatus.index}/" ><div title="${myStudents[studentStatus.index].username}" class="handMarkingStudent submitted current">&nbsp;</div></a>
			</c:when>
			<c:when test="${student}">
				<a href="../${studentStatus.index}/" ><div title="${myStudents[studentStatus.index].username}" class="handMarkingStudent submitted">&nbsp;</div></a>
				<c:if test="${savingStudentIndex < studentStatus.index && nextStudent == -1}">
					<c:set var="nextStudent" value="${studentStatus.index}"/>
				</c:if>
			</c:when>
			<c:otherwise>
				<div title="${myStudents[studentStatus.index].username}" class="handMarkingStudent didnotsubmit">&nbsp;</div>
			</c:otherwise>
		</c:choose>
</c:forEach>

<h1>${assessmentName} - ${student} -  Submitted: ${assessmentResult.submissionDate}</h1>

<ul class="list">
<jsp:include page="../../recursive/fileWriter.jsp"/>
</ul>

<style>
th, td{
	min-width:200px;
}
</style>
<c:choose>
	<c:when test="${not empty student}">
		<form:form commandName="assessmentResult" action="../${nextStudent}/" enctype="multipart/form-data" method="POST">
		<c:choose>
				<c:when test="${assessmentResult.compileError}">
					<div style="width:100%; text-align:right;">
						<button type="button" onclick='$("#details").slideToggle("slow")'>Details</button>
					</div>
					<h5>Compile Errors</h5>
					<div id="details" class="ui-state-error ui-corner-all" style="font-size: 1em;display:none;">
						<pre>
							${assessmentResult.compilationError}
						</pre>
					</div>
				</c:when>
				<c:otherwise>
					<h3>Automatic Marking Results</h3>
					<c:if test="${not empty assessmentResult.assessment.unitTests or not empty assessmentResult.assessment.secretUnitTests}">
						<c:forEach var="allUnitTests" items="${assessmentResult.unitTests}">
							<c:choose>
								<c:when test="${allUnitTests.secret}">
									<c:forEach var="unitTestCase" items="${allUnitTests.testCases}">
										<div class="pastaUnitTestBoxResult pastaUnitTestBoxResultSecret${unitTestCase.testResult}" title="${unitTestCase.testName}">&nbsp</div>
									</c:forEach>
								</c:when>
								<c:otherwise>
									<c:forEach var="unitTestCase" items="${allUnitTests.testCases}">
										<div class="pastaUnitTestBoxResult pastaUnitTestBoxResult${unitTestCase.testResult}" title="${unitTestCase.testName}">&nbsp</div>
									</c:forEach>
								</c:otherwise>
							</c:choose>
						</c:forEach>
					</c:if>
					<div style="width:100%; text-align:right;">
						<button type=button onclick='$("#details").slideToggle("slow")'>Details</button>
					</div>
					<div id="details" style="display:none">
						<c:if test="${not empty assessmentResult.assessment.unitTests or not empty assessmentResult.assessment.secretUnitTests}">
							<table class="pastaTable" >
								<tr><th>Status</th><th>Test Name</th><th>Execution Time</th><th>Message</th></tr>
								<c:forEach var="allUnitTests" items="${assessmentResult.unitTests}">
									<c:forEach var="testCase" items="${allUnitTests.testCases}">
										<tr>
											<td><span class="pastaUnitTestResult pastaUnitTestResult${testCase.testResult}">${testCase.testResult}</span></td>
											<td style="text-align:left;">${testCase.testName}</td>
											<td>${testCase.time}</td>
											<td>
												<pre>${testCase.type} - ${fn:replace(fn:replace(testCase.testMessage, '>', '&gt;'), '<', '&lt;')}</pre>

											</td>
										</tr>
									</c:forEach>
								</c:forEach>
							</table>
						</c:if>
					</div>
				</c:otherwise>
			</c:choose>

			<h3>Hand Marking Guidelines</h3>
			<c:forEach var="handMarking" items="${handMarkingList}" varStatus="handMarkingStatus">
				<input type="hidden" name="student" value="${student}"/>
				<c:choose>
					<c:when test="${empty last}">
						<input type="submit" onclick="changed = false;" value="Save and continue" id="submit" style="margin-top:1em;"/>
					</c:when>
					<c:otherwise>
						<input type="submit" onclick="changed = false;" value="Save and exit" id="submit" style="margin-top:1em;"/>
					</c:otherwise>
				</c:choose>
				<form:input type="hidden" path="handMarkingResults[${handMarkingStatus.index}].handMarkingTemplateShortName" value="${handMarking.handMarking.shortName}"/>
				<div style="width:100%; overflow:auto">
					<table id="handMarkingTable${handMarkingStatus.index}" style="table-layout:fixed; overflow:auto">
						<thead>
							<tr>
								<th></th> <!-- empty on purpose -->
								<c:forEach items="${handMarking.handMarking.columnHeader}" varStatus="columnStatus">
									<th style="cursor:pointer" onclick="clickAllInColumn(this.cellIndex, ${handMarkingStatus.index})">
										${handMarking.handMarking.columnHeader[columnStatus.index].name}</br>
										${handMarking.handMarking.columnHeader[columnStatus.index].weight}</br>
									</th>
								</c:forEach>
							</tr>
						</thead>
						<tbody>
							<c:forEach var="row" items="${handMarking.handMarking.rowHeader}" varStatus="rowStatus">
								<tr>
									<th>
										${handMarking.handMarking.rowHeader[rowStatus.index].name}</br>
										${handMarking.handMarking.rowHeader[rowStatus.index].weight}
									</th>
									<c:forEach var="column" items="${handMarking.handMarking.columnHeader}">
										<td style="cursor:pointer" onclick="clickCell(this.cellIndex, this.parentNode.rowIndex ,${handMarkingStatus.index})">
											<c:if test="${not empty handMarking.handMarking.data[column.name][row.name] or handMarking.handMarking.data[column.name][row.name] == \"\"}">
												<span><fmt:formatNumber type="number" maxIntegerDigits="3" value="${row.weight * column.weight}" /></span>
												</br>
												${handMarking.handMarking.data[column.name][row.name]}</br>
												<form:radiobutton path="handMarkingResults[${handMarkingStatus.index}].result['${row.name}']" value="${column.name}"/>
											</c:if>
										</td>
									</c:forEach>
								</tr>
							</c:forEach>
						</tbody>
					</table>
				</div>
			</c:forEach>
			
			<form:textarea style="height:200px; width:95%" path="comments" onkeydown="changed=true;"/>
			<c:choose>
				<c:when test="${empty last}">
					<input type="submit" onclick="changed = false;" value="Save and continue" id="submit" style="margin-top:1em;"/>
				</c:when>
				<c:otherwise>
					<input type="submit" onclick="changed = false;" value="Save and exit" id="submit" style="margin-top:1em;"/>
				</c:otherwise>
			</c:choose>
			
		</form:form>
	</c:when>
	<c:otherwise>
		<b>No submissions left to mark</b>
	</c:otherwise>
</c:choose>

<div class="popup" id="comfirmPopup">
	<span class="button bClose"> <span><b>X</b></span>
	</span>
	<h1>Would you like to save your changes?</h1>
	<button id="yesButton" onClick="">Yes</button>
	<button id="noButton" onClick="">No</button>
</div>

<script>
	var changed = false;
	function clickAllInColumn(column, tableIndex){
		changed = true;
		var table=document.getElementById("handMarkingTable"+tableIndex);
		for (var i=1; i<table.rows.length; i++) {
			var currHeader = table.rows[i].cells[column].getElementsByTagName("input");
			
			if(currHeader.length != 0){
				currHeader[0].checked = true;
			}
		}
	}
	
	function clickCell(column,  row, tableIndex){
		changed = true;
		var table=document.getElementById("handMarkingTable"+tableIndex);
		var currHeader = table.rows[row].cells[column].getElementsByTagName("input");
			
		if(currHeader.length != 0){
			currHeader[0].checked = true;
		}
	}
	
	(function($) {
	$(document).ready(function() {

		window.onbeforeunload = function() {
			if(window.changed){
		    	return "You have unsaved changes!";
			}
		}
		
		$('#comments').wysiwyg({
			initialContent: function() {
				return value_of_textarea;
			},
		  controls: {
			bold          : { visible : true },
			italic        : { visible : true },
			underline     : { visible : true },
			strikeThrough : { visible : true },
			
			justifyLeft   : { visible : true },
			justifyCenter : { visible : true },
			justifyRight  : { visible : true },
			justifyFull   : { visible : true },

			indent  : { visible : true },
			outdent : { visible : true },

			subscript   : { visible : true },
			superscript : { visible : true },
			
			undo : { visible : true },
			redo : { visible : true },
			
			insertOrderedList    : { visible : true },
			insertUnorderedList  : { visible : true },
			insertHorizontalRule : { visible : true },

			h4: {
				visible: true,
				className: 'h4',
				command: ($.browser.msie || $.browser.safari) ? 'formatBlock' : 'heading',
				arguments: ($.browser.msie || $.browser.safari) ? '<h4>' : 'h4',
				tags: ['h4'],
				tooltip: 'Header 4'
			},
			h5: {
				visible: true,
				className: 'h5',
				command: ($.browser.msie || $.browser.safari) ? 'formatBlock' : 'heading',
				arguments: ($.browser.msie || $.browser.safari) ? '<h5>' : 'h5',
				tags: ['h5'],
				tooltip: 'Header 5'
			},
			h6: {
				visible: true,
				className: 'h6',
				command: ($.browser.msie || $.browser.safari) ? 'formatBlock' : 'heading',
				arguments: ($.browser.msie || $.browser.safari) ? '<h6>' : 'h6',
				tags: ['h6'],
				tooltip: 'Header 6'
			},
			cut   : { visible : true },
			copy  : { visible : true },
			paste : { visible : true },
			html  : { visible: true },
			increaseFontSize : { visible : true },
			decreaseFontSize : { visible : true },
			exam_html: {
				exec: function() {
					this.insertHtml('<abbr title="exam">Jam</abbr>');
					return true;
				},
				visible: true
			}
		  }
		});
	});
})(jQuery);
</script>