def UPSTREAM_PROJECTS_LIST = []

Map pipelineParams = [ "upstreamProjects" : UPSTREAM_PROJECTS_LIST.join(','),
                       "mavenAdditionalArgs" : " -f jaxb-ri/pom.xml ",
                       "devBranchesRegex" : "mule-master(-\\d+\\.\\d+\\.\\d+)?",
                       "enableAllureTestReportStage" : false
]

runtimeProjectsBuild(pipelineParams)
