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

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import pasta.domain.UserPermissionLevel;
import pasta.domain.form.ChangePasswordForm;
import pasta.domain.form.UpdateUsersForm;
import pasta.domain.form.validate.UpdateUsersFormValidator;
import pasta.domain.user.PASTAUser;
import pasta.login.DBAuthValidator;
import pasta.service.ExecutionManager;
import pasta.service.UserManager;
import pasta.util.ProjectProperties;
import pasta.web.WebUtils;

/**
 * Controller class for admin functions. 
 * <p>
 * Handles mappings of $PASTAUrl$/admin/...
 * <p>
 * Both students and teaching staff can access this url.
 * Students have highly limited functionality. If the authentication
 * system is not using the database, then it's an empty page.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2014-01-23
 *
 */
@Controller
@RequestMapping("admin/")
public class AdminController {


	protected final Log logger = LogFactory.getLog(getClass());
	
	@Autowired
	private UserManager userManager;
	
	@Autowired
	private ExecutionManager executionManager;
	
	@Autowired
	private UpdateUsersFormValidator updateValidator;

	// ///////////////////////////////////////////////////////////////////////////
	// Models //
	// ///////////////////////////////////////////////////////////////////////////

	/**
	 * Return the empty form for changing your password (DB authentication only)
	 * 
	 * @return the correct form.
	 */
	@ModelAttribute("changePasswordForm")
	public ChangePasswordForm returnNewUnitTestModel() {
		return new ChangePasswordForm();
	}
	
	@ModelAttribute("updateUsersForm")
	public UpdateUsersForm returnUpdateUsersForm() {
		return new UpdateUsersForm();
	}
	
	@ModelAttribute("user")
	public PASTAUser loadUser(HttpServletRequest request) {
		WebUtils.ensureLoggedIn(request);
		return WebUtils.getUser();
	}

	// ///////////////////////////////////////////////////////////////////////////
	// ADMIN //
	// ///////////////////////////////////////////////////////////////////////////

	/**
	 * $PASTAUrl$/admin/
	 * <p>
	 * Ensure user exists and has authenticated against the system.
	 * If they have not, redirect them to the login screen.
	 * <p>
	 * Attributes:
	 * <table>
	 * 	<tr><td>authType</td><td>ProjectProperties.getInstance().getAuthenticationValidator().getClass().getName()</td></tr>
	 * 	<tr><td>people</td><td><b>Only if tutor or instructor!</b> - the list of users (both students and teaching staff)</td></tr>
	 * 	<tr><td>addresses</td><td><b>Only if tutor or instructor!</b> - the list of sever addresses for which to authenticate against</td></tr>
	 * </table>
	 * JSP:
	 * <ul>
	 * 	<li>user/admin</li>
	 * </ul>
	 * 
	 * @param model the model
	 * @return "redirect:/login" or "user/admin".
	 */
	@RequestMapping(value = "", method = RequestMethod.GET)
	public String viewAdmin(@ModelAttribute("user") PASTAUser user, ModelMap model) {
		model.addAttribute("authType", ProjectProperties.getInstance().getAuthenticationValidator().getClass().getName());
		if(user.isTutor()){
			model.addAttribute("people", userManager.getUserList());
			model.addAttribute("addresses", ProjectProperties.getInstance().getAuthenticationSettings().getServerAddresses());
			model.addAttribute("taskDetails", executionManager.getExecutingTaskDetails());
		}
		return "user/admin";
	}
	
	/**
	 * $PASTAUrl$/admin/changePassword/
	 * <p>
	 * Change the password of a user.
	 * <p>
	 * Ensure user exists and has authenticated against the system.
	 * If they have not, redirect them to the referring url.
	 * 
	 * @param model the model
	 * @param request the http request object, used to send the user back to the page that referred them
	 * @param form the change password form
	 * @return redirect to the referrer url
	 */
	@RequestMapping(value = "/changePassword/", method = RequestMethod.POST)
	public String changePassword(@ModelAttribute("user") PASTAUser user, ModelMap model, HttpServletRequest request,
			@ModelAttribute(value = "changePasswordForm") ChangePasswordForm form) {
				
		Validator val = ProjectProperties.getInstance().getAuthenticationValidator();
		if(val instanceof DBAuthValidator){
			DBAuthValidator authenticator = (DBAuthValidator)val;
			if(authenticator.authenticate(user.getUsername(), form.getOldPassword())
					&& form.getNewPassword().equals(form.getConfirmPassword())){
				logger.info("swapping password");
				userManager.updatePassword(user.getUsername(), form.getNewPassword());
			}
			else{
				logger.info("an error occured");
			}
			
		}
		
		return "redirect:" + request.getHeader("Referer");
	}
	
