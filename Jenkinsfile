#!groovy

COMMIT_ID = ""
FALLBACK_RELEASE_VERSION = ""
SELECTED_SEMANTIC_VERSION_UPDATE = ""
DAYS_TO_KEEP_BUILDS = "1"
NUMBER_OF_BUILDS_TO_KEEP = "10"
REPOSITORY_URL="git@github.com:mahanhz/participant-microservice.git"

properties([[$class: 'BuildDiscarderProperty', strategy:
            [$class: 'LogRotator', artifactDaysToKeepStr: '',
             artifactNumToKeepStr: '', daysToKeepStr: DAYS_TO_KEEP_BUILDS, numToKeepStr: NUMBER_OF_BUILDS_TO_KEEP]],
            [$class: 'ThrottleJobProperty', categories: [], limitOneJobWithMatchingParams: false,
             maxConcurrentPerNode: 0, maxConcurrentTotal: 0, paramsToUseForLimit: '',
             throttleEnabled: false, throttleOption: 'project']])

stage ('Build') {
    node {
        checkout scm

        gradle 'clean test assemble'

        stash excludes: 'build/', includes: '**', name: 'source'
        stash includes: 'build/jacoco/*.exec', name: 'unitCodeCoverage'
        // step([$class: 'JUnitResultArchiver', testResults: '**/build/test-results/*.xml'])

        // Obtaining commit id like this until JENKINS-26100 is implemented
        // See http://stackoverflow.com/questions/36304208/jenkins-workflow-checkout-accessing-branch-name-and-git-commit
        sh 'git rev-parse HEAD > commit'
        COMMIT_ID = readFile('commit').trim()

        // Custom environment variable (e.g. for display in Spring Boot manage info page)
        env.GIT_COMMIT_ID = COMMIT_ID

        FALLBACK_RELEASE_VERSION = releaseVersion()
    }
}

if (!isMasterBranch()) {
    stage ('Integration test') {
        node {
            unstash 'source'
            sh 'chmod 755 gradlew'
            sh 'SPRING_PROFILES_ACTIVE=online,test ./gradlew integrationTest'

            stash includes: 'build/jacoco/*.exec', name: 'integrationCodeCoverage'
        }
    }

    stage ('Functional test') {
        node {
            unstash 'source'
            sh 'chmod 755 gradlew'
            gradle 'functionalTest'

            stash includes: 'build/jacoco/*.exec', name: 'functionalCodeCoverage'
        }
    }

    stage ('Code coverage') {
        node {
            unstash 'source'
            unstash 'unitCodeCoverage'
            unstash 'integrationCodeCoverage'
            unstash 'functionalCodeCoverage'

            sh 'chmod 755 gradlew'
            gradle 'jacocoTestReport'

            publishHTML(target: [reportDir:'build/reports/jacoco/test/html', reportFiles: 'index.html', reportName: 'Code Coverage'])
            // step([$class: 'JacocoPublisher', execPattern:'build/jacoco/*.exec', classPattern: 'build/classes/main', sourcePattern: 'src/main/java'])
        }
    }

    // one at a time!
    lock('lock-merge') {
        stage ('Merge') {
            node {
                checkout scm: [$class: 'GitSCM',
                               branches: [[name: '*/master']],
                               doGenerateSubmoduleConfigurations: false,
                               extensions: [[$class: 'LocalBranch', localBranch: 'master'], [$class: 'WipeWorkspace']],
                               submoduleCfg: [],
                               userRemoteConfigs: [[url: REPOSITORY_URL]]]

                sh "git merge ${COMMIT_ID}"
                sh "git push origin master"
            }
        }
    }
}

if (isMasterBranch()) {
    // one at a time!
    lock('lock-publish-snapshot') {
        stage ('Publish snapshot') {
            node {
                unstash 'source'
                sh 'chmod 755 gradlew'

                gradle 'assemble uploadArchives'
            }
        }
    }

    stage ('Approve RC?') {
        timeout(time: 1, unit: 'DAYS') {
            SELECTED_SEMANTIC_VERSION_UPDATE =
                    input message: 'Publish release candidate?',
                            parameters: [[$class: 'ChoiceParameterDefinition',
                                          choices: 'patch\nminor\nmajor',
                                          description: 'Determine the semantic version to release',
                                          name: '']]
        }
    }

    // one at a time!
    lock('lock-publish-release-candidate') {
        stage ('Publish RC') {
            node {
                sh "git branch -a -v --no-abbrev"

                checkout scm: [$class: 'GitSCM',
                               branches: [[name: '*/master']],
                               doGenerateSubmoduleConfigurations: false,
                               extensions: [[$class: 'LocalBranch', localBranch: 'master'], [$class: 'WipeWorkspace']],
                               submoduleCfg: [],
                               userRemoteConfigs: [[url: REPOSITORY_URL]]]

                stash includes: 'gradle.properties', name: 'masterProperties'

                unstash 'source'
                unstash 'masterProperties'

                def script = "scripts/release/participant_release.sh"
                sh "chmod 755 " + script
                sh 'chmod 755 gradlew'

                sh "./" + script + " ${SELECTED_SEMANTIC_VERSION_UPDATE} ${FALLBACK_RELEASE_VERSION}"
            }
        }
    }
}

def releaseVersion() {
    def props = readProperties file: 'gradle.properties'
    def version = props['version']

    if (version.contains('-SNAPSHOT')) {
        version = version.replaceFirst('-SNAPSHOT', '')
    }

    return version
}

def isMasterBranch() {
    return env.BRANCH_NAME == "master"
}

void gradle(String tasks, String switches = null) {
    String gradleCommand = "";
    gradleCommand += './gradlew '
    gradleCommand += tasks

    if(switches != null) {
        gradleCommand += ' '
        gradleCommand += switches
    }

    sh gradleCommand.toString()
}