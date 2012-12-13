package pasta.repository;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pasta.domain.template.Assessment;
import pasta.domain.template.Competition;
import pasta.domain.template.HandMarking;
import pasta.domain.template.Tuple;
import pasta.domain.template.UnitTest;
import pasta.domain.template.WeightedUnitTest;
import pasta.domain.upload.NewHandMarking;
import pasta.util.ProjectProperties;

public class AssessmentDAO {

	// assessmentTemplates are cached
	Map<String, Assessment> allAssessments;
	Map<String, UnitTest> allUnitTests;
	Map<String, HandMarking> allHandMarking;
	Map<String, Competition> allCompetitions;

	protected final Log logger = LogFactory.getLog(getClass());

	public AssessmentDAO() {
		// load up all cached objects

		// load up unit tests
		allUnitTests = new TreeMap<String, UnitTest>();
		loadUnitTests();

		// load up hand marking TODO #47
		allHandMarking = new TreeMap<String, HandMarking>();
		loadHandMarking();
		// load up competitions TODO #48

		// load up all assessments TODO #49
		allAssessments = new TreeMap<String, Assessment>();
		loadAssessments();
	}

	public Map<String, UnitTest> getAllUnitTests() {
		return allUnitTests;
	}

	public Assessment getAssessment(String name) {
		return allAssessments.get(name);
	}

	public HandMarking getHandMarking(String name) {
		return allHandMarking.get(name);
	}

	public Collection<HandMarking> getAllHandMarking() {
		return allHandMarking.values();
	}

	public Collection<Assessment> getAssessmentList() {
		return allAssessments.values();
	}

	public void addUnitTest(UnitTest newUnitTest) {
		allUnitTests.put(newUnitTest.getShortName(), newUnitTest);
	}

	public void addAssessment(Assessment newAssessment) {
		allAssessments.put(newAssessment.getShortName(), newAssessment);
	}

	public void removeUnitTest(String unitTestName) {
		allUnitTests.remove(unitTestName);
		try {
			FileUtils.deleteDirectory(new File(ProjectProperties.getInstance()
					.getProjectLocation()
					+ "/template/unitTest/"
					+ unitTestName));
		} catch (Exception e) {
			logger.error("Could not delete the folder for " + unitTestName
					+ "\r\n" + e);
		}
	}

	public void removeAssessment(String assessmentName) {
		allAssessments.remove(assessmentName);
		try {
			FileUtils.deleteDirectory(new File(ProjectProperties.getInstance()
					.getProjectLocation()
					+ "/template/assessment/"
					+ assessmentName));
		} catch (Exception e) {
			logger.error("Could not delete the folder for " + assessmentName
					+ "\r\n" + e);
		}
	}

	/**
	 * Load all unit tests.
	 */
	private void loadUnitTests() {
		// get unit test location
		String allTestLocation = ProjectProperties.getInstance()
				.getProjectLocation() + "/template/unitTest";
		String[] allUnitTestNames = (new File(allTestLocation)).list();
		Arrays.sort(allUnitTestNames);

		// load properties
		for (String name : allUnitTestNames) {
			UnitTest test = getUnitTestFromDisk(allTestLocation + "/" + name);
			if (test != null) {
				allUnitTests.put(name, test);
			}
		}

	}

	/**
	 * Load all assessments.
	 */
	private void loadAssessments() {
		// get unit test location
		String allTestLocation = ProjectProperties.getInstance()
				.getProjectLocation() + "/template/assessment";
		String[] allAssessmentNames = (new File(allTestLocation)).list();
		Arrays.sort(allAssessmentNames);

		// load properties
		for (String name : allAssessmentNames) {
			Assessment test = getAssessmentFromDisk(allTestLocation + "/"
					+ name);
			if (test != null) {
				allAssessments.put(name, test);
			}
		}

	}

	/**
	 * Load all handmarkings.
	 */
	private void loadHandMarking() {
		// get unit test location
		String allTestLocation = ProjectProperties.getInstance()
				.getProjectLocation() + "/template/handmarking";
		String[] allHandMarkingNames = (new File(allTestLocation)).list();
		Arrays.sort(allHandMarkingNames);

		// load properties
		for (String name : allHandMarkingNames) {
			HandMarking test = getHandMarkingFromDisk(allTestLocation + "/"
					+ name);
			if (test != null) {
				allHandMarking.put(test.getShortName(), test);
			}
		}
	}

