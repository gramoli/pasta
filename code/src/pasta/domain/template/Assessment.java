package pasta.domain.template;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.FactoryUtils;
import org.apache.commons.collections.list.LazyList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Assessment {
	private List<WeightedUnitTest> unitTests = LazyList.decorate(new ArrayList<WeightedUnitTest>(),
			FactoryUtils.instantiateFactory(WeightedUnitTest.class));
	private List<WeightedUnitTest> secretUnitTests = LazyList.decorate(new ArrayList<WeightedUnitTest>(),
			FactoryUtils.instantiateFactory(WeightedUnitTest.class));
	private List<WeightedHandMarking> handMarking = LazyList.decorate(new ArrayList<WeightedHandMarking>(),
			FactoryUtils.instantiateFactory(WeightedHandMarking.class));
	private String name;
	private double marks;
	private Date dueDate = new Date();
	private String description;
	private int numSubmissionsAllowed;
	private boolean released = false;

	protected final Log logger = LogFactory.getLog(getClass());

	public void addUnitTest(WeightedUnitTest test) {
		unitTests.add(test);
	}

	public void removeUnitTest(WeightedUnitTest test) {
		unitTests.remove(test);
	}

	public boolean isReleased() {
		return released;
	}

	public void setReleased(boolean released) {
		this.released = released;
	}

	public void addSecretUnitTest(WeightedUnitTest test) {
		secretUnitTests.add(test);
	}

	public void removeSecretUnitTest(WeightedUnitTest test) {
		secretUnitTests.remove(test);
	}
	
	public void addHandMarking(WeightedHandMarking test) {
		handMarking.add(test);
	}

	public void removeHandMarking(WeightedHandMarking test) {
		handMarking.remove(test);
	}

	public double getMarks() {
		return marks;
	}

	public List<WeightedUnitTest> getUnitTests() {
		return unitTests;
	}

	public void setUnitTests(ArrayList<WeightedUnitTest> unitTests) {
		this.unitTests.clear();
		this.unitTests.addAll(unitTests);
	}

	public List<WeightedUnitTest> getSecretUnitTests() {
		return secretUnitTests;
	}

	public void setSecretUnitTests(ArrayList<WeightedUnitTest> secretUnitTests) {
		this.secretUnitTests.clear();
		this.secretUnitTests.addAll(secretUnitTests);
	}
	
	public List<WeightedHandMarking> getHandMarking() {
		return handMarking;
	}

	public void setHandMarking(ArrayList<WeightedHandMarking> handMarking) {
		this.handMarking.clear();
		this.handMarking.addAll(handMarking);
	}

	public String getName() {
		return name;
	}

	public String getShortName() {
		return name.replace(" ", "");
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setMarks(double marks) {
		this.marks = marks;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public String getSimpleDueDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		return sdf.format(dueDate);
	}

	public void setSimpleDueDate(String date) {
		logger.info(date);
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
			dueDate = sdf.parse(date.trim());
		} catch (ParseException e) {
			logger.error("Could not parse date " + e);
		}
		logger.info(dueDate);
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public int getNumSubmissionsAllowed() {
		return numSubmissionsAllowed;
	}

	public void setNumSubmissionsAllowed(int numSubmissionsAllowed) {
		this.numSubmissionsAllowed = numSubmissionsAllowed;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean isCompletelyTested() {
		for (WeightedUnitTest test : unitTests) {
			if (!test.getTest().isTested()) {
				return false;
			}
		}

		for (WeightedUnitTest test : secretUnitTests) {
			if (!test.getTest().isTested()) {
				return false;
			}
		}
		return true;
	}

	public boolean isClosed() {
		return (new Date()).after(getDueDate());
	}  
	
	public void setGarbage(ArrayList<WeightedUnitTest> unitTests) {
	}

	public List<WeightedUnitTest> getGarbage() {
		return LazyList.decorate(new ArrayList<WeightedUnitTest>(),
				FactoryUtils.instantiateFactory(WeightedUnitTest.class));
	}
	
	public void setHandGarbage(ArrayList<WeightedHandMarking> unitTests) {
	}

	public List<WeightedHandMarking> getHandGarbage() {
		return LazyList.decorate(new ArrayList<WeightedHandMarking>(),
				FactoryUtils.instantiateFactory(WeightedHandMarking.class));
	}

	public String toString() {
		String output = "";
		output += "<assessment>" + System.getProperty("line.separator");
		output += "\t<name>" + getName() + "</name>" + System.getProperty("line.separator");
		output += "\t<released>" + isReleased() + "</released>" + System.getProperty("line.separator");
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/YYYY");
		output += "\t<dueDate>" + sdf.format(getDueDate()) + "</dueDate>" + System.getProperty("line.separator");
		output += "\t<marks>" + getMarks() + "</marks>" + System.getProperty("line.separator");
		output += "\t<submissionsAllowed>" + getNumSubmissionsAllowed() + "</submissionsAllowed>"
				+ System.getProperty("line.separator");
		if (unitTests.size() + secretUnitTests.size() > 0) {
			output += "\t<unitTestSuite>" + System.getProperty("line.separator");
			for (WeightedUnitTest unitTest : unitTests) {
				output += "\t\t<unitTest name=\"" + unitTest.getTest().getShortName() + "\" weight=\""
						+ unitTest.getWeight() + "\"/>" + System.getProperty("line.separator");
			}

			for (WeightedUnitTest unitTest : secretUnitTests) {
				output += "\t\t<unitTest name=\"" + unitTest.getTest().getShortName() + "\" weight=\""
						+ unitTest.getWeight() + "\" secret=\"true\" />" + System.getProperty("line.separator");
			}
			output += "\t</unitTestSuite>" + System.getProperty("line.separator");
		}
		// handMarks
		if (handMarking.size() > 0) {
			output += "\t<handMarkingSuite>" + System.getProperty("line.separator");
			for (WeightedHandMarking handMarks : handMarking) {
				output += "\t\t<handMarks name=\"" + handMarks.getHandMarking().getShortName() + "\" weight=\""
						+ handMarks.getWeight() + "\"/>" + System.getProperty("line.separator");
			}
			output += "\t</handMarkingSuite>" + System.getProperty("line.separator");
		}
		// TODO all competitions
		output += "</assessment>" + System.getProperty("line.separator");
		return output;
	}
	
	public double getWeighting(UnitTest test){
		for(WeightedUnitTest myTest: unitTests){
			if(test == myTest.getTest()){
				return myTest.getWeight();
			}
		}
		for(WeightedUnitTest myTest: secretUnitTests){
			if(test == myTest.getTest()){
				return myTest.getWeight();
			}
		}
		return 0;
	}
}