node {
    checkout scm

    echo "${env.BRANCH_NAME}"
    echo "${env.BUILD_NUMBER}"

    stage 'Compile'
    sh './gradlew assemble'

    stage 'Unit test'
    sh './gradlew check'

    stage 'Integration test'
    sh './gradlew integrationTest'

    stage 'Functional test'
    sh './gradlew functionalTest'

    stage 'Merge'
    build 'Participant-microservice-merge'
}