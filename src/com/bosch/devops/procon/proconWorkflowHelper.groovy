def jaCoCoDependencyForMaven() {
	def temp = libraryResource 'sonar/jaCoCoDependencyForMaven.txt'
	return temp
}

def  addJacocoDependyForMavenProject() {
	//def moduleMap = [:]
	//normaly, when the global pom.xml contains <modules> we have a multimodule project
	if(!readFile('pom.xml').contains("<modules>")) {
		echo 'Inside of Single Module project'
		buildFile = readFile("pom.xml")
		if (!buildFile.contains("<groupId>org.jacoco</groupId>")){
			if (buildFile.contains("<build>")) {
				buildFile = buildFile.replaceFirst("</plugins>", jaCoCoDependencyForMaven()+"\n</plugins>\n")
				writeFile file: "pom.xml", text: buildFile
			}
			else{
				buildFile = buildFile.replaceFirst("</project>", "<build>\n<plugins>\n "+jaCoCoDependencyForMaven()+"\n </plugins>\n </build> \n </project>")
				writeFile file: "pom.xml", text: buildFile
			}
		}
		echo "Plugin added...."
	}
	//like the method name says it: convert the map of all modules & modulepath to a map with sonarproperties
	//return transformModuleMapToSonarConfigMap(moduleMap)
}