	/**
	 * $PASTAUrl$/admin/updateUsers/ - POST
	 * <p>
	 * Updates the list of either students or tutors/instructors. This may be an update or replace operation.
	 * <p>
	 * Ensure user exists and has authenticated against the system.
	 * If they have not, redirect them to the referring url.
	 * <p>
	 * <b>REQUIRES: Tutor or higher permission</b>
	 * 
	 * @param model the model
	 * @param request the http request object, used to send the user back to the page that referred them
	 * @return redirect to the referrer url
	 */
	@RequestMapping(value = "/updateUsers/", method = RequestMethod.POST)
	public String updateUsers(
			@Valid @ModelAttribute("updateUsersForm") UpdateUsersForm form, BindingResult result, 
			RedirectAttributes attr, Model model, HttpServletRequest request) {
		if(form.isUpdateTutors()) {
			WebUtils.ensureAccess(UserPermissionLevel.INSTRUCTOR);
		} else {
			WebUtils.ensureAccess(UserPermissionLevel.TUTOR);
		}
		
		updateValidator.validate(form, result);
		if(result.hasErrors()) { 
			attr.addFlashAttribute("updateUsersForm", form);
			attr.addFlashAttribute("org.springframework.validation.BindingResult.updateUsersForm", result);
			return "redirect:../.";
		}
		
		userManager.updateUsers(form);
		
		return "redirect:" + request.getHeader("Referer");
	}
	
	/**
	 * $PASTAUrl$/admin/delete/{deleteUsername}/
	 * <p>
	 * Delete a user from the system.
	 * <p>
	 * Ensure user exists and has authenticated against the system.
	 * If they have not, redirect them to the referring url.
	 * <p>
	 * <b>REQUIRES: Tutor or higher permission</b>
	 * 
	 * @param model the model
	 * @param request the http request object, used to send the user back to the page that referred them
	 * @param deleteUsername the name of the user
	 * @return redirect to the referrer url
	 */
	@RequestMapping(value = "/delete/{deleteUsername}/")
	public String deleteUser(ModelMap model, HttpServletRequest request,
			@PathVariable("deleteUsername") String username) {
				
		PASTAUser toDelete = userManager.getUser(username);
		if(toDelete.isTutor()) {
			WebUtils.ensureAccess(UserPermissionLevel.INSTRUCTOR);
		} else {
			WebUtils.ensureAccess(UserPermissionLevel.TUTOR);
		}
	
		userManager.deleteUser(toDelete);
		return "redirect:" + request.getHeader("Referer");
	}
	
	/**
	 * $PASTAUrl$/admin/auth/
	 * <p>
	 * Change the authentication system and/or addresses.
	 * <p>
	 * Ensure user exists and has authenticated against the system.
	 * If they have not, redirect them to the referring url.
	 * <p>
	 * <b>REQUIRES: Tutor or higher permission</b>
	 * 
	 * @param model the model
	 * @param request the http request object, used to send the user back to the page that referred them
	 * @param type the authentication type
	 * @param address the addresses which will be used by the authentication
	 * @return redirect to the referrer url
	 */
	@RequestMapping(value = "/auth/", method = RequestMethod.GET)
	public String changeAuthType(ModelMap model, HttpServletRequest request,
			@RequestParam(value="type") String type,
			@RequestParam(value="address") String[] address) {
		WebUtils.ensureAccess(UserPermissionLevel.INSTRUCTOR);
	
		ProjectProperties.getInstance().changeAuthMethod(type, address);
		return "redirect:" + request.getHeader("Referer");
	}
	
	@RequestMapping(value = "/forceSubmissionRefresh/", method = RequestMethod.POST)
	public String forceSubmissionRefresh(HttpServletRequest request) {
		WebUtils.ensureAccess(UserPermissionLevel.INSTRUCTOR);
		executionManager.forceSubmissionRefresh();
		return "redirect:" + request.getHeader("Referer");
	}
}
