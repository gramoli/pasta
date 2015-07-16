/*
Copyright (c) 2014, Alex Radu
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
   list of conditions and the following disclaimer. 
2. Redistributions in binary form must reproduce the above copyright notice,
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

The views and conclusions contained in the software and documentation are those
of the authors and should not be interpreted as representing official policies, 
either expressed or implied, of the PASTA Project.
 */

package pasta.web.controller;

import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import pasta.domain.template.Assessment;
import pasta.domain.upload.UpdateGroupsForm;
import pasta.domain.user.PASTAGroup;
import pasta.domain.user.PASTAUser;
import pasta.service.AssessmentManager;
import pasta.service.GroupManager;
import pasta.service.UserManager;

/**
 * Controller class for group functions. 
 * <p>
 * Handles mappings of $PASTAUrl$/groups/...
 * 
 * 
 * @author Joshua Stretton
 * @version 1.0
 * @since 2015-04-13
 *
 */
@Controller
@RequestMapping("groups/")
public class GroupController {

	protected final Log logger = LogFactory.getLog(getClass());

	@Autowired
	private UserManager userManager;
	@Autowired
	private GroupManager groupManager;
	@Autowired
	private AssessmentManager assessmentManager;


	// ///////////////////////////////////////////////////////////////////////////
	// Helper Methods //
	// ///////////////////////////////////////////////////////////////////////////

	/**
	 * Get the currently logged in user.
	 * 
	 * @return the currently used user, null if nobody is logged in or user isn't registered.
	 */
	public PASTAUser getUser() {
		PASTAUser user = (PASTAUser) RequestContextHolder
				.currentRequestAttributes().getAttribute("user",
						RequestAttributes.SCOPE_SESSION);
		return user;
	}
	
	
	// ///////////////////////////////////////////////////////////////////////////
	// Model //
	// ///////////////////////////////////////////////////////////////////////////
	
	@ModelAttribute("updateGroupsForm")
	public UpdateGroupsForm loadUpdateGroupsForm() {
		return new UpdateGroupsForm();
	}

	// ///////////////////////////////////////////////////////////////////////////
	// Groups //
	// ///////////////////////////////////////////////////////////////////////////
	
	@RequestMapping(value = "/{assessmentId}/", method = RequestMethod.GET)
	public String viewAssessmentGroup(
			@PathVariable("assessmentId") long assessmentId,
			Model model) {
		PASTAUser user = getUser();
		if(user == null) {
			return "redirect:../../login/";
		}
		
		Assessment assessment = assessmentManager.getAssessment(assessmentId);
		if(!assessment.isReleasedTo(user)) {
			return "redirect:../../home/";
		}
		
		model.addAttribute("user", user);
		model.addAttribute("unikey", user);
		model.addAttribute("assessment", assessment);
		
		PASTAGroup myGroup = groupManager.getGroup(user, assessment);
		model.addAttribute("myGroup", myGroup);
		
		Set<PASTAGroup> allGroups = groupManager.getGroups(assessment);
		model.addAttribute("allGroups", allGroups);
		
		Set<PASTAGroup> otherGroups = new TreeSet<PASTAGroup>(allGroups);
		if(myGroup != null) {
			otherGroups.remove(myGroup);
		}
		model.addAttribute("otherGroups", otherGroups);
		
		Set<PASTAUser> noGroupUsers = new TreeSet<PASTAUser>(userManager.getStudentList());
		for(PASTAGroup group : allGroups) {
			noGroupUsers.removeAll(group.getMembers());
		}
		model.addAttribute("allStudents", noGroupUsers);
		
		return "user/assessmentGroups";
	}
	
	@RequestMapping(value = "/{assessmentId}/leaveGroup/", method = RequestMethod.POST)
	public String leaveGroup(
			@PathVariable("assessmentId") long assessmentId,
			Model model) {
		PASTAUser user = getUser();
		if(user == null) {
			return "redirect:../../../login/";
		}
		
		Assessment assessment = assessmentManager.getAssessment(assessmentId);
		if(!assessment.isReleasedTo(user)) {
			return "redirect:../../../home/";
		}
		
		groupManager.leaveCurrentGroup(user, assessment);
		
		return "redirect:../";
	}
	
	@RequestMapping(value = "/{assessmentId}/joinGroup/{groupId}/", method = RequestMethod.POST)
	public String joinGroup(
			@PathVariable("assessmentId") long assessmentId,
			@PathVariable("groupId") long groupId,
			Model model) {
		PASTAUser user = getUser();
		if(user == null) {
			return "redirect:../../../../login/";
		}
		
		Assessment assessment = assessmentManager.getAssessment(assessmentId);
		if(!assessment.isReleasedTo(user)) {
			return "redirect:../../../../home/";
		}
		
		PASTAGroup group = groupManager.getGroup(groupId);
		if(group == null) {
			return "redirect:../../";
		}
		
		groupManager.joinGroup(user, assessment, group);
		
		return "redirect:../../";
	}
	
	@RequestMapping(value = "/{assessmentId}/addGroup/", method = RequestMethod.POST)
	public String addGroup(
			@PathVariable("assessmentId") long assessmentId,
			Model model) {
		PASTAUser user = getUser();
		if(user == null) {
			return "redirect:../../../login/";
		}
		
		Assessment assessment = assessmentManager.getAssessment(assessmentId);
		if(!assessment.isReleasedTo(user)) {
			return "redirect:../../../home/";
		}
		
		groupManager.addGroup(assessment);
		
		return "redirect:../";
	}
	
	@RequestMapping(value = "/{assessmentId}/saveAll/", method = RequestMethod.POST)
	public String updateGroups(
			@PathVariable("assessmentId") long assessmentId,
			@ModelAttribute("updateGroupsForm") UpdateGroupsForm form,
			Model model) {
		PASTAUser user = getUser();
		if(user == null) {
			return "redirect:../../../login/";
		}
		
		Assessment assessment = assessmentManager.getAssessment(assessmentId);
		if(!assessment.isReleasedTo(user)) {
			return "redirect:../../../home/";
		}
		
		groupManager.saveAllGroups(assessment, form);
		
		return "redirect:../";
	}
}