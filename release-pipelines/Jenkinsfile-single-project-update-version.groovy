properties([
        parameters([
                string(name: 'repo_name_param', defaultValue: 'jaxb-v2', description: 'Github Repository'),
                string(name: 'branch_param', defaultValue: '', description: 'Repository From Branch'),
                string(name: 'new_version_param', defaultValue: '', description: ''),
                string(name: 'custom_pom_path_param', defaultValue: "jaxb-ri/pom.xml", description: 'In case this option is populated this path is going to be used in place of the normal root project path'),
                choice(name: 'slack_channel', choices: getDefaultChoiceSlackChannelsList().join("\n"), description: 'Slack channel to send the job notifications'),
                string(name: 'pipeline_branch', defaultValue: '4.x', description: 'mule-runtime-release repo branch where the Jenkins file is going to be use'),
        ]),
        buildDiscarder(logRotator(artifactDaysToKeepStr: '14', artifactNumToKeepStr: '3', daysToKeepStr: '60', numToKeepStr: '')),
        [$class: 'GithubProjectProperty', displayName: '', projectUrlStr: 'https://github.com/mulesoft/mule-runtime-release/'],
])

node('hi-speed||ubuntu-14.04||ubuntu-18.04') {

    try {

    //    def auto_increment_version_arg = auto_increment_version_param
        def new_version_arg = new_version_param

        String custom_pom_path_arg = (custom_pom_path_param.isEmpty()) ? '-f pom.xml' : "-f ${custom_pom_path_param}"


        stage('Prepare workspace') { // for display purposes
            deleteDir()

            installJdk()
            installMaven()
        }

        stage("Clone Repo") {
            sh "git clone --branch ${branch_param} git@github.com:mulesoft/${repo_name_param}.git"
        }

    //    if(auto_increment_version_arg){
    //        new_version_arg = getUpdatedVersion(versionTypeToUpdate, )
    //    }

        String customPomPathDisplayName = (custom_pom_path_param.isEmpty()) ? "" : " - path: '${custom_pom_path_arg.minus("-f ")}'"
        currentBuild.displayName = "${env.BUILD_ID}: ${repo_name_param} - version ${new_version_arg} in branch ${branch_param} ${customPomPathDisplayName}"

        stage('Update Version') {
            dir("${repo_name_param}"){
                mvn("versions:set -DgenerateBackupPoms=false -DnewVersion=${new_version_arg} ${custom_pom_path_arg}")
            }
        }

        stage('Update Version Parent bom') {
            dir("${repo_name_param}"){
                String custom_pom_path_bom = "-f jaxb-ri/boms/bom"
                mvn("versions:set -DgenerateBackupPoms=false -DnewVersion=${new_version_arg} ${custom_pom_path_bom}")
            }
        }

        stage('Update parents versions jaxb-ri and bom-ext') {
            dir("${repo_name_param}"){
                sh "perl -0777 -i -pe 's/(<parent>.*<version)(.*)(\\/version>.*<\\/parent>)/\${1}>${new_version_arg}<\${3}/s' jaxb-ri/pom.xml "
                sh "perl -0777 -i -pe 's/(<parent>.*<version)(.*)(\\/version>.*<\\/parent>)/\${1}>${new_version_arg}<\${3}/s' jaxb-ri/boms/bom-ext/pom.xml "
            }
        }

        stage('Commit and push changes') {
            dir("${repo_name_param}") {
                setGlobalGitUserNameAndEmail()
                String customPomPathGitMessage = (custom_pom_path_arg.isEmpty()) ? "" : "in folder: '${custom_pom_path_arg.minus("-f ").minus("/pom.xml")}'"

                sh "git commit -a -m 'Update version to ${new_version_arg} ${customPomPathGitMessage}'"
                sh "git push origin refs/heads/${branch_param}:refs/heads/${branch_param}"
            }
        }
    } catch (e) {
        currentBuild.result = 'FAILURE'
        throw e
    } finally {

        stage('Clean workspace') {
            deleteDir()
        }

        notifySlack(currentBuild.result, slack_channel)
    }
}

//def getUpdatedVersion(String versionTypeToUpdate, String currentVersion){
//
//    def split = currentVersion.split('\\.')
//    switch (versionTypeToUpdate){
//        case "major":
//            split[0]=++Integer.parseInt(split[0])
//            break
//        case "minor":
//            split[1]=++Integer.parseInt(split[1])
//            break
//        case "patch":
//            split[2]=++Integer.parseInt(split[2])
//            break
//    }
//    return split.join('.')
//}
//
