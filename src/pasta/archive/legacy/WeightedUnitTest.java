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

package pasta.archive.legacy;

/**
 * Container class for a weighted unit test.
 * <p>
 * This class contains a link to a unit test assessment module to an assessment
 * and the weight of the unit test assessment module in the assessment.
 * 
 * Using this, you can have the same unit test assessment module as part of multiple
 * assessments with different weights.
 * 
 * @author Alex Radu
 * @version 2.0
 * @since 2012-11-14
 *
 */
public class WeightedUnitTest {
	private UnitTest test = new UnitTest();
	private double weight;
	private String unitTestName;
	
	public UnitTest getTest() {
		return test;
	}
	public void setTest(UnitTest test) {
		this.test = test;
		if(!test.getName().trim().equals("nullgarbagetemptestihopenobodynamestheirtestthis")){
			unitTestName = test.getName();
		}
	}
	public double getWeight() {
		return weight;
	}
	public void setWeight(double weight) {
		this.weight = weight;
	}
	public String getUnitTestName() {
		return unitTestName;
	}
	public void setUnitTestName(String unitTestName) {
		this.unitTestName = unitTestName;
	}
	
}