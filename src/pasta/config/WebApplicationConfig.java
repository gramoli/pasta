/*
MIT License

Copyright (c) 2012-2017 PASTA Contributors (see CONTRIBUTORS.txt)

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package pasta.config;

import java.io.IOException;
import java.util.Properties;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.orm.hibernate4.LocalSessionFactoryBean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import pasta.scheduler.AssessmentJobExecutor;
import pasta.service.PASTAOptions;

@Configuration
@EnableWebMvc
@EnableTransactionManagement
@EnableScheduling
@ComponentScan(basePackages={"pasta.repository", "pasta.login", "pasta.service", "pasta.scheduler", "pasta.domain", "pasta.util", "pasta.docker", "pasta.config.options"})
@ImportResource({"classpath:applicationContext-security.xml"})
@PropertySource("classpath:project.properties")
@PropertySource("classpath:database.properties")
public class WebApplicationConfig {

	protected static Logger logger = Logger.getLogger(WebApplicationConfig.class);
	
	@Autowired
	private Environment env;
	
	@Bean(name="projectSettings")
	public Properties createProjectSettings() {
		Properties props = new Properties();
		props.put("name", env.getProperty("project.name"));
		props.put("location", env.getProperty("project.location"));
		props.put("hostLocation", env.getProperty("project.hostLocation"));
		props.put("authentication", env.getProperty("project.authentication","ldap"));
		props.put("createAccountOnSuccessfulLogin", env.getProperty("project.createAccountOnSuccessfulLogin","true"));
		props.put("pathUnitTests", env.getProperty("project.pathUnitTests",""));
		props.put("pathSubmissions", env.getProperty("project.pathSubmissions",""));
		props.put("proxydomain", env.getProperty("project.proxydomain",""));
		props.put("proxyport", env.getProperty("project.proxyport",""));
		props.put("initialInstructor", env.getProperty("project.initialInstructor",""));
		return props;
	}
	
	@Bean(name="dataSource", destroyMethod="close")
	public BasicDataSource createBasicDataSource() {
		String jdbcDriverClassName = env.getProperty("jdbc.driverClassName");
		String jdbcUrl = env.getProperty("jdbc.url");
		String jdbcUsername = env.getProperty("jdbc.username");
		String jdbcPassword = env.getProperty("jdbc.password");
		
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName(jdbcDriverClassName);
		ds.setUrl(jdbcUrl);
		ds.setUsername(jdbcUsername);
		ds.setPassword(jdbcPassword);
		return ds;
	}
	
	@Bean(name="assessmentJobExecutor")
	@DependsOn("pastaOptions")
	public AssessmentJobExecutor createAssessmentJobExecutor() {
		String core = PASTAOptions.instance().get("execution.threads.core.size");
		int corePoolSize = 1;
		if(core != null) {
			try { corePoolSize = new Integer(core); } catch (NumberFormatException e){}
		}
		String max = PASTAOptions.instance().get("execution.threads.max.size");
		int maxPoolSize = 1;
		if(max != null) {
			try { maxPoolSize = new Integer(max); } catch (NumberFormatException e){}
		}
		return new AssessmentJobExecutor(corePoolSize, maxPoolSize);
	}
	
	@Autowired
	@Bean(name="sessionFactory")
	public LocalSessionFactoryBean createSessionFactory(BasicDataSource dataSource) {
		LocalSessionFactoryBean sf = new LocalSessionFactoryBean();
		sf.setDataSource(dataSource);
		
		Properties hibernateProperties = new Properties();
		hibernateProperties.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQLDialect");
		hibernateProperties.setProperty("hibernate.show_sql", "false");
		hibernateProperties.setProperty("hibernate.hbm2ddl.auto", "update");
		sf.setHibernateProperties(hibernateProperties);
		
		sf.setAnnotatedClasses(
				pasta.domain.BaseEntity.class,
				pasta.domain.PASTALoginUser.class,
				pasta.domain.options.Option.class,
				pasta.domain.ratings.AssessmentRating.class,
				pasta.domain.release.ClassRule.class,
				pasta.domain.release.DateRule.class,
				pasta.domain.release.HasSubmittedRule.class,
				pasta.domain.release.MarkCompareRule.class,
				pasta.domain.release.ReleaseAllResultsRule.class,
				pasta.domain.release.ReleaseAndRule.class,
				pasta.domain.release.ReleaseOrRule.class,
				pasta.domain.release.ReleaseResultsRule.class,
				pasta.domain.release.ReleaseRule.class,
				pasta.domain.release.StreamRule.class,
				pasta.domain.release.SubmissionCountRule.class,
				pasta.domain.release.UsernameRule.class,
				pasta.domain.reporting.Report.class,
				pasta.domain.reporting.ReportPermission.class,
				pasta.domain.result.AssessmentResult.class,
				pasta.domain.result.AssessmentResultSummary.class,
				pasta.domain.result.AssessmentResultSummary.AssessmentResultSummaryId.class,
				pasta.domain.result.HandMarkingResult.class,
				pasta.domain.result.UnitTestCaseResult.class,
				pasta.domain.result.UnitTestResult.class,
				pasta.domain.security.AuthenticationSettings.class,
				pasta.domain.template.Assessment.class,
				pasta.domain.template.AssessmentExtension.class,
				pasta.domain.template.BlackBoxTestCase.class,
				pasta.domain.template.BlackBoxOptions.class,
				pasta.domain.template.HandMarkData.class,
				pasta.domain.template.HandMarking.class,
				pasta.domain.template.UnitTest.class,
				pasta.domain.template.WeightedField.class,
				pasta.domain.template.WeightedHandMarking.class,
				pasta.domain.template.WeightedUnitTest.class,
				pasta.domain.user.PASTAGroup.class,
				pasta.domain.user.PASTAUser.class,
				pasta.scheduler.AssessmentJob.class,
				pasta.scheduler.Job.class
		);
		
		return sf;
	}
	
	@Autowired
	@Bean(name="transactionManager")
	public HibernateTransactionManager createHibernateTransactionManager(SessionFactory sessionFactory) {
		return new HibernateTransactionManager(sessionFactory);
	}
	
	@Bean(name="messageSource")
	public ResourceBundleMessageSource createResourceBundleMessageSource() {
		ResourceBundleMessageSource ms = new ResourceBundleMessageSource();
		ms.setBasename("messages");
		return ms;
	}
	
	@Bean
	public MethodInvokingFactoryBean createLogReloader() {
		MethodInvokingFactoryBean lr = new MethodInvokingFactoryBean();
		lr.setTargetClass(org.springframework.util.Log4jConfigurer.class);
		lr.setTargetMethod("initLogging");
		lr.setArguments(new Object[]{"classpath:log4j.properties", 30000L});
		return lr;
	}
	
	@Bean(name="defaultProperties")
	public Properties createDefaultProperties() {
		return getProperties("defaults.properties");
	}
	@Bean(name="languageProperties")
	public Properties createLanguageProperties() {
		return getProperties("languages.properties");
	}
	@Bean(name="programProperties")
	public Properties createProgramProperties() {
		return getProperties("programs.properties");
	}
	
	private Properties getProperties(String propertiesName) {
		try {
			return PropertiesLoaderUtils.loadProperties(new ClassPathResource(propertiesName));
		} catch (IOException e) {
			logger.error("Cannot load " + propertiesName, e);
			return null;
		}
	}
}
