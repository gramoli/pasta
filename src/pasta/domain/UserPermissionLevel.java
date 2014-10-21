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

package pasta.domain;
/**
 * @author Alex Radu
 * @version 2.0
 * @since 2012-12-10
 * 
 * Different permission levels:
 * <p>
 * STUDENT
 * <ul>
 * 	<li>Allowed to submit assessment.</li>
 * 	<li>Must abide by any restrictions (e.g. time, number of submissions) set by the instructor.</li>
 * 	<li>Can view feedback details.</li>
 * </ul>
 * TUTOR
 * <ul>
 * 	<li>Can view all assessments or assessment components but cannot modify</li>
 * 	<li>Can submit to any assessment</li>
 * 	<li>Can submit for a student</li>
 * 	<li>Can bypass any restrictions set by the instructor</li>
 * 	<li>Can change their own tutorial classes.</li>
 * 	<li>Can change the tutorial allocation for students.</li>
 * 	<li>Can add/delete students from the system (adding/removing is loss-less in terms of assessment submissions)</li>
 * </ul>
 *  INSTRUCTOR
 * <ul>
 * 	<li>Can view and modify all assessments or assessment components</li>
 * 	<li>Can submit to any assessment</li>
 * 	<li>Can submit for a student</li>
 * 	<li>Can bypass any restrictions set by the instructor</li>
 * 	<li>Can change their own tutorial classes.</li>
 * 	<li>Can change the tutorial allocation for students.</li>
 * 	<li>Can add/delete students from the system (adding/removing is loss-less in terms of assessment submissions)</li>
 * </ul>
 */
public enum UserPermissionLevel {
	STUDENT, TUTOR, INSTRUCTOR;
}
