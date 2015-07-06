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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import pasta.domain.PASTAUser;
import pasta.domain.template.Competition;
import pasta.domain.upload.NewCompetitionForm;
import pasta.service.CompetitionManager;

/**
 * Controller class for Competition functions. 
 * <p>
 * Handles mappings of $PASTAUrl$/competition/...
 * <p>
 * Both students and teaching staff can access this url.
 * Students have highly limited functionality. 
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2013-08-15
 *
 */
@Controller
@RequestMapping("competition/")
public class AllCompetitionsController {

	protected final Log logger = LogFactory.getLog(getClass());

	private CompetitionManager competitionManager;

	@Autowired
	public void setMyService(CompetitionManager myService) {
		this.competitionManager = myService;
	}

	// ///////////////////////////////////////////////////////////////////////////
	// Models //
	// ///////////////////////////////////////////////////////////////////////////

	@ModelAttribute("newCompetitionModel")
	public NewCompetitionForm returnNewCompetitionModel() {
		return new NewCompetitionForm();
	}

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
	// COMPETITIONS //
	// ///////////////////////////////////////////////////////////////////////////

	/**
	 * $PASTAUrl$/competition/
	 * <p>
	 * The landing page for competitions.
	 * Lists all competitions and shows how the user can interact with them.
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * ATTRIBUTES:
	 * <table>
	 * 	<tr><td>unikey</td><td>The user object</td></tr>
	 * 	<tr><td>allCompetitions</td><td>The collection of all competitions</td></tr>
	 * </table>
	 * 
	 * JSP:<ul><li>assessment/viewAll/competition</li></ul>
	 * 
	 * @param model the model being used
	 * @return "redirect:/login/" or "assessment/viewAll/competition"
	 */
	@RequestMapping(value = "")
	public String viewAllCompetitions(Model model) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}

		model.addAttribute("unikey", user);
		model.addAttribute("allCompetitions", competitionManager.getCompetitionList());
		model.addAttribute("liveAssessmentCounts", competitionManager.getLiveAssessmentCounts(user));

		return "assessment/viewAll/competition";
	}

	/**
	 * $PASTAUrl$/competition/ - POST
	 * <p>
	 * Add a new competition
	 * 
	 * If the user has not authenticated: redirect to login.
	 * 
	 * If the user is not a tutor: redirect to home
	 * 
	 * If the user is an Instructor: add the competition using 
	 * {@link pasta.service.CompetitionManager#addCompetition(NewCompetitionForm)}
	 * 
	 * redirect to mirror
	 * 
	 * @param model the model being used
	 * @param form the new competition form
	 * @return "redirect:/login" or "redirect:/home/" or "redirect:/mirror/"
	 */
	@RequestMapping(value = "", method = RequestMethod.POST)
	public String newCompetition(Model model,
			@ModelAttribute(value = "newCompetitionModel") NewCompetitionForm form) {
		PASTAUser user = getUser();
		if (user == null) {
			return "redirect:/login/";
		}
		if (!user.isTutor()) {
			return "redirect:/home/";
		}
		if(user.isInstructor()){
			Competition newComp = competitionManager.addCompetition(form);
			if(newComp != null) {
				return "redirect:./" + newComp.getId() + "/";
			}
		}

		return "redirect:/mirror/";
	}
}