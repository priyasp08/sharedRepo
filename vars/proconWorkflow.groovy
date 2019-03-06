#!/usr/bin/env groovy
//Jenkins Proocn Flow

import hudson.model.*
import hudson.EnvVars
import groovy.json.JsonSlurperClassic
import groovy.json.JsonBuilder
import groovy.json.JsonOutput
import java.net.URL
import org.jenkinsci.plugins.gitclient.Git;
import org.jenkinsci.plugins.gitclient.GitClient;
import com.bosch.devops.procon.*

def call(body) {
	// evaluate the body block, and collect configuration into the object
	def config = [:]
	body.resolveStrategy = Closure.DELEGATE_FIRST
	body.delegate = config
	body()

  def proconWorkflowHelper = new proconWorkflowHelper()
try {
node {

 stage('preparation')
  {
  runSonarQubeAnalysis = (config.sonarqubeAnalysis!=null)?config.sonarqubeAnalysis:true
  echo("runSonarQubeAnalysis: ${runSonarQubeAnalysis}")
  branchName = "${env.BRANCH_NAME}"
  echo("branchName: ${branchName}")
  
 }
 
 stage('Checkout'){
  echo "Git Checkout"
  checkout scm
 }
 
 stage('Build'){
    def mvnHome = tool 'Maven-3.6'
    proconWorkflowHelper.addJacocoDependyForMavenProject()
   // def javahome = tool 'openjdk'
    sh("${mvnHome}/bin/mvn -B test -Dmaven.test.skip=true")
  }
  
  stage('SonarQube Analysis'){
 if (runSonarQubeAnalysis){
	if (branchName.startsWith("master") || branchName.startsWith("release") || branchName.startsWith("develop")){
	echo "Hi Sonar"
	withSonarQubeEnv('sonar-6'){
		def mvnHome = tool 'Maven-3.6'
    sh("${mvnHome}/bin/mvn test jacoco:prepare-agent jacoco:report")
		sh("${mvnHome}/bin/mvn checkstyle:checkstyle cobertura:cobertura sonar:sonar")
    checkstyle canComputeNew: false, defaultEncoding: '', healthy: '', pattern: '', unHealthy: ''
    cobertura autoUpdateHealth: false, autoUpdateStability: false, coberturaReportFile: ' **/target/site/cobertura/coverage.xml', conditionalCoverageTargets: '70, 0, 0', failUnhealthy: false, failUnstable: false, lineCoverageTargets: '80, 0, 0',
     maxNumberOfBuilds: 0, methodCoverageTargets: '80, 0, 0', onlyStable: false, sourceEncoding: 'ASCII', zoomCoverageChart: false
    //proconWorkflowHelper.addJacocoDependyForMavenProject()

		}
	  }
	}
  } 
 
} 
}
catch (exc) {
 print exc
 err = caughtError
 echo err
/* currentBuild.result = "FAILURE"
 String recipient = 'infra@lists.jenkins-ci.org'
 mail subject: "${env.JOB_NAME} (${env.BUILD_NUMBER}) failed",
         body: "It appears that ${env.BUILD_URL} is failing, somebody should do something about that",
           to: recipient,
      replyTo: recipient,
 from: 'noreply@ci.jenkins.io'
*/
} finally {
  
 (currentBuild.result != "ABORTED") && node("master") {
     // Send e-mail notifications for failed or unstable builds.
     // currentBuild.result must be non-null for this step to work.
     //step([$class: 'Mailer',
     //   notifyEveryUnstableBuild: true,
     //   recipients: "${email_to}",
     //   sendToIndividuals: true])
 }
}
}	
	
	