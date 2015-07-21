package pasta.service;

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import pasta.domain.result.AssessmentResult;
import pasta.domain.result.CombinedAssessmentResult;
import pasta.domain.user.PASTAUser;
import pasta.repository.ResultDAO;
import pasta.util.PASTAUtil;

/**
 * @author Joshua Stretton
 * @version 1.0
 * @since 16 Jul 2015
 */
@Service("resultManager")
@Repository
public class ResultManager {
	private static Logger logger = Logger.getLogger(ResultManager.class);
	
	@Autowired
	private ApplicationContext context;
	@Autowired
	private ResultDAO resultDAO;
	
	@Autowired
	private GroupManager groupManager;
	
	public Collection<AssessmentResult> getAllResultsForUserAssessment(PASTAUser user, long assessmentId) {
		return resultDAO.getAllResults(user, assessmentId, true, false);
	}
	
	/**
	 * Gets all assessmentResults from this user and their group - latest first.
	 * @param user the user
	 * @param assessmentId the id of the assessment
	 * @return the collection of submission history for a user for a given assessment
	 */
	public List<AssessmentResult> getAssessmentHistory(PASTAUser user, long assessmentId){
		return resultDAO.getAllResults(user, assessmentId, true, true);
	}

	public AssessmentResult getAssessmentResult(long id) {
		return resultDAO.getAssessmentResult(id);
	}

	public AssessmentResult getAssessmentResult(PASTAUser user, long assessmentId, Date submissionDate) {
		return resultDAO.getResult(user, assessmentId, submissionDate, false);
	}
	public AssessmentResult getAssessmentResultIncludingGroup(PASTAUser user, long assessmentId, Date submissionDate) {
		return resultDAO.getResult(user, assessmentId, submissionDate, true);
	}
	
	public AssessmentResult getLatestAssessmentResult(PASTAUser user, long assessmentId) {
		return resultDAO.getLatestIndividualResult(user, assessmentId);
	}
	
	public AssessmentResult getLatestResultIncludingGroup(PASTAUser user, long assessmentId) {
		AssessmentResult indResult = getLatestAssessmentResult(user, assessmentId);
		AssessmentResult grpResult = resultDAO.getLatestGroupResult(user, assessmentId);
		if(grpResult == null) {
			return indResult;
		}
		return new CombinedAssessmentResult(user, indResult, grpResult);
	}
	
	/**
	 * Get the latest result for the collection of users.
	 * <p>
	 * Gets all of the cached assessment results for every assessment on the system, for the
	 * collection of users given.
	 * 
	 * @param allUsers the collection of {@link pasta.domain.user.PASTAUser} that are being queried
	 * @return the map (Long userId , Long assessmentId, {@link pasta.domain.result.AssessmentResult} assessmentResults) 
	 */
	public Map<PASTAUser, Map<Long, AssessmentResult>> getLatestResults(Collection<PASTAUser> allUsers){
		Map<PASTAUser, Map<Long, AssessmentResult>> results = new TreeMap<>();
		
		for(PASTAUser user: allUsers){
			Map<Long, AssessmentResult> currResultMap = getLatestResults(user);
			results.put(user, currResultMap);
		}
		
		return results;
	}
	
	/**
	 * Get the latest result for the collection of users, including their marks from the group.
	 * 
	 * @param allUsers the collection of {@link pasta.domain.user.PASTAUser} that are being queried
	 * @return the map (Long userId , Long assessmentId, {@link pasta.domain.result.CombinedAssessmentResult} assessmentResults) 
	 */
	public Map<PASTAUser, Map<Long, CombinedAssessmentResult>> getLatestResultsIncludingGroup(Collection<PASTAUser> allUsers){
		Map<PASTAUser, Map<Long, CombinedAssessmentResult>> results = new TreeMap<>();
		
		for(PASTAUser user: allUsers){
			Map<Long, CombinedAssessmentResult> currResultMap = getLatestResultsIncludingGroups(user);
			results.put(user, currResultMap);
		}
		
		return results;
	}
	
	/**
	 * Get the latest result for a user.
	 * 
	 * @param user the user
	 * @return the map which holds the assessment results with a key which is the assessment id
	 */
	public Map<Long, AssessmentResult> getLatestResults(PASTAUser user){
		Map<Long, AssessmentResult> results = new HashMap<Long, AssessmentResult>();
		
		List<AssessmentResult> allResults = resultDAO.getAllResults(user, -1, true, false);
		
		for(AssessmentResult result : allResults) {
			if(results.containsKey(result.getAssessment().getId())) {
				continue;
			}
			results.put(result.getAssessment().getId(), result);
		}
		
		return results;
	}
	
	/**
	 * <p>Get the latest result for a user and any groups they are in.
	 * 
	 * <p>This method is intended as a way to summarise results, and so 
	 * the returned objects will only contain the assessment and the 
	 * specific results.
	 * 
	 * <p>Do not use this method if you need details such as user or submission date.
	 * 
	 * @param user the user to get a summary for
	 * @return the map which holds the assessment results with a key which is the assessment id
	 */
	public Map<Long, CombinedAssessmentResult> getLatestResultsIncludingGroups(PASTAUser user){
		Map<Long, CombinedAssessmentResult> results = new HashMap<Long, CombinedAssessmentResult>();
		
		List<AssessmentResult> allResults = resultDAO.getAllResults(user, -1, true, true);
		
		HashSet<Long> addedGroupResults = new HashSet<Long>();
		HashSet<Long> addedUserResults = new HashSet<Long>();
		
		for(AssessmentResult result : allResults) {
			boolean isGroup = result.getUser().isGroup();
			long assessmentId = result.getAssessment().getId();
			
			if((isGroup ? addedGroupResults : addedUserResults).contains(assessmentId)) {
				continue;
			}
			
			(isGroup ? addedGroupResults : addedUserResults).add(assessmentId);
			
			CombinedAssessmentResult summaryResult = results.get(assessmentId);
			if(summaryResult == null) {
				summaryResult = new CombinedAssessmentResult(user, result);
				results.put(assessmentId, summaryResult);
			} else {
				summaryResult.addResult(result);
			}
		}
		
		return results;
	}
	
	/**
	 * Gets an assessment result given a user, assessment and formatted submission date.
	 * 
	 * @param user the user
	 * @param assessmentId the id of the assessment 
	 * @param assessmentDate the date (formatted "yyyy-MM-dd'T'hh-mm-ss")
	 * @return the queried assessment result or null if not available.
	 */
	public AssessmentResult loadAssessmentResult(PASTAUser user, long assessmentId,
			String assessmentDate) {
		AssessmentResult result;
		try {
			result = getAssessmentResultIncludingGroup(user, assessmentId, PASTAUtil.parseDate(assessmentDate));
		} catch (ParseException e) {
			logger.error("Error parsing date", e);
			return null;
		}
		
		return result;
	}

	public void updateAssessmentResults(AssessmentResult result) {
		resultDAO.update(result);
	}

	public void updateComment(long resultId, String newComment) {
		AssessmentResult result = getAssessmentResult(resultId);
		result.setComments(newComment);
		resultDAO.update(result);
	}

	public void save(AssessmentResult result) {
		resultDAO.save(result);
	}

	public void update(AssessmentResult result) {
		resultDAO.update(result);
	}
}
