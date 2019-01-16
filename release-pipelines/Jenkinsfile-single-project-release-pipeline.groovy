#!groovy​

properties([
        parameters([
                string(name: 'repo_branch_from_param', defaultValue: 'mule-master-2.3.1', description: '''The branch from the release is going to be done. <b>IMPORTANT</b> doing the release from the "master" branch is not supported.
                                                                                                
                                                                                              For a new minor/major version it is needed to create a new ".x" (1.0.x, 1.1.x, etc) and use that branch. 
                                                                                                
                                                                                              Check this doc for more details: <a href="https://docs.google.com/document/d/17oVKSEqZY4hPi9yB0CvlEqTPEfDrJbsvxP9AFiBgJcI/edit#heading=h.bukd5eto4253">Extensions Release Doc</a>'''),
                string(name: 'repo_version_to_param', defaultValue: '2.3.1-MULE-001', description: 'Version to release'),
                string(name: 'new_dev_version_in_from_branch_param', defaultValue: '2.3.1-MULE-002-SNAPSHOT', description: 'Next version to use in the dev branch'),
                booleanParam(name: 'tagRelease', defaultValue: true, description: '<hr>'),
                choice(name: 'slack_channel_param', choices: getDefaultChoiceSlackChannelsList().join("\n"), description: 'Slack channel to send the job notifications'),
                booleanParam(name: 'send_notification_on_completion_param', defaultValue: true, description: 'To send a Slack notification to the channel selected in the next paremeter when the extension was succesful released.'),
                choice(name: 'slack_channel_on_completion_param', choices: getDefaultChoiceSlackChannelsOnCompletionList().join("\n"), description: 'The slack channel to use for the notification when the whole pipeline has finished and the Extension Release has finished.<hr>'),
                booleanParam(name: 'dry_run_param', defaultValue: false, description: 'Change the place to deploy to use the test nexus instace used for dry-runs.'),
                string(name: 'pipeline_branch_param', defaultValue: '4.x', description: 'mule-runtime-release repo branch where the Jenkins file is going to be use'),
        ]),
        buildDiscarder(logRotator(artifactDaysToKeepStr: '14', artifactNumToKeepStr: '3', daysToKeepStr: '60', numToKeepStr: '')),
        [$class: 'GithubProjectProperty', displayName: '', projectUrlStr: 'https://github.com/mulesoft/mule-runtime-release/'],
])


slack_channel = "mule-4-test-release"
slack_user = ""
node('docker||ubuntu-14.04||ubuntu-18.04') {

    // Using the wrap BuildUser without a node fails with MissingContextVariableException: Required context class hudson.FilePath is missing
    wrap([$class: 'BuildUser']) {
        echo slack_user

        slack_user = "@" + "${env.BUILD_USER_ID}".replace("@mulesoft.com", "")
    }

}

String jobsNamePrefix = ""
String jobsNameSuffix = ""

if(isJenkinsOnPrem()){
    jobsNamePrefix = getMuleRuntimeReleaseJobFolderPreffix()
    jobsNameSuffix = "/${pipeline_branch_param}"
}

try {

    notifySlack()

    def repo_branch_from_arg = repo_branch_from_param

    def repo_branch_to_arg = repo_version_to_param
    def repo_version_to_arg = repo_version_to_param
    def new_dev_version_in_from_branch_arg = new_dev_version_in_from_branch_param

    def slack_channel_arg = slack_channel_param
    def slack_channel_on_completion_arg = slack_channel_on_completion_param
    def send_notification_on_completion_arg = send_notification_on_completion_param
    def pipeline_branch_arg = pipeline_branch_param

    def dry_run_arg = "${dry_run_param}".toBoolean()

    currentBuild.displayName = "${BUILD_NUMBER}: 'jaxb-v2' - version: '${repo_version_to_arg}'"

    stage('Release Jaxb Artifacts') {

        build job: jobsNamePrefix + 'Mule-4-Single-Project-Release-Pipeline' + jobsNameSuffix,
                parameters: [string(name: 'repo_name_param', value: "jaxb-v2"),
                             string(name: 'repo_branch_from_param', value: "${repo_branch_from_arg}"),
                             booleanParam(name: "update_version_param", value: true),
                             string(name: 'repo_version_to_param', value: "${repo_version_to_arg}"),
                             string(name: 'new_dev_version_in_from_branch_param', value: "${new_dev_version_in_from_branch_arg}"),
                             booleanParam(name: 'update_deps_versions_param', value: false),
                             booleanParam(name: 'skipTests', value: true),
                             booleanParam(name: 'tagRelease', value: false),
                             string(name: 'maven_additional_args_param', value: "-Prelease-profile -Dgpg.skip=true"),
                             booleanParam(name: 'deploy_to_alt_repo_param', value: false),
                             string(name: 'slack_channel_param', value: "${slack_channel_arg}"),
                             booleanParam(name: 'send_notification_on_completion_param', value: false),
                             string(name: 'slack_channel_on_completion_param', value: "${slack_channel_on_completion_arg}"),
                             booleanParam(name: 'use_different_branch_param', value: false),
                             string(name: 'repo_branch_to_param', value: ""),
                             string(name: 'custom_pom_path_param', value: "jaxb-ri/pom.xml"),
                             string(name: 'custom_release_artifacts_script_param', value: ""),
                             string(name: 'custom_update_version_script_param', value: getMuleRuntimeReleaseJobFolderPreffix() + "jaxb-v2-Update-Version/mule-master-2.3.1-release"),
                             string(name: 'custom_jdk_label_param', value: "JDK11-MANUAL-INSTALL"),
                             booleanParam(name: 'delete_branch_release_param', value: true),
                             booleanParam(name: 'copy_artifacts_to_releases_ee_repo_param', value: false),
                             booleanParam(name: 'send_reminder_to_add_release_notes_param', value: false),

                             booleanParam(name: 'dry_run_param', value: false),
                             string(name: 'pipeline_branch_param', value: pipeline_branch_arg)]


    }

    if("${send_notification_on_completion_arg}".toBoolean()){
        slackSend(channel: "${slack_channel_on_completion_arg}", color: 'good', message: "SUCCESS: `${env.JOB_NAME}` ${currentBuild.displayName}:\n${env.BUILD_URL}")
    }


} catch (e) {
    currentBuild.result = 'FAILURE'
    throw e
} finally {

    notifySlack(currentBuild.result)
}

def waitForInput(String projectName, String message, String slackChannel = "${slack_channel}") {
    stage("Wait for ${projectName}") {
        slackSend(channel: "${slackChannel}", color: "good", message: "${message}")

        input id: "release-${projectName.replaceAll(" ", "-")}", message: "Is ${projectName} Released?"

    }
}