	/**
	 * Method to get a unit test from a location
	 * 
	 * @param location
	 *            - the location of the unit test
	 * @return null - there is no unit test at that location to be retrieved
	 * @return test - the unit test at that location.
	 */
	private UnitTest getUnitTestFromDisk(String location) {
		try {

			File fXmlFile = new File(location + "/unitTestProperties.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder;
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();
			String name = doc.getElementsByTagName("name").item(0)
					.getChildNodes().item(0).getNodeValue();
			boolean tested = Boolean.parseBoolean(doc
					.getElementsByTagName("tested").item(0).getChildNodes()
					.item(0).getNodeValue());

			return new UnitTest(name, tested);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Method to get an assessment from a location
	 * 
	 * @param location
	 *            - the location of the assessment
	 * @return null - there is no assessment at that location to be retrieved
	 * @return test - the assessment at that location.
	 */
	private Assessment getAssessmentFromDisk(String location) {
		try {

			File fXmlFile = new File(location + "/assessmentProperties.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder;
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();

			Assessment currentAssessment = new Assessment();

			currentAssessment.setName(doc.getElementsByTagName("name").item(0)
					.getChildNodes().item(0).getNodeValue());
			currentAssessment.setMarks(Double.parseDouble(doc
					.getElementsByTagName("marks").item(0).getChildNodes()
					.item(0).getNodeValue()));
			currentAssessment.setReleased(Boolean.parseBoolean(doc
					.getElementsByTagName("released").item(0).getChildNodes()
					.item(0).getNodeValue()));
			currentAssessment.setNumSubmissionsAllowed(Integer.parseInt(doc
					.getElementsByTagName("submissionsAllowed").item(0)
					.getChildNodes().item(0).getNodeValue()));

			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm dd/MM/yyyy");
			currentAssessment.setDueDate(sdf.parse(doc
					.getElementsByTagName("dueDate").item(0).getChildNodes()
					.item(0).getNodeValue()));

			// load description from file
			String description = "";
			try {
				Scanner in = new Scanner(new File(location
						+ "/description.html"));
				while (in.hasNextLine()) {
					description += in.nextLine()
							+ System.getProperty("line.separator");
				}
				in.close();
			} catch (Exception e) {
				description = "<pre>Error loading description"
						+ System.getProperty("line.separator") + e + "</pre>";
			}
			currentAssessment.setDescription(description);

			NodeList unitTestList = doc.getElementsByTagName("unitTest");
			if (unitTestList != null && unitTestList.getLength() > 0) {
				for (int i = 0; i < unitTestList.getLength(); i++) {
					Node unitTestNode = unitTestList.item(i);
					if (unitTestNode.getNodeType() == Node.ELEMENT_NODE) {
						Element unitTestElement = (Element) unitTestNode;

						WeightedUnitTest weightedTest = new WeightedUnitTest();
						weightedTest.setTest(allUnitTests.get(unitTestElement
								.getAttribute("name")));
						weightedTest.setWeight(Double
								.parseDouble(unitTestElement
										.getAttribute("weight")));
						if (unitTestElement.getAttribute("secret") != null
								&& Boolean.parseBoolean(unitTestElement
										.getAttribute("secret"))) {
							currentAssessment.addSecretUnitTest(weightedTest);
						} else {
							currentAssessment.addUnitTest(weightedTest);
						}
					}
				}
			}

			// TODO add hand marking

			// TODO add competitions

			return currentAssessment;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Method to get a handmarking from a location
	 * 
	 * @param location
	 *            - the location of the handmarking
	 * @return null - there is no handmarking at that location to be retrieved
	 * @return test - the handmarking at that location.
	 */
	private HandMarking getHandMarkingFromDisk(String location) {
		try {

			File fXmlFile = new File(location + "/handMarkingProperties.xml");
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder;
			dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();

			HandMarking markingTemplate = new HandMarking();

			// load name
			markingTemplate.setName(doc.getElementsByTagName("name").item(0)
					.getChildNodes().item(0).getNodeValue());

			// load column list
			NodeList columnList = doc.getElementsByTagName("column");
			List<Tuple> columnHeaderList = new ArrayList<Tuple>();
			if (columnList != null && columnList.getLength() > 0) {
				for (int i = 0; i < columnList.getLength(); i++) {
					Node columnNode = columnList.item(i);
					if (columnNode.getNodeType() == Node.ELEMENT_NODE) {
						Element columnElement = (Element) columnNode;

						Tuple tuple = new Tuple();
						tuple.setName(columnElement.getAttribute("name"));
						tuple.setWeight(Double.parseDouble(columnElement
								.getAttribute("weight")));

						columnHeaderList.add(tuple);
					}
				}
			}
			markingTemplate.setColumnHeader(columnHeaderList);

			// load row list
			NodeList rowList = doc.getElementsByTagName("row");
			List<Tuple> rowHeaderList = new ArrayList<Tuple>();
			if (rowList != null && rowList.getLength() > 0) {
				for (int i = 0; i < rowList.getLength(); i++) {
					Node rowNode = rowList.item(i);
					if (rowNode.getNodeType() == Node.ELEMENT_NODE) {
						Element rowElement = (Element) rowNode;

						Tuple tuple = new Tuple();
						tuple.setName(rowElement.getAttribute("name"));
						tuple.setWeight(Double.parseDouble(rowElement
								.getAttribute("weight")));

						rowHeaderList.add(tuple);
					}
				}
			}
			markingTemplate.setRowHeader(rowHeaderList);

			// load data
			HashMap<String, HashMap<String, String>> descriptionMap = new HashMap<String, HashMap<String, String>>();
			for (Tuple column : markingTemplate.getColumnHeader()) {
				HashMap<String, String> currDescriptionMap = new HashMap<String, String>();
				for (Tuple row : markingTemplate.getRowHeader()) {
					try {
						Scanner in = new Scanner(new File(location + "/"
								+ column.getName().replace(" ", "") + "-"
								+ row.getName().replace(" ", "") + ".txt"));
						String description = "";
						while (in.hasNextLine()) {
							description += in.nextLine()
									+ System.getProperty("line.separator");
						}
						currDescriptionMap.put(row.getName(), description);
					} catch (Exception e) {
						// do nothing
					}
				}
				descriptionMap.put(column.getName(), currDescriptionMap);
			}

			markingTemplate.setData(descriptionMap);

			return markingTemplate;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void updateHandMarking(HandMarking newHandMarking) {
		allHandMarking.put(newHandMarking.getShortName(), newHandMarking);
		// save to drive

		String location = ProjectProperties.getInstance().getProjectLocation()
				+ "/template/handmarking/" + newHandMarking.getShortName();

		try {
			FileUtils.deleteDirectory(new File(location));
		} catch (IOException e) {
			// Don't care if it doesn't exist
		}

		// make the folder
		(new File(location)).mkdirs();

		try {
			PrintWriter handMarkingProperties = new PrintWriter(new File(
					location + "/handMarkingProperties.xml"));
			handMarkingProperties.println("<handMarkingProperties>");
			// name
			handMarkingProperties.println("\t<name>" + newHandMarking.getName()
					+ "</name>");
			// columns
			if (!newHandMarking.getColumnHeader().isEmpty()) {
				handMarkingProperties.println("\t<columns>");
				for (Tuple column : newHandMarking.getColumnHeader()) {
					handMarkingProperties.println("\t\t<column name=\""
							+ column.getName() + "\" weight=\""
							+ column.getWeight() + "\"/>");
				}
				handMarkingProperties.println("\t</columns>");
			}

			// rows
			if (!newHandMarking.getRowHeader().isEmpty()) {
				handMarkingProperties.println("\t<rows>");
				for (Tuple row : newHandMarking.getRowHeader()) {
					handMarkingProperties.println("\t\t<row name=\""
							+ row.getName() + "\" weight=\"" + row.getWeight()
							+ "\"/>");
				}
				handMarkingProperties.println("\t</rows>");
			}
			handMarkingProperties.println("</handMarkingProperties>");
			handMarkingProperties.close();

			for (Entry<String, HashMap<String, String>> entry1 : newHandMarking
					.getData().entrySet()) {
				for (Entry<String, String> entry2 : entry1.getValue()
						.entrySet()) {
					PrintWriter dataOut = new PrintWriter(new File(location
							+ "/" + entry1.getKey().replace(" ", "") + "-"
							+ entry2.getKey().replace(" ", "") + ".txt"));
					
					dataOut.println(entry2.getValue());
					dataOut.close();
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void newHandMarking(NewHandMarking newHandMarking) {

		HandMarking newMarking = new HandMarking();
		newMarking.setName(newHandMarking.getName());

		ArrayList<Tuple> columns = new ArrayList<Tuple>();
		columns.add(new Tuple("Poor", 0));
		columns.add(new Tuple("Acceptable", 0.5));
		columns.add(new Tuple("Excelent", 1));
		newMarking.setColumnHeader(columns);

		ArrayList<Tuple> rows = new ArrayList<Tuple>();
		rows.add(new Tuple("Formatting", 0.2));
		rows.add(new Tuple("Code Reuse", 0.4));
		rows.add(new Tuple("Variable naming", 0.4));
		newMarking.setRowHeader(rows);

		HashMap<String, HashMap<String, String>> data = new HashMap<String, HashMap<String, String>>();
		for (Tuple column : columns) {
			HashMap<String, String> currData = new HashMap<String, String>();
			for (Tuple row : rows) {
				currData.put(row.getName(), "");
			}
			data.put(column.getName(), currData);
		}
		newMarking.setData(data);

		updateHandMarking(newMarking);
	}
}
