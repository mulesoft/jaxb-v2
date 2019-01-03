def UPSTREAM_PROJECTS_LIST = []

Map pipelineParams = [ "upstreamProjects" : UPSTREAM_PROJECTS_LIST.join(','),
                       "mavenAdditionalArgs" : " -f jaxb-ri/pom.xml ",
                       "devBranchesRegex" : "mule-master|mule-master-2.3.1",
                       "enableAllureTestReportStage" : false
]

runtimeProjectsBuild(pipelineParams)
