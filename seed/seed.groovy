job("${SEED_PROJECT}-${SEED_BRANCH}-build") {
   description "Building the ${BRANCH} branch."
   parameters {
      stringParam('COMMIT', 'HEAD', 'Commit to build')
   }
   scm {
      git {
         remote {
             url PROJECT_SCM_URL
             branch '${COMMIT}'
         }
         extensions {
             wipeOutWorkspace()
             localBranch BRANCH
         }
      }
   }
   steps {
      shell "Look! I'm building ${BRANCH}!"
   }
}
